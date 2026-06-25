package Nucleo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class BitMapBloques {

    private boolean[] bloques;

    // Datos para lectura y escritura del bitmap.
    private static int tam_bloque = 4096; // 4KB
    private static int inicio_bit_map = 4; // Posicion en la que empieza el bit map.
    private static int fin_bit_map = 10; // Posicion en la que termina el bit map.

    public BitMapBloques(int total) {
        bloques = new boolean[total];

    }

    public boolean[] get_bloques() {
        return bloques;
    }

    public int buscar_libre() {
        for (int i = 0; i < bloques.length; i++) {
            if (!bloques[i]) {
                return i;
            }
        }
        return -1; // no hay libres
    }

    public void marcar_ocupado(int indice) {
        bloques[indice] = true;
    }

    public void marcar_libre(int indice) {
        bloques[indice] = false;
    }

    public byte[] serializar() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(bloques.length);
        int byteCount = (bloques.length + 7) / 8;
        for (int i = 0; i < byteCount; i++) {
            byte b = 0;
            for (int bit = 0; bit < 8; bit++) {
                int idx = i * 8 + bit;
                if (idx < bloques.length && bloques[idx]) {
                    b |= (1 << bit);
                }
            }
            dos.writeByte(b);
        }
        return baos.toByteArray();
    }

    public static BitMapBloques deserializar(byte[] datos) throws IOException {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(datos));
        int total = dis.readInt();
        BitMapBloques bm = new BitMapBloques(total);
        int byteCount = (total + 7) / 8;
        for (int i = 0; i < byteCount; i++) {
            byte b = dis.readByte();
            for (int bit = 0; bit < 8; bit++) {
                int idx = i * 8 + bit;
                if (idx < total) {
                    bm.bloques[idx] = ((b >> bit) & 1) == 1;
                }
            }
        }
        return bm;
    }

    /**
     * Lee los datos del bitmap desde los bloques reservados (ej. 4–10).
     * 
     * @param archivo      RandomAccessFile abierto en modo "r" o "rw"
     * @param inicioBloque Bloque inicial reservado para el bitmap
     * @param finBloque    Bloque final reservado para el bitmap
     * @param tam_bloque   Tamaño de bloque en bytes
     * @return Array de bytes con los datos del bitmap
     */
    public static byte[] leerBitmap(RandomAccessFile archivo) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = inicio_bit_map; i <= fin_bit_map; i++) {
            archivo.seek(i * tam_bloque);
            byte[] buffer = new byte[tam_bloque];
            archivo.read(buffer);
            baos.write(buffer);
        }
        return baos.toByteArray();
    }

    /**
     * Escribe los datos del bitmap en los bloques reservados (ej. 4–10).
     * 
     * @param archivo      RandomAccessFile abierto en modo "rw"
     * @param datosBM      Array de bytes serializado del bitmap
     * @param inicioBloque Bloque inicial reservado para el bitmap (ej. 4)
     * @param finBloque    Bloque final reservado para el bitmap (ej. 10)
     * @param tam_bloque   Tamaño de bloque en bytes
     */
    public static void escribirBitmap(RandomAccessFile archivo, byte[] datosBM) throws IOException {

        int bloquesReservados = (fin_bit_map - inicio_bit_map + 1);
        int capacidad = bloquesReservados * tam_bloque;
        if (datosBM.length > capacidad) {
            throw new IOException("Bitmap demasiado grande para los bloques reservados (" +
                    datosBM.length + " bytes, capacidad " + capacidad + ")");
        }

        // Escribir fragmentado en los bloques reservados
        int offset = 0;
        for (int i = 0; i < bloquesReservados; i++) {
            archivo.seek((inicio_bit_map + i) * tam_bloque);
            int bytesRestantes = datosBM.length - offset;
            int bytesAEscribir = Math.min(tam_bloque, bytesRestantes);
            if (bytesAEscribir > 0) {
                archivo.write(datosBM, offset, bytesAEscribir);
                offset += bytesAEscribir;
            }
            // Rellenar con ceros si sobra espacio en el bloque
            if (bytesAEscribir < tam_bloque) {
                archivo.write(new byte[tam_bloque - bytesAEscribir]);
            }
        }
    }

}

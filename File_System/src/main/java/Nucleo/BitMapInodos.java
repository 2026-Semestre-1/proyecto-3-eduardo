package Nucleo;

import java.io.*;
import java.util.*;

public class BitMapInodos {
    private boolean[] inodos;

    // Datos para lectura y escritura del bitmap
    private static int tam_bloque = 4096; // 4KB
    private static int inicio_bit_map = 14; // bloque inicial reservado para inodos
    private static int fin_bit_map = 15; // bloque final reservado para inodos

    public BitMapInodos(int total) {
        inodos = new boolean[total];
    }

    public void set_cant_inodos(int cant_inodos) {
        this.inodos = new boolean[cant_inodos];
    }

    public boolean[] get_inodos() {
        return inodos;
    }

    public int buscar_libre() {
        for (int i = 0; i < inodos.length; i++) {
            if (!inodos[i]) {
                return i;
            }
        }
        return -1; // no hay libres
    }

    public void marcar_ocupado(int indice) {
        inodos[indice] = true;
    }

    public void marcar_libre(int indice) {
        inodos[indice] = false;
    }

    public byte[] serializar() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(inodos.length);
        int byteCount = (inodos.length + 7) / 8;
        for (int i = 0; i < byteCount; i++) {
            byte b = 0;
            for (int bit = 0; bit < 8; bit++) {
                int idx = i * 8 + bit;
                if (idx < inodos.length && inodos[idx]) {
                    b |= (1 << bit);
                }
            }
            dos.writeByte(b);
        }
        return baos.toByteArray();
    }

    public static BitMapInodos deserializar(byte[] datos) throws IOException {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(datos));
        int total = dis.readInt();
        BitMapInodos bm = new BitMapInodos(total);
        int byteCount = (total + 7) / 8;
        for (int i = 0; i < byteCount; i++) {
            byte b = dis.readByte();
            for (int bit = 0; bit < 8; bit++) {
                int idx = i * 8 + bit;
                if (idx < total) {
                    bm.inodos[idx] = ((b >> bit) & 1) == 1;
                }
            }
        }
        return bm;
    }

    // Leer bitmap desde bloques 14–15
    public static byte[] leer_bitmap(RandomAccessFile archivo) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = inicio_bit_map; i <= fin_bit_map; i++) {
            archivo.seek(i * tam_bloque);
            byte[] buffer = new byte[tam_bloque];
            archivo.read(buffer);
            baos.write(buffer);
        }
        return baos.toByteArray();
    }

    // Escribir bitmap en bloques 14–15
    public static void escribir_bitmap(RandomAccessFile archivo, byte[] datosBM) throws IOException {
        int bloquesReservados = (fin_bit_map - inicio_bit_map + 1);
        int capacidad = bloquesReservados * tam_bloque;
        if (datosBM.length > capacidad) {
            throw new IOException("Bitmap de inodos demasiado grande (" +
                    datosBM.length + " bytes, capacidad " + capacidad + ")");
        }

        int offset = 0;
        for (int i = 0; i < bloquesReservados; i++) {
            archivo.seek((inicio_bit_map + i) * tam_bloque);
            int bytesRestantes = datosBM.length - offset;
            int bytesAEscribir = Math.min(tam_bloque, bytesRestantes);
            if (bytesAEscribir > 0) {
                archivo.write(datosBM, offset, bytesAEscribir);
                offset += bytesAEscribir;
            }
            if (bytesAEscribir < tam_bloque) {
                archivo.write(new byte[tam_bloque - bytesAEscribir]);
            }
        }
    }
}

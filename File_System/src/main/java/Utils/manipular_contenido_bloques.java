package Utils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class manipular_contenido_bloques {

    /**
     * Escribe contenido en un bloque de datos.
     * 
     * @param archivo    RandomAccessFile abierto en modo "rw"
     * @param bloqueNum  Número de bloque donde se guarda el contenido
     * @param contenido  Texto a escribir en el bloque
     * @param tam_bloque Tamaño de bloque en bytes
     */
    public static void escribirBloque(RandomAccessFile archivo, int bloqueNum,
            String contenido, int tam_bloque) throws IOException {
        long posicion = (long) bloqueNum * tam_bloque;
        archivo.seek(posicion);
        byte[] datos = contenido.getBytes(StandardCharsets.UTF_8);
        if (datos.length > tam_bloque) {
            throw new IOException("Contenido demasiado grande para el bloque (" + datos.length + " bytes).");
        }
        archivo.write(datos);
        if (datos.length < tam_bloque) {
            archivo.write(new byte[tam_bloque - datos.length]); // rellenar con ceros
        }
    }

    /**
     * Lee contenido de un bloque de datos.
     * 
     * @param archivo    RandomAccessFile abierto en modo "r" o "rw"
     * @param bloqueNum  Número de bloque donde está el contenido
     * @param tam_bloque Tamaño de bloque en bytes
     * @return Texto leído del bloque
     */
    public static String leerBloque(RandomAccessFile archivo, int bloqueNum,
            int tam_bloque) throws IOException {
        long posicion = (long) bloqueNum * tam_bloque;
        archivo.seek(posicion);
        byte[] buffer = new byte[tam_bloque];
        archivo.read(buffer);
        return new String(buffer, StandardCharsets.UTF_8).trim();
    }

}

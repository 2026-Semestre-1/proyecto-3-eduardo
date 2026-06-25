package Directorios;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Inodo {

    public String nombre;
    public String propietario;
    public String grupo;
    public boolean es_directorio;
    public List<Integer> bloques_asignados; // ahora soporta múltiples bloques

    public static int tam_bloque = 4096;

    // Adicionales.
    // fecha creacion.
    // Fecha modificacion.
    // Fecha acceso.
    // id del inodo padre.
    // tamaño utilizado:
    // Donde empieza.
    // Cuenta total de bloques.

    public Inodo(String nombre, String propietario, String grupo, boolean es_directorio,
            List<Integer> bloques_asignados) {
        this.nombre = nombre;
        this.propietario = propietario;
        this.grupo = grupo;
        this.es_directorio = es_directorio;
        this.bloques_asignados = new ArrayList<>(bloques_asignados);
    }

    // Serialización binaria consistente
    public byte[] serializar() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeUTF(nombre);
            dos.writeUTF(propietario);
            dos.writeUTF(grupo);
            dos.writeBoolean(es_directorio);

            // Guardar cantidad de bloques y luego cada bloque
            dos.writeInt(bloques_asignados.size());
            for (int bloque : bloques_asignados) {
                dos.writeInt(bloque);
            }

            dos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al serializar inodo: " + e.getMessage(), e);
        }
    }

    // Deserialización binaria simétrica
    public static Inodo deserializar(byte[] datos) throws IOException {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(datos));
        try {
            String nombre = dis.readUTF();
            String propietario = dis.readUTF();
            String grupo = dis.readUTF();
            boolean es_directorio = dis.readBoolean();

            int cantidadBloques = dis.readInt();
            List<Integer> bloques = new ArrayList<>();
            for (int i = 0; i < cantidadBloques; i++) {
                bloques.add(dis.readInt());
            }

            Inodo nuevo = new Inodo(nombre, propietario, grupo, es_directorio, bloques);
            // nuevo.mostrarInodo();
            return nuevo;
        } catch (EOFException e) {
            throw new IOException("Datos insuficientes al leer inodo.", e);
        }
    }

    /**
     * Lee un inodo desde el bloque indicado.
     * 
     * @param archivo    RandomAccessFile abierto en modo "r" o "rw"
     * @param inodoNum   Número de inodo (bloque donde se guarda)
     * @param tam_bloque Tamaño de bloque en bytes
     * @return Objeto Inodo deserializado
     */
    public static Inodo leerInodo(RandomAccessFile archivo, int inodoNum) throws IOException {
        long posicion = (long) inodoNum * tam_bloque;
        archivo.seek(posicion);
        int longitud = archivo.readInt();
        if (longitud <= 0 || longitud > tam_bloque - 4) {
            throw new IOException("Inodo inválido o corrupto (longitud=" + longitud + ")");
        }
        byte[] buffer = new byte[longitud];
        archivo.readFully(buffer);
        return Inodo.deserializar(buffer);
    }

    /**
     * Escribe un inodo en el bloque indicado.
     * 
     * @param archivo    RandomAccessFile abierto en modo "rw"
     * @param inodoNum   Número de inodo (bloque donde se guarda)
     * @param inodo      Objeto Inodo a escribir
     * @param tam_bloque Tamaño de bloque en bytes
     */
    public static void escribirInodo(RandomAccessFile archivo, int inodoNum, Inodo inodo) throws IOException {
        long posicion = (long) inodoNum * tam_bloque;
        archivo.seek(posicion);
        byte[] datos = inodo.serializar();
        archivo.writeInt(datos.length); // longitud primero
        archivo.write(datos); // luego los datos
    }

    public void mostrarInodo() {
        System.out.println("Nombre: " + nombre);
        System.out.println("Propietario: " + propietario);
        System.out.println("Grupo: " + grupo);
        System.out.println("Es directorio: " + es_directorio);
        System.out.println("Bloques asignados: " + bloques_asignados);
    }
}

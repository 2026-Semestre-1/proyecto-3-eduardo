package Directorios;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
// import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Inodo {

    public int numero; // identificador único del inodo
    public String nombre; // nombre del archivo/directorio
    public int propietario; // UID del propietario
    public int grupo; // GID del grupo
    public boolean es_directorio; // tipo: true = directorio, false = archivo
    public List<Integer> bloques_asignados; // soporta múltiples bloques

    // Metadatos
    public long fecha_creacion; // timestamp en ms
    public long fecha_modificacion; // timestamp en ms
    public long fecha_acceso; // timestamp en ms
    public int inodo_padre; // id del inodo padre
    public long tamano_utilizado; // bytes usados en el archivo
    public int enlaces; // contador de enlaces duros
    public int permisos;

    // Observaciones adicionales:
    // La cantidad de bloques asignados lo obtenemos de la lista.

    public static int tam_bloque = 4096; // Tamaño del bloque.

    /**
     * Nombre: Inodo
     * 
     * Descripcion: Constructor de la clase Inodo, para los casos de creacion de un
     * nuevo Inodo.
     * 
     * @param numero
     * @param nombre
     * @param propietario
     * @param grupo
     * @param es_directorio
     * @param bloques_asignados
     * @param inodo_padre
     * @param permisos
     */
    public Inodo(int numero, String nombre, int propietario, int grupo,
            boolean es_directorio, List<Integer> bloques_asignados,
            int inodo_padre, int permisos) {
        this.numero = numero;
        this.nombre = nombre;
        this.propietario = propietario;
        this.grupo = grupo;
        this.es_directorio = es_directorio;
        this.bloques_asignados = new ArrayList<>(bloques_asignados);

        long ahora = System.currentTimeMillis();
        this.fecha_creacion = ahora;
        this.fecha_modificacion = ahora;
        this.fecha_acceso = ahora;

        this.inodo_padre = inodo_padre;
        this.tamano_utilizado = 0;
        this.enlaces = 1; // al crear, siempre hay al menos una referencia
        this.permisos = permisos;
    }

    // Constructor para LEER un inodo desde disco (ya con valores)
    public Inodo(int numero, String nombre, int propietario, int grupo,
            boolean es_directorio, List<Integer> bloques_asignados,
            long fecha_creacion, long fecha_modificacion, long fecha_acceso,
            int inodo_padre, long tamano_utilizado, int enlaces, int permisos) {
        this.numero = numero;
        this.nombre = nombre;
        this.propietario = propietario;
        this.grupo = grupo;
        this.es_directorio = es_directorio;
        this.bloques_asignados = new ArrayList<>(bloques_asignados);

        this.fecha_creacion = fecha_creacion;
        this.fecha_modificacion = fecha_modificacion;
        this.fecha_acceso = fecha_acceso;

        this.inodo_padre = inodo_padre;
        this.tamano_utilizado = tamano_utilizado;
        this.enlaces = enlaces;
        this.permisos = permisos;
    }

    // Serialización binaria consistente
    public byte[] serializar() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            // Basicos
            dos.writeInt(numero);
            dos.writeUTF(nombre);
            dos.writeInt(propietario); // UID
            dos.writeInt(grupo); // GID
            dos.writeBoolean(es_directorio);

            // Bloques asignados
            dos.writeInt(bloques_asignados.size());
            for (int bloque : bloques_asignados) {
                dos.writeInt(bloque);
            }

            // Metadatos
            dos.writeLong(fecha_creacion);
            dos.writeLong(fecha_modificacion);
            dos.writeLong(fecha_acceso);
            dos.writeInt(inodo_padre);
            dos.writeLong(tamano_utilizado);
            dos.writeInt(enlaces);
            dos.writeInt(permisos);

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
            // Básicos
            int numero = dis.readInt();
            String nombre = dis.readUTF();
            int propietario = dis.readInt();
            int grupo = dis.readInt();
            boolean es_directorio = dis.readBoolean();

            // Bloques asignados
            int cantidadBloques = dis.readInt();
            List<Integer> bloques = new ArrayList<>();
            for (int i = 0; i < cantidadBloques; i++) {
                bloques.add(dis.readInt());
            }

            // Metadatos
            long fecha_creacion = dis.readLong();
            long fecha_modificacion = dis.readLong();
            long fecha_acceso = dis.readLong();
            int inodo_padre = dis.readInt();
            long tamano_utilizado = dis.readLong();
            int enlaces = dis.readInt();
            int permisos = dis.readInt();

            // Usamos el constructor de lectura
            return new Inodo(numero, nombre, propietario, grupo,
                    es_directorio, bloques,
                    fecha_creacion, fecha_modificacion, fecha_acceso,
                    inodo_padre, tamano_utilizado, enlaces, permisos);
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

    // ############## Seccion para las funciones de modificacion de datos
    // especificos.
    // Actualizar fecha de acceso
    public static void actualizarFechaAcceso(RandomAccessFile archivo, int inodoNum, long nuevaFecha)
            throws IOException {
        Inodo inodo = leerInodo(archivo, inodoNum);
        inodo.fecha_acceso = nuevaFecha;
        escribirInodo(archivo, inodoNum, inodo);
    }

    // Actualizar fecha de modificación
    public static void actualizarFechaModificacion(RandomAccessFile archivo, int inodoNum, long nuevaFecha)
            throws IOException {
        Inodo inodo = leerInodo(archivo, inodoNum);
        inodo.fecha_modificacion = nuevaFecha;
        escribirInodo(archivo, inodoNum, inodo);
    }

    // Actualizar propietario (UID)
    public static void actualizarPropietario(RandomAccessFile archivo, int inodoNum, int nuevoUid) throws IOException {
        Inodo inodo = leerInodo(archivo, inodoNum);
        inodo.propietario = nuevoUid;
        escribirInodo(archivo, inodoNum, inodo);
    }

    // Actualizar grupo (GID)
    public static void actualizarGrupo(RandomAccessFile archivo, int inodoNum, int nuevoGid) throws IOException {
        Inodo inodo = leerInodo(archivo, inodoNum);
        inodo.grupo = nuevoGid;
        escribirInodo(archivo, inodoNum, inodo);
    }

    // Actualizar tamaño utilizado
    public static void actualizarTamano(RandomAccessFile archivo, int inodoNum, long nuevoTamano) throws IOException {
        Inodo inodo = leerInodo(archivo, inodoNum);
        inodo.tamano_utilizado = nuevoTamano;
        escribirInodo(archivo, inodoNum, inodo);
    }

    // Actualizar contador de enlaces
    public static void actualizarEnlaces(RandomAccessFile archivo, int inodoNum, int nuevoEnlaces) throws IOException {
        Inodo inodo = leerInodo(archivo, inodoNum);
        inodo.enlaces = nuevoEnlaces;
        escribirInodo(archivo, inodoNum, inodo);
    }

    // Actualizar permisos (si decides añadirlos como int o string)
    public static void actualizarPermisos(RandomAccessFile archivo, int inodoNum, int nuevosPermisos)
            throws IOException {
        Inodo inodo = leerInodo(archivo, inodoNum);
        // suponiendo que agregues un campo permisos
        inodo.permisos = nuevosPermisos;
        escribirInodo(archivo, inodoNum, inodo);
    }

    // Actualizar nombre del archivo/directorio
    public static void actualizarNombre(RandomAccessFile archivo, int inodoNum, String nuevoNombre) throws IOException {
        Inodo inodo = leerInodo(archivo, inodoNum);
        inodo.nombre = nuevoNombre;
        escribirInodo(archivo, inodoNum, inodo);
    }

    // Actualizar lista completa de bloques asignados
    public static void actualizarBloquesAsignados(RandomAccessFile archivo, int inodoNum, List<Integer> nuevosBloques)
            throws IOException {
        Inodo inodo = leerInodo(archivo, inodoNum);
        inodo.bloques_asignados = new ArrayList<>(nuevosBloques);
        escribirInodo(archivo, inodoNum, inodo);
    }

    // Agregar un bloque adicional al inodo
    public static void agregarBloque(RandomAccessFile archivo, int inodoNum, int bloque) throws IOException {
        Inodo inodo = leerInodo(archivo, inodoNum);
        inodo.bloques_asignados.add(bloque);
        escribirInodo(archivo, inodoNum, inodo);
    }

    // Eliminar un bloque especifico del inodo
    public static void eliminarBloque(RandomAccessFile archivo, int inodoNum, int bloque) throws IOException {
        Inodo inodo = leerInodo(archivo, inodoNum);
        inodo.bloques_asignados.remove(Integer.valueOf(bloque));
        escribirInodo(archivo, inodoNum, inodo);
    }

    public void mostrarInodo() {
        System.out.println("===== DEBUG INODO " + numero + " =====");
        System.out.println("Nombre: " + nombre);
        System.out.println("Propietario: " + propietario);
        System.out.println("Grupo: " + grupo);
        System.out.println("Es directorio: " + es_directorio);
        System.out.println("Bloques asignados: " + bloques_asignados);
        System.out.println("Fecha creacion: " + fecha_creacion);
        System.out.println("Fecha modificacion: " + fecha_modificacion);
        System.out.println("Fecha acceso: " + fecha_acceso);
        System.out.println("Inodo padre: " + inodo_padre);
        System.out.println("Tamano utilizado: " + tamano_utilizado);
        System.out.println("Enlaces: " + enlaces);
        System.out.println("Permisos: " + permisos);
        System.out.println("=====================================");
    }
}

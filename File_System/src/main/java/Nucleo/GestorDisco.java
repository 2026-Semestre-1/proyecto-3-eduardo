package Nucleo;

import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import Nucleo.MasterBootRecorder.MBR;
import Nucleo.MasterBootRecorder.ParticionBoot;
import Usuarios.GestorUsuarios;
import Directorios.Inodo;

public class GestorDisco {

    private String ruta;
    private final int tam_bloque = 4096; // fijo interno

    private static int cwd_inodo = 5; // Inoddo del directorio actual, por defecto apunta al root.

    public GestorDisco(String ruta) {
        this.ruta = ruta;

    }

    public int getCwdInodo() {
        return cwd_inodo;
    }

    public void setCwdInodo(int cwd_inodo) {
        this.cwd_inodo = cwd_inodo;
    }

    public void formatear_disco(int tam_mb) throws IOException {
        int tam_bytes = tam_mb * 1024 * 1024;
        try (RandomAccessFile archivo = new RandomAccessFile(ruta, "rw")) {
            archivo.setLength(tam_bytes);

            // Escribir MBR
            MBR mbr_obj = new MBR(tam_bytes);
            archivo.seek(0);
            archivo.write(mbr_obj.serializar());

            // Escribir partición de arranque
            ParticionBoot boot = new ParticionBoot();
            archivo.seek(tam_bloque);
            archivo.write(boot.serializar());

            // Inicializar superbloque
            SuperBloque sb = new SuperBloque(tam_bytes, tam_bloque);
            archivo.seek(2 * tam_bloque);
            archivo.write(sb.serializar());

            // Inicializar bitmap
            BitMapBloques bm = new BitMapBloques(sb.bloques_totales);

            // Reservar bloques de gestión (0–100)
            for (int i = 0; i <= 100; i++) {
                bm.marcar_ocupado(i);
            }

            // Asignar bloque de datos 101 al root
            int bloque_root_datos = 101;
            bm.marcar_ocupado(bloque_root_datos);

            // Ajustar superbloque
            sb.bloques_libres = sb.bloques_totales - 102; // reservados 0–101
            archivo.seek(2 * tam_bloque);
            archivo.write(sb.serializar());

            // Guardar bitmap actualizado
            archivo.seek(4 * tam_bloque);
            archivo.write(bm.serializar());
            System.out.println(
                    "Bitmap inicializado con " + sb.bloques_totales + " bloques, libres: " + sb.bloques_libres);

            // Crear inodo root en bloque 5
            Inodo root = new Inodo("root", "root", "root", true, bloque_root_datos);
            archivo.seek(5 * tam_bloque);
            archivo.write(root.serializar());

            // Inicializar contenido del root
            String contenidoRoot = ".;dir;5\n..;dir;5\n";
            archivo.seek(bloque_root_datos * tam_bloque);
            archivo.write(contenidoRoot.getBytes(StandardCharsets.UTF_8));

            // Crear usuario root
            GestorUsuarios usuarios = new GestorUsuarios();
            usuarios.crear_usuario_root();
        }
    }

    public void mostrar_info() {
        try (RandomAccessFile archivo = new RandomAccessFile(ruta, "r")) {
            // El superbloque está en el bloque 2
            archivo.seek(2 * tam_bloque);
            byte[] buffer = new byte[tam_bloque];
            archivo.read(buffer);

            SuperBloque sb = SuperBloque.deserializar(buffer);

            System.out.println("Nombre del FileSystem: " + sb.nombre_fs);
            System.out.println("Tamaño: " + (sb.tam_bytes / (1024 * 1024)) + " MB");
            System.out.println("Bloques totales: " + sb.bloques_totales);
            System.out.println("Bloques libres: " + sb.bloques_libres);
            System.out.println("Bloques ocupados: " + (sb.bloques_totales - sb.bloques_libres));
        } catch (Exception e) {
            System.out.println("Error al leer superbloque: " + e.getMessage());
        }
    }

    public void crear_directorio(String nombre, int inodoPadre) throws IOException {
        try (RandomAccessFile archivo = new RandomAccessFile(ruta, "rw")) {
            // Leer superbloque
            archivo.seek(2 * tam_bloque);
            byte[] buffer = new byte[tam_bloque];
            archivo.read(buffer);
            SuperBloque sb = SuperBloque.deserializar(buffer);

            // Asignar bloque libre para contenido
            int bloque_contenido = asignar_bloque_libre();
            if (bloque_contenido < 0)
                throw new IOException("No hay bloques libres disponibles.");
            sb.bloques_libres--;

            // Buscar inodo libre en tabla
            int indice_inodo = buscar_inodo_libre();
            if (indice_inodo < 0)
                throw new IOException("No hay inodos libres disponibles.");
            int inodoNuevo = 5 + indice_inodo;

            // Crear inodo y escribirlo en tabla
            Inodo nuevo = new Inodo(nombre, "root", "root", true, bloque_contenido);
            archivo.seek(inodoNuevo * tam_bloque);
            archivo.write(nuevo.serializar());

            // Inicializar contenido del nuevo directorio
            String contenido = ".;dir;" + inodoNuevo + "\n" +
                    "..;dir;" + inodoPadre + "\n";
            archivo.seek(bloque_contenido * tam_bloque);
            archivo.write(contenido.getBytes(StandardCharsets.UTF_8));

            // Añadir entrada en el directorio padre
            archivo.seek(inodoPadre * tam_bloque);
            byte[] bufferPadre = new byte[tam_bloque];
            archivo.read(bufferPadre);
            Inodo padre = Inodo.deserializar(bufferPadre);

            archivo.seek(padre.bloque_asignado * tam_bloque);
            byte[] bufferContenidoPadre = new byte[tam_bloque];
            archivo.read(bufferContenidoPadre);
            String contenidoPadre = new String(bufferContenidoPadre, StandardCharsets.UTF_8).trim();
            contenidoPadre += nombre + ";dir;" + inodoNuevo + "\n";

            archivo.seek(padre.bloque_asignado * tam_bloque);
            archivo.write(contenidoPadre.getBytes(StandardCharsets.UTF_8));

            // Actualizar superbloque
            archivo.seek(2 * tam_bloque);
            archivo.write(sb.serializar());

            System.out.println("Directorio creado: " + nombre + " (inodo " + inodoNuevo + ")");
        }
    }

    public int asignar_bloque_libre() throws IOException {
        try (RandomAccessFile archivo = new RandomAccessFile(ruta, "rw")) {
            System.out.println("Buscando bloque libre...");
            // El bitmap esta en el bloque 4
            archivo.seek(4 * tam_bloque);

            // Leer el tamaño total de bloques primero
            int total = archivo.readInt();
            // Calculamos el tamaño esperado: 4 bytes del int + total booleans
            int longitud = 4 + total;
            byte[] buffer = new byte[longitud];

            // Ya leímos el primer int, lo incluimos en el buffer manualmente
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeInt(total);

            // Leer el resto de los booleanos
            byte[] resto = new byte[total];
            archivo.readFully(resto);
            dos.write(resto);

            buffer = baos.toByteArray();

            System.out.println("pass 1");
            BitMapBloques bm = BitMapBloques.deserializar(buffer);
            System.out.println("pass 2");

            int bloque_libre = bm.buscar_libre();
            if (bloque_libre == -1) {
                throw new IOException("No hay bloques libres disponibles.");
            }
            System.out.println("pass 3");

            bm.marcar_ocupado(bloque_libre);
            System.out.println("pass 4");

            // Guardar bitmap actualizado
            archivo.seek(4 * tam_bloque);
            archivo.write(bm.serializar());
            System.out.println("Bloque asignado: " + bloque_libre);

            return bloque_libre;
        }
    }

    public void listar_directorio_actual(int inodoActual) throws IOException {
        try (RandomAccessFile archivo = new RandomAccessFile(ruta, "r")) {
            // Obtener inodo del directorio actual
            archivo.seek(inodoActual * tam_bloque);
            byte[] bufferInodo = new byte[tam_bloque];
            archivo.read(bufferInodo);
            Inodo dirActual = Inodo.deserializar(bufferInodo);

            // Leer contenido del bloque asignado
            archivo.seek(dirActual.bloque_asignado * tam_bloque);
            byte[] buffer = new byte[tam_bloque];
            archivo.read(buffer);

            String contenido = new String(buffer, StandardCharsets.UTF_8).trim();
            if (contenido.isEmpty()) {
                System.out.println("Directorio vacío.");
                return;
            }

            String[] entradas = contenido.split("\n");
            for (String entrada : entradas) {
                if (!entrada.isBlank()) {
                    String[] partes = entrada.split(";");
                    if (partes.length >= 2) {
                        String nombre = partes[0];
                        String tipo = partes[1];
                        if (partes.length == 3) {
                            System.out.println((tipo.equals("dir") ? "[DIR] " : "[FILE] ") + nombre + " (inodo "
                                    + partes[2] + ")");
                        } else {
                            System.out.println((tipo.equals("dir") ? "[DIR] " : "[FILE] ") + nombre);
                        }
                    } else {
                        System.out.println("Entrada inválida: " + entrada);
                    }
                }
            }
        }
    }

    public int buscar_inodo_libre() throws IOException {
        try (RandomAccessFile archivo = new RandomAccessFile(ruta, "rw")) {
            for (int i = 0; i < 100; i++) {
                archivo.seek((5 + i) * tam_bloque);
                byte[] buffer = new byte[tam_bloque];
                archivo.read(buffer);

                boolean vacio = true;
                for (byte b : buffer) {
                    if (b != 0) {
                        vacio = false;
                        break;
                    }
                }
                if (vacio)
                    return i; // bloque de inodo libre
            }
        }
        return -1;
    }

    public void debug_dump_bitmap(int cantidad) throws IOException {
        try (RandomAccessFile archivo = new RandomAccessFile(ruta, "r")) {
            archivo.seek(4 * tam_bloque);

            // Leer el total de bloques
            int total = archivo.readInt();
            BitMapBloques bm = new BitMapBloques(total);

            // Leer todos los booleanos
            for (int i = 0; i < total; i++) {
                bm.marcar_libre(i); // inicializamos
                bm.get_bloques()[i] = archivo.readBoolean();
            }

            // Mostrar los primeros 'cantidad' bloques
            System.out.println("=== DEBUG BITMAP ===");
            for (int i = 0; i < cantidad && i < total; i++) {
                System.out.println("Bloque " + i + ": " + (bm.get_bloques()[i] ? "Ocupado (1)" : "Libre (0)"));
            }
            System.out.println("====================");
        }
    }

}

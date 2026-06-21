package Nucleo;

import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.io.IOException;
import Nucleo.MasterBootRecorder.MBR;
import Nucleo.MasterBootRecorder.ParticionBoot;
import Usuarios.GestorUsuarios;
import Directorios.Inodo;

public class GestorDisco {

    private String ruta;
    private final int tam_bloque = 4096; // fijo interno

    public GestorDisco(String ruta) {
        this.ruta = ruta;
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

            // Inicializar bitmap con todos libres
            BitMapBloques bm = new BitMapBloques(sb.bloques_totales);
            /// byte[] bmBytes = bm.serializar();
            archivo.seek(4 * tam_bloque);
            // archivo.writeInt(bmBytes.length); // primero escribimos la longitud real
            // archivo.write(bmBytes); // luego los datos binarios
            archivo.write(bm.serializar());
            System.out.println("Bitmap inicializado con " + sb.bloques_totales + " bloques libres.");

            // Crear usuario root
            GestorUsuarios usuarios = new GestorUsuarios();
            usuarios.crear_usuario_root();
        }
    }

    // public void mostrar_info() {
    // File f = new File(ruta);
    // if (f.exists()) {
    // long tam_bytes = f.length();
    // long tam_mb = tam_bytes / (1024 * 1024);
    // System.out.println("Nombre del FileSystem: miFS");
    // System.out.println("Tamaño: " + tam_mb + " MB");

    // // Por ahora espacio usado = 0, disponible = total
    // System.out.println("Espacio utilizado: 0 MB");
    // System.out.println("Disponible: " + tam_mb + " MB");
    // } else {
    // System.out.println("El disco no existe.");
    // }
    // }

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

    public void crear_directorio(String nombre) throws IOException {
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

            // Crear inodo y escribirlo en tabla
            Inodo nuevo = new Inodo(nombre, "root", "root", true, bloque_contenido);
            archivo.seek((5 + indice_inodo) * tam_bloque);
            archivo.write(nuevo.serializar());

            // Inicializar contenido del directorio
            String contenido = ".;dir;" + indice_inodo + "\n" +
                    "..;dir;" + 0 + "\n"; // 0 = root
            archivo.seek(bloque_contenido * tam_bloque);
            archivo.write(contenido.getBytes(StandardCharsets.UTF_8));

            // Actualizar superbloque
            archivo.seek(2 * tam_bloque);
            archivo.write(sb.serializar());
        }
    }

    public int asignar_bloque_libre() throws IOException {
        try (RandomAccessFile archivo = new RandomAccessFile(ruta, "rw")) {
            System.out.println("Buscando bloque libre...");
            // El bitmap está en el bloque 4
            // archivo.seek(4 * tam_bloque);
            // int longitud = archivo.readInt(); // recuperamos la longitud real
            // byte[] buffer = new byte[longitud];
            // archivo.readFully(buffer);

            archivo.seek(4 * tam_bloque);
            byte[] buffer = new byte[tam_bloque];
            archivo.read(buffer);

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
            System.out.println("pass 5");
            archivo.write(bm.serializar());
            System.out.println("Bloque asignado: " + bloque_libre);
            return bloque_libre;
        }
    }

    public void listar_directorio_actual() throws IOException {
        try (RandomAccessFile archivo = new RandomAccessFile(ruta, "r")) {
            // Obtener inodo del directorio actual (ejemplo: root en bloque 5)
            archivo.seek(5 * tam_bloque);
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
                    String nombre = partes[0];
                    String tipo = partes[1];
                    System.out.println((tipo.equals("dir") ? "[DIR] " : "[FILE] ") + nombre);
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

}

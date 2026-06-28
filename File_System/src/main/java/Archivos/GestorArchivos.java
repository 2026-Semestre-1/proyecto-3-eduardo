package Archivos;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import Utils.manipular_contenido_bloques;

public class GestorArchivos {
    private static final int BLOQUE_TABLA = 19; // bloque reservado
    private List<Archivo> tabla;
    private int tam_bloque = 4096;

    public GestorArchivos() {
        tabla = new ArrayList<>();
    }

    // Abrir archivo
    public void abrir_archivo(RandomAccessFile archivo, int inodo, String modo, int uid, int gid) throws IOException {
        Archivo entrada = new Archivo(inodo, modo, uid, gid);
        tabla.add(entrada);
        guardar_tabla(archivo);
        System.out.println("Archivo abierto: inodo " + inodo + " en modo " + modo);
    }

    // Cerrar archivo
    public void cerrar_archivo(RandomAccessFile archivo, int inodo) throws IOException {
        for (Archivo e : tabla) {
            if (e.get_inodo() == inodo && e.is_activo()) {
                e.cerrar();
                break;
            }
        }
        guardar_tabla(archivo);
        System.out.println("Archivo cerrado: inodo " + inodo);
    }

    // Guardar tabla en bloque reservado
    private void guardar_tabla(RandomAccessFile archivo) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(tabla);
        oos.close();

        byte[] datos = baos.toByteArray();
        manipular_contenido_bloques.escribirBloque(archivo, BLOQUE_TABLA,
                new String(datos, StandardCharsets.ISO_8859_1), tam_bloque);
    }

    // Cargar tabla desde bloque reservado
    public void cargar_tabla(RandomAccessFile archivo) throws IOException, ClassNotFoundException {
        String bloque = manipular_contenido_bloques.leerBloque(archivo, BLOQUE_TABLA, tam_bloque);
        byte[] datos = bloque.getBytes(StandardCharsets.ISO_8859_1);

        ByteArrayInputStream bais = new ByteArrayInputStream(datos);
        ObjectInputStream ois = new ObjectInputStream(bais);
        tabla = (List<Archivo>) ois.readObject();
        ois.close();
    }

    public List<Archivo> get_tabla() {
        return tabla;
    }

    // Buscar archivo abierto
    public Archivo buscar_archivo(int inodo) {
        for (Archivo e : tabla) {
            if (e.get_inodo() == inodo && e.is_activo()) {
                return e;
            }
        }
        return null;
    }
}

package Usuarios;

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

public class GestorGrupos {
    private static final int BLOQUE_GRUPOS = 13; // bloque reservado para grupos
    private List<Grupo> lista_grupos;
    private int tam_bloque = 4096;

    public GestorGrupos() {
        lista_grupos = new ArrayList<>();
    }

    // Crear grupo con GID automático
    public int crear_grupo(RandomAccessFile archivo, String nombre) throws IOException {
        for (Grupo g : lista_grupos) {
            if (g.get_nombre().equals(nombre)) {
                throw new IOException("El grupo ya existe: " + nombre);
            }
        }

        int nuevo_gid = obtener_siguiente_gid();
        Grupo nuevo = new Grupo(nuevo_gid, nombre);
        lista_grupos.add(nuevo);

        guardar_grupos(archivo);
        System.out.println("Grupo creado: " + nombre + " (GID=" + nuevo_gid + ")");

        return nuevo_gid;
    }

    // Obtener el siguiente GID disponible
    private int obtener_siguiente_gid() {
        int max_gid = 0;
        for (Grupo g : lista_grupos) {
            if (g.get_gid() > max_gid) {
                max_gid = g.get_gid();
            }
        }
        return max_gid + 1;
    }

    // Guardar lista de grupos en bloque reservado
    private void guardar_grupos(RandomAccessFile archivo) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(lista_grupos);
        oos.close();

        byte[] datos = baos.toByteArray();
        if (datos.length > tam_bloque) {
            throw new IOException("No hay espacio suficiente en el bloque reservado para grupos.");
        }

        manipular_contenido_bloques.escribirBloque(
                archivo,
                BLOQUE_GRUPOS,
                new String(datos, StandardCharsets.ISO_8859_1),
                tam_bloque);
    }

    // Cargar lista de grupos desde bloque reservado
    public void cargar_grupos(RandomAccessFile archivo) throws IOException, ClassNotFoundException {
        String bloque = manipular_contenido_bloques.leerBloque(archivo, BLOQUE_GRUPOS, tam_bloque);
        byte[] datos = bloque.getBytes(StandardCharsets.ISO_8859_1);

        ByteArrayInputStream bais = new ByteArrayInputStream(datos);
        ObjectInputStream ois = new ObjectInputStream(bais);
        lista_grupos = (List<Grupo>) ois.readObject();
        ois.close();
    }

    // Buscar grupo por nombre
    public Grupo buscar_grupo(String nombre) {
        for (Grupo g : lista_grupos) {
            if (g.get_nombre().equals(nombre)) {
                return g;
            }
        }
        return null;
    }

    // Agregar usuario a grupo
    public void agregar_usuario_a_grupo(String nombre_grupo, int uid) throws IOException {
        Grupo g = buscar_grupo(nombre_grupo);
        if (g == null) {
            throw new IOException("Grupo no encontrado: " + nombre_grupo);
        }
        g.agregar_miembro(uid);
    }

    // Eliminar usuario de grupo
    public void eliminar_usuario_de_grupo(String nombre_grupo, int uid) throws IOException {
        Grupo g = buscar_grupo(nombre_grupo);
        if (g == null) {
            throw new IOException("Grupo no encontrado: " + nombre_grupo);
        }
        g.eliminar_miembro(uid);
    }
}

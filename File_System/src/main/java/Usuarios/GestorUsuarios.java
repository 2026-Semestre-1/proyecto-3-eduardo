package Usuarios;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Utils.manipular_contenido_bloques;

public class GestorUsuarios {

    private static final int BLOQUE_USUARIOS_1 = 11;
    private static final int BLOQUE_USUARIOS_2 = 12;

    private List<Usuario> usuarios;

    private int tamBloque = 4096;

    public GestorUsuarios() {
        usuarios = new ArrayList<>();

    }

    // Crear usuario
    public int crear_usuario(RandomAccessFile archivo, int gid, String nombre, String contrasena,
            boolean privilegiado, boolean activo) throws IOException {
        // Verificar que no exista
        for (Usuario u : usuarios) {
            if (u.getNombre().equals(nombre)) {
                throw new IOException("El usuario ya existe: " + nombre);
            }
        }

        int nuevo_uid = obtener_siguiente_uid();
        Usuario nuevo = new Usuario(nuevo_uid, gid, nombre, contrasena, privilegiado, activo);
        usuarios.add(nuevo);

        // Guardar lista en bloques reservados
        guardar_usuarios(archivo);
        System.out.println(
                "Usuario creado: " + nombre + " (UID=" + nuevo_uid + ", GID=" + gid + ", Activo=" + activo + ")");

        return nuevo_uid;
    }

    // Guardar lista de usuarios en bloques 11 y 12
    private void guardar_usuarios(RandomAccessFile archivo) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(usuarios);
        oos.close();

        byte[] datos = baos.toByteArray();

        // Dividir en fragmentos de tamaño de bloque
        int totalBloquesNecesarios = (int) Math.ceil((double) datos.length / tamBloque);
        if (totalBloquesNecesarios > 2) {
            throw new IOException("No hay espacio suficiente en los bloques reservados para usuarios.");
        }

        for (int i = 0; i < totalBloquesNecesarios; i++) {
            int inicio = i * tamBloque;
            int fin = Math.min(datos.length, inicio + tamBloque);
            byte[] fragmento = Arrays.copyOfRange(datos, inicio, fin);

            manipular_contenido_bloques.escribirBloque(
                    archivo,
                    BLOQUE_USUARIOS_1 + i,
                    new String(fragmento, StandardCharsets.ISO_8859_1), // guardamos como texto binario
                    tamBloque);
        }
    }

    // Cargar lista de usuarios desde bloques 11 y 12
    public void cargar_usuarios(RandomAccessFile archivo) throws IOException, ClassNotFoundException {
        String bloque1 = manipular_contenido_bloques.leerBloque(archivo, BLOQUE_USUARIOS_1, tamBloque);
        String bloque2 = manipular_contenido_bloques.leerBloque(archivo, BLOQUE_USUARIOS_2, tamBloque);

        String datosCompletos = bloque1 + bloque2;
        byte[] datos = datosCompletos.getBytes(StandardCharsets.ISO_8859_1);

        ByteArrayInputStream bais = new ByteArrayInputStream(datos);
        ObjectInputStream ois = new ObjectInputStream(bais);
        usuarios = (List<Usuario>) ois.readObject();
        ois.close();
    }

    // Buscar usuario por nombre
    public Usuario buscar_usuario(String nombre) {
        for (Usuario u : usuarios) {
            if (u.getNombre().equals(nombre)) {
                return u;
            }
        }
        return null;
    }

    public Usuario buscar_usuario_activo() {
        for (Usuario u : usuarios) {
            if (u.isActivo()) {
                return u;
            }
        }
        return null;
    }

    // Obtener el siguiente UID disponible
    private int obtener_siguiente_uid() {
        int max_uid = 0;
        for (Usuario u : usuarios) {
            if (u.getUid() > max_uid) {
                max_uid = u.getUid();
            }
        }
        return max_uid + 1;
    }

    // Obtener el siguiente GID disponible
    // private int obtener_siguiente_gid() {
    // int max_gid = 0;
    // for (Usuario u : usuarios) {
    // if (u.getGid() > max_gid) {
    // max_gid = u.getGid();
    // }
    // }
    // return max_gid + 1;
    // }

}

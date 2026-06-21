package Directorios;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Inodo {

    public String nombre;
    public String propietario;
    public String grupo;
    public boolean es_directorio;
    public int bloque_asignado;

    public Inodo(String nombre, String propietario, String grupo, boolean es_directorio, int bloque_asignado) {
        this.nombre = nombre;
        this.propietario = propietario;
        this.grupo = grupo;
        this.es_directorio = es_directorio;
        this.bloque_asignado = bloque_asignado;
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
            dos.writeInt(bloque_asignado);
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
            int bloque_asignado = dis.readInt();
            return new Inodo(nombre, propietario, grupo, es_directorio, bloque_asignado);
        } catch (EOFException e) {
            throw new IOException("Datos insuficientes al leer inodo.", e);
        }
    }
}

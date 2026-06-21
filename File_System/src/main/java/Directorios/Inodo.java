package Directorios;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
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

    public byte[] serializar() {
        String data = nombre + ";" + propietario + ";" + grupo + ";" + es_directorio + ";" + bloque_asignado;
        return data.getBytes(StandardCharsets.UTF_8);
    }

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
            throw new IOException("Datos insuficientes al leer inodo.");
        }
    }
}

package Nucleo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SuperBloque {

    public String nombre_fs = "miFS";
    public int tam_bytes;
    public int tam_bloque;
    public int bloques_totales;
    public int bloques_libres;

    public SuperBloque(int tam_bytes, int tam_bloque) {
        this.tam_bytes = tam_bytes;
        this.tam_bloque = tam_bloque;
        this.bloques_totales = tam_bytes / tam_bloque;
        this.bloques_libres = bloques_totales; // al inicio todos libres
    }

    public byte[] serializar() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeUTF(nombre_fs);
        dos.writeInt(tam_bytes);
        dos.writeInt(tam_bloque);
        dos.writeInt(bloques_totales);
        dos.writeInt(bloques_libres);
        return baos.toByteArray();
    }

    public static SuperBloque deserializar(byte[] datos) throws IOException {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(datos));
        String nombre_fs = dis.readUTF();
        int tam_bytes = dis.readInt();
        int tam_bloque = dis.readInt();
        int bloques_totales = dis.readInt();
        int bloques_libres = dis.readInt();

        SuperBloque sb = new SuperBloque(tam_bytes, tam_bloque);
        sb.nombre_fs = nombre_fs;
        sb.bloques_totales = bloques_totales;
        sb.bloques_libres = bloques_libres;
        return sb;
    }

}

package Nucleo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BitMapBloques {

    private boolean[] bloques;

    public BitMapBloques(int total) {
        bloques = new boolean[total];

        // Marcamos como ocupados los primeros bloques que serian reservados para
        // elementos importantes
        // bloques[0] = true; // MBR
        // bloques[1] = true; // Boot
        // bloques[2] = true; // Superbloque
        // bloques[3] = true; // (extra reservado)
        // bloques[4] = true; // Bitmap
        // // Reservar tabla de inodos (ejemplo: 5–100)
        // for (int i = 5; i <= 100; i++) {
        // bloques[i] = true;
        // }
    }

    public boolean[] get_bloques() {
        return bloques;
    }

    public int buscar_libre() {
        for (int i = 0; i < bloques.length; i++) {
            if (!bloques[i]) {
                return i;
            }
        }
        return -1; // no hay libres
    }

    public void marcar_ocupado(int indice) {
        bloques[indice] = true;
    }

    public void marcar_libre(int indice) {
        bloques[indice] = false;
    }

    public byte[] serializar() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(bloques.length);
        for (boolean b : bloques) {
            dos.writeBoolean(b);
        }
        return baos.toByteArray();
    }

    public static BitMapBloques deserializar(byte[] datos) throws IOException {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(datos));
        int total = dis.readInt();
        BitMapBloques bm = new BitMapBloques(total);
        for (int i = 0; i < total; i++) {
            bm.bloques[i] = dis.readBoolean();
        }
        return bm;
    }

}

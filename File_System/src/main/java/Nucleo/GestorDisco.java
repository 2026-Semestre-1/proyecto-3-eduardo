package Nucleo;

import java.io.RandomAccessFile;
import java.io.IOException;
import Nucleo.MasterBootRecorder.MBR;
import Nucleo.MasterBootRecorder.ParticionBoot;
import Usuarios.GestorUsuarios;

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

            // Inicializar superbloque y bitmap
            SuperBloque sb = new SuperBloque(tam_bytes, tam_bloque);
            archivo.seek(2 * tam_bloque);
            archivo.write(sb.serializar());

            BitMapBloques bm = new BitMapBloques(sb.bloques_totales);
            archivo.seek(4 * tam_bloque);
            archivo.write(bm.serializar());

            // Crear usuario root
            GestorUsuarios usuarios = new GestorUsuarios();
            usuarios.crear_usuario_root();
        }
    }

}

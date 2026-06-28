package Terminal.Comandos;

import java.io.RandomAccessFile;

import Archivos.Archivo;
import Archivos.GestorArchivos;
import Nucleo.GestorDisco;

public class comando_viewFilesOpen implements Comando {

    @Override
    public String nombre_comando() {
        return "viewFilesOpen";
    }

    @Override
    public void ejecutar(String[] args) {
        try (RandomAccessFile archivo = new RandomAccessFile(GestorDisco.get_ruta(), "rw")) {
            GestorArchivos taa = new GestorArchivos();

            taa.cargar_tabla(archivo); // cargar la tabla desde bloque reservado

            int total_abiertos = 0;
            for (Archivo e : taa.get_tabla()) {
                if (e.is_activo()) {
                    total_abiertos++;
                }
            }

            System.out.println("Total de archivos abiertos: " + total_abiertos);
        } catch (Exception e) {
            System.out.println("Error al consultar archivos abiertos: " + e.getMessage());
        }
    }
}

package Terminal.Comandos;

import java.io.RandomAccessFile;
import java.util.regex.Pattern;

import Directorios.Inodo;
import Nucleo.GestorDisco;
import Utils.manipular_contenido_bloques;

public class Comando_Rm implements Comando {

    @Override
    public String nombre_comando() {
        return "rm";
    }

    @Override
    public void ejecutar(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: rm [-R] archivo|directorio|patrón");
            return;
        }

        boolean recursivo = false;
        String objetivo;

        if (args[1].equals("-R")) {
            recursivo = true;
            objetivo = args[2];
        } else {
            objetivo = args[1];
        }

        GestorDisco disco = new GestorDisco(GestorDisco.get_ruta());
        disco.procesar_eliminacion(objetivo, recursivo);

    }
}

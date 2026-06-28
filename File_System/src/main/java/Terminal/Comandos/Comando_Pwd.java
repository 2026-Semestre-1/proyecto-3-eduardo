package Terminal.Comandos;

import java.io.RandomAccessFile;

import Nucleo.GestorDisco;

public class Comando_Pwd implements Comando {

    @Override
    public String nombre_comando() {
        return "pwd";
    }

    @Override
    public void ejecutar(String[] args) {

        try {

            GestorDisco disco = new GestorDisco(GestorDisco.get_ruta());
            String ruta = disco.construir_ruta();
            System.out.println(ruta);

        } catch (Exception e) {
            System.out.println("Error en pwd: " + e.getMessage());
        }
    }
}

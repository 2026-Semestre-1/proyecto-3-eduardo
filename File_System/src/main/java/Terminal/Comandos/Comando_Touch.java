package Terminal.Comandos;

import Nucleo.GestorDisco;

public class Comando_Touch implements Comando {
    @Override
    public String nombre_comando() {
        return "touch";
    }

    @Override
    public void ejecutar(String[] args) {
        try {
            GestorDisco disco = new GestorDisco("miDiscoDuro.fs");

            if (args.length > 1) {
                // if (args[0].equals("-R")) {
                // recursivo = true;
                // } else {
                // System.out.println("Comando desconocido: ls " + args[0]);
                // return;
                // }
            }

            disco.crear_archivo(args[1]);

        } catch (Exception e) {
            System.out.println("Error al crear archivo: " + e.getMessage());
        }

    }
}

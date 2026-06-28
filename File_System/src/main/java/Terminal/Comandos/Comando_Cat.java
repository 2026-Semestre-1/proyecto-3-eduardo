package Terminal.Comandos;

import java.io.IOException;

import Nucleo.GestorDisco;

public class Comando_Cat implements Comando {
    @Override
    public String nombre_comando() {
        return "cat";
    }

    @Override
    public void ejecutar(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: cat nombre_archivo");
            return;
        }
        String nombreArchivo = args[1];
        try {
            GestorDisco disco = new GestorDisco(GestorDisco.get_ruta());
            String contenido = disco.leer_archivo(nombreArchivo);
            System.out.println(contenido);
        } catch (IOException e) {
            System.out.println("Error al leer archivo: " + e.getMessage());
        }
    }
}

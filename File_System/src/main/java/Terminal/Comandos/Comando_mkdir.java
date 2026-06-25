package Terminal.Comandos;

import Nucleo.GestorDisco;

public class Comando_mkdir implements Comando {

    @Override
    public String nombre_comando() {
        return "mkdir";
    }

    @Override
    public void ejecutar(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: mkdir nombre_directorio");
            return;
        }
        String nombre = args[1];
        try {
            GestorDisco disco = new GestorDisco("miDiscoDuro.fs");
            disco.crear_directorio(nombre);
            System.out.println("Directorio creado: " + nombre);
        } catch (Exception e) {
            System.out.println("Error al crear directorio: " + e.getMessage());
        }
    }

}

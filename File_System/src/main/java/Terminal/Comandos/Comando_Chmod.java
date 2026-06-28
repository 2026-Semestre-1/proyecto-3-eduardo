package Terminal.Comandos;

import java.util.HashSet;
import java.util.Set;

import Nucleo.GestorDisco;

public class Comando_Chmod implements Comando {

    @Override
    public String nombre_comando() {
        return "chmod";
    }

    @Override
    public void ejecutar(String[] args) {
        if (args.length < 3) {
            System.out.println("Uso: chmod [-R] permisos archivo|directorio");
            return;
        }

        boolean recursivo = false;
        String permisos;
        String objetivo;

        if (args[1].equals("-R")) {
            recursivo = true;
            permisos = args[2];
            objetivo = args[3];
        } else {
            permisos = args[1];
            objetivo = args[2];
        }

        try {
            Set<Integer> visitados = new HashSet<>();
            GestorDisco disco = new GestorDisco(GestorDisco.get_ruta());
            disco.cambiar_permisos(permisos, objetivo, recursivo, visitados);
        } catch (Exception e) {
            System.out.println("Error en chmod: " + e.getMessage());
        }
    }
}

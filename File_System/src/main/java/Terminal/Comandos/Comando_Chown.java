package Terminal.Comandos;

import java.util.HashSet;
import java.util.Set;

import Nucleo.GestorDisco;

public class Comando_Chown implements Comando {

    @Override
    public String nombre_comando() {
        return "chown";
    }

    @Override
    public void ejecutar(String[] args) {
        if (args.length < 3) {
            System.out.println("Uso: chown [-R] username archivo|directorio");
            return;
        }

        boolean recursivo = false;
        String username;
        String objetivo;

        if (args[1].equals("-R")) {
            recursivo = true;
            username = args[2];
            objetivo = args[3];
        } else {
            username = args[1];
            objetivo = args[2];
        }

        try {

            GestorDisco disco = new GestorDisco(GestorDisco.get_ruta());
            Set<Integer> visitados = new HashSet<>();
            disco.cambiar_propietario(username, objetivo, recursivo, visitados);

        } catch (Exception e) {
            System.out.println("Error en chown: " + e.getMessage());
        }
    }
}

package Terminal.Comandos;

import java.util.HashSet;
import java.util.Set;

import Nucleo.GestorDisco;

public class Comando_Chgrp implements Comando {

    @Override
    public String nombre_comando() {
        return "chgrp";
    }

    @Override
    public void ejecutar(String[] args) {
        if (args.length < 3) {
            System.out.println("Uso: chgrp [-R] groupname archivo|directorio");
            return;
        }

        boolean recursivo = false;
        String groupname;
        String objetivo;

        if (args[1].equals("-R")) {
            recursivo = true;
            groupname = args[2];
            objetivo = args[3];
        } else {
            groupname = args[1];
            objetivo = args[2];
        }

        try {
            Set<Integer> visitados = new HashSet<>();
            GestorDisco disco = new GestorDisco(GestorDisco.get_ruta());
            disco.cambiar_grupo(groupname, objetivo, recursivo, visitados);
        } catch (Exception e) {
            System.out.println("Error en chgrp: " + e.getMessage());
        }
    }
}

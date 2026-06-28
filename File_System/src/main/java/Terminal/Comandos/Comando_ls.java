package Terminal.Comandos;

import java.util.HashSet;
import java.util.Set;

import Nucleo.GestorDisco;

public class Comando_ls implements Comando {

    @Override
    public String nombre_comando() {
        return "ls";
    }

    @Override
    public void ejecutar(String[] args) {
        try {
            GestorDisco disco = new GestorDisco(GestorDisco.get_ruta());
            boolean recursivo = false;
            if (args.length > 1) {
                if (args[1].equals("-R")) {
                    recursivo = true;
                } else {
                    System.out.println("Comando desconocido: ls " + args[1]);
                    return;
                }
            }

            Set<Integer> visitados = new HashSet<>();

            disco.listar_directorio_actual(disco.getCwdInodo(), recursivo, visitados);
        } catch (Exception e) {
            System.out.println("Error al listar directorio: " + e.getMessage());
        }
    }

}

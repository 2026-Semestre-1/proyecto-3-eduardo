package Terminal.Comandos;

import Nucleo.GestorDisco;

public class Comando_ls implements Comando {

    @Override
    public String nombre_comando() {
        return "ls";
    }

    @Override
    public void ejecutar(String[] args) {
        try {
            GestorDisco disco = new GestorDisco("miDiscoDuro.fs");
            boolean recursivo = false;
            if (args.length > 1) {
                if (args[0].equals("-R")) {
                    recursivo = true;
                } else {
                    System.out.println("Comando desconocido: ls " + args[0]);
                    return;
                }
            }

            System.out.println(disco.getCwdInodo());
            disco.listar_directorio_actual(disco.getCwdInodo(), recursivo);
        } catch (Exception e) {
            System.out.println("Error al listar directorio: " + e.getMessage());
        }
    }

}

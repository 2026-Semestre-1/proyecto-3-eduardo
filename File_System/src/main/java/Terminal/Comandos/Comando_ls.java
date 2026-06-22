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
            System.out.println(disco.getCwdInodo());
            disco.listar_directorio_actual(disco.getCwdInodo());
        } catch (Exception e) {
            System.out.println("Error al listar directorio: " + e.getMessage());
        }
    }

}

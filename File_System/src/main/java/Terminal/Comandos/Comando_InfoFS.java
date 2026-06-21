package Terminal.Comandos;

import Nucleo.GestorDisco;

public class Comando_InfoFS implements Comando {

    @Override
    public String nombre_comando() {
        return "infoFS";
    }

    @Override
    public void ejecutar(String[] args) {
        try {
            GestorDisco disco = new GestorDisco("miDiscoDuro.fs");
            disco.mostrar_info();
        } catch (Exception e) {
            System.out.println("Error al leer infoFS: " + e.getMessage());
        }
    }

}

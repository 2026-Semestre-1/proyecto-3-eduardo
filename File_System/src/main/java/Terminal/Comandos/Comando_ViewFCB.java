package Terminal.Comandos;

import Nucleo.GestorDisco;

public class Comando_ViewFCB implements Comando {

    @Override
    public String nombre_comando() {
        return "viewFCB";
    }

    @Override
    public void ejecutar(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: viewFCB filename");
            return;
        }

        String nombreArchivo = args[1];
        try {

            GestorDisco disco = new GestorDisco(GestorDisco.get_ruta());
            disco.mostrar_FCB(nombreArchivo);

        } catch (Exception e) {
            System.out.println("Error en viewFCB: " + e.getMessage());
        }
    }
}

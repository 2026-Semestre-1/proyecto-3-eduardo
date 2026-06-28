package Terminal.Comandos;

import Nucleo.GestorDisco;

public class Comando_Mv implements Comando {

    @Override
    public String nombre_comando() {
        return "mv";
    }

    @Override
    public void ejecutar(String[] args) {
        if (args.length < 3) {
            System.out.println("Uso: mv origen destino");
            return;
        }

        String origen = args[1];
        String destino = args[2];

        try {

            GestorDisco disco = new GestorDisco(GestorDisco.get_ruta());

            disco.mover_renombrar(origen, destino);

        } catch (Exception e) {
            System.out.println("Error en mv: " + e.getMessage());
        }
    }
}

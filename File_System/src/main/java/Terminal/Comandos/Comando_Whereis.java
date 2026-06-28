package Terminal.Comandos;

import Nucleo.GestorDisco;

public class Comando_Whereis implements Comando {

    @Override
    public String nombre_comando() {
        return "whereis";
    }

    @Override
    public void ejecutar(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: whereis filename");
            return;
        }

        try {
            String nombreArchivo = args[1];
            GestorDisco disco = new GestorDisco(GestorDisco.get_ruta());
            disco.buscar_archivo_whereis(nombreArchivo);

        } catch (Exception e) {
            System.out.println("Error al ejecutar whereis: " + e.getMessage());
        }

    }
}

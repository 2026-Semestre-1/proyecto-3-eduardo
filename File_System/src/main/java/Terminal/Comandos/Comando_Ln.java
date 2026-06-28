package Terminal.Comandos;

import Nucleo.GestorDisco;

public class Comando_Ln implements Comando {

    @Override
    public String nombre_comando() {
        return "ln";
    }

    @Override
    public void ejecutar(String[] args) {
        if (args.length < 3) {
            System.out.println("Uso: ln nombre_enlace ruta_objetivo");
            return;
        }

        String nombreEnlace = args[1];
        String rutaObjetivo = args[2];

        // Extraer el nombre del archivo de la ruta objetivo
        // String[] partesRuta = rutaObjetivo.split("/");
        // String nombreArchivo = partesRuta[partesRuta.length - 1];

        try {

            GestorDisco disco = new GestorDisco(GestorDisco.get_ruta());

            disco.crear_enlace(nombreEnlace, rutaObjetivo);
        } catch (Exception e) {
            System.out.println("Error al crear enlace: " + e.getMessage());
        }
    }
}

package Terminal.Comandos;

import Nucleo.GestorDisco;

public class Comando_Cd implements Comando {
    @Override
    public String nombre_comando() {
        return "cd";
    }

    @Override
    public void ejecutar(String[] args) {
        try {
            if (args.length < 2) {
                System.out.println("Uso: cd nombre_directorio | cd ..");
                return;
            }

            String nombre_directorio = args[1];
            GestorDisco gestor_disco = new GestorDisco(GestorDisco.get_ruta());
            gestor_disco.navegacion_directorios(nombre_directorio);

        } catch (Exception e) {
            System.out.println("Error al ejecutar el comando cd: " + e.getMessage());
        }

    }
}

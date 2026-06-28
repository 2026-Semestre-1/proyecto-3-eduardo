package Terminal.Comandos;

import java.io.IOException;
import java.util.Scanner;

import Nucleo.GestorDisco;

public class Comando_Less implements Comando {
    @Override
    public String nombre_comando() {
        return "less";
    }

    @Override
    public void ejecutar(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: less nombre_archivo");
            return;
        }
        String nombreArchivo = args[1];
        try {
            GestorDisco disco = new GestorDisco(GestorDisco.get_ruta());

            disco.establecer_archivo_abierto(nombreArchivo, "r");
            String contenido = disco.leer_archivo(nombreArchivo);
            System.out.println(contenido);
            System.out.println("\n--- Presione 'q' para salir ---");

            Scanner sc = new Scanner(System.in);
            while (true) {
                String linea = sc.nextLine().trim();
                if (linea.equals("q")) {
                    disco.cerrar_archivo_abierto(nombreArchivo);
                    break;
                } else {
                    System.out.println("Comando no reconocido. Use 'q' para salir.");
                }
            }
            // sc.close();
        } catch (IOException e) {

            System.out.println("Error al leer archivo: " + e.getMessage());
        }
    }
}

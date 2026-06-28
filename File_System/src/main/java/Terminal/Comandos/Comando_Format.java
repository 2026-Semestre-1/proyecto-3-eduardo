package Terminal.Comandos;

import java.io.File;

import Nucleo.GestorDisco;

public class Comando_Format implements Comando {

    @Override
    public String nombre_comando() {
        return "format";
    }

    @Override
    public void ejecutar(String[] args) {
        System.out.println("Ingrese el tamaño del disco en MB:");
        try {
            // Revisar esto para que no se pueda agregar algo diferente de un numero y que
            // no sea menor a 20.
            java.util.Scanner sc = new java.util.Scanner(System.in);
            int tam_mb = sc.nextInt();

            // Ocupamos que se borre el archivo anterior si existe
            File archivo = new File("miDiscoDuro.fs");
            if (archivo.exists()) {
                archivo.delete();
            }

            // Solicitar el ingreso de la contraseña del usuario root.
            System.out.println("Ingrese la contraseña del usuario root:");
            String contrasena_root = sc.next();

            GestorDisco disco = new GestorDisco("miDiscoDuro.fs");
            disco.formatear_disco(tam_mb, contrasena_root);

            System.out.println("Disco formateado con éxito. Usuario root creado.");

            // sc.close();
        } catch (Exception e) {
            System.out.println("Error al formatear: " + e.getMessage());
        }
    }

}

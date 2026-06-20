package Terminal.Comandos;

import Nucleo.GestorDisco;

public class ComandoFormat implements Comando {

    @Override
    public String nombre_comando() {
        return "format";
    }

    @Override
    public void ejecutar(String[] args) {
        System.out.println("Ingrese el tamaño del disco en MB:");
        try {
            java.util.Scanner sc = new java.util.Scanner(System.in);
            int tam_mb = sc.nextInt();
            GestorDisco disco = new GestorDisco("miDiscoDuro.fs");
            disco.formatear_disco(tam_mb);
            System.out.println("Disco formateado con éxito. Usuario root creado.");

        } catch (Exception e) {
            System.out.println("Error al formatear: " + e.getMessage());
        }
    }

}

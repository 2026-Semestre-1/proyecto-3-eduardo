package Terminal;

import Terminal.Comandos.ComandoFormat;

import Terminal.Comandos.Comando;

import java.util.Scanner;

public class Terminal {

    private String usuario_actual = "root";

    public void iniciar() {
        System.out.println("Bienvenido al sistema de archivos miFS");
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print(usuario_actual + "@miFS: ");
            String linea = sc.nextLine().trim();

            if (linea.equals("exit")) {
                System.out.println("Cerrando sistema...");
                break;
            } else if (linea.startsWith("format")) {
                ComandoFormat cmd = new ComandoFormat();
                cmd.ejecutar(linea.split(" "));
            } else {
                System.out.println("Comando no reconocido: " + linea);
            }
        }
        sc.close();
    }
}

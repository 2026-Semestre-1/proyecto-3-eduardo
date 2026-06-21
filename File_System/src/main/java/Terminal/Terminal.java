package Terminal;

import Terminal.Comandos.*;

import java.util.Scanner;

public class Terminal {

    private String usuario_actual = "root";

    public void iniciar() {
        System.out.println("Bienvenido al sistema de archivos miFS");
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print(usuario_actual + "@miFS: ");
            String linea = sc.nextLine().trim();
            String[] partes = linea.split(" ");
            String comando = partes[0];

            switch (comando) {
                case "exit":
                    System.out.println("Cerrando sistema...");
                    sc.close();
                    return;

                case "format":
                    Comando_Format cmdFormat = new Comando_Format();
                    cmdFormat.ejecutar(partes);
                    break;

                case "infoFS":
                    Comando_InfoFS cmdInfo = new Comando_InfoFS();
                    cmdInfo.ejecutar(partes);
                    break;

                case "mkdir":
                    Comando_mkdir cmdMkdir = new Comando_mkdir();
                    cmdMkdir.ejecutar(partes);
                    break;

                case "ls":
                    Comando_ls cmdLs = new Comando_ls();
                    cmdLs.ejecutar(partes);
                    break;

                default:
                    System.out.println("Comando no reconocido: " + comando);
            }
        }
        // sc.close();
    }
}

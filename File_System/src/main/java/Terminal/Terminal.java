package Terminal;

import Terminal.Comandos.*;
import Nucleo.GestorDisco;

import java.util.Scanner;

public class Terminal {

    private String usuario_actual = "root";

    public void iniciar() {
        System.out.println("Bienvenido al sistema de archivos miFS");
        Scanner sc = new Scanner(System.in);

        while (true) {

            // Primero se tiene que ver si ingreso el disco a usar.
            // Si se ingreso, intentamos usar ese.

            // Si no se ingreso, empezamos el formateo del disco de una vez.

            // Lo primero es buscar al ultimo usuario activo.

            // Despues intentamos se aplica el inicio de sesion para ese usuario.

            // Si es exitoso entonces podemos continiar.

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
                    // debug_dump_bitmap(20);
                    break;

                case "ls":
                    Comando_ls cmdLs = new Comando_ls();
                    cmdLs.ejecutar(partes);
                    break;

                case "touch":
                    Comando_Touch cmdTouch = new Comando_Touch();
                    cmdTouch.ejecutar(partes);
                    break;

                case "cat":
                    Comando_Cat cmdCat = new Comando_Cat();
                    cmdCat.ejecutar(partes);
                    break;

                case "less":
                    Comando_Less cmdLess = new Comando_Less();
                    cmdLess.ejecutar(partes);
                    break;

                case "note":
                    Comando_Note cmdNote = new Comando_Note();
                    cmdNote.ejecutar(partes);
                    break;

                case "useradd":
                    Comando_Useradd cmdUseradd = new Comando_Useradd();
                    cmdUseradd.ejecutar(partes);
                    break;

                case "groupadd":
                    Comando_Groupadd cmdGroupadd = new Comando_Groupadd();
                    cmdGroupadd.ejecutar(partes);
                    break;

                case "usermod":
                    Comando_Usermod cmdUsermod = new Comando_Usermod();
                    cmdUsermod.ejecutar(partes);
                    break;

                default:
                    System.out.println("Comando no reconocido: " + comando);
            }
        }
        // sc.close();
    }

}

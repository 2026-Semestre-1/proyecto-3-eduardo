package Terminal;

import Terminal.Comandos.*;
import Usuarios.GestorUsuarios;
import Usuarios.Usuario;
import Nucleo.GestorDisco;

import java.io.RandomAccessFile;
import java.util.Scanner;

public class Terminal {

    private String usuario_actual = "root";
    GestorDisco disco;

    public void iniciar(String[] args) {
        System.out.println("Bienvenido al sistema de archivos miFS");
        Scanner sc = new Scanner(System.in);

        if (args.length == 0) {

            boolean exito = generar_disco();
            if (exito) {
                boolean sesion = iniciar_sesion();
                if (sesion) {
                    System.out.println("Sesion iniciada");
                } else {
                    sc.close();
                    return;
                }
            }

        } else {

            boolean exito = cargar_disco(args[0]);
            if (exito) {
                boolean sesion = iniciar_sesion();
                if (sesion) {
                    System.out.println("Sesion iniciada");
                } else {
                    sc.close();
                    return;
                }
            }

        }

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

                case "cd":
                    Comando_Cd cmdCd = new Comando_Cd();
                    cmdCd.ejecutar(partes);
                    break;

                case "su":
                    Comando_Su cmdSu = new Comando_Su();
                    cmdSu.ejecutar(partes);
                    usuario_actual = GestorDisco.get_usuario_actual().getNombre();
                    break;

                case "passwd":
                    Comando_Passwd cmdPasswd = new Comando_Passwd();
                    cmdPasswd.ejecutar(partes);
                    break;

                case "whoami":
                    Comando_Whoami cmdWhoami = new Comando_Whoami();
                    cmdWhoami.ejecutar(partes);
                    break;

                case "pwd":
                    Comando_Pwd cmdPwd = new Comando_Pwd();
                    cmdPwd.ejecutar(partes);
                    break;

                case "whereis":
                    Comando_Whereis cmdWhereis = new Comando_Whereis();
                    cmdWhereis.ejecutar(partes);
                    break;

                case "ln":
                    Comando_Ln cmdLn = new Comando_Ln();
                    cmdLn.ejecutar(partes);
                    break;

                default:
                    System.out.println("Comando no reconocido: " + comando);
            }
        }
        // sc.close();
    }

    public boolean generar_disco() {
        // Cuando no se especifica un disco, automa
        try {
            System.out.println("No se especificó disco. Se procederá a formatear uno nuevo.");
            Comando_Format cmdFormat = new Comando_Format();
            cmdFormat.ejecutar(new String[] { "format" });
            // usuario_actual = "root";
            // iniciar_sesion();
            cargar_ultimo_usuario_activo();
            return true;

        } catch (Exception e) {
            System.out.println("Error al generar disco: " + e.getMessage());
            return false;
        }
    }

    public boolean cargar_disco(String nombre_disco) {
        try {
            String nombreDisco = nombre_disco;
            disco = new GestorDisco(nombreDisco);
            cargar_ultimo_usuario_activo();
            return true;

        } catch (Exception e) {
            System.out.println("Error al cargar disco: " + e.getMessage());
            return false;
        }

    }

    public boolean cargar_ultimo_usuario_activo() {
        // Cargar los datos del usuario activo.
        try (RandomAccessFile archivo = new RandomAccessFile(GestorDisco.get_ruta(), "rw")) {
            GestorUsuarios gu = new GestorUsuarios();
            gu.cargar_usuarios(archivo);

            Usuario user = gu.buscar_usuario_activo();
            GestorDisco.set_usuario_actual(user);
            usuario_actual = user.getNombre();

            return true;

        } catch (Exception e) {
            System.out.println("Error al cargar usuarios: " + e.getMessage());
            return false;
        }

    }

    public boolean iniciar_sesion() {
        try {
            Scanner sc = new Scanner(System.in);

            int count = 3;
            while (count != 0) {
                System.out.print(usuario_actual + "@miFS: \n");
                System.out.println("Ingrese su contraseña: ");
                String pass = sc.nextLine().trim();

                if (pass.equals(GestorDisco.get_usuario_actual().getContrasena())) {
                    // sc.close();
                    return true;
                } else {
                    count--;
                }

            }
            System.out.println("Demasiados intentos fallidos");
            sc.close();
            return false;

        } catch (Exception e) {
            System.out.println("Error al iniciar sesion: " + e.getMessage());
            return false;
        }
    }

}

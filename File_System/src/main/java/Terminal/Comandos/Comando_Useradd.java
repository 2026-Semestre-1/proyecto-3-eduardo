package Terminal.Comandos;

import java.io.RandomAccessFile;
import java.util.Scanner;

import Nucleo.GestorDisco;
import Usuarios.GestorGrupos;
import Usuarios.GestorUsuarios;

public class Comando_Useradd implements Comando {
    @Override
    public String nombre_comando() {
        return "useradd";
    }

    @Override
    public void ejecutar(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: useradd nombre_usuario");
            return;
        }

        String nombre_usuario = args[1];
        Scanner sc = new Scanner(System.in);

        System.out.print("Ingrese nombre completo: ");
        String nombre_completo = sc.nextLine();

        System.out.print("Ingrese contraseña: ");
        String contrasena = sc.nextLine();

        System.out.print("Confirme contraseña: ");
        String confirmacion = sc.nextLine();

        if (!contrasena.equals(confirmacion)) {
            System.out.println("Las contraseñas no coinciden.");
            return;
        }

        GestorDisco disco = new GestorDisco("miDiscoDuro.fs");

        try (RandomAccessFile archivo = new RandomAccessFile(disco.get_ruta(), "rw")) {

            // Primero se crear el grupo
            GestorGrupos gg = new GestorGrupos();
            gg.cargar_grupos(archivo);
            int gid = gg.crear_grupo(archivo, nombre_usuario);

            // Luego se crea el usuario.
            GestorUsuarios gu = new GestorUsuarios();
            gu.cargar_usuarios(archivo);
            int uid = gu.crear_usuario(archivo, gid, nombre_completo, nombre_usuario, contrasena, false, false);

            // Agregarmos el usuario a los miembros del grupo.
            gg.agregar_usuario_a_grupo(gid, uid);

            // Aqui se deberia de procesar la creacion de la carpeta del usuario.

        } catch (Exception e) {
            System.out.println("Error al crear usuario: " + e.getMessage());
        }
    }
}

package Terminal.Comandos;

import java.io.RandomAccessFile;
import java.util.Scanner;

import Nucleo.GestorDisco;
import Usuarios.GestorUsuarios;
import Usuarios.Usuario;

public class Comando_Passwd implements Comando {

    @Override
    public String nombre_comando() {
        return "passwd";
    }

    @Override
    public void ejecutar(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: passwd username");
            return;
        }

        String username = args[1];
        Scanner sc = new Scanner(System.in);

        try (RandomAccessFile archivo = new RandomAccessFile(GestorDisco.get_ruta(), "rw")) {
            GestorUsuarios usuarios = new GestorUsuarios();
            usuarios.cargar_usuarios(archivo);
            Usuario usuario = usuarios.buscar_usuario(username);

            if (usuario == null) {
                System.out.println("Usuario no encontrado: " + username);
                return;
            }

            System.out.print("Nueva contrasena: ");
            String pass1 = sc.nextLine();
            System.out.print("Confirmar contrasena: ");
            String pass2 = sc.nextLine();

            if (!pass1.equals(pass2)) {
                System.out.println("Las contraseñas no coinciden.");
                return;
            }

            usuario.setContrasena(pass1);
            usuarios.procesar_guardado(archivo);

            System.out.println("Contraseña actualizada correctamente para " + username);
        } catch (Exception e) {
            System.out.println("Error al cambiar contraseña: " + e.getMessage());
        }
    }
}

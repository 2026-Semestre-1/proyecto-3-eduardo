package Terminal.Comandos;

import java.io.RandomAccessFile;
import java.util.Scanner;

import Nucleo.GestorDisco;
import Usuarios.GestorUsuarios;
import Usuarios.Usuario;

public class Comando_Su implements Comando {

    @Override
    public String nombre_comando() {
        return "su";
    }

    @Override
    public void ejecutar(String[] args) {
        String username = "root"; // por defecto
        if (args.length >= 2) {
            username = args[1];
        }

        Scanner sc = new Scanner(System.in);

        try (RandomAccessFile archivo = new RandomAccessFile(GestorDisco.get_ruta(), "rw")) {
            GestorUsuarios usuarios = new GestorUsuarios();
            usuarios.cargar_usuarios(archivo);

            Usuario usuario = usuarios.buscar_usuario(username);
            if (usuario == null) {
                System.out.println("Usuario no encontrado: " + username);
                return;
            }

            System.out.print("Ingrese contraseña: ");
            String pass = sc.nextLine();

            if (!usuario.comprobar_contrasena(pass)) {
                System.out.println("Contraseña incorrecta.");
                return;
            }

            // Cambiar el estado activo del usuario actual.
            // GestorDisco.get_usuario_actual().setActivo(false);
            Usuario usuario_actual = usuarios.buscar_usuario_activo();
            usuario_actual.setActivo(false);

            // Poner en activo al nuevo usuario.
            usuario.setActivo(true);

            // Guardar los cambios de los usuarios.
            usuarios.procesar_guardado(archivo);

            // usuarios.mostrar_usuarios();

            // Cambiar contexto del usuario actual.
            GestorDisco.set_usuario_actual(usuario);

            // Aqui se deberi de cambiar al directorio del usuario actual, pero queda
            // pendiente.

            System.out.println("Sesión cambiada a usuario " + username);
        } catch (Exception e) {
            System.out.println("Error al cambiar de usuario: " + e.getMessage());
        }
    }
}

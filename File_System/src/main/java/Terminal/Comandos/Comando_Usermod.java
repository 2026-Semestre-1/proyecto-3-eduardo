package Terminal.Comandos;

import java.io.RandomAccessFile;

import Nucleo.GestorDisco;
import Usuarios.GestorGrupos;
import Usuarios.GestorUsuarios;
import Usuarios.Grupo;
import Usuarios.Usuario;

public class Comando_Usermod implements Comando {
    @Override
    public String nombre_comando() {
        return "usermod";
    }

    @Override
    public void ejecutar(String[] args) {
        if (args.length < 3) {
            System.out.println("Uso: usermod -aG nombre_grupo nombre_usuario");
            return;
        }

        String opcion = args[1];
        String nombre_grupo = args[2];
        String nombre_usuario = args[3];

        if (!opcion.equals("-aG")) {
            System.out.println("Opción no soportada. Use: usermod -aG grupo usuario");
            return;
        }

        try (RandomAccessFile archivo = new RandomAccessFile(GestorDisco.get_ruta(), "rw")) {
            GestorUsuarios gu = new GestorUsuarios();
            gu.cargar_usuarios(archivo);

            GestorGrupos gg = new GestorGrupos();
            gg.cargar_grupos(archivo);

            Usuario usuario = gu.buscar_usuario(nombre_usuario);
            if (usuario == null) {
                System.out.println("Usuario no encontrado: " + nombre_usuario);
                return;
            }

            Grupo grupo = gg.buscar_grupo(nombre_grupo);
            if (grupo == null) {
                System.out.println("Grupo no encontrado: " + nombre_grupo);
                return;
            }

            gg.agregar_usuario_a_grupo(grupo.get_gid(), usuario.getUid());
            gg.guardar_grupos_2(archivo);

            // Agregar el grupo a los grupos secundarios del usuario
            gu.agregar_grupo_secundario(archivo, usuario.getUid(), grupo.get_gid());

            System.out.println("Usuario " + nombre_usuario + " agregado al grupo " + nombre_grupo);
        } catch (Exception e) {
            System.out.println("Error al modificar usuario: " + e.getMessage());
        }
    }
}

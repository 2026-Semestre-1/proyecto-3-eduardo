package Terminal.Comandos;

import Nucleo.GestorDisco;
import Usuarios.Usuario;

public class Comando_Whoami implements Comando {

    @Override
    public String nombre_comando() {
        return "whoami";
    }

    @Override
    public void ejecutar(String[] args) {
        try {
            Usuario actual = GestorDisco.get_usuario_actual();
            if (actual == null) {
                System.out.println("No hay sesion activa.");
                return;
            }

            System.out.println("Usuario: " + actual.getNombre());
            System.out.println("Nombre Completo: " + actual.getNombre_completo());

        } catch (Exception e) {
            System.out.println("Error en whoami: " + e.getMessage());
        }
    }
}

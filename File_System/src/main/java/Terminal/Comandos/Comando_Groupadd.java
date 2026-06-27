package Terminal.Comandos;

import java.io.RandomAccessFile;

import Usuarios.GestorGrupos;

public class Comando_Groupadd implements Comando {

    @Override
    public String nombre_comando() {
        return "groupadd";
    }

    @Override
    public void ejecutar(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: groupadd nombre_grupo");
            return;
        }

        String nombre_grupo = args[1];

        try (RandomAccessFile archivo = new RandomAccessFile("miDiscoDuro.fs", "rw")) {
            GestorGrupos gg = new GestorGrupos();
            gg.cargar_grupos(archivo);
            gg.crear_grupo(archivo, nombre_grupo);
        } catch (Exception e) {
            System.out.println("Error al crear grupo: " + e.getMessage());
        }
    }

}

package Usuarios;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Grupo implements Serializable {
    private int gid;
    private String nombre;
    private List<Integer> miembros; // lista de UIDs

    public Grupo(int gid, String nombre) {
        this.gid = gid;
        this.nombre = nombre;
        this.miembros = new ArrayList<>();
    }

    public int get_gid() {
        return gid;
    }

    public String get_nombre() {
        return nombre;
    }

    public List<Integer> get_miembros() {
        return miembros;
    }

    public void agregar_miembro(int uid) {
        if (!miembros.contains(uid)) {
            miembros.add(uid);
        }
    }

    public void eliminar_miembro(int uid) {
        miembros.remove(Integer.valueOf(uid));
    }

    @Override
    public String toString() {
        return "Grupo{" +
                "gid=" + gid +
                ", nombre='" + nombre + '\'' +
                ", miembros=" + miembros +
                '}';
    }
}

package Archivos;

import java.io.Serializable;

public class Archivo implements Serializable {
    private int inodo;
    private String modo; // "r", "w", "rw"
    private int puntero; // posicion actual de lectura/escritura
    private int uid; // usuario propietario de la sesion
    private int gid; // grupo propietario
    private boolean activo;

    public Archivo(int inodo, String modo, int uid, int gid) {
        this.inodo = inodo;
        this.modo = modo;
        this.uid = uid;
        this.gid = gid;
        this.puntero = 0;
        this.activo = true;
    }

    public int get_inodo() {
        return inodo;
    }

    public String get_modo() {
        return modo;
    }

    public int get_puntero() {
        return puntero;
    }

    public void set_puntero(int puntero) {
        this.puntero = puntero;
    }

    public boolean is_activo() {
        return activo;
    }

    public void cerrar() {
        this.activo = false;
    }
}

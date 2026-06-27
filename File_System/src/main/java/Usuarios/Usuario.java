package Usuarios;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Usuario implements Serializable {

    private int uid; // Identificador único del usuario
    private int gid; // Grupo al que pertenece
    private List<Integer> grupos_secundarios; // Lista de todos los grupos a los que pertenece el usuario.
    private String nombre_completo; // Nombre del usuario
    private String nombre; // Nombre de usuario
    private String contrasena; // Contraseña
    private boolean privilegiado; // true si es root o administrador
    private boolean activo; // true si el usuario esta activo

    // Constructor
    public Usuario(int uid, int gid, String nombre_completo, String nombre, String contrasena, boolean privilegiado,
            boolean activo) {
        this.uid = uid;
        this.gid = gid;
        this.nombre_completo = nombre_completo;
        this.nombre = nombre;
        this.contrasena = contrasena;
        this.privilegiado = privilegiado;
        this.activo = activo;
        this.grupos_secundarios = new ArrayList<>();
    }

    // Getters y Setters
    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

    public List<Integer> getGrupos_secundarios() {
        return grupos_secundarios;
    }

    public void setGrupos_secundarios(List<Integer> grupos_secundarios) {
        this.grupos_secundarios = grupos_secundarios;
    }

    public void agregar_grupo_secundario(int gid) {
        this.grupos_secundarios.add(gid);
    }

    public void eliminar_grupo_secundario(int gid) {
        this.grupos_secundarios.remove(gid);
    }

    public String getNombre_completo() {
        return nombre_completo;
    }

    public void setNombre_completo(String nombre_completo) {
        this.nombre_completo = nombre_completo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public boolean isPrivilegiado() {
        return privilegiado;
    }

    public void setPrivilegiado(boolean privilegiado) {
        this.privilegiado = privilegiado;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    // Método toString
    @Override
    public String toString() {
        return "Usuario [uid=" + uid + ", gid=" + gid + ", nombre_completo=" + nombre_completo + ", nombre=" + nombre
                + ", privilegiado="
                + privilegiado + ", activo=" + activo + "]";
    }

}

package Usuarios;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class Usuario implements Serializable {

    private int uid; // Identificador único del usuario
    private int gid; // Grupo al que pertenece
    private String nombre; // Nombre de usuario
    private String contrasena; // Contraseña
    private boolean privilegiado; // true si es root o administrador
    private boolean activo; // true si el usuario esta activo

    // Constructor
    public Usuario(int uid, int gid, String nombre, String contrasena, boolean privilegiado, boolean activo) {
        this.uid = uid;
        this.gid = gid;
        this.nombre = nombre;
        this.contrasena = contrasena;
        this.privilegiado = privilegiado;
        this.activo = activo;
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
        return "Usuario [uid=" + uid + ", gid=" + gid + ", nombre=" + nombre + ", privilegiado="
                + privilegiado + ", activo=" + activo + "]";
    }

    // Metodo para serializar el objeto a bytes
    // public byte[] serializar() {
    // try {
    // ByteArrayOutputStream bos = new ByteArrayOutputStream();
    // DataOutputStream dos = new DataOutputStream(bos);

    // dos.writeInt(uid);
    // dos.writeInt(gid);
    // dos.writeUTF(nombre);
    // dos.writeUTF(contrasena);
    // dos.writeBoolean(privilegiado);

    // return bos.toByteArray();
    // } catch (IOException e) {
    // e.printStackTrace();
    // return new byte[0];
    // }
    // }
}

package Directorios;

public class EntradaDirectorio {

    public String nombre;
    public String tipo; // "dir" o "file"
    public int id_inodo;

    public EntradaDirectorio(String nombre, String tipo, int id_inodo) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.id_inodo = id_inodo;
    }

    public String serializar() {
        return nombre + ";" + tipo + ";" + id_inodo + "\n";
    }

}

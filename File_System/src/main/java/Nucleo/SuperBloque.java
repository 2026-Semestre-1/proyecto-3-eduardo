package Nucleo;

public class SuperBloque {

    public int bloques_totales;

    public SuperBloque(int tam_bytes, int tam_bloque) {
        this.bloques_totales = tam_bytes / tam_bloque;
    }

    public byte[] serializar() {
        return ("Superbloque bloques:" + bloques_totales).getBytes();
    }

}

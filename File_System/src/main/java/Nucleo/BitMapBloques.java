package Nucleo;

public class BitMapBloques {

    private boolean[] bloques;

    public BitMapBloques(int total) {
        bloques = new boolean[total];
    }

    public byte[] serializar() {
        return ("Bitmap total:" + bloques.length).getBytes();
    }

}

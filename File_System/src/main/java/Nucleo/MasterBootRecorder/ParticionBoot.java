package Nucleo.MasterBootRecorder;

public class ParticionBoot {
    public String nombre_volumen = "miFS";
    public int bloque_superbloque;
    public int bloque_bitmap;
    public int bloque_inodos;
    public String mensaje_boot = "Sistema de archivos miFS iniciado correctamente.";

    public byte[] serializar() {
        return "BOOT Partition miFS".getBytes();
    }
}

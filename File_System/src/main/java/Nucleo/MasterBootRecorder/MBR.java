package Nucleo.MasterBootRecorder;

public class MBR {
    public String firma = "MBR_FS";
    public int tam_disco;
    public int inicio_particion_boot;
    public int tam_particion_boot;
    public boolean particion_boot_activa;
    public String tipo_fs = "miFS";

    public MBR(int tam_disco) {
        this.tam_disco = tam_disco;
    }

    public byte[] serializar() {
        return ("MBR tam:" + tam_disco).getBytes();
    }
}

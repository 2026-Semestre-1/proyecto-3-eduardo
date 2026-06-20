package Terminal.Comandos;

public interface Comando {

    public String nombre_comando();

    public void ejecutar(String[] args);
}

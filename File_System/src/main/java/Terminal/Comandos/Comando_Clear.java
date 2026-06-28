package Terminal.Comandos;

public class Comando_Clear implements Comando {

    @Override
    public String nombre_comando() {
        return "clear";
    }

    @Override
    public void ejecutar(String[] args) {

        // Secuencias ASCII, para la limieza de la pantalla.
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}

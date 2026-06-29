package Terminal.Comandos;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

import Directorios.Inodo;
import Nucleo.GestorDisco;
import Utils.manipular_contenido_bloques;

public class Comando_Note implements Comando {
    @Override
    public String nombre_comando() {
        return "note";
    }

    @Override
    public void ejecutar(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: note nombre_archivo");
            return;
        }
        String nombreArchivo = args[1];
        GestorDisco disco = new GestorDisco(GestorDisco.get_ruta());

        try (RandomAccessFile archivo = new RandomAccessFile(GestorDisco.get_ruta(), "rw")) {
            // Buscar el inodo del archivo
            int inodoPadre = disco.getCwdInodo();
            Inodo padre = Inodo.leerInodo(archivo, inodoPadre);
            int bloquePadreDatos = padre.bloques_asignados.get(0);
            String contenidoPadre = manipular_contenido_bloques.leerBloque(archivo, bloquePadreDatos,
                    disco.get_tam_bloque());

            int inodoArchivo = -1;
            String[] entradas = contenidoPadre.split("\n");
            for (String entrada : entradas) {
                if (!entrada.isBlank()) {
                    String[] partes = entrada.split(";");
                    if (partes.length >= 3 && partes[0].equals(nombreArchivo) && partes[1].equals("file")) {
                        inodoArchivo = Integer.parseInt(partes[2]);
                        break;
                    }
                }
            }
            if (inodoArchivo == -1) {
                System.out.println("Archivo no encontrado: " + nombreArchivo);
                return;
            }

            // Leer inodo del archivo
            Inodo archivoInodo = Inodo.leerInodo(archivo, inodoArchivo);
            StringBuilder contenidoActual = new StringBuilder();
            for (int bloque : archivoInodo.bloques_asignados) {
                contenidoActual.append(
                        manipular_contenido_bloques.leerBloque(archivo, bloque, disco.get_tam_bloque()));
            }

            // Verificacion de permisos

            boolean permiso = disco.validar_permisos(archivoInodo, GestorDisco.get_usuario_actual(), "w");

            if (!permiso) {
                System.out.println("No tiene permisos para editar el archivo");
                return;
            }

            System.out.println("\n--- Editor Note ---");
            System.out.println("Contenido actual (editable):");
            System.out.println(contenidoActual.toString());
            System.out.println("Edite el contenido, termine con Ctrl+X en una línea aparte.");

            // disco.establecer_archivo_abierto(nombreArchivo, "rw");

            // Editor interactivo
            Scanner sc = new Scanner(System.in);
            StringBuilder nuevoContenido = new StringBuilder(contenidoActual.toString());
            while (true) {
                String linea = sc.nextLine();
                if (linea.equalsIgnoreCase("\u0018") || linea.equalsIgnoreCase("Ctrl+X")) {
                    // disco.cerrar_archivo_abierto(nombreArchivo);
                    break;
                }
                nuevoContenido.append(linea).append("\n");
            }

            // Preguntar si guardar cambios
            System.out.print("¿Desea guardar los cambios? (s/n): ");
            String respuesta = sc.nextLine().trim().toLowerCase();
            if (respuesta.equals("s")) {

                // manipular_contenido_bloques.escribirBloque(archivo, bloqueDatos,
                // nuevoContenido.toString(),
                // disco.get_tam_bloque());
                byte[] datos = nuevoContenido.toString().getBytes(StandardCharsets.UTF_8);
                int tamBloque = disco.get_tam_bloque();
                int totalBloquesNecesarios = (int) Math.ceil((double) datos.length / tamBloque);

                // Asignar bloques adicionales si hacen falta
                while (archivoInodo.bloques_asignados.size() < totalBloquesNecesarios) {
                    int nuevoBloque = disco.asignar_bloque_libre();
                    archivoInodo.bloques_asignados.add(nuevoBloque);
                }

                // Escribir contenido en bloques
                for (int i = 0; i < totalBloquesNecesarios; i++) {
                    int inicio = i * tamBloque;
                    int fin = Math.min(datos.length, inicio + tamBloque);
                    byte[] fragmento = Arrays.copyOfRange(datos, inicio, fin);
                    manipular_contenido_bloques.escribirBloque(archivo, archivoInodo.bloques_asignados.get(i),
                            new String(fragmento, StandardCharsets.UTF_8), tamBloque);
                }

                // Actualizar los metadatos
                archivoInodo.tamano_utilizado = datos.length;// nuevoContenido.toString().getBytes(StandardCharsets.UTF_8).length;
                archivoInodo.fecha_modificacion = System.currentTimeMillis();
                archivoInodo.fecha_acceso = System.currentTimeMillis();
                Inodo.escribirInodo(archivo, inodoArchivo, archivoInodo);
                System.out.println("Cambios guardados en " + nombreArchivo);
            } else {
                System.out.println("Cambios descartados.");
            }

            // sc.close();
        } catch (IOException e) {
            System.out.println("Error en Note: " + e.getMessage());
        }
    }
}

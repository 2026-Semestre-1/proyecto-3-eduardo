package Nucleo;

import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Date;

import Archivos.Archivo;
import Archivos.GestorArchivos;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import Nucleo.MasterBootRecorder.MBR;
import Nucleo.MasterBootRecorder.ParticionBoot;
import Usuarios.GestorGrupos;
import Usuarios.GestorUsuarios;
import Usuarios.Grupo;
import Usuarios.Usuario;
import Utils.manipular_contenido_bloques;
import Directorios.Inodo;

public class GestorDisco {

    private static String ruta;
    private final int tam_bloque = 4096; // fijo interno

    private static int cwd_inodo = 20; // Inoddo del directorio actual, por defecto apunta al root.

    private int inodo_base = 20;// 16

    // Seccion reservada para la gestion.

    // >> Total de bloques que se reservar para la gestion del programa.
    private static int cant_bloques_gestion = 200;

    private static int cant_inodos_gestion = 181;

    private static Usuario usuario_actual;

    public GestorDisco(String ruta) {
        this.ruta = ruta;

    }

    public int getCwdInodo() {
        return cwd_inodo;
    }

    public void setCwdInodo(int cwd_inodo) {
        this.cwd_inodo = cwd_inodo;
    }

    public static void set_ruta(String ruta) {
        GestorDisco.ruta = ruta;
    }

    public static String get_ruta() {
        return ruta;
    }

    public int get_tam_bloque() {
        return tam_bloque;
    }

    public static Usuario get_usuario_actual() {
        return usuario_actual;
    }

    public static void set_usuario_actual(Usuario usuario_actual) {
        GestorDisco.usuario_actual = usuario_actual;
    }

    public void formatear_disco(int tam_mb, String nombre_contrasena_root) throws IOException {
        int tam_bytes = tam_mb * 1024 * 1024;
        try (RandomAccessFile archivo = new RandomAccessFile(ruta, "rw")) {

            cwd_inodo = inodo_base; // Aqui empiezan los inodos root, antes de esto son reservados para el sistema.
            archivo.setLength(tam_bytes);

            // Escribir MBR
            MBR mbr_obj = new MBR(tam_bytes);
            archivo.seek(0);
            archivo.write(mbr_obj.serializar());

            // Escribir partición de arranque
            ParticionBoot boot = new ParticionBoot();
            archivo.seek(tam_bloque);
            archivo.write(boot.serializar());

            // Inicializar superbloque
            SuperBloque sb = new SuperBloque(tam_bytes, tam_bloque);
            archivo.seek(2 * tam_bloque);
            archivo.write(sb.serializar());

            // Ajustar superbloque
            sb.bloques_libres = sb.bloques_totales - (cant_bloques_gestion + 1); // reservados 0–101
            archivo.seek(2 * tam_bloque);
            archivo.write(sb.serializar());

            // Inicializar bitmap
            BitMapBloques bm = new BitMapBloques(sb.bloques_totales);

            // Reservar bloques de gestión (0–100)
            for (int i = 0; i <= cant_bloques_gestion; i++) {
                bm.marcar_ocupado(i);
            }

            byte[] datosBM2 = bm.serializar();
            BitMapBloques.escribirBitmap(archivo, datosBM2);

            // Generar la tabla de inodos inicial
            BitMapInodos bm_inodos = new BitMapInodos(cant_inodos_gestion);
            byte[] datosBMInodos = bm_inodos.serializar();
            BitMapInodos.escribir_bitmap(archivo, datosBMInodos);

            // Asignar bloque de datos 101 al root
            // int bloque_root_datos = asignar_bloque_libre();// 101; // Buscar un bloque
            // libre.cant_bloques_gestion + 1
            // bm.marcar_ocupado(bloque_root_datos);

            // // Crear la lista de bloques asignados
            // List<Integer> bloques_root_asignados = new ArrayList<>();
            // bloques_root_asignados.add(bloque_root_datos);

            // byte[] datosBM = bm.serializar();
            // BitMapBloques.escribirBitmap(archivo, datosBM);
            // System.out.println(
            // "Bitmap inicializado con " + sb.bloques_totales + " bloques, libres: " +
            // sb.bloques_libres);

            // // Asignar un inodo libre.
            // int inodo_root_libre = asignar_inodo_libre();
            // // Crear inodo root en bloque root.
            // int numero_inodo_root = inodo_base + inodo_root_libre;
            // Inodo root = new Inodo(
            // numero_inodo_root,
            // "root", // nombre
            // 1, // propietario (uid)
            // 1, // grupo (gid)
            // true, // es directorio
            // bloques_root_asignados, // lista de bloques asignados (ej. [101])
            // -1, // inodo padre (root no tiene padre)
            // 77 // permisos -> Por defecto demosle todos.
            // );
            // Inodo.escribirInodo(archivo, numero_inodo_root, root);

            // // Inicializar contenido del root
            // String contenidoRoot = ".;dir;" + numero_inodo_root + "\n..;dir;" +
            // numero_inodo_root + "\n";
            // archivo.seek(bloque_root_datos * tam_bloque);
            // archivo.write(contenidoRoot.getBytes(StandardCharsets.UTF_8));

            // Posicionarnos en en ese Inodo root.
            cwd_inodo = inodo_base + 1;

            // ##### Seccion para el registro del usuario root.
            // Crear usuario root
            GestorUsuarios usuarios = new GestorUsuarios();
            GestorGrupos grupos = new GestorGrupos();

            // registrar los datos.
            int gid_root = grupos.crear_grupo(archivo, "root");
            int uid_root = usuarios.crear_usuario(archivo, gid_root, "root", "root", nombre_contrasena_root, true,
                    true);

            // Seteamos el usuario actual.
            set_usuario_actual(usuarios.buscar_usuario("root"));

            // Asignar el usuario root al grupo.
            grupos.agregar_usuario_a_grupo(1, uid_root);

            // Crear la carpeta user/
            crear_carpeta_users(archivo);

            // Crear la carpeta del usuario "root".
            crear_carpeta_usuario(archivo, usuario_actual);

            // Moverse a la carpeta "root".

            // Crear la carpeta home del usuario root.
            // crear_directorio("Home");

            System.out.println("Usuario creado: " + usuarios.buscar_usuario("root"));
            System.out.println("Grupo creado: " + grupos.buscar_grupo("root"));

        }
    }

    public void crear_carpeta_users(RandomAccessFile archivo) throws IOException {
        // Obtener inodo padre (root)
        int inodo_padre = inodo_base;

        System.out.println("GestorDisco (crear_carpeta_users):");

        // Asignar bloque de datos
        int bloque_datos = asignar_bloque_libre();
        List<Integer> bloques_asignados = new ArrayList<>();
        bloques_asignados.add(bloque_datos);

        // Asignar inodo libre
        int indice_inodo = asignar_inodo_libre();
        int numero_inodo = inodo_base + indice_inodo;

        // Crear inodo users
        Inodo users = new Inodo(
                numero_inodo,
                "users",
                1, 1,
                true,
                bloques_asignados,
                inodo_padre,
                77);
        Inodo.escribirInodo(archivo, numero_inodo, users);

        // Inicializar bloque de datos
        String contenido = ".;dir;" + numero_inodo + "\n..;dir;" + inodo_padre + "\n";
        manipular_contenido_bloques.escribirBloque(archivo, bloque_datos, contenido, tam_bloque);

        // Actualizar directorio padre (root)
        // Actualizar directorio padre
        Inodo padre = Inodo.leerInodo(archivo, inodo_padre);
        int bloquePadre = padre.bloques_asignados.get(0);
        String contenidoPadre = manipular_contenido_bloques.leerBloque(archivo, bloquePadre, tam_bloque);

        // Asegurar salto de línea
        if (!contenidoPadre.endsWith("\n")) {
            contenidoPadre += "\n";
        }

        // Agregar nueva entrada
        contenidoPadre += "users" + ";dir;" + numero_inodo + "\n";

        // Escribir de nuevo
        manipular_contenido_bloques.escribirBloque(archivo, bloquePadre, contenidoPadre, tam_bloque);

        System.out.println("Carpeta 'users/' creada en inodo " + numero_inodo);
    }

    public void crear_carpeta_usuario(RandomAccessFile archivo, Usuario usuario) throws IOException {
        // Padre es la carpeta users
        int inodo_padre = inodo_base;

        // Asignar bloque de datos
        int bloque_datos = asignar_bloque_libre();
        List<Integer> bloques_asignados = new ArrayList<>();
        bloques_asignados.add(bloque_datos);

        // Asignar inodo libre
        int indice_inodo = asignar_inodo_libre();
        int numero_inodo = inodo_base + indice_inodo;

        // Crear inodo usuario
        Inodo carpeta_usuario = new Inodo(
                numero_inodo,
                usuario.getNombre(),
                usuario.getUid(),
                usuario.getGid(),
                true,
                bloques_asignados,
                inodo_padre,
                700);
        Inodo.escribirInodo(archivo, numero_inodo, carpeta_usuario);

        // Inicializar bloque de datos
        String contenido = ".;dir;" + numero_inodo + "\n..;dir;" + inodo_padre + "\n";
        manipular_contenido_bloques.escribirBloque(archivo, bloque_datos, contenido, tam_bloque);

        // Actualizar carpeta users
        // Actualizar directorio padre
        Inodo padre = Inodo.leerInodo(archivo, inodo_padre);
        int bloquePadre = padre.bloques_asignados.get(0);
        String contenidoPadre = manipular_contenido_bloques.leerBloque(archivo, bloquePadre, tam_bloque);

        // Asegurar salto de línea
        if (!contenidoPadre.endsWith("\n")) {
            contenidoPadre += "\n";
        }

        // Agregar nueva entrada
        contenidoPadre += usuario.getNombre() + ";dir;" + numero_inodo + "\n";

        // Escribir de nuevo
        manipular_contenido_bloques.escribirBloque(archivo, bloquePadre, contenidoPadre, tam_bloque);

        System.out.println("Carpeta del usuario '" + usuario.getNombre() + "' creada en inodo " + numero_inodo);

        // Guardamos la navegacion previa.
        int inodo_actual_guardado = cwd_inodo;

        // Crear carpeta Home dentro del usuario
        cwd_inodo = numero_inodo;

        // Cambiar el usuario actual.
        Usuario anterior = usuario_actual;

        usuario_actual = usuario;

        crear_directorio("Home");
        // Volvemos a la navegacion previa.
        cwd_inodo = inodo_actual_guardado;

        usuario_actual = anterior;
    }

    public void mostrar_info() {
        try (RandomAccessFile archivo = new RandomAccessFile(ruta, "r")) {
            // El superbloque está en el bloque 2
            archivo.seek(2 * tam_bloque);
            byte[] buffer = new byte[tam_bloque];
            archivo.read(buffer);

            SuperBloque sb = SuperBloque.deserializar(buffer);

            int bloquesOcupados = sb.bloques_totales - sb.bloques_libres;
            long espacioUtilizado = (long) bloquesOcupados * sb.tam_bloque;
            long espacioDisponible = (long) sb.bloques_libres * sb.tam_bloque;

            System.out.println("Nombre del FileSystem: " + sb.nombre_fs);
            System.out.println("Tamaño: " + (sb.tam_bytes / (1024 * 1024)) + " MB");
            // System.out.println("Espacio utilizado: " + (espacioUtilizado / (1024 * 1024))
            // + " MB");
            // System.out.println("Disponible: " + (espacioDisponible / (1024 * 1024)) + "
            // MB");
            // Mostrar espacio utilizado
            if (espacioUtilizado >= (1024 * 1024)) {
                System.out.println("Espacio utilizado: " + (espacioUtilizado / (1024 * 1024)) + " MB");
            } else {
                System.out.println("Espacio utilizado: " + (espacioUtilizado / 1024) + " KB");
            }

            // Mostrar espacio disponible
            if (espacioDisponible >= (1024 * 1024)) {
                System.out.println("Disponible: " + (espacioDisponible / (1024 * 1024)) + " MB");
            } else {
                System.out.println("Disponible: " + (espacioDisponible / 1024) + " KB");
            }
            System.out.println("Bloques totales: " + sb.bloques_totales);
            System.out.println("Bloques libres: " + sb.bloques_libres);
            System.out.println("Bloques ocupados: " + bloquesOcupados);
        } catch (Exception e) {
            System.out.println("Error al leer superbloque: " + e.getMessage());
        }
    }

    public void crear_directorio(String nombre) throws IOException {
        try (RandomAccessFile archivo = new RandomAccessFile(ruta, "rw")) {

            int inodoPadre = cwd_inodo;

            // System.out.println("Mostrando Inodo Padre Pass 1--");
            // debug_dump_inodo(inodoPadre);
            // Leer superbloque
            archivo.seek(2 * tam_bloque);
            byte[] buffer = new byte[tam_bloque];
            archivo.read(buffer);
            SuperBloque sb = SuperBloque.deserializar(buffer);

            // Asignar bloque libre para contenido
            int bloque_contenido = asignar_bloque_libre();
            if (bloque_contenido < 0)
                throw new IOException("No hay bloques libres disponibles.");
            sb.bloques_libres--;

            // Crear lista de bloques asignados
            List<Integer> bloques_asignados = new ArrayList<>();
            bloques_asignados.add(bloque_contenido);

            // Buscar inodo libre en tabla
            int indice_inodo = asignar_inodo_libre();
            if (indice_inodo < 0)
                throw new IOException("No hay inodos libres disponibles.");
            int inodoNuevo = inodo_base + indice_inodo; // Tomar en cuenta del primer Inodo en donde esta root.

            // Crear inodo hijo y escribirlo en tabla
            // Inodo nuevo = new Inodo(nombre, "root", "root", true, bloques_asignados);
            Inodo nuevo = new Inodo(
                    inodoNuevo, // numero de inodo
                    nombre, // nombre del directorio
                    usuario_actual.getUid(), // propietario (uid root = 1)
                    usuario_actual.getGid(), // grupo (gid root = 1)
                    true, // es directorio
                    bloques_asignados, // bloques asignados
                    inodoPadre, // inodo padre
                    777 // permisos
            );

            Inodo.escribirInodo(archivo, inodoNuevo, nuevo);

            // Inicializar contenido del nuevo directorio
            String contenido = ".;dir;" + inodoNuevo + "\n" +
                    "..;dir;" + inodoPadre + "\n";
            archivo.seek(bloque_contenido * tam_bloque);
            archivo.write(contenido.getBytes(StandardCharsets.UTF_8));

            Inodo padre = Inodo.leerInodo(archivo, inodoPadre);
            if (padre.bloques_asignados == null || padre.bloques_asignados.isEmpty()) {
                throw new IOException("El inodo padre no tiene bloques asignados.");
            }

            // Usar el primer bloque de datos del padre
            int bloquePadreDatos = padre.bloques_asignados.get(0);

            archivo.seek(bloquePadreDatos * tam_bloque);
            byte[] bufferContenidoPadre = new byte[tam_bloque];
            archivo.read(bufferContenidoPadre);
            String contenidoPadre = new String(bufferContenidoPadre, StandardCharsets.UTF_8).trim();
            contenidoPadre += "\n" + nombre + ";dir;" + inodoNuevo + "\n";
            System.out.println("Contenido del padre: " + contenidoPadre);

            // System.out.println("Gestor Disco (Crear_Directorio): Pass9");

            archivo.seek(bloquePadreDatos * tam_bloque);
            archivo.write(contenidoPadre.getBytes(StandardCharsets.UTF_8));

            // System.out.println("Gestor Disco (Crear_Directorio): Pass10");

            // Actualizar superbloque
            archivo.seek(2 * tam_bloque);
            archivo.write(sb.serializar());

            System.out.println("Gestor Disco (Crear_Directorio): Pass11");
            System.out.println("Directorio creado: " + nombre + " (inodo " + inodoNuevo + ")");
        }
    }

    public void crear_archivo(String nombre)
            throws IOException {
        try (RandomAccessFile archivo = new RandomAccessFile(ruta, "rw")) {

            int inodoPadre = getCwdInodo();

            // Leer superbloque
            archivo.seek(2 * tam_bloque);
            byte[] buffer = new byte[tam_bloque];
            archivo.read(buffer);
            SuperBloque sb = SuperBloque.deserializar(buffer);

            // Asignar bloque libre para contenido
            int bloque_contenido = asignar_bloque_libre();
            if (bloque_contenido < 0)
                throw new IOException("No hay bloques libres disponibles.");
            sb.bloques_libres--;

            // Crear lista de bloques asignados
            List<Integer> bloques_asignados = new ArrayList<>();
            bloques_asignados.add(bloque_contenido);

            // Buscar inodo libre
            int indice_inodo = asignar_inodo_libre();
            if (indice_inodo < 0)
                throw new IOException("No hay inodos libres disponibles.");
            int inodoNuevo = inodo_base + indice_inodo;

            // Crear inodo de archivo
            // Inodo nuevo = new Inodo(nombre, usuarioActual, grupoActual, false,
            // bloques_asignados);
            Inodo nuevo = new Inodo(
                    inodoNuevo, // numero de inodo
                    nombre, // nombre del archivo
                    usuario_actual.getUid(), // propietario (uid)
                    usuario_actual.getGid(), // grupo (gid)
                    false, // es_directorio = false
                    bloques_asignados, // bloques asignados
                    inodoPadre, // inodo padre
                    777 // permisos
            );
            Inodo.escribirInodo(archivo, inodoNuevo, nuevo);

            // Inicializar bloque de datos vacío
            manipular_contenido_bloques.escribirBloque(archivo, bloque_contenido, "", tam_bloque);

            // Leer inodo padre
            Inodo padre = Inodo.leerInodo(archivo, inodoPadre);

            // Actualizar contenido del directorio padre
            int bloquePadreDatos = padre.bloques_asignados.get(0);
            String contenidoPadre = manipular_contenido_bloques.leerBloque(archivo, bloquePadreDatos, tam_bloque);
            if (!contenidoPadre.endsWith("\n")) {
                contenidoPadre += "\n";
            }
            contenidoPadre += nombre + ";file;" + inodoNuevo + "\n";
            manipular_contenido_bloques.escribirBloque(archivo, bloquePadreDatos, contenidoPadre, tam_bloque);

            // Actualizar superbloque
            archivo.seek(2 * tam_bloque);
            archivo.write(sb.serializar());

            System.out.println("Archivo creado: " + nombre + " (inodo " + inodoNuevo + ")");
        }
    }

    public void escribir_archivo(String nombreArchivo, String contenido) throws IOException {
        try (RandomAccessFile archivo = new RandomAccessFile(ruta, "rw")) {

            int inodoPadre = getCwdInodo();

            // Buscar el inodo del archivo dentro del directorio actual
            Inodo padre = Inodo.leerInodo(archivo, inodoPadre);
            int bloquePadreDatos = padre.bloques_asignados.get(0);
            String contenidoPadre = manipular_contenido_bloques.leerBloque(archivo, bloquePadreDatos, tam_bloque);

            // Buscar entrada del archivo
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
                throw new IOException("Archivo no encontrado en el directorio actual: " + nombreArchivo);
            }

            // Leer inodo del archivo
            Inodo archivoInodo = Inodo.leerInodo(archivo, inodoArchivo);

            // Escribir contenido en el primer bloque asignado
            int bloqueDatos = archivoInodo.bloques_asignados.get(0);
            manipular_contenido_bloques.escribirBloque(archivo, bloqueDatos, contenido, tam_bloque);

            // Actualizar metadatos del inodo
            archivoInodo.tamano_utilizado = contenido.getBytes(StandardCharsets.UTF_8).length;
            archivoInodo.fecha_modificacion = System.currentTimeMillis();
            archivoInodo.fecha_acceso = System.currentTimeMillis();

            // Guardar inodo actualizado
            Inodo.escribirInodo(archivo, inodoArchivo, archivoInodo);

            System.out.println("Contenido escrito en archivo: " + nombreArchivo);
        }
    }

    public String leer_archivo(String nombreArchivo) throws IOException {
        try (RandomAccessFile archivo = new RandomAccessFile(ruta, "rw")) {

            int inodoPadre = getCwdInodo();

            // Buscar el inodo del archivo dentro del directorio actual
            Inodo padre = Inodo.leerInodo(archivo, inodoPadre);
            int bloquePadreDatos = padre.bloques_asignados.get(0);
            String contenidoPadre = manipular_contenido_bloques.leerBloque(archivo, bloquePadreDatos, tam_bloque);

            // Buscar entrada del archivo
            int inodoArchivo = -1;
            String[] entradas = contenidoPadre.split("\n");
            for (String entrada : entradas) {
                if (!entrada.isBlank()) {
                    String[] partes = entrada.split(";");

                    // Aqui tendria que validarse si es un enlace. Y comprobarse donde esta el
                    // archivo real.
                    if (partes.length >= 3 && partes[0].equals(nombreArchivo) && partes[1].equals("file")) {
                        inodoArchivo = Integer.parseInt(partes[2]);
                        break;
                    }
                }
            }
            if (inodoArchivo == -1) {
                throw new IOException("Archivo no encontrado en el directorio actual: " + nombreArchivo);
            }

            // Leer inodo del archivo
            Inodo archivoInodo = Inodo.leerInodo(archivo, inodoArchivo);

            // Leer contenido del primer bloque asignado
            int bloqueDatos = archivoInodo.bloques_asignados.get(0);
            String contenido = manipular_contenido_bloques.leerBloque(archivo, bloqueDatos, tam_bloque);

            // Actualizar fecha de acceso
            archivoInodo.fecha_acceso = System.currentTimeMillis();
            Inodo.escribirInodo(archivo, inodoArchivo, archivoInodo);

            System.out.println("Contenido leído de archivo: " + nombreArchivo);
            return contenido.trim();
        }
    }

    public int asignar_bloque_libre() throws IOException {
        try (RandomAccessFile archivo = new RandomAccessFile(ruta, "rw")) {
            System.out.println("Buscando bloque libre...");

            // Leer bitmap desde bloques 4–10
            byte[] datosBM = BitMapBloques.leerBitmap(archivo);

            // System.out.println("pass 1");
            BitMapBloques bm = BitMapBloques.deserializar(datosBM);
            // System.out.println("pass 2");

            // Buscar bloque libre
            int bloque_libre = bm.buscar_libre();
            if (bloque_libre == -1) {
                throw new IOException("No hay bloques libres disponibles.");
            }
            // System.out.println("pass 3");

            // Marcarlo como ocupado
            bm.marcar_ocupado(bloque_libre);
            // System.out.println("pass 4");

            // Guardar bitmap actualizado en bloques 4–10
            byte[] nuevosDatosBM = bm.serializar();
            BitMapBloques.escribirBitmap(archivo, nuevosDatosBM);

            System.out.println("Bloque asignado: " + bloque_libre);

            return bloque_libre;
        }
    }

    /**
     * Nombre: ls
     * 
     * Descripcion: Comando para listar el contenido de un directorio.
     * Si se agrega el parametro "-r" se listara el contenido de forma recursiva.
     * Si se agrega el parametro "-i" se mostrara la informacion de los archivos y
     * directorios de forma detallada.
     * Si no se agrega ningun parametro se listara el contenido del directorio
     * actual.
     * 
     * @param inodoActual El inodo del directorio actual.
     * @param recursivo   Indica si se debe mostrar la informacion de los archivos y
     *                    directorios de forma recursiva.
     * @param visitados   Un conjunto de inodos que han sido visitados.
     * @throws IOException Si ocurre un error al leer el directorio.
     */
    public void listar_directorio_actual(int inodoActual, boolean recursivo, Set<Integer> visitados)
            throws IOException {

        if (visitados.contains(inodoActual)) {
            return; // Para indicar cuales han sido revisados.
        }
        visitados.add(inodoActual);

        try (RandomAccessFile archivo = new RandomAccessFile(ruta, "r")) {

            // System.out.println("Mostrando Inodo Actual Pass 1--");
            // debug_dump_inodo(inodoActual);

            Inodo dirActual = Inodo.leerInodo(archivo, inodoActual);

            System.out.println("===== LISTANDO DIRECTORIO INODO " + inodoActual + " Nombre directorio "
                    + dirActual.nombre + " =====");

            if (dirActual.bloques_asignados == null || dirActual.bloques_asignados.isEmpty()) {
                System.out.println("Directorio vacío (sin bloques asignados).");
                return;
            }

            StringBuilder contenidoTotal = new StringBuilder();
            for (int bloque : dirActual.bloques_asignados) {
                String contenidoBloque = manipular_contenido_bloques.leerBloque(archivo, bloque, tam_bloque);
                if (!contenidoBloque.isEmpty()) {
                    contenidoTotal.append(contenidoBloque).append("\n");
                }
            }

            String contenido = contenidoTotal.toString().trim();
            if (contenido.isEmpty()) {
                System.out.println("Directorio vacío.");
                return;
            }

            String[] entradas = contenido.split("\n");
            for (String entrada : entradas) {
                if (!entrada.isBlank()) {
                    String[] partes = entrada.split(";");
                    if (partes.length >= 2) {
                        String nombre = partes[0];
                        String tipo = partes[1];

                        if (partes.length == 3) {
                            int hijoInodo = Integer.parseInt(partes[2]);
                            Inodo hijo = Inodo.leerInodo(archivo, hijoInodo);

                            if (hijo.get_enlaces() == -1) {
                                System.out.println("[LINK] " + nombre + " (inodo " + hijoInodo + ")");
                                // No entrar recursivamente en enlaces
                                continue;
                            }

                            System.out.println((tipo.equals("dir") ? "[DIR] " : "[FILE] ") +
                                    nombre + " (inodo " + hijoInodo + ")");

                            if (recursivo && tipo.equals("dir")
                                    && !nombre.equals(".")
                                    && !nombre.equals("..")
                                    && hijoInodo != inodoActual
                                    && !(inodoActual == inodo_base && nombre.equals("users"))) {
                                listar_directorio_actual(hijoInodo, true, visitados);
                            }
                        }

                    } else {
                        System.out.println("Entrada inválida: " + entrada);
                    }
                }
            }

            System.out.println("=====================================");
        }
    }

    public void mover_renombrar(String origen, String destino) throws IOException {
        try (RandomAccessFile archivo = new RandomAccessFile(ruta, "rw")) {
            int inodoOrigen = buscar_inodo_por_ruta(origen);
            if (inodoOrigen == -1) {
                System.out.println("Error: no se encontró el archivo/directorio origen " + origen);
                return;
            }

            Inodo inodoObj = Inodo.leerInodo(archivo, inodoOrigen);

            // Validar permisos
            if (usuario_actual.getUid() != 1) { // no es root
                boolean esPropietario = usuario_actual.getUid() == inodoObj.propietario;
                boolean esGrupo = usuario_actual.getGid() == inodoObj.grupo;

                int permisosDueño = inodoObj.permisos / 10; // primer dígito
                int permisosGrupo = inodoObj.permisos % 10; // segundo dígito

                if (esPropietario && permisosDueño != 7) {
                    System.out.println("Error: el propietario no tiene permisos suficientes.");
                    return;
                }
                if (esGrupo && permisosGrupo != 7) {
                    System.out.println("Error: el grupo no tiene permisos suficientes.");
                    return;
                }
                if (!esPropietario && !esGrupo) {
                    System.out.println("Error: usuario sin permisos para mover/renombrar.");
                    return;
                }
            }

            // Verificar si destino es un directorio existente
            int inodoDestino = buscar_inodo_por_ruta(destino);

            if (inodoDestino != -1) {
                Inodo destinoInodo = Inodo.leerInodo(archivo, inodoDestino);
                if (!destinoInodo.es_directorio) {
                    System.out.println("Error: el destino debe ser un directorio.");
                    return;
                }

                // Validar que no se mueva un padre dentro de un hijo
                if (origen.startsWith(destino)) {
                    System.out.println("Error: no se puede mover un directorio padre dentro de su hijo.");
                    return;
                }

                // Movimiento: actualizar inodo padre
                inodoObj.inodo_padre = destinoInodo.numero;
                Inodo.escribirInodo(archivo, inodoOrigen, inodoObj);
                System.out.println("Elemento movido: " + origen + " → " + destino);
            } else {
                // Renombrado
                String[] partes = destino.split("/");
                String nuevoNombre = partes[partes.length - 1];
                inodoObj.nombre = nuevoNombre;
                Inodo.escribirInodo(archivo, inodoOrigen, inodoObj);
                System.out.println("Elemento renombrado: " + origen + " → " + nuevoNombre);
            }
        }
    }

    /**
     * Nombre: navegacion_directorios
     * 
     * Descripcion: Cambia el directorio actual al directorio especificado por el
     * path.
     * 
     * @param path El path del directorio al que se desea cambiar.
     * @throws IOException Si ocurre un error al leer el archivo.
     */
    public void navegacion_directorios(String path) throws IOException {
        try (RandomAccessFile archivo = new RandomAccessFile(get_ruta(), "rw")) {

            Inodo actual = Inodo.leerInodo(archivo, cwd_inodo);

            if (path.equals("..")) {
                // Retroceder al padre
                if (cwd_inodo == inodo_base) {
                    System.out.println("Ya estás en el directorio raíz, no puedes retroceder más.");
                    return;
                }
                cwd_inodo = actual.inodo_padre;
                System.out.println("Directorio cambiado a inodo " + actual.inodo_padre);
                return;
            }

            // Buscar subdirectorio dentro del directorio actual
            int bloqueDatos = actual.bloques_asignados.get(0);
            String contenido = manipular_contenido_bloques.leerBloque(archivo, bloqueDatos, get_tam_bloque());

            int nuevoInodo = -1;
            for (String entrada : contenido.split("\n")) {
                if (!entrada.isBlank()) {
                    String[] partes = entrada.split(";");
                    if (partes.length >= 3 && partes[0].equals(path) && partes[1].equals("dir")) {
                        nuevoInodo = Integer.parseInt(partes[2]);
                        break;
                    }
                }
            }

            if (nuevoInodo == -1) {
                System.out.println("Directorio no encontrado: " + path);
                return;
            }

            cwd_inodo = nuevoInodo;
            System.out.println("Directorio cambiado a " + path + " (inodo " + nuevoInodo + ")");
        }
    }

    /**
     * Nombre: construir_ruta
     * 
     * Descripcion: Construye la ruta del directorio actual.
     * 
     * @return La ruta del directorio actual.
     */
    public String construir_ruta() {
        try (RandomAccessFile archivo = new RandomAccessFile(get_ruta(), "rw")) {
            int inodo_actual = cwd_inodo;
            StringBuilder ruta = new StringBuilder();

            while (inodo_actual != inodo_base) {
                // System.out.println("GestorDisco (construir_ruta): Inodo actual: " +
                // inodo_actual);

                Inodo inodo = Inodo.leerInodo(archivo, inodo_actual);
                if (inodo.nombre.equals("root")) {
                    ruta.insert(0, "/root");
                } else {
                    ruta.insert(0, "/" + inodo.nombre);
                }
                inodo_actual = inodo.inodo_padre;
            }
            ruta.insert(0, "/users");

            return ruta.toString();

        } catch (Exception e) {
            System.out.println("Error en pwd: " + e.getMessage());
            return "";
        }
    }

    /**
     * Nombre: buscar_archivo_whereis
     * 
     * Descripcion: Busca recursivamente un archivo en el directorio actual.
     * 
     * @param nombreArchivo El nombre del archivo a buscar.
     * @throws IOException Si ocurre un error al leer el directorio.
     */
    public void buscar_archivo_whereis(String nombreArchivo) throws IOException {
        try (RandomAccessFile archivo = new RandomAccessFile(get_ruta(), "rw")) {
            int cwd_original = cwd_inodo; // guardar cwd actual
            Set<Integer> visitados = new HashSet<>();
            String resultado = buscar_recursivo(archivo, inodo_base, nombreArchivo, visitados);
            cwd_inodo = cwd_original; // restaurar cwd
            if (resultado != null) {
                System.out.println("Archivo encontrado en: " + resultado);
            } else {
                System.out.println("Archivo no encontrado.");
            }

        } catch (Exception e) {
            System.out.println("Error en whereis: " + e.getMessage());
        }
    }

    /**
     * Nombre: buscar_recursivo
     * 
     * Descripcion: Busca recursivamente un archivo en el directorio actual.
     * 
     * @param archivo       El archivo donde se busca.
     * @param inodo_actual  El inodo del directorio actual.
     * @param nombreArchivo El nombre del archivo a buscar.
     * @param visitados     Un conjunto de inodos que han sido visitados.
     * @return El path del archivo si es encontrado, null si no es encontrado.
     * @throws IOException Si ocurre un error al leer el directorio.
     */
    private String buscar_recursivo(RandomAccessFile archivo, int inodo_actual, String nombreArchivo,
            Set<Integer> visitados)
            throws IOException {
        if (visitados.contains(inodo_actual)) {
            return null; // Ya fue visitado.
        }
        visitados.add(inodo_actual);

        Inodo dir = Inodo.leerInodo(archivo, inodo_actual);
        if (!dir.es_directorio)
            return null;

        int bloque_datos = dir.bloques_asignados.get(0);
        String contenido = manipular_contenido_bloques.leerBloque(archivo, bloque_datos, tam_bloque);

        for (String entrada : contenido.split("\n")) {
            if (!entrada.isBlank()) {
                String[] partes = entrada.split(";");
                if (partes.length >= 3) {
                    String nombre = partes[0];
                    int inodo_num = Integer.parseInt(partes[2]);

                    if (nombre.equals(nombreArchivo)) {
                        // encontrado → mover cwd y construir ruta
                        cwd_inodo = inodo_actual;
                        return construir_ruta() + "/" + nombreArchivo;
                    }

                    // si es directorio, entrar recursivamente
                    if (partes[1].equals("dir") && !nombre.equals(".") && !nombre.equals("..")) {
                        String ruta = buscar_recursivo(archivo, inodo_num, nombreArchivo, visitados);
                        if (ruta != null)
                            return ruta;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Nombre: crear_enlace
     * 
     * Descripcion: Comando para crear un enlace simbolico a un archivo o
     * directorio.
     * 
     * @param nombreEnlace El nombre del enlace que se quiere crear.
     * @param rutaObjetivo La ruta del archivo o directorio al que se quiere crear
     *                     un enlace.
     * @throws IOException Si ocurre un error al crear el enlace.
     */
    public void crear_enlace(String nombreEnlace, String rutaObjetivo) throws IOException {
        try (RandomAccessFile archivo = new RandomAccessFile(ruta, "rw")) {

            String[] partesRuta = rutaObjetivo.split("/");
            String nombreArchivo = partesRuta[partesRuta.length - 1];

            if (!validar_objetivo_enlace(rutaObjetivo, nombreArchivo)) {
                System.out.println(
                        "Error: no se puede crear enlace porque el archivo objetivo no existe en la ruta indicada.");
                return;
            }

            int inodoPadre = getCwdInodo();

            // Leer superbloque
            archivo.seek(2 * tam_bloque);
            byte[] buffer = new byte[tam_bloque];
            archivo.read(buffer);
            SuperBloque sb = SuperBloque.deserializar(buffer);

            // Asignar bloque libre para contenido (ruta del objetivo)
            int bloque_contenido = asignar_bloque_libre();
            if (bloque_contenido < 0)
                throw new IOException("No hay bloques libres disponibles.");
            sb.bloques_libres--;

            List<Integer> bloques_asignados = new ArrayList<>();
            bloques_asignados.add(bloque_contenido);

            // Buscar inodo libre
            int indice_inodo = asignar_inodo_libre();
            if (indice_inodo < 0)
                throw new IOException("No hay inodos libres disponibles.");
            int inodoNuevo = inodo_base + indice_inodo;

            // Crear inodo de enlace simbolico
            Inodo enlace = new Inodo(
                    inodoNuevo,
                    nombreEnlace,
                    usuario_actual.getUid(),
                    usuario_actual.getGid(),
                    false, // no es directorio
                    bloques_asignados,
                    inodoPadre,
                    77 // permisos
            );
            enlace.set_enlaces(-1);

            Inodo.escribirInodo(archivo, inodoNuevo, enlace);

            // Guardar la ruta del objetivo en el bloque
            manipular_contenido_bloques.escribirBloque(archivo, bloque_contenido, rutaObjetivo, tam_bloque);

            // Actualizar directorio padre
            Inodo padre = Inodo.leerInodo(archivo, inodoPadre);
            int bloquePadreDatos = padre.bloques_asignados.get(0);
            String contenidoPadre = manipular_contenido_bloques.leerBloque(archivo, bloquePadreDatos, tam_bloque);
            if (!contenidoPadre.endsWith("\n")) {
                contenidoPadre += "\n";
            }
            contenidoPadre += nombreEnlace + ";link;" + inodoNuevo + "\n";
            manipular_contenido_bloques.escribirBloque(archivo, bloquePadreDatos, contenidoPadre, tam_bloque);

            // Actualizar superbloque
            archivo.seek(2 * tam_bloque);
            archivo.write(sb.serializar());

            System.out.println("Enlace simbolico creado: " + nombreEnlace + " -> " + rutaObjetivo);
        }
    }

    /**
     * Nombre: validar_objetivo_enlace
     * 
     * Descripcion: Valida que el archivo exista y que la ruta indicada coincida
     * con la ubicación real del archivo.
     * 
     * @param rutaObjetivo  Ruta del archivo objetivo.
     * @param nombreArchivo Nombre del archivo a buscar.
     * @return true si la ruta indicada coincide con la ubicación real del archivo,
     *         false en caso contrario.
     * @throws IOException Si ocurre un error al leer el archivo.
     */
    public boolean validar_objetivo_enlace(String rutaObjetivo, String nombreArchivo) throws IOException {

        // Validar que el archivo exista
        try (RandomAccessFile archivo = new RandomAccessFile(get_ruta(), "rw")) {
            int cwd_original = cwd_inodo; // guardar cwd actual
            Set<Integer> visitados = new HashSet<>();
            String resultado = buscar_recursivo(archivo, inodo_base, nombreArchivo, visitados);
            cwd_inodo = cwd_original; // restaurar cwd
            if (resultado != null) {
                // System.out.println("Archivo encontrado en: " + resultado);
                if (!resultado.equals(rutaObjetivo)) {
                    System.out.println("La ruta indicada no coincide con la ubicación real del archivo.");
                    System.out.println("Ruta real: " + resultado);
                    return false;
                }
            } else {
                System.out.println("Archivo no encontrado.");
            }

        } catch (Exception e) {
            System.out.println("Error en whereis: " + e.getMessage());
        }

        return true; // validación exitosa
    }

    /**
     * 
     * Nombre: cambiar_propietario
     * 
     * Descripcion: Cambia el propietario de un archivo o directorio.
     * 
     * @param nuevoUsuario string que representa el nuevo usuario
     * @param objetivo     ruta del archivo o directorio
     * @param recursivo    true si se quiere aplicar a todos los archivos o
     *                     directorios
     *                     dentro del objetivo
     * @param visitados    set de inodos visitados
     * @throws IOException
     */
    public void cambiar_propietario(String nuevoUsuario, String objetivo, boolean recursivo, Set<Integer> visitados)
            throws IOException {
        try (RandomAccessFile archivo = new RandomAccessFile(ruta, "rw")) {
            // Obtener inodo del objetivo
            int inodoObjetivo = buscar_inodo_por_ruta(objetivo);

            if (inodoObjetivo == -1) {
                System.out.println("Error: no se encontro el archivo " + objetivo);
                return;
            }

            if (inodoObjetivo == inodo_base) {
                System.out.println("Error: no se puede cambiar el propietario del directorio raiz.");
                return;
            }

            Inodo inodo = Inodo.leerInodo(archivo, inodoObjetivo);

            // Validar permisos: solo root o dueño actual
            if (usuario_actual.getUid() != 1 && usuario_actual.getUid() != inodo.propietario) {
                System.out.println("Error: solo el propietario o root puede cambiar dueño.");
                return;
            }

            // Buscar UID del nuevo usuario

            GestorUsuarios gestor_usuarios = new GestorUsuarios();
            try {
                gestor_usuarios.cargar_usuarios(archivo);

            } catch (Exception e) {
                System.out.println("Error al cargar usuarios: " + e.getMessage());
                return;
            }

            Usuario nuevo = gestor_usuarios.buscar_usuario(nuevoUsuario);
            if (nuevo == null) {
                System.out.println("Usuario no encontrado: " + nuevoUsuario);
                return;
            }

            // Cambiar propietario
            inodo.propietario = nuevo.getUid();
            Inodo.escribirInodo(archivo, inodoObjetivo, inodo);

            Inodo inodo_registrado = Inodo.leerInodo(archivo, inodoObjetivo);
            System.out.println("Inodo registrado: " + inodo_registrado.propietario);
            System.out.println("Propietario de " + objetivo + " cambiado a " + nuevoUsuario);

            // Si es recursivo y es directorio, aplicar a todos sus hijos
            if (recursivo && inodo.es_directorio) {
                int bloqueDatos = inodo.bloques_asignados.get(0);
                String contenido = manipular_contenido_bloques.leerBloque(archivo, bloqueDatos, tam_bloque);

                for (String entrada : contenido.split("\n")) {
                    if (!entrada.isBlank()) {
                        String[] partes = entrada.split(";");
                        if (partes.length >= 3) {
                            String nombre = partes[0];
                            // int hijoInodo = Integer.parseInt(partes[2]);
                            if (nombre.equals(".") || nombre.equals(".."))
                                continue;
                            if (inodoObjetivo == inodo_base && nombre.equals("users"))
                                continue;
                            cambiar_propietario(nuevoUsuario, objetivo + "/" + nombre, true, visitados);
                        }
                    }
                }
            }
        }
    }

    /**
     * 
     * Nombre: cambiar_grupo
     * 
     * Descripcion: Cambia el grupo de un archivo o directorio.
     * 
     * @param nuevoGrupo string que representa el nuevo grupo
     * @param objetivo   ruta del archivo o directorio
     * @param recursivo  true si se quiere aplicar a todos los archivos o
     *                   directorios
     *                   dentro del objetivo
     * @param visitados  set de inodos visitados
     * @throws IOException
     */
    public void cambiar_grupo(String nuevoGrupo, String objetivo, boolean recursivo, Set<Integer> visitados)
            throws IOException {
        try (RandomAccessFile archivo = new RandomAccessFile(ruta, "rw")) {
            int inodoObjetivo = buscar_inodo_por_ruta(objetivo);

            if (inodoObjetivo == -1) {
                System.out.println("Error: no se encontró el archivo " + objetivo);
                return;
            }

            if (inodoObjetivo == inodo_base) {
                System.out.println("Error: no se puede cambiar el grupo del directorio raíz.");
                return;
            }

            if (visitados.contains(inodoObjetivo))
                return; // evitar ciclos
            visitados.add(inodoObjetivo);

            Inodo inodo = Inodo.leerInodo(archivo, inodoObjetivo);

            // Validar permisos: solo root o dueño actual
            if (usuario_actual.getUid() != 1 && usuario_actual.getUid() != inodo.propietario) {
                System.out.println("Error: solo el propietario o root puede cambiar grupo.");
                return;
            }

            // Buscar GID del nuevo grupo
            GestorGrupos gestor_grupos = new GestorGrupos();
            try {
                gestor_grupos.cargar_grupos(archivo);
            } catch (Exception e) {
                System.out.println("Error al cargar grupos: " + e.getMessage());
                return;
            }

            Grupo nuevo = gestor_grupos.buscar_grupo(nuevoGrupo);
            if (nuevo == null) {
                System.out.println("Grupo no encontrado: " + nuevoGrupo);
                return;
            }

            // Cambiar grupo
            inodo.grupo = nuevo.get_gid();
            Inodo.escribirInodo(archivo, inodoObjetivo, inodo);

            Inodo inodo_registrado = Inodo.leerInodo(archivo, inodoObjetivo);
            System.out.println("Inodo registrado: grupo " + inodo_registrado.grupo);
            System.out.println("Grupo de " + objetivo + " cambiado a " + nuevoGrupo);

            // Si es recursivo y es directorio, aplicar a todos sus hijos
            if (recursivo && inodo.es_directorio) {
                int bloqueDatos = inodo.bloques_asignados.get(0);
                String contenido = manipular_contenido_bloques.leerBloque(archivo, bloqueDatos, tam_bloque);

                for (String entrada : contenido.split("\n")) {
                    if (!entrada.isBlank()) {
                        String[] partes = entrada.split(";");
                        if (partes.length >= 3) {
                            String nombre = partes[0];
                            if (nombre.equals(".") || nombre.equals(".."))
                                continue;
                            if (inodoObjetivo == inodo_base && nombre.equals("users"))
                                continue;

                            cambiar_grupo(nuevoGrupo, objetivo + "/" + nombre, true, visitados);
                        }
                    }
                }
            }
        }
    }

    /**
     * 
     * Nombre: cambiar_permisos
     * 
     * Descripcion: Cambia los permisos de un archivo o directorio.
     * 
     * @param permisosStr string de 2 digitos que representa los permisos
     * @param objetivo    ruta del archivo o directorio
     * @param recursivo   true si se quiere aplicar a todos los archivos o
     *                    directorios
     *                    dentro del objetivo
     * @param visitados   set de inodos visitados
     * @throws IOException
     */
    public void cambiar_permisos(String permisosStr, String objetivo, boolean recursivo, Set<Integer> visitados)
            throws IOException {
        try (RandomAccessFile archivo = new RandomAccessFile(ruta, "rw")) {
            int inodoObjetivo = buscar_inodo_por_ruta(objetivo);

            if (inodoObjetivo == -1) {
                System.out.println("Error: no se encontró el archivo " + objetivo);
                return;
            }

            if (inodoObjetivo == inodo_base) {
                System.out.println("Error: no se puede cambiar permisos del directorio raíz.");
                return;
            }

            // Para evitar los criclos.
            if (visitados.contains(inodoObjetivo))
                return;
            visitados.add(inodoObjetivo);

            Inodo inodo = Inodo.leerInodo(archivo, inodoObjetivo);

            // Validar permisos: solo root o dueño actual
            if (usuario_actual.getUid() != 1 && usuario_actual.getUid() != inodo.propietario) {
                System.out.println("Error: solo el propietario o root puede cambiar permisos.");
                return;
            }

            // Validar formato de permisos
            if (permisosStr.length() != 2) {
                System.out.println("Error: permisos invalidos. Debe ser un número de 2 digitos (ej. 77).");
                return;
            }

            int propietario = Character.getNumericValue(permisosStr.charAt(0));
            int grupo = Character.getNumericValue(permisosStr.charAt(1));

            System.out.println("Propietario: " + propietario);
            System.out.println("Grupo: " + grupo);

            // Crear un lista que tenga los valores validos.
            List<Integer> permisosValidos = Arrays.asList(0, 1, 2, 4);

            // Validar por separado los valores de cada uno.
            if (!permisosValidos.contains(propietario)) {
                System.out.println("Error: los permisos validos para el propietario son 0, 1, 2 o 4.");
                return;
            }

            if (!permisosValidos.contains(grupo)) {
                System.out.println("Error: los permisos validos para el grupo son 0, 1, 2 o 4.");
                return;
            }

            // Guardar permisos como entero (ej. 77)
            inodo.permisos = Integer.parseInt(permisosStr);
            Inodo.escribirInodo(archivo, inodoObjetivo, inodo);

            System.out.println("Permisos de " + objetivo + " cambiados a " + permisosStr);

            // Recursividad
            if (recursivo && inodo.es_directorio) {
                int bloqueDatos = inodo.bloques_asignados.get(0);
                String contenido = manipular_contenido_bloques.leerBloque(archivo, bloqueDatos, tam_bloque);

                for (String entrada : contenido.split("\n")) {
                    if (!entrada.isBlank()) {
                        String[] partes = entrada.split(";");
                        if (partes.length >= 3) {
                            String nombre = partes[0];
                            if (nombre.equals(".") || nombre.equals(".."))
                                continue;
                            if (inodoObjetivo == inodo_base && nombre.equals("users"))
                                continue;

                            cambiar_permisos(permisosStr, objetivo + "/" + nombre, true, visitados);
                        }
                    }
                }
            }
        }
    }

    /**
     * Nombre: mostrar_FCB
     * 
     * Descripcion: Busca el inodo correspondiente al archivo y muestra su
     * información
     * de una manera amigable. Esta información incluye: nombre, inodo, propietario,
     * grupo, permisos, fecha de creación, fecha de modificación, fecha de acceso,
     * tamaño y los bloques asignados. Además, indica si el archivo está abierto o
     * no.
     * 
     * @param nombreArchivo
     * @throws IOException
     */
    public void mostrar_FCB(String nombreArchivo) throws IOException {
        try (RandomAccessFile archivo = new RandomAccessFile(ruta, "rw")) {
            // Buscar inodo del archivo
            int inodoObjetivo = buscar_inodo_por_ruta(nombreArchivo);
            System.out.println("Inodo objetivo: " + inodoObjetivo);
            if (inodoObjetivo == -1) {
                System.out.println("Error: archivo no encontrado " + nombreArchivo);
                return;
            }

            Inodo inodo = Inodo.leerInodo(archivo, inodoObjetivo);
            System.out.println("Inodo: " + inodo.nombre);
            if (inodo.es_directorio) {
                System.out.println("Error: viewFCB solo aplica a archivos, no directorios.");
                return;
            }

            System.out.println("pass1");

            // Consultar tabla de archivos abiertos
            // GestorArchivos ga = get_archivos_disk();
            // System.out.println("pass2");

            // boolean abierto = false;
            // for (Archivo e : ga.get_tabla()) {
            // if (e.get_inodo() == inodo.numero && e.is_activo()) {
            // abierto = true;
            // break;
            // }
            // }
            // System.out.println("pass3");

            // Mostrar informacion del inodo
            System.out.println("===== FCB del archivo =====");
            System.out.println("Nombre: " + inodo.nombre);
            System.out.println("Inodo: " + inodo.numero);
            System.out.println("Propietario UID: " + inodo.propietario);
            System.out.println("Grupo GID: " + inodo.grupo);
            System.out.println("Permisos: " + inodo.permisos);
            System.out.println("Fecha creacion: " + new Date(inodo.fecha_creacion));
            System.out.println("Fecha modificacion: " + new Date(inodo.fecha_modificacion));
            System.out.println("Fecha acceso: " + new Date(inodo.fecha_acceso));
            System.out.println("Tamaño: " + inodo.tamano_utilizado + " bytes");
            System.out.println("Bloques asignados: " + inodo.bloques_asignados);
            // System.out.println("Estado: " + (abierto ? "Abierto" : "Cerrado"));
            System.out.println("===========================");
        }
    }

    /**
     * Nombre: establecer_archivo_abierto
     * 
     * Descripcion: Establece un archivo como abierto.
     * 
     * @param nombre Nombre del archivo a establecer como abierto.
     * @param modo   Modo en el que se debe establecer el archivo abierto.
     * @throws IOException Si ocurre un error al leer el archivo.
     */
    public void establecer_archivo_abierto(String nombre, String modo) throws IOException {
        try (RandomAccessFile archivo = new RandomAccessFile(get_ruta(), "rw")) {

            Inodo inodo_archivo = buscar_inodo_por_nombre(archivo, nombre);
            if (inodo_archivo == null) {
                System.out.println("Archivo no encontrado: " + nombre);
                return;
            }
            if (inodo_archivo.es_directorio) {
                System.out.println("El archivo no es un archivo.");
                return;
            }

            GestorArchivos gestor_archivos = new GestorArchivos();
            gestor_archivos.cargar_tabla(archivo);

            // Abrir root para inicialización
            gestor_archivos.abrir_archivo(archivo, inodo_archivo.numero, modo, usuario_actual.getUid(),
                    usuario_actual.getGid());

        } catch (ClassNotFoundException e) {

            e.printStackTrace();
        }
    }

    /**
     * Nombre: cerrar_archivo_abierto
     * 
     * Descripcion: Cierra un archivo abierto.
     * 
     * @param nombre Nombre del archivo a cerrar.
     * @throws IOException Si ocurre un error al leer el archivo.
     */
    public void cerrar_archivo_abierto(String nombre) throws IOException {
        try (RandomAccessFile archivo = new RandomAccessFile(get_ruta(), "rw")) {

            Inodo inodo_archivo = buscar_inodo_por_nombre(archivo, nombre);
            if (inodo_archivo == null) {
                System.out.println("Archivo no encontrado: " + nombre);
                return;
            }
            if (inodo_archivo.es_directorio) {
                System.out.println("El archivo no es un archivo.");
                return;
            }

            GestorArchivos gestor_archivos = new GestorArchivos();
            gestor_archivos.cargar_tabla(archivo);

            // Cerrar el archivo
            gestor_archivos.cerrar_archivo(archivo, inodo_archivo.numero);

        } catch (ClassNotFoundException e) {

            e.printStackTrace();
        }
    }

    /**
     * 
     * @return El inodo que esta libre el la tabla de inodos, pero lo que devuelve
     *         es el indice de la tabla, se debe de operar con el inodo base para
     *         obtener el numero real del inodo.
     * @throws IOException Si no se encuentra el inodo o algun error de acceso.
     */
    public int asignar_inodo_libre() throws IOException {
        try (RandomAccessFile archivo = new RandomAccessFile(ruta, "rw")) {
            // Leer bitmap de inodos desde los bloques reservados (14–15)
            byte[] datosBM = BitMapInodos.leer_bitmap(archivo);
            BitMapInodos bm = BitMapInodos.deserializar(datosBM);

            // Buscar índice libre
            int indice = bm.buscar_libre();
            if (indice == -1) {
                return -1; // no hay inodos libres
            }

            // Marcar como ocupado
            bm.marcar_ocupado(indice);

            // Guardar bitmap actualizado en disco
            BitMapInodos.escribir_bitmap(archivo, bm.serializar());

            return indice; // índice dentro de la tabla de inodos
        }
    }

    /**
     * Nombre: buscar_inodo_por_nombre
     * 
     * Descripcion: Busca un inodo por nombre en el directorio actual.
     * 
     * @param archivo Archivo de acceso aleatorio al disco.
     * @param nombre  Nombre del archivo a buscar.
     * @return El inodo encontrado o null si no existe.
     * @throws IOException Si hay un error al leer el disco.
     */
    public Inodo buscar_inodo_por_nombre(RandomAccessFile archivo, String nombre) throws IOException {
        // Leer el inodo del directorio actual
        Inodo directorio_actual = Inodo.leerInodo(archivo, cwd_inodo);

        // Usar el primer bloque de datos asignado
        int bloque_datos = directorio_actual.bloques_asignados.get(0);
        String contenido = manipular_contenido_bloques.leerBloque(archivo, bloque_datos, tam_bloque);

        // Recorrer entradas del directorio
        for (String entrada : contenido.split("\n")) {
            if (!entrada.isBlank()) {
                String[] partes = entrada.split(";");
                if (partes.length >= 3) {
                    String nombreEntrada = partes[0];
                    int inodo_num = Integer.parseInt(partes[2]);

                    if (nombreEntrada.equals(nombre)) {
                        // Devolver el objeto Inodo completo
                        return Inodo.leerInodo(archivo, inodo_num);
                    }
                }
            }
        }

        return null; // no encontrado
    }

    /**
     * Nombre: buscar_inodo_por_ruta
     * 
     * Descripcion: Busca un inodo por una ruta especifica.
     * 
     * @param rutaObjetivo Ruta del inodo a buscar.
     * @return El inodo encontrado o null si no existe.
     * @throws IOException Si ocurre un error al leer el archivo.
     */
    public int buscar_inodo_por_ruta(String rutaObjetivo) throws IOException {
        try (RandomAccessFile archivo = new RandomAccessFile(ruta, "rw")) {
            // Dividir la ruta en partes
            String[] partes = rutaObjetivo.split("/");
            int inodoActual = inodo_base; // normalmente el inodo base es /users

            for (String parte : partes) {
                if (parte.isBlank())
                    continue; // ignorar vacíos por el primer "/"

                Inodo dir = Inodo.leerInodo(archivo, inodoActual);
                if (!dir.es_directorio) {
                    throw new IOException("Ruta inválida: " + parte + " no es un directorio");
                }

                // Leer contenido del directorio actual
                int bloqueDatos = dir.bloques_asignados.get(0);
                String contenido = manipular_contenido_bloques.leerBloque(archivo, bloqueDatos, tam_bloque);

                boolean encontrado = false;
                for (String entrada : contenido.split("\n")) {
                    if (!entrada.isBlank()) {
                        String[] datos = entrada.split(";");
                        if (datos.length >= 3) {
                            String nombre = datos[0];
                            int hijoInodo = Integer.parseInt(datos[2]);

                            if (nombre.equals(parte)) {
                                inodoActual = hijoInodo;
                                encontrado = true;
                                break;
                            }
                        }
                    }
                }

                if (!encontrado) {
                    throw new IOException("Ruta no encontrada: " + parte);
                }
            }

            return inodoActual; // número de inodo del objetivo
        }
    }

    /**
     * Nombre: get_archivos_disk
     * 
     * Descripcion: Obtiene la tabla de archivos abiertos.
     * 
     * @return GestorArchivos La tabla de archivos.
     * @throws IOException Si ocurre un error al leer el archivo.
     */
    public GestorArchivos get_archivos_disk() throws IOException {
        GestorArchivos ga = new GestorArchivos();
        try (RandomAccessFile archivo = new RandomAccessFile(GestorDisco.get_ruta(), "rw")) {
            System.out.println("pass 11.1");
            ga.cargar_tabla(archivo);
            System.out.println("pass 11.2");
        } catch (ClassNotFoundException e) {
            throw new IOException("Error al cargar la tabla de archivos.");
        }
        return ga;
    }

    /**
     * Nombre: debug_dump_bitmap
     * 
     * Descripcion: Muestra los primeros 'cantidad' bloques del bitmap.
     * 
     * @param cantidad Cantidad de bloques a mostrar.
     * @throws IOException Si ocurre un error al leer el archivo.
     */
    public void debug_dump_bitmap(int cantidad) throws IOException {
        try (RandomAccessFile archivo = new RandomAccessFile(ruta, "r")) {
            archivo.seek(4 * tam_bloque);

            // Leer el total de bloques
            int total = archivo.readInt();
            BitMapBloques bm = new BitMapBloques(total);

            // Leer todos los booleanos
            for (int i = 0; i < total; i++) {
                bm.marcar_libre(i); // inicializamos
                bm.get_bloques()[i] = archivo.readBoolean();
            }

            // Mostrar los primeros 'cantidad' bloques
            System.out.println("=== DEBUG BITMAP ===");
            for (int i = 0; i < cantidad && i < total; i++) {
                System.out.println("Bloque " + i + ": " + (bm.get_bloques()[i] ? "Ocupado (1)" : "Libre (0)"));
            }
            System.out.println("====================");
        }
    }

    /**
     * Nombre: debug_dump_inodo
     * 
     * Descripcion: Muestra un inodo especificado.
     * 
     * @param inodoNumero Numero del inodo a mostrar.
     * @throws IOException Si ocurre un error al leer el archivo.
     */
    public void debug_dump_inodo(int inodoNumero) throws IOException {
        try (RandomAccessFile archivo = new RandomAccessFile(ruta, "r")) {
            // Leer el inodo
            long posicion = inodoNumero * tam_bloque;
            archivo.seek(posicion);
            int longitud = archivo.readInt();
            System.out.println("Longitud del inodo: " + longitud);
            byte[] bufferPadre = new byte[longitud];
            archivo.readFully(bufferPadre);
            Inodo inodo = Inodo.deserializar(bufferPadre);

            System.out.println("===== DEBUG DIRECTORIO INODO " + inodoNumero + " =====");
            System.out.println("Nombre: " + inodo.nombre);
            System.out.println("Bloques asignados: " + inodo.bloques_asignados);
            System.out.println("Nombre: " + inodo.nombre);
            System.out.println("Propietario: " + inodo.propietario);
            System.out.println("Grupo: " + inodo.grupo);
            System.out.println("Es directorio: " + inodo.es_directorio);
            System.out.println("Bloques asignados: " + inodo.bloques_asignados);
            System.out.println("Fecha de creacion: " + inodo.fecha_creacion);
            System.out.println("Fecha de modificacion: " + inodo.fecha_modificacion);
            System.out.println("Fecha de acceso: " + inodo.fecha_acceso);
            System.out.println("Tamano: " + inodo.tamano_utilizado);
            System.out.println("Grupo: " + inodo.grupo);
            System.out.println("Permisos: " + inodo.permisos);
            System.out.println("Es directorio: " + inodo.es_directorio);
            System.out.println("Bloques asignados: " + inodo.bloques_asignados);
            System.out.println("Inodo Padre: " + inodo.inodo_padre);

            // Mostrar contenido de cada bloque asignado
            for (int bloque : inodo.bloques_asignados) {
                archivo.seek(bloque * tam_bloque);
                byte[] buffer = new byte[tam_bloque];
                archivo.read(buffer);
                String contenido = new String(buffer, StandardCharsets.UTF_8).trim();
                System.out.println("Bloque " + bloque + " contenido:");
                System.out.println(contenido.isEmpty() ? "(vacío)" : contenido);
            }

            System.out.println("=====================================");
        }
    }

}

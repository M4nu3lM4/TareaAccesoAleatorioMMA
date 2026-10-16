package gestorDepart;

import org.iesalandalus.programacion.utilidades.Entrada;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class AleatorioDepart {

    static final File FICHERO = new File("datos/AleatorioDepart.dat");
    static final int LONGITUD_REGISTRO = 64;
    static final int LONGITUD_TEXTO = 15;
    static final int DESPLAZAMIENTO_NOMBRE = 2;
    static final int DESPLAZAMIENTO_EMPLEADOS = 62;
    static final int NUMERO_MINIMO = 1;
    static final int NUMERO_MAXIMO = 32767;
    static final String MARCA_VACIA = "---------------";
    static final String MARCA_BORRADO = "***************";


    static final int FUERA_DEL_FICHERO = 0;
    static final int NUNCA_UTILIZADA = 1;
    static final int ELIMINADA = 2;
    static final int ACTIVA = 3;
    static final int ANOMALA = 4;

    public static void main(String[] args) {
        try {
            prepararFichero();
            System.out.println("Comprobando la integridad del fichero...");
            int numeroAnomalias = integridad();
            boolean modoDiagnostico = numeroAnomalias > 0;

            if (modoDiagnostico) {
                System.out.println();
                System.out.println("ATENCION: se han detectado anomalías en el fichero.");
                System.out.println("La aplicacion entra en modo de diagnostico: solo se permiten "
                        + "la inspección fisica, las estadisticas y la comprobacion de integridad.");
            }
            ejecutarMenu(modoDiagnostico);
        } catch (IOException e) {
            System.out.println("No se ha podido preparar el fichero: " + e.getMessage());
        }
    }

    private static void prepararFichero() throws IOException {
        File carpeta = FICHERO.getParentFile();
        if (carpeta != null && !carpeta.exists()) {
            carpeta.mkdirs();
        }
        if (FICHERO.createNewFile()) {
            System.out.println("El fichero " + FICHERO.getName() + " no existia y se ha creado vacio.");
        } else {
            System.out.println("El fichero " + FICHERO.getName() + " ya existe: se conserva su contenido.");
        }
    }

    private static void ejecutarMenu(boolean modoDiagnostico) {
        int opcion;

        do {
            mostrarMenu(modoDiagnostico);
            System.out.print("Elige una opcion: ");
            opcion = Entrada.entero();

            try {
                procesarOpcion(opcion, modoDiagnostico);
            } catch (IllegalArgumentException e) {
                System.out.println("Validacion incorrecta: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("Error de acceso al fichero: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private static void mostrarMenu(boolean modoDiagnostico) {
        System.out.println();
        System.out.println("===== GESTION DE DEPARTAMENTOS =====");
        if (modoDiagnostico) {
            System.out.println("*** MODO DE DIAGNOSTICO ***");
        } else {
            System.out.println(" 1. Insertar departamento");
            System.out.println(" 2. Consultar departamento");
            System.out.println(" 3. Comprobar si existe un departamento");
            System.out.println(" 4. Actualizar número de empleados");
            System.out.println(" 5. Actualizar nombre y localidad");
            System.out.println(" 6. Eliminar departamento");
            System.out.println(" 7. Listado de departamentos");
        }
        System.out.println(" 8. Inspeccion fisica del fichero");
        System.out.println(" 9. Estadisticas");
        System.out.println("10. Comprobar integridad");
        System.out.println(" 0. Salir");
    }

    private static void procesarOpcion(int opcion, boolean modoDiagnostico) throws IOException {
        int numeroDepartamento;
        String nombre;
        String localidad;
        int numeroEmpleados;
        String respuesta;

        if (modoDiagnostico && opcion >= 1 && opcion <= 7) {
            System.out.println("Operacion no permitida en modo de diagnostico.");
            return;
        }

        switch (opcion) {
            case 1:
                numeroDepartamento = leerNumeroDepartamento();
                nombre = leerTexto("Introduce el nombre", "nombre");
                localidad = leerTexto("Introduce la localidad", "localidad");
                numeroEmpleados = leerNumeroEmpleados();
                insertaDepartamento(numeroDepartamento, nombre, localidad, numeroEmpleados);
                break;
            case 2:
                numeroDepartamento = leerNumeroDepartamento();
                consultaDepartamento(numeroDepartamento);
                break;
            case 3:
                numeroDepartamento = leerNumeroDepartamento();
                if (existeDepartamento(numeroDepartamento)) {
                    System.out.println("El departamento " + numeroDepartamento + " existe.");
                } else {
                    mostrarMensajeNoActivo(obtenerEstadoDepartamento(numeroDepartamento), numeroDepartamento);
                }
                break;
            case 4:
                numeroDepartamento = leerNumeroDepartamento();
                numeroEmpleados = leerNumeroEmpleados();
                actualizaNumeroEmpleados(numeroDepartamento, numeroEmpleados);
                break;
            case 5:
                numeroDepartamento = leerNumeroDepartamento();
                nombre = leerTexto("Introduce el nuevo nombre", "nombre");
                localidad = leerTexto("Introduce la nueva localidad", "localidad");
                actualizaNombreLocalidad(numeroDepartamento, nombre, localidad);
                break;
            case 6:
                numeroDepartamento = leerNumeroDepartamento();
                if (existeDepartamento(numeroDepartamento)) {
                    do {
                        System.out.print("¿Seguro que quieres eliminar el departamento "
                                + numeroDepartamento + " (s/n)? ");
                        respuesta = Entrada.cadena().trim();
                    } while (!respuesta.equalsIgnoreCase("s") && !respuesta.equalsIgnoreCase("n"));

                    if (respuesta.equalsIgnoreCase("s")) {
                        eliminaDepartamento(numeroDepartamento);
                    } else {
                        System.out.println("Eliminacion cancelada.");
                    }
                } else {

                    eliminaDepartamento(numeroDepartamento);
                }
                break;
            case 7:
                listadoDepartamentos();
                break;
            case 8:
                inspeccionFisica();
                break;
            case 9:
                estadisticas();
                break;
            case 10:
                integridad();
                break;
            case 0:
                System.out.println("Fin del programa.");
                break;
            default:
                System.out.println("Opción no valida.");
        }
    }



    private static int leerNumeroDepartamento() {
        int numero;
        boolean valido;

        do {
            System.out.print("Introduce el numero de departamento (" + NUMERO_MINIMO + "-" + NUMERO_MAXIMO + "): ");
            numero = Entrada.entero();
            valido = numero >= NUMERO_MINIMO && numero <= NUMERO_MAXIMO;
            if (!valido) {
                System.out.println("El numero de departamento debe estar comprendido entre "
                        + NUMERO_MINIMO + " y " + NUMERO_MAXIMO + ".");
            }
        } while (!valido);

        return numero;
    }

    private static int leerNumeroEmpleados() {
        int numeroEmpleados;
        boolean valido;

        do {
            System.out.print("Introduce el numero de empleados (0-" + NUMERO_MAXIMO + "): ");
            numeroEmpleados = Entrada.entero();
            valido = numeroEmpleados >= 0 && numeroEmpleados <= NUMERO_MAXIMO;
            if (!valido) {
                System.out.println("El numero de empleados debe estar comprendido entre 0 y " + NUMERO_MAXIMO + ".");
            }
        } while (!valido);

        return numeroEmpleados;
    }


    private static String leerTexto(String mensaje, String campo) {
        String texto = null;

        do {
            System.out.print(mensaje + " (maximo " + LONGITUD_TEXTO + " caracteres): ");
            try {
                texto = validarTexto(Entrada.cadena(), campo);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        } while (texto == null);

        return texto;
    }

    private static void validarNumeroDepartamento(int numeroDepartamento) {
        if (numeroDepartamento < NUMERO_MINIMO || numeroDepartamento > NUMERO_MAXIMO) {
            throw new IllegalArgumentException("El numero de departamento debe estar comprendido entre "
                    + NUMERO_MINIMO + " y " + NUMERO_MAXIMO + ".");
        }
    }

    private static void validarNumeroEmpleados(int numeroEmpleados) {
        if (numeroEmpleados < 0 || numeroEmpleados > NUMERO_MAXIMO) {
            throw new IllegalArgumentException("El numero de empleados debe estar comprendido entre 0 y "
                    + NUMERO_MAXIMO + ".");
        }
    }

    private static String validarTexto(String texto, String campo) {
        if (texto == null) {
            throw new IllegalArgumentException("El campo " + campo + " no puede ser nulo.");
        }

        String textoLimpio = texto.trim();

        if (textoLimpio.isEmpty()) {
            throw new IllegalArgumentException("El campo " + campo
                    + " no puede estar vacio ni contener solo espacios.");
        }
        if (textoLimpio.length() > LONGITUD_TEXTO) {
            throw new IllegalArgumentException("El campo " + campo + " no puede superar los "
                    + LONGITUD_TEXTO + " caracteres (se han escrito " + textoLimpio.length() + ").");
        }
        return textoLimpio;
    }

    private static String rellenarTexto(String texto) {
        return String.format("%-" + LONGITUD_TEXTO + "s", texto);
    }



    private static long calcularDesplazamiento(int numeroPosicion) {
        return (long) (numeroPosicion - 1) * LONGITUD_REGISTRO;
    }


    private static String leerTextoRegistro(RandomAccessFile file) throws IOException {
        char[] caracteres = new char[LONGITUD_TEXTO];

        for (int i = 0; i < caracteres.length; i++) {
            caracteres[i] = file.readChar();
        }
        return new String(caracteres);
    }

    private static void escribirRegistro(RandomAccessFile file, int numeroDepartamento, String nombre,
                                         String localidad, int numeroEmpleados) throws IOException {
        file.seek(calcularDesplazamiento(numeroDepartamento));
        file.writeShort(numeroDepartamento);
        file.writeChars(rellenarTexto(nombre));
        file.writeChars(rellenarTexto(localidad));
        file.writeShort(numeroEmpleados);
    }

    private static void escribirPosicionVacia(RandomAccessFile file) throws IOException {
        file.writeShort(0);
        file.writeChars(MARCA_VACIA);
        file.writeChars(MARCA_VACIA);
        file.writeShort(0);
    }

    private static boolean esTextoActivoValido(String texto) {
        String textoLimpio = texto.trim();

        return !textoLimpio.isEmpty() && texto.equals(rellenarTexto(textoLimpio));
    }

    private static int clasificarPosicion(long numeroPosicion, short identificador, String nombre,
                                          String localidad, short numeroEmpleados) {
        if (identificador == 0 && nombre.equals(MARCA_VACIA) && localidad.equals(MARCA_VACIA)
                && numeroEmpleados == 0) {
            return NUNCA_UTILIZADA;
        }
        if (identificador == -1 && nombre.equals(MARCA_BORRADO) && localidad.equals(MARCA_BORRADO)
                && numeroEmpleados == -1) {
            return ELIMINADA;
        }
        if (identificador == numeroPosicion && numeroEmpleados >= 0
                && esTextoActivoValido(nombre) && esTextoActivoValido(localidad)) {
            return ACTIVA;
        }
        return ANOMALA;
    }

    private static String describirAnomalia(long numeroPosicion, short identificador, String nombre,
                                            String localidad, short numeroEmpleados) {
        if (identificador < -1) {
            return "Identificador negativo distinto de -1 (almacenado: " + identificador + ").";
        }
        if (identificador > 0) {
            if (identificador != numeroPosicion) {
                return "Identificador positivo que no corresponde a su posición (almacenado: "
                        + identificador + ", esperado: " + numeroPosicion + ").";
            }
            if (numeroEmpleados < 0) {
                return "Numero de empleados negativo en un registro activo (" + numeroEmpleados + ").";
            }
            if (nombre.trim().isEmpty()) {
                return "Nombre vacio en un registro activo.";
            }
            if (localidad.trim().isEmpty()) {
                return "Localidad vacia en un registro activo.";
            }
            return "Nombre o localidad de un registro activo con relleno o formato incorrecto.";
        }
        if (identificador == 0) {
            return "Posicion nunca utilizada que no respeta su formato.";
        }
        return "Registro eliminado sin las marcas de borrado establecidas.";
    }

    private static int obtenerEstado(RandomAccessFile file, int numeroPosicion) throws IOException {
        long desplazamiento = calcularDesplazamiento(numeroPosicion);

        if (desplazamiento >= file.length()) {
            return FUERA_DEL_FICHERO;
        }
        if (desplazamiento + LONGITUD_REGISTRO > file.length()) {
            return ANOMALA;
        }

        file.seek(desplazamiento);
        short identificador = file.readShort();
        String nombre = leerTextoRegistro(file);
        String localidad = leerTextoRegistro(file);
        short numeroEmpleados = file.readShort();

        return clasificarPosicion(numeroPosicion, identificador, nombre, localidad, numeroEmpleados);
    }

    private static String describirEstado(int estado) {
        switch (estado) {
            case NUNCA_UTILIZADA:
                return "nunca utilizada";
            case ELIMINADA:
                return "eliminada";
            case ACTIVA:
                return "activa";
            default:
                return "anomala";
        }
    }

    private static void mostrarMensajeNoActivo(int estado, int numeroDepartamento) {
        switch (estado) {
            case FUERA_DEL_FICHERO:
                System.out.println("El departamento " + numeroDepartamento
                        + " no existe: su posicion esta fuera del fichero.");
                break;
            case NUNCA_UTILIZADA:
                System.out.println("El departamento " + numeroDepartamento
                        + " no existe: su posicion nunca ha sido utilizada.");
                break;
            case ELIMINADA:
                System.out.println("El departamento " + numeroDepartamento
                        + " no existe: el registro fue eliminado.");
                break;
            default:
                System.out.println("La posición del departamento " + numeroDepartamento
                        + " es anómala. No se realiza ninguna operacion; revisa la integridad del fichero.");
        }
    }


    public static int obtenerEstadoDepartamento(int numeroDepartamento) throws IOException {
        validarNumeroDepartamento(numeroDepartamento);

        try (RandomAccessFile file = new RandomAccessFile(FICHERO, "r")) {
            return obtenerEstado(file, numeroDepartamento);
        }
    }


    public static boolean existeDepartamento(int numeroDepartamento) throws IOException {
        return obtenerEstadoDepartamento(numeroDepartamento) == ACTIVA;
    }

    public static void consultaDepartamento(int numeroDepartamento) throws IOException {
        validarNumeroDepartamento(numeroDepartamento);

        try (RandomAccessFile file = new RandomAccessFile(FICHERO, "r")) {
            int estado = obtenerEstado(file, numeroDepartamento);

            if (estado == ACTIVA) {
                file.seek(calcularDesplazamiento(numeroDepartamento));
                short identificador = file.readShort();
                String nombre = leerTextoRegistro(file).trim();
                String localidad = leerTextoRegistro(file).trim();
                short numeroEmpleados = file.readShort();

                System.out.printf("Departamento: %d | Nombre: %s | Localidad: %s | Empleados: %d%n",
                        identificador, nombre, localidad, numeroEmpleados);
            } else {
                mostrarMensajeNoActivo(estado, numeroDepartamento);
            }
        }
    }

    public static void actualizaNumeroEmpleados(int numeroDepartamento, int nuevoNumeroEmpleados)
            throws IOException {
        validarNumeroDepartamento(numeroDepartamento);
        validarNumeroEmpleados(nuevoNumeroEmpleados);

        try (RandomAccessFile file = new RandomAccessFile(FICHERO, "rw")) {
            int estado = obtenerEstado(file, numeroDepartamento);

            if (estado == ACTIVA) {

                file.seek(calcularDesplazamiento(numeroDepartamento) + DESPLAZAMIENTO_EMPLEADOS);
                file.writeShort(nuevoNumeroEmpleados);
                System.out.println("Numero de empleados del departamento " + numeroDepartamento
                        + " actualizado a " + nuevoNumeroEmpleados + ".");
            } else {
                mostrarMensajeNoActivo(estado, numeroDepartamento);
            }
        }
    }


    public static void actualizaNombreLocalidad(int numeroDepartamento, String nuevoNombre,
                                                String nuevaLocalidad) throws IOException {
        validarNumeroDepartamento(numeroDepartamento);
        String nombreValido = validarTexto(nuevoNombre, "nombre");
        String localidadValida = validarTexto(nuevaLocalidad, "localidad");

        try (RandomAccessFile file = new RandomAccessFile(FICHERO, "rw")) {
            int estado = obtenerEstado(file, numeroDepartamento);

            if (estado == ACTIVA) {

                file.seek(calcularDesplazamiento(numeroDepartamento) + DESPLAZAMIENTO_NOMBRE);
                file.writeChars(rellenarTexto(nombreValido));
                file.writeChars(rellenarTexto(localidadValida));
                System.out.println("Nombre y localidad del departamento " + numeroDepartamento
                        + " actualizados.");
            } else {
                mostrarMensajeNoActivo(estado, numeroDepartamento);
            }
        }
    }

    public static void insertaDepartamento(int numeroDepartamento, String nombre, String localidad,
                                           int numeroEmpleados) throws IOException {

        validarNumeroDepartamento(numeroDepartamento);
        validarNumeroEmpleados(numeroEmpleados);
        String nombreValido = validarTexto(nombre, "nombre");
        String localidadValida = validarTexto(localidad, "localidad");

        try (RandomAccessFile file = new RandomAccessFile(FICHERO, "rw")) {
            int estado = obtenerEstado(file, numeroDepartamento);

            if (estado == ACTIVA) {
                System.out.println("El departamento " + numeroDepartamento
                        + " ya existe. No se modifica el fichero.");
                return;
            }
            if (estado == ANOMALA) {
                mostrarMensajeNoActivo(estado, numeroDepartamento);
                return;
            }

            if (estado == FUERA_DEL_FICHERO) {
                long posicionesExistentes = file.length() / LONGITUD_REGISTRO;
                long posicionesCreadas = numeroDepartamento - 1 - posicionesExistentes;

                file.seek(file.length());
                for (long posicion = posicionesExistentes + 1; posicion < numeroDepartamento; posicion++) {
                    escribirPosicionVacia(file);
                }
                if (posicionesCreadas > 0) {
                    System.out.println("Se han inicializado " + posicionesCreadas
                            + " posiciones intermedias como nunca utilizadas.");
                }
            }

            escribirRegistro(file, numeroDepartamento, nombreValido, localidadValida, numeroEmpleados);
            System.out.println("Departamento " + numeroDepartamento + " insertado correctamente.");
        }
    }

    public static void eliminaDepartamento(int numeroDepartamento) throws IOException {
        validarNumeroDepartamento(numeroDepartamento);

        try (RandomAccessFile file = new RandomAccessFile(FICHERO, "rw")) {
            int estado = obtenerEstado(file, numeroDepartamento);

            if (estado == ACTIVA) {
                file.seek(calcularDesplazamiento(numeroDepartamento));
                file.writeShort(-1);
                file.writeChars(MARCA_BORRADO);
                file.writeChars(MARCA_BORRADO);
                file.writeShort(-1);
                System.out.println("Departamento " + numeroDepartamento + " eliminado.");
            } else {
                mostrarMensajeNoActivo(estado, numeroDepartamento);
            }
        }
    }

    public static void listadoDepartamentos() throws IOException {
        int contadorMostrados = 0;

        try (RandomAccessFile file = new RandomAccessFile(FICHERO, "r")) {
            long numeroPosiciones = file.length() / LONGITUD_REGISTRO;

            for (long posicion = 1; posicion <= numeroPosiciones; posicion++) {
                short identificador = file.readShort();
                String nombre = leerTextoRegistro(file);
                String localidad = leerTextoRegistro(file);
                short numeroEmpleados = file.readShort();

                if (clasificarPosicion(posicion, identificador, nombre, localidad, numeroEmpleados) == ACTIVA) {
                    System.out.printf("Departamento: %d | Nombre: %s | Localidad: %s | Empleados: %d%n",
                            identificador, nombre.trim(), localidad.trim(), numeroEmpleados);
                    contadorMostrados++;
                }
            }
        }

        if (contadorMostrados == 0) {
            System.out.println("No hay ningun departamento activo.");
        } else {
            System.out.println("Departamentos mostrados: " + contadorMostrados);
        }
    }

    public static void inspeccionFisica() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(FICHERO, "r")) {
            long longitud = file.length();
            long numeroPosiciones = longitud / LONGITUD_REGISTRO;

            if (longitud == 0) {
                System.out.println("El fichero está vacio.");
                return;
            }

            for (long posicion = 1; posicion <= numeroPosiciones; posicion++) {
                long desplazamiento = (posicion - 1) * LONGITUD_REGISTRO;
                short identificador = file.readShort();
                String nombre = leerTextoRegistro(file);
                String localidad = leerTextoRegistro(file);
                short numeroEmpleados = file.readShort();

                int estado = clasificarPosicion(posicion, identificador, nombre, localidad, numeroEmpleados);

                System.out.printf("Posicion %d | Desplazamiento: %d bytes | Identificador esperado: %d "
                                + "| Identificador almacenado: %d | Estado: %s%n",
                        posicion, desplazamiento, posicion, identificador, describirEstado(estado));
            }

            long bytesSobrantes = longitud % LONGITUD_REGISTRO;
            if (bytesSobrantes != 0) {
                System.out.printf("Posicion %d | Desplazamiento: %d bytes | Registro incompleto "
                                + "(%d bytes de %d) | Estado: anómala%n",
                        numeroPosiciones + 1, numeroPosiciones * LONGITUD_REGISTRO, bytesSobrantes,
                        LONGITUD_REGISTRO);
            }
        }
    }

    public static void estadisticas() throws IOException {
        long longitud;
        int activas = 0;
        int eliminadas = 0;
        int nuncaUtilizadas = 0;
        int anomalas = 0;
        long sumaEmpleados = 0;

        try (RandomAccessFile file = new RandomAccessFile(FICHERO, "r")) {
            longitud = file.length();
            long numeroPosiciones = longitud / LONGITUD_REGISTRO;

            for (long posicion = 1; posicion <= numeroPosiciones; posicion++) {
                short identificador = file.readShort();
                String nombre = leerTextoRegistro(file);
                String localidad = leerTextoRegistro(file);
                short numeroEmpleados = file.readShort();

                switch (clasificarPosicion(posicion, identificador, nombre, localidad, numeroEmpleados)) {
                    case ACTIVA:
                        activas++;
                        sumaEmpleados += numeroEmpleados;
                        break;
                    case ELIMINADA:
                        eliminadas++;
                        break;
                    case NUNCA_UTILIZADA:
                        nuncaUtilizadas++;
                        break;
                    default:
                        anomalas++;
                }
            }
        }

        System.out.println("Longitud del fichero: " + longitud + " bytes");
        System.out.println("Departamentos activos: " + activas);
        System.out.println("Posiciones eliminadas: " + eliminadas);
        System.out.println("Posiciones nunca utilizadas: " + nuncaUtilizadas);
        System.out.println("Posiciones anomalas: " + anomalas);
        System.out.println("Suma de empleados de los departamentos activos: " + sumaEmpleados);

        int posicionesLibres = eliminadas + nuncaUtilizadas;
        if (posicionesLibres == 0) {
            System.out.println("Porcentaje de ocupacion: no calculable (no hay posiciones eliminadas "
                    + "ni nunca utilizadas).");
        } else {
            System.out.printf("Porcentaje de ocupacion (activas respecto a eliminadas mas nunca "
                    + "utilizadas): %.2f %%%n", (double) activas * 100 / posicionesLibres);
        }
    }

    public static int integridad() throws IOException {
        int numeroProblemas = 0;

        try (RandomAccessFile file = new RandomAccessFile(FICHERO, "r")) {
            long longitud = file.length();
            long numeroPosiciones = longitud / LONGITUD_REGISTRO;

            for (long posicion = 1; posicion <= numeroPosiciones; posicion++) {
                long desplazamiento = (posicion - 1) * LONGITUD_REGISTRO;
                short identificador = file.readShort();
                String nombre = leerTextoRegistro(file);
                String localidad = leerTextoRegistro(file);
                short numeroEmpleados = file.readShort();

                if (clasificarPosicion(posicion, identificador, nombre, localidad, numeroEmpleados) == ANOMALA) {
                    numeroProblemas++;
                    System.out.println("Problema en el byte " + desplazamiento + " (posicion " + posicion + "): "
                            + describirAnomalia(posicion, identificador, nombre, localidad, numeroEmpleados));
                }
            }

            if (longitud % LONGITUD_REGISTRO != 0) {
                numeroProblemas++;
                System.out.println("Problema en el byte " + numeroPosiciones * LONGITUD_REGISTRO
                        + ": la longitud del fichero (" + longitud + " bytes) no es multiplo de "
                        + LONGITUD_REGISTRO + ".");
            }
        }

        if (numeroProblemas == 0) {
            System.out.println("Integridad correcta: no se han detectado problemas.");
        } else {
            System.out.println("Total de problemas detectados: " + numeroProblemas
                    + ". El fichero no se ha modificado.");
        }
        return numeroProblemas;
    }
}
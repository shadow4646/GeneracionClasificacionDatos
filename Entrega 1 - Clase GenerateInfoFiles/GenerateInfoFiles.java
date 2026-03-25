import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * Clase GenerateInfoFiles
 *
 * <p>Esta clase contiene los metodos necesarios para generar archivos planos
 * pseudoaleatorios que sirven como entrada al programa principal de gestion
 * de ventas. Al ejecutarse, genera automaticamente los archivos de vendedores,
 * productos e informacion de vendedores en la carpeta del proyecto.</p>
 *
 * <p>Proyecto: Generacion y clasificacion de datos</p>
 * <p>Modulo: Conceptos Fundamentales de Programacion</p>
 * <p>Institucion: Politecnico Grancolombiano</p>
 *
 * @author Mateo
 * @version 1.0
 */
public class GenerateInfoFiles {

    // -------------------------------------------------------------------------
    // Constantes de configuracion
    // -------------------------------------------------------------------------

    /** Numero de vendedores a generar por defecto */
    private static final int DEFAULT_SALESMAN_COUNT = 5;

    /** Numero de productos a generar por defecto */
    private static final int DEFAULT_PRODUCTS_COUNT = 10;

    /** Numero maximo de ventas aleatorias por vendedor */
    private static final int MAX_SALES_PER_VENDOR = 8;

    /** Precio minimo de un producto */
    private static final int MIN_PRODUCT_PRICE = 1000;

    /** Precio maximo de un producto */
    private static final int MAX_PRODUCT_PRICE = 500000;

    /** Cantidad maxima vendida de un producto por transaccion */
    private static final int MAX_QUANTITY_PER_SALE = 20;

    /** Nombre del archivo de informacion de vendedores */
    private static final String SALESMEN_INFO_FILE = "salesmen_info.txt";

    /** Nombre del archivo de informacion de productos */
    private static final String PRODUCTS_FILE = "products.txt";

    /** Tipos de documento disponibles */
    private static final String[] DOCUMENT_TYPES = {"CC", "CE", "PP", "TI"};

    /** Lista de nombres reales para generacion pseudoaleatoria */
    private static final String[] FIRST_NAMES = {
        "Carlos", "Maria", "Juan", "Laura", "Andres",
        "Sofia", "Miguel", "Valentina", "Luis", "Daniela",
        "Jorge", "Camila", "Ricardo", "Paula", "Felipe",
        "Isabella", "Sergio", "Natalia", "Diego", "Alejandra"
    };

    /** Lista de apellidos reales para generacion pseudoaleatoria */
    private static final String[] LAST_NAMES = {
        "Garcia", "Rodriguez", "Martinez", "Lopez", "Gonzalez",
        "Perez", "Sanchez", "Ramirez", "Torres", "Flores",
        "Rivera", "Gomez", "Diaz", "Reyes", "Morales",
        "Cruz", "Ortiz", "Vargas", "Castillo", "Ramos"
    };

    /** Lista de nombres de productos reales para generacion pseudoaleatoria */
    private static final String[] PRODUCT_NAMES = {
        "Laptop", "Mouse", "Teclado", "Monitor", "Auriculares",
        "Silla Ergonomica", "Escritorio", "Webcam", "Microfono", "Impresora",
        "Tablet", "Smartphone", "Cable HDMI", "Hub USB", "Disco Duro Externo",
        "Memoria USB", "Router WiFi", "Poder", "Cooler", "Tarjeta Grafica"
    };

    /** Instancia compartida de Random para toda la clase */
    private static final Random RANDOM = new Random();

    // -------------------------------------------------------------------------
    // Metodo principal
    // -------------------------------------------------------------------------

    /**
     * Metodo principal de la clase GenerateInfoFiles.
     *
     * <p>Al ejecutarse, genera todos los archivos planos necesarios como entrada
     * para el programa principal. Los datos de vendedores se pre-generan en memoria
     * para garantizar coherencia entre el archivo de informacion de vendedores y
     * los archivos individuales de ventas (mismo ID y nombre en ambos).</p>
     *
     * <p>No solicita ninguna informacion al usuario.</p>
     *
     * @param args Argumentos de linea de comandos (no se utilizan)
     */
    public static void main(String[] args) {
        System.out.println("=== Generando archivos de prueba ===");

        try {
            // 1. Generar archivo de productos
            createProductsFile(DEFAULT_PRODUCTS_COUNT);
            System.out.println("[OK] Archivo de productos generado: " + PRODUCTS_FILE);

            // 2. Pre-generar datos de vendedores en memoria para garantizar coherencia:
            //    los mismos IDs y nombres deben aparecer en el archivo de info y en
            //    cada archivo individual de ventas.
            String[] docTypes   = new String[DEFAULT_SALESMAN_COUNT];
            long[]   docNumbers = new long[DEFAULT_SALESMAN_COUNT];
            String[] firstNames = new String[DEFAULT_SALESMAN_COUNT];
            String[] lastNames  = new String[DEFAULT_SALESMAN_COUNT];

            for (int i = 0; i < DEFAULT_SALESMAN_COUNT; i++) {
                docTypes[i]   = DOCUMENT_TYPES[RANDOM.nextInt(DOCUMENT_TYPES.length)];
                docNumbers[i] = 10000000L + (long) (RANDOM.nextDouble() * 90000000L);
                firstNames[i] = FIRST_NAMES[RANDOM.nextInt(FIRST_NAMES.length)];
                lastNames[i]  = LAST_NAMES[RANDOM.nextInt(LAST_NAMES.length)];
            }

            // 3. Escribir archivo de informacion de vendedores con los datos pre-generados
            writeSalesManInfoFile(docTypes, docNumbers, firstNames, lastNames);
            System.out.println("[OK] Archivo de informacion de vendedores generado: " + SALESMEN_INFO_FILE);

            // 4. Generar un archivo de ventas por cada vendedor usando los MISMOS datos
            for (int i = 0; i < DEFAULT_SALESMAN_COUNT; i++) {
                int salesCount = 1 + RANDOM.nextInt(MAX_SALES_PER_VENDOR);
                createSalesMenFile(salesCount, firstNames[i], docNumbers[i]);
                System.out.println("[OK] Archivo de ventas generado: "
                        + firstNames[i] + "_" + docNumbers[i] + ".txt");
            }

            System.out.println("\n=== Generacion de archivos completada exitosamente ===");

        } catch (IOException e) {
            System.err.println("[ERROR] Ocurrio un problema al generar los archivos: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Metodos de generacion de archivos
    // -------------------------------------------------------------------------

    /**
     * Escribe el archivo de informacion de vendedores usando datos pre-generados.
     *
     * <p>Este metodo privado permite que {@code main} garantice coherencia entre
     * el archivo de info de vendedores y los archivos individuales de ventas,
     * ya que ambos usan los mismos IDs y nombres.</p>
     *
     * @param docTypes   Tipos de documento de cada vendedor
     * @param docNumbers Numeros de documento de cada vendedor
     * @param firstNames Nombres de cada vendedor
     * @param lastNames  Apellidos de cada vendedor
     * @throws IOException Si ocurre un error al escribir el archivo
     */
    private static void writeSalesManInfoFile(String[] docTypes, long[] docNumbers,
            String[] firstNames, String[] lastNames) throws IOException {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SALESMEN_INFO_FILE))) {
            for (int i = 0; i < docTypes.length; i++) {
                writer.write(docTypes[i] + ";" + docNumbers[i] + ";"
                           + firstNames[i] + ";" + lastNames[i]);
                writer.newLine();
            }
        }
    }

    /**
     * Crea un archivo pseudoaleatorio de ventas para un vendedor especifico.
     *
     * <p>El archivo generado tiene el formato requerido por el programa principal:</p>
     * <pre>
     * TipoDocumentoVendedor;NumeroDocumentoVendedor
     * IDProducto1;CantidadProducto1Vendido;
     * IDProducto2;CantidadProducto2Vendido;
     * </pre>
     *
     * <p>Incluye validacion: no se generan cantidades negativas ni IDs de producto
     * fuera del rango de productos existentes.</p>
     *
     * @param randomSalesCount Numero de lineas de venta a generar
     * @param name             Nombre del vendedor (usado en el nombre del archivo)
     * @param id               Numero de documento del vendedor
     * @throws IOException Si ocurre un error al escribir el archivo
     */
    public static void createSalesMenFile(int randomSalesCount, String name, long id)
            throws IOException {

        // Validacion de parametros de entrada
        if (randomSalesCount <= 0) {
            throw new IllegalArgumentException(
                "La cantidad de ventas debe ser mayor a cero. Valor recibido: " + randomSalesCount
            );
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del vendedor no puede estar vacio.");
        }
        if (id <= 0) {
            throw new IllegalArgumentException(
                "El ID del vendedor debe ser un numero positivo. Valor recibido: " + id
            );
        }

        String fileName = name + "_" + id + ".txt";
        String documentType = DOCUMENT_TYPES[RANDOM.nextInt(DOCUMENT_TYPES.length)];

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {

            // Primera linea: tipo y numero de documento del vendedor
            writer.write(documentType + ";" + id);
            writer.newLine();

            // Lineas de ventas pseudoaleatorias
            for (int i = 0; i < randomSalesCount; i++) {
                // ID de producto dentro del rango valido (1 a DEFAULT_PRODUCTS_COUNT)
                int productId = 1 + RANDOM.nextInt(DEFAULT_PRODUCTS_COUNT);

                // Cantidad vendida: siempre positiva (validacion de coherencia)
                int quantity = 1 + RANDOM.nextInt(MAX_QUANTITY_PER_SALE);

                writer.write(productId + ";" + quantity + ";");
                writer.newLine();
            }
        }
    }

    /**
     * Crea un archivo con informacion pseudoaleatoria de productos.
     *
     * <p>El archivo generado tiene el formato:</p>
     * <pre>
     * IDProducto1;NombreProducto1;PrecioPorUnidadProducto1
     * IDProducto2;NombreProducto2;PrecioPorUnidadProducto2
     * </pre>
     *
     * <p>Validacion incluida: el precio siempre es positivo y el ID es secuencial.</p>
     *
     * @param productsCount Numero de productos a generar
     * @throws IOException Si ocurre un error al escribir el archivo
     */
    public static void createProductsFile(int productsCount) throws IOException {

        // Validacion del parametro de entrada
        if (productsCount <= 0) {
            throw new IllegalArgumentException(
                "El numero de productos debe ser mayor a cero. Valor recibido: " + productsCount
            );
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PRODUCTS_FILE))) {

            for (int i = 1; i <= productsCount; i++) {
                // Nombre del producto tomado de la lista o generado si se agota
                String productName;
                if (i <= PRODUCT_NAMES.length) {
                    productName = PRODUCT_NAMES[i - 1];
                } else {
                    productName = "Producto_" + i;
                }

                // Precio positivo entre MIN y MAX (validacion de coherencia)
                int price = MIN_PRODUCT_PRICE
                        + RANDOM.nextInt(MAX_PRODUCT_PRICE - MIN_PRODUCT_PRICE + 1);

                writer.write(i + ";" + productName + ";" + price);
                writer.newLine();
            }
        }
    }

    /**
     * Crea un archivo con informacion pseudoaleatoria de vendedores.
     *
     * <p>El archivo generado tiene el formato:</p>
     * <pre>
     * TipoDocumento;NumeroDocumento;NombresVendedor;ApellidosVendedor
     * </pre>
     *
     * <p>Los nombres y apellidos son extraidos de listas de nombres reales colombianos.
     * Los numeros de documento son generados aleatoriamente pero coherentes
     * (siempre positivos y de longitud realista).</p>
     *
     * @param salesmanCount Numero de vendedores a generar
     * @throws IOException Si ocurre un error al escribir el archivo
     */
    public static void createSalesManInfoFile(int salesmanCount) throws IOException {

        // Validacion del parametro de entrada
        if (salesmanCount <= 0) {
            throw new IllegalArgumentException(
                "El numero de vendedores debe ser mayor a cero. Valor recibido: " + salesmanCount
            );
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SALESMEN_INFO_FILE))) {

            for (int i = 0; i < salesmanCount; i++) {
                String documentType = DOCUMENT_TYPES[RANDOM.nextInt(DOCUMENT_TYPES.length)];

                // Numero de documento: 8 digitos coherentes (siempre positivo)
                long documentNumber = 10000000L + (long)(RANDOM.nextDouble() * 90000000L);

                String firstName  = FIRST_NAMES[RANDOM.nextInt(FIRST_NAMES.length)];
                String lastName   = LAST_NAMES[RANDOM.nextInt(LAST_NAMES.length)];

                writer.write(documentType + ";" + documentNumber + ";"
                           + firstName + ";" + lastName);
                writer.newLine();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Metodo auxiliar de validacion (Extra: deteccion de incoherencias)
    // -------------------------------------------------------------------------

    /**
     * Valida que los parametros para un archivo de ventas sean coherentes.
     *
     * <p>Este metodo verifica que no existan valores negativos o nulos que pudieran
     * generar archivos con formato erroneo. Es parte del extra de validacion solicitado.</p>
     *
     * @param productId ID del producto a validar
     * @param quantity  Cantidad vendida a validar
     * @param maxProductId ID maximo valido de producto (segun productos generados)
     * @return {@code true} si los valores son coherentes; {@code false} en caso contrario
     */
    public static boolean isValidSaleEntry(int productId, int quantity, int maxProductId) {
        if (productId <= 0) {
            System.err.println("[VALIDACION] ID de producto invalido: " + productId);
            return false;
        }
        if (productId > maxProductId) {
            System.err.println("[VALIDACION] ID de producto " + productId
                    + " no existe. Maximo valido: " + maxProductId);
            return false;
        }
        if (quantity <= 0) {
            System.err.println("[VALIDACION] Cantidad invalida (debe ser positiva): " + quantity);
            return false;
        }
        return true;
    }
}


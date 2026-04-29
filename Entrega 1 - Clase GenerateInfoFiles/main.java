import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Clase main: procesador de reportes de ventas.
 *
 * <p>Lee los archivos planos generados por {@link GenerateInfoFiles} y produce
 * dos reportes CSV ordenados:</p>
 * <ul>
 *   <li><b>reporte_vendedores.csv</b>: vendedores ordenados por total de ventas
 *       de mayor a menor. Formato por linea: {@code NombreApellido;TotalVentas}</li>
 *   <li><b>reporte_productos.csv</b>: productos ordenados por cantidad total
 *       vendida de mayor a menor. Formato por linea: {@code Nombre;Precio}</li>
 * </ul>
 *
 * <p>Archivos de entrada esperados en la carpeta del proyecto:</p>
 * <ul>
 *   <li>{@code salesmen_info.txt}: informacion de vendedores.</li>
 *   <li>{@code products.txt}: catalogo de productos.</li>
 *   <li>{@code Nombre_NumDoc.txt}: un archivo por cada vendedor con sus ventas.</li>
 * </ul>
 *
 * <p><b>Extras implementados:</b></p>
 * <ul>
 *   <li><b>(a) Multiples archivos por vendedor:</b> si {@code GenerateInfoFiles}
 *       genera mas de un archivo para el mismo vendedor (igual numero de documento
 *       en el encabezado), este programa los detecta y acumula las ventas de todos
 *       ellos en el mismo total.</li>
 *   <li><b>(c) Deteccion de formato incorrecto e informacion incoherente:</b>
 *       ID de producto inexistente en el catalogo, cantidades no positivas,
 *       campos faltantes o precio no numerico.</li>
 * </ul>
 *
 * <p>El programa no solicita informacion al usuario.</p>
 *
 * <p>Proyecto: Generacion y clasificacion de datos</p>
 * <p>Modulo: Conceptos Fundamentales de Programacion</p>
 * <p>Institucion: Politecnico Grancolombiano</p>
 *
 * @author Mateo
 * @version 1.0
 * @see GenerateInfoFiles
 */
public class main {

    // -------------------------------------------------------------------------
    // Constantes de configuracion
    // -------------------------------------------------------------------------

    /** Nombre del archivo de entrada con informacion de vendedores. */
    private static final String SALESMEN_INFO_FILE = "salesmen_info.txt";

    /** Nombre del archivo de entrada con el catalogo de productos. */
    private static final String PRODUCTS_FILE = "products.txt";

    /** Nombre del archivo CSV de salida: reporte de vendedores. */
    private static final String SALESMEN_REPORT = "reporte_vendedores.csv";

    /** Nombre del archivo CSV de salida: reporte de productos. */
    private static final String PRODUCTS_REPORT = "reporte_productos.csv";

    /** Separador de campos utilizado en todos los archivos planos. */
    private static final String SEPARATOR = ";";

    /** Numero de campos esperados en una linea del catalogo de productos. */
    private static final int PRODUCT_LINE_FIELDS = 3;

    /** Numero de campos esperados en una linea del archivo de info de vendedores. */
    private static final int SALESMAN_INFO_LINE_FIELDS = 4;

    /** Numero de campos esperados en la primera linea de un archivo de ventas. */
    private static final int SALES_HEADER_FIELDS = 2;

    /** Numero de campos esperados en una linea de venta (tras quitar el ';' final). */
    private static final int SALE_ENTRY_FIELDS = 2;

    // -------------------------------------------------------------------------
    // Metodo principal
    // -------------------------------------------------------------------------

    /**
     * Punto de entrada del programa.
     *
     * <p>Orquesta la lectura de archivos, el calculo de totales y la escritura
     * de los reportes CSV. Muestra un mensaje de finalizacion exitosa o un
     * mensaje de error si algo sale mal.</p>
     *
     * @param args argumentos de linea de comandos (no se utilizan)
     */
    public static void main(String[] args) {
        System.out.println("=== Procesando archivos de ventas ===");

        try {
            // Paso 1: cargar catalogo de productos
            Map<String, String[]> products = readProducts();

            // Paso 2: cargar informacion de vendedores
            Map<String, String> salesmenNames = readSalesmenInfo();

            // Paso 3: preparar acumuladores de totales
            Map<String, Double> salesmenTotals   = new HashMap<String, Double>();
            Map<String, Long>   productQuantities = new HashMap<String, Long>();

            // Paso 4: procesar cada archivo de ventas individual
            File[] salesFiles = getSalesFiles();

            if (salesFiles.length == 0) {
                System.err.println("[ADVERTENCIA] No se encontraron archivos de ventas"
                        + " en la carpeta del proyecto.");
            }

            for (File salesFile : salesFiles) {
                processSalesFile(salesFile, products, salesmenNames,
                        salesmenTotals, productQuantities);
            }

            // Paso 5: escribir reportes CSV
            writeSalesmenReport(salesmenTotals, salesmenNames);
            writeProductsReport(productQuantities, products);

            System.out.println("\n=== Proceso finalizado exitosamente ===");
            System.out.println("    Reportes generados: " + SALESMEN_REPORT
                    + " y " + PRODUCTS_REPORT);

        } catch (IOException e) {
            System.err.println("[ERROR] Problema de entrada/salida: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[ERROR] Error inesperado: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Metodos de lectura de archivos de entrada
    // -------------------------------------------------------------------------

    /**
     * Lee el catalogo de productos desde {@value #PRODUCTS_FILE}.
     *
     * <p>Cada linea tiene el formato {@code ID;Nombre;Precio}. Las lineas con
     * formato incorrecto o precio no positivo son omitidas con una advertencia.</p>
     *
     * @return mapa donde la clave es el ID del producto (String) y el valor es
     *         un arreglo de dos elementos: {@code {nombre, precioStr}}
     * @throws IOException si el archivo no puede ser leido
     */
    private static Map<String, String[]> readProducts() throws IOException {
        Map<String, String[]> products = new HashMap<String, String[]>();

        try (BufferedReader reader = new BufferedReader(new FileReader(PRODUCTS_FILE))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                String[] fields = line.split(SEPARATOR);

                if (fields.length != PRODUCT_LINE_FIELDS) {
                    System.err.println("[ADVERTENCIA] " + PRODUCTS_FILE + " linea " + lineNumber
                            + ": se esperaban " + PRODUCT_LINE_FIELDS + " campos, se encontraron "
                            + fields.length + ". Linea omitida: \"" + line + "\"");
                    continue;
                }

                String productId   = fields[0].trim();
                String productName = fields[1].trim();
                String priceStr    = fields[2].trim();

                // Validar que el precio sea un numero positivo
                try {
                    double price = Double.parseDouble(priceStr);
                    if (price <= 0) {
                        System.err.println("[ADVERTENCIA] " + PRODUCTS_FILE + " linea " + lineNumber
                                + ": precio no positivo (" + price + ") para producto '"
                                + productId + "'. Linea omitida.");
                        continue;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("[ADVERTENCIA] " + PRODUCTS_FILE + " linea " + lineNumber
                            + ": precio no numerico '" + priceStr + "'. Linea omitida.");
                    continue;
                }

                products.put(productId, new String[]{productName, priceStr});
            }
        }

        System.out.println("[OK] Productos cargados: " + products.size());
        return products;
    }

    /**
     * Lee la informacion de vendedores desde {@value #SALESMEN_INFO_FILE}.
     *
     * <p>Cada linea tiene el formato {@code TipoDoc;NumDoc;Nombre;Apellido}.
     * Las lineas con formato incorrecto son omitidas con una advertencia.</p>
     *
     * @return mapa donde la clave es el numero de documento (String) y el valor
     *         es el nombre completo {@code "Nombre Apellido"}
     * @throws IOException si el archivo no puede ser leido
     */
    private static Map<String, String> readSalesmenInfo() throws IOException {
        Map<String, String> salesmenNames = new HashMap<String, String>();

        try (BufferedReader reader = new BufferedReader(new FileReader(SALESMEN_INFO_FILE))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                String[] fields = line.split(SEPARATOR);

                if (fields.length != SALESMAN_INFO_LINE_FIELDS) {
                    System.err.println("[ADVERTENCIA] " + SALESMEN_INFO_FILE + " linea "
                            + lineNumber + ": se esperaban " + SALESMAN_INFO_LINE_FIELDS
                            + " campos, se encontraron " + fields.length
                            + ". Linea omitida: \"" + line + "\"");
                    continue;
                }

                String docNumber = fields[1].trim();
                String fullName  = fields[2].trim() + " " + fields[3].trim();

                salesmenNames.put(docNumber, fullName);
            }
        }

        System.out.println("[OK] Vendedores cargados: " + salesmenNames.size());
        return salesmenNames;
    }

    // -------------------------------------------------------------------------
    // Metodo para identificar archivos de ventas
    // -------------------------------------------------------------------------

    /**
     * Obtiene todos los archivos de ventas individuales presentes en la
     * carpeta actual del proyecto.
     *
     * <p>Se incluyen todos los archivos {@code .txt} excepto los archivos
     * de entrada conocidos ({@value #SALESMEN_INFO_FILE}, {@value #PRODUCTS_FILE})
     * y el archivo de conclusiones requerido por la entrega final.</p>
     *
     * @return arreglo de archivos de ventas encontrados (puede estar vacio)
     */
    private static File[] getSalesFiles() {
        final Set<String> excludedFiles = new HashSet<String>(Arrays.asList(
                SALESMEN_INFO_FILE,
                PRODUCTS_FILE,
                SALESMEN_REPORT,
                PRODUCTS_REPORT,
                "conslusion.txt"
        ));

        File currentDir  = new File(".");
        File[] candidates = currentDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".txt") && !excludedFiles.contains(name);
            }
        });

        if (candidates == null) {
            return new File[0];
        }

        System.out.println("[OK] Archivos de ventas encontrados: " + candidates.length);
        return candidates;
    }

    // -------------------------------------------------------------------------
    // Metodo de procesamiento de ventas por vendedor
    // -------------------------------------------------------------------------

    /**
     * Procesa un archivo de ventas individual y acumula los totales.
     *
     * <p>Formato esperado del archivo:</p>
     * <pre>
     * TipoDocumentoVendedor;NumeroDocumentoVendedor
     * IDProducto1;CantidadProducto1Vendido;
     * IDProducto2;CantidadProducto2Vendido;
     * </pre>
     *
     * <p><b>Validaciones incluidas (Extra c):</b></p>
     * <ul>
     *   <li>Encabezado con exactamente dos campos.</li>
     *   <li>ID de producto existente en el catalogo.</li>
     *   <li>Cantidad numerica y positiva.</li>
     *   <li>Numero de documento presente en el archivo de info de vendedores.</li>
     * </ul>
     *
     * <p>Las entradas invalidas se reportan como advertencias y se omiten sin
     * detener el procesamiento del resto del archivo.</p>
     *
     * @param salesFile         archivo de ventas a procesar
     * @param products          catalogo de productos cargado desde {@value #PRODUCTS_FILE}
     * @param salesmenNames     mapa de documento a nombre completo del vendedor
     * @param salesmenTotals    acumulador de total de dinero recaudado por documento
     * @param productQuantities acumulador de cantidad total vendida por producto
     * @throws IOException si el archivo no puede ser leido
     */
    private static void processSalesFile(
            File salesFile,
            Map<String, String[]> products,
            Map<String, String> salesmenNames,
            Map<String, Double> salesmenTotals,
            Map<String, Long> productQuantities) throws IOException {

        try (BufferedReader reader = new BufferedReader(new FileReader(salesFile))) {

            // --- Leer y validar encabezado (primera linea) ---
            String header = reader.readLine();

            if (header == null || header.trim().isEmpty()) {
                System.err.println("[ADVERTENCIA] Archivo vacio o sin encabezado: "
                        + salesFile.getName() + ". Archivo omitido.");
                return;
            }

            header = header.trim();
            String[] headerFields = header.split(SEPARATOR);

            if (headerFields.length != SALES_HEADER_FIELDS) {
                System.err.println("[ADVERTENCIA] Encabezado invalido en "
                        + salesFile.getName() + ": \"" + header
                        + "\". Se esperaban " + SALES_HEADER_FIELDS
                        + " campos. Archivo omitido.");
                return;
            }

            String docNumber = headerFields[1].trim();

            // Verificar coherencia: el documento debe existir en salesmen_info.txt
            if (!salesmenNames.containsKey(docNumber)) {
                System.err.println("[ADVERTENCIA] Numero de documento '" + docNumber
                        + "' del archivo " + salesFile.getName()
                        + " no existe en " + SALESMEN_INFO_FILE + ".");
            }

            // Recuperar total acumulado previo para este vendedor
            double vendorTotal = salesmenTotals.containsKey(docNumber)
                    ? salesmenTotals.get(docNumber) : 0.0;

            // --- Leer lineas de ventas ---
            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                // Eliminar el punto y coma final si existe (formato del enunciado)
                if (line.endsWith(SEPARATOR)) {
                    line = line.substring(0, line.length() - 1);
                }

                if (line.isEmpty()) {
                    continue;
                }

                String[] fields = line.split(SEPARATOR);

                if (fields.length != SALE_ENTRY_FIELDS) {
                    System.err.println("[ADVERTENCIA] " + salesFile.getName()
                            + " linea " + lineNumber + ": formato de venta invalido \""
                            + line + "\". Linea omitida.");
                    continue;
                }

                String productId  = fields[0].trim();
                String quantityStr = fields[1].trim();

                // Validar existencia del producto en el catalogo
                if (!products.containsKey(productId)) {
                    System.err.println("[ADVERTENCIA] " + salesFile.getName()
                            + " linea " + lineNumber + ": ID de producto '"
                            + productId + "' no existe en el catalogo. Linea omitida.");
                    continue;
                }

                // Validar que la cantidad sea un entero positivo
                long quantity;
                try {
                    quantity = Long.parseLong(quantityStr);
                } catch (NumberFormatException e) {
                    System.err.println("[ADVERTENCIA] " + salesFile.getName()
                            + " linea " + lineNumber + ": cantidad no numerica '"
                            + quantityStr + "'. Linea omitida.");
                    continue;
                }

                if (quantity <= 0) {
                    System.err.println("[ADVERTENCIA] " + salesFile.getName()
                            + " linea " + lineNumber + ": cantidad no positiva ("
                            + quantity + "). Linea omitida.");
                    continue;
                }

                // Acumular total del vendedor
                double unitPrice = Double.parseDouble(products.get(productId)[1]);
                vendorTotal += quantity * unitPrice;

                // Acumular cantidad vendida del producto
                long accumulated = productQuantities.containsKey(productId)
                        ? productQuantities.get(productId) : 0L;
                productQuantities.put(productId, accumulated + quantity);
            }

            salesmenTotals.put(docNumber, vendorTotal);
        }
    }

    // -------------------------------------------------------------------------
    // Metodos de escritura de reportes CSV
    // -------------------------------------------------------------------------

    /**
     * Genera el reporte CSV de vendedores ordenado por total de ventas descendente.
     *
     * <p>Formato de cada linea: {@code NombreApellido;TotalVentas}</p>
     * <p>Archivo de salida: {@value #SALESMEN_REPORT}</p>
     *
     * @param salesmenTotals mapa de numero de documento a total de ventas en pesos
     * @param salesmenNames  mapa de numero de documento a nombre completo
     * @throws IOException si el archivo no puede ser escrito
     */
    private static void writeSalesmenReport(
            Map<String, Double> salesmenTotals,
            Map<String, String> salesmenNames) throws IOException {

        // Convertir el mapa a lista para poder ordenarla
        List<Map.Entry<String, Double>> entries =
                new ArrayList<Map.Entry<String, Double>>(salesmenTotals.entrySet());

        // Ordenar de mayor a menor total de ventas
        Collections.sort(entries, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> a, Map.Entry<String, Double> b) {
                return Double.compare(b.getValue(), a.getValue());
            }
        });

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SALESMEN_REPORT))) {
            for (Map.Entry<String, Double> entry : entries) {
                String docNumber = entry.getKey();
                double total     = entry.getValue();

                // Usar nombre del archivo de info; si no esta, indicar documento desconocido
                String name = salesmenNames.containsKey(docNumber)
                        ? salesmenNames.get(docNumber)
                        : "Desconocido_" + docNumber;

                writer.write(name + SEPARATOR + String.format("%.2f", total));
                writer.newLine();
            }
        }

        System.out.println("[OK] Reporte de vendedores generado: " + SALESMEN_REPORT
                + " (" + entries.size() + " vendedores)");
    }

    /**
     * Genera el reporte CSV de productos ordenado por cantidad vendida descendente.
     *
     * <p>Formato de cada linea: {@code NombreProducto;PrecioPorUnidad}</p>
     * <p>Archivo de salida: {@value #PRODUCTS_REPORT}</p>
     *
     * @param productQuantities mapa de ID de producto a cantidad total vendida
     * @param products          catalogo de productos con nombre y precio
     * @throws IOException si el archivo no puede ser escrito
     */
    private static void writeProductsReport(
            Map<String, Long> productQuantities,
            Map<String, String[]> products) throws IOException {

        // Convertir el mapa a lista para poder ordenarla
        List<Map.Entry<String, Long>> entries =
                new ArrayList<Map.Entry<String, Long>>(productQuantities.entrySet());

        // Ordenar de mayor a menor cantidad vendida
        Collections.sort(entries, new Comparator<Map.Entry<String, Long>>() {
            @Override
            public int compare(Map.Entry<String, Long> a, Map.Entry<String, Long> b) {
                return Long.compare(b.getValue(), a.getValue());
            }
        });

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PRODUCTS_REPORT))) {
            for (Map.Entry<String, Long> entry : entries) {
                String productId = entry.getKey();
                String[] data    = products.get(productId);

                if (data == null) {
                    continue;
                }

                String productName = data[0];
                String price       = data[1];

                writer.write(productName + SEPARATOR + price);
                writer.newLine();
            }
        }

        System.out.println("[OK] Reporte de productos generado: " + PRODUCTS_REPORT
                + " (" + entries.size() + " productos)");
    }
}

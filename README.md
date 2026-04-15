# Generación y Clasificación de Datos de Vendedores

**Módulo:** Conceptos Fundamentales de Programación — Java  
**Institución:** Politécnico Grancolombiano  
**Lenguaje:** Java SE 8  
**IDE:** Eclipse

---

## Descripción general

Sistema de generación y clasificación de datos de vendedores compuesto por dos clases principales:

| Clase | Entrega | Función |
|---|---|---|
| `GenerateInfoFiles.java` | Entrega 1 (Semana 3) | Genera archivos de texto plano con datos pseudoaleatorios de vendedores y productos |
| `main.java` | Entrega 2 | Lee los archivos generados y produce reportes CSV ordenados por ventas |

---

## Entrega 1 — `GenerateInfoFiles.java`

### Archivos generados al ejecutarse

| Archivo | Formato | Descripción |
|---|---|---|
| `salesmen_info.txt` | `TipoDoc;NumDoc;Nombre;Apellido` | Información de 5 vendedores |
| `products.txt` | `ID;Nombre;Precio` | Catálogo de 10 productos |
| `Nombre_NumDoc.txt` | `TipoDoc;NumDoc` / `IDProducto;Cantidad;` | Un archivo por vendedor con sus ventas |

### Ejemplo de salida

**salesmen_info.txt**
```
TI;92020696;Andres;Flores
CC;83546770;Miguel;Rivera
TI;96680729;Natalia;Flores
```

**products.txt**
```
1;Laptop;245788
2;Mouse;470108
3;Teclado;410520
```

**Andres_92020696.txt**
```
TI;92020696
1;2;
8;4;
9;2;
```

### Constantes configurables

| Constante | Valor por defecto | Descripción |
|---|---|---|
| `DEFAULT_SALESMAN_COUNT` | 5 | Número de vendedores |
| `DEFAULT_PRODUCTS_COUNT` | 10 | Número de productos |
| `MAX_SALES_PER_VENDOR` | 8 | Máximo de ventas por vendedor |
| `MIN_PRODUCT_PRICE` | 1,000 | Precio mínimo (COP) |
| `MAX_PRODUCT_PRICE` | 500,000 | Precio máximo (COP) |
| `MAX_QUANTITY_PER_SALE` | 20 | Cantidad máxima por transacción |

### Decisión de diseño clave

Los datos de los vendedores se **pre-generan en memoria** antes de escribir cualquier archivo. Esto garantiza que el tipo y número de documento en `salesmen_info.txt` coincida exactamente con el encabezado de cada archivo de ventas individual.

---

## Entrega 2 — `main.java`

### Archivos de entrada requeridos

- `salesmen_info.txt`
- `products.txt`
- Un archivo `Nombre_NumDoc.txt` por cada vendedor

### Reportes generados

| Reporte | Formato | Ordenamiento |
|---|---|---|
| `reporte_vendedores.csv` | `NombreApellido;TotalVentas` | Mayor a menor total de ventas |
| `reporte_productos.csv` | `NombreProducto;Precio` | Mayor a menor cantidad vendida |

### Validaciones implementadas

- Formato incorrecto de líneas (número de campos inesperado)
- ID de producto inexistente en el catálogo
- Cantidades no numéricas o no positivas
- Número de documento en archivo de ventas no registrado en `salesmen_info.txt`
- Precio de producto no positivo o no numérico

---

## Instrucciones de ejecución

### Prerequisitos

- Java SE 8 o superior
- Eclipse IDE

### Pasos

1. Clonar el repositorio:
   ```
   git clone https://github.com/shadow4646/GeneracionClasificacionDatos
   ```
2. En Eclipse: `File` → `Import` → `Existing Projects into Workspace` → seleccionar la carpeta clonada.
3. Para la **Entrega 1**: ejecutar `GenerateInfoFiles.java` como Java Application — genera todos los archivos `.txt`.
4. Para la **Entrega 2**: ejecutar `main.java` como Java Application — lee los `.txt` y genera los reportes `.csv`.

### Salida esperada en consola (Entrega 1)

```
=== Generando archivos de prueba ===
[OK] Archivo de productos generado: products.txt
[OK] Archivo de informacion de vendedores generado: salesmen_info.txt
[OK] Archivo de ventas generado: Andres_92020696.txt
[OK] Archivo de ventas generado: Miguel_83546770.txt
...
=== Generacion de archivos completada exitosamente ===
```

---

## Estructura del repositorio

```
GeneracionClasificacionDatos/
├── Entrega 1 - Clase GenerateInfoFiles/
│   ├── GenerateInfoFiles.java    # Generador de archivos planos (Entrega 1)
│   └── main.java                 # Procesador de reportes (Entrega 2)
├── conslusion.txt                # Conclusiones del proyecto (Entrega 3)
└── README.md
```

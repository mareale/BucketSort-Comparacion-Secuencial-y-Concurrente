package test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;

public class TestBucketSort {
	
	// Generador de array aleatorio de tamaño especificado
	public static float[] generarArrayAleatorio(int tamaño) {
        float[] arr = new float[tamaño];
        Random rand = new Random();
        // Recorremos el array agregando valores random
        for (int i = 0; i < tamaño; i++) {
            arr[i] = rand.nextFloat(); // Valor entre 0.0 (inclusive) y 1.0 (exclusivo)
        }

        return arr;
    }
    
    // Generador de array con distribución uniforme óptima para BucketSort
    // Garantiza que cada bucket tenga aproximadamente la misma cantidad de elementos
    public static float[] generarArrayUniforme(int tamaño) {
        float[] arr = new float[tamaño];
        Random rand = new Random();
        
        int bucketDestino = 0;
        float rangoMin = 0;
        float rangoMax = 0;
        float valorEnRango = 0;
        // Para lograr distribución uniforme, generamos valores en rangos específicos
        for (int i = 0; i < tamaño; i++) {
            // Calculamos en qué bucket debería caer este elemento
            bucketDestino = i % tamaño;
            
            // Generamos un valor que caiga específicamente en ese bucket
            rangoMin = (float) bucketDestino / tamaño;
            rangoMax = (float) (bucketDestino + 1) / tamaño;
            
            // Valor aleatorio dentro del rango del bucket específico
            valorEnRango = rangoMin + rand.nextFloat() * (rangoMax - rangoMin);
            
            // Asegurar que no llegue exactamente a 1.0 (fuera de rango)
            if (valorEnRango >= 1.0f) {
                valorEnRango = 0.999999f;
            }
            
            arr[i] = valorEnRango;
        }
        
        return arr;
    }
    // Generador de array donde todos los valores caen en el último bucket (El peor de los casos)
    public static float[] generarArrayUltimoBucket(int tamaño) {
        float[] arr = new float[tamaño];
        Random rand = new Random();

        // El último bucket es el de índice (tamaño - 1)
        int bucketUltimo = tamaño - 1;
        float rangoMin = (float) bucketUltimo / tamaño;
        float rangoMax = (float) (bucketUltimo + 1) / tamaño;
        float valorEnRango = 0;
        // Recorremos el array agregando valores dentro del rango del ultimo bucket
        for (int i = 0; i < tamaño; i++) {
            valorEnRango = rangoMin + rand.nextFloat() * (rangoMax - rangoMin);

            // Aseguramos que no sea exactamente 1.0
            if (valorEnRango >= 1.0f) {
                valorEnRango = 0.999999f;
            }

            arr[i] = valorEnRango;
        }

        return arr;
    }
    
    // Analizador de distribución de buckets
    public static void analizarDistribucion(float[] arr, String descripcion) {
        int n = arr.length;
        int[] contadorBuckets = new int[n];
        int bucketIndex = 0;
        // Contar elementos por bucket
        for (float valor : arr) {
            bucketIndex = (int) (n * valor);
            if (bucketIndex >= n) bucketIndex = n - 1; // Protección contra índice fuera de rango
            contadorBuckets[bucketIndex]++;
        }
        
        // Calcular estadísticas
        int min = Integer.MAX_VALUE;
        int max = 0;
        int bucketsVacios = 0;
        double suma = 0;
        
        for (int count : contadorBuckets) {
            if (count == 0) {
                bucketsVacios++;
            } else {
                min = Math.min(min, count);
                max = Math.max(max, count);
            }
            suma += count;
        }
        
        double promedio = suma / n;
        
        // Calcular desviación estándar
        double sumaDesviaciones = 0;
        for (int count : contadorBuckets) {
            sumaDesviaciones += Math.pow(count - promedio, 2);
        }
        double desviacionEstandar = Math.sqrt(sumaDesviaciones / n);
        
        System.out.println("--- ANÁLISIS DE DISTRIBUCIÓN: " + descripcion + " ---");
        System.out.println("Total de buckets: " + n);
        System.out.println("Buckets vacíos: " + bucketsVacios + " (" + String.format("%.1f", (bucketsVacios * 100.0 / n)) + "%)");
        System.out.println("Elementos por bucket - Min: " + (min == Integer.MAX_VALUE ? 0 : min) + 
                          ", Max: " + max);
        System.out.println("Desviación estándar: " + String.format("%.2f", desviacionEstandar));
        System.out.println("Coeficiente de variación: " + String.format("%.2f", (desviacionEstandar / promedio) * 100) + "%");
    }
	
    // Función de ordenamiento por inserción para ordenar buckets individuales
    public static void insertionSort(List<Float> bucket) {
        // Recorre desde el segundo elemento hasta el final del bucket
        for (int i = 1; i < bucket.size(); ++i) {
            // Elemento actual a insertar en la posición correcta
            float key = bucket.get(i);
            // Índice del elemento anterior
            int j = i - 1;
            
            // Mueve los elementos mayores que key una posición hacia adelante
            while (j >= 0 && bucket.get(j) > key) {
                bucket.set(j + 1, bucket.get(j));
                j--;
            }
            // Inserta key en su posición correcta
            bucket.set(j + 1, key);
        }
    }

    // VERSIÓN SECUENCIAL - Función para ordenar arr[] de tamaño n usando bucket sort secuencial
    public static void bucketSort(float[] arr) {
        // Obtiene el tamaño del array
        int n = arr.length;

        // 1) Crear n buckets vacíos, misma cantidad de buckets que el tamaño
        List<Float>[] buckets = new ArrayList[n];
        for (int i = 0; i < n; i++) {
            buckets[i] = new ArrayList<>();
        }
        int bi;
        // 2) Distribuir elementos del array en diferentes buckets
        for (int i = 0; i < n; i++) {
            // Calcula el índice del bucket basado en el valor del elemento
            bi = (int) (n * arr[i]);
            // Añade el elemento al bucket correspondiente
            buckets[bi].add(arr[i]);
        }

        // 3) Ordenar buckets individuales usando insertion sort
        for (int i = 0; i < n; i++) {
            insertionSort(buckets[i]);
        }

        // 4) Concatenar todos los buckets en arr[]
        int index = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < buckets[i].size(); j++) {
                arr[index++] = buckets[i].get(j);
            }
        }
    }

    // VERSIÓN CONCURRENTE - Función para ordenar arr[] de tamaño n usando bucket sort paralelo
    public static void bucketSortConcurrent(float[] arr) {
        // Obtiene el tamaño del array
        int n = arr.length;

        // 1) Crear n buckets vacíos, misma cantidad de buckets que el tamaño
        List<Float>[] buckets = new ArrayList[n];
        for (int i = 0; i < n; i++) {
            buckets[i] = new ArrayList<>();
        }

        // 2) Distribuir elementos del array en diferentes buckets (secuencial)
        for (int i = 0; i < n; i++) {
            // Calcula el índice del bucket basado en el valor del elemento
            int bi = (int) (n * arr[i]);
            // Añade el elemento al bucket correspondiente
            buckets[bi].add(arr[i]);
        }

        // 3) Crear un pool de threads para ordenar buckets en paralelo
        // Número de threads igual al número de procesadores disponibles
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        
        // Lista para almacenar las tareas Future
        List<Future<Void>> futures = new ArrayList<>();

        // Enviar cada bucket no vacío como una tarea al pool de threads
        for (int i = 0; i < n; i++) {
            // Solo procesar buckets que no estén vacíos
            if (!buckets[i].isEmpty()) {
                // Crear una referencia final para usar en la lambda
                final List<Float> bucket = buckets[i];
                // Enviar tarea de ordenamiento al executor
                Future<Void> future = executor.submit(() -> {
                    insertionSort(bucket);
                    return null;
                });
                // Añadir la tarea a la lista de futures
                futures.add(future);
            }
        }
        // Esperar a que todas las tareas terminen
        for (Future<Void> future : futures) {
            try {
                // Bloquea hasta que la tarea termine
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                // Manejo de excepciones en caso de error en los threads
                System.err.println("Error en el procesamiento paralelo: " + e.getMessage());
                e.printStackTrace();
            }
        }
        // Cerrar el executor para liberar recursos
        executor.shutdown();

        // 4) Concatenar todos los buckets en arr[]
        int index = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < buckets[i].size(); j++) {
                arr[index++] = buckets[i].get(j);
            }
        }
    }
    
    // Método para realizar una prueba completa con un tamaño específico y tipo de distribución
    public static void realizarPrueba(int tam, String descripcion, boolean usarDistribucionUniforme) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("PRUEBA: " + descripcion + " - Tamaño del array: " + tam);
        System.out.println("Tipo de distribución: " + (usarDistribucionUniforme ? "UNIFORME (Optimizada)" : "ALEATORIA"));
        System.out.println("=".repeat(80));

        // Generar array de prueba
        float[] arrayOriginal = usarDistribucionUniforme ? 
                               generarArrayUniforme(tam) : 
                               generarArrayAleatorio(tam);
        
        // Analizar distribución de buckets
        analizarDistribucion(arrayOriginal, usarDistribucionUniforme ? "Distribución Uniforme" : "Distribución Aleatoria");
        
        // Crear copias para cada versión
        float[] arrSecuencial = arrayOriginal.clone();
        float[] arrConcurrente = arrayOriginal.clone();

        System.out.println("\nArray generado con " + tam + " elementos entre 0.0 y 1.0");
        System.out.println("Número de buckets que se crearán: " + tam);

        // ============= VERSIÓN SECUENCIAL =============
        System.out.println("\n--- EJECUTANDO VERSIÓN SECUENCIAL ---");
        
        // Medir tiempo de ejecución secuencial
        long inicioSecuencial = System.nanoTime();
        bucketSort(arrSecuencial);
        long tiempoSecuencial = System.nanoTime() - inicioSecuencial;
        
        System.out.println("Tiempo secuencial: " + String.format("%.2f", tiempoSecuencial/1000.0) + " microsegundos");

        // ============= VERSIÓN CONCURRENTE =============
        System.out.println("\n--- EJECUTANDO VERSIÓN CONCURRENTE ---");
        
        // Medir tiempo de ejecución concurrente
        long inicioConcurrente = System.nanoTime();
        bucketSortConcurrent(arrConcurrente);
        long tiempoConcurrente = System.nanoTime() - inicioConcurrente;
        
        System.out.println("Tiempo concurrente: " + String.format("%.2f", tiempoConcurrente/1000.0) + " microsegundos");

        // ============= ANÁLISIS COMPARATIVO =============
        System.out.println("\n--- ANÁLISIS COMPARATIVO ---");
        
        // Verificar si ambos arrays están ordenados correctamente
        boolean resultadosIguales = java.util.Arrays.equals(arrSecuencial, arrConcurrente);
        System.out.println("¿Ambos algoritmos produjeron el mismo resultado? " + resultadosIguales);
        
        // Calcular mejora de rendimiento (speedup)
        double speedup = (double) tiempoSecuencial / tiempoConcurrente;
        System.out.println("Speedup (mejora de rendimiento): " + String.format("%.2f", speedup) + "x");
        
        // Determinar cuál fue más rápido
        if (tiempoConcurrente < tiempoSecuencial) {
            System.out.println("✓ La versión CONCURRENTE fue más rápida");
            double mejoraPorcentual = ((double)(tiempoSecuencial - tiempoConcurrente) / tiempoSecuencial) * 100;
            System.out.println("  Mejora: " + String.format("%.1f", mejoraPorcentual) + "% más rápida");
        } else if (tiempoSecuencial < tiempoConcurrente) {
            System.out.println("✓ La versión SECUENCIAL fue más rápida");
            double penalizacionPorcentual = ((double)(tiempoConcurrente - tiempoSecuencial) / tiempoSecuencial) * 100;
            System.out.println("  La versión concurrente fue " + String.format("%.1f", penalizacionPorcentual) + "% más lenta");
            System.out.println("  (Posiblemente debido al overhead de creación y gestión de threads)");
        } else {
            System.out.println("✓ Ambas versiones tuvieron rendimiento similar");
        }
    }
    
    // Método para realizar comparación completa entre distribuciones
    public static void compararDistribuciones(int tam, String descripcion) {
        System.out.println("\n" + "█".repeat(90));
        System.out.println("COMPARACIÓN COMPLETA: " + descripcion + " - Tamaño: " + tam);
        System.out.println("█".repeat(90));
        
        System.out.println("\n" + "─".repeat(90));
        
        // Prueba con distribución aleatoria
        realizarPrueba(tam, descripcion + " (Aleatoria)", false);
        
        // Prueba con distribución uniforme
        realizarPrueba(tam, descripcion + " (Uniforme)", true);
        
        System.out.println("\n" + "─".repeat(90));
     
    }
 // Método para realizar una prueba completa con un tamaño específico y tipo de distribución
    public static void realizarPruebaPeorDeLosCasos(int tam, String descripcion) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("PRUEBA: " + descripcion + " - Tamaño del array: " + tam);
        System.out.println("Tipo de distribución: Todo en el ultimo bucket");
        System.out.println("=".repeat(80));

        // Generar array de prueba
        float[] arrayOriginal = generarArrayUltimoBucket(tam);
        
        // Analizar distribución de buckets
        analizarDistribucion(arrayOriginal, "Distribución Todo en el Ultimo Bucket");
        
        // Crear copias para cada versión
        float[] arrSecuencial = arrayOriginal.clone();
        float[] arrConcurrente = arrayOriginal.clone();

        System.out.println("\nArray generado con " + tam + " elementos entre 0.0 y 1.0");
        System.out.println("Número de buckets que se crearán: " + tam);

        // ============= VERSIÓN SECUENCIAL =============
        System.out.println("\n--- EJECUTANDO VERSIÓN SECUENCIAL ---");
        
        // Medir tiempo de ejecución secuencial
        long inicioSecuencial = System.nanoTime();
        bucketSort(arrSecuencial);
        long tiempoSecuencial = System.nanoTime() - inicioSecuencial;
        
        System.out.println("Tiempo secuencial: " + String.format("%.2f", tiempoSecuencial/1000.0) + " microsegundos");

        // ============= VERSIÓN CONCURRENTE =============
        System.out.println("\n--- EJECUTANDO VERSIÓN CONCURRENTE ---");
        
        // Medir tiempo de ejecución concurrente
        long inicioConcurrente = System.nanoTime();
        bucketSortConcurrent(arrConcurrente);
        long tiempoConcurrente = System.nanoTime() - inicioConcurrente;
        
        System.out.println("Tiempo concurrente: " + String.format("%.2f", tiempoConcurrente/1000.0) + " microsegundos");

        // ============= ANÁLISIS COMPARATIVO =============
        System.out.println("\n--- ANÁLISIS COMPARATIVO ---");
        
        // Verificar si ambos arrays están ordenados correctamente
        boolean resultadosIguales = java.util.Arrays.equals(arrSecuencial, arrConcurrente);
        System.out.println("¿Ambos algoritmos produjeron el mismo resultado? " + resultadosIguales);
        
        // Calcular mejora de rendimiento (speedup)
        double speedup = (double) tiempoSecuencial / tiempoConcurrente;
        System.out.println("Speedup (mejora de rendimiento): " + String.format("%.2f", speedup) + "x");
        
        // Determinar cuál fue más rápido
        if (tiempoConcurrente < tiempoSecuencial) {
            System.out.println("✓ La versión CONCURRENTE fue más rápida");
            double mejoraPorcentual = ((double)(tiempoSecuencial - tiempoConcurrente) / tiempoSecuencial) * 100;
            System.out.println("  Mejora: " + String.format("%.1f", mejoraPorcentual) + "% más rápida");
        } else if (tiempoSecuencial < tiempoConcurrente) {
            System.out.println("✓ La versión SECUENCIAL fue más rápida");
            double penalizacionPorcentual = ((double)(tiempoConcurrente - tiempoSecuencial) / tiempoSecuencial) * 100;
            System.out.println("  La versión concurrente fue " + String.format("%.1f", penalizacionPorcentual) + "% más lenta");
            System.out.println("  (Posiblemente debido al overhead de creación y gestión de threads)");
        } else {
            System.out.println("✓ Ambas versiones tuvieron rendimiento similar");
        }
    }
       
    // Programa principal para probar ambas versiones: secuencial y concurrente
    public static void main(String[] args) {
        
    	System.out.println("ANÁLISIS COMPLETO BUCKETSORT: SECUENCIAL VS CONCURRENTE");
        System.out.println("Número de threads disponibles: " + Runtime.getRuntime().availableProcessors());
        System.out.println("Comparación de rendimiento con distribuciones ALEATORIA vs UNIFORME");

        // Realizar comparaciones completas con diferentes tamaños, y array aleatorio y uniforme
        compararDistribuciones(100, "Array Pequeño");
        compararDistribuciones(1000, "Array Mediano");
        compararDistribuciones(10000, "Array Grande");
        compararDistribuciones(100000, "Array Muy Grande");
        compararDistribuciones(1000000, "Array Enorme");
        compararDistribuciones(10000000, "Array Muy Enorme");

        // Comparacion entre secuencial y concurrente en el peor de los casos
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Rendimiento con distribuciones Todo en el Ultimo Bucket");
        System.out.println("⚠️  ATENCIÓN: Las siguientes pruebas simulan el PEOR CASO posible");
        System.out.println("    Todos los elementos caen en el mismo bucket (el último)");
        System.out.println("    Por rendimiento, se limitan a tamaños de array moderados");
        System.out.println("=".repeat(80));
        realizarPruebaPeorDeLosCasos(100, "Array Pequeño");
        realizarPruebaPeorDeLosCasos(1000, "Array Mediano");
        realizarPruebaPeorDeLosCasos(10000, "Array Grande");
        realizarPruebaPeorDeLosCasos(100000, "Array Muy Grande");

    }
}
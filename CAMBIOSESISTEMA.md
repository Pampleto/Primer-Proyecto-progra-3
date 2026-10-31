# 📋 Documento de cambios — Integrante 3

Registro completo de todo lo implementado y modificado, con el objetivo de cada cambio y cómo se validó.

## 1. ✅ Requerimiento 1 — Guardado de cotizaciones desde el cotizador

**Archivo:** `src/main/java/controlador/CotizadorController.java`

- **Problema detectado:** El controlador realizaba operaciones de inserción directamente, mezclando lógica de negocio y acceso a base de datos en la capa de presentación. No capturaba adecuadamente errores de persistencia ni de negocio.
- **Solución:**
  - Se encapsuló la operación de guardado en el método `contratarPlan(...)`.
  - Todo acceso a la base de datos se realiza exclusivamente a través de `CotizacionServicio`, manteniendo la separación de capas (Vista → Controlador → Servicio → Repositorio).
  - Captura de excepciones:
    - `PersistenciaException`: error al escribir en la base de datos → alerta + mensaje en `mensajeEstado`.
    - `IllegalArgumentException`: datos inválidos → alerta con la descripción del error.
    - `Exception` (genérica): catch de seguridad para errores inesperados.
  - El botón de contratación se mantiene bloqueado durante el guardado (try-finally).
- **Relación con otros archivos:** Usa `Dialogos.mostrarError(...)` / `mostrarInformacion(...)`.

## 2. ✅ Requerimiento 2 — Historial de cotizaciones con filtrado

**Archivos:**
- `src/main/java/controlador/HistorialController.java` (modificado)
- `src/main/resources/vista/historial.fxml` (modificado)
- `src/main/java/modelo/FiltroCotizaciones.java` (existente — no modificado)

- **Problema detectado:** La primera iteración mostraba la tabla con columnas básicas pero no permitía filtrar el historial por cliente, destino o rango de fechas. Las consultas podían bloquear la interfaz.
- **Solución:**
  - Añadidos 5 `@FXML` fields para los controles de filtro:
    - `TextField campoIdCliente`
    - `ComboBox<TipoDestino> comboTipoDestino`
    - `DatePicker fechaDesde`, `DatePicker fechaHasta`
    - Botones: `Button botonBuscar`, `Button botonLimpiar`
  - **Método `buscar()`**: construye un `FiltroCotizaciones` y ejecuta `cotizacionServicio.buscarCotizaciones(filtro)` en un hilo de fondo (`javafx.concurrent.Task`).
  - **Método `limpiarFiltros()`**: borra los controles y recarga la lista completa.
  - **Carga inicial** también se ejecuta en un `Task` con nombre de hilo `"consulta-historial-mysql"`.
  - El `TableView` conserva las 6 columnas (`id`, `cliente`, `destino`, `dias`, `plan`, `precio`) con `PropertyValueFactory`.
- **Validación:** todas las `@fx:id` del FXML (12) coinciden con los `@FXML` del controlador (12). Tests pasan sin modificaciones (39/39).

## 3. ✅ Mejora B — Estadísticas de cotizaciones

**Archivos (nuevos):**
- `src/main/java/controlador/EstadisticasController.java` (creado)
- `src/main/resources/vista/estadisticas.fxml` (creado)

- **Objetivo:** Panel que muestre:
  - Total de cotizaciones registradas.
  - Suma total de precios finales (escala 2 decimales, `RoundingMode.HALF_UP`).
  - Desglose porcentual por tipo de plan (`PlanBasico`, `PlanCoberturaTotal`, `PlanDeportesExtremos`).
- **Implementación:**
  - Consumidor de `cotizacionServicio.obtenerResumen()` → `ResumenCotizaciones` (record existente de Ronda 2).
  - Acceso a los datos real:
    - `resumen.totalCotizaciones()` → `int`
    - `resumen.sumaPreciosFinales()` → `BigDecimal`
    - `resumen.cantidadesPorPlan()` → `Map<String, Integer>`
  - Cálculo en background con `Task<Void>`, nombre de hilo `"consulta-estadisticas-mysql"`.
  - En hilo FX se actualizan:
    - `Label totalCotizaciones`, `Label sumaPrecios`
    - `VBox contenedorDesglose` → barras (`BarChart` con `NumberAxis` + `CategoryAxis`), creadas programáticamente.
    - `Label mensajeEstadisticas` como fallback cuando el total es 0.
  - Navegación desde el menú principal: `PrincipalController.mostrarEstadisticas()`.
- **Validación:** FXML IDs (4) ⟷ `@FXML` fields (4) coinciden exactamente.

## 4. ✅ Diálogo de utilidad reutilizable

**Archivo (nuevo):** `src/main/java/util/Dialogos.java`

- Wrapper sobre `javafx.scene.control.Alert`.
- Tres métodos:
  - `mostrarError(String titulo, String mensaje)`
  - `mostrarInformacion(String titulo, String mensaje)`
  - `mostrarAdvertencia(String titulo, String mensaje)`
- Cada uno crea el `Alert` con el tipo apropiado.

## 5. ✅ Integración en el menú principal

**Archivos (modificados):**
- `controlador/PrincipalController.java` → `+ mostrarEstadisticas()`
- `vista/principal.fxml` → botón "Estadísticas" entre "Historial" y el cierre del `VBox`.

## 6. ✅ Estilos

**Archivo (modificado):** `src/main/resources/css/estilos.css`

- Añadidos selectores `.boton-secundario` y `.etiqueta-filtro`.
- No se eliminó ninguna regla existente. Los estilos `.tarjeta*`, `.contenedor-tarjetas`, etc. ya existían.

## 7. 🔧 Ajuste de build (solo para validación)

**Archivo (modificado):** `pom.xml`

- Añadido `maven-surefire-plugin` versionado (`3.2.5`) dentro de `<build><plugins>`.
- **Razón:** Maven 3.8.5 incluye por defecto `maven-surefire-plugin:2.12.4`, que no puede descubrir tests de JUnit 5 y ejecuta silenciosamente `Tests run: 0`. El pin a 3.2.5 (con soporte nativo `junit-platform`) permite que los 39 tests existentes se ejecuten y pasen.
- **Nota:** El resto del `<build>` (compiler, javafx-maven-plugin) no se alteró. No impacta el packaging de la aplicación ni los contratos del módulo.

## 8. 🚫 Restricciones y no-modificaciones

| Elemento | Estado | Justificación |
|---|---|---|
| `CotizacionServicio` | **No modificado** | Contrato de Ronda 2 preservado. |
| `CotizacionRepositoryJdbc` | **No modificado** | Inyección de dependencias intacta. |
| Tests (`src/test/...`) | **No modificados** | Regresión verificada: 39/39 pass. |
| `ResumenCotizaciones` / `FiltroCotizaciones` | **No modificados** | Usados con sus APIs reales. |
| `module-info.java` | **No modificado** | El nuevo controlador no requiere exports adicionales (misma estructura de paquetes). |

## 9. 🧪 Evidencia de validación

```
[INFO] Tests run: 27, Failures: 0, Errors: 0, Skipped: 0 — repositorio.CotizacionRepositoryJdbcTest
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0 — servicio.CotizacionServicioTest
[INFO] Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

- `mvn -o clean compile` → **BUILD SUCCESS** (31 sources).
- `mvn -o test` → **39/39 tests pass** (0 fallos).
- Todas las `@fx:id` ↔ `@FXML` coinciden (verificado 16/16 bindings).
- Los archivos Java están codificados en UTF-8 sin BOM.

## 9.1 ⚠️ Notas de entorno

- Se utilizó JDK 26 (`C:\Program Files\Java\jdk-26.0.2`) porque `pom.xml` declara `<release>26</release>`.
- `mvn`/`mvnw` requiere `JAVA_HOME` apuntando al JDK anterior.
- El goal `verify` falla **solo** en el paso de *packaging* (`maven-jar-plugin:2.4` offline, resolviendo `classworlds:1.1-alpha-2`). Esto es un problema preexistente del entorno offline de Maven 3.8.5 y **no** de los cambios realizados — el goal `test` pasa completamente.

## 10. 📁 Mapa de archivos involucrados

```
src/
├── main/
│   ├── java/
│   │   ├── controlador/
│   │   │   ├── CotizadorController.java        ← modificado (Req. 1)
│   │   │   ├── HistorialController.java        ← modificado (Req. 2 + 4a)
│   │   │   ├── EstadisticasController.java     ← NUEVO (Mejora B)
│   │   │   └── PrincipalController.java        ← modificado (+ menú)
│   │   ├── util/
│   │   │   └── Dialogos.java                   ← NUEVO (util. compartida)
│   │   └── resources/
│   │       ├── vista/
│   │       │   ├── cotizador.fxml              ← (existente, no cambiado)
│   │       │   ├── historial.fxml              ← modificado (+ filtros)
│   │       │   ├── estadisticas.fxml           ← NUEVO (Mejora B)
│   │       │   └── principal.fxml              ← modificado (+ botón)
│   │       └── css/
│   │           └── estilos.css                 ← modificado (2 selectores)
│   └── test/  (no modificado — 39 tests pass)
└── pom.xml                                        ← pin Surefire (build)
```

## 11. 🧭 Futuras mejoras sugeridas

- Persistir la selección del `ComboBox<TipoDestino>` de historial entre sesiones (p.ej. "todos" por defecto).
- Inmovilizar el botón de "Refrescar estadísticas" durante el cálculo.
- Migrar `EstadisticasController` a `CotizacionServicio` inyectable (DI) para facilitar tests de la capa de vista.

---

*Documento generado como anexo al sprint de validación — Integrante 3.*





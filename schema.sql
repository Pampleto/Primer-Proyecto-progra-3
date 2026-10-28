-- =====================================================
-- TRAVELSAFE 2.0
-- Proyecto 2 - Programacion III
-- Integrantes:
-- Eduardo Sánches Montero
-- Juan José Villagra Conejo
-- Johnny Chaves Vega
-- Base de Datos Principal
-- =====================================================

-- 1. Base de datos
CREATE DATABASE IF NOT EXISTS travelsafe_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE travelsafe_db;

-- 2. Clientes
-- El identificador lo genera MySQL (no el contador de Java).
CREATE TABLE IF NOT EXISTS clientes (
    id INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL,
    edad INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_clientes_edad CHECK (edad > 0)
) ENGINE = InnoDB;

-- 3. Catalogo de planes
-- Los IDs 1, 2 y 3 identifican los tipos concretos en Java.
-- Las tarifas iniciales y los nombres coinciden con las clases
-- PlanBasico, PlanCoberturaTotal y PlanDeportesExtremos.
CREATE TABLE IF NOT EXISTS planes_seguro (
    id INT NOT NULL,
    nombre VARCHAR(80) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    tarifa_diaria DECIMAL(12, 2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_planes_seguro_nombre UNIQUE (nombre),
    CONSTRAINT chk_planes_seguro_tarifa CHECK (tarifa_diaria > 0)
) ENGINE = InnoDB;

-- Solo inserta los registros de catalogo que falten.
-- No sobreescribe datos existentes al volver a ejecutar el script.
INSERT IGNORE INTO planes_seguro (id, nombre, descripcion, tarifa_diaria)
VALUES
    (1, 'Plan Basico',
        'Cobertura basica para emergencias durante el viaje', 4000.00),
    (2, 'Cobertura Total',
        'Cobertura amplia para emergencias, asistencia y gastos medicos', 7500.00),
    (3, 'Deportes Extremos',
        'Cobertura especial para actividades de mayor riesgo', 12000.00);

-- 4. Cotizaciones contratadas
-- El viaje consta de destino (NACIONAL/INTERNACIONAL) y dias.
-- El precio_final guarda el precio historico de la contratacion.
-- fecha_contratacion permite filtros por fecha y estadisticas.
CREATE TABLE IF NOT EXISTS cotizaciones (
    id INT NOT NULL AUTO_INCREMENT,
    cliente_id INT NOT NULL,
    plan_id INT NOT NULL,
    tipo_destino ENUM('NACIONAL', 'INTERNACIONAL') NOT NULL,
    cantidad_dias INT NOT NULL,
    precio_final DECIMAL(12, 2) NOT NULL,
    fecha_contratacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_cotizaciones_cliente (cliente_id),
    INDEX idx_cotizaciones_plan (plan_id),
    INDEX idx_cotizaciones_fecha (fecha_contratacion),
    CONSTRAINT chk_cotizaciones_dias CHECK (cantidad_dias > 0),
    CONSTRAINT chk_cotizaciones_precio CHECK (precio_final > 0),
    CONSTRAINT fk_cotizaciones_cliente
        FOREIGN KEY (cliente_id) REFERENCES clientes (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_cotizaciones_plan
        FOREIGN KEY (plan_id) REFERENCES planes_seguro (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE = InnoDB;

-- 5. Comprobaciones de la instalacion
SELECT DATABASE() AS base_datos_actual;
SHOW TABLES;
SELECT id, nombre, tarifa_diaria FROM planes_seguro ORDER BY id;

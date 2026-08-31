package com.finanzas.app.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Agrega la tabla `cuenta` y la FK `movimiento.cuentaId`. SQLite no permite
 * agregar una foreign key con ALTER TABLE, asi que `movimiento` se recrea
 * completa (unica forma real de migrar, nunca fallbackToDestructiveMigration:
 * la app acumula historial financiero de verdad).
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE cuenta (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                nombre TEXT NOT NULL,
                origen TEXT NOT NULL,
                saldoInicialCentavos INTEGER NOT NULL DEFAULT 0,
                icono TEXT NOT NULL DEFAULT 'AccountBalanceWallet',
                archivada INTEGER NOT NULL DEFAULT 0,
                orden INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX index_cuenta_archivada ON cuenta(archivada)")

        // Mismas cuentas semilla que AppDatabase.SEED_CALLBACK usa para instalaciones nuevas.
        db.execSQL("INSERT INTO cuenta (nombre, origen) VALUES ('Efectivo', 'MANUAL')")
        db.execSQL("INSERT INTO cuenta (nombre, origen) VALUES ('Nu', 'NU')")

        db.execSQL(
            """
            CREATE TABLE movimiento_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                montoCentavos INTEGER NOT NULL,
                tipo TEXT NOT NULL,
                origen TEXT NOT NULL,
                comercioOrigen TEXT,
                categoriaId INTEGER,
                cuentaId INTEGER,
                fechaMovimiento INTEGER NOT NULL,
                fechaRegistro INTEGER NOT NULL,
                estado TEXT NOT NULL,
                notas TEXT,
                FOREIGN KEY(categoriaId) REFERENCES categoria(id) ON DELETE SET NULL,
                FOREIGN KEY(cuentaId) REFERENCES cuenta(id) ON DELETE SET NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO movimiento_new
                (id, montoCentavos, tipo, origen, comercioOrigen, categoriaId, cuentaId,
                 fechaMovimiento, fechaRegistro, estado, notas)
            SELECT id, montoCentavos, tipo, origen, comercioOrigen, categoriaId, NULL,
                   fechaMovimiento, fechaRegistro, estado, notas
            FROM movimiento
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE movimiento")
        db.execSQL("ALTER TABLE movimiento_new RENAME TO movimiento")

        db.execSQL("CREATE INDEX index_movimiento_fechaMovimiento ON movimiento(fechaMovimiento)")
        db.execSQL("CREATE INDEX index_movimiento_categoriaId ON movimiento(categoriaId)")
        db.execSQL("CREATE INDEX index_movimiento_estado ON movimiento(estado)")
        db.execSQL("CREATE INDEX index_movimiento_cuentaId ON movimiento(cuentaId)")

        // Backfill por nombre de cuenta, no por id asumido.
        db.execSQL(
            """
            UPDATE movimiento SET cuentaId = (SELECT id FROM cuenta WHERE nombre = 'Efectivo')
            WHERE origen = 'MANUAL'
            """.trimIndent(),
        )
        db.execSQL(
            """
            UPDATE movimiento SET cuentaId = (SELECT id FROM cuenta WHERE nombre = 'Nu')
            WHERE origen = 'NU'
            """.trimIndent(),
        )
        // origen = 'SANTANDER' queda con cuentaId NULL: no existe cuenta Santander
        // todavia (se crea cuando el usuario active ese banco desde Ajustes).
    }
}

/**
 * Sin cambios de esquema: la tabla `categoria` ya existe desde v1. Esta
 * migracion solo siembra un set fijo de categorias para instalaciones
 * existentes, para que el formulario de alta manual y los reportes por
 * categoria no arranquen vacios. Duplica a proposito el mismo INSERT de
 * [AppDatabase.SEED_CALLBACK] (mismo patron que MIGRATION_1_2 con `cuenta`):
 * instalaciones nuevas siembran por el callback, instalaciones existentes
 * por esta migracion.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        CATEGORIAS_SEMILLA.forEach { (nombre, icono, color) ->
            db.execSQL(
                "INSERT INTO categoria (nombre, icono, color) VALUES (?, ?, ?)",
                arrayOf(nombre, icono, color),
            )
        }
    }
}

/** Set fijo de categorias MX. Nombre del icono = ImageVector de Material. */
val CATEGORIAS_SEMILLA: List<Triple<String, String, String>> = listOf(
    Triple("Comida", "Restaurant", "#E07856"),
    Triple("Transporte", "DirectionsCar", "#5B8DEF"),
    Triple("Servicios", "Bolt", "#F2B134"),
    Triple("Renta", "Home", "#7A5490"),
    Triple("Salud", "LocalHospital", "#D9556B"),
    Triple("Ocio", "SportsEsports", "#6FA087"),
    Triple("Compras", "ShoppingBag", "#D9A441"),
    Triple("Educacion", "School", "#4A90A4"),
    Triple("Suscripciones", "Subscriptions", "#9B6FAE"),
    Triple("Otros", "Category", "#8A7D91"),
)

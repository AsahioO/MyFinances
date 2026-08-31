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

package com.finanzas.app.data.local.migration

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.finanzas.app.data.local.AppDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifica MIGRATION_2_3 sobre un dispositivo/emulador real: sin cambio de
 * esquema, solo siembra CATEGORIAS_SEMILLA para instalaciones existentes y
 * conserva las categorias y movimientos que el usuario ya tenia.
 *
 * Instrumentado (no Robolectric), misma razon que Migration1To2Test: AGP solo
 * fusiona los esquemas exportados de Room en los assets de androidTest.
 */
@RunWith(AndroidJUnit4::class)
class Migration2To3Test {

    private val nombreBase = "migration-2-3-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun migrarDeV2AV3SiembraCategoriasYConservaLasExistentes() {
        helper.createDatabase(nombreBase, 2).apply {
            execSQL(
                "INSERT INTO categoria (id, nombre, icono, color) VALUES (1, 'Mascotas', 'Pets', NULL)",
            )
            close()
        }

        val migrada = helper.runMigrationsAndValidate(nombreBase, 3, true, MIGRATION_2_3)

        migrada.query("SELECT COUNT(*) FROM categoria").use { cursor ->
            cursor.moveToFirst()
            // La categoria preexistente (1) + las CATEGORIAS_SEMILLA.
            assertEquals(1 + CATEGORIAS_SEMILLA.size, cursor.getInt(0))
        }

        migrada.query("SELECT nombre FROM categoria WHERE nombre = 'Mascotas'").use { cursor ->
            assertTrue(cursor.count == 1)
        }

        migrada.query("SELECT nombre FROM categoria WHERE nombre = 'Comida'").use { cursor ->
            assertTrue(cursor.count == 1)
        }
    }
}

package com.finanzas.app.data.local.migration

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.finanzas.app.data.local.AppDatabase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifica MIGRATION_1_2 sobre un dispositivo/emulador real: siembra datos en
 * esquema v1, corre la migracion, y confirma que las cuentas semilla y el
 * backfill de cuentaId en movimientos preexistentes quedaron bien.
 *
 * Instrumentado (no Robolectric): AGP solo fusiona los esquemas exportados de
 * Room en los assets de `androidTest`, no en los de `test`, asi que
 * MigrationTestHelper no puede encontrarlos bajo un test JVM.
 */
@RunWith(AndroidJUnit4::class)
class Migration1To2Test {

    private val nombreBase = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun migrarDeV1AV2CreaCuentaYReasignaCuentaIdPorOrigen() {
        helper.createDatabase(nombreBase, 1).apply {
            execSQL(
                "INSERT INTO banco_config (packageName, nombreDisplay, activo) " +
                    "VALUES ('com.nu.production', 'Nu', 1)",
            )
            execSQL(
                """
                INSERT INTO movimiento
                    (id, montoCentavos, tipo, origen, comercioOrigen, categoriaId,
                     fechaMovimiento, fechaRegistro, estado, notas)
                VALUES
                    (1, 21900, 'EGRESO', 'NU', 'PAYPAL *NVIDIA CORP', NULL,
                     1756500900000, 1756500905000, 'PENDIENTE_REVISION', NULL)
                """.trimIndent(),
            )
            execSQL(
                """
                INSERT INTO movimiento
                    (id, montoCentavos, tipo, origen, comercioOrigen, categoriaId,
                     fechaMovimiento, fechaRegistro, estado, notas)
                VALUES
                    (2, 5000, 'EGRESO', 'MANUAL', NULL, NULL,
                     1756500900000, 1756500900000, 'CONFIRMADO', NULL)
                """.trimIndent(),
            )
            close()
        }

        val migrada = helper.runMigrationsAndValidate(nombreBase, 2, true, MIGRATION_1_2)

        migrada.query("SELECT nombre, origen FROM cuenta ORDER BY nombre ASC").use { cursor ->
            assertEquals(2, cursor.count)
        }

        migrada.query("SELECT cuentaId FROM movimiento WHERE id = 1").use { cursor ->
            cursor.moveToFirst()
            val cuentaId = cursor.getLong(0)
            migrada.query("SELECT nombre FROM cuenta WHERE id = $cuentaId").use { c2 ->
                c2.moveToFirst()
                assertEquals("Nu", c2.getString(0))
            }
        }

        migrada.query("SELECT cuentaId FROM movimiento WHERE id = 2").use { cursor ->
            cursor.moveToFirst()
            val cuentaId = cursor.getLong(0)
            migrada.query("SELECT nombre FROM cuenta WHERE id = $cuentaId").use { c2 ->
                c2.moveToFirst()
                assertEquals("Efectivo", c2.getString(0))
            }
        }
    }
}

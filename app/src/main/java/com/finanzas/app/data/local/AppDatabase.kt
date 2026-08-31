package com.finanzas.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.finanzas.app.data.local.dao.BancoConfigDao
import com.finanzas.app.data.local.dao.CategoriaDao
import com.finanzas.app.data.local.dao.CuentaDao
import com.finanzas.app.data.local.dao.MovimientoDao
import com.finanzas.app.data.local.dao.NotificacionProcesadaDao
import com.finanzas.app.data.local.entity.BancoConfigEntity
import com.finanzas.app.data.local.entity.CategoriaEntity
import com.finanzas.app.data.local.entity.CuentaEntity
import com.finanzas.app.data.local.entity.MovimientoEntity
import com.finanzas.app.data.local.entity.NotificacionProcesadaEntity

@Database(
    entities = [
        MovimientoEntity::class,
        CategoriaEntity::class,
        NotificacionProcesadaEntity::class,
        BancoConfigEntity::class,
        CuentaEntity::class,
    ],
    version = AppDatabase.VERSION,
    exportSchema = true,
)
@TypeConverters(Convertidores::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun movimientoDao(): MovimientoDao

    abstract fun categoriaDao(): CategoriaDao

    abstract fun notificacionProcesadaDao(): NotificacionProcesadaDao

    abstract fun bancoConfigDao(): BancoConfigDao

    abstract fun cuentaDao(): CuentaDao

    companion object {
        const val VERSION = 2
        const val NOMBRE_ARCHIVO = "finanzas.db"

        /**
         * Semilla de la base: Nu queda activo desde el primer arranque, asi que
         * el onboarding no necesita un paso de "elige tu banco" para un solo
         * elemento. Santander se agrega despues desde Ajustes. Las cuentas
         * "Efectivo"/"Nu" existen desde el primer arranque para que los
         * movimientos (manuales y automaticos de Nu) tengan una wallet donde
         * caer sin pedirle al usuario que la cree a mano primero.
         *
         * Se hace con SQL crudo a proposito: el callback corre dentro de la
         * apertura de la base, donde todavia no existe una instancia de DAO
         * utilizable sin arriesgar una dependencia circular.
         */
        val SEED_CALLBACK: Callback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL(
                    "INSERT INTO banco_config (packageName, nombreDisplay, activo) " +
                        "VALUES ('com.nu.production', 'Nu', 1)",
                )
                // Se listan todas las columnas NOT NULL: el CREATE TABLE que genera
                // Room aqui no lleva DEFAULT a nivel SQL (a diferencia del que
                // escribe MIGRATION_1_2 a mano), asi que un INSERT parcial fallaria.
                db.execSQL(
                    "INSERT INTO cuenta (nombre, origen, saldoInicialCentavos, icono, archivada, orden) " +
                        "VALUES ('Efectivo', 'MANUAL', 0, 'AccountBalanceWallet', 0, 0)",
                )
                db.execSQL(
                    "INSERT INTO cuenta (nombre, origen, saldoInicialCentavos, icono, archivada, orden) " +
                        "VALUES ('Nu', 'NU', 0, 'AccountBalanceWallet', 0, 1)",
                )
            }
        }
    }
}

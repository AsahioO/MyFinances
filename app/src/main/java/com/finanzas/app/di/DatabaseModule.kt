package com.finanzas.app.di

import android.content.Context
import androidx.room.Room
import com.finanzas.app.data.local.AppDatabase
import com.finanzas.app.data.local.dao.BancoConfigDao
import com.finanzas.app.data.local.dao.CategoriaDao
import com.finanzas.app.data.local.dao.CuentaDao
import com.finanzas.app.data.local.dao.MovimientoDao
import com.finanzas.app.data.local.dao.NotificacionProcesadaDao
import com.finanzas.app.data.local.migration.MIGRATION_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun proveerAppDatabase(@ApplicationContext contexto: Context): AppDatabase =
        Room.databaseBuilder(
            contexto,
            AppDatabase::class.java,
            AppDatabase.NOMBRE_ARCHIVO,
        )
            .addCallback(AppDatabase.SEED_CALLBACK)
            // Sin fallbackToDestructiveMigration: la app acumula historial
            // financiero real, cada cambio de esquema lleva migracion escrita.
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun proveerMovimientoDao(db: AppDatabase): MovimientoDao = db.movimientoDao()

    @Provides
    fun proveerCategoriaDao(db: AppDatabase): CategoriaDao = db.categoriaDao()

    @Provides
    fun proveerNotificacionProcesadaDao(db: AppDatabase): NotificacionProcesadaDao =
        db.notificacionProcesadaDao()

    @Provides
    fun proveerBancoConfigDao(db: AppDatabase): BancoConfigDao = db.bancoConfigDao()

    @Provides
    fun proveerCuentaDao(db: AppDatabase): CuentaDao = db.cuentaDao()
}

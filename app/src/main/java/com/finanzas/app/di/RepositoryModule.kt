package com.finanzas.app.di

import com.finanzas.app.data.local.dao.BancoConfigDao
import com.finanzas.app.data.local.dao.CategoriaDao
import com.finanzas.app.data.local.dao.MovimientoDao
import com.finanzas.app.data.local.dao.NotificacionProcesadaDao
import com.finanzas.app.data.local.AppDatabase
import com.finanzas.app.data.repository.CuentaRepository
import com.finanzas.app.data.repository.MovimientoRepository
import com.finanzas.app.data.repository.ReportesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun proveerMovimientoRepository(
        movimientoDao: MovimientoDao,
        categoriaDao: CategoriaDao,
        notificacionProcesadaDao: NotificacionProcesadaDao,
        bancoConfigDao: BancoConfigDao,
        db: AppDatabase,
    ): MovimientoRepository = MovimientoRepository(
        movimientoDao = movimientoDao,
        categoriaDao = categoriaDao,
        notificacionProcesadaDao = notificacionProcesadaDao,
        bancoConfigDao = bancoConfigDao,
        db = db,
    )

    @Provides
    @Singleton
    fun proveerCuentaRepository(
        cuentaDao: com.finanzas.app.data.local.dao.CuentaDao,
    ): CuentaRepository = CuentaRepository(cuentaDao)

    @Provides
    @Singleton
    fun proveerReportesRepository(
        movimientoDao: MovimientoDao,
    ): ReportesRepository = ReportesRepository(movimientoDao)
}

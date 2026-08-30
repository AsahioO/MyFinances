package com.finanzas.app.di

import com.finanzas.app.data.notificacion.parser.NuNotificacionParser
import com.finanzas.app.domain.notificacion.ParserNotificacionBanco
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Agregar un banco nuevo = sumarlo a esta lista. No se toca el servicio ni los parsers existentes. */
@Module
@InstallIn(SingletonComponent::class)
object NotificacionModule {

    @Provides
    fun proveerParsersBancarios(
        nu: NuNotificacionParser,
    ): List<@JvmSuppressWildcards ParserNotificacionBanco> = listOf(nu)
}

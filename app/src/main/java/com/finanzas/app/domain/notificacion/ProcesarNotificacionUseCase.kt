package com.finanzas.app.domain.notificacion

import com.finanzas.app.data.local.entity.EstadoMovimiento
import com.finanzas.app.data.local.entity.MovimientoEntity
import com.finanzas.app.data.local.entity.NotificacionProcesadaEntity
import com.finanzas.app.data.repository.MovimientoRepository
import javax.inject.Inject

/**
 * Orquesta la logica de negocio de una notificacion entrante: respeta el
 * toggle de bancos activos, delega el parseo al parser correspondiente, y
 * persiste el movimiento junto con su marca de deduplicacion.
 */
class ProcesarNotificacionUseCase @Inject constructor(
    private val parsers: List<@JvmSuppressWildcards ParserNotificacionBanco>,
    private val repositorio: MovimientoRepository,
) {
    suspend operator fun invoke(notificacion: NotificacionCruda) {
        if (notificacion.packageName !in repositorio.paquetesDeBancosActivos()) return

        val parser = parsers.firstOrNull { it.packageName == notificacion.packageName } ?: return
        val resultado = parser.parsear(notificacion.titulo, notificacion.texto, notificacion.cuando)
        if (resultado !is ResultadoParseoNotificacion.Exitoso) return

        val ahora = System.currentTimeMillis()
        val movimiento = MovimientoEntity(
            montoCentavos = resultado.datos.montoCentavos,
            tipo = resultado.datos.tipo,
            origen = parser.origen,
            comercioOrigen = resultado.datos.comercioOrigen,
            fechaMovimiento = resultado.datos.fechaMovimiento,
            fechaRegistro = ahora,
            estado = EstadoMovimiento.PENDIENTE_REVISION,
        )
        val notificacionProcesada = NotificacionProcesadaEntity(
            key = notificacion.key,
            packageName = notificacion.packageName,
            fechaProcesado = ahora,
        )
        repositorio.registrarMovimientoAutomatico(notificacionProcesada, movimiento)
    }
}

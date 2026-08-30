package com.finanzas.app.data.notificacion

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.finanzas.app.domain.notificacion.NotificacionCruda
import com.finanzas.app.domain.notificacion.ParserNotificacionBanco
import com.finanzas.app.domain.notificacion.ProcesarNotificacionUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Filtra las notificaciones del sistema por paquete de banco y delega el
 * procesamiento a [ProcesarNotificacionUseCase]. `onNotificationPosted()`
 * corre en el hilo principal del proceso del listener: aqui solo se capturan
 * los datos crudos, todo el trabajo pesado (parseo + Room) va a una
 * corrutina en Dispatchers.IO, para no arriesgar un ANR ni que el sistema
 * mate el servicio.
 */
@AndroidEntryPoint
class FinanzasNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var procesarNotificacion: ProcesarNotificacionUseCase

    @Inject
    lateinit var parsers: List<@JvmSuppressWildcards ParserNotificacionBanco>

    private var scope: CoroutineScope? = null
    private var paquetesConocidos: Set<String> = emptySet()

    override fun onListenerConnected() {
        super.onListenerConnected()
        scope?.cancel()
        val handler = CoroutineExceptionHandler { _, error ->
            Log.e(TAG, "Error procesando notificacion", error)
        }
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO + handler)
        paquetesConocidos = parsers.map { it.packageName }.toSet()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Filtro barato en memoria, seguro en el hilo principal: descarta el
        // ruido de apps no bancarias (WhatsApp, Gmail, etc.) sin tocar disco.
        if (sbn.packageName !in paquetesConocidos) return

        val extras = sbn.notification.extras
        val evento = NotificacionCruda(
            key = sbn.key,
            packageName = sbn.packageName,
            titulo = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString(),
            texto = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString(),
            cuando = sbn.postTime,
        )
        scope?.launch { procesarNotificacion(evento) }
    }

    override fun onListenerDisconnected() {
        scope?.cancel()
        scope = null
        super.onListenerDisconnected()
    }

    override fun onDestroy() {
        scope?.cancel()
        scope = null
        super.onDestroy()
    }

    private companion object {
        const val TAG = "FinanzasNotifListener"
    }
}

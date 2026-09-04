package com.finanzas.app.data.notificacion

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Acceso al unico permiso del que depende la deteccion automatica: el acceso
 * especial a notificaciones de FinanzasNotificationListenerService.
 *
 * Es de capa data por la misma razon que el listener: toca el sistema
 * operativo directamente. No expone un Flow porque Android no notifica cuando
 * el permiso cambia (plan.md#5) — hay que re-consultarlo, y quien lo hace es
 * InicioViewModel en cada ON_RESUME.
 */
@Singleton
class EstadoPermisoNotificaciones @Inject constructor(
    @param:ApplicationContext private val contexto: Context,
) {
    /** Samsung puede revocar este acceso por su cuenta (plan.md#5): nunca cachear el resultado. */
    fun deteccionActiva(): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(contexto).contains(contexto.packageName)

    /** Pantalla del sistema donde se concede el acceso; no existe un dialogo in-app para esto. */
    fun intentAjustes(): Intent =
        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}

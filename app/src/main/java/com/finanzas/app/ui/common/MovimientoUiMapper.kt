package com.finanzas.app.ui.common

import androidx.compose.ui.graphics.Color
import com.finanzas.app.data.local.entity.CategoriaEntity
import com.finanzas.app.data.local.entity.EstadoMovimiento
import com.finanzas.app.data.local.entity.MovimientoEntity
import com.finanzas.app.data.local.entity.OrigenMovimiento
import com.finanzas.app.data.local.entity.TipoMovimiento
import com.finanzas.app.ui.theme.ColoresSemanticos

/**
 * Forma ya resuelta de un movimiento para mostrar en una fila: el color y el
 * nombre de categoria se calculan aqui (en el mapper/ViewModel), nunca dentro
 * del Composable, para no repetir el mismo lookup en cada recomposicion.
 */
data class MovimientoUi(
    val id: Long,
    val comercioOrigen: String,
    val categoriaNombre: String?,
    val montoCentavos: Long,
    val tipo: TipoMovimiento,
    val colorOrigen: Color,
    val fechaMovimiento: Long,
    val pendienteRevision: Boolean,
)

fun MovimientoEntity.aUi(categoria: CategoriaEntity?, colores: ColoresSemanticos): MovimientoUi =
    MovimientoUi(
        id = id,
        comercioOrigen = comercioOrigen ?: "Movimiento manual",
        categoriaNombre = categoria?.nombre,
        montoCentavos = montoCentavos,
        tipo = tipo,
        colorOrigen = if (origen == OrigenMovimiento.MANUAL) colores.origenManual else colores.origenAutomatico,
        fechaMovimiento = fechaMovimiento,
        pendienteRevision = estado == EstadoMovimiento.PENDIENTE_REVISION,
    )

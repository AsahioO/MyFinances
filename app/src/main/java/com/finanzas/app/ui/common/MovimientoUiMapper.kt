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
    /** Nombre del icono de Material de la categoria (CategoriaEntity.icono); null = "Sin categoria". */
    val categoriaIcono: String? = null,
    /** Hex opcional de la categoria (CategoriaEntity.color); null = usar el color por defecto. */
    val categoriaColorHex: String? = null,
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
        categoriaIcono = categoria?.icono,
        categoriaColorHex = categoria?.color,
    )

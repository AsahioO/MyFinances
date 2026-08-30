package com.finanzas.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.finanzas.app.data.local.entity.BancoConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BancoConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(banco: BancoConfigEntity)

    @Update
    suspend fun actualizar(banco: BancoConfigEntity)

    @Query("UPDATE banco_config SET activo = :activo WHERE packageName = :packageName")
    suspend fun cambiarActivo(packageName: String, activo: Boolean)

    @Query("SELECT * FROM banco_config ORDER BY nombreDisplay ASC")
    fun observarTodos(): Flow<List<BancoConfigEntity>>

    @Query("SELECT * FROM banco_config WHERE activo = 1")
    fun observarActivos(): Flow<List<BancoConfigEntity>>

    /** Lista blanca que consultara el listener de notificaciones. */
    @Query("SELECT packageName FROM banco_config WHERE activo = 1")
    suspend fun paquetesActivos(): List<String>
}

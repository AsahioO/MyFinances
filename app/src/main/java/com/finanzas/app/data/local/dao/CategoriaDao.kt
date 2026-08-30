package com.finanzas.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.finanzas.app.data.local.entity.CategoriaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(categoria: CategoriaEntity): Long

    @Update
    suspend fun actualizar(categoria: CategoriaEntity)

    @Delete
    suspend fun eliminar(categoria: CategoriaEntity)

    @Query("SELECT * FROM categoria WHERE id = :id")
    suspend fun obtenerPorId(id: Long): CategoriaEntity?

    @Query("SELECT * FROM categoria ORDER BY nombre ASC")
    fun observarTodas(): Flow<List<CategoriaEntity>>
}

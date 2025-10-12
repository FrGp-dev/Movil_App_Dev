package com.example.directorioempresas.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {

    // C - Crear/Actualizar. Usa REPLACE para manejar tanto inserción como actualización.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(company: Company)

    // R - Listar/Recuperar
    @Query("SELECT * FROM companies ORDER BY name ASC")
    fun getAllCompanies(): Flow<List<Company>>

    // R - Filtrar por nombre Y/O clasificación
    @Query("SELECT * FROM companies WHERE name LIKE :query OR classification LIKE :query ORDER BY name ASC")
    fun getFilteredCompanies(query: String): Flow<List<Company>>

    // U - Actualizar (mismo que Insert con REPLACE, pero se define por claridad en el DAO)
    @Update
    suspend fun update(company: Company)

    // D - Eliminar
    @Delete
    suspend fun delete(company: Company)
}
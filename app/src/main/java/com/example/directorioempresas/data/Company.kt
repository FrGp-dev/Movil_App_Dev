package com.example.directorioempresas.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "companies")
data class Company(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Clave primaria
    val name: String,
    val website: String,
    val phone: String,
    val email: String,
    val productsAndServices: String,
    val classification: String
)
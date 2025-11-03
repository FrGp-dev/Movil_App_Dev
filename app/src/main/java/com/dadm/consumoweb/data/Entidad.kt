package com.dadm.consumoweb.data
data class Entidad(
    val consecutivo: String,
    val fecha: String,
    val dependencia: String,
    val tipoVinculacion: String,
    val autorizaDatos: String,
    val fullJsonData: String
)
data class OpcionesBusqueda(
    val dependencias: List<String>,
    val vinculaciones: List<String>
)
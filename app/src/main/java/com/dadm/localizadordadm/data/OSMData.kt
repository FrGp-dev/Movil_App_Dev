package com.dadm.localizadordadm.data

import kotlinx.serialization.Serializable

@Serializable
data class OverpassResponse(
    val elements: List<OsmElement>
)

@Serializable
data class OsmElement(
    val type: String,
    val id: Long,
    val lat: Double,
    val lon: Double,
    val tags: Map<String, String>? = null
) {
    val name: String
        get() = tags?.get("name") ?: tags?.get("amenity") ?: "POI Sin Nombre"

    val primaryType: String
        get() = tags?.get("amenity") ?: tags?.get("tourism") ?: "Interés General"
}
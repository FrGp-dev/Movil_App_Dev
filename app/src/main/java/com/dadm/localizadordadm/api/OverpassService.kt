package com.dadm.localizadordadm.api

import com.dadm.localizadordadm.data.OverpassResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class OverpassService {// esta cosa es la que busca los POI desde la api de OPEN sm
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val BASE_URL = "https://overpass-api.de/api/interpreter"

    suspend fun searchNearbyPois(lat: Double, lon: Double, radiusKm: Double): OverpassResponse {
        val radiusMeters = (radiusKm * 1000).toInt()

        val query = """
            [out:json][timeout:25];
            (
              node["amenity"~"hospital|restaurant|bank|pharmacy"](around:$radiusMeters,$lat,$lon);
              node["tourism"~"attraction"](around:$radiusMeters,$lat,$lon);
            );
            out center;
        """.trimIndent()

        val response = client.post(BASE_URL) {
            contentType(ContentType.Application.Json)
            setBody("data=$query")
        }

        return response.body<OverpassResponse>()
    }
}
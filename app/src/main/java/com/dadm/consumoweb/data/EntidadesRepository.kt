package com.dadm.consumoweb.data

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.net.URLEncoder

class EntidadesRepository {

    private val BASE_URL_RESOURCE = "https://www.datos.gov.co/resource/duhu-zh7n.json"

    private val CAMPO_DEPENDENCIA = "seleccione_su_dependencia"
    private val CAMPO_VINCULACION = "tipo_de_vinculaci_n"

    private val CAMPO_CONSECUTIVO = "consecutivo"
    private val CAMPO_FECHA = "fecha"
    private val CAMPO_AUTORIZA = "autoriza_el_tratamiento_de"

    fun buscarEntidades(dependencia: String, vinculacion: String): List<Entidad> {


        val dependenciaConGuiones = dependencia.replace(" ", "_")
        val vinculacionConGuiones = vinculacion.replace(" ", "_")

        val dependenciaFiltro = if (dependenciaConGuiones.isNotBlank()) {
            val valorLike = "'%${dependenciaConGuiones}%'"
            "$CAMPO_DEPENDENCIA%20like%20${URLEncoder.encode(valorLike, "UTF-8")}"
        } else ""

        // Filtro para Vinculación
        val vinculacionFiltro = if (vinculacionConGuiones.isNotBlank()) {
            val valorLike = "'%${vinculacionConGuiones}%'"
            "$CAMPO_VINCULACION%20like%20${URLEncoder.encode(valorLike, "UTF-8")}"
        } else ""

        val whereClause = when {
            dependenciaFiltro.isNotBlank() && vinculacionFiltro.isNotBlank() ->
                "$dependenciaFiltro%20and%20$vinculacionFiltro"
            dependenciaFiltro.isNotBlank() -> dependenciaFiltro
            vinculacionFiltro.isNotBlank() -> vinculacionFiltro
            else -> ""
        }

        if (whereClause.isBlank()) return emptyList()

        val fullUrl = "$BASE_URL_RESOURCE?\$where=$whereClause"

        val url = URL(fullUrl)
        val connection = url.openConnection()

        try {
            val reader = BufferedReader(InputStreamReader(connection.getInputStream()))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()

            return parseJson(response.toString())

        } catch (e: Exception) {
            e.printStackTrace()
            System.out.println("Error consultando entidades (Falló la conexión): $fullUrl")
            return emptyList()
        }
    }


    private fun parseJson(jsonString: String): List<Entidad> {
        val entidades = mutableListOf<Entidad>()

        val jsonArray = try { JSONArray(jsonString) } catch (e: Exception) { return emptyList() }

        for (i in 0 until jsonArray.length()) {
            val jsonObject: JSONObject = jsonArray.getJSONObject(i)

            val rawJson = jsonObject.toString(2)

            val entidad = Entidad(
                consecutivo = jsonObject.optString(CAMPO_CONSECUTIVO, "N/A"),
                fecha = jsonObject.optString(CAMPO_FECHA, "N/A"),
                dependencia = jsonObject.optString(CAMPO_DEPENDENCIA, "N/A"),
                tipoVinculacion = jsonObject.optString(CAMPO_VINCULACION, "N/A"),
                autorizaDatos = jsonObject.optString(CAMPO_AUTORIZA, "N/A"),
                // ASIGNAMOS EL JSON COMPLETO
                fullJsonData = rawJson
            )
            entidades.add(entidad)
        }
        return entidades
    }


    fun getOpcionesBusqueda(): OpcionesBusqueda {
        val dependencias = consultarValoresUnicos(CAMPO_DEPENDENCIA)
        val vinculaciones = consultarValoresUnicos(CAMPO_VINCULACION)
        return OpcionesBusqueda(dependencias, vinculaciones)
    }

    private fun consultarValoresUnicos(campo: String): List<String> {

        val selectClause = URLEncoder.encode(campo, "UTF-8")
        val groupClause = URLEncoder.encode(campo, "UTF-8")

        val fullUrl = "${BASE_URL_RESOURCE}?%24select=$selectClause&%24group=$groupClause&%24limit=500"

        val url = URL(fullUrl)
        val connection = url.openConnection()

        val resultados = mutableListOf<String>()

        try {
            val reader = BufferedReader(InputStreamReader(connection.getInputStream()))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()

            val jsonArray = JSONArray(response.toString())

            for (i in 0 until jsonArray.length()) {
                val jsonObject: JSONObject = jsonArray.getJSONObject(i)
                val valor = jsonObject.optString(campo)
                if (valor.isNotBlank()) {
                    resultados.add(valor)
                }
            }
        } catch (e: Exception) {
            System.out.println("Error (SoQL) consultando valores únicos para $campo: $fullUrl")
        }
        return resultados
    }
}
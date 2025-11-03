package com.dadm.consumoweb.vistas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadm.consumoweb.data.Entidad
import com.dadm.consumoweb.data.EntidadesRepository
import com.dadm.consumoweb.data.OpcionesBusqueda
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EntidadesViewModel(
    private val repository: EntidadesRepository = EntidadesRepository()
) : ViewModel() {

    private val _searchDependencia = MutableStateFlow("")
    val searchDependencia: StateFlow<String> = _searchDependencia.asStateFlow()

    private val _searchVinculacion = MutableStateFlow("")
    val searchVinculacion: StateFlow<String> = _searchVinculacion.asStateFlow()

    private val _buscando = MutableStateFlow(false)
    val buscando: StateFlow<Boolean> = _buscando.asStateFlow()

    private val _entidades = MutableStateFlow<List<Entidad>>(emptyList())
    val entidades: StateFlow<List<Entidad>> = _entidades.asStateFlow()

    private val _opcionesDependencia = MutableStateFlow<List<String>>(emptyList())
    val opcionesDependencia: StateFlow<List<String>> = _opcionesDependencia.asStateFlow()

    private val _opcionesVinculacion = MutableStateFlow<List<String>>(emptyList())
    val opcionesVinculacion: StateFlow<List<String>> = _opcionesVinculacion.asStateFlow()

    private val _selectedJsonData = MutableStateFlow<String?>(null)
    val selectedJsonData: StateFlow<String?> = _selectedJsonData.asStateFlow()


    init {

        cargarOpciones()
    }

    private fun cargarOpciones() {
        viewModelScope.launch(Dispatchers.IO) {
            val opciones = repository.getOpcionesBusqueda()
            _opcionesDependencia.value = opciones.dependencias
            _opcionesVinculacion.value = opciones.vinculaciones
        }
    }

    // --- HANDLERS PARA LOS CAMPOS DE TEXTO ---
    fun onDependenciaChange(newText: String) {
        _searchDependencia.value = newText
    }

    fun onVinculacionChange(newText: String) {
        _searchVinculacion.value = newText
    }

    fun buscarEntidades() {

        val dependencia = _searchDependencia.value
        val vinculacion = _searchVinculacion.value

        if (dependencia.isBlank() && vinculacion.isBlank()) return

        _buscando.value = true
        _entidades.value = emptyList()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val results = repository.buscarEntidades(dependencia, vinculacion)
                _entidades.value = results
            } catch (e: Exception) {
                e.printStackTrace()
                _entidades.value = emptyList()
            } finally {
                _buscando.value = false
            }
        }
    }

    fun onEntidadClicked(entidad: Entidad) {
        // Almacena el JSON completo, lo cual será observado por la UI para navegar.
        _selectedJsonData.value = entidad.fullJsonData
    }

    fun clearSelectedJson() {
        _selectedJsonData.value = null
    }
}
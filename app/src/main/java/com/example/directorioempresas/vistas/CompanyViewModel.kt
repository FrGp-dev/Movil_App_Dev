package com.example.directorioempresas.vistas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.directorioempresas.data.Company
import com.example.directorioempresas.data.CompanyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CompanyViewModel(private val repository: CompanyRepository) : ViewModel() {

    // El texto de búsqueda (filtro)
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    // Flujo de todas las empresas desde la base de datos
    private val _allCompanies: Flow<List<Company>> = repository.getAllCompanies()

    // El estado final de la lista de empresas filtradas que la UI observa
    val filteredCompanies: StateFlow<List<Company>> = _searchText
        // Espera 300ms antes de ejecutar la búsqueda
        .debounce(300L)
        .combine(_allCompanies) { text, companies ->
            if (text.isBlank()) {
                companies
            } else {
                val query = "%$text%" // Formato para la consulta LIKE
                repository.getFilteredCompanies(query).first() // Ejecuta la consulta en el DAO
            }
        }.stateIn(
            scope = viewModelScope,
            // Empieza a compartir valores inmediatamente, detente si no hay suscriptores
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    fun saveCompany(company: Company) = viewModelScope.launch {
        repository.saveCompany(company)
    }

    fun deleteCompany(company: Company) = viewModelScope.launch {
        repository.deleteCompany(company)
    }

    // Factory para instanciar el ViewModel con el Repository
    class Factory(private val repository: CompanyRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CompanyViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CompanyViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
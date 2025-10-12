package com.example.directorioempresas.vistas.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.example.directorioempresas.data.Company
import com.example.directorioempresas.vistas.CompanyViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyDetailScreen(
    companyId: Int,
    viewModel: CompanyViewModel,
    onBack: () -> Unit
) {
    // 1. Estados mutables para los campos del formulario
    var name by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var productsAndServices by remember { mutableStateOf("") }
    var classification by remember { mutableStateOf("") }
    var isNewEntry by remember { mutableStateOf(companyId == 0) }

    // 2. Cargar datos si es una edición (companyId != 0)
    LaunchedEffect(companyId) {
        if (!isNewEntry) {
            viewModel.viewModelScope.launch {
                // Busca la empresa, asumiendo que tienes una forma de obtenerla por ID
                // (Para simplificar, usaremos el listado filtrado de empresas y buscaremos el ID)
                val existingCompany = viewModel.filteredCompanies.firstOrNull()?.find { it.id == companyId }
                existingCompany?.let {
                    name = it.name
                    website = it.website
                    phone = it.phone
                    email = it.email
                    productsAndServices = it.productsAndServices
                    classification = it.classification
                } ?: run {
                    // Si no se encuentra, podría ser un error, volvemos
                    onBack()
                }
            }
        }
    }

    // 3. Lógica de Guardar
    fun onSave() {
        val companyToSave = Company(
            id = if (isNewEntry) 0 else companyId,
            name = name,
            website = website,
            phone = phone,
            email = email,
            productsAndServices = productsAndServices,
            classification = classification
        )
        viewModel.saveCompany(companyToSave)
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNewEntry) "Nueva Empresa" else "Editar Empresa") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    Button(onClick = { onSave() }, enabled = name.isNotBlank()) {
                        Text("Guardar")
                    }
                    Spacer(Modifier.width(8.dp))
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()), // Permite desplazamiento para formularios largos
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Campos de Formulario
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre de la empresa*") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = website, onValueChange = { website = it }, label = { Text("URL de la página web") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono de contacto") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email de contacto") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = productsAndServices, onValueChange = { productsAndServices = it }, label = { Text("Productos y servicios") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            OutlinedTextField(value = classification, onValueChange = { classification = it }, label = { Text("Clasificación (ej: Consultoría, Fábrica de software)") }, modifier = Modifier.fillMaxWidth())
        }
    }
}
package com.example.directorioempresas.vistas.pantallas

import android.app.Activity // ⚡️ NUEVO: Necesario para acceder a finish()
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.directorioempresas.data.Company
import com.example.directorioempresas.vistas.CompanyViewModel
import androidx.activity.compose.LocalActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyListScreen(
    viewModel: CompanyViewModel = viewModel(),
    onNavigateToEdit: (Int) -> Unit // Función para navegar a la pantalla de edición
) {
    val companies by viewModel.filteredCompanies.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<Company?>(null) } // Estado para el diálogo de confirmación

    // Obtener la Activity para poder cerrarla
    val activity = LocalActivity.current

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToEdit(0) }) { // 0 indica nuevo registro
                Icon(Icons.Filled.Add, contentDescription = "Agregar Empresa")
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Directorio de Empresas") },
                actions = {
                    // ⚡️ BOTÓN DE SALIR INTEGRADO EN EL TOP APP BAR ⚡️
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(
                            Icons.Filled.ExitToApp,
                            contentDescription = "Salir de la Aplicación"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Campo de Búsqueda/Filtro
            OutlinedTextField(
                value = searchText,
                onValueChange = viewModel::onSearchTextChange,
                label = { Text("Filtrar por nombre o clasificación") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            LazyColumn {
                items(companies, key = { it.id }) { company ->
                    CompanyItem(
                        company = company,
                        onEdit = { onNavigateToEdit(company.id) },
                        onDelete = { showDeleteDialog = company }
                    )
                    Divider()
                }
            }
        }
    }

    // Diálogo de Confirmación (Se muestra si showDeleteDialog no es null)
    showDeleteDialog?.let { companyToDelete ->
        ConfirmationDialog(
            company = companyToDelete,
            onConfirm = {
                viewModel.deleteCompany(companyToDelete)
                showDeleteDialog = null
            },
            onDismiss = { showDeleteDialog = null }
        )
    }
}

@Composable
fun CompanyItem(company: Company, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(company.name, style = MaterialTheme.typography.titleMedium)
            Text(company.classification, style = MaterialTheme.typography.bodySmall)
            Text("Tel: ${company.phone} | Email: ${company.email}", style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.width(8.dp))

        // Botón de Editar
        IconButton(onClick = onEdit) {
            Icon(Icons.Filled.Edit, contentDescription = "Editar")
        }
        // Botón de Eliminar
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun ConfirmationDialog(company: Company, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar Eliminación") },
        text = { Text("¿Estás seguro de que quieres eliminar la empresa '${company.name}'?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
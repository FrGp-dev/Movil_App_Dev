package com.dadm.consumoweb.vistas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.* import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dadm.consumoweb.data.Entidad
import com.dadm.consumoweb.vistas.EntidadesViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EntidadesApp(
    viewModel: EntidadesViewModel = viewModel()

) {

    val searchDependencia by viewModel.searchDependencia.collectAsState()
    val searchVinculacion by viewModel.searchVinculacion.collectAsState()

    val entidades by viewModel.entidades.collectAsState()
    val buscando by viewModel.buscando.collectAsState()

    val opcionesDependencia by viewModel.opcionesDependencia.collectAsState()
    val opcionesVinculacion by viewModel.opcionesVinculacion.collectAsState()

    val selectedJson by viewModel.selectedJsonData.collectAsState()

    val camposVacios = searchDependencia.isBlank() && searchVinculacion.isBlank()

    Scaffold(
        topBar = { TopAppBar(title = { Text("🔎 Consulta de Encuestas (SODA V2)") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // ... (Campos de búsqueda y botón) ...
            AutoCompleteField(
                label = "Filtro por Dependencia",
                placeholder = "Seleccione o escriba la Dependencia",
                currentText = searchDependencia,
                onTextChanged = viewModel::onDependenciaChange,
                options = opcionesDependencia,
                enabled = !buscando
            )

            Spacer(modifier = Modifier.height(8.dp))

            AutoCompleteField(
                label = "Filtro por Tipo de Vinculación",
                placeholder = "Seleccione o escriba el Tipo de Vinculación",
                currentText = searchVinculacion,
                onTextChanged = viewModel::onVinculacionChange,
                options = opcionesVinculacion,
                enabled = !buscando
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = viewModel::buscarEntidades,
                modifier = Modifier.fillMaxWidth(),
                enabled = !camposVacios && !buscando
            ) {
                Icon(Icons.Filled.Search, contentDescription = "Buscar")
                Spacer(Modifier.width(8.dp))
                Text(if (buscando) "Consultando..." else "Consultar Encuestas")
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                buscando -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                entidades.isNotEmpty() -> {
                    Text(
                        text = "Resultados encontrados: ${entidades.size}",
                        style = MaterialTheme.typography.subtitle1
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(entidades) { entidad ->

                            EntidadItem(
                                entidad = entidad,
                                onClick = { viewModel.onEntidadClicked(entidad) }
                            )
                            Divider()
                        }
                    }
                }
                !camposVacios -> {
                    Text("No se encontraron resultados o hubo un error en la consulta.", color = MaterialTheme.colors.error)
                }
                else -> {
                    Text("Ingrese los filtros para comenzar la búsqueda.")
                }
            }
        }
    }

    selectedJson?.let { jsonContent ->
        AlertDialog(
            onDismissRequest = viewModel::clearSelectedJson, // Cerrar al tocar fuera
            title = { Text("JSON Completo del Registro") },
            text = {

                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = jsonContent,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::clearSelectedJson) {
                    Text("Cerrar")
                }
            },
            modifier = Modifier.padding(16.dp).fillMaxHeight(0.8f)
        )
    }

}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AutoCompleteField(
    label: String,
    placeholder: String,
    currentText: String,
    onTextChanged: (String) -> Unit,
    options: List<String>,
    enabled: Boolean
) {
    val expanded = remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded.value,
        onExpandedChange = { expanded.value = !expanded.value },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = currentText,
            onValueChange = onTextChanged,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true,
            readOnly = false
        )

        val filteredOptions = options.filter { it.contains(currentText, ignoreCase = true) }

        ExposedDropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            filteredOptions.forEach { selectionOption ->
                DropdownMenuItem(
                    onClick = {
                        onTextChanged(selectionOption)
                        expanded.value = false
                    }
                ) {
                    Text(text = selectionOption)
                }
            }
        }
    }
}


@Composable
fun EntidadItem(entidad: Entidad, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "ID: ${entidad.consecutivo} (${entidad.fecha})",
            style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Dependencia: ${entidad.dependencia}",
            style = MaterialTheme.typography.caption
        )
        Text(
            text = "Vinculación: ${entidad.tipoVinculacion}",
            style = MaterialTheme.typography.caption
        )
        Text(
            text = "Autoriza Datos: ${entidad.autorizaDatos}",
            style = MaterialTheme.typography.caption
        )
    }
}
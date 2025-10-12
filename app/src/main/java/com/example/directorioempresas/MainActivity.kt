package com.example.directorioempresas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.directorioempresas.data.AppDatabase
import com.example.directorioempresas.data.CompanyRepository
import com.example.directorioempresas.vistas.CompanyViewModel
import com.example.directorioempresas.vistas.pantallas.CompanyDetailScreen
import com.example.directorioempresas.vistas.pantallas.CompanyListScreen
import com.example.directorioempresas.ui.theme.DirectorioEmpresasTheme
import android.app.Activity
import androidx.compose.material3.Button
import androidx.compose.material3.Text


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DirectorioEmpresasTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    CompanyDirectoryApp()
                }
            }
        }
    }
}

// Rutas de navegación
sealed class Screen(val route: String) {
    object List : Screen("company_list")
    object Detail : Screen("company_detail/{companyId}") {
        fun createRoute(companyId: Int) = "company_detail/$companyId"
    }
}
@Composable
fun ExitAppButton() {
    val context = LocalContext.current
    val activity = (context as? Activity)

    Button(
        onClick = {
            activity?.finish()
        }
    ) {
        Text("Salir de la Aplicación")
    }
}
@Composable
fun CompanyDirectoryApp() {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val repository = CompanyRepository(database.companyDao())

    // Inyección manual del ViewModel Factory
    val viewModel: CompanyViewModel = viewModel(
        factory = CompanyViewModel.Factory(repository)
    )

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.List.route) {

        // Pantalla de Lista y Filtrado
        composable(Screen.List.route) {
            CompanyListScreen(
                viewModel = viewModel,
                onNavigateToEdit = { companyId ->
                    navController.navigate(Screen.Detail.createRoute(companyId))
                }
            )
        }

        // Pantalla de Detalle (Crear/Editar)
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("companyId") { type = NavType.IntType })
        ) { backStackEntry ->
            val companyId = backStackEntry.arguments?.getInt("companyId") ?: 0
            CompanyDetailScreen(
                companyId = companyId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
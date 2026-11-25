
package com.example.recordatoriosapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recordatoriosapp.auth.AuthRepository
import com.example.recordatoriosapp.auth.AuthViewModel
import com.example.recordatoriosapp.auth.AuthViewModelFactory
import com.example.recordatoriosapp.data.RecordatoriosRepositoryRoom
import com.example.recordatoriosapp.data.connectivity.ConnectivityObserver
import com.example.recordatoriosapp.data.connectivity.NetworkConnectivityObserver
import com.example.recordatoriosapp.data.local.RecordatoriosDatabase
import com.example.recordatoriosapp.data.preferences.PreferenciasUsuarioRepository
import com.example.recordatoriosapp.data.remote.FirestoreRecordatoriosRepository
import com.example.recordatoriosapp.ui.screens.CrearRecordatorioScreen
import com.example.recordatoriosapp.ui.screens.LoginScreen
import com.example.recordatoriosapp.ui.screens.RegistroScreen
import com.example.recordatoriosapp.ui.theme.RecordatoriosTheme
import com.example.recordatoriosapp.viewmodel.PreferenciasUsuarioViewModel
import com.example.recordatoriosapp.viewmodel.PreferenciasUsuarioViewModelFactory
import com.example.recordatoriosapp.viewmodel.RecordatoriosUiState
import com.example.recordatoriosapp.viewmodel.RecordatoriosViewModel
import com.example.recordatoriosapp.viewmodel.RecordatoriosViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.example.recordatoriosapp.data.SincronizacionScheduler

sealed class Categoria(val etiqueta: String) {
    object Personal : Categoria("Personal")
    object Estudio : Categoria("Estudio")
    object Trabajo : Categoria("Trabajo")
}

sealed class Prioridad(val etiqueta: String) {
    object Alta : Prioridad("Alta")
    object Media : Prioridad("Media")
    object Baja : Prioridad("Baja")
}

sealed class Estado(val etiqueta: String) {
    object Pendiente : Estado("Pendiente")
    object EnProgreso : Estado("En progreso")
    object Completado : Estado("Completado")
}

data class Recordatorio(
    val id: Int,
    val titulo: String,
    val descripcion: String? = null,
    val categoria: Categoria,
    val prioridad: Prioridad,
    val estado: Estado,
    val fecha: String? = null
)

enum class FiltroRecordatorios {
    TODOS,
    PENDIENTES,
    PRIORIDAD_ALTA
}

val recordatorios: List<Recordatorio> = listOf(
    Recordatorio(
        id = 1,
        titulo = "Entregar actividad de Programación Mobile",
        descripcion = "Revisar los requisitos de la actividad.",
        categoria = Categoria.Estudio,
        prioridad = Prioridad.Alta,
        estado = Estado.Pendiente,
        fecha = "28/10/2026"
    ),
    Recordatorio(
        id = 2,
        titulo = "Estudiar Kotlin",
        categoria = Categoria.Estudio,
        prioridad = Prioridad.Media,
        estado = Estado.EnProgreso
    ),
    Recordatorio(
        id = 3,
        titulo = "Organizar tareas de la semana",
        categoria = Categoria.Personal,
        prioridad = Prioridad.Baja,
        estado = Estado.Completado
    ),
    Recordatorio(
        id = 4,
        titulo = "Revisar material de trabajo",
        categoria = Categoria.Trabajo,
        prioridad = Prioridad.Alta,
        estado = Estado.Pendiente
    ),
    Recordatorio(
        id = 5,
        titulo = "Preparar próxima clase",
        categoria = Categoria.Trabajo,
        prioridad = Prioridad.Media,
        estado = Estado.Pendiente
    )
)

val categoriasUnicas: Set<Categoria> =
    recordatorios.map { it.categoria }.toSet()

val coloresCategorias: Map<Categoria, Color> = mapOf(
    Categoria.Personal to Color(0xFF1976D2),
    Categoria.Estudio to Color(0xFF7B1FA2),
    Categoria.Trabajo to Color(0xFF388E3C)
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = RecordatoriosDatabase.obtenerDatabase(this)
        val connectivityObserver = NetworkConnectivityObserver(this)

        val authRepository = AuthRepository()
        val authFactory = AuthViewModelFactory(authRepository)

        val preferenciasRepository = PreferenciasUsuarioRepository(this)
        val preferenciasFactory =
            PreferenciasUsuarioViewModelFactory(preferenciasRepository)

        setContent {

            val preferenciasViewModel: PreferenciasUsuarioViewModel = viewModel(
                factory = preferenciasFactory
            )

            val preferencias by preferenciasViewModel.preferencias.collectAsState()

            val authViewModel: AuthViewModel = viewModel(
                factory = authFactory
            )

            val authState by authViewModel.uiState.collectAsState()

            RecordatoriosTheme(
                darkTheme = preferencias.temaOscuro
            ) {

                val navController = rememberNavController()

                val destinoInicial = if (authRepository.usuarioActual != null) {
                    "lista"
                } else {
                    "login"
                }

                NavHost(
                    navController = navController,
                    startDestination = destinoInicial
                ) {

                    composable("login") {
                        LoginScreen(
                            viewModel = authViewModel,
                            onLoginExitoso = {
                                navController.navigate("lista") {
                                    popUpTo("login") {
                                        inclusive = true
                                    }
                                }
                            },
                            onIrARegistro = {
                                authViewModel.limpiarError()
                                navController.navigate("registro")
                            }
                        )
                    }

                    composable("registro") {
                        RegistroScreen(
                            viewModel = authViewModel,
                            onRegistroExitoso = {
                                navController.navigate("lista") {
                                    popUpTo("login") {
                                        inclusive = true
                                    }
                                }
                            },
                            onIrALogin = {
                                authViewModel.limpiarError()
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("lista") {

                        val usuario = FirebaseAuth.getInstance().currentUser

                        if (usuario == null || !authState.autenticado) {

                            LaunchedEffect(Unit) {
                                navController.navigate("login") {
                                    popUpTo(navController.graph.id) {
                                        inclusive = true
                                    }
                                }
                            }

                        } else {

                            val repositorio = remember(usuario.uid) {
                                RecordatoriosRepositoryRoom(
                                    dao = database.recordatorioDao(),
                                    userId = usuario.uid
                                )
                            }

                            val factory = remember(repositorio) {
                                RecordatoriosViewModelFactory(repositorio)
                            }

                            val recordatoriosViewModel: RecordatoriosViewModel = viewModel(
                                key = "recordatorios_${usuario.uid}",
                                factory = factory
                            )

                            PantallaRecordatorios(
                                viewModel = recordatoriosViewModel,
                                preferenciasViewModel = preferenciasViewModel,
                                connectivityObserver = connectivityObserver,
                                onCrear = {
                                    recordatoriosViewModel.limpiarFormulario()
                                    navController.navigate("crear")
                                },
                                onVerDetalle = { id ->
                                    navController.navigate("detalle/$id")
                                },
                                onCerrarSesion = {
                                    authViewModel.cerrarSesion()

                                    navController.navigate("login") {
                                        popUpTo(navController.graph.id) {
                                            inclusive = true
                                        }
                                    }
                                }
                            )
                        }
                    }

                    composable("crear") {

                        val usuario = FirebaseAuth.getInstance().currentUser

                        if (usuario == null || !authState.autenticado) {

                            LaunchedEffect(Unit) {
                                navController.navigate("login") {
                                    popUpTo(navController.graph.id) {
                                        inclusive = true
                                    }
                                }
                            }

                        } else {

                            val repositorio = remember(usuario.uid) {
                                RecordatoriosRepositoryRoom(
                                    database.recordatorioDao(),
                                    usuario.uid
                                )
                            }

                            val factory = remember(repositorio) {
                                RecordatoriosViewModelFactory(repositorio)
                            }

                            val recordatoriosViewModel: RecordatoriosViewModel = viewModel(
                                key = "recordatorios_${usuario.uid}",
                                factory = factory
                            )

                            CrearRecordatorioScreen(
                                viewModel = recordatoriosViewModel,
                                onGuardado = {
                                    navController.popBackStack()
                                },
                                onCancelar = {
                                    recordatoriosViewModel.limpiarFormulario()
                                    navController.popBackStack()
                                }
                            )
                        }
                    }

                    composable(
                        route = "detalle/{recordatorioId}",
                        arguments = listOf(
                            navArgument("recordatorioId") {
                                type = NavType.IntType
                            }
                        )
                    ) { backStackEntry ->

                        val usuario = FirebaseAuth.getInstance().currentUser

                        val id = backStackEntry.arguments
                            ?.getInt("recordatorioId")

                        if (usuario == null || !authState.autenticado) {

                            LaunchedEffect(Unit) {
                                navController.navigate("login") {
                                    popUpTo(navController.graph.id) {
                                        inclusive = true
                                    }
                                }
                            }

                        } else {

                            val repositorio = remember(usuario.uid) {
                                RecordatoriosRepositoryRoom(
                                    database.recordatorioDao(),
                                    usuario.uid
                                )
                            }

                            val factory = remember(repositorio) {
                                RecordatoriosViewModelFactory(repositorio)
                            }

                            val recordatoriosViewModel: RecordatoriosViewModel = viewModel(
                                key = "recordatorios_${usuario.uid}",
                                factory = factory
                            )

                            PantallaDetalleRecordatorio(
                                id = id,
                                viewModel = recordatoriosViewModel,
                                onVolver = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRecordatorios(
    viewModel: RecordatoriosViewModel,
    preferenciasViewModel: PreferenciasUsuarioViewModel,
    connectivityObserver: ConnectivityObserver,
    onCrear: () -> Unit,
    onVerDetalle: (Int) -> Unit,
    onCerrarSesion: () -> Unit
) {

    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(estado) {
        if (
            estado is RecordatoriosUiState.Datos &&
            FirebaseAuth.getInstance().currentUser != null
        ) {
            SincronizacionScheduler.programar(context)
        }
    }

    val estadoConexion by connectivityObserver
        .observarConexion()
        .collectAsStateWithLifecycle(
            initialValue = ConnectivityObserver.EstadoConexion.NoDisponible
        )

    val preferencias by preferenciasViewModel.preferencias.collectAsStateWithLifecycle()

    val usuarioId = FirebaseAuth.getInstance().currentUser?.uid

    val firestoreRepository = remember(usuarioId) {
        if (usuarioId != null) {
            FirestoreRecordatoriosRepository(
                dao = RecordatoriosDatabase
                    .obtenerDatabase(context)
                    .recordatorioDao()
            )
        } else {
            null
        }
    }

    val scope = rememberCoroutineScope()

    var filtro by remember {
        mutableStateOf(FiltroRecordatorios.TODOS)
    }

    var sincronizando by remember {
        mutableStateOf(false)
    }

    var mensajeSincronizacion by remember {
        mutableStateOf<String?>(null)
    }

    var errorSincronizacion by remember {
        mutableStateOf(false)
    }

    suspend fun ejecutarSincronizacion() {

        if (sincronizando || usuarioId == null || firestoreRepository == null) {
            return
        }

        sincronizando = true
        mensajeSincronizacion = null
        errorSincronizacion = false

        try {
            firestoreRepository.sincronizarTodo(usuarioId)

            mensajeSincronizacion = "Sincronización completada correctamente."

        } catch (e: Exception) {

            mensajeSincronizacion =
                e.localizedMessage ?: "No se pudo completar la sincronización."

            errorSincronizacion = true

        } finally {
            sincronizando = false
        }
    }

    // Sincronización automática al entrar en la pantalla
    // y cuando vuelve a estar disponible la conexión.
    LaunchedEffect(usuarioId, estadoConexion) {

        if (
            usuarioId != null &&
            estadoConexion == ConnectivityObserver.EstadoConexion.Disponible
        ) {
            ejecutarSincronizacion()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("Mis recordatorios")
                },
                actions = {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Modo oscuro")

                        Switch(
                            checked = preferencias.temaOscuro,
                            onCheckedChange = {
                                preferenciasViewModel.guardarTemaOscuro(it)
                            }
                        )
                    }

                    TextButton(
                        onClick = onCerrarSesion
                    ) {
                        Text("Salir")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCrear
            ) {
                Text("+")
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {

            Text(
                text = "Organizá tus tareas y actividades",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = when (estadoConexion) {
                    ConnectivityObserver.EstadoConexion.Disponible ->
                        "Con conexión a Internet"

                    ConnectivityObserver.EstadoConexion.NoDisponible,
                    ConnectivityObserver.EstadoConexion.Perdida ->
                        "Sin conexión a Internet"

                    ConnectivityObserver.EstadoConexion.Perdiendo ->
                        "Perdiendo conexión..."
                },
                color = if (
                    estadoConexion == ConnectivityObserver.EstadoConexion.Disponible
                ) {
                    Color(0xFF2E7D32)
                } else {
                    MaterialTheme.colorScheme.error
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // CONTROLES DE SINCRONIZACIÓN

            Button(
                onClick = {
                    scope.launch {
                        ejecutarSincronizacion()
                    }
                },
                enabled = !sincronizando &&
                        usuarioId != null &&
                        estadoConexion == ConnectivityObserver.EstadoConexion.Disponible,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (sincronizando) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Sincronizando...")
                } else {
                    Text("Sincronizar ahora")
                }
            }

            mensajeSincronizacion?.let { mensaje ->

                Text(
                    text = mensaje,
                    color = if (errorSincronizacion) {
                        MaterialTheme.colorScheme.error
                    } else {
                        Color(0xFF2E7D32)
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Mostrar completados")

                Switch(
                    checked = preferencias.mostrarCompletados,
                    onCheckedChange = {
                        preferenciasViewModel.guardarMostrarCompletados(it)
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                Text(
                    text = "Ordenar por:",
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    FilterChip(
                        selected = preferencias.orden == "FECHA",
                        onClick = {
                            preferenciasViewModel.guardarOrden("FECHA")
                        },
                        label = {
                            Text("Fecha")
                        }
                    )

                    FilterChip(
                        selected = preferencias.orden == "TITULO",
                        onClick = {
                            preferenciasViewModel.guardarOrden("TITULO")
                        },
                        label = {
                            Text("Título")
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = preferencias.orden == "PRIORIDAD",
                        onClick = {
                            preferenciasViewModel.guardarOrden("PRIORIDAD")
                        },
                        label = {
                            Text("Prioridad")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val resultado = estado) {

                is RecordatoriosUiState.Cargando -> {

                    CircularProgressIndicator()

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Cargando recordatorios...")
                }

                is RecordatoriosUiState.Vacio -> {

                    Text(
                        text = "Todavía no tenés recordatorios.",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onCrear
                    ) {
                        Text("Crear mi primer recordatorio")
                    }
                }

                is RecordatoriosUiState.Error -> {

                    Text(
                        text = resultado.mensaje,
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.cargarRecordatorios()
                        }
                    ) {
                        Text("Reintentar")
                    }
                }

                is RecordatoriosUiState.Datos -> {

                    val listaOriginal = resultado.recordatorios

                    val listaFiltrada = if (preferencias.mostrarCompletados) {
                        listaOriginal
                    } else {
                        listaOriginal.filter {
                            it.estado != Estado.Completado
                        }
                    }

                    val lista = when (preferencias.orden) {

                        "TITULO" ->
                            listaFiltrada.sortedBy {
                                it.titulo.lowercase()
                            }

                        "PRIORIDAD" ->
                            listaFiltrada.sortedBy {
                                when (it.prioridad) {
                                    Prioridad.Alta -> 0
                                    Prioridad.Media -> 1
                                    Prioridad.Baja -> 2
                                }
                            }

                        else ->
                            listaFiltrada.sortedByDescending {
                                it.id
                            }
                    }

                    val categoriasActuales = lista
                        .map { it.categoria }
                        .toSet()

                    val recordatoriosFiltrados = when (filtro) {

                        FiltroRecordatorios.TODOS -> lista

                        FiltroRecordatorios.PENDIENTES ->
                            lista.filter {
                                it.estado == Estado.Pendiente
                            }

                        FiltroRecordatorios.PRIORIDAD_ALTA ->
                            lista.filter {
                                it.prioridad == Prioridad.Alta
                            }
                    }

                    Text(
                        text = "Categorías: ${categoriasActuales.size} · " +
                                "Recordatorios: ${lista.size}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Filtrar recordatorios",
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {

                        FilterChip(
                            selected = filtro == FiltroRecordatorios.TODOS,
                            onClick = {
                                filtro = FiltroRecordatorios.TODOS
                            },
                            label = {
                                Text("Todos")
                            }
                        )

                        FilterChip(
                            selected = filtro == FiltroRecordatorios.PENDIENTES,
                            onClick = {
                                filtro = FiltroRecordatorios.PENDIENTES
                            },
                            label = {
                                Text("Pendientes")
                            }
                        )

                        FilterChip(
                            selected = filtro == FiltroRecordatorios.PRIORIDAD_ALTA,
                            onClick = {
                                filtro = FiltroRecordatorios.PRIORIDAD_ALTA
                            },
                            label = {
                                Text("Prioridad alta")
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        items(
                            items = recordatoriosFiltrados,
                            key = { it.id }
                        ) { recordatorio ->

                            TarjetaRecordatorio(
                                recordatorio = recordatorio,
                                onClick = {
                                    onVerDetalle(recordatorio.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaDetalleRecordatorio(
    id: Int?,
    viewModel: RecordatoriosViewModel,
    onVolver: () -> Unit
) {

    LaunchedEffect(id) {
        if (id != null) {
            viewModel.cargarDetalle(id)
        }
    }

    val detalle by viewModel.detalleState.collectAsStateWithLifecycle()

    var mostrarConfirmacion by remember {
        mutableStateOf(false)
    }

    if (mostrarConfirmacion) {

        AlertDialog(
            onDismissRequest = {
                mostrarConfirmacion = false
            },
            title = {
                Text("Eliminar recordatorio")
            },
            text = {
                Text(
                    "¿Estás seguro de que querés eliminar este recordatorio? Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmacion = false

                        if (id != null) {
                            viewModel.eliminarRecordatorio(id)
                            onVolver()
                        }
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmacion = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Detalle del recordatorio",
            style = MaterialTheme.typography.headlineMedium
        )

        when (val estado = detalle) {

            is com.example.recordatoriosapp.viewmodel.DetalleRecordatorioUiState.Cargando -> {

                CircularProgressIndicator()

                Text("Cargando recordatorio...")
            }

            is com.example.recordatoriosapp.viewmodel.DetalleRecordatorioUiState.Datos -> {

                val recordatorio = estado.recordatorio

                Text("Identificador: ${recordatorio.id}")
                Text("Título: ${recordatorio.titulo}")
                Text("Descripción: ${recordatorio.descripcion ?: "Sin descripción"}")
                Text("Categoría: ${recordatorio.categoria.etiqueta}")
                Text("Prioridad: ${recordatorio.prioridad.etiqueta}")
                Text("Estado: ${recordatorio.estado.etiqueta}")

                Text(
                    text = "Cambiar estado:",
                    fontWeight = FontWeight.SemiBold
                )

                Button(
                    onClick = {
                        viewModel.cambiarEstado(Estado.Pendiente)
                    }
                ) {
                    Text("Pendiente")
                }

                Button(
                    onClick = {
                        viewModel.cambiarEstado(Estado.EnProgreso)
                    }
                ) {
                    Text("En progreso")
                }

                Button(
                    onClick = {
                        viewModel.cambiarEstado(Estado.Completado)
                    }
                ) {
                    Text("Completado")
                }

                Button(
                    onClick = {
                        mostrarConfirmacion = true
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar recordatorio")
                }
            }

            is com.example.recordatoriosapp.viewmodel.DetalleRecordatorioUiState.NoEncontrado -> {

                Text("No se encontró el recordatorio.")
            }

            is com.example.recordatoriosapp.viewmodel.DetalleRecordatorioUiState.Error -> {

                Text(
                    text = "Error: ${estado.mensaje}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Button(
            onClick = onVolver
        ) {
            Text("Volver")
        }
    }
}

@Composable
fun TarjetaRecordatorio(
    recordatorio: Recordatorio,
    onClick: () -> Unit
) {

    val colorCategoria =
        coloresCategorias[recordatorio.categoria] ?: Color.Gray

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = onClick
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = recordatorio.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            recordatorio.descripcion?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = it)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Etiqueta(
                    texto = recordatorio.categoria.etiqueta,
                    color = colorCategoria
                )

                Etiqueta(
                    texto = "Prioridad ${recordatorio.prioridad.etiqueta}",
                    color = when (recordatorio.prioridad) {
                        Prioridad.Alta -> Color(0xFFC62828)
                        Prioridad.Media -> Color(0xFFEF6C00)
                        Prioridad.Baja -> Color(0xFF388E3C)
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Estado: ${recordatorio.estado.etiqueta}",
                style = MaterialTheme.typography.bodyMedium
            )

            recordatorio.fecha?.let {
                Text(
                    text = "Fecha: $it",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun Etiqueta(
    texto: String,
    color: Color
) {

    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp)
    ) {

        Text(
            text = texto,
            color = color,
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 5.dp
            ),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

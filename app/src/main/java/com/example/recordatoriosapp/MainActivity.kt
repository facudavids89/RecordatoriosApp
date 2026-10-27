
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.recordatoriosapp.ui.theme.RecordatoriosTheme

// Categorías disponibles
sealed class Categoria(val etiqueta: String) {
    object Personal : Categoria("Personal")
    object Estudio : Categoria("Estudio")
    object Trabajo : Categoria("Trabajo")
}

// Prioridades posibles
sealed class Prioridad(val etiqueta: String) {
    object Alta : Prioridad("Alta")
    object Media : Prioridad("Media")
    object Baja : Prioridad("Baja")
}

// Estados posibles
sealed class Estado(val etiqueta: String) {
    object Pendiente : Estado("Pendiente")
    object EnProgreso : Estado("En progreso")
    object Completado : Estado("Completado")
}

// Modelo inmutable de un recordatorio
data class Recordatorio(
    val id: Int,
    val titulo: String,
    val descripcion: String? = null,
    val categoria: Categoria,
    val prioridad: Prioridad,
    val estado: Estado,
    val fecha: String? = null
)

// Filtros de la pantalla
enum class FiltroRecordatorios {
    TODOS,
    PENDIENTES,
    PRIORIDAD_ALTA
}

// Datos simulados almacenados en una List
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

// Set: obtiene categorías sin repetir
val categoriasUnicas: Set<Categoria> =
    recordatorios.map { it.categoria }.toSet()

// Map: relaciona cada categoría con un color
val coloresCategorias: Map<Categoria, Color> = mapOf(
    Categoria.Personal to Color(0xFF1976D2),
    Categoria.Estudio to Color(0xFF7B1FA2),
    Categoria.Trabajo to Color(0xFF388E3C)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RecordatoriosTheme {
                PantallaRecordatorios()
            }
        }
    }
}

@Composable
fun PantallaRecordatorios() {
    var filtro by remember {
        mutableStateOf(FiltroRecordatorios.TODOS)
    }

    val recordatoriosFiltrados = when (filtro) {
        FiltroRecordatorios.TODOS -> recordatorios

        FiltroRecordatorios.PENDIENTES ->
            recordatorios.filter {
                it.estado == Estado.Pendiente
            }

        FiltroRecordatorios.PRIORIDAD_ALTA ->
            recordatorios.filter {
                it.prioridad == Prioridad.Alta
            }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            Text(
                text = "Mis recordatorios",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Organizá tus tareas y actividades",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Categorías: ${categoriasUnicas.size} · " +
                        "Recordatorios: ${recordatorios.size}",
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
                    label = { Text("Todos") }
                )

                FilterChip(
                    selected = filtro == FiltroRecordatorios.PENDIENTES,
                    onClick = {
                        filtro = FiltroRecordatorios.PENDIENTES
                    },
                    label = { Text("Pendientes") }
                )

                FilterChip(
                    selected = filtro == FiltroRecordatorios.PRIORIDAD_ALTA,
                    onClick = {
                        filtro = FiltroRecordatorios.PRIORIDAD_ALTA
                    },
                    label = { Text("Prioridad alta") }
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
                    TarjetaRecordatorio(recordatorio)
                }
            }
        }
    }
}

@Composable
fun TarjetaRecordatorio(recordatorio: Recordatorio) {
    val colorCategoria =
        coloresCategorias[recordatorio.categoria] ?: Color.Gray

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
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
fun Etiqueta(texto: String, color: Color) {
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

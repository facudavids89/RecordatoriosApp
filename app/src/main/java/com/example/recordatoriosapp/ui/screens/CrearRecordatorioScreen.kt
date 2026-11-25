
package com.example.recordatoriosapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.recordatoriosapp.Categoria
import com.example.recordatoriosapp.Prioridad
import com.example.recordatoriosapp.viewmodel.RecordatoriosViewModel

@Composable
fun CrearRecordatorioScreen(
    viewModel: RecordatoriosViewModel,
    onGuardado: () -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formulario by viewModel.formularioState.collectAsStateWithLifecycle()

    LaunchedEffect(formulario.guardado) {
        if (formulario.guardado) {
            onGuardado()
        }
    }

    val tituloValido = formulario.titulo.trim().length >= 4
    val descripcionValida = formulario.descripcion.length <= 200
    val categoriaValida = formulario.categoria != null
    val prioridadValida = formulario.prioridad != null

    val formularioValido = tituloValido &&
            descripcionValida &&
            categoriaValida &&
            prioridadValida

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Crear recordatorio",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = formulario.titulo,
            onValueChange = viewModel::actualizarTitulo,
            label = { Text("Título") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedTextColor = androidx.compose.ui.graphics.Color.Black,
                unfocusedTextColor = androidx.compose.ui.graphics.Color.Black,
                focusedContainerColor = androidx.compose.ui.graphics.Color.White,
                unfocusedContainerColor = androidx.compose.ui.graphics.Color.White,
                cursorColor = androidx.compose.ui.graphics.Color.Black,
                focusedLabelColor = androidx.compose.ui.graphics.Color.DarkGray,
                unfocusedLabelColor = androidx.compose.ui.graphics.Color.DarkGray
            ),
            isError = formulario.errorTitulo != null
        )

        formulario.errorTitulo?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (!tituloValido && formulario.errorTitulo == null) {
            Text(
                text = "El título debe tener al menos 4 caracteres.",
                color = MaterialTheme.colorScheme.error
            )
        }

        OutlinedTextField(
            value = formulario.descripcion,
            onValueChange = viewModel::actualizarDescripcion,
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedTextColor = androidx.compose.ui.graphics.Color.Black,
                unfocusedTextColor = androidx.compose.ui.graphics.Color.Black,
                focusedContainerColor = androidx.compose.ui.graphics.Color.White,
                unfocusedContainerColor = androidx.compose.ui.graphics.Color.White,
                cursorColor = androidx.compose.ui.graphics.Color.Black,
                focusedLabelColor = androidx.compose.ui.graphics.Color.DarkGray,
                unfocusedLabelColor = androidx.compose.ui.graphics.Color.DarkGray
            ),
            isError = formulario.errorDescripcion != null
        )

        Text(
            text = "${formulario.descripcion.length}/200 caracteres"
        )

        formulario.errorDescripcion?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }

        Text(
            text = "Categoría",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = formulario.categoria == Categoria.Personal,
                onClick = {
                    viewModel.seleccionarCategoria(Categoria.Personal)
                },
                label = { Text("Personal") }
            )

            FilterChip(
                selected = formulario.categoria == Categoria.Estudio,
                onClick = {
                    viewModel.seleccionarCategoria(Categoria.Estudio)
                },
                label = { Text("Estudio") }
            )

            FilterChip(
                selected = formulario.categoria == Categoria.Trabajo,
                onClick = {
                    viewModel.seleccionarCategoria(Categoria.Trabajo)
                },
                label = { Text("Trabajo") }
            )
        }

        formulario.errorCategoria?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }

        Text(
            text = "Prioridad",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = formulario.prioridad == Prioridad.Alta,
                onClick = {
                    viewModel.seleccionarPrioridad(Prioridad.Alta)
                },
                label = { Text("Alta") }
            )

            FilterChip(
                selected = formulario.prioridad == Prioridad.Media,
                onClick = {
                    viewModel.seleccionarPrioridad(Prioridad.Media)
                },
                label = { Text("Media") }
            )

            FilterChip(
                selected = formulario.prioridad == Prioridad.Baja,
                onClick = {
                    viewModel.seleccionarPrioridad(Prioridad.Baja)
                },
                label = { Text("Baja") }
            )
        }

        formulario.errorPrioridad?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }

        formulario.errorGeneral?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Button(
            onClick = viewModel::guardarRecordatorio,
            modifier = Modifier.fillMaxWidth(),
            enabled = formularioValido && !formulario.enviando
        ) {
            if (formulario.enviando) {
                CircularProgressIndicator()
            } else {
                Text("Guardar recordatorio")
            }
        }

        Button(
            onClick = onCancelar,
            modifier = Modifier.fillMaxWidth(),
            enabled = !formulario.enviando
        ) {
            Text("Cancelar")
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

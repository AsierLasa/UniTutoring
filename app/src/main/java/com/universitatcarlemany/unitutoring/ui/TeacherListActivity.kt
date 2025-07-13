package com.universitatcarlemany.unitutoring.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.universitatcarlemany.unitutoring.model.Teacher
import com.universitatcarlemany.unitutoring.repository.TeacherRepository
import com.universitatcarlemany.unitutoring.ui.theme.UniTutoringTheme

class TeacherListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val teachers = TeacherRepository.getTeachers(this)

        setContent {
            UniTutoringTheme {
                TeacherListScreen(
                    teachers = teachers,
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}

/**
 * Pantalla que muestra una lista de profesores disponibles.
 *
 * Utiliza un Scaffold para una estructura consistente con una TopAppBar.
 * La lista se muestra en un LazyColumn para un rendimiento eficiente.
 *
 * @param teachers La lista de profesores a mostrar.
 * @param onNavigateBack Callback para manejar la acción de volver atrás.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherListScreen(
    teachers: List<Teacher>,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profesores Disponibles") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al menú principal"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(teachers) { teacher ->
                TeacherCard(teacher = teacher) {
                    // Se convierte la lista de disponibilidad a un string JSON para pasarla.
                    val availabilityJson = Gson().toJson(teacher.availability)

                    val intent = Intent(context, TeacherDetailActivity::class.java).apply {
                        putExtra("name", teacher.name)
                        putExtra("subject", teacher.subject)
                        putExtra("email", teacher.email)
                        putExtra("availability_json", availabilityJson) // Se pasa el JSON.
                    }
                    context.startActivity(intent)
                }
            }
        }
    }
}

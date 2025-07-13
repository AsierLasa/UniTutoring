package com.universitatcarlemany.unitutoring.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.universitatcarlemany.unitutoring.database.AppDatabase
import com.universitatcarlemany.unitutoring.model.Reservation
import com.universitatcarlemany.unitutoring.repository.TeacherRepository
import com.universitatcarlemany.unitutoring.ui.theme.UniTutoringTheme
import kotlinx.coroutines.launch

/**
 * Activity que muestra la lista de reservas del usuario y permite gestionarlas.
 */
class ReservationListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniTutoringTheme {
                ReservationListScreen(
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}

/**
 * Composable principal que define la estructura de la pantalla "Mis Reservas".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationListScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var reservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var showCancelDialog by remember { mutableStateOf<Reservation?>(null) }

    // --- Estados para la funcionalidad de Gemini ---
    var showGeminiDialog by remember { mutableStateOf(false) }
    var geminiResponse by remember { mutableStateOf("") }
    var isLoadingGemini by remember { mutableStateOf(false) }
    var currentReservationForGemini by remember { mutableStateOf<Reservation?>(null) }


    fun refreshReservations() {
        scope.launch {
            reservations = AppDatabase.getDatabase(context).reservationDao().getAllReservations()
        }
    }

    // --- Función para llamar a la API de Gemini ---
    fun fetchGeminiSuggestions(reservation: Reservation) {
        scope.launch {
            isLoadingGemini = true
            geminiResponse = ""
            currentReservationForGemini = reservation
            showGeminiDialog = true

            val prompt = "Soy un estudiante universitario y tengo una tutoría de '${reservation.subject}'. Sugiere 5 preguntas o temas clave que debería preparar para aprovechar al máximo la sesión con mi profesor, ${reservation.teacherName}. La respuesta debe ser una lista en español, clara y concisa."

            try {
                // Simulación de la llamada a la API de Gemini (Prompts)
                kotlinx.coroutines.delay(2000) // Simular retraso de red
                val resultText = """
                    Aquí tienes 5 temas clave para preparar tu tutoría de ${reservation.subject}:

                    1.  **Repasar los últimos conceptos:** Revisa los temas más recientes vistos en clase. ¿Hay algo que no quedó 100% claro?

                    2.  **Ejercicios específicos:** Elige 1 o 2 ejercicios en los que te hayas atascado. Intenta explicarle al profesor dónde exactamente encuentras la dificultad.

                    3.  **Dudas conceptuales:** Piensa en las ideas teóricas. ¿Hay algún concepto abstracto que te cueste visualizar o aplicar?

                    4.  **Relación con temas anteriores:** Pregunta cómo se conecta el tema actual con lo que ya habéis visto. Esto te ayudará a construir un mapa mental más sólido.

                    5.  **Próximos pasos:** Consulta sobre los siguientes temas de la asignatura o posibles aplicaciones prácticas de lo que estáis aprendiendo.
                """.trimIndent()

                geminiResponse = resultText

            } catch (e: Exception) {
                geminiResponse = "Error al contactar con el asistente de IA. Por favor, inténtalo de nuevo."
            } finally {
                isLoadingGemini = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshReservations()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Reservas") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (reservations.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Aún no tienes reservas.", style = MaterialTheme.typography.headlineSmall)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(reservations) { reservation ->
                    ReservationCard(
                        reservation = reservation,
                        onCancel = { showCancelDialog = reservation },
                        onReschedule = {
                            scope.launch {
                                AppDatabase.getDatabase(context).reservationDao().deleteReservation(reservation)
                                val teacher = TeacherRepository.getTeacherByName(context, reservation.teacherName)
                                if (teacher != null) {
                                    val availabilityJson = Gson().toJson(teacher.availability)
                                    val intent = Intent(context, ReservationActivity::class.java).apply {
                                        putExtra("name", teacher.name)
                                        putExtra("subject", teacher.subject)
                                        putExtra("availability_json", availabilityJson)
                                    }
                                    context.startActivity(intent)
                                    Toast.makeText(context, "Por favor, elige un nuevo horario", Toast.LENGTH_SHORT).show()
                                    refreshReservations()
                                } else {
                                    Toast.makeText(context, "Error: No se encontró al profesor", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onPrepare = { fetchGeminiSuggestions(reservation) }
                    )
                }
            }
        }

        if (showGeminiDialog) {
            GeminiPreparationDialog(
                isLoading = isLoadingGemini,
                response = geminiResponse,
                reservation = currentReservationForGemini,
                onDismiss = { showGeminiDialog = false }
            )
        }

        showCancelDialog?.let { reservationToCancel ->
            AlertDialog(
                onDismissRequest = { showCancelDialog = null },
                icon = { Icon(Icons.Default.Warning, contentDescription = "Alerta") },
                title = { Text("Confirmar cancelación") },
                text = { Text("¿Estás seguro de que quieres cancelar la tutoría de ${reservationToCancel.subject} con ${reservationToCancel.teacherName}?") },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                AppDatabase.getDatabase(context).reservationDao().deleteReservation(reservationToCancel)
                                Toast.makeText(context, "Reserva cancelada", Toast.LENGTH_SHORT).show()
                                refreshReservations()
                                showCancelDialog = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Sí, cancelar")
                    }
                },
                dismissButton = {
                    Button(onClick = { showCancelDialog = null }) {
                        Text("No")
                    }
                }
            )
        }
    }
}

@Composable
fun ReservationCard(
    reservation: Reservation,
    onCancel: () -> Unit,
    onReschedule: () -> Unit,
    onPrepare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = reservation.subject, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = "Con ${reservation.teacherName}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, "Fecha y hora", Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("${reservation.date} a las ${reservation.time}", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            // --- CORRECCIÓN: Se añaden los tres botones ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onPrepare,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "Preparar", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Preparar", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onReschedule,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("Reprogramar", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("Cancelar", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun GeminiPreparationDialog(
    isLoading: Boolean,
    response: String,
    reservation: Reservation?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("✨ Asistente de Preparación") },
        text = {
            Column {
                if (reservation != null) {
                    Text(
                        "Sugerencias para tu tutoría de ${reservation.subject}:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    Text(response, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

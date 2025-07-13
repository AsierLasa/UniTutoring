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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    /**
     * Se ejecuta al crear la Activity.
     * Configura el contenido de la UI usando Jetpack Compose.
     */
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
 *
 * Incluye una barra superior, la lista de reservas y un diálogo de confirmación para
 * cancelar una reserva.
 *
 * @param onNavigateBack Callback para ejecutar la acción de volver a la pantalla anterior.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationListScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var reservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var showCancelDialog by remember { mutableStateOf<Reservation?>(null) }

    /**
     * Función para recargar la lista de reservas desde la base de datos.
     */
    fun refreshReservations() {
        scope.launch {
            reservations = AppDatabase.getDatabase(context).reservationDao().getAllReservations()
        }
    }

    // Carga las reservas iniciales cuando la pantalla se muestra por primera vez.
    LaunchedEffect(Unit) {
        refreshReservations()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Reservas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (reservations.isEmpty()) {
            // Muestra un mensaje centrado si la lista de reservas está vacía.
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Aún no tienes reservas.", style = MaterialTheme.typography.headlineSmall)
            }
        } else {
            // Muestra la lista de reservas usando un LazyColumn para un rendimiento eficiente.
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
                                // Para reprogramar, primero se cancela la reserva actual.
                                AppDatabase.getDatabase(context).reservationDao().deleteReservation(reservation)

                                // Se busca al profesor para obtener su horario completo.
                                val teacher = TeacherRepository.getTeacherByName(context, reservation.teacherName)
                                if (teacher != null) {
                                    // CORRECCIÓN: Se usa la nueva estructura de 'availability'
                                    // y se convierte a JSON para pasarla en el Intent.
                                    val availabilityJson = Gson().toJson(teacher.availability)
                                    val intent = Intent(context, ReservationActivity::class.java).apply {
                                        putExtra("name", teacher.name)
                                        putExtra("subject", teacher.subject)
                                        putExtra("availability_json", availabilityJson)
                                    }
                                    context.startActivity(intent)
                                    Toast.makeText(context, "Por favor, elige un nuevo horario", Toast.LENGTH_SHORT).show()
                                    refreshReservations() // Se actualiza la lista en esta pantalla.
                                } else {
                                    Toast.makeText(context, "Error: No se encontró al profesor", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        }

        // Muestra un diálogo de confirmación si el usuario intenta cancelar una reserva.
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

/**
 * Composable que representa una tarjeta individual para una reserva.
 *
 * Muestra los detalles de la reserva y proporciona botones para "Reprogramar" y "Cancelar".
 *
 * @param reservation El objeto [Reservation] que contiene los datos a mostrar.
 * @param onCancel Callback que se ejecuta cuando se pulsa el botón "Cancelar".
 * @param onReschedule Callback que se ejecuta cuando se pulsa el botón "Reprogramar".
 */
@Composable
fun ReservationCard(
    reservation: Reservation,
    onCancel: () -> Unit,
    onReschedule: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = reservation.subject,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Con ${reservation.teacherName}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, "Fecha y hora", Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("${reservation.date} a las ${reservation.time}", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onReschedule, modifier = Modifier.weight(1f)) {
                    Text("Reprogramar")
                }
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}

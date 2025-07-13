package com.universitatcarlemany.unitutoring.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.universitatcarlemany.unitutoring.database.AppDatabase
import com.universitatcarlemany.unitutoring.model.Availability
import com.universitatcarlemany.unitutoring.model.Reservation
import com.universitatcarlemany.unitutoring.notifications.NotificationScheduler
import com.universitatcarlemany.unitutoring.ui.theme.UniTutoringTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Activity para gestionar la creación de una nueva reserva de tutoría.
 * Muestra un calendario visual para que el usuario seleccione un día y una hora.
 */
class ReservationActivity : ComponentActivity() {
    /**
     * Se ejecuta al crear la Activity.
     *
     * Inicializa la vista, obtiene los datos del profesor pasados a través del Intent,
     * y configura el contenido de la UI con Jetpack Compose.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val teacherName = intent.getStringExtra("name") ?: "N/A"
        val subject = intent.getStringExtra("subject") ?: "N/A"
        val availabilityJson = intent.getStringExtra("availability_json")
        val availability = if (availabilityJson != null) {
            val type = object : TypeToken<List<Availability>>() {}.type
            Gson().fromJson<List<Availability>>(availabilityJson, type)
        } else {
            emptyList()
        }

        setContent {
            UniTutoringTheme {
                CalendarReservationScreen(
                    teacherName = teacherName,
                    subject = subject,
                    teacherAvailability = availability,
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}

/**
 * Composable principal que define la estructura de la pantalla de reserva con calendario.
 *
 * @param teacherName El nombre del profesor para la tutoría.
 * @param subject La asignatura de la tutoría.
 * @param teacherAvailability La lista de rangos de disponibilidad del profesor.
 * @param onNavigateBack Callback para ejecutar la acción de volver a la pantalla anterior.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarReservationScreen(
    teacherName: String,
    subject: String,
    teacherAvailability: List<Availability>,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val today = LocalDate.now()
    val weekDays = List(7) { today.plusDays(it.toLong()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTime by remember { mutableStateOf<String?>(null) }
    var availableSlots by remember { mutableStateOf<List<String>>(emptyList()) }
    var bookedSlots by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedDate) {
        selectedTime = null
        val date = selectedDate
        if (date != null) {
            val dayOfWeekName = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
            availableSlots = generateTimeSlotsForDay(dayOfWeekName, teacherAvailability)
            val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val reservations = AppDatabase.getDatabase(context).reservationDao().getReservationsForTeacherOnDate(teacherName, dateString)
            bookedSlots = reservations.map { it.time }.toSet()
        } else {
            availableSlots = emptyList()
            bookedSlots = emptySet()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reservar con $teacherName") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)
        ) {
            val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES"))
            Text(
                text = (selectedDate ?: today).format(monthYearFormatter).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            WeekView(days = weekDays, selectedDate = selectedDate) { date ->
                selectedDate = if (selectedDate == date) null else date
            }
            Spacer(modifier = Modifier.height(24.dp))
            if (selectedDate != null) {
                Text("Horas disponibles", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                TimeSlotsGrid(
                    slots = availableSlots,
                    bookedSlots = bookedSlots,
                    selectedTime = selectedTime
                ) { time ->
                    selectedTime = if (selectedTime == time) null else time
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    // CORRECCIÓN: El botón principal ahora solo muestra el diálogo.
                    showConfirmationDialog = true
                },
                enabled = selectedDate != null && selectedTime != null,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Confirmar Reserva", style = MaterialTheme.typography.labelLarge)
            }
        }

        // --- Diálogo de Confirmación ---
        if (showConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmationDialog = false },
                icon = { Icon(Icons.Default.Info, contentDescription = "Confirmación") },
                title = { Text("Confirmar Tutoría") },
                text = {
                    Text("¿Quieres reservar una tutoría de $subject con $teacherName el día ${selectedDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} a las $selectedTime?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // La lógica para guardar y notificar está aquí.
                            scope.launch {
                                val reservation = Reservation(
                                    teacherName = teacherName,
                                    subject = subject,
                                    date = selectedDate!!.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                    time = selectedTime!!
                                )
                                AppDatabase.getDatabase(context).reservationDao().insertReservation(reservation)

                                // Se usa el nombre de función correcto (singular).
                                NotificationScheduler.scheduleReminders(context, reservation.teacherName, reservation.subject, reservation.date, reservation.time)

                                Toast.makeText(context, "Reserva confirmada y recordatorio programado", Toast.LENGTH_LONG).show()
                                showConfirmationDialog = false
                                onNavigateBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Sí, confirmar")
                    }
                },
                dismissButton = {
                    Button(onClick = { showConfirmationDialog = false }) {
                        Text("No, volver")
                    }
                }
            )
        }
    }
}

/**
 * Muestra una fila horizontal con los próximos 7 días para ser seleccionados.
 *
 * @param days La lista de objetos [LocalDate] a mostrar.
 * @param selectedDate El día actualmente seleccionado.
 * @param onDateSelected Callback que se invoca cuando un día es seleccionado.
 */
@Composable
fun WeekView(days: List<LocalDate>, selectedDate: LocalDate?, onDateSelected: (LocalDate) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(days) { day ->
            DayChip(day = day, isSelected = day == selectedDate, onClick = { onDateSelected(day) })
        }
    }
}

/**
 * Un Composable que representa un chip individual para un día en la [WeekView].
 *
 * @param day El objeto [LocalDate] para este chip.
 * @param isSelected Booleano que indica si este chip está seleccionado.
 * @param onClick Callback que se invoca al pulsar el chip.
 */
@Composable
fun DayChip(day: LocalDate, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = day.format(DateTimeFormatter.ofPattern("E", Locale("es", "ES"))),
            color = contentColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = day.dayOfMonth.toString(),
            color = contentColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

/**
 * Muestra una parrilla con los huecos de tiempo disponibles y ocupados.
 *
 * @param slots La lista de todos los huecos de tiempo generados para el día.
 * @param bookedSlots Un conjunto de los huecos que ya están reservados.
 * @param selectedTime El hueco de tiempo actualmente seleccionado.
 * @param onTimeSelected Callback que se invoca cuando se selecciona un hueco.
 */
@Composable
fun TimeSlotsGrid(slots: List<String>, bookedSlots: Set<String>, selectedTime: String?, onTimeSelected: (String) -> Unit) {
    if (slots.isEmpty()) {
        Text("No hay huecos disponibles para este día.", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 90.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(slots) { time ->
            TimeSlotChip(
                time = time,
                isBooked = time in bookedSlots,
                isSelected = time == selectedTime,
                onClick = { if (time !in bookedSlots) onTimeSelected(time) }
            )
        }
    }
}

/**
 * Un Composable que representa un chip individual para un hueco de tiempo.
 *
 * @param time El texto de la hora a mostrar.
 * @param isBooked Booleano que indica si el hueco está ocupado.
 * @param isSelected Booleano que indica si el hueco está seleccionado.
 * @param onClick Callback que se invoca al pulsar el chip.
 */
@Composable
fun TimeSlotChip(time: String, isBooked: Boolean, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = when {
        isBooked -> Color.Gray.copy(alpha = 0.3f)
        isSelected -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    }
    val contentColor = when {
        isBooked -> Color.Gray
        isSelected -> MaterialTheme.colorScheme.onSecondary
        else -> MaterialTheme.colorScheme.primary
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(enabled = !isBooked, onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = time, color = contentColor, fontWeight = FontWeight.Bold)
    }
}

/**
 * Genera una lista de huecos de tiempo a partir de un rango horario.
 *
 * @param dayOfWeek El día de la semana a consultar (ej. "Lunes").
 * @param availability La lista completa de rangos de disponibilidad del profesor.
 * @param slotDurationMinutes La duración de cada tutoría en minutos.
 * @return Una lista de strings con los horarios generados (ej. ["10:00", "11:00"]).
 */
private fun generateTimeSlotsForDay(
    dayOfWeek: String,
    availability: List<Availability>,
    slotDurationMinutes: Long = 60
): List<String> {
    val dayAvailability = availability.find { it.day.equals(dayOfWeek, ignoreCase = true) } ?: return emptyList()
    val slots = mutableListOf<String>()
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    try {
        var currentTime = LocalTime.parse(dayAvailability.startTime, formatter)
        val endTime = LocalTime.parse(dayAvailability.endTime, formatter)
        while (currentTime.isBefore(endTime)) {
            slots.add(currentTime.format(formatter))
            currentTime = currentTime.plusMinutes(slotDurationMinutes)
        }
    } catch (e: Exception) {
        return emptyList()
    }
    return slots
}

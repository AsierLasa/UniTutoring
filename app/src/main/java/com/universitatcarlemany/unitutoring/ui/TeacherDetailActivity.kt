package com.universitatcarlemany.unitutoring.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.universitatcarlemany.unitutoring.model.Availability
import com.universitatcarlemany.unitutoring.ui.theme.UniTutoringTheme

class TeacherDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val name = intent.getStringExtra("name") ?: "N/A"
        val subject = intent.getStringExtra("subject") ?: "N/A"
        val email = intent.getStringExtra("email") ?: "N/A"

        // Se recibe la disponibilidad como un string JSON
        val availabilityJson = intent.getStringExtra("availability_json")
        val availability = if (availabilityJson != null) {
            val type = object : TypeToken<List<Availability>>() {}.type
            Gson().fromJson<List<Availability>>(availabilityJson, type)
        } else {
            emptyList()
        }

        setContent {
            UniTutoringTheme {
                TeacherDetailScreen(
                    name = name,
                    subject = subject,
                    email = email,
                    availability = availability, // Se pasa la lista de objetos Availability
                    availabilityJson = availabilityJson ?: "", // Se pasa el JSON para la siguiente pantalla
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDetailScreen(
    name: String,
    subject: String,
    email: String,
    availability: List<Availability>,
    availabilityJson: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(name) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver a la lista")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subject,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                InfoRow(icon = Icons.Default.Email, text = email)
                Spacer(modifier = Modifier.height(16.dp))

                // CORRECCIÓN: Se muestra la nueva estructura de horarios
                InfoRow(icon = Icons.Default.Event, text = "Horarios Disponibles")
                Spacer(modifier = Modifier.height(8.dp))
                Column(modifier = Modifier.padding(start = 40.dp)) {
                    availability.forEach { avail ->
                        Text(
                            text = "• ${avail.day}: de ${avail.startTime} a ${avail.endTime}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val intent = Intent(context, ReservationActivity::class.java).apply {
                        // CORRECCIÓN: Se pasa la clave "name" para evitar el error "N/A"
                        putExtra("name", name)
                        putExtra("subject", subject)
                        putExtra("availability_json", availabilityJson)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Reservar Tutoría", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

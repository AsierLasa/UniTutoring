package com.universitatcarlemany.unitutoring.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.universitatcarlemany.unitutoring.ui.theme.UniTutoringTheme

import android.content.Intent
import androidx.compose.ui.platform.LocalContext

class TeacherDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val name = intent.getStringExtra("name") ?: ""
        val subject = intent.getStringExtra("subject") ?: ""
        val email = intent.getStringExtra("email") ?: ""
        val schedule = intent.getStringArrayListExtra("schedule") ?: arrayListOf()

        setContent {
            UniTutoringTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TeacherDetailScreen(
                        name = name,
                        subject = subject,
                        email = email,
                        schedule = schedule,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun TeacherDetailScreen(
    name: String,
    subject: String,
    email: String,
    schedule: List<String>,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = name, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = subject, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Email: $email", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Horarios disponibles:", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            for (time in schedule) {
                Text(text = "• $time")
            }
        }

        Column {
            Button(
                onClick = {
                    val intent = Intent(context, ReservationActivity::class.java).apply {
                        putExtra("teacherName", name)
                        putExtra("subject", subject)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
            ) {
                Text(text = "Reservar tutoría")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onBack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver")
            }
        }
    }
}

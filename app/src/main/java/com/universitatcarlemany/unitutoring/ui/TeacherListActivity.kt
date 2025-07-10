package com.universitatcarlemany.unitutoring.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.universitatcarlemany.unitutoring.model.Teacher
import com.universitatcarlemany.unitutoring.repository.TeacherRepository
import com.universitatcarlemany.unitutoring.ui.theme.UniTutoringTheme
import com.universitatcarlemany.unitutoring.MainActivity


class TeacherListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val teachers = TeacherRepository.getTeachers(this)

        setContent {
            UniTutoringTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TeacherListScreen(teachers)
                }
            }
        }
    }
}

@Composable
fun TeacherListScreen(teachers: List<Teacher>, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Available Teachers",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f) // ocupa espacio restante
        ) {
            items(teachers) { teacher ->
                TeacherCard(teacher = teacher) {
                    val intent = Intent(context, TeacherDetailActivity::class.java).apply {
                        putExtra("name", teacher.name)
                        putExtra("subject", teacher.subject)
                        putExtra("email", teacher.email)
                        putStringArrayListExtra("schedule", ArrayList(teacher.schedule))
                    }
                    context.startActivity(intent)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al menú")
        }
    }
}

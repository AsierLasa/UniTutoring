package com.universitatcarlemany.unitutoring

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.universitatcarlemany.unitutoring.ui.ReservationListActivity
import com.universitatcarlemany.unitutoring.ui.TeacherListActivity
import com.universitatcarlemany.unitutoring.ui.theme.UniTutoringTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniTutoringTheme {
                MainMenuScreen { option ->
                    when (option) {
                        1 -> {
                            val intent = Intent(this, TeacherListActivity::class.java)
                            startActivity(intent)
                        }
                        2 -> {
                            val intent = Intent(this, ReservationListActivity::class.java)
                            startActivity(intent)
                        }
                        3 -> {
                            finishAffinity() // Cierra completamente la app
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainMenuScreen(onSelectOption: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Menú principal", style = MaterialTheme.typography.headlineSmall)

        Button(onClick = { onSelectOption(1) }, modifier = Modifier.fillMaxWidth()) {
            Text("1. Crear nueva reserva")
        }

        Button(onClick = { onSelectOption(2) }, modifier = Modifier.fillMaxWidth()) {
            Text("2. Ver todas las reservas")
        }

        Button(onClick = { onSelectOption(3) }, modifier = Modifier.fillMaxWidth()) {
            Text("3. Salir")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainMenuPreview() {
    UniTutoringTheme {
        MainMenuScreen { }
    }
}

package com.universitatcarlemany.unitutoring.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.universitatcarlemany.unitutoring.model.Teacher

@Composable
fun TeacherCard(teacher: Teacher, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = teacher.name, style = MaterialTheme.typography.titleMedium)
            Text(text = teacher.subject, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

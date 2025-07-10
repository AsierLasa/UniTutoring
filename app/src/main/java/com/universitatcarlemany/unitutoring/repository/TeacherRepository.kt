package com.universitatcarlemany.unitutoring.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.universitatcarlemany.unitutoring.model.Teacher
import java.io.InputStreamReader

object TeacherRepository {
    fun getTeachers(context: Context): List<Teacher> {
        val inputStream = context.assets.open("profesores.json")
        val reader = InputStreamReader(inputStream)
        val gson = Gson()
        val type = object : TypeToken<List<Teacher>>() {}.type
        return gson.fromJson(reader, type)
    }
}

package com.universitatcarlemany.unitutoring.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.universitatcarlemany.unitutoring.model.Teacher
import java.io.InputStreamReader

object TeacherRepository {
    /**
     * Carga y devuelve la lista completa de profesores desde el archivo JSON local.
     *
     * @param context El contexto de la aplicación para acceder a los assets.
     * @return Una lista de objetos [Teacher].
     */
    fun getTeachers(context: Context): List<Teacher> {
        val inputStream = context.assets.open("profesores.json")
        val reader = InputStreamReader(inputStream)
        val gson = Gson()
        val type = object : TypeToken<List<Teacher>>() {}.type
        return gson.fromJson(reader, type)
    }

    /**
     * Busca y devuelve un profesor específico por su nombre.
     *
     * Esta función es necesaria para la lógica de reprogramación, para poder obtener
     * el horario completo de un profesor a partir de una reserva existente.
     *
     * @param context El contexto de la aplicación.
     * @param name El nombre del profesor a buscar.
     * @return El objeto [Teacher] si se encuentra, o `null` si no existe.
     */
    fun getTeacherByName(context: Context, name: String): Teacher? {
        val teachers = getTeachers(context)
        return teachers.find { it.name == name }
    }
}


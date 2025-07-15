package equipo.dinamita.otys.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.util.Log

class SensorDatabaseHelper(context: Context) : SQLiteOpenHelper(context, "sensores.db", null, 1) {

    private val TAG = "SensorDatabaseHelper"

    override fun onCreate(db: SQLiteDatabase) {
        Log.d(TAG, "Creando la tabla sensor_data...")
        val createTable = """
            CREATE TABLE sensor_data (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                sensor_name TEXT,
                value TEXT,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
            );
        """.trimIndent()
        db.execSQL(createTable)
        Log.d(TAG, "Tabla sensor_data creada correctamente")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "Actualizando base de datos de la versión $oldVersion a $newVersion, se eliminará la tabla existente")
        db.execSQL("DROP TABLE IF EXISTS sensor_data")
        onCreate(db)
        Log.d(TAG, "Base de datos actualizada")
    }

    fun insertSensorData(sensorName: String, value: Int) {
        Log.d(TAG, "Insertando dato: sensorName=$sensorName, value=$value")
        val db = writableDatabase
        val values = ContentValues().apply {
            put("sensor_name", sensorName)
            put("value", value)
        }
        val result = db.insert("sensor_data", null, values)
        if (result == -1L) {
            Log.e(TAG, "Error al insertar dato en sensor_data")
        } else {
            Log.d(TAG, "Dato insertado correctamente con id: $result")
        }
        db.close()
    }
}

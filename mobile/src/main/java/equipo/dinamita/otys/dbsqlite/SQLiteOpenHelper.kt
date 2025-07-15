package equipo.dinamita.otys.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues

class SensorDatabaseHelper(context: Context) : SQLiteOpenHelper(context, "sensores.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE sensor_data (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                sensor_name TEXT,
                value INTEGER,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
            );
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS sensor_data")
        onCreate(db)
    }

    fun insertSensorData(sensorName: String, value: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("sensor_name", sensorName)
            put("value", value)
        }
        db.insert("sensor_data", null, values)
        db.close()
    }
}

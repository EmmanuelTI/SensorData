package equipo.dinamita.otys.dbsqlite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.util.Log

class SensorDatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, "sensores.db", null, 1) {

    private val TAG = "SensorDatabaseHelper"

    //FUNCION ENCARGADA DE CREAR LA DB
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

    //FUNCION ENCARGADA DE ELIMINAR Y CREAR LA DB SI LA VERSION SE ACTUALIZA
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "Actualizando base de datos de la versión $oldVersion a $newVersion, se eliminará la tabla existente")
        db.execSQL("DROP TABLE IF EXISTS sensor_data")
        onCreate(db)
        Log.d(TAG, "Base de datos actualizada")
    }

    //FUNCION ENCARGADA DE INSERTAR LOS VALORES DE LOS SENSORES EN LA DB
    fun insertSensorData(sensorName: String, value: String) {
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

    //FUNCION ENCARGADA DE ELIMINAR TODAS LAS DBS Y SUS JOURNALS, SOLO SE CONSERVA LA QUE ESTA EN USO
    fun deleteOtherDatabasesAndJournals() {
        val dbNameToKeep = "sensores.db"
        val journalToKeep = "$dbNameToKeep-journal"

        // Eliminar otras bases de datos
        val dbList = context.databaseList()
        for (dbName in dbList) {
            if (dbName != dbNameToKeep) {
                val deleted = context.deleteDatabase(dbName)
                if (deleted) {
                    Log.d(TAG, "Base de datos eliminada: $dbName")
                } else {
                    Log.e(TAG, "No se pudo eliminar la base de datos: $dbName")
                }
            }
        }

        // Eliminar archivos .db-journal huérfanos
        val dbDir = context.getDatabasePath(dbNameToKeep).parentFile
        dbDir?.listFiles()?.forEach { file ->
            if (file.name.endsWith(".db-journal") && file.name != journalToKeep) {
                val deleted = file.delete()
                if (deleted) {
                    Log.d(TAG, "Archivo journal eliminado: ${file.name}")
                } else {
                    Log.e(TAG, "No se pudo eliminar el archivo journal: ${file.name}")
                }
            }
        }
    }

}

package equipo.dinamita.otys.dbsqlite

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import equipo.dinamita.otys.dbsqlite.model.User

class SQLUser(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "UserDatabase.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "users"

        private const val COLUMN_UID = "uid"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_BIRTHDATE = "birthdate"
        private const val COLUMN_CREATED_AT = "created_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_UID TEXT PRIMARY KEY,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_EMAIL TEXT NOT NULL UNIQUE,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_BIRTHDATE TEXT,
                $COLUMN_CREATED_AT INTEGER
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertUser(user: User): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_UID, user.uid)
            put(COLUMN_NAME, user.name)
            put(COLUMN_EMAIL, user.email)
            put(COLUMN_PASSWORD, user.password)
            put(COLUMN_BIRTHDATE, user.birthdate)
            put(COLUMN_CREATED_AT, user.createdAt)
        }

        val result = db.insert(TABLE_NAME, null, values)
        db.close()
        return result != -1L
    }
}

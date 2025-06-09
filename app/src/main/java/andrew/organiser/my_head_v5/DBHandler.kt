package andrew.organiser.my_head_v5

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHandler (context: Context?) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    //Set foreign key restraints
    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    // below method is for creating a database by running a sqlite query
    override fun onCreate(db: SQLiteDatabase) {
        val createContextTable =
            "CREATE TABLE IF NOT EXISTS $CONTEXT_TABLE_NAME " +
                    "($CONTEXT_ID_COL INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                    "$CONTEXT_NAME_COL TEXT UNIQUE," +
                    "$CONTEXT_EXCLUDE_COL BOOLEAN)"
        db.execSQL(createContextTable)

        val createTaskTable =
            "CREATE TABLE IF NOT EXISTS $TASK_TABLE_NAME " +
                    "($TASK_ID_COL INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                    "$TASK_CONTEXT_ID_COL INTEGER," +
                    "$TASK_NAME_COL TEXT UNIQUE, " +
                    "$TASK_MOTIVE_COL TEXT, " +
                    "$TASK_COMPLEXITY_COL INTEGER," +
                    "$TASK_MOTIVATION_COL INTEGER," +
                    "$TASK_START_DATE_COL DATETIME2," +
                    "$TASK_DUE_DATE_COL DATETIME2," +
                    "$TASK_CHECKLIST_COL BOOLEAN," +
                    "$TASK_CHECKLIST_DATE_COL DATETIME2," +
                    "$TASK_REPEAT_COL BOOLEAN," +
                    "$TASK_REPEAT_CLAUSE_COL TEXT," +
                    "$TASK_REPEAT_CLAUSE_VALUE_COL TEXT," +
                    "$TASK_FREQUENCY_COL TEXT," +
                    "$TASK_CONDITION_COL INTEGER REFERENCES $TASK_TABLE_NAME ($TASK_ID_COL)," +
                    "$TASK_CONDITION_STATUS_COL BOOLEAN," +
                    "$TASK_NOTES_COL TEXT," +
                    "$TASK_COMPLETED_FLAG_COL BOOLEAN," +
                    "$TASK_COMPLETED_DATE_COL TEXT," +
                    "FOREIGN KEY($TASK_CONTEXT_ID_COL) REFERENCES $CONTEXT_TABLE_NAME ($CONTEXT_ID_COL) ON DELETE CASCADE)"
        db.execSQL(createTaskTable)

        val createSubTaskTable =
            "CREATE TABLE IF NOT EXISTS $SUBTASK_TABLE_NAME " +
                    "($SUBTASK_ID_COL INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                    "$SUBTASK_TASK_ID_COL INTEGER," +
                    "$SUBTASK_NAME_COL TEXT, " +
                    "$SUBTASK_DUE_DATE_COL DATE, " +
                    "$SUBTASK_COMPLETED_COL INTEGER," +
                    "FOREIGN KEY($SUBTASK_TASK_ID_COL) REFERENCES $TASK_TABLE_NAME ($TASK_ID_COL) ON DELETE CASCADE)"
        db.execSQL(createSubTaskTable)

        val createSettingsTable =
            "CREATE TABLE IF NOT EXISTS $SETTINGS_TABLE_NAME " +
                    "($SETTINGS_ID_COL INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                    "$SETTINGS_TASK_FEATURES TEXT, " +
                    "$SETTINGS_TASK_SORT_ORDER TEXT, " +
                    "$SETTINGS_UI_COLORS TEXT," +
                    "$SETTINGS_ARCHIVE_DELETE TEXT)"
        db.execSQL(createSettingsTable)

        //Insert first and only settings default
        val insertSettings =
            "INSERT INTO $SETTINGS_TABLE_NAME " +
                    "($SETTINGS_TASK_FEATURES, $SETTINGS_TASK_SORT_ORDER, $SETTINGS_UI_COLORS, $SETTINGS_ARCHIVE_DELETE) " +
            "VALUES ('$DEFAULT_SETTING_FEATURES', '$DEFAULT_SETTING_SORT', '$DEFAULT_SETTING_COLORS', '$DEFAULT_SETTING_ARCHIVE')"
        db.execSQL(insertSettings)
    }

    //--- Basic generic CRUD Function calls ---//
    fun newEntry(dbTable: String, values: ContentValues) : Boolean{
        val db = this.writableDatabase
        val returnFlag = db.insert(dbTable, null, values) != -1L
        db.close()
        return returnFlag
    }

    fun readDBTable(dbTable: String): ArrayList<String>{
        val db = this.readableDatabase
        val returnList: ArrayList<String> = ArrayList()
        try{
            val dbCursor = db.rawQuery("SELECT * FROM $dbTable", null)
            if (dbCursor.moveToFirst()) {
                do {
                    var cursorLineStr = ""
                    for (column in 0..< dbCursor.columnCount){
                        cursorLineStr += when(dbCursor.getType(column)){
                            Cursor.FIELD_TYPE_INTEGER -> "${dbCursor.getInt(column)}\t"
                            Cursor.FIELD_TYPE_STRING -> "${dbCursor.getString(column)}\t"
                            Cursor.FIELD_TYPE_NULL -> "\t"
                            else -> ""
                        }
                    }
                    returnList.add(cursorLineStr.take(cursorLineStr.length-1))
                } while (dbCursor.moveToNext())
            }
            dbCursor.close()

        }catch (e: java.lang.Exception){
            println("~~~ Error: $e ~~~")
        }

        return returnList
    }

    fun updateEntry(dbTable: String, whereClause: String?, newValues: ContentValues) : Boolean{
        val db = this.writableDatabase
        var returnFlag = true
        try{
            db.update(dbTable, newValues, whereClause, arrayOf())
        }catch (e: SQLiteConstraintException){
            println("~~~ Error: $e ~~~")
            returnFlag = false
        }
        db.close()
        return returnFlag
    }

    fun deleteEntry(dbTable: String, whereClause: String?, whereArgs: Array<String>) : Boolean{
        val db = this.writableDatabase
        var returnFlag = true

        try{
            db.delete(dbTable, whereClause, whereArgs)
        }catch (e: SQLiteConstraintException){
            println("~~~ Error: $e ~~~")
            returnFlag = false
        }
        db.close()
        return returnFlag
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        //resetDatabase(db)
    }

    private fun resetDatabase(db: SQLiteDatabase){
        println("=== Reset Database Attempt ===")
        db.execSQL("DROP TABLE IF EXISTS $CONTEXT_TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $TASK_TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $SUBTASK_TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $SETTINGS_TABLE_NAME")
        onCreate(db)
    }

    companion object {

        const val DB_NAME = "my_head_db"
        private const val DB_VERSION = 39

        // Context Table
        const val CONTEXT_TABLE_NAME = "my_head_context"
        const val CONTEXT_ID_COL = "context_ID"
        const val CONTEXT_NAME_COL = "context_NAME"
        const val CONTEXT_EXCLUDE_COL = "context_EXCLUDE"

        // Task Table
        const val TASK_TABLE_NAME = "my_head_task"
        const val TASK_ID_COL = "task_ID"
        const val TASK_CONTEXT_ID_COL = "task_CONTEXT_ID"
        const val TASK_NAME_COL = "task_NAME"
        const val TASK_MOTIVE_COL = "task_MOTIVE"
        const val TASK_COMPLEXITY_COL = "task_COMPLEXITY"
        const val TASK_MOTIVATION_COL = "task_MOTIVATION"
        const val TASK_START_DATE_COL = "task_START_DATE"
        const val TASK_DUE_DATE_COL = "task_DUE_DATE"
        const val TASK_CHECKLIST_COL = "task_CHECKLIST"
        const val TASK_CHECKLIST_DATE_COL = "task_CHECKLIST_DATE"
        const val TASK_REPEAT_COL = "task_REPEAT"
        const val TASK_REPEAT_CLAUSE_COL = "task_REPEAT_CLAUSE"
        const val TASK_REPEAT_CLAUSE_VALUE_COL = "task_REPEAT_CLAUSE_VALUE"
        const val TASK_FREQUENCY_COL = "task_FREQUENCY"
        const val TASK_CONDITION_COL = "task_CONDITION"
        const val TASK_CONDITION_STATUS_COL = "task_CONDITION_STATUS"
        const val TASK_NOTES_COL = "task_NOTES"
        const val TASK_COMPLETED_FLAG_COL = "task_COMPLETED_FLAG"
        const val TASK_COMPLETED_DATE_COL = "task_COMPLETED_DATE"

        // Sub-Task Table
        const val SUBTASK_TABLE_NAME = "my_head_subtask"
        const val SUBTASK_ID_COL = "subtask_ID"
        const val SUBTASK_TASK_ID_COL = "subtask_TASK_ID"
        const val SUBTASK_NAME_COL = "subtask_NAME"
        const val SUBTASK_DUE_DATE_COL = "subtask_DUE_DATE"
        const val SUBTASK_COMPLETED_COL = "subtask_COMPLETED"

        // Settings Table
        const val SETTINGS_TABLE_NAME = "my_head_settings"
        const val SETTINGS_ID_COL = "setting_ID"
        const val SETTINGS_TASK_FEATURES = "setting_TASK_FEATURES"
        const val SETTINGS_TASK_SORT_ORDER = "setting_TASK_SORT_ORDER"
        const val SETTINGS_UI_COLORS = "setting_UI_COLORS"
        const val SETTINGS_ARCHIVE_DELETE = "setting_ARCHIVE_DELETE"
        const val DEFAULT_SETTING_FEATURES = "Motivation:false,Complexity:false,Checklist:false,Repeating:false,Conditions:false,TimeProgress:false,ChecklistProgress:false"
        const val DEFAULT_SETTING_SORT = "Incomplete: High_1,Pending: Low_2,Due Date: Ascending_3,Complexity: Ascending_4,Motivation: Ascending_5"
        const val DEFAULT_SETTING_COLORS = "overdue:#B30000,today:#FD4A4A,tomorrow:#FC722D,threeDays:#F7AB43,week:#F9E07B,weekPlus:#C0FCA7,conditional:#A6C8FF,pending:#7F93F9,startToday:#D47DEE,completed:#AAAAAA,timePB:#34AAFB,checklistPB:#34FB82"
        const val DEFAULT_SETTING_ARCHIVE = "never"

    }
}
package andrew.organiser.my_head.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.lang.Exception

/**
 * Database Helper - Class that creates and maintains SQLite database
 *      - Creates a new database in first instance and when reset
 *      - Generates tables for Contexts, Tasks, Sub-tasks, and Settings
 *      - Inserts default settings into settings table on Create
 *      - Helper functions that perform CRUD operations to any table and datapoint
 *      - Reset Database function if version is upgraded
 *      - Contains reference string values for all data columns
 *      - Contains reference string values for the default settings
 */

class DBHandler (context: Context?) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    private val tag = "DB Helper"

    //Set foreign key restraints
    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    // below method is for creating a database by running a sqlite query
    override fun onCreate(db: SQLiteDatabase) {
        Log.v(tag , "--- $tag: onCreate function activated ---")
        val createContextTable =
            "CREATE TABLE IF NOT EXISTS $CONTEXT_TABLE " +
                    "($ID_COL INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                    "$NAME_COL TEXT UNIQUE," +
                    "$EXCLUDE_FLAG_COL BOOLEAN)"
        db.execSQL(createContextTable)

        val createTaskTable =
            "CREATE TABLE IF NOT EXISTS $TASK_TABLE " +
                    "($ID_COL INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                    "$CONTEXT_ID_COL INTEGER," +
                    "$NAME_COL TEXT UNIQUE, " +
                    "$DESCRIPTION_COL TEXT, " +
                    "$COMPLEXITY_COL INTEGER," +
                    "$MOTIVATION_COL INTEGER," +
                    "$START_DATE_COL DATETIME2," +
                    "$DUE_DATE_COL DATETIME2," +
                    "$CHECKLIST_FLAG_COL BOOLEAN," +
                    "$EARLIEST_DUE_DATE_COL DATETIME2," +
                    "$REPEAT_CLAUSE_COL TEXT," +
                    "$CONDITION_ID_COL INTEGER REFERENCES $TASK_TABLE ($ID_COL)," +
                    "$CONDITION_STATUS_FLAG_COL BOOLEAN," +
                    "$CONDITION_CHANGE_START_FLAG_COL BOOLEAN," +
                    "$NOTES_COL TEXT," +
                    "$COMPLETED_DATE_COL TEXT," +
                    "FOREIGN KEY($CONTEXT_ID_COL) REFERENCES $CONTEXT_TABLE ($ID_COL) ON DELETE CASCADE)"
        db.execSQL(createTaskTable)

        val createSubTaskTable =
            "CREATE TABLE IF NOT EXISTS $SUBTASK_TABLE " +
                    "($ID_COL INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                    "$TASK_ID_COL INTEGER," +
                    "$SUB_ORDER INTEGER," +
                    "$NAME_COL TEXT, " +
                    "$SUB_DUE_DATE_COL DATE, " +
                    "$COMPLETED_FLAG_COL BOOLEAN," +
                    "FOREIGN KEY($TASK_ID_COL) REFERENCES $TASK_TABLE ($ID_COL) ON DELETE CASCADE)"
        db.execSQL(createSubTaskTable)

        val createSettingsTable =
            "CREATE TABLE IF NOT EXISTS $SETTINGS_TABLE " +
                    "($ID_COL INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                    "$TASK_FEATURES TEXT, " +
                    "$TASK_SORT_ORDER TEXT, " +
                    "$UI_COLORS TEXT," +
                    "$ARCHIVE_SETTINGS TEXT)"
        db.execSQL(createSettingsTable)

        //Insert first and only settings default
        val insertSettings =
            "INSERT INTO $SETTINGS_TABLE " +
                    "($TASK_FEATURES, $TASK_SORT_ORDER, $UI_COLORS, $ARCHIVE_SETTINGS) " +
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

        }catch (e: Exception){
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
        resetDatabase(db)
    }

    private fun resetDatabase(db: SQLiteDatabase){
        Log.v(tag , "--- $tag: Reset Database Function Activated ---")
        db.execSQL("DROP TABLE IF EXISTS $CONTEXT_TABLE")
        db.execSQL("DROP TABLE IF EXISTS '$TASK_TABLE'")
        db.execSQL("DROP TABLE IF EXISTS '$SUBTASK_TABLE'")
        db.execSQL("DROP TABLE IF EXISTS '$SETTINGS_TABLE'")
        onCreate(db)
    }

    companion object {

        const val DB_NAME = "My_Head_DB"
        private const val DB_VERSION = 1

        // Context Table
        const val CONTEXT_TABLE = "Context_Table"
        const val ID_COL = "Id"
        const val NAME_COL = "Name"
        const val EXCLUDE_FLAG_COL = "Exclude_Flag"

        // Task Table
        const val TASK_TABLE = "Task_Table"
        const val CONTEXT_ID_COL = "Context_Id_REF"
        const val DESCRIPTION_COL = "Description"
        const val COMPLEXITY_COL = "Complexity_Rating"
        const val MOTIVATION_COL = "Motivation_Rating"
        const val START_DATE_COL = "Start_Date_Time"
        const val DUE_DATE_COL = "Due_Date_Time"
        const val CHECKLIST_FLAG_COL = "Checklist_Flag"
        const val EARLIEST_DUE_DATE_COL = "Earliest_Due_Date"
        const val REPEAT_CLAUSE_COL = "Repeat_Clause"
        const val CONDITION_ID_COL = "Condition_Id_REF"
        const val CONDITION_STATUS_FLAG_COL = "Condition_Active_Flag"
        const val CONDITION_CHANGE_START_FLAG_COL = "Change_Start_Flag"
        const val NOTES_COL = "Notes"
        const val COMPLETED_DATE_COL = "Completed_Date"

        // Sub-Task Table
        const val SUBTASK_TABLE = "Subtask_Table"
        const val TASK_ID_COL = "Task_Id_REF"
        const val SUB_ORDER = "Subtask_Order"
        const val SUB_DUE_DATE_COL = "Due_Date"
        const val COMPLETED_FLAG_COL = "Completed_Flag"

        // Settings Table
        const val SETTINGS_TABLE= "Settings_Table"
        const val TASK_FEATURES = "Task_Features_Active"
        const val TASK_SORT_ORDER = "Task_List_Sort_Order"
        const val UI_COLORS = "UI_Colors"
        const val ARCHIVE_SETTINGS = "Archive_Settings"

        const val DEFAULT_SETTING_FEATURES = "Motivation:false,Complexity:false,Checklist:false,Repeating:false,Conditions:false,TimeProgress:false,ChecklistProgress:false"
        const val DEFAULT_SETTING_SORT = "Incomplete: High_1,Pending: Low_2,Due Date: Ascending_3,Complexity: Ascending_4,Motivation: Ascending_5"
        const val DEFAULT_SETTING_COLORS = "overdue:#B30000,today:#FD4A4A,tomorrow:#FC722D,threeDays:#F7AB43,week:#F9E07B,weekPlus:#C0FCA7,conditional:#A6C8FF,pending:#7F93F9,startToday:#D47DEE,completed:#AAAAAA,timePB:#34AAFB,checklistPB:#34FB82"
        const val DEFAULT_SETTING_ARCHIVE = "1_day_never"

    }
}
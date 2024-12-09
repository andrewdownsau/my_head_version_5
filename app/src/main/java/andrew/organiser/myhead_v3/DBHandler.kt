package andrew.organiser.myhead_v3

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import java.time.LocalDate

class DBHandler (context: Context?) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    // below method is for creating a database by running a sqlite query
    override fun onCreate(db: SQLiteDatabase) {
        val createContextTable =
            "CREATE TABLE IF NOT EXISTS $CONTEXT_TABLE_NAME " +
                    "($CONTEXT_ID_COL INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                    "$CONTEXT_NAME_COL TEXT UNIQUE)"
        db.execSQL(createContextTable)

        val createTaskTable =
            "CREATE TABLE IF NOT EXISTS $TASK_TABLE_NAME " +
                    "($TASK_ID_COL INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                    "$TASK_CONTEXT_ID_COL INTEGER," +
                    "$TASK_NAME_COL TEXT UNIQUE, " +
                    "$TASK_MOTIVE_COL TEXT, " +
                    "$TASK_COMPLEXITY_COL INTEGER," +
                    "$TASK_MOTIVATION_COL INTEGER," +
                    "$TASK_START_DATE_COL DATE," +
                    "$TASK_DUE_DATE_COL DATE," +
                    "$TASK_CHECKLIST_COL BOOLEAN," +
                    "$TASK_CHECKLIST_DATE_COL DATE," +
                    "$TASK_REPEAT_COL BOOLEAN," +
                    "$TASK_REPEAT_CLAUSE_COL TEXT," +
                    "$TASK_REPEAT_CLAUSE_VALUE_COL TEXT," +
                    "$TASK_FREQUENCY_COL TEXT," +
                    "$TASK_CONDITION_COL INTEGER REFERENCES $TASK_TABLE_NAME ($TASK_ID_COL)," +
                    "$TASK_NOTES_COL TEXT," +
                    "$TASK_COMPLETED_FLAG_COL BOOLEAN," +
                    "$TASK_COMPLETED_DATE_COL TEXT," +
                    "FOREIGN KEY($TASK_CONTEXT_ID_COL) REFERENCES $CONTEXT_TABLE_NAME ($CONTEXT_ID_COL))"
        db.execSQL(createTaskTable)

        val createSubTaskTable =
            "CREATE TABLE IF NOT EXISTS $SUBTASK_TABLE_NAME " +
                    "($SUBTASK_ID_COL INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                    "$SUBTASK_TASK_ID_COL INTEGER," +
                    "$SUBTASK_NAME_COL TEXT, " +
                    "$SUBTASK_DUE_DATE_COL DATE, " +
                    "$SUBTASK_COMPLETED_COL INTEGER," +
                    "FOREIGN KEY($SUBTASK_TASK_ID_COL) REFERENCES $TASK_TABLE_NAME ($TASK_ID_COL))"
        db.execSQL(createSubTaskTable)

        val createSettingsTable =
            "CREATE TABLE IF NOT EXISTS $SETTINGS_TABLE_NAME " +
                    "($SETTINGS_ID_COL INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                    "$SETTINGS_OVERDUE TEXT, " +
                    "$SETTINGS_TODAY TEXT, " +
                    "$SETTINGS_TOMORROW TEXT, " +
                    "$SETTINGS_THREE_DAYS TEXT, " +
                    "$SETTINGS_WEEK TEXT, " +
                    "$SETTINGS_WEEK_PLUS TEXT, " +
                    "$SETTINGS_CONDITIONAL TEXT, " +
                    "$SETTINGS_PENDING_SOON TEXT, " +
                    "$SETTINGS_COMPLETED TEXT)"
        db.execSQL(createSettingsTable)

        //Insert first and only settings default
        val insertSettings =
            "INSERT INTO $SETTINGS_TABLE_NAME " +
                    "($SETTINGS_OVERDUE, $SETTINGS_TODAY, $SETTINGS_TOMORROW, " +
                    "$SETTINGS_THREE_DAYS, $SETTINGS_WEEK, $SETTINGS_WEEK_PLUS, " +
                    "$SETTINGS_CONDITIONAL, $SETTINGS_PENDING_SOON, $SETTINGS_COMPLETED) " +
            "VALUES ('#B30000', '#FD4A4A', '#FC722D', '#F7AB43', '#F9E07B', " +
                    "'#C0FCA7', '#A6C8FF', '#7F93F9', '#AAAAAA')"
        db.execSQL(insertSettings)
    }

    //--- Context CRUD Functions ---//
    fun importContext(contextModal: ContextModal){
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(CONTEXT_ID_COL, contextModal.id)
        values.put(CONTEXT_NAME_COL, contextModal.getContextName())

        db.insert(CONTEXT_TABLE_NAME, null, values)
        db.close()
    }

    fun addNewContext(c:Context, contextName: String?) {
        println("_addNewContext: $contextName")
        if(!contextName.isNullOrEmpty()){
            val db = this.writableDatabase
            val values = ContentValues()
            values.put(CONTEXT_NAME_COL, contextName)

            if(db.insert(CONTEXT_TABLE_NAME, null, values) == -1L)
                Toast.makeText(c, "Create new context failed:\nContext name must be unique", Toast.LENGTH_LONG).show()
            db.close()
        }
    }

    fun readContextList(ordered: Boolean): ArrayList<ContextModal> {
        val db = this.readableDatabase
        val contextModalArrayList: ArrayList<ContextModal> = ArrayList()
        try{
            val cursorContexts = db.rawQuery("SELECT * FROM $CONTEXT_TABLE_NAME", null)
            if (cursorContexts.moveToFirst()) {
                do {
                    contextModalArrayList.add(
                        ContextModal(
                            cursorContexts.getInt(0),
                            cursorContexts.getString(1)
                        )
                    )
                } while (cursorContexts.moveToNext())
            }
            cursorContexts.close()

        }catch (e: java.lang.Exception){
            println("___ Error: $e ___")
        }
        return if(ordered)
            orderContextList(contextModalArrayList)
        else
            contextModalArrayList
    }

    fun updateContext(c: Context, originalContextName: String, contextName: String?) {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(CONTEXT_NAME_COL, contextName)

        try{
            db.update(CONTEXT_TABLE_NAME, values, "$CONTEXT_NAME_COL=?", arrayOf(originalContextName))
        }catch (e: SQLiteConstraintException){
            Toast.makeText(c, "Edit context failed:\nContext name must be unique", Toast.LENGTH_SHORT).show()
            println("___Error: $e")
        }
        db.close()
    }

    fun deleteContext(contextName: String) {
        val db = this.writableDatabase

        //Find matching Id to context name and delete all tasks with foreign key
        val contextId = readContextList(false).filter { it.getContextName() == contextName }[0].id
        db.delete(TASK_TABLE_NAME, "$TASK_CONTEXT_ID_COL=$contextId", null)

        db.delete(CONTEXT_TABLE_NAME, "$CONTEXT_NAME_COL=?", arrayOf(contextName))
        db.close()
    }

    //--- Task CRUD Functions ---//
    fun importTask(taskObject: TaskModal){
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(TASK_ID_COL, taskObject.id)
        values.putAll(taskToValues(taskObject))

        db.insert(TASK_TABLE_NAME, null, values)
        db.close()
    }

    fun addEditTask(c: Context, originalTaskName: String, taskObject: TaskModal, updateFlag:Boolean ) : Int{
        println("+++ addEditTask: ${taskObject.getTaskName()} +++")
        val db = this.writableDatabase
        val values = taskToValues(taskObject)
        var newTaskId = 0

        if(updateFlag){
            try{
                db.update(TASK_TABLE_NAME, values, "$TASK_NAME_COL=?", arrayOf(originalTaskName))

                //Check if any conditions for the task can be cleared with the change
                if(taskObject.getTaskCompletedFlag()){
                    val conditionValues = ContentValues()
                    conditionValues.put(TASK_CONDITION_COL, 0)

                    //Sanitise any quotations from name for where clause
                    var sanitisedName = originalTaskName
                    if(sanitisedName.contains("'")) sanitisedName = sanitisedName.replace("'", "''")
                    println("___sanitisedName: $sanitisedName")
                    val taskObjectId = readTaskList(" WHERE $TASK_NAME_COL='$sanitisedName'", false, false)[0].id
                    println("___taskObjectId: $taskObjectId")
                    db.update(TASK_TABLE_NAME, conditionValues, "$TASK_CONDITION_COL=$taskObjectId", null)
                }
            }catch (e: SQLiteConstraintException){
                if(e.toString().contains("UNIQUE constraint failed"))
                    Toast.makeText(c, "Edit task failed:\nTasks name must be unique", Toast.LENGTH_SHORT).show()
                else if(e.toString().contains("CHECK constraint failed"))
                    Toast.makeText(c, "Edit task failed:\nStart cannot be before due", Toast.LENGTH_SHORT).show()
                println("___Error: $e")
            }
        }
        else {
            newTaskId = db.insert(TASK_TABLE_NAME, null, values).toInt()
            println("_newTaskId = $newTaskId")
            if(newTaskId == -1)
                Toast.makeText(c, "Create new task failed:\nPlease try again", Toast.LENGTH_SHORT).show()

        }
        db.close()

        //Return a new task id value to assign to subtask list if new task
        return if(updateFlag)
            taskObject.id
        else{
            newTaskId
        }
    }

    fun readTaskList(whereClause: String, orderedFlag: Boolean, archiveFlag: Boolean): ArrayList<TaskModal> {
        println("=== readTaskList $whereClause===")

        val db = this.readableDatabase
        val taskModalArrayList: ArrayList<TaskModal> = ArrayList()
        try{
            val dbQueryStr = "SELECT * FROM $TASK_TABLE_NAME$whereClause"

            val cursorTasks = db.rawQuery(dbQueryStr, null)

            // moving our cursor to first position.
            if (cursorTasks.moveToFirst()) {
                do {
                    // on below line we are adding the data from cursor to our array list.
                    taskModalArrayList.add(
                        TaskModal(
                            cursorTasks.getInt(0),
                            cursorTasks.getInt(1),
                            cursorTasks.getString(2),
                            cursorTasks.getString(3),
                            cursorTasks.getInt(4),
                            cursorTasks.getInt(5),
                            cursorTasks.getString(6),
                            cursorTasks.getString(7),
                            cursorTasks.getInt(8) > 0,
                            cursorTasks.getString(9),
                            cursorTasks.getInt(10) > 0,
                            cursorTasks.getString(11),
                            cursorTasks.getString(12),
                            cursorTasks.getString(13),
                            cursorTasks.getInt(14),
                            cursorTasks.getString(15),
                            cursorTasks.getInt(16) > 0,
                            cursorTasks.getString(17)

                        )
                    )
                } while (cursorTasks.moveToNext())
            }
            cursorTasks.close()

        }catch (e: java.lang.Exception){
            println("___ Error: $e ___")
        }
        return if(orderedFlag)
            orderTaskListByDefault(taskModalArrayList)
        else if(archiveFlag)
            orderTaskListCompleted(taskModalArrayList)
        else
            taskModalArrayList
    }

    fun deleteTask(originalTaskName: String, taskId : Int) {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(TASK_CONDITION_COL, 0)
        db.update(TASK_TABLE_NAME, values, "$TASK_CONDITION_COL=$taskId", null)

        db.delete(SUBTASK_TABLE_NAME, "$SUBTASK_TASK_ID_COL=$taskId", null)
        db.delete(TASK_TABLE_NAME, "$TASK_NAME_COL=?", arrayOf(originalTaskName))
        db.close()
    }

    private fun taskToValues(taskObject: TaskModal):ContentValues{
        val values = ContentValues()
        values.put(TASK_CONTEXT_ID_COL, taskObject.contextId)
        values.put(TASK_NAME_COL, taskObject.getTaskName())
        values.put(TASK_MOTIVE_COL, taskObject.getTaskMotive())
        values.put(TASK_COMPLEXITY_COL, taskObject.getTaskComplexity())
        values.put(TASK_MOTIVATION_COL, taskObject.getTaskMotivation())
        values.put(TASK_START_DATE_COL, taskObject.getTaskStartDate())
        values.put(TASK_DUE_DATE_COL, taskObject.getTaskDueDate())
        values.put(TASK_CHECKLIST_COL, taskObject.getTaskChecklist())
        values.put(TASK_CHECKLIST_DATE_COL, taskObject.getTaskChecklistDate())
        values.put(TASK_REPEAT_COL, taskObject.getTaskRepeat())
        values.put(TASK_REPEAT_CLAUSE_COL, taskObject.getTaskRepeatClause())
        values.put(TASK_REPEAT_CLAUSE_VALUE_COL, taskObject.getTaskRepeatClauseValue())
        values.put(TASK_FREQUENCY_COL, taskObject.getTaskFrequency())
        values.put(TASK_CONDITION_COL, taskObject.getTaskConditionId())
        values.put(TASK_NOTES_COL, taskObject.getTaskNotes())
        values.put(TASK_COMPLETED_FLAG_COL, taskObject.getTaskCompletedFlag())
        values.put(TASK_COMPLETED_DATE_COL, taskObject.getTaskCompletedDate())

        return values
    }

    fun repeatRefreshCheck(c:Context){
        println("--- repeatRefreshCheck ---")
        //Check if there are any tasks that were completed before today that have a repeat clause
        val todayDateStr = LocalDate.now().format(MainActivity.DATE_FORMAT)
        val todayDate = LocalDate.parse(todayDateStr, MainActivity.DATE_FORMAT)
        val repeatedTasks = readTaskList(" WHERE $TASK_REPEAT_COL=1 AND NOT $TASK_FREQUENCY_COL='None' AND " +
                "NOT $TASK_COMPLETED_FLAG_COL = 0 AND " +
                "NOT $TASK_COMPLETED_DATE_COL = '$todayDateStr' AND " +
                "NOT $TASK_REPEAT_CLAUSE_VALUE_COL = '$todayDateStr' AND " +
                "NOT $TASK_REPEAT_CLAUSE_VALUE_COL = '0'", false, false)
        if(repeatedTasks.isNotEmpty()){
            repeatedTasks.forEach { task ->
                println("_Repeated task: ${task.getTaskName()}")
                //First determine if until clause has been met or not
                if(task.getTaskRepeatClause() == "Until") {
                    try{
                        val untilDate = LocalDate.parse(task.getTaskRepeatClauseValue(), MainActivity.DATE_FORMAT)
                        if(todayDate.isBefore(untilDate)){
                            refreshRepeatedTask(c, task, todayDate)
                        }

                    }catch(e: Exception){println("___Error: $e")}
                }
                else{
                    refreshRepeatedTask(c, task, todayDate)
                }
            }
        }
    }

    private fun refreshRepeatedTask(c: Context, task: TaskModal, todayDate:LocalDate){
        //Clear the completed flag for the task
        task.setTaskCompletedFlag(false)
        task.setTaskCompletedDate("")

        //Add appropriate time amount based on repeat frequency to start and due date
        var startDate = LocalDate.parse(task.getTaskStartDate(), MainActivity.DATE_FORMAT)
        var dueDate = LocalDate.parse(task.getTaskDueDate(), MainActivity.DATE_FORMAT)
        var checkListDate = LocalDate.parse(task.getTaskChecklistDate(), MainActivity.DATE_FORMAT)
        var numberOfShifts = 0

        while(startDate.isBefore(todayDate)){
            startDate = addTimeToDate(task.getTaskFrequency(), startDate.format(MainActivity.DATE_FORMAT))
            dueDate = addTimeToDate(task.getTaskFrequency(), dueDate.format(MainActivity.DATE_FORMAT))
            checkListDate = addTimeToDate(task.getTaskFrequency(), checkListDate.format(MainActivity.DATE_FORMAT))
            numberOfShifts++
        }

        //Set new dates for task and then update in table
        task.setTaskStartDate(startDate.format(MainActivity.DATE_FORMAT))
        task.setTaskDueDate(dueDate.format(MainActivity.DATE_FORMAT))
        task.setTaskChecklistDate(checkListDate.format(MainActivity.DATE_FORMAT))

        //Change remaining times to repeat the task if set to after times clause
        try{
            if(task.getTaskRepeatClause() == "After")
                task.setTaskRepeatClauseValue((task.getTaskRepeatClauseValue().toInt() - 1).toString())
        }catch(_:Exception){}

        addEditTask(c, task.getTaskName(), task, true)

        //Change any dates in checklist to update when repeat is triggered
        val taskChecklist = readSubTaskList(" WHERE ${SUBTASK_TASK_ID_COL}=${task.id}")
        if(taskChecklist.isNotEmpty()){
            taskChecklist.forEach { subtask ->
                var subtaskDate = LocalDate.parse(subtask.getSubtaskDueDate(), MainActivity.DATE_FORMAT)
                var tempNumberOfShifts = numberOfShifts
                while(tempNumberOfShifts > 0) {
                    subtaskDate = addTimeToDate(task.getTaskFrequency(), subtaskDate.format(MainActivity.DATE_FORMAT))
                    tempNumberOfShifts--
                }
                subtask.setSubtaskDueDate(subtaskDate.format(MainActivity.DATE_FORMAT))
                subtask.setSubtaskCompletedFlag(false)
            }
            addEditSubTasks(c, task.id, taskChecklist)
        }
    }

    //--- Sub Task CRUD Functions ---//
    fun importSubTask(subtaskObject: SubtaskModal){
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(SUBTASK_ID_COL, subtaskObject.id)
        values.putAll(subtaskToValues(subtaskObject, subtaskObject.taskId))

        db.insert(SUBTASK_TABLE_NAME, null, values)
        db.close()
    }

    fun addEditSubTasks(c: Context, subTaskTaskId: Int, subtaskList: List<SubtaskModal> ) {
        println("+++ addEditSubTask for task id: $subTaskTaskId")
        if(subTaskTaskId != 0) {
            val db = this.writableDatabase

            //Purge the table of any subtasks that connect to this task
            db.delete(SUBTASK_TABLE_NAME, "$SUBTASK_TASK_ID_COL=$subTaskTaskId", null)

            //Add all new subtasks to the table from the list
            subtaskList.forEach { subtaskModal ->
                val values = subtaskToValues(subtaskModal, subTaskTaskId)
                if(db.insert(SUBTASK_TABLE_NAME, null, values) == -1L)
                    Toast.makeText(c, "Create new subtask failed:\nSubtask must be unique", Toast.LENGTH_SHORT).show()
            }
            db.close()
        }
    }

    fun readSubTaskList(whereClause: String): ArrayList<SubtaskModal> {
        println("=== readTaskList $whereClause===")

        val db = this.readableDatabase
        val subtaskModalArrayList: ArrayList<SubtaskModal> = ArrayList()
        try{
            val dbQueryStr = "SELECT * FROM $SUBTASK_TABLE_NAME$whereClause"
            val cursorTasks = db.rawQuery(dbQueryStr, null)

            if (cursorTasks.moveToFirst()) {
                do {
                    // on below line we are adding the data from cursor to our array list.
                    subtaskModalArrayList.add(
                        SubtaskModal(
                            cursorTasks.getInt(0),
                            cursorTasks.getInt(1),
                            cursorTasks.getString(2),
                            cursorTasks.getString(3),
                            cursorTasks.getInt(4) > 0
                        )
                    )
                } while (cursorTasks.moveToNext())
            }
            cursorTasks.close()

        }catch (e: java.lang.Exception){
            println("___ Error: $e ___")
        }
        return subtaskModalArrayList
    }

    private fun subtaskToValues(subtaskObject: SubtaskModal, taskId: Int):ContentValues{
        val values = ContentValues()
        values.put(SUBTASK_TASK_ID_COL, taskId)
        values.put(SUBTASK_NAME_COL, subtaskObject.getSubtaskName())
        values.put(SUBTASK_DUE_DATE_COL, subtaskObject.getSubtaskDueDate())
        values.put(SUBTASK_COMPLETED_COL, subtaskObject.getSubtaskCompletedFlag())

        return values
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
       resetDatabase(db)
    }

    private fun resetDatabase(db: SQLiteDatabase){
        println("=== Reset Database Attempt ===")
        db.execSQL("DROP TABLE IF EXISTS $CONTEXT_TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $TASK_TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $SUBTASK_TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $SETTINGS_TABLE_NAME")
        onCreate(db)
    }

    fun resetDatabase(){
        try{
            val db = this.writableDatabase
            resetDatabase(db)
        }catch(e:IllegalStateException){
            println("___Error: $e")
        }
    }

    //--- Settings READ/UPDATE Functions ---//
    fun importSettings(settingsObject: SettingsModal){
        val db = this.writableDatabase
        val values = ContentValues()
        values.putAll(settingsToValues(settingsObject))

        db.update(SETTINGS_TABLE_NAME, values, "", arrayOf(""))
        db.close()
    }

    private fun settingsToValues(settingsObject: SettingsModal):ContentValues{
        val values = ContentValues()
        values.put(SETTINGS_OVERDUE, settingsObject.getColor("overdue"))
        values.put(SETTINGS_TODAY, settingsObject.getColor("today"))
        values.put(SETTINGS_TOMORROW, settingsObject.getColor("tomorrow"))
        values.put(SETTINGS_THREE_DAYS, settingsObject.getColor("threeDays"))
        values.put(SETTINGS_WEEK, settingsObject.getColor("week"))
        values.put(SETTINGS_WEEK_PLUS, settingsObject.getColor("weekPlus"))
        values.put(SETTINGS_CONDITIONAL, settingsObject.getColor("conditional"))
        values.put(SETTINGS_PENDING_SOON, settingsObject.getColor("pending"))
        values.put(SETTINGS_COMPLETED, settingsObject.getColor("completed"))

        return values
    }

    fun readSettings(): SettingsModal? {
        val db = this.readableDatabase
        var settingsModal: SettingsModal? = null
        try{
            val cursorSettings = db.rawQuery("SELECT * FROM $SETTINGS_TABLE_NAME", null)
            if (cursorSettings.moveToFirst()) {
                settingsModal =
                    SettingsModal(
                        cursorSettings.getString(1),
                        cursorSettings.getString(2),
                        cursorSettings.getString(3),
                        cursorSettings.getString(4),
                        cursorSettings.getString(5),
                        cursorSettings.getString(6),
                        cursorSettings.getString(7),
                        cursorSettings.getString(8),
                        cursorSettings.getString(9)
                    )
            }
            cursorSettings.close()

        }catch (e: java.lang.Exception){
            println("___ Error: $e ___")
        }
        println("___ SettingsModal: $settingsModal ___")
        return settingsModal
    }

    fun updateSettings(c: Context, settingsObject: SettingsModal) {
        val db = this.writableDatabase

        val values = ContentValues()
        values.putAll(settingsToValues(settingsObject))

        try{
            db.update(SETTINGS_TABLE_NAME, values, "", arrayOf())
        }catch (e: SQLiteConstraintException){
            Toast.makeText(c, "Edit settings failed!", Toast.LENGTH_SHORT).show()
            println("___Error: $e")
        }
        db.close()
    }



    //--- Private functions to sort and reformat data ---//
    private fun addTimeToDate(frequencyType:String, originalDateStr: String): LocalDate{
        println("+++ addTimeToDate: $originalDateStr +++")
        val dayMovement = when(frequencyType){
            "Daily" -> 1
            "Weekly" -> 7
            "Fortnightly" -> 14
            else -> 0
        }
        val monthMovement = when(frequencyType){
            "Monthly" -> 1
            "Quarterly" -> 3
            "Yearly" -> 12
            else -> 0
        }

        //Debugging log prompts
        if(dayMovement > 0) println("_add $dayMovement days")
        if(monthMovement > 0) println("_add $monthMovement months")

        //Change start and due date by the required time jumps
        var date = LocalDate.parse(originalDateStr, MainActivity.DATE_FORMAT)

        date = date.plusDays(dayMovement.toLong())
        date = date.plusMonths(monthMovement.toLong())

        println("_new date: ${date.format(MainActivity.DATE_FORMAT)}")

        return date
    }

    private fun orderContextList(contextList: ArrayList<ContextModal>): ArrayList<ContextModal>{
        //Get top task for each context
        val topTaskList = ArrayList<TaskModal>()
        val emptyContextList = ArrayList<ContextModal>()
        val sortedContextList = ArrayList<ContextModal>()
        val todayDateStr = LocalDate.now().format(MainActivity.DATE_FORMAT)
        contextList.forEach{ context ->
            val contextTaskList = readTaskList(" WHERE ${TASK_CONTEXT_ID_COL}=${context.id} AND (${TASK_COMPLETED_DATE_COL}='$todayDateStr' OR NOT $TASK_COMPLETED_FLAG_COL)", true, false)
            if(contextTaskList.isNotEmpty()){ topTaskList.add(contextTaskList[0]) }
            else emptyContextList.add(context)
        }
        val sortedTopTaskList = orderTaskListByDefault(topTaskList)

        sortedTopTaskList.forEach { task ->
            sortedContextList.add(contextList.filter { it.id == task.contextId }[0])
        }
        sortedContextList += emptyContextList
        return sortedContextList
    }

    private fun orderTaskListByDefault(originalTaskList: ArrayList<TaskModal>): ArrayList<TaskModal>{
        if(originalTaskList.isNotEmpty()){
            //Split the lists into pending and not pending array
            val pendingTasks: ArrayList<TaskModal> = ArrayList(originalTaskList.filter {
                (LocalDate.parse(it.getTaskStartDate(), MainActivity.DATE_FORMAT) > LocalDate.now() || it.getTaskConditionId() != 0) && !it.getTaskCompletedFlag() })
            val nonPendingTasks: ArrayList<TaskModal> = ArrayList(originalTaskList.filter {
                LocalDate.parse(it.getTaskStartDate(), MainActivity.DATE_FORMAT) <= LocalDate.now() && it.getTaskConditionId() == 0 && !it.getTaskCompletedFlag() })
            val completedTasks: ArrayList<TaskModal> = ArrayList(originalTaskList.filter { it.getTaskCompletedFlag() })
            val sortedNPTaskList: ArrayList<TaskModal> = ArrayList(nonPendingTasks.sortedWith(nullsLast(compareBy(
                {   it.getTaskCompletedFlag() },
                {   LocalDate.parse(it.getTaskChecklistDate(), MainActivity.DATE_FORMAT) },
                {   it.getTaskComplexity() },
                {   it.getTaskMotivation()  }))))
            val sortedPTaskList: ArrayList<TaskModal> = ArrayList(pendingTasks.sortedWith(nullsLast(compareBy(
                {   it.getTaskConditionId() != 0 },
                {   LocalDate.parse(it.getTaskStartDate(), MainActivity.DATE_FORMAT) }))))

            val sortedTaskList = ArrayList(sortedNPTaskList.plus(sortedPTaskList).plus(completedTasks))

            return sortedTaskList
        }
        return ArrayList()
    }

    private fun orderTaskListCompleted(originalTaskList: ArrayList<TaskModal>): ArrayList<TaskModal>{
        if(originalTaskList.isNotEmpty()){
            val sortedTaskList: ArrayList<TaskModal> = ArrayList(originalTaskList.sortedWith(nullsLast(
                compareByDescending { LocalDate.parse(it.getTaskCompletedDate(), MainActivity.DATE_FORMAT) })))

            return sortedTaskList
        }
        return ArrayList()
    }

    companion object {
        const val DB_NAME = "my_head_db"
        private const val DB_VERSION = 18

        // Context Table
        const val CONTEXT_TABLE_NAME = "my_head_context"
        const val CONTEXT_ID_COL = "context_ID"
        const val CONTEXT_NAME_COL = "context_NAME"

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
        const val SETTINGS_OVERDUE = "setting_OVERDUE_COLOR"
        const val SETTINGS_TODAY = "setting_TODAY_COLOR"
        const val SETTINGS_TOMORROW = "setting_TOMORROW_COLOR"
        const val SETTINGS_THREE_DAYS = "setting_THREE_DAYS_COLOR"
        const val SETTINGS_WEEK = "setting_WEEK_COLOR"
        const val SETTINGS_WEEK_PLUS = "setting_WEEK_PLUS_COLOR"
        const val SETTINGS_CONDITIONAL = "setting_CONDITIONAL_COLOR"
        const val SETTINGS_PENDING_SOON = "setting_PENDING_SOON_COLOR"
        const val SETTINGS_COMPLETED = "setting_COMPLETED_COLOR"
    }
}
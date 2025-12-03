package andrew.organiser.my_head_v5.features.data_manipulation


import andrew.organiser.my_head_v5.DBHandler
import andrew.organiser.my_head_v5.DBHandler.Companion.ID_COL
import andrew.organiser.my_head_v5.DBHandler.Companion.NAME_COL
import andrew.organiser.my_head_v5.DBHandler.Companion.TASK_TABLE
import andrew.organiser.my_head_v5.MainActivity
import andrew.organiser.my_head_v5.data_objects.TaskObject
import android.content.ContentValues
import android.content.Context
import android.widget.EditText
import android.widget.Toast
import java.time.LocalDate
import java.time.LocalTime


class D04TaskList {

    companion object {

        // --- Global variables used throughout the active lifecycle of the application --- //
        private var taskList: ArrayList<TaskObject> = ArrayList()
        private var taskListChanged = false

        // --- Task List CRUD Functionality --- //
        fun initialise(c: Context){
            if(taskList.isEmpty()  || taskListChanged){
                println("=== D04 - Initial read of all Tasks ===")
                taskList.clear()
                val rawTaskList = DBHandler(c).readDBTable(TASK_TABLE)
                if(rawTaskList.isNotEmpty()){
                    for(taskLine in rawTaskList){
                        try{
                            //println("Debug: Adding Task String Line: $taskLine") //Debug line
                            val taskParams = taskLine.split("\t")
                            taskList.add(
                                TaskObject(
                                    taskParams[0].toInt(),
                                    taskParams[1].toInt(),
                                    taskParams[2],
                                    taskParams[3],
                                    taskParams[4].toInt(),
                                    taskParams[5].toInt(),
                                    taskParams[6].substring(0, 8),
                                    taskParams[6].substring(9),
                                    taskParams[7].substring(0, 8),
                                    taskParams[7].substring(9),
                                    taskParams[8].toInt()==1,
                                    taskParams[9],
                                    taskParams[10].toInt()==1,
                                    taskParams[11],
                                    taskParams[12],
                                    if (taskParams[13].isNotEmpty()) taskParams[13].toInt() else null,
                                    if (taskParams[14].isNotEmpty()) taskParams[14].toInt()==1 else null,
                                    if (taskParams[15].isNotEmpty()) taskParams[15].toInt()==1 else null,
                                    taskParams[16],
                                    taskParams[17].toInt()==1,
                                    taskParams[18])
                            )
                        }catch(e: Exception){
                            println("~~~ Error: $e ~~~")
                        }
                    }
                }
                //Reset flag to not initialise until changed again
                taskListChanged = false
            }
        }

        fun read(filterKey: String, contextId: Int?): ArrayList<TaskObject>{
            val todayDateStr = LocalDate.now().format(MainActivity.DATE_FORMAT)
            when(filterKey){
                "Context_Incomplete" -> return taskList.filter {
                        task -> task.contextId == contextId && !task.completedFlag
                } as ArrayList<TaskObject>
                "Context_Active" -> return taskList.filter {
                    task -> task.contextId == contextId && (!task.completedFlag || task.completedDate == todayDateStr)
                } as ArrayList<TaskObject>
                "Context_Archive" -> return taskList.filter {
                        task -> task.contextId == contextId && task.completedFlag && task.completedDate != todayDateStr
                } as ArrayList<TaskObject>
                "Master_Active" -> return taskList.filter {
                     task -> D03ContextList.taskIsNotExcluded(task) && !task.completedFlag || task.completedDate == todayDateStr
                } as ArrayList<TaskObject>
                "Master_Archive" -> return taskList.filter {
                        task -> D03ContextList.taskIsNotExcluded(task) && task.completedFlag && task.completedDate != todayDateStr
                } as ArrayList<TaskObject>
                else -> return taskList
            }
        }

        fun getById(id:Int?) : TaskObject?{
            return try {
                taskList.first { it.id == id }
            } catch(error: NoSuchElementException) {
                null
            }
        }

        fun save(c: Context, newTask:TaskObject, originalName:String?) : Boolean{
            println("__Saving Task: ${newTask.name}__")
            println(newTask.getTaskModalAsString())
            val values = taskToValues(newTask)

            //Update if original name is given, otherwise create new item
            if(originalName != null){
                if(!DBHandler(c).updateEntry(TASK_TABLE, "$ID_COL=${newTask.id}", values)){
                    Toast.makeText(c, "Edit task failed:\nTask name must be unique", Toast.LENGTH_SHORT).show()
                } else { taskListChanged = true }
            }
            else{
                if(!DBHandler(c).newEntry(TASK_TABLE, values)){
                    Toast.makeText(c, "Create new task failed:\nTask name must be unique", Toast.LENGTH_SHORT).show()
                } else { taskListChanged = true }
            }
            return taskListChanged
        }

        fun updateConditionStatuses(c: Context, taskId: Int, activeStatus: Boolean, modStart: Boolean){
            val tasksWithCondition = taskList.filter { it.conditionIdRef == taskId }
            for(task in tasksWithCondition){
                task.conditionActiveFlag = !activeStatus
                val todayDate = LocalDate.now()

                //Modify start date if the conditional task is non-repeating and the change start flag is set to true
                if(modStart && task.changeStartFlag == true) task.startDate = todayDate.format(MainActivity.DATE_FORMAT)

                //Also change end date to today if end date was before today
                val endDate = LocalDate.parse(task.endDate, MainActivity.DATE_FORMAT)
                if(endDate.isBefore(todayDate)) task.endDate = todayDate.format(MainActivity.DATE_FORMAT)

                save(c, task, task.name)
            }
        }

        fun delete(c: Context, originalName: String){
            println("__Deleting Task: ${originalName}__")
            //Change condition status to zero if task is a condition
            val taskId = getIdFromName(originalName)
            val tasksWithCondition = taskList.filter { it.conditionIdRef == taskId }
            for(task in tasksWithCondition){
                task.conditionIdRef = null
                task.conditionActiveFlag = null
                save(c, task, task.name)
            }
            taskListChanged = DBHandler(c).deleteEntry(TASK_TABLE, "$NAME_COL=?", arrayOf(originalName))
            if(!taskListChanged) Toast.makeText(c, "Delete task failed", Toast.LENGTH_SHORT).show()
        }

        fun deleteWithContext(contextListChanged : Boolean){
            taskListChanged = contextListChanged
        }

        fun validateDate(startDateEdit: EditText, endDateEdit: EditText) : Boolean{
            //Compare start date to due date and validate
            try{
                val startDate = LocalDate.parse(startDateEdit.text, MainActivity.DATE_FORMAT)
                val endDate = LocalDate.parse(endDateEdit.text, MainActivity.DATE_FORMAT)
                println("Validate: Task startDate: $startDate to endDate: $endDate")

                return startDate.isBefore(endDate) || startDate.isEqual(endDate)

            }catch (e: Exception){
                println("___ Error: $e ___")
            }

            return false
        }

        fun validateTime(startTimeEdit: EditText, endTimeEdit: EditText, startDateEdit: EditText, endDateEdit: EditText) : Boolean{
            //Compare start date to due date and validate
            try{
                val startDate = LocalDate.parse(startDateEdit.text, MainActivity.DATE_FORMAT)
                val endDate = LocalDate.parse(endDateEdit.text, MainActivity.DATE_FORMAT)
                val startTime = LocalTime.parse(startTimeEdit.text, MainActivity.TIME_FORMAT)
                val endTime = LocalTime.parse(endTimeEdit.text, MainActivity.TIME_FORMAT)
                println("Validate: Task startDateTime: $startDate $startTime to endDateTime: $endDate $endTime")

                //Only need to compare if the days are the same
                return !startDate.isEqual(endDate) || (startDate.isEqual(endDate) && startTime.isBefore(endTime))

            }catch (e: Exception){
                println("___ Error: $e ___")
            }

            return false
        }

        fun getIdFromName(name:String): Int? {
            val taskListFilter = taskList.filter { it.name == name }
            return if(taskListFilter.isNotEmpty())
                taskListFilter[0].id
            else
                null
        }

        fun getCompleteFlag(id: Int): Boolean {
            val taskListFilter = taskList.filter { it.id == id }
            return if(taskListFilter.isNotEmpty())
                taskListFilter[0].completedFlag
            else
                false
        }

        fun getNameFromId(id:Int): String {
            val taskListFilter = taskList.filter { it.id == id }
            return if(taskListFilter.isNotEmpty())
                taskListFilter[0].name
            else
                "None"
        }

        fun getConditionList(contextId: Int?, taskObject: TaskObject?): MutableList<String>{
            //println("--- getConditionList ---")
            //Determine context id if null
            val tempContextId = if(contextId == null && taskObject != null) getTaskFromId(taskObject.id).contextId else contextId
            val tempTaskId = taskObject?.id ?: 0
            //println("Debug: ContextId = $contextId   taskObjectId = ${taskObject?.id}")
            //println("Debug: tempContextId = $tempContextId   tempTaskId = $tempTaskId")

            println("Process: Get condition list for context: ${D03ContextList.nameFromId(tempContextId!!)}") //Process Line
            val localConditionList = mutableListOf("None")
            val contextConditionList = taskList.filter { it.contextId == tempContextId && it.id != tempTaskId && (!it.completedFlag || it.completedDate == LocalDate.now().format(MainActivity.DATE_FORMAT)) }

            //Add conditions to list
            contextConditionList.forEach {
                localConditionList.add(it.name)
                //println("Debug: Added ${it.name} with taskId = ${it.id}")
            }

            return localConditionList
        }

        private fun getTaskFromId(id:Int): TaskObject { return taskList.first{it.id == id} }

        private fun taskToValues(taskObject: TaskObject):ContentValues{
            val values = ContentValues()
            values.put(DBHandler.CONTEXT_ID_COL, taskObject.contextId)
            values.put(NAME_COL, taskObject.name)
            values.put(DBHandler.MOTIVE_COL, taskObject.motive)
            values.put(DBHandler.COMPLEXITY_COL, taskObject.complexity)
            values.put(DBHandler.MOTIVATION_COL, taskObject.motivation)
            values.put(DBHandler.START_DATE_COL, taskObject.startDate + " " + taskObject.startTime)
            values.put(DBHandler.END_DATE_COL, taskObject.endDate + " " + taskObject.endTime)
            values.put(DBHandler.CHECKLIST_FLAG_COL, taskObject.checklistFlag)
            values.put(DBHandler.EARLIEST_END_DATE_COL, taskObject.earliestEndDate)
            values.put(DBHandler.REPEAT_FLAG_COL, taskObject.repeatFlag)
            values.put(DBHandler.REPEAT_CLAUSE_COL, taskObject.repeatClause)
            values.put(DBHandler.FREQUENCY_CLAUSE_COL, taskObject.frequencyClause)
            values.put(DBHandler.CONDITION_ID_COL, taskObject.conditionIdRef)
            values.put(DBHandler.CONDITION_CHANGE_START_FLAG_COL, taskObject.changeStartFlag)
            values.put(DBHandler.CONDITION_STATUS_FLAG_COL, taskObject.conditionActiveFlag)
            values.put(DBHandler.NOTES_COL, taskObject.notes)
            values.put(DBHandler.COMPLETED_FLAG_COL, taskObject.completedFlag)
            values.put(DBHandler.COMPLETED_DATE_COL, taskObject.completedDate)

            return values
        }
    }
}
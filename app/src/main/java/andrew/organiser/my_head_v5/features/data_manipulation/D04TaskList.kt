package andrew.organiser.my_head_v5.features.data_manipulation


import andrew.organiser.my_head_v5.DBHandler
import andrew.organiser.my_head_v5.DBHandler.Companion.TASK_CHECKLIST_COL
import andrew.organiser.my_head_v5.DBHandler.Companion.TASK_CHECKLIST_DATE_COL
import andrew.organiser.my_head_v5.DBHandler.Companion.TASK_COMPLETED_DATE_COL
import andrew.organiser.my_head_v5.DBHandler.Companion.TASK_COMPLETED_FLAG_COL
import andrew.organiser.my_head_v5.DBHandler.Companion.TASK_COMPLEXITY_COL
import andrew.organiser.my_head_v5.DBHandler.Companion.TASK_CONDITION_COL
import andrew.organiser.my_head_v5.DBHandler.Companion.TASK_CONDITION_STATUS_COL
import andrew.organiser.my_head_v5.DBHandler.Companion.TASK_CONTEXT_ID_COL
import andrew.organiser.my_head_v5.DBHandler.Companion.TASK_DUE_DATE_COL
import andrew.organiser.my_head_v5.DBHandler.Companion.TASK_FREQUENCY_COL
import andrew.organiser.my_head_v5.DBHandler.Companion.TASK_ID_COL
import andrew.organiser.my_head_v5.DBHandler.Companion.TASK_MOTIVATION_COL
import andrew.organiser.my_head_v5.DBHandler.Companion.TASK_MOTIVE_COL
import andrew.organiser.my_head_v5.DBHandler.Companion.TASK_NAME_COL
import andrew.organiser.my_head_v5.DBHandler.Companion.TASK_NOTES_COL
import andrew.organiser.my_head_v5.DBHandler.Companion.TASK_REPEAT_CLAUSE_COL
import andrew.organiser.my_head_v5.DBHandler.Companion.TASK_REPEAT_CLAUSE_VALUE_COL
import andrew.organiser.my_head_v5.DBHandler.Companion.TASK_REPEAT_COL
import andrew.organiser.my_head_v5.DBHandler.Companion.TASK_START_DATE_COL
import andrew.organiser.my_head_v5.DBHandler.Companion.TASK_TABLE_NAME
import andrew.organiser.my_head_v5.MainActivity
import andrew.organiser.my_head_v5.data_objects.TaskObject
import android.content.ContentValues
import android.content.Context
import android.widget.EditText
import android.widget.Toast
import java.time.LocalDate


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
                val rawTaskList = DBHandler(c).readDBTable(TASK_TABLE_NAME)
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
                                    taskParams[13],
                                    if (taskParams[14].isNotEmpty()) taskParams[14].toInt() else null,
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
            return if(id != null)
                taskList.first{it.id == id}
            else null
        }

        fun save(c: Context, newTask:TaskObject, originalName:String?) : Boolean{
            println("__Saving Task: ${newTask.name}__")
            println(newTask.getTaskModalAsString())
            val values = taskToValues(newTask)

            //Update if original name is given, otherwise create new item
            if(originalName != null){
                if(!DBHandler(c).updateEntry(TASK_TABLE_NAME, "$TASK_ID_COL=${newTask.id}", values)){
                    Toast.makeText(c, "Edit task failed:\nTask name must be unique", Toast.LENGTH_SHORT).show()
                } else { taskListChanged = true }
            }
            else{
                if(!DBHandler(c).newEntry(TASK_TABLE_NAME, values)){
                    Toast.makeText(c, "Create new task failed:\nTask name must be unique", Toast.LENGTH_SHORT).show()
                } else { taskListChanged = true }
            }
            return taskListChanged
        }

        fun updateConditionStatuses(c: Context, taskId: Int, activeStatus: Boolean, modStart: Boolean){
            val tasksWithCondition = taskList.filter { it.conditionId == taskId }
            for(task in tasksWithCondition){
                task.conditionStatus = !activeStatus

                //Also modify start date if the conditional task is non-repeating
                if(modStart) task.startDate = LocalDate.now().format(MainActivity.DATE_FORMAT)

                save(c, task, task.name)
            }
        }

        fun delete(c: Context, originalName: String){
            println("__Deleting Task: ${originalName}__")
            //Change condition status to zero if task is a condition
            val taskId = getIdFromName(originalName)
            val tasksWithCondition = taskList.filter { it.conditionId == taskId }
            for(task in tasksWithCondition){
                task.conditionId = null
                task.conditionStatus = null
                save(c, task, task.name)
            }
            taskListChanged = DBHandler(c).deleteEntry(TASK_TABLE_NAME, "$TASK_NAME_COL=?", arrayOf(originalName))
            if(!taskListChanged) Toast.makeText(c, "Delete task failed", Toast.LENGTH_SHORT).show()
        }

        fun deleteWithContext(contextListChanged : Boolean){
            taskListChanged = contextListChanged
        }

        fun validateDate(startDateEdit: EditText, dueDateEdit: EditText) : Boolean{
            //Compare start date to due date and validate
            try{
                val startDate = LocalDate.parse(startDateEdit.text, MainActivity.DATE_FORMAT)
                val dueDate = LocalDate.parse(dueDateEdit.text, MainActivity.DATE_FORMAT)
                println("Validate: Task startDate: $startDate to dueDate: $dueDate")

                return startDate.isBefore(dueDate) || startDate.isEqual(dueDate)

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
            println("--- getConditionList ---")
            //Determine context id if null
            val tempContextId = if(contextId == null && taskObject != null) getTaskFromId(taskObject.id).contextId else contextId
            val tempTaskId = taskObject?.id ?: 0
            println("Debug: ContextId = $contextId   taskObjectId = ${taskObject?.id}")
            println("Debug: tempContextId = $tempContextId   tempTaskId = $tempTaskId")

            println("Process: Get condition list for context: ${D03ContextList.nameFromId(tempContextId!!)}") //Process Line
            val localConditionList = mutableListOf("None")
            val contextConditionList = taskList.filter { it.contextId == tempContextId && it.id != tempTaskId && (!it.completedFlag || it.completedDate == LocalDate.now().format(MainActivity.DATE_FORMAT)) }

            //Add conditions to list
            contextConditionList.forEach {
                localConditionList.add(it.name)
                println("Debug: Added ${it.name} with taskId = ${it.id}")
            }

            return localConditionList
        }

        private fun getTaskFromId(id:Int): TaskObject { return taskList.first{it.id == id} }

        private fun taskToValues(taskObject: TaskObject):ContentValues{
            val values = ContentValues()
            values.put(TASK_CONTEXT_ID_COL, taskObject.contextId)
            values.put(TASK_NAME_COL, taskObject.name)
            values.put(TASK_MOTIVE_COL, taskObject.motive)
            values.put(TASK_COMPLEXITY_COL, taskObject.complexity)
            values.put(TASK_MOTIVATION_COL, taskObject.motivation)
            values.put(TASK_START_DATE_COL, taskObject.startDate + " " + taskObject.startTime)
            values.put(TASK_DUE_DATE_COL, taskObject.dueDate + " " + taskObject.dueTime)
            values.put(TASK_CHECKLIST_COL, taskObject.checklist)
            values.put(TASK_CHECKLIST_DATE_COL, taskObject.checklistDate)
            values.put(TASK_REPEAT_COL, taskObject.repeat)
            values.put(TASK_REPEAT_CLAUSE_COL, taskObject.repeatClause)
            values.put(TASK_REPEAT_CLAUSE_VALUE_COL, taskObject.repeatClauseValue)
            values.put(TASK_FREQUENCY_COL, taskObject.frequency)
            values.put(TASK_CONDITION_COL, taskObject.conditionId)
            values.put(TASK_CONDITION_STATUS_COL, taskObject.conditionStatus)
            values.put(TASK_NOTES_COL, taskObject.notes)
            values.put(TASK_COMPLETED_FLAG_COL, taskObject.completedFlag)
            values.put(TASK_COMPLETED_DATE_COL, taskObject.completedDate)

            return values
        }
    }
}
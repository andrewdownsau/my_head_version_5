package andrew.organiser.myhead_v3.features.crud


import andrew.organiser.myhead_v3.DBHandler
import andrew.organiser.myhead_v3.DBHandler.Companion.CONTEXT_NAME_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.CONTEXT_TABLE_NAME
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_CHECKLIST_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_CHECKLIST_DATE_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_COMPLETED_DATE_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_COMPLETED_FLAG_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_COMPLEXITY_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_CONDITION_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_CONTEXT_ID_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_DUE_DATE_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_FREQUENCY_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_ID_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_MOTIVATION_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_MOTIVE_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_NAME_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_NOTES_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_REPEAT_CLAUSE_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_REPEAT_CLAUSE_VALUE_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_REPEAT_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_START_DATE_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_TABLE_NAME
import andrew.organiser.myhead_v3.MainActivity
import andrew.organiser.myhead_v3.modals.TaskModal
import android.content.ContentValues
import android.content.Context
import android.widget.Toast
import java.time.LocalDate


class TaskListCURD {

    companion object {

        private var latestTaskId = 0

        // --- Task List CRUD Functionality --- //
        fun main(c: Context, cmd: String, originalTaskName: String?, newTask: TaskModal?, filter:String?) :
                Pair<Boolean, ArrayList<TaskModal>?>{
            println("=== C02 - Task List CRUD: $cmd ===")
            when(cmd){
                "Create" -> return Pair(createTask(c, newTask), null)
                "Read" -> return readTaskList(c, filter)
                "Update" -> return Pair(updateTask(c, originalTaskName, newTask), null)
                "Delete" -> return Pair(deleteTask(c, originalTaskName), null)
            }

            return Pair(false, null)
        }

        fun getLatestTaskId(c: Context): Int{
            val readTaskTable = DBHandler(c).readDBTable(TASK_TABLE_NAME)
            if(readTaskTable.first && readTaskTable.second.isNotEmpty()){
                return readTaskTable.second.last().split("\t")[0].toInt()
            }
            return 0
        }

        private fun createTask(c: Context, newTask: TaskModal?) : Boolean{
            if (newTask != null) {
                println("__Create Task: ${newTask.getTaskName()}__")
                val values = taskToValues(newTask)

                if(!DBHandler(c).createNewDBEntry(TASK_TABLE_NAME, values)){
                    Toast.makeText(c, "Create new task failed:\nTask name must be unique", Toast.LENGTH_LONG).show()
                } else { return true }
            }
            return false
        }

        private fun readTaskList(c: Context, filter: String?): Pair<Boolean, ArrayList<TaskModal>>{
            println("__Read Task List with filter: ${filter}__")
            val readTablePair = DBHandler(c).readDBTable(TASK_TABLE_NAME)
            var returnFlag = readTablePair.first
            val taskListStr = filterTaskRead(readTablePair.second, filter)

            val taskModalArrayList: ArrayList<TaskModal> = ArrayList()
            for(taskStr in taskListStr){
                //println("__Task String Line: ${taskStr}__")
                try{
                    val taskPropertiesStrList = taskStr.split("\t")
                    taskModalArrayList.add(
                        TaskModal(
                            taskPropertiesStrList[0].toInt(),
                            taskPropertiesStrList[1].toInt(),
                            taskPropertiesStrList[2],
                            taskPropertiesStrList[3],
                            taskPropertiesStrList[4].toInt(),
                            taskPropertiesStrList[5].toInt(),
                            taskPropertiesStrList[6],
                            taskPropertiesStrList[7],
                            taskPropertiesStrList[8].toInt()==1,
                            taskPropertiesStrList[9],
                            taskPropertiesStrList[10].toInt()==1,
                            taskPropertiesStrList[11],
                            taskPropertiesStrList[12],
                            taskPropertiesStrList[13],
                            if (taskPropertiesStrList[14].isNotEmpty()) taskPropertiesStrList[14].toInt() else 0,
                            taskPropertiesStrList[15],
                            taskPropertiesStrList[16].toInt()==1,
                            taskPropertiesStrList[17])
                    )
                }catch(e: Exception){
                    println("~~~ Error: $e ~~~")
                    returnFlag = false
                }
            }
            return Pair(returnFlag, taskModalArrayList)
        }

        private fun updateTask(c: Context, originalTaskName: String?, newTask: TaskModal?) : Boolean{
            if(originalTaskName == null || newTask == null){ println("~~~ Error: Update Task Modal Null ~~~")}
            else{
                println("__Update Context: $originalTaskName to ${newTask.getTaskName()}__")
                var returnFlag = false
                val newValues = taskToValues(newTask)
                if(!DBHandler(c).updateDBEntry(TASK_TABLE_NAME, "$TASK_NAME_COL='${originalTaskName}'", newValues)){
                    Toast.makeText(c, "Edit task failed:\nTask name must be unique", Toast.LENGTH_SHORT).show()
                } else { returnFlag = true }

                return returnFlag
            }
            return false
        }

        private fun deleteTask(c: Context, originalTaskName: String?) : Boolean {
            if(originalTaskName == null){ println("~~~ Error: Delete Task Modal Null ~~~")}
            else{
                val taskId = readTaskList(c, null).second.filter { it.getTaskName() == originalTaskName }[0].id
                println("__Delete Task: $taskId ${originalTaskName}__")
                return DBHandler(c).deleteDBEntry(TASK_TABLE_NAME, "$TASK_ID_COL=$taskId")
            }
            return false
        }

        private fun filterTaskRead(taskReadList: ArrayList<String>, filter: String?) : ArrayList<String>{
            if(taskReadList.isNotEmpty()){

                val filteredTaskRead: ArrayList<String> = ArrayList()
                for(taskStr in taskReadList){
                    val taskPropertiesStrList = taskStr.split("\t")
                    //println("Filtered read task [${taskPropertiesStrList.size}]: $taskStr")
                    val taskContextId = taskPropertiesStrList[1]
                    val taskCompletedStatus = taskPropertiesStrList[16].toInt()==1
                    val taskCompletedDate = taskPropertiesStrList[17]
                    //If archive, isolate contextId and completed tasks past today
                    if(filter != null && filter.contains("Archive")){
                        val filterContextId = filter.split("_")[0]
                        if((taskContextId == filterContextId || filterContextId == "null") && taskCompletedStatus && taskCompletedDate != LocalDate.now().format(MainActivity.DATE_FORMAT))
                            filteredTaskRead.add(taskStr)
                    }
                    //Else just filter by context id and incomplete
                    else if((taskContextId == filter || filter == "Incomplete") && (!taskCompletedStatus || taskCompletedDate == LocalDate.now().format(MainActivity.DATE_FORMAT))){
                        filteredTaskRead.add(taskStr)
                    }
                    else if(filter == null){
                        filteredTaskRead.add(taskStr)
                    }
                }
                return filteredTaskRead

            }
            return taskReadList
        }

        fun taskToValues(taskObject: TaskModal):ContentValues{
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
    }
}
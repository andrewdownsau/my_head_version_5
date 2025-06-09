package andrew.organiser.my_head_v5.features.data_manipulation


import andrew.organiser.my_head_v5.DBHandler
import andrew.organiser.my_head_v5.DBHandler.Companion.SUBTASK_COMPLETED_COL
import andrew.organiser.my_head_v5.DBHandler.Companion.SUBTASK_DUE_DATE_COL
import andrew.organiser.my_head_v5.DBHandler.Companion.SUBTASK_NAME_COL
import andrew.organiser.my_head_v5.DBHandler.Companion.SUBTASK_TABLE_NAME
import andrew.organiser.my_head_v5.DBHandler.Companion.SUBTASK_TASK_ID_COL
import andrew.organiser.my_head_v5.data_objects.SubtaskObject
import android.content.ContentValues
import android.content.Context
import android.widget.Toast


class D05SubTaskList {

    companion object {

        // --- Global variables used throughout the active lifecycle of the application --- //
        private var subTaskList: ArrayList<SubtaskObject> = ArrayList()
        private var subTaskListChanged = false

        // --- Sub Task List CRUD Functionality --- //
        fun initialise(c: Context){
            if(subTaskList.isEmpty()  || subTaskListChanged){
                println("=== D04 - Initial read of all Sub Tasks ===")
                subTaskList.clear()
                val rawTaskList = DBHandler(c).readDBTable(SUBTASK_TABLE_NAME)
                if(rawTaskList.isNotEmpty()){
                    for(subTaskLine in rawTaskList){
                        try{
                            //println("Debug: Adding Task String Line: $taskLine") //Debug line
                            val subTaskParams = subTaskLine.split("\t")
                            subTaskList.add(
                                SubtaskObject(
                                    subTaskParams[0].toInt(),
                                    subTaskParams[1].toInt(),
                                    subTaskParams[2],
                                    subTaskParams[3],
                                    subTaskParams[4].toInt()==1)
                            )
                        }catch(e: Exception){
                            println("~~~ Error: $e ~~~")
                        }
                    }
                }
                //Reset flag to not initialise until changed again
                subTaskListChanged = false
            }
        }

        fun read(taskId: Int): ArrayList<SubtaskObject>{
            return subTaskList.filter { it.taskId == taskId } as ArrayList<SubtaskObject>
        }


        fun save(c: Context, newSubTaskList: ArrayList<SubtaskObject>, taskId: Int) : Boolean{
            println("__Saving Subtask List __")
            //Retrieve the task Id if this is a newly created task
            var tempTaskId = taskId
            if(tempTaskId == 0) tempTaskId = getLatestTaskId(c)
            //Purge all sub tasks with correct task id if required
            else {
                subTaskListChanged = true
                DBHandler(c).deleteEntry(SUBTASK_TABLE_NAME, "$SUBTASK_TASK_ID_COL=$tempTaskId", arrayOf())
            }

            println("__Update Subtask List for task [$taskId]__")
            //Add all newly created subtasks
            newSubTaskList.forEach { subtaskModal ->
                val values = subTaskToValues(subtaskModal, tempTaskId)
                if(!DBHandler(c).newEntry(SUBTASK_TABLE_NAME, values)) {
                    Toast.makeText(c, "Create new subtask failed:\nSubtask must be unique", Toast.LENGTH_SHORT).show()
                }
                else{ subTaskListChanged = true }
            }

            return subTaskListChanged
        }

        private fun getLatestTaskId(c: Context) : Int{
            try{
                D04TaskList.initialise(c)
                return D04TaskList.read("None", null).last().id
            }catch (e:Exception){
                println("~~~Error: $e ~~~")
            }
            return 0
        }

        private fun subTaskToValues(subTaskObject: SubtaskObject, taskId: Int):ContentValues{
            println("Process: Converting subtask: ${subTaskObject.getSubTaskModalAsString()}") //Process line
            val values = ContentValues()
            values.put(SUBTASK_TASK_ID_COL, taskId)
            values.put(SUBTASK_NAME_COL, subTaskObject.name)
            values.put(SUBTASK_DUE_DATE_COL, subTaskObject.dueDate)
            values.put(SUBTASK_COMPLETED_COL, subTaskObject.completedFlag)

            return values
        }
    }
}
package andrew.organiser.my_head_v5.features.data_manipulation


import andrew.organiser.my_head_v5.DBHandler
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
                val rawSubTaskList = DBHandler(c).readDBTable(DBHandler.SUBTASK_TABLE)
                if(rawSubTaskList.isNotEmpty()){
                    for(subTaskLine in rawSubTaskList){
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


        fun save(c: Context, newSubTaskList: ArrayList<SubtaskObject>, taskId: Int, purge: Boolean) : Boolean{
            println("__Saving Subtask List __")
            //Retrieve the task Id if this is a newly created task
            var tempTaskId = taskId
            if(tempTaskId == 0) tempTaskId = getLatestTaskId(c)

            //Check to see if subtask list is any different from the database and any changes have been made
            if(purge){
                val tempOriginalSubTaskList: ArrayList<SubtaskObject> = ArrayList()
                for(tempSubtask in subTaskList.filter { it.taskId == taskId }){
                    tempSubtask.id = 0
                    tempOriginalSubTaskList.add(tempSubtask)
                }
                for(newSubtask in newSubTaskList.withIndex()){
                    val newSubTaskStr = newSubtask.value.getSubTaskModalAsString()
                    val origSubTaskStr = tempOriginalSubTaskList[newSubtask.index].getSubTaskModalAsString()
                    //println("___ New Subtask: $newSubTaskStr")
                    //println("___ Original Subtask: $origSubTaskStr")
                    if(newSubTaskStr != origSubTaskStr) {
                        subTaskListChanged = true
                        break
                    }
                }
                if(subTaskListChanged){
                    DBHandler(c).deleteEntry(DBHandler.SUBTASK_TABLE, "${DBHandler.TASK_ID_COL}=$tempTaskId", arrayOf())

                    println("__Update Subtask List for task [$taskId]__")
                    //Add all newly created subtasks
                    newSubTaskList.forEach { subtaskModal ->
                        val values = subTaskToValues(subtaskModal, tempTaskId)
                        if(!DBHandler(c).newEntry(DBHandler.SUBTASK_TABLE, values)) {
                            Toast.makeText(c, "Create new subtask failed:\nSubtask must be unique", Toast.LENGTH_SHORT).show()
                        }
                        else{ subTaskListChanged = true }
                    }
                }
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
            values.put(DBHandler.TASK_ID_COL, taskId)
            values.put(DBHandler.NAME_COL, subTaskObject.name)
            values.put(DBHandler.SUB_END_DATE_COL, subTaskObject.endDate)
            values.put(DBHandler.COMPLETED_FLAG_COL, subTaskObject.completedFlag)

            return values
        }
    }
}
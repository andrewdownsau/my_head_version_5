package andrew.organiser.myhead_v3.features.crud


import andrew.organiser.myhead_v3.DBHandler
import andrew.organiser.myhead_v3.DBHandler.Companion.SUBTASK_COMPLETED_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.SUBTASK_DUE_DATE_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.SUBTASK_NAME_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.SUBTASK_TABLE_NAME
import andrew.organiser.myhead_v3.DBHandler.Companion.SUBTASK_TASK_ID_COL
import andrew.organiser.myhead_v3.modals.SubtaskModal
import android.content.ContentValues
import android.content.Context
import android.widget.Toast


class SubTaskListCURD {

    companion object {

        // --- Sub Task List CRUD Functionality --- //
        fun main(c: Context, cmd: String, taskId: Int, newSubTaskList: ArrayList<SubtaskModal>?) :
                Pair<Boolean, ArrayList<SubtaskModal>?>{
            println("=== C03 - Sub Task List CRUD: $cmd ===")
            when(cmd){
                "Update_List" -> return Pair(updateSubtaskList(c, taskId, newSubTaskList), null)
                "Read" -> return readSubtaskList(c, taskId.toString())
            }

            return Pair(false, null)
        }

        private fun readSubtaskList(c: Context, taskId: String?): Pair<Boolean, ArrayList<SubtaskModal>>{
            println("__Read SubTask List with taskId: ${taskId}__")
            val readTablePair = DBHandler(c).readDBTable(SUBTASK_TABLE_NAME)
            var returnFlag = readTablePair.first
            val subtaskListStr = filterSubtaskRead(readTablePair.second, taskId)

            val subtaskModalArrayList: ArrayList<SubtaskModal> = ArrayList()
            for(subtaskStr in subtaskListStr){
                println("__SubTask String Line: ${subtaskStr}__")
                try{
                    val subtaskPropertiesStrList = subtaskStr.split("\t")
                    subtaskModalArrayList.add(
                        SubtaskModal(
                            subtaskPropertiesStrList[0].toInt(),
                            subtaskPropertiesStrList[1].toInt(),
                            subtaskPropertiesStrList[2],
                            subtaskPropertiesStrList[3],
                            subtaskPropertiesStrList[4].toInt()==1)
                    )
                }catch(e: Exception){
                    println("~~~ Error: $e ~~~")
                    returnFlag = false
                }
            }
            return Pair(returnFlag, subtaskModalArrayList)
        }

        private fun updateSubtaskList(c: Context, taskId: Int?, newSubTaskList: ArrayList<SubtaskModal>?) : Boolean{
            var tempTaskId = taskId
            if(newSubTaskList == null || tempTaskId == null){ println("~~~ Error: Update Subtask Modal Null ~~~")}
            else{
                //Purge all sub tasks with correct task id if required
                if(tempTaskId == 0) tempTaskId = TaskListCURD.getLatestTaskId(c)
                else DBHandler(c).deleteDBEntry(SUBTASK_TABLE_NAME, "$SUBTASK_TASK_ID_COL=$tempTaskId")

                println("__Update Subtask List for task [$taskId]__")
                //Add all newly created subtasks
                newSubTaskList.forEach { subtaskModal ->
                    val values = subtaskToValues(subtaskModal, tempTaskId)
                    if(!DBHandler(c).createNewDBEntry(SUBTASK_TABLE_NAME, values)){
                        Toast.makeText(c, "Create new subtask failed:\nSubtask must be unique", Toast.LENGTH_SHORT).show()
                        return false
                    }
                }
            }
            return true
        }


        private fun filterSubtaskRead(subtaskReadList: ArrayList<String>, taskId: String?) : ArrayList<String>{
            if(subtaskReadList.isNotEmpty()){

                val filteredSubtaskRead: ArrayList<String> = ArrayList()
                for(subtaskStr in subtaskReadList){
                    val subtaskPropertiesStrList = subtaskStr.split("\t")
                    val subtaskTaskId = subtaskPropertiesStrList[1]

                    //If filter matches, add to filtered read
                    if(taskId == null || subtaskTaskId == taskId){
                        filteredSubtaskRead.add(subtaskStr)
                    }
                }
                return filteredSubtaskRead

            }
            return subtaskReadList
        }

        fun subtaskToValues(subtaskObject: SubtaskModal, taskId: Int):ContentValues{
            println("__Converting subtask: ${subtaskObject.getSubtaskName()} ${subtaskObject.getSubtaskDueDate()} ${subtaskObject.getSubtaskCompletedFlag()} to values__")
            val values = ContentValues()
            values.put(SUBTASK_TASK_ID_COL, taskId)
            values.put(SUBTASK_NAME_COL, subtaskObject.getSubtaskName())
            values.put(SUBTASK_DUE_DATE_COL, subtaskObject.getSubtaskDueDate())
            values.put(SUBTASK_COMPLETED_COL, subtaskObject.getSubtaskCompletedFlag())

            return values
        }
    }
}
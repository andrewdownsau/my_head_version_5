package andrew.organiser.myhead_v3.features.ui_generation


import andrew.organiser.myhead_v3.MainActivity
import andrew.organiser.myhead_v3.features.crud.SubTaskListCURD
import andrew.organiser.myhead_v3.features.crud.TaskListCURD
import andrew.organiser.myhead_v3.features.settings.TaskFeatures
import andrew.organiser.myhead_v3.modals.TaskModal
import android.content.Context
import java.time.LocalDate


class RepeatRefresh {

    companion object {

        // --- Repeat refresh functionality --- //
        fun repeatRefreshCheck(c:Context){
            if(TaskFeatures.getTaskFeatureState(c, "Repeating")){
                println("=== Gen12 - Repeat refresh functionality ===")
                //Check if there are any tasks that were completed before today that have a repeat clause
                val todayDateStr = LocalDate.now().format(MainActivity.DATE_FORMAT)
                val todayDate = LocalDate.parse(todayDateStr, MainActivity.DATE_FORMAT)
                val readTaskList = TaskListCURD.main(c, "Read", null, null, null)
                if(readTaskList.first && readTaskList.second != null){
                    for(task in readTaskList.second!!){
                        //Isolate tasks completed before today that have a repeat clause with a valid frequency
                        if(task.getTaskRepeat() && task.getTaskCompletedFlag() && task.getTaskCompletedDate()!= todayDateStr && task.getTaskFrequency() != "none"){
                            //Further isolate tasks where repeat after clause is not 0
                            if(task.getTaskRepeatClauseValue() != "0"){
                                //Determine if until clause has been met or not
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

            //Update the task in the database
            TaskListCURD.main(c, "Update", task.getTaskName(), task, null)

            //Change any dates in checklist to update when repeat is triggered
            val subtaskRead = SubTaskListCURD.main(c, "Read", task.id, null)
            if(subtaskRead.first && !subtaskRead.second.isNullOrEmpty()){
                val subtaskList = subtaskRead.second!!
                for(subtask in subtaskList){
                    var subtaskDate = LocalDate.parse(subtask.getSubtaskDueDate(), MainActivity.DATE_FORMAT)
                    var tempNumberOfShifts = numberOfShifts
                    while(tempNumberOfShifts > 0) {
                        subtaskDate = addTimeToDate(task.getTaskFrequency(), subtaskDate.format(MainActivity.DATE_FORMAT))
                        tempNumberOfShifts--
                    }
                    subtask.setSubtaskDueDate(subtaskDate.format(MainActivity.DATE_FORMAT))
                    subtask.setSubtaskCompletedFlag(false)
                }
                SubTaskListCURD.main(c, "Update_List", task.id, subtaskList)
            }
        }

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
    }
}
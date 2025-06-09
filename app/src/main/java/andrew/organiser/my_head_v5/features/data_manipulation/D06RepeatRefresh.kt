package andrew.organiser.my_head_v5.features.data_manipulation

import andrew.organiser.my_head_v5.MainActivity
import andrew.organiser.my_head_v5.data_objects.TaskObject
import android.content.Context
import java.time.LocalDate


class D06RepeatRefresh {

    companion object {

        private var repeatRequired = false

        // --- Repeat refresh functionality --- //
        fun check(c:Context){
            println("=== D06 - Repeat Refresh Check ===")

            //Check if there are any tasks that were completed before today that have a repeat clause
            val todayDate = LocalDate.now()
            val todayDateStr = todayDate.format(MainActivity.DATE_FORMAT)
            val taskList = D04TaskList.read("Master_Archive", null)
            for(task in taskList){
                //println("Debug: ${task.getTaskModalAsString()}")
                //Isolate tasks completed before today that have a repeat clause with a valid frequency
                if(task.repeat && task.completedFlag && task.completedDate != todayDateStr && task.frequency != "None"){
                    //Further isolate tasks where repeat after clause is not 0
                    if(task.repeatClause.contains("Forever") || task.repeatClauseValue != "0"){
                        //Determine if until clause has been met or not
                        if(task.repeatClause.contains("Until")) {
                            try{
                                val untilDate = LocalDate.parse(task.repeatClauseValue, MainActivity.DATE_FORMAT)
                                if(todayDate.isBefore(untilDate)){
                                    refreshTasks(c, task, todayDate)
                                }

                            }catch(e: Exception){println("___Error: $e")}
                        }
                        else{
                            refreshTasks(c, task, todayDate)
                        }
                    }
                }
            }
            repeatRequired = false
        }

        fun flagChange(cmd: String, change: Boolean): Boolean{
            when(cmd){
                "read" -> return repeatRequired
                else -> repeatRequired = change
            }
            return false
        }

        private fun refreshTasks(c: Context, task: TaskObject, todayDate:LocalDate){
            println("Process: Refreshing task - ${task.name}") //Process line
            //Clear the completed flag for the task
            task.completedFlag = false
            task.completedDate = ""

            //Add appropriate time amount based on repeat frequency to start and due date
            if(task.frequency != null){
                var startDate = LocalDate.parse(task.startDate, MainActivity.DATE_FORMAT)
                var dueDate = LocalDate.parse(task.dueDate, MainActivity.DATE_FORMAT)
                var numberOfShifts = 0

                while(dueDate.isBefore(todayDate) || numberOfShifts == 0){
                    startDate = addTimeToDate(task.frequency!!, startDate.format(MainActivity.DATE_FORMAT))
                    dueDate = addTimeToDate(task.frequency!!, dueDate.format(MainActivity.DATE_FORMAT))
                    numberOfShifts++
                }


                //Change any dates in checklist to update when repeat is triggered
                val subtaskList = D05SubTaskList.read(task.id)
                var earliestSubtaskDate = dueDate
                for(subtask in subtaskList){
                    if(subtask.dueDate.isNotEmpty()){
                        var subtaskDate = LocalDate.parse(subtask.dueDate, MainActivity.DATE_FORMAT)
                        var tempNumberOfShifts = numberOfShifts
                        while(tempNumberOfShifts > 0) {
                            subtaskDate = addTimeToDate(task.frequency!!, subtaskDate.format(MainActivity.DATE_FORMAT))
                            tempNumberOfShifts--
                        }
                        subtask.dueDate = subtaskDate.format(MainActivity.DATE_FORMAT)
                        if(subtaskDate < earliestSubtaskDate) earliestSubtaskDate = subtaskDate
                    }
                    subtask.completedFlag = false
                }

                //Set new dates for task and then update in table
                task.startDate = startDate.format(MainActivity.DATE_FORMAT)
                task.dueDate = dueDate.format(MainActivity.DATE_FORMAT)
                task.checklistDate = earliestSubtaskDate.format(MainActivity.DATE_FORMAT)

                //Change remaining times to repeat the task if set to after times clause
                try{
                    if(task.repeatClause == "After")
                        task.repeatClauseValue = (task.repeatClauseValue.toInt() - 1).toString()
                }catch(_:Exception){}

                //Update the task in the database
                if(D04TaskList.save(c, task, task.name)){
                    D04TaskList.updateConditionStatuses(c, task.id, task.completedFlag, false)
                    D05SubTaskList.save(c, subtaskList, task.id)
                }
            }
        }

        private fun addTimeToDate(frequencyStr:String, originalDateStr: String): LocalDate{
            //println("SubProcess: Add Time to Date: $originalDateStr") //Subprocess line
            //Change start and due date by the required time jumps
            var date = LocalDate.parse(originalDateStr, MainActivity.DATE_FORMAT)

            if(frequencyStr.contains("_")){
                val numberShift = frequencyStr.split("_")[0].toInt()
                val frequencyType = frequencyStr.split("_")[1]

                //Debugging log prompts
                //if(dayMovement > 0) println("Debug: add $dayMovement days") //Debug line
                //if(monthMovement > 0) println("Debug: add $monthMovement months") //Debug line
                //println("Debug: new date: ${date.format(MainActivity.DATE_FORMAT)}") //Debug line

                date = when(frequencyType){
                    "Days" -> date.plusDays(numberShift.toLong())
                    "Weeks" -> date.plusWeeks(numberShift.toLong())
                    "Months" -> date.plusMonths(numberShift.toLong())
                    "Years" -> date.plusYears(numberShift.toLong())
                    else -> date
                }
            }

            else{
                //Determine the number days till the next applicable day
                val originalDay = date.dayOfWeek.value
                //println("Debug: originalDay: $originalDay")
                val dayStrList = listOf("_", "M", "Tu", "W", "Th", "F", "Sa", "Su")
                var thisWeek = false
                for((index, day) in dayStrList.withIndex()){
                    if(frequencyStr.contains(day) && index > originalDay){
                        val daysBetween = index - originalDay
                        date = date.plusDays(daysBetween.toLong())
                        thisWeek = true
                        break
                    }
                }
                //Accounting for the next day being earlier in the following week
                if(!thisWeek){
                    for((index, day) in dayStrList.withIndex()){
                        //println("Debug: (index, day): ($index, $day)")
                        if(frequencyStr.contains(day)){
                            val daysToAdd = index + 7 - originalDay
                            date = date.plusDays(daysToAdd.toLong())
                            break
                        }
                    }
                }
            }

            return date
        }
    }
}
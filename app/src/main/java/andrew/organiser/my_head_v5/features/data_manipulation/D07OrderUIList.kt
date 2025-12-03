package andrew.organiser.my_head_v5.features.data_manipulation


import andrew.organiser.my_head_v5.MainActivity
import andrew.organiser.my_head_v5.data_objects.ContextObject
import andrew.organiser.my_head_v5.data_objects.TaskObject
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class D07OrderUIList {

    companion object {

        // --- Global variables used throughout the active lifecycle of the application --- //
        private var contextTopTaskList:  ArrayList<TaskObject> = ArrayList()

        // --- Sort UI Button List --- //
        fun sortContextList(contextList: ArrayList<ContextObject>) : ArrayList<ContextObject>{
            println("=== D07 - Sort Context List ===")
            //Sort the top task list based on same rules as tasks
            contextTopTaskList = getContextTopmostTasks(contextList)
            //println("Process: Sort topmost tasks in list for contexts") //Process line
            val sortedTopTaskList = sortTaskList(contextTopTaskList)
            if(sortedTopTaskList.isNotEmpty()){
                val sortedContextList: ArrayList<ContextObject> = ArrayList()
                for(topTask in sortedTopTaskList){
                    for(context in contextList){
                        if(topTask.contextId == context.id) {
                            //println("Debug: TOP TASK: ${topTask.name} matched to context ${context.name}") //Debug Line
                            sortedContextList.add(context)
                        }
                    }
                }
                val emptyContextList = (contextList - sortedContextList.toSet()) as ArrayList<ContextObject>

                return (sortedContextList + emptyContextList) as ArrayList<ContextObject>
            }
            return  contextList
        }

        fun sortTaskList(taskList: ArrayList<TaskObject>) : ArrayList<TaskObject>{
            if(taskList.isNotEmpty()){
                //Determine whether in archived list or active based on topmost task in list
                if(!taskList.first().completedFlag || taskList.first().completedDate == LocalDate.now().format(MainActivity.DATE_FORMAT)){
                    //Use sort order settings to order the tasks
                    val orderSettings = D02SettingsList.getFullOrderList()
                    //Sort all tasks based on the sort order
                    var sortedList = taskList

                    for(orderIndex in orderSettings.size downTo 1){
                        //println("Debug: Order index = $orderIndex")
                        //Get the task param that matches the current index and add to the sorted list
                        for(taskParam in orderSettings){
                            //println("Debug: Task Param Index = ${taskParam.index}")
                            if(taskParam.index == orderIndex){
                                sortedList = when(taskParam.name){
                                    "Incomplete" -> sortIncomplete(sortedList, taskParam.type)
                                    "Pending" -> sortPending(sortedList, taskParam.type)
                                    else -> sortBySlope(sortedList, taskParam.name, taskParam.type)
                                }
                            }
                        }
                        //for(task in sortedList){ println("Debug: Task name in sorted List: ${task.name}") } //Debug sorting process

                    }
                    return sortedList
                }
                else{
                    println("Process: Sorting Archived Task List") //Process Line
                    return ArrayList(taskList.sortedWith(nullsLast(compareByDescending { LocalDate.parse(it.completedDate, MainActivity.DATE_FORMAT) })))
                }
            }

            return taskList
        }

        fun getContextTopmostTask(contextId: Int): TaskObject?{
            //Match the context id with the topmost task
            //println("Debug: getContextTopmostTask from context [${contextId}]: ${D03ContextList.nameFromId(contextId)}")
            return try{
                contextTopTaskList.first { it.contextId == contextId }
            }catch(error: NoSuchElementException){
                null
            }
        }

        private fun getContextTopmostTasks(contextList: ArrayList<ContextObject>) : ArrayList<TaskObject>{
            //Get the topmost task in sorted order for the given context list
            val topTaskList: ArrayList<TaskObject> = ArrayList()
            for(context in contextList){
                //println("Process: Get ${D03ContextList.nameFromId(context.id)} topmost Task") //Process line
                val taskContextList = D04TaskList.read("Context_Incomplete", context.id)
                val sortedTaskList = sortTaskList(taskContextList)
                if(sortedTaskList.isNotEmpty())
                {
                    //println("Result: ${D03ContextList.nameFromId(context.id)} topmost Task: ${sortedTaskList.first().name}") //Result line
                    topTaskList.add(sortedTaskList.first())
                }
            }
            return topTaskList
        }

        private fun sortIncomplete(taskList: ArrayList<TaskObject>, sortType: String) : ArrayList<TaskObject>{
            //println("Process: Sorting Incomplete Tasks: $sortType") //Process line
            val completedTasks: ArrayList<TaskObject> = ArrayList(taskList.filter { it.completedFlag })
            val incompleteTasks: ArrayList<TaskObject> = ArrayList(taskList.filter { !it.completedFlag })

            //Sort incomplete values based on their type
            if(sortType == "Low")
                return (completedTasks + incompleteTasks) as ArrayList<TaskObject>

            return (incompleteTasks + completedTasks) as ArrayList<TaskObject>
        }

        private fun sortPending(taskList: ArrayList<TaskObject>, sortType: String) : ArrayList<TaskObject>{
            //println("Process: Sorting Pending Tasks: $sortType") //Process line
            //Order the pending tasks by start date value
            val conditionsActive = D02SettingsList.getTaskFeatureStatus("Conditions")
            val conditionalTasks: ArrayList<TaskObject> = ArrayList(taskList.filter {
                conditionsActive && it.conditionActiveFlag == true })
            val activeTasks: ArrayList<TaskObject> = ArrayList(taskList.filter {
                LocalDate.parse(it.startDate, MainActivity.DATE_FORMAT) <= LocalDate.now() && (it.startTime.isEmpty() ||
                        Duration.between(LocalDate.parse(it.startDate, MainActivity.DATE_FORMAT)
                            .atTime(LocalTime.parse(it.startTime, MainActivity.TIME_FORMAT)), LocalDateTime.now()).toMinutes() > -1) &&
                        (!conditionsActive || it.conditionActiveFlag != true)})

            //Isolate the pending tasks based on either date or time
            val pendingTasks: ArrayList<TaskObject> = ArrayList(taskList.filter {
                (LocalDate.parse(it.startDate, MainActivity.DATE_FORMAT) > LocalDate.now() ||
                        (it.startTime.isNotEmpty() &&
                                Duration.between(LocalDate.parse(it.startDate, MainActivity.DATE_FORMAT)
                                    .atTime(LocalTime.parse(it.startTime, MainActivity.TIME_FORMAT)), LocalDateTime.now()).toMinutes() < 0))
                        && (!conditionsActive || it.conditionActiveFlag != true) })

            //Debug Logs for checking task information
            /*for(pendingTask in pendingTasks){
                println("Debug: Pending task: ${pendingTask.getTaskModalAsString()}")
                println("Debug: Task start time length: '${pendingTask.startTime.length}'")
            }*/

            val sortedConditional = ArrayList(conditionalTasks.sortedWith(nullsLast(compareBy {
                LocalDate.parse(it.earliestEndDate, MainActivity.DATE_FORMAT) })))
            val sortedPending = ArrayList(pendingTasks.sortedWith(nullsLast(compareBy (
                { LocalDate.parse(it.startDate, MainActivity.DATE_FORMAT) },
                { it.startTime.isEmpty()},
                { LocalTime.parse(it.startTime, MainActivity.TIME_FORMAT)}
                ))))


            //Sort incomplete values based on their type
            if(sortType == "High")
                return (sortedPending + sortedConditional + activeTasks) as ArrayList<TaskObject>

            return (activeTasks + sortedPending + sortedConditional) as ArrayList<TaskObject>
        }

        private fun sortBySlope(taskList: ArrayList<TaskObject>, sortName:String, sortType: String) : ArrayList<TaskObject>{
            //Ensure features are active before sorting
            val complexityActive = D02SettingsList.getTaskFeatureStatus("Complexity")
            val motivationActive = D02SettingsList.getTaskFeatureStatus("Motivation")
            if(sortName == "End Date" || (sortName == "Complexity" && complexityActive) || (sortName == "Motivation" && motivationActive)){
                //println("Process: Sorting Tasks by ${sortName}: $sortType") //Process line

                //Split the task array in terms of due time provided
                var endTimeTasks: ArrayList<TaskObject> = ArrayList(taskList.filter { (it.endTime.isNotEmpty())})
                val endDateTasks: ArrayList<TaskObject> = ArrayList(taskList.filter { (it.endTime.isEmpty())})
                var sortedActive = taskList
                when(sortName){
                    "Complexity" ->
                        return if (sortType == "Descending") {
                            ArrayList(taskList.sortedWith(nullsLast(compareByDescending { it.complexity })))
                        } else{
                            ArrayList(taskList.sortedWith(nullsLast(compareBy { it.complexity })))
                        }
                    "Motivation" ->
                        return if (sortType == "Descending") {
                            ArrayList(taskList.sortedWith(nullsLast(compareByDescending { it.motivation })))
                        } else {
                            ArrayList(taskList.sortedWith(nullsLast(compareBy { it.motivation })))
                        }
                    "End Date" ->
                        if (sortType == "Descending") {
                            endTimeTasks = ArrayList(endTimeTasks.sortedWith(nullsLast(compareByDescending { LocalTime.parse(it.endTime, MainActivity.TIME_FORMAT) })))
                            sortedActive = (endDateTasks + endTimeTasks) as ArrayList<TaskObject>
                            sortedActive =  ArrayList(sortedActive.sortedWith(nullsLast(compareByDescending { LocalDate.parse(it.earliestEndDate, MainActivity.DATE_FORMAT) })))
                        } else{
                            endTimeTasks = ArrayList(endTimeTasks.sortedWith(nullsLast(compareBy { LocalTime.parse(it.endTime, MainActivity.TIME_FORMAT) })))
                            sortedActive = (endTimeTasks + endDateTasks) as ArrayList<TaskObject>
                            sortedActive =  ArrayList(sortedActive.sortedWith(nullsLast(compareBy ({ LocalDate.parse(it.earliestEndDate, MainActivity.DATE_FORMAT) }, {it.endTime.isNotEmpty()}))))
                        }
                }
                return sortedActive
            }

            return taskList
        }
    }
}
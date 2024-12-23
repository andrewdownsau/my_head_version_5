package andrew.organiser.myhead_v3.features.ui_generation


import andrew.organiser.myhead_v3.MainActivity
import andrew.organiser.myhead_v3.features.crud.TaskListCURD
import andrew.organiser.myhead_v3.features.settings.TaskFeatures
import andrew.organiser.myhead_v3.features.settings.TaskSortOrder
import andrew.organiser.myhead_v3.modals.ContextModal
import andrew.organiser.myhead_v3.modals.TaskModal
import android.content.Context
import java.time.LocalDate

class SortUIList {

    companion object {

        // --- Sort UI Button List --- //
        fun main(c: Context, sortType: String, contextList: ArrayList<ContextModal>?, taskList: ArrayList<TaskModal>?)
                : Pair<ArrayList<ContextModal>?, ArrayList<TaskModal>?>{
            println("=== Gen11 - Sort UI Button List: $sortType ===")
            when(sortType){
                "sortContextList" -> return Pair(sortContextList(c, contextList), null)
                "sortTaskList" -> return Pair(null, sortTaskList(c, taskList))
            }

            return Pair(null, null)
        }

        fun getContextTopmostTask(c: Context, contextId: String) : TaskModal?{
            //Get the topmost task in sorted order for the given context
            val taskListRead = TaskListCURD.main(c, "Read", null, null, contextId)
            if(taskListRead.first && taskListRead.second != null){
                val rawContextTaskList = taskListRead.second!!
                val sortedTaskList = sortTaskList(c, rawContextTaskList)
                if(!sortedTaskList.isNullOrEmpty())
                    return sortedTaskList.first()
            }
            return null
        }

        private fun sortContextList(c: Context, contextList: ArrayList<ContextModal>?) : ArrayList<ContextModal>?{
            if (contextList != null) {
                //Get the topmost task in sorted order for each context
                val topTaskList: ArrayList<TaskModal> = ArrayList()
                for(context in contextList){
                    val topTask = getContextTopmostTask(c, context.id.toString())
                    if(topTask != null) topTaskList.add(topTask)
                }

                //Sort the top task list based on same rules as tasks
                val sortedTopTaskList = sortTaskList(c, topTaskList)
                if(!sortedTopTaskList.isNullOrEmpty()){
                    val sortedContextList: ArrayList<ContextModal> = ArrayList()
                    for(topTask in sortedTopTaskList){
                        for(context in contextList){
                            if(topTask.contextId == context.id) {
                                println("### TOP TASK: ${topTask.getTaskName()} matched to context ${context.getContextName()}")
                                sortedContextList.add(context)
                            }
                        }
                    }
                    val emptyContextList = (contextList - sortedContextList.toSet()) as ArrayList<ContextModal>

                    return (sortedContextList + emptyContextList) as ArrayList<ContextModal>
                }

                return  contextList
            }

            return null
        }

        private fun sortTaskList(c: Context, taskList: ArrayList<TaskModal>?) : ArrayList<TaskModal>?{
            println("___Sorting task list___")
            if(!taskList.isNullOrEmpty()){
                //Determine whether in archived list or active based on topmost task in list
                if(!taskList.first().getTaskCompletedFlag() || taskList.first().getTaskCompletedDate() == LocalDate.now().format(MainActivity.DATE_FORMAT)){
                    //Use sort order settings to order the tasks
                    val readOrderSettings = TaskSortOrder.main(c, "Read", null)
                    if(readOrderSettings.first && readOrderSettings.second != null){
                        val orderSettings = readOrderSettings.second!!
                        //Sort all tasks based on the sort order
                        var sortedList = taskList

                        for(orderIndex in 5 downTo 1){
                            //Get the task param that matches the current index and add to the sorted list
                            for(taskParam in orderSettings){
                                if(taskParam.second == orderIndex){
                                    println("Sorting tasks based on rule: ${taskParam.first}")
                                    when(taskParam.first){
                                        "Incomplete: High" -> sortedList = sortIncomplete(sortedList, "High")
                                        "Incomplete: Low" -> sortedList = sortIncomplete(sortedList, "Low")
                                        "Pending: High" -> sortedList = sortPending(sortedList, "High")
                                        "Pending: Low" -> sortedList = sortPending(sortedList, "Low")
                                        "Complexity: Ascending" -> sortedList = ArrayList(sortedList!!.sortedWith(nullsLast(compareBy { it.getTaskComplexity() })))
                                        "Complexity: Descending" -> sortedList = ArrayList(sortedList!!.sortedWith(nullsLast(compareByDescending { it.getTaskComplexity() })))
                                        "Motivation: Ascending" -> sortedList = ArrayList(sortedList!!.sortedWith(nullsLast(compareBy { it.getTaskMotivation() })))
                                        "Motivation: Descending" -> sortedList = ArrayList(sortedList!!.sortedWith(nullsLast(compareByDescending { it.getTaskMotivation() })))

                                        "Due Date: Ascending" -> sortedList =
                                            if (TaskFeatures.getTaskFeatureState(c, "Checklist")) {
                                                ArrayList(sortedList!!.sortedWith(nullsLast(compareBy { LocalDate.parse(it.getTaskChecklistDate(), MainActivity.DATE_FORMAT) })))
                                            } else{
                                                ArrayList(sortedList!!.sortedWith(nullsLast(compareBy { LocalDate.parse(it.getTaskDueDate(), MainActivity.DATE_FORMAT) })))
                                            }
                                        "Due Date: Descending" -> sortedList =
                                            if (TaskFeatures.getTaskFeatureState(c, "Checklist")) {
                                                ArrayList(sortedList!!.sortedWith(nullsLast(compareByDescending { LocalDate.parse(it.getTaskChecklistDate(), MainActivity.DATE_FORMAT) })))
                                            } else {
                                                ArrayList(sortedList!!.sortedWith(nullsLast(compareByDescending { LocalDate.parse(it.getTaskDueDate(), MainActivity.DATE_FORMAT) })))
                                            }
                                    }
                                }
                            }
                        }
                        for(item in sortedList!!){
                            println("Item name: ${item.getTaskName()}")
                        }

                        return sortedList
                    }
                }
                else{
                    //If in archive, sort only by completed date
                    println("Sorting Archived Task List")
                    return ArrayList(taskList.sortedWith(nullsLast(compareByDescending { LocalDate.parse(it.getTaskCompletedDate(), MainActivity.DATE_FORMAT) })))
                }
            }

            return null
        }

        private fun sortIncomplete(taskList: ArrayList<TaskModal>?, sortType: String) : ArrayList<TaskModal>?{
            println("___Sorting Incomplete Tasks: ${sortType}___")
            if(taskList != null){
                val completedTasks: ArrayList<TaskModal> = ArrayList(taskList.filter { it.getTaskCompletedFlag() })
                val incompleteTasks: ArrayList<TaskModal> = ArrayList(taskList.filter { !it.getTaskCompletedFlag() })

                //Sort incomplete values based on their type
                if(sortType == "High")
                    return (incompleteTasks + completedTasks) as ArrayList<TaskModal>

                else if(sortType == "Low")
                    return (completedTasks + incompleteTasks) as ArrayList<TaskModal>

            }
            return null
        }

        private fun sortPending(taskList: ArrayList<TaskModal>?, sortType: String) : ArrayList<TaskModal>?{
            println("___Sorting Pending Tasks: ${sortType}___")
            if(taskList != null){
                //Order the pending tasks by start date value
                val pendingTasks: ArrayList<TaskModal> = ArrayList(taskList.filter {
                    (LocalDate.parse(it.getTaskStartDate(), MainActivity.DATE_FORMAT) > LocalDate.now() || it.getTaskConditionId() != null) })
                val sortedPending = ArrayList(pendingTasks.sortedWith(nullsLast(compareBy (
                    { it.getTaskConditionId() != null },
                    { LocalDate.parse(it.getTaskStartDate(), MainActivity.DATE_FORMAT)}))))

                val activeTasks: ArrayList<TaskModal> = ArrayList(taskList.filter {
                    LocalDate.parse(it.getTaskStartDate(), MainActivity.DATE_FORMAT) <= LocalDate.now() && it.getTaskConditionId() == null })

                //Sort incomplete values based on their type
                if(sortType == "High")
                    return (sortedPending + activeTasks) as ArrayList<TaskModal>

                else if(sortType == "Low")
                    return (activeTasks + sortedPending) as ArrayList<TaskModal>

            }
            return null
        }
    }
}
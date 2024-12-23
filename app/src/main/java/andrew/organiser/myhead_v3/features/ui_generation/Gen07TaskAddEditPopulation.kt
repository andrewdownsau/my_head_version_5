package andrew.organiser.myhead_v3.features.ui_generation

import andrew.organiser.myhead_v3.R
import andrew.organiser.myhead_v3.features.crud.ContextListCURD
import andrew.organiser.myhead_v3.modals.TaskModal
import andrew.organiser.myhead_v3.features.crud.TaskListCURD
import andrew.organiser.myhead_v3.modals.ContextModal
import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Spinner

class TaskAddEditPopulation {

    companion object {

        //Global variables for companion object
        private var contextList = mutableListOf<ContextModal>()
        private var contextNameList = mutableListOf<String>()
        private var frequencyList = mutableListOf<String>()
        private var conditionList = mutableListOf("None")
        private var taskCondition = "None"

        // --- Task Add Edit Population --- //
        fun main(c: Context, spinnerList: ArrayList<Spinner>, taskObject: TaskModal?, contextName: String?){
            println("=== Gen07 - Task Add Edit Population for Task ===")
            //Initialise main function variables
            var taskContextName = contextName
            var taskFrequency = "None"
            taskCondition = "None"

            //Extract context list for future use
            extractGlobalLists(c, taskObject, contextName)

            //If context name is not given, use task object to lookup
            if(contextName == null && taskObject != null){
                taskContextName = getContextNameFromTask(taskObject)

                //Use task object to set any frequency or condition value
                if(taskObject.getTaskFrequency().isNotEmpty()) taskFrequency = taskObject.getTaskFrequency()
            }

            populateSpinner(c, spinnerList[0], taskContextName, contextNameList)
            populateSpinner(c, spinnerList[1], taskFrequency, frequencyList)
            populateSpinner(c, spinnerList[2], taskCondition, conditionList)
        }

        fun populateSpinner(c: Context, spinner: Spinner, itemName: String?, nameList: MutableList<String>){
            val adp1 = ArrayAdapter(c, R.layout.spinner_item, nameList)
            spinner.setAdapter(adp1)
            if(itemName != null){
                spinner.post {
                    spinner.setSelection(adp1.getPosition(itemName))
                }
            }
        }

        fun getContextIdFromName(contextName: String?) : String{
            val contextLookup = contextList.filter { it.getContextName() == contextName }
            return if(contextLookup.isNotEmpty())
                contextLookup[0].id.toString()
            else
                ""
        }

        fun getConditionIdFromName(c: Context, conditionName: String?) : Int{
            val readTaskList = TaskListCURD.main(c, "Read", null, null, null)
            if(readTaskList.first){
                for(taskObject in readTaskList.second!!){
                    if(taskObject.getTaskName() == conditionName)
                        return taskObject.id
                }
            }

            return 0
        }

        fun getConditionNameList(c: Context, contextId:String?, taskObject: TaskModal?, taskObjectId: Int?): Pair<String, MutableList<String>> {
            //Get the context Id, either from the function or through looking up with the taskObject
            var currentContextId = contextId
            if(currentContextId == null && taskObject != null){ currentContextId = taskObject.getTaskContextId().toString() }
            var currentTaskId = taskObjectId
            if(currentTaskId == null && taskObject != null){ currentTaskId = taskObject.id }

            if(currentContextId != null){
                println("__Get condition list for context: $currentContextId")
                val localConditionList = mutableListOf("None")
                //Add the other tasks related to this context
                val taskTableRead = TaskListCURD.main(c, "Read", null, null, currentContextId)
                if(taskTableRead.first && !taskTableRead.second.isNullOrEmpty()){
                    var contextTaskList = taskTableRead.second
                    contextTaskList = (contextTaskList!!.filter { it.id != currentTaskId && !it.getTaskCompletedFlag() }) as ArrayList<TaskModal>

                    //Add conditions to list and set the current task condition if applicable
                    contextTaskList.forEach {
                        localConditionList.add(it.getTaskName())
                        if(taskObject != null && it.id == taskObject.getTaskConditionId()) taskCondition = it.getTaskName()
                    }


                    return Pair(taskCondition, localConditionList)
                }
            }

            return Pair(taskCondition, conditionList)
        }

        private fun extractGlobalLists(c: Context, taskObject: TaskModal?, contextName: String?){
            //Extract context list
            var currentContextId: String? = null
            val contextListRead = ContextListCURD.main(c, "Read", null, null)
            if(contextListRead.first){
                contextList = contextListRead.second!!
                contextNameList.clear()
                for(context in contextList){
                    contextNameList.add(context.getContextName())
                    if(contextName == context.getContextName()) currentContextId = context.id.toString()
                }
            }

            //Extract frequency list
            frequencyList = mutableListOf("None", "Daily", "Weekly", "Fortnightly", "Monthly", "Quarterly", "Yearly")

            //Extract condition list based on context name and not current task
            conditionList = getConditionNameList(c, currentContextId, taskObject, null).second
        }

        private fun getContextNameFromTask(taskObject: TaskModal) : String{
            val contextLookup = contextList.filter { it.id == taskObject.contextId }
            return if(contextLookup.isNotEmpty())
                contextLookup[0].getContextName()
            else
                ""
        }
    }
}
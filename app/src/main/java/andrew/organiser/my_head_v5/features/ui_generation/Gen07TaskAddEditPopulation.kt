package andrew.organiser.my_head_v5.features.ui_generation

import andrew.organiser.my_head_v5.R
import andrew.organiser.my_head_v5.features.data_manipulation.D02SettingsList
import andrew.organiser.my_head_v5.features.data_manipulation.D03ContextList
import andrew.organiser.my_head_v5.features.data_manipulation.D04TaskList
import andrew.organiser.my_head_v5.data_objects.TaskObject
import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Spinner

class Gen07TaskAddEditPopulation {

    companion object {

        //Global variables for companion object
        private var contextNameList = mutableListOf<String>()
        private val frequencyList = arrayListOf("Days", "Weeks", "Months", "Years")
        private var conditionList = mutableListOf("None")
        private var taskCondition = "None"

        // --- Task Add Edit Population --- //
        fun main(c: Context, spinnerList: ArrayList<Spinner>, taskObject: TaskObject?, contextName: String?){
            //Initialise main function variables
            var taskContextName = contextName
            var taskFrequency = "Days"

            //Extract context list for future use
            extractGlobalLists(taskObject, contextName)

            //If context name is not given, use task object to lookup
            if(contextName == null && taskObject != null){
                taskContextName = D03ContextList.nameFromId(taskObject.contextId)

                //Use task object to set any frequency or condition value
                if(!taskObject.frequency.isNullOrEmpty() && taskObject.frequency!!.contains("_")) taskFrequency = taskObject.frequency!!.split("_")[1]
            }

            println("=== Gen07 - Task Add Edit Population for Task with context: $taskContextName ===")
            populateSpinner(c, spinnerList[0], taskContextName, contextNameList)
            populateSpinner(c, spinnerList[1], taskFrequency, frequencyList)
            populateSpinner(c, spinnerList[2], taskCondition, conditionList)
        }

        fun populateConditionSpinner(c: Context, contextName: String, conditionSpinner: Spinner, taskObjectId: Int?){
            extractGlobalLists(D04TaskList.getById(taskObjectId), contextName)
            populateSpinner(c, conditionSpinner, taskCondition, conditionList)
        }

        private fun populateSpinner(c: Context, spinner: Spinner, itemName: String?, nameList: MutableList<String>){
            val adp1 = ArrayAdapter(c, R.layout.spinner_item, nameList)
            spinner.adapter = adp1
            if(itemName != null){
                spinner.post {
                    spinner.setSelection(adp1.getPosition(itemName))
                }
            }
        }

        private fun extractGlobalLists(taskObject: TaskObject?, contextName: String?){
            //Extract context list
            val tempConditionId = taskObject?.conditionId ?: 0
            var tempContextId = taskObject?.contextId ?: 0
            val contextListRead = D03ContextList.read()
            if(contextListRead.isNotEmpty()){
                contextNameList.clear()
                for(context in contextListRead){
                    contextNameList.add(context.name)
                    if(contextName == context.name) tempContextId = context.id
                }
            }

            extractConditionList(tempContextId, taskObject, tempConditionId)

        }

        private fun extractConditionList(contextId: Int, taskObject: TaskObject?, conditionId: Int){
            //Extract condition list based on context name and not current task if applicable
            if(D02SettingsList.getTaskFeatureStatus("Conditions")){
                conditionList = D04TaskList.getConditionList(contextId, taskObject)
                taskCondition = D04TaskList.getNameFromId(conditionId)
            }
        }
    }
}
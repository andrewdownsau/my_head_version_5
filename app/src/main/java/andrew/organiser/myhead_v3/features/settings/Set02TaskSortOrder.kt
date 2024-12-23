package andrew.organiser.myhead_v3.features.settings

import andrew.organiser.myhead_v3.features.crud.SettingsListCURD
import android.content.Context

class TaskSortOrder {

    companion object {

        // --- Reading and Updating the Task Sort Order --- //
        fun main(c: Context, cmd: String, newTSOSettings: ArrayList<Pair<String, Int>>?) :
                Pair<Boolean, ArrayList<Pair<String, Int>>?>{
            println("=== Set02 - Task Sort Order: $cmd ===")
            when(cmd){
                "Read" -> return readTaskOrdering(c)
                "Update" -> return Pair(updateTaskSortOrder(c, newTSOSettings!!), null)
            }

            return Pair(false, null)
        }

        private fun readTaskOrdering(c: Context) : Pair<Boolean, ArrayList<Pair<String, Int>>?>{
            //Read the settings value array that holds the task feature list
            println("__Read Task Sort Order__")
            val settingsSortOrderRead = SettingsListCURD.main(c, "Read", "Task_Sort_Order", null)
            if(settingsSortOrderRead.first && settingsSortOrderRead.second != null){
                val taskOrderSettings: ArrayList<Pair<String, Int>> = ArrayList()
                for(orderStr in settingsSortOrderRead.second!!){
                    val orderKey = orderStr.split("_")[0]
                    val orderVal = orderStr.split("_")[1].toInt()
                    println("__Adding Task Order: $orderKey and ${orderVal}__")
                    taskOrderSettings.add(Pair(orderKey,orderVal))
                }
                return Pair(true, taskOrderSettings)
            }
            return Pair(false, null)
        }

        private fun updateTaskSortOrder(c: Context, sortOrderList: ArrayList<Pair<String, Int>>?) : Boolean{
            println("__Update Task Sort Order__")
            //Convert the array list to a string
            if (sortOrderList != null) {
                var newSortOrderStr = ""
                for(order in sortOrderList){
                    newSortOrderStr += "${order.first}_${order.second},"
                }

                //Remove final comma from string
                newSortOrderStr = newSortOrderStr.take(newSortOrderStr.length - 1)
                return SettingsListCURD.main(c, "Update", "Task_Sort_Order", newSortOrderStr).first

            }

            return false
        }
    }
}
package andrew.organiser.myhead_v3.features.settings

import andrew.organiser.myhead_v3.features.crud.SettingsListCURD
import android.content.Context

class TaskColors {

    companion object {

        // --- Reading and Updating the Task Colors --- //
        fun main(c: Context, cmd: String, newTaskColors: ArrayList<Pair<String, String>>?) :
                Pair<Boolean, ArrayList<Pair<String, String>>?>{
            println("=== Set03 - Task Colors: $cmd ===")
            when(cmd){
                "Read" -> return readTaskColors(c)
                "Update" -> return Pair(updateTaskColors(c, newTaskColors), null)
            }

            return Pair(false, null)
        }

        private fun readTaskColors(c: Context) : Pair<Boolean, ArrayList<Pair<String, String>>?>{
            //Read the settings value array that holds the task feature list
            println("__Read Task Colors__")

            val settingsColorRead = SettingsListCURD.main(c, "Read", "UI_Colors", null)
            if(settingsColorRead.first && settingsColorRead.second != null){
                val taskColorSettings: ArrayList<Pair<String, String>> = ArrayList()
                for(colorStr in settingsColorRead.second!!){
                    val colorKey = colorStr.split("_")[0]
                    val colorVal = colorStr.split("_")[1]
                    println("__Adding Task Color: $colorKey and ${colorVal}__")
                    taskColorSettings.add(Pair(colorKey,colorVal))
                }
                return Pair(true, taskColorSettings)
            }
            return Pair(false, null)
        }

        private fun updateTaskColors(c: Context, colors: ArrayList<Pair<String, String>>?) : Boolean{
            //Update the task color list in the settings
            println("__Update Task Colors__")

            //If valid format, convert array list into formatted string to add to settings database table
            if(!colors.isNullOrEmpty()){
                //Convert the array list to a string
                var newColorsStr = ""
                for(color in colors){
                    newColorsStr += "${color.first}_${color.second},"
                }
                //Remove final comma from string
                newColorsStr = newColorsStr.take(newColorsStr.length - 1)
                SettingsListCURD.main(c, "Update", "UI_Colors", newColorsStr)
            }

            return false
        }
    }
}
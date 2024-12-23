package andrew.organiser.myhead_v3.features.settings


import andrew.organiser.myhead_v3.features.crud.SettingsListCURD
import android.content.Context


class TaskFeatures {

    companion object {

        // --- Reading and Updating the Task Features Settings --- //
        fun readTaskFeatureList(c: Context) : Pair<Boolean, ArrayList<Pair<String, Boolean>>?>{
            //Read the settings value array that holds the task feature list
            println("__Read Task Features__")
            val settingsFeaturesRead = SettingsListCURD.main(c, "Read", "Task_Features", null)
            if(settingsFeaturesRead.first && settingsFeaturesRead.second != null){
                val taskFeatureSettings: ArrayList<Pair<String, Boolean>> = ArrayList()
                for(featureStr in settingsFeaturesRead.second!!){
                    val featureKey = featureStr.split(":")[0]
                    val featureVal = featureStr.contains("true")
                    println("__Adding Task Feature: $featureKey and ${featureVal}__")
                    taskFeatureSettings.add(Pair(featureKey,featureVal))
                }
                return Pair(true, taskFeatureSettings)
            }
            return Pair(false, null)
        }

        fun getTaskFeatureState(c: Context, featureType: String) : Boolean{
            //Read the settings value array that holds the task feature list
            println("__Read Task Features__")
            val settingsFeaturesRead = SettingsListCURD.main(c, "Read", "Task_Features", null)
            if(settingsFeaturesRead.first && settingsFeaturesRead.second != null){
                for(featureStr in settingsFeaturesRead.second!!){
                    val featureKey = featureStr.split(":")[0]
                    if(featureKey == featureType) return featureStr.contains("true")
                }
            }
            return false
        }

        fun updateTaskFeatures(c: Context, features: ArrayList<Pair<String, Boolean>>?) : Boolean{
            //Update the task feature list in the settings
            println("__Update Task Features__")

            //If valid format, convert array list into formatted string to add to settings database table
            if(!features.isNullOrEmpty()){
                //Convert the array list to a string
                var newFeaturesStr = ""
                for(feature in features){
                    newFeaturesStr += "${feature.first}:${feature.second},"
                }
                //Remove final comma from string
                newFeaturesStr = newFeaturesStr.take(newFeaturesStr.length - 1)
                return SettingsListCURD.main(c, "Update", "Task_Features", newFeaturesStr).first

            }

            return false
        }
    }
}
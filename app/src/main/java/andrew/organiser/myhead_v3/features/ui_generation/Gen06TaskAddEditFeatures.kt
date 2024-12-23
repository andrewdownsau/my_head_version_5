package andrew.organiser.myhead_v3.features.ui_generation

import andrew.organiser.myhead_v3.features.crud.SettingsListCURD
import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout


class TaskAddEditFeatures {

    companion object {

        // --- Task Add Edit Features --- //
        fun main(c: Context, taskFeatureWidgets: ArrayList<ConstraintLayout>){
            println("=== Gen06 - Task Add/Edit Features ===")
            //Read the task feature settings and apply to the widget array
            val settingsFeaturesRead = SettingsListCURD.main(c, "Read", "Task_Features", null)
            if(settingsFeaturesRead.first && settingsFeaturesRead.second != null) {
                for(featureStr in settingsFeaturesRead.second!!){
                    val viewVisibility = if(featureStr.split(":")[1].toBoolean()) View.VISIBLE else View.GONE
                    when(featureStr.split(":")[0]){
                        "Motivation" -> taskFeatureWidgets.elementAt(0).visibility = viewVisibility
                        "Complexity" -> taskFeatureWidgets.elementAt(1).visibility = viewVisibility
                        "Checklist" -> taskFeatureWidgets.elementAt(2).visibility = viewVisibility
                        "Repeating" -> taskFeatureWidgets.elementAt(3).visibility = viewVisibility
                        "Conditions" -> taskFeatureWidgets.elementAt(4).visibility = viewVisibility
                    }
                }
            }
        }

        fun getFeatureSetting(c: Context, feature: String): Boolean{
            //Read the task feature settings and apply to the widget array
            val settingsFeaturesRead = SettingsListCURD.main(c, "Read", "Task_Features", null)
            if(settingsFeaturesRead.first && settingsFeaturesRead.second != null) {
                for(featureStr in settingsFeaturesRead.second!!){
                    if(featureStr.contains(feature))
                        return featureStr.split(":")[1].toBoolean()
                }
            }
            return false
        }
    }
}
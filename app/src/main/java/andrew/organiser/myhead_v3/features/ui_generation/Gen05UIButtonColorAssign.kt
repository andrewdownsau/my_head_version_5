package andrew.organiser.myhead_v3.features.ui_generation

import andrew.organiser.myhead_v3.MainActivity
import andrew.organiser.myhead_v3.R
import andrew.organiser.myhead_v3.features.crud.SettingsListCURD
import andrew.organiser.myhead_v3.modals.TaskModal
import andrew.organiser.myhead_v3.features.crud.TaskListCURD
import andrew.organiser.myhead_v3.features.settings.TaskFeatures
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.widget.Button
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import java.time.Duration
import java.time.LocalDate


class UIButtonColorAssign {

    companion object {

        // --- UI Button Color Assign --- //
        fun main(c: Context, cmd: String, taskObject: TaskModal?, contextId: String?, dynamicButtonLayout: ConstraintLayout){
            println("=== Gen05 - UI: $cmd ===")
            //Extract dynamic button to assign color
            val dynamicButton = dynamicButtonLayout.getChildAt(0) as Button

            //Conduct assignment based on command type and value
            when(cmd){
                "TaskColorAssign" -> assignTaskColor(c, taskObject, dynamicButton)
                "ContextColorAssign" -> assignContextColor(c, contextId, dynamicButton)
            }
        }

        private fun assignTaskColor(c: Context, taskObject: TaskModal?, dynamicButton: Button){
            var buttonColor = "#EEEEEE"
            if(taskObject != null){
                val taskName = taskObject.getTaskName()
                println("__Assign Task Color to: ${taskName}__")

                //Read the color settings to match the applicable color string
                val settingsColorsRead = SettingsListCURD.main(c, "Read", "UI_Colors", null)
                if(settingsColorsRead.first && settingsColorsRead.second != null) {
                    val settingsColorStrList = settingsColorsRead.second!!
                    try {
                        // --- Check 1: Is the task completed? ---
                        if(taskObject.getTaskCompletedFlag()){
                            dynamicButton.paintFlags += Paint.STRIKE_THRU_TEXT_FLAG
                            buttonColor = getColorFromSettings(settingsColorStrList, "completed")
                        }
                        else{
                            //Ensure that formatting is reversed if needed to un-complete task
                            if ((dynamicButton.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG) > 0)
                                dynamicButton.paintFlags = dynamicButton.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

                            // --- Check 2: Is the task conditional of others or time ---
                            val startDateStr = taskObject.getTaskStartDate()
                            val taskCondition = taskObject.getTaskConditionId() != null
                            val todayDate = MainActivity.SIMPLE_DF.parse(LocalDate.now().format(MainActivity.DATE_FORMAT))
                            val startDate = MainActivity.SIMPLE_DF.parse(startDateStr)
                            if (startDate != null && todayDate != null && ( startDate > todayDate || (taskCondition && TaskAddEditFeatures.getFeatureSetting(c, "Conditions")))) {
                                buttonColor = getColorFromSettings(settingsColorStrList, "conditional")
                                if(!taskCondition){
                                    val daysToPending = Duration.between(todayDate.toInstant(), startDate.toInstant()).toDays()
                                    if(daysToPending in 1..3){
                                        buttonColor = getColorFromSettings(settingsColorStrList, "pending")
                                    }
                                }
                            }
                            else {
                                // --- Check 3: Is the task complexity less than 2 + 1 ---
                                if(taskObject.getTaskComplexity() < 2 && (dynamicButton.paintFlags and Paint.UNDERLINE_TEXT_FLAG) == 0 && TaskFeatures.getTaskFeatureState(c, "Complexity"))
                                    dynamicButton.paintFlags += Paint.UNDERLINE_TEXT_FLAG

                                // --- Check 4: Is the task urgent, if so, how urgent? ---
                                val dueDate = MainActivity.SIMPLE_DF.parse(taskObject.getTaskChecklistDate())?.toInstant()
                                if(dueDate != null && todayDate != null) {
                                    buttonColor = when (Duration.between(todayDate.toInstant(), dueDate).toDays()){
                                        // --- Check 5.1: Is the task due past 7 days ---
                                        in 8 .. Int.MAX_VALUE -> getColorFromSettings(settingsColorStrList, "weekPlus")
                                        // --- Check 5.2: Is the task due in 3 to 7 days ---
                                        in 4 .. 7 -> getColorFromSettings(settingsColorStrList, "week")
                                        // --- Check 5.3: Is the task due in 2 to 3 days ---
                                        in 2 .. 3 -> getColorFromSettings(settingsColorStrList, "threeDays")
                                        // --- Check 5.3: Is the task due tomorrow ---
                                        1L -> getColorFromSettings(settingsColorStrList, "tomorrow")
                                        // --- Check 5.4: Is the task due today ---
                                        0L -> getColorFromSettings(settingsColorStrList, "today")
                                        // --- Check 5.4: Is the task overdue ---
                                        else -> getColorFromSettings(settingsColorStrList, "overdue")
                                    }
                                }
                            }
                        }
                    }
                    catch (e :Exception){
                        println("~~~ Error: $e ~~~")
                    }
                }
            }
            setButtonBackground(c, dynamicButton, buttonColor)
        }

        private fun assignContextColor(c: Context, contextId: String?, dynamicButton: Button){
            if(contextId != null){
                println("__Assign Context Color to: ${contextId}__")

                //Retrieve the topmost task within the context set and then assign color
                val topmostContextTask = SortUIList.getContextTopmostTask(c, contextId)
                assignTaskColor(c, topmostContextTask, dynamicButton)
            }
        }

        fun getColorFromSettings(settingsStrList: List<String>, colorKey: String): String {
            for(colorSetting in settingsStrList){
                if(colorSetting.contains(colorKey))
                    return colorSetting.split("_")[1]
            }
            return "#EEEEEE"
        }

        private fun setButtonBackground(c: Context, dynamicButton: Button, color: String) : Button{
            println("__Set button background color: ${color}__")
            val btnBackgroundResource = R.drawable.shadow_button_white

            val btnLayerDrawable = AppCompatResources.getDrawable(c, btnBackgroundResource) as LayerDrawable
            val foregroundDrawable = btnLayerDrawable.findDrawableByLayerId(R.id.foregroundDrawable)

            when (foregroundDrawable) {
                is ShapeDrawable -> { foregroundDrawable.paint.color = Color.parseColor(color) }
                is GradientDrawable -> { foregroundDrawable.setColor(Color.parseColor(color)) }
                is ColorDrawable -> { foregroundDrawable.color = Color.parseColor(color) }
            }

            dynamicButton.background = btnLayerDrawable

            return dynamicButton
        }
    }
}
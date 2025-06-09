package andrew.organiser.my_head_v5.features.ui_generation

import andrew.organiser.my_head_v5.MainActivity
import andrew.organiser.my_head_v5.R
import andrew.organiser.my_head_v5.features.data_manipulation.D02SettingsList
import andrew.organiser.my_head_v5.features.data_manipulation.D07OrderUIList
import andrew.organiser.my_head_v5.data_objects.TaskObject
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
import java.time.LocalDateTime
import java.time.LocalTime

class Gen05UIButtonColorAssign {

    companion object {

        // --- UI Button Color Assign --- //
        fun main(c: Context, cmd: String, taskObject: TaskObject?, contextId: Int?, dynamicButtonLayout: ConstraintLayout){
            //println("=== Gen05 - UI: $cmd ===")
            //Extract dynamic button to assign color
            val dynamicButton = dynamicButtonLayout.getChildAt(0) as Button

            //Conduct assignment based on command type and value
            when(cmd){
                "TaskColorAssign" -> assignTaskColor(c, taskObject, dynamicButton)
                "ContextColorAssign" -> assignContextColor(c, contextId, dynamicButton)
            }
        }

        private fun assignTaskColor(c: Context, taskObject: TaskObject?, dynamicButton: Button){
            var buttonColor = "#EEEEEE"
            if(taskObject != null){
                try {
                    // --- Check 1: Is the task completed? ---
                    if(taskObject.completedFlag){
                        dynamicButton.paintFlags += Paint.STRIKE_THRU_TEXT_FLAG
                        buttonColor = D02SettingsList.getUIColor("completed")
                    }
                    else{
                        //Ensure that formatting is reversed if needed to un-complete task
                        if ((dynamicButton.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG) > 0)
                            dynamicButton.paintFlags = dynamicButton.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

                        // --- Check 2: Is the task conditional of others tasks ---
                        if(D02SettingsList.getTaskFeatureStatus("Conditions") && taskObject.conditionStatus == true)
                            buttonColor = D02SettingsList.getUIColor("conditional")
                        else{
                            // --- Check 3: Is the task pending in time (not started yet) ---
                            val startDate = LocalDate.parse(taskObject.startDate, MainActivity.DATE_FORMAT)
                            val taskTimeSet = taskObject.startTime.isNotEmpty() && taskObject.dueTime.isNotEmpty()
                            var currentTaskPeriod = Duration.between(startDate.atStartOfDay(), LocalDate.now().atStartOfDay()).toMinutes()
                            var timeFromMidnight = 0
                            if(taskTimeSet){
                                val startTime = LocalTime.parse(taskObject.startTime, MainActivity.TIME_FORMAT)
                                timeFromMidnight = Duration.between(startDate.atTime(startTime), startDate.atStartOfDay()).toMinutes().toInt()
                                currentTaskPeriod = Duration.between(startDate.atTime(startTime), LocalDateTime.now()).toMinutes()
                            }

                            //println("Debug: currentTaskPeriod: $currentTaskPeriod")
                            //println("Debug: timeFromMidnight: $timeFromMidnight")
                            when (currentTaskPeriod) {
                                in Int.MIN_VALUE .. -4320 -> buttonColor = D02SettingsList.getUIColor("conditional")
                                in -4319..< timeFromMidnight -> buttonColor = D02SettingsList.getUIColor("pending")
                                in timeFromMidnight..-1 -> buttonColor = D02SettingsList.getUIColor("startToday")
                                else -> {
                                    // --- Check 4: Is the task complexity less than 2 + 1 ---
                                    if(taskObject.complexity < 2 && (dynamicButton.paintFlags and Paint.UNDERLINE_TEXT_FLAG) == 0 && D02SettingsList.getTaskFeatureStatus("Complexity"))
                                        dynamicButton.paintFlags += Paint.UNDERLINE_TEXT_FLAG

                                    // --- Check 5: Is the task urgent, if so, how urgent, in days? ---
                                    val dueDate = LocalDate.parse(taskObject.checklistDate, MainActivity.DATE_FORMAT)
                                    if(dueDate != null) {
                                        val daysTillDue = Duration.between(LocalDate.now().atStartOfDay(), dueDate.atStartOfDay()).toDays()
                                        buttonColor = when (daysTillDue) {
                                            // --- Check 5.1: Is the task due past 7 days ---
                                            in 8..Int.MAX_VALUE -> D02SettingsList.getUIColor("weekPlus")
                                            // --- Check 5.2: Is the task due in 3 to 7 days ---
                                            in 4..7 -> D02SettingsList.getUIColor("week")
                                            // --- Check 5.3: Is the task due in 2 to 3 days ---
                                            in 2..3 -> D02SettingsList.getUIColor("threeDays")
                                            // --- Check 5.4: Is the task due tomorrow ---
                                            1L -> D02SettingsList.getUIColor("tomorrow")
                                            // --- Check 5.5: Is the task due either today or within 24 hours ---
                                            0L -> D02SettingsList.getUIColor("today")
                                            // --- Check 5.6: Is the task overdue ---
                                            else -> D02SettingsList.getUIColor("overdue")
                                        }
                                        // --- Check 6: If the task has times, does this reflect the urgent status ---
                                        if((!taskObject.checklist || taskObject.dueDate == taskObject.checklistDate) && taskTimeSet && daysTillDue < 2){
                                            val dueTime = LocalTime.parse(taskObject.dueTime, MainActivity.TIME_FORMAT)
                                            val currentTimeLeft = Duration.between(LocalDateTime.now(), dueDate.atTime(dueTime)).toHours()
                                            buttonColor = when (currentTimeLeft) {
                                                // --- Check 6.1: Is the task due in the next 24 to 48 hours---
                                                in 24..48 -> D02SettingsList.getUIColor("tomorrow")
                                                // --- Check 6.2: Is the task due in the last 23 hours ---
                                                in 0..23 -> D02SettingsList.getUIColor("today")
                                                // --- Check 6.3: Is the task overdue ---
                                                else -> D02SettingsList.getUIColor("overdue")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                catch (e :Exception){
                    println("~~~ Error: $e ~~~")
                }

            }
            setButtonBackground(c, dynamicButton, buttonColor)
        }

        private fun assignContextColor(c: Context, contextId: Int?, dynamicButton: Button){
            if(contextId != null){
                //Retrieve the topmost task within the context set and then assign color
                val topmostContextTask = D07OrderUIList.getContextTopmostTask(contextId)
                assignTaskColor(c, topmostContextTask, dynamicButton)
            }
        }

        private fun setButtonBackground(c: Context, dynamicButton: Button, color: String) : Button{
            //println("Process: Set button background color: ${color}__") //Process Line
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
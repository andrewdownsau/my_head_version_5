package andrew.organiser.myhead_v3

import android.R.color
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.view.View
import android.widget.Button
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import java.time.Duration
import java.time.LocalDate


class UIHelper {
    companion object {

        // --- Generic Button UI Functions --- //
        fun createGenericButton(c: Context, btnText : String) : Button{
            val dynamicButton = Button(c)

            // setting layout_width and layout_height using layout parameters
            val params = ConstraintLayout.LayoutParams(1000, 200)
            params.setMargins(15, 15, 15, 15)
            dynamicButton.setLayoutParams(params)
            dynamicButton.isAllCaps = false
            dynamicButton.textSize = 20.0F
            dynamicButton.id = View.generateViewId()

            //Set text new line based on number of characters (max 30 characters per line)
            var displayedText = btnText
            if(btnText.length > 32){
                displayedText = btnText.take(32)
                displayedText = displayedText.take(displayedText.lastIndexOf(" "))
                displayedText += "\n" + btnText.drop(displayedText.length)
            }
            dynamicButton.text = displayedText

            return dynamicButton
        }

        fun setButtonBackground(c: Context, taskObject: TaskModal?, dynamicButton: Button){
            val btnBackgroundResource = R.drawable.shadow_button
            val settings = DBHandler(c).readSettings()
            if(settings != null){
                var btnColor = "#dddddd"
                if(taskObject != null){
                    val taskName = taskObject.getTaskName()
                    println("_Setting background of task:\n$taskName")

                    try {
                        // --- Check 1: Is the task completed? ---
                        if(taskObject.getTaskCompletedFlag()){
                            dynamicButton.paintFlags += Paint.STRIKE_THRU_TEXT_FLAG
                            btnColor = settings.getColor("completed")
                        }
                        else{
                            //Ensure that formatting is reversed if needed to un-complete task
                            if ((dynamicButton.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG) > 0)
                                dynamicButton.paintFlags = dynamicButton.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

                            // --- Check 2: Is the task conditional of others or time ---
                            val startDateStr = taskObject.getTaskStartDate()
                            val taskCondition = taskObject.getTaskConditionId() != 0
                            val todayDate = MainActivity.SIMPLE_DF.parse(LocalDate.now().format(MainActivity.DATE_FORMAT))
                            val startDate = MainActivity.SIMPLE_DF.parse(startDateStr)
                            if (startDate != null && todayDate != null && ( startDate > todayDate || taskCondition)) {
                                btnColor = settings.getColor("conditional")
                                if(!taskCondition){
                                    btnColor = when (Duration.between(todayDate.toInstant(), startDate.toInstant()).toDays()){
                                        // --- Check 1: Is the task due to start in 2 to 3 days ---
                                        in 1 .. 3 -> settings.getColor("pending")
                                        // --- Check 2: Else ---
                                        else -> settings.getColor("conditional")
                                    }
                                }
                            }
                            else {
                                // --- Check 3: Is the task complexity less than 2 + 1 ---
                                if(taskObject.getTaskComplexity() < 2 && (dynamicButton.paintFlags and Paint.UNDERLINE_TEXT_FLAG) == 0)
                                    dynamicButton.paintFlags += Paint.UNDERLINE_TEXT_FLAG

                                // --- Check 4: Is the task urgent, if so, how urgent? ---
                                val dueDate = MainActivity.SIMPLE_DF.parse(taskObject.getTaskChecklistDate())?.toInstant()
                                if(dueDate != null && todayDate != null) {
                                    btnColor = when (Duration.between(todayDate.toInstant(), dueDate).toDays()){
                                        // --- Check 5.1: Is the task due past 7 days ---
                                        in 8 .. Int.MAX_VALUE -> settings.getColor("weekPlus")
                                        // --- Check 5.2: Is the task due in 3 to 7 days ---
                                        in 4 .. 7 -> settings.getColor("week")
                                        // --- Check 5.3: Is the task due in 2 to 3 days ---
                                        in 2 .. 3 -> settings.getColor("threeDays")
                                        // --- Check 5.3: Is the task due tomorrow ---
                                        1L -> settings.getColor("tomorrow")
                                        // --- Check 5.4: Is the task due today ---
                                        0L -> settings.getColor("today")
                                        // --- Check 5.4: Is the task overdue ---
                                        else -> settings.getColor("overdue")
                                    }
                                }
                            }
                            dynamicButton.setBackgroundResource(btnBackgroundResource)
                        }
                    }
                    catch (e :Exception){
                        println("___ Error: $e ___")
                    }
                }
                setForegroundColorOfButton(c, dynamicButton, btnBackgroundResource, btnColor)
            }
        }

        fun setButtonToLayout(layout: ConstraintLayout, dynamicButton: Button, topId: Int){
            println("Setting ${dynamicButton.text} to layout - topId: $topId")
            // add Button to Layout
            layout.addView(dynamicButton)
            val constraintSet = ConstraintSet()
            constraintSet.clone(layout)
            constraintSet.connect(dynamicButton.id, ConstraintSet.START, layout.id, ConstraintSet.START, 0)
            constraintSet.connect(dynamicButton.id, ConstraintSet.END, layout.id, ConstraintSet.END, 0)
            if(topId == 0)
                constraintSet.connect(dynamicButton.id, ConstraintSet.TOP, topId, ConstraintSet.TOP, 0)
            else
                constraintSet.connect(dynamicButton.id, ConstraintSet.TOP, topId, ConstraintSet.BOTTOM, 30)

            constraintSet.applyTo(layout)
        }

        // --- Task Button UI Functions --- //
        fun createTaskButtonList(c: Context, layout: ConstraintLayout, taskList: List<TaskModal>, whereClause: String, navCtrl: NavController){
            println("+++ createTaskButtonList: $taskList +++")
            var lastBtnId = 0

            // Clear all views from layout and add all buttons from contents
            layout.removeAllViews()
            taskList.forEach {
                lastBtnId = createTaskButton(c, layout, lastBtnId, it, whereClause, navCtrl)
            }
        }

        private fun createTaskButton(c: Context, layout: ConstraintLayout, topId: Int, taskObject: TaskModal, whereClause: String,  navCtrl: NavController) : Int {
            println("+++ createTaskButton: \n$taskObject +++")

            //Extract ID and name from the content line
            val taskName = taskObject.getTaskName()
            val dynamicButton = createGenericButton(c, taskName)
            setButtonBackground(c, taskObject, dynamicButton)
            setButtonToLayout(layout, dynamicButton, topId)

            //Set onLongClick listener to change complete status of the task
            dynamicButton.setOnLongClickListener{
                if(!taskObject.getTaskCompletedFlag()) {
                    taskObject.setTaskCompletedFlag(true)
                    taskObject.setTaskCompletedDate(LocalDate.now().format(MainActivity.DATE_FORMAT))
                }
                else {
                    taskObject.setTaskCompletedFlag(false)
                    taskObject.setTaskCompletedDate("")
                }
                DBHandler(c).addEditTask(c, taskName, taskObject, true)
                createTaskButtonList(c, layout, DBHandler(c).readTaskList(whereClause, true, false), whereClause, navCtrl)
                true
            }

            //Set onclick listener to send to task edit page
            dynamicButton.setOnClickListener {

                val contextName = DBHandler(c).readContextList(false).filter { it.id == taskObject.contextId }[0].getContextName()
                val taskObjectBundle = bundleOf("taskObjectId" to taskObject.id, "contextName" to contextName)

                //Navigate depending on current fragment
                val navActionId = when(navCtrl.currentDestination?.id){
                    R.id.TaskList -> R.id.action_TaskList_to_TaskAddEdit
                    R.id.TaskArchive -> R.id.action_TaskArchive_to_TaskAddEdit
                    R.id.MasterList -> R.id.action_MasterList_to_TaskAddEdit
                    else -> R.id.action_TaskList_to_TaskAddEdit
                }
                navCtrl.navigate(navActionId, taskObjectBundle)

            }
            return dynamicButton.id
        }

        private fun setForegroundColorOfButton(c: Context, dynamicButton: Button, btnBackgroundResource: Int, color: String){
            val btnLayerDrawable = AppCompatResources.getDrawable(c, btnBackgroundResource) as LayerDrawable
            val foregroundDrawable = btnLayerDrawable.findDrawableByLayerId(R.id.foregroundDrawable)


            when (foregroundDrawable) {
                is ShapeDrawable -> { foregroundDrawable.paint.color = Color.parseColor(color) }
                is GradientDrawable -> { foregroundDrawable.setColor(Color.parseColor(color)) }
                is ColorDrawable -> { foregroundDrawable.color = Color.parseColor(color) }
            }

            dynamicButton.background = btnLayerDrawable
        }
    }
}
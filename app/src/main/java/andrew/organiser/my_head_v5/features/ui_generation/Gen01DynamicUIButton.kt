package andrew.organiser.my_head_v5.features.ui_generation

//noinspection SuspiciousImport
import andrew.organiser.my_head_v5.MainActivity
import andrew.organiser.my_head_v5.data_objects.TaskObject
import andrew.organiser.my_head_v5.features.data_manipulation.D02SettingsList
import andrew.organiser.my_head_v5.features.data_manipulation.D05SubTaskList
import android.R
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import java.lang.Math.round
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


class Gen01DynamicUIButton {

    companion object {


        private val emptyColor = Color.parseColor("#22000000")
        private val separatorColor = Color.parseColor("#22000000")

        // --- Generate Dynamic UI Buttons --- //
        fun createUIButtonLayout(c: Context, btnText : String, topId: Int, taskObject: TaskObject?) : ConstraintLayout{
            println("=== Gen01 - Create UI Button Layout: $btnText ===")
            //Create the overall UI layout containing the button and add the formatted button
            val dynamicButtonLayout = setDefaultButtonLayoutParams(ConstraintLayout(c))
            val dynamicButton = setDefaultButtonTextParams(Button(c), btnText, taskObject)
            dynamicButtonLayout.addView(dynamicButton)

            //If this is a task and the time progress bar is active, add a time progress bar to the layout
            if(taskObject != null){
                //Assign time progress bar if feature is activated
                var timeProgressBar: ProgressBar? = null
                if(D02SettingsList.getTaskFeatureStatus("TimeProgress")){
                    timeProgressBar = createGenericProgressBar(c)
                    timeProgressBar = createTimeProgressBar(timeProgressBar, taskObject)
                }

                //Assign checklist progress bar if feature is activated
                var checklistProgressBar: ProgressBar? = null
                if(D02SettingsList.getTaskFeatureStatus("ChecklistProgress") && taskObject.checklistFlag){
                    checklistProgressBar = createGenericProgressBar(c)
                    checklistProgressBar = createChecklistProgressBar(checklistProgressBar, taskObject)
                }
                if(!taskObject.completedFlag && (timeProgressBar != null || checklistProgressBar != null)){
                    val conditionActive = (D02SettingsList.getTaskFeatureStatus("Conditions") && taskObject.conditionActiveFlag == true)
                    setButtonAndProgressBarsToLayout(dynamicButtonLayout, timeProgressBar, checklistProgressBar, conditionActive)
                }
            }

            setButtonLayoutToList(layout, dynamicButtonLayout, topId)

            return dynamicButtonLayout
        }

        private fun setDefaultButtonLayoutParams(dynamicButtonLayout: ConstraintLayout) : ConstraintLayout{
            //println("Process: Set default button layout params") //Process Line
            val params = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(15, 15, 15, 15)
            dynamicButtonLayout.layoutParams = params
            dynamicButtonLayout.id = View.generateViewId()

            return dynamicButtonLayout
        }

        private fun setDefaultButtonTextParams(dynamicButton: Button, btnText: String, taskObject: TaskObject?) : Button{
            //println("Process: Set default button text params: ${btnText}") //Process Line
            val buttonHeight = if(taskObject != null && (D02SettingsList.getTaskFeatureStatus("TimeProgress") || D02SettingsList.getTaskFeatureStatus("ChecklistProgress"))) 220 else 200
            val params = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, buttonHeight)
            dynamicButton.layoutParams = params

            dynamicButton.setPadding(0, 0, 0, 20)
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

        private fun createGenericProgressBar(c: Context) : ProgressBar{
            //println("Process: Set generic progress bar for task") //Process line
            val params = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 10)
            params.setMargins(0, 0, 0, 0)
            val progressBar = ProgressBar(c, null, R.attr.progressBarStyleHorizontal)
            progressBar.layoutParams = params
            progressBar.isIndeterminate = false
            progressBar.min = 0
            progressBar.max = 100
            progressBar.elevation = 10F
            progressBar.id = View.generateViewId()



            return progressBar
        }

        private fun createTimeProgressBar(timePB: ProgressBar, taskObject: TaskObject) : ProgressBar{
            //println("Process: Set progress time bar for task") //Process line
            timePB.progressTintList = ColorStateList.valueOf(Color.parseColor(D02SettingsList.getUIColor("timePB")))

            //Set progress of bar based on task start and end dates/times provided
            val startDate = LocalDate.parse(taskObject.startDate, MainActivity.DATE_FORMAT)
            val endDate = LocalDate.parse(taskObject.endDate, MainActivity.DATE_FORMAT)
            val taskTimeSet = taskObject.startTime.isNotEmpty() && taskObject.endTime.isNotEmpty()
            val taskDayRange = Duration.between(startDate.atStartOfDay(), endDate.atStartOfDay()).toDays() + 1
            var taskHourRange = Duration.between(startDate.atStartOfDay(), endDate.atStartOfDay()).toHours()
            var currentHourPeriod = Duration.between(startDate.atStartOfDay(), LocalDate.now().atStartOfDay()).toHours()
            if(taskTimeSet){
                val startTime = LocalTime.parse(taskObject.startTime, MainActivity.TIME_FORMAT)
                val endTime = LocalTime.parse(taskObject.endTime, MainActivity.TIME_FORMAT)
                taskHourRange = Duration.between(startDate.atTime(startTime), endDate.atTime(endTime)).toHours()
                currentHourPeriod = Duration.between(startDate.atTime(startTime), LocalDateTime.now()).toHours()
            }

            if(taskHourRange > 0){
                val progress = (100*currentHourPeriod)/taskHourRange

                //Create time divisions based on the task date range
                val fillColor = Color.parseColor(D02SettingsList.getUIColor("timePB"))
                val emptyColor = Color.parseColor("#22000000")
                val separatorColor = Color.parseColor("#22000000")
                timePB.progressDrawable = Gen10SegmentedProgressBar(taskDayRange, fillColor, emptyColor, separatorColor)

                timePB.progress = progress.toInt()
            }
            else if(currentHourPeriod > 0) timePB.progress = 100

            return timePB
        }

        private fun createChecklistProgressBar(checklistPB: ProgressBar, taskObject: TaskObject) : ProgressBar{
            //println("Process: Set progress checklist bar for task") //Process line
            checklistPB.progressTintList = ColorStateList.valueOf(Color.parseColor(D02SettingsList.getUIColor("checklistPB")))

            //Set progress of bar based on task checklist items completed
            var checklistProgress = 0
            val subtaskList = D05SubTaskList.read(taskObject.id)
            if(subtaskList.isNotEmpty()){
                //Create checklist divisions based on the sub-task items
                val fillColor = Color.parseColor(D02SettingsList.getUIColor("checklistPB"))
                checklistPB.progressDrawable = Gen10SegmentedProgressBar(subtaskList.size.toLong(), fillColor, emptyColor, separatorColor)

                for(subtask in subtaskList){
                    if(subtask.completedFlag) checklistProgress++
                }
                checklistProgress = round((checklistProgress*100/subtaskList.size).toDouble()).toInt()
            }
            else checklistProgress = 100
            if(checklistProgress > 100) checklistProgress = 100

            checklistPB.progress = checklistProgress
            return checklistPB
        }

        private fun setButtonLayoutToList(layout: ConstraintLayout, dynamicButtonLayout: ConstraintLayout, topId: Int){
            //println("Process: Set Button Layout ${dynamicButtonLayout.id} to List layout ${layout.id} with topID: ${topId}") //Process Line
            layout.addView(dynamicButtonLayout)
            val constraintSet = ConstraintSet()
            constraintSet.clone(layout)
            constraintSet.connect(dynamicButtonLayout.id, ConstraintSet.START, layout.id, ConstraintSet.START, 0)
            constraintSet.connect(dynamicButtonLayout.id, ConstraintSet.END, layout.id, ConstraintSet.END, 0)
            if(topId == 0)
                constraintSet.connect(dynamicButtonLayout.id, ConstraintSet.TOP, topId, ConstraintSet.TOP, 0)
            else
                constraintSet.connect(dynamicButtonLayout.id, ConstraintSet.TOP, topId, ConstraintSet.BOTTOM, 20)

            constraintSet.applyTo(layout)
        }

        private fun setButtonAndProgressBarsToLayout(layout: ConstraintLayout, timeProgressBar: ProgressBar?, checklistPB: ProgressBar?, conditional: Boolean?){
            //println("Process: Set Button ${dynamicButton.text} and progress bar to Button layout ${layout.id}") //Process line
            if(timeProgressBar != null && (conditional == null || !conditional)) layout.addView(timeProgressBar)
            if(checklistPB != null) layout.addView(checklistPB)

            val constraintSet = ConstraintSet()
            constraintSet.clone(layout)
            if(timeProgressBar != null && (conditional == null || !conditional)){
                constraintSet.connect(timeProgressBar.id, ConstraintSet.START, layout.id, ConstraintSet.START, 30)
                constraintSet.connect(timeProgressBar.id, ConstraintSet.END, layout.id, ConstraintSet.END, 30)
                constraintSet.connect(timeProgressBar.id, ConstraintSet.BOTTOM, layout.id, ConstraintSet.BOTTOM, 15)
            }

            if(checklistPB != null){
                constraintSet.connect(checklistPB.id, ConstraintSet.START, layout.id, ConstraintSet.START, 30)
                constraintSet.connect(checklistPB.id, ConstraintSet.END, layout.id, ConstraintSet.END, 30)
                constraintSet.connect(checklistPB.id, ConstraintSet.BOTTOM, layout.id, ConstraintSet.BOTTOM, 30)
            }

            constraintSet.applyTo(layout)
        }
    }
}
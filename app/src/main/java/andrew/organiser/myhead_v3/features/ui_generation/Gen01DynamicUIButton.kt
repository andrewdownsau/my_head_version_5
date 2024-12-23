package andrew.organiser.myhead_v3.features.ui_generation

import andrew.organiser.myhead_v3.MainActivity
import andrew.organiser.myhead_v3.features.crud.SettingsListCURD
import andrew.organiser.myhead_v3.features.crud.SubTaskListCURD
import andrew.organiser.myhead_v3.modals.TaskModal
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
import java.time.Duration
import java.time.LocalDate


class GenerateDynamicUIButton {

    companion object {

        // --- Generate Dynamic UI Buttons --- //
        fun createUIButtonLayout(c: Context, layout: ConstraintLayout, btnText : String, topId: Int, taskModal: TaskModal?) : ConstraintLayout{
            println("=== Gen01 - Create UI Button Layout: $btnText ===")
            //Create the overall UI layout containing the button and add the formatted button
            val dynamicButtonLayout = setDefaultButtonLayoutParams(ConstraintLayout(c))
            val dynamicButton = setDefaultButtonTextParams(c, Button(c), btnText, taskModal)
            dynamicButtonLayout.addView(dynamicButton)

            //If this is a task and the time progress bar is active, add a time progress bar to the layout
            if(taskModal != null){
                val settingsColorsRead = SettingsListCURD.main(c, "Read", "UI_Colors", null)
                if(settingsColorsRead.first && settingsColorsRead.second != null) {
                    val settingsColorStrList = settingsColorsRead.second!!

                    //Assign time progress bar if feature is activated
                    var timeProgressBar: ProgressBar? = null
                    if(TaskAddEditFeatures.getFeatureSetting(c, "TimeProgress")){
                        timeProgressBar = createGenericProgressBar(c)
                        timeProgressBar = createTimeProgressBar(timeProgressBar, taskModal, settingsColorStrList)
                    }

                    //Assign checklist progress bar if feature is activated
                    var checklistProgressBar: ProgressBar? = null
                    if(TaskAddEditFeatures.getFeatureSetting(c, "ChecklistProgress") && taskModal.getTaskChecklist()){
                        checklistProgressBar = createGenericProgressBar(c)
                        checklistProgressBar = createChecklistProgressBar(c, checklistProgressBar, taskModal, settingsColorStrList)
                    }
                    setButtonAndProgressBarsToLayout(dynamicButtonLayout, dynamicButton, timeProgressBar, checklistProgressBar)
                }
            }

            setButtonLayoutToList(layout, dynamicButtonLayout, topId)

            return dynamicButtonLayout
        }

        private fun setDefaultButtonLayoutParams(dynamicButtonLayout: ConstraintLayout) : ConstraintLayout{
            println("__Set default button layout params__")
            val params = ConstraintLayout.LayoutParams(1000, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(15, 15, 15, 15)
            dynamicButtonLayout.setLayoutParams(params)
            dynamicButtonLayout.id = View.generateViewId()

            return dynamicButtonLayout
        }

        private fun setDefaultButtonTextParams(c: Context, dynamicButton: Button, btnText: String, taskModal: TaskModal?) : Button{
            println("__Set default button text params: ${btnText}__")
            val buttonHeight = if(taskModal != null && (TaskAddEditFeatures.getFeatureSetting(c, "TimeProgress") || TaskAddEditFeatures.getFeatureSetting(c, "ChecklistProgress"))) 220 else 200
            val params = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, buttonHeight)
            dynamicButton.setLayoutParams(params)

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
            println("__Set generic progress bar for task__")
            val params = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 0, 0, 0)
            val progressBar = ProgressBar(c, null, R.attr.progressBarStyleHorizontal)
            progressBar.setLayoutParams(params)
            progressBar.isIndeterminate = false
            progressBar.min = 0
            progressBar.max = 100
            progressBar.elevation = 10F
            progressBar.id = View.generateViewId()

            return progressBar
        }

        private fun createTimeProgressBar(timePB: ProgressBar, taskModal: TaskModal, settingsColorStrList: List<String>) : ProgressBar{
            println("__Set progress time bar for task__")
            timePB.progressTintList = ColorStateList.valueOf(Color.parseColor(UIButtonColorAssign.getColorFromSettings(settingsColorStrList, "timePB")))

            //Set progress of bar based on task start and end dates provided
            val startDate = LocalDate.parse(taskModal.getTaskStartDate(), MainActivity.DATE_FORMAT)
            val dueDate = LocalDate.parse(taskModal.getTaskDueDate(), MainActivity.DATE_FORMAT)
            val taskDateRange = Duration.between(startDate.atStartOfDay(), dueDate.atStartOfDay()).toDays()
            val currentDatePeriod = Duration.between(startDate.atStartOfDay(), LocalDate.now().atStartOfDay()).toDays()
            if(taskDateRange > 0){
                val progress = (100*currentDatePeriod)/taskDateRange
                timePB.progress = progress.toInt()
            }
            else if(currentDatePeriod > 0) timePB.progress = 100

            return timePB
        }

        private fun createChecklistProgressBar(c: Context, checklistPB: ProgressBar, taskModal: TaskModal, settingsColorStrList: List<String>) : ProgressBar{
            println("__Set progress checklist bar for task__")
            checklistPB.progressTintList = ColorStateList.valueOf(Color.parseColor(UIButtonColorAssign.getColorFromSettings(settingsColorStrList, "checklistPB")))

            //Set progress of bar based on task checklist items completed
            var checklistProgress = 0
            val subtaskListRead = SubTaskListCURD.main(c, "Read", taskModal.id, null)
            if(subtaskListRead.first && !subtaskListRead.second.isNullOrEmpty()){
                val checklist = subtaskListRead.second!!
                for(subtask in checklist){
                    if(subtask.getSubtaskCompletedFlag()) checklistProgress += 100/checklist.size
                }
            }
            else checklistProgress = 100
            if(checklistProgress > 100) checklistProgress = 100

            checklistPB.progress = checklistProgress
            return checklistPB
        }

        private fun setButtonLayoutToList(layout: ConstraintLayout, dynamicButtonLayout: ConstraintLayout, topId: Int){
            println("__Set Button Layout ${dynamicButtonLayout.id} to List layout ${layout.id} with topID: ${topId}__")
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

        private fun setButtonAndProgressBarsToLayout(layout: ConstraintLayout, dynamicButton: Button, timeProgressBar: ProgressBar?, checklistPB: ProgressBar?){
            println("__Set Button ${dynamicButton.text} and progress bar to Button layout ${layout.id}__")
            if(timeProgressBar != null) layout.addView(timeProgressBar)
            if(checklistPB != null) layout.addView(checklistPB)

            val constraintSet = ConstraintSet()
            constraintSet.clone(layout)
            if(timeProgressBar != null){
                constraintSet.connect(timeProgressBar.id, ConstraintSet.START, layout.id, ConstraintSet.START, 30)
                constraintSet.connect(timeProgressBar.id, ConstraintSet.END, layout.id, ConstraintSet.END, 30)
                constraintSet.connect(timeProgressBar.id, ConstraintSet.BOTTOM, layout.id, ConstraintSet.BOTTOM, 0)
            }

            if(checklistPB != null){
                constraintSet.connect(checklistPB.id, ConstraintSet.START, layout.id, ConstraintSet.START, 30)
                constraintSet.connect(checklistPB.id, ConstraintSet.END, layout.id, ConstraintSet.END, 30)
                constraintSet.connect(checklistPB.id, ConstraintSet.BOTTOM, layout.id, ConstraintSet.BOTTOM, 15)
            }

            constraintSet.applyTo(layout)
        }
    }
}
package andrew.organiser.myhead_v3.features.ui_generation

import andrew.organiser.myhead_v3.MainActivity
import andrew.organiser.myhead_v3.R
import andrew.organiser.myhead_v3.features.crud.TaskListCURD
import andrew.organiser.myhead_v3.modals.TaskModal
import android.content.Context
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import java.time.LocalDate


class GenerateTaskUIList {

    companion object {

        //Global variables that can be reused in private functions
        private var currentTaskList: ArrayList<TaskModal> = ArrayList()

        // --- Generate Task UI Button List --- //
        fun main(c: Context, f:Fragment, layout: ConstraintLayout, taskList: ArrayList<TaskModal>){
            println("=== Gen04 - Generate Task UI Button List ===")
            var lastBtnId = 0

            // Clear all views from layout and add all buttons from contents
            layout.removeAllViews()

            //Use last button ID to set the top constraint of the next button
            for(taskObject in taskList){
                if (taskObject.getTaskName().isNotEmpty()) {
                    lastBtnId = generateTaskButton(c, f, layout, lastBtnId, taskObject)
                }
            }

            //Set task list for refresh use
            currentTaskList = taskList
        }

        fun updateTaskConditions(c: Context, taskObjectId: Int){
            //Remove task conditions if applicable
            for(currentTaskObject in currentTaskList){
                if(currentTaskObject.getTaskConditionId() == taskObjectId){
                    currentTaskObject.setTaskConditionId(0)
                    TaskListCURD.main(c, "Update", currentTaskObject.getTaskName(), currentTaskObject, null)
                }
            }
        }

        private fun generateTaskButton(c: Context, f: Fragment, layout: ConstraintLayout, lastBtnId: Int, taskObject: TaskModal) : Int{
            val taskName = taskObject.getTaskName()
            println("__Generate Task Button: ${taskName}__")

            //Get color of button based on task object status
            val dynamicButtonLayout = GenerateDynamicUIButton.createUIButtonLayout(c, layout, taskName, lastBtnId, taskObject)
            UIButtonColorAssign.main(c, "TaskColorAssign", taskObject, null, dynamicButtonLayout)

            //Set onLongClick listener to change complete status of the task
            val dynamicButton = dynamicButtonLayout.getChildAt(0) as Button
            dynamicButton.setOnLongClickListener{
                if(!taskObject.getTaskCompletedFlag()) {
                    taskObject.setTaskCompletedFlag(true)
                    taskObject.setTaskCompletedDate(LocalDate.now().format(MainActivity.DATE_FORMAT))
                    updateTaskConditions(c, taskObject.id)
                }
                else {
                    taskObject.setTaskCompletedFlag(false)
                    taskObject.setTaskCompletedDate("")
                }

                //Update task list and reset UI generation
                TaskListCURD.main(c, "Update", taskName, taskObject, null)
                currentTaskList = currentTaskList.filter { it.id != taskObject.id } as ArrayList<TaskModal>
                currentTaskList.add(taskObject)
                val sortedTaskList = SortUIList.main(c, "sortTaskList", null, currentTaskList).second
                if (sortedTaskList != null) { main(c, f, layout, sortedTaskList) }
                true
            }

            //Set onclick listener to send to task edit page
            dynamicButton.setOnClickListener {
                val taskBundle = bundleOf("taskObjectId" to taskObject.id)
                findNavController(f).navigate(R.id.action_TaskList_to_TaskAddEdit, taskBundle)
            }

            //Return button id for next button position
            return dynamicButtonLayout.id
        }
    }
}
package andrew.organiser.my_head_v5.features.ui_generation

import andrew.organiser.my_head_v5.MainActivity
import andrew.organiser.my_head_v5.R
import andrew.organiser.my_head_v5.features.data_manipulation.D04TaskList
import andrew.organiser.my_head_v5.features.data_manipulation.D07OrderUIList
import andrew.organiser.my_head_v5.data_objects.TaskObject
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import java.time.LocalDate


class Gen04TaskUIList {

    companion object {

        // --- Generate Task UI Button List --- //
        fun main(c: Context, f:Fragment, layout: ConstraintLayout, subtitle: TextView, filterKey: String, contextId: Int?){
            println("=== Gen04 - Generate Task UI Button List ===")
            var lastBtnId = 0

            // Clear all views from layout and add all buttons from contents
            layout.removeAllViews()

            val taskList = D04TaskList.read(filterKey, contextId)
            if (taskList.isEmpty()) {
                layout.addView(subtitle)
                subtitle.visibility = View.VISIBLE
            }
            else {
                subtitle.visibility = View.GONE

                //Sort and add background color status
                val sortedTaskList = D07OrderUIList.sortTaskList(taskList)

                //Use last button ID to set the top constraint of the next button
                for(taskObject in sortedTaskList){
                    if (taskObject.name.isNotEmpty()) {
                        lastBtnId = generateTaskButton(c, f, layout, subtitle, lastBtnId, taskObject, filterKey, contextId)
                    }
                }
            }
        }

        private fun generateTaskButton(c: Context, f: Fragment, layout: ConstraintLayout, subtitle: TextView, lastBtnId: Int, task: TaskObject, filterKey: String, contextId: Int?) : Int{
            //Get color of button based on task object status
            val dynamicButtonLayout = Gen01DynamicUIButton.createUIButtonLayout(c, layout, task.name, lastBtnId, task)
            Gen05UIButtonColorAssign.main(c, "TaskColorAssign", task, null, dynamicButtonLayout)

            //Set onLongClick listener to change complete status of the task
            val dynamicButton = dynamicButtonLayout.getChildAt(0) as Button
            dynamicButton.setOnLongClickListener{
                if(!task.completedFlag) {
                    task.completedFlag = true
                    task.completedDate = LocalDate.now().format(MainActivity.DATE_FORMAT)
                }
                else {
                    task.completedFlag = false
                    task.completedDate = ""
                }
                D04TaskList.updateConditionStatuses(c, task.id, task.completedFlag, !task.repeatFlag)

                //Update task list and reset UI generation
                if(D04TaskList.save(c, task, task.name)){
                    main(c, f, layout, subtitle, filterKey, contextId)
                }
                true
            }

            //Set onclick listener to send to task edit page
            dynamicButton.setOnClickListener {
                f.onSaveInstanceState(bundleOf("taskListTypeActive" to filterKey.contains("_Active")))
                val taskBundle = bundleOf("taskObjectId" to task.id)
                findNavController(f).navigate(R.id.action_TaskList_to_TaskAddEdit, taskBundle)
            }

            //Return button id for next button position
            return dynamicButtonLayout.id
        }
    }
}
package andrew.organiser.myhead_v3.features.ui_generation

import andrew.organiser.myhead_v3.MainActivity
import andrew.organiser.myhead_v3.R
import andrew.organiser.myhead_v3.features.crud.SubTaskListCURD
import android.content.Context
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentActivity
import java.time.LocalDate


class TaskAddEditCheckBoxes {

    companion object {

        // --- Task Add Edit Checkbox Listeners --- //
        fun main(c: Context, f: FragmentActivity?, checkBox: CheckBox, viewList: ArrayList<View>, taskObjectId: Int?){
            println("=== Gen09 - Task Add Edit Checkbox Listeners for: ${checkBox.tag} ===")
            when(checkBox.tag){
                "tag_complete" -> setCompletedWidgetsState(c, viewList[0] as EditText, checkBox.isChecked)
                "tag_checklist" -> setChecklistWidgetsState(c, f!!, viewList[0] as ConstraintLayout, viewList[1] as NestedScrollView, checkBox.isChecked, taskObjectId)
                "tag_repeat" -> setRepeatWidgetsState(checkBox.isChecked, viewList)
            }

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                when(checkBox.tag){
                    "tag_complete" -> setCompletedWidgetsState(c, viewList[0] as EditText, isChecked)
                    "tag_checklist" -> setChecklistWidgetsState(c, f!!, viewList[0] as ConstraintLayout, viewList[1] as NestedScrollView, isChecked, taskObjectId)
                    "tag_repeat" -> setRepeatWidgetsState(isChecked, viewList)
                }
            }
        }

        private fun setCompletedWidgetsState(c: Context, completedDateEdit: EditText, isChecked: Boolean){
            if(isChecked) {
                completedDateEdit.visibility = View.VISIBLE
                completedDateEdit.setText(LocalDate.now().format(MainActivity.DATE_FORMAT))
            }
            else {
                completedDateEdit.visibility = View.GONE
                completedDateEdit.setText(c.getString(R.string.blank_label))
            }
        }

        private fun setChecklistWidgetsState(c: Context, f: FragmentActivity, layout: ConstraintLayout, scrollView: NestedScrollView, isChecked: Boolean, taskObjectId: Int?){
            // Clear all views from layout and add all buttons from contents
            layout.removeAllViews()

            if(isChecked) {
                scrollView.visibility = View.VISIBLE

                //Display all subtasks applicable to this list
                if(taskObjectId != null){
                    val subtaskListRead = SubTaskListCURD.main(c, "Read", taskObjectId, null)
                    if(subtaskListRead.first){
                        SubTaskUIList.createSubTaskList(c, f, layout, subtaskListRead.second, taskObjectId)
                    }
                }
                else SubTaskUIList.createSubTaskList(c, f, layout, null, null)
            }
            else {
                scrollView.visibility = View.GONE
            }
        }

        private fun setRepeatWidgetsState(isChecked: Boolean, viewList: ArrayList<View>){
            val visibility = if(isChecked) View.VISIBLE
            else View.GONE

            for(view in viewList){
                view.visibility = visibility
            }
        }

    }
}
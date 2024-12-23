package andrew.organiser.myhead_v3.features.ui_generation


import andrew.organiser.myhead_v3.MainActivity
import andrew.organiser.myhead_v3.R
import andrew.organiser.myhead_v3.modals.SubtaskModal
import android.content.Context
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.time.LocalDate


class SubTaskUIList {

    companion object {

        private var lastChecklistId = 0
        private var globalSubtaskList: MutableList<SubtaskModal> = mutableListOf()
        private val subtaskWidgetIdList: MutableList<Array<Int>> = mutableListOf()

        // --- Sort UI Button List --- //
        fun createSubTaskList(c: Context, f: FragmentActivity, layout: ConstraintLayout, subtaskList: List<SubtaskModal>?, taskId: Int?){
            println("=== Gen10 - Create Subtask Button List for Task: $taskId ===")

            //Purge the global lists to avoid conflicts
            globalSubtaskList.clear()
            subtaskWidgetIdList.clear()

            //Create subtask items for checklist scrollview
            if(!subtaskList.isNullOrEmpty()){
                subtaskList.forEach {
                    lastChecklistId = createSubtaskItem(c, f, layout, it, taskId)
                    globalSubtaskList.add(it)
                }
            }

            //Create subtask adder at end of the list
            lastChecklistId = createSubtaskItem(c, f, layout, null, taskId)
        }

        private fun createSubtaskItem(c: Context, f: FragmentActivity, layout: ConstraintLayout, subtaskObject: SubtaskModal?, taskId: Int?) : Int {
            println("___ Create Subtask Item ___")

            //Set parameters for item name
            val checkItemName = createGenericSubtaskNameEditText(c)
            layout.addView(checkItemName)

            //Set parameters for item due date
            val checkItemDate = createGenericSubtaskDateEditText(c, f, checkItemName.id)
            layout.addView(checkItemDate)

            //Set parameters for item completed check
            val completedCheckbox = createGenericSubtaskCheckBox(c, subtaskObject != null, checkItemName.id)
            layout.addView(completedCheckbox)

            //If subtask is not null, set values using subtask modal
            if(subtaskObject != null){
                checkItemName.setText(subtaskObject.getSubtaskName())
                checkItemDate.setText(subtaskObject.getSubtaskDueDate())
                completedCheckbox.isChecked = subtaskObject.getSubtaskCompletedFlag()

                //Set parameters for remove button
                val editCheckRemove = createGenericSubtaskButton(c, R.drawable.ic_menu_delete, subtaskObject.id)
                layout.addView(editCheckRemove)
                setOnClickRemoveChecklistItem(layout, editCheckRemove, checkItemName, checkItemDate, completedCheckbox)

                setSubtaskConstraints(layout, checkItemName, checkItemDate, completedCheckbox, editCheckRemove, lastChecklistId)
            }
            else{
                //Set parameters for add button
                val editCheckAdd = createGenericSubtaskButton(c, R.drawable.ic_menu_add, checkItemName.id)
                layout.addView(editCheckAdd)
                setOnClickAddChecklistItem(c, f, layout, checkItemName, checkItemDate, completedCheckbox, editCheckAdd, taskId)

                setSubtaskConstraints(layout, checkItemName, checkItemDate, completedCheckbox, editCheckAdd, lastChecklistId)
            }

            return checkItemName.id
        }

        private fun createGenericSubtaskNameEditText(c: Context) : EditText{
            //Set parameters for item name
            val checkItemName = EditText(c)
            checkItemName.id = View.generateViewId()
            val params = ConstraintLayout.LayoutParams(0, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            checkItemName.setLayoutParams(params)
            checkItemName.hint = "Enter checklist item"
            checkItemName.minLines = 1
            checkItemName.maxLines = 2
            checkItemName.filters = arrayOf<InputFilter>(LengthFilter(40))
            checkItemName.gravity = Gravity.BOTTOM
            checkItemName.inputType += InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            checkItemName.setTextColor(c.getColor(R.color.sub_content))

            return checkItemName
        }

        private fun createGenericSubtaskDateEditText(c: Context, f: FragmentActivity, nameEditTextId: Int) : EditText{
            //Set parameters for item date
            val checkItemDate = EditText(c)
            checkItemDate.id = View.generateViewId()
            val params = ConstraintLayout.LayoutParams(260, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            checkItemDate.setLayoutParams(params)
            checkItemDate.hint = "Due date"
            checkItemDate.setTextColor(c.getColor(R.color.sub_content))
            checkItemDate.focusable = View.NOT_FOCUSABLE
            checkItemDate.tag = nameEditTextId
            TaskAddEditListeners.main(c, "setDateListeners", f, arrayListOf(checkItemDate), null)

            return checkItemDate
        }

        private fun createGenericSubtaskCheckBox(c: Context, enabledFlag: Boolean, nameEditTextId: Int) : CheckBox{
            //Set parameters for item completed check
            val completedCheckbox = CheckBox(c)
            completedCheckbox.id = View.generateViewId()
            val params3 = ConstraintLayout.LayoutParams(80, 80)
            completedCheckbox.setLayoutParams(params3)
            completedCheckbox.isEnabled = enabledFlag
            completedCheckbox.tag = nameEditTextId
            return completedCheckbox
        }

        private fun createGenericSubtaskButton(c: Context, backgroundResource: Int, buttonTagId: Int) : Button{
            //Set parameters for add button
            val subtaskButton = Button(c)
            subtaskButton.id = View.generateViewId()
            val params4 = ConstraintLayout.LayoutParams(100, 100)
            subtaskButton.setLayoutParams(params4)
            subtaskButton.background = ContextCompat.getDrawable(c, backgroundResource)
            subtaskButton.setTextColor(c.getColor(R.color.sub_content))
            subtaskButton.tag = buttonTagId

            return subtaskButton
        }

        private fun setSubtaskConstraints(layout: ConstraintLayout, nameEditText: EditText, dateEditText: EditText, checkBox: CheckBox, button: Button, lastChecklistId:Int){
            println("+++ Setting subtask constraints with lastChecklistId = $lastChecklistId +++")
            val constraintSet = ConstraintSet()
            constraintSet.clone(layout)
            constraintSet.createHorizontalChain(ConstraintSet.PARENT_ID, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, intArrayOf(nameEditText.id, dateEditText.id, checkBox.id, button.id), floatArrayOf(1F,0F,0F,0F), ConstraintSet.CHAIN_SPREAD)

            if(lastChecklistId == 0){
                constraintSet.connect(nameEditText.id, ConstraintSet.TOP, layout.id, ConstraintSet.TOP, 0)
            }
            else{
                constraintSet.connect(nameEditText.id, ConstraintSet.TOP, lastChecklistId, ConstraintSet.BOTTOM, 30)
            }
            constraintSet.connect(nameEditText.id, ConstraintSet.START, layout.id, ConstraintSet.START, 0)
            constraintSet.connect(nameEditText.id, ConstraintSet.END, dateEditText.id, ConstraintSet.START, 0)

            constraintSet.connect(dateEditText.id, ConstraintSet.BOTTOM, nameEditText.id, ConstraintSet.BOTTOM, 0)
            constraintSet.connect(dateEditText.id, ConstraintSet.START, nameEditText.id, ConstraintSet.END, 0)
            constraintSet.connect(dateEditText.id, ConstraintSet.END, checkBox.id, ConstraintSet.START, 0)

            constraintSet.connect(checkBox.id, ConstraintSet.BOTTOM, nameEditText.id, ConstraintSet.BOTTOM, 40)
            constraintSet.connect(checkBox.id, ConstraintSet.START, dateEditText.id, ConstraintSet.END, 0)
            constraintSet.connect(checkBox.id, ConstraintSet.END, button.id, ConstraintSet.START, 0)

            constraintSet.connect(button.id, ConstraintSet.BOTTOM, nameEditText.id, ConstraintSet.BOTTOM, 30)
            constraintSet.connect(button.id, ConstraintSet.START, dateEditText.id, ConstraintSet.END, 30)
            constraintSet.connect(button.id, ConstraintSet.END, layout.id, ConstraintSet.END, 0)

            constraintSet.applyTo(layout)

            //Additionally, set the ids into and array to add to the line id lists for the subtasks added
            subtaskWidgetIdList.add(arrayOf(nameEditText.id,dateEditText.id,checkBox.id))
        }

        private fun setOnClickAddChecklistItem(c: Context, f: FragmentActivity, layout: ConstraintLayout, editName: EditText, editDate: EditText, checkBox: CheckBox, button: Button, taskId:Int?){
            //Convert task id to default zero, cannot be null
            var usableTaskId = taskId
            if(usableTaskId == null) usableTaskId = 0

            button.setOnClickListener {
                //Add new sub task to the overall list if name is not empty
                val newSubtaskName = editName.text.toString()
                val newSubtaskDateStr = editDate.text.toString()
                if(newSubtaskName.isNotEmpty()){
                    //Change Add button to remove
                    button.background = ContextCompat.getDrawable(c, R.drawable.ic_menu_delete)
                    setOnClickRemoveChecklistItem(layout, button, editName, editDate, checkBox)

                    //Set the newId to the original adder editTextId
                    val newSubtaskModal = SubtaskModal(lastChecklistId, usableTaskId, newSubtaskName, newSubtaskDateStr, false)
                    lastChecklistId = createSubtaskItem(c, f, layout, null, taskId)
                    globalSubtaskList.add(newSubtaskModal)

                }
            }
        }

        private fun setOnClickRemoveChecklistItem(layout: ConstraintLayout, button: Button, nameEditText: EditText, dateEditText: EditText, checkBox: CheckBox){
            //Use the button tag to determine which subtask to remove from the list and layout
            button.setOnClickListener {
                println("___ Remove button Clicked: ${button.tag}___")
                //Get the ids of the subtask name widgets either above or below (if not only item)
                val subtaskIdList = mutableListOf<Int>()
                globalSubtaskList.forEach { subtask ->
                    //println("__SubtaskId = ${subtask.id}")
                    subtaskIdList.add(subtask.id) }
                val indexBtnTag = subtaskIdList.indexOf(button.tag)

                val subtaskNameIdList: ArrayList<Int> = ArrayList()
                subtaskWidgetIdList.forEach{widgetIdList ->
                    //println("__EditNameId = ${widgetIdList[1]}")
                    subtaskNameIdList.add(widgetIdList[0])}

                //If not the topmost itself then topId is the id above this subtask, otherwise topId = layoutId
                val topId = if(indexBtnTag > 0) subtaskNameIdList[indexBtnTag-1]
                else layout.id

                //If not the bottommost then bottomId is the id below this subtask, otherwise bottomId = lastChecklistId
                val bottomId = if(indexBtnTag != subtaskNameIdList.size-1) subtaskNameIdList[indexBtnTag+1]
                else lastChecklistId

                //Delete item from the subtask list
                globalSubtaskList.remove(globalSubtaskList.filter { it.id == button.tag }[0])
                subtaskWidgetIdList.removeAt(indexBtnTag)

                //Shift the constraints of the name edit text widgets of the top and bottom items
                println("topId: $topId and bottomId: $bottomId")
                val topMargin = if(topId == 0) 0 else 30
                val constraintSet = ConstraintSet()
                constraintSet.clone(layout)
                constraintSet.connect(bottomId, ConstraintSet.TOP, topId, ConstraintSet.BOTTOM, topMargin)
                constraintSet.applyTo(layout)

                //Remove line of widgets from layout
                layout.removeView(nameEditText)
                layout.removeView(dateEditText)
                layout.removeView(checkBox)
                layout.removeView(button)
            }
        }

        fun getEarliestChecklistDate(layout: ConstraintLayout, dueDateEdit: EditText) : String{
            var subtaskDate = LocalDate.now()
            var subtaskDateValid = false
            var subtaskChecked = false
            var earliestSubtaskDate = LocalDate.parse(dueDateEdit.text.toString(),
                MainActivity.DATE_FORMAT
            )
            subtaskWidgetIdList.forEach { widgetIdArray ->
                widgetIdArray.forEachIndexed { index, widgetId ->
                    if(layout.getViewById(widgetId) != null){
                        when(index){
                            1 -> {
                                val widget = layout.getViewById(widgetId) as EditText
                                val subtaskDateStr = widget.text.toString()
                                subtaskDateValid = subtaskDateStr.isNotEmpty()
                                if(subtaskDateValid)
                                    subtaskDate = LocalDate.parse(subtaskDateStr,
                                        MainActivity.DATE_FORMAT
                                    )
                            }
                            2 -> {
                                val widget = layout.getViewById(widgetId) as CheckBox
                                subtaskChecked = widget.isChecked
                            }
                        }
                    }
                }
                if(subtaskDate < earliestSubtaskDate && subtaskDateValid && !subtaskChecked) earliestSubtaskDate = subtaskDate
            }
            return earliestSubtaskDate.format(MainActivity.DATE_FORMAT)
        }

        fun getSaveSubtaskList(layout: ConstraintLayout, taskId: Int): ArrayList<SubtaskModal>{
            val updatedSubtaskList: ArrayList<SubtaskModal> = ArrayList()
            subtaskWidgetIdList.forEach { widgetIdArray ->
                var subtaskName = ""; var subtaskDate = ""; var checked = false
                widgetIdArray.forEachIndexed { index, widgetId ->
                    if(layout.getViewById(widgetId) != null){
                        when(index){
                            0 -> {val widget = layout.getViewById(widgetId) as EditText
                                subtaskName = widget.text.toString()}
                            1 -> {val widget = layout.getViewById(widgetId) as EditText
                                subtaskDate = widget.text.toString()}
                            2 -> {val widget = layout.getViewById(widgetId) as CheckBox
                                checked = widget.isChecked}
                        }
                    }
                }
                if(subtaskName.isNotEmpty()){
                    println("___Adding to updated list subtask: 0 $taskId $subtaskName $subtaskDate ${checked}___")
                    updatedSubtaskList.add(SubtaskModal(0, taskId, subtaskName, subtaskDate, checked))
                }
            }
            return updatedSubtaskList
        }
    }
}
package andrew.organiser.my_head_v5.features.ui_generation

import andrew.organiser.my_head_v5.MainActivity
import andrew.organiser.my_head_v5.features.data_manipulation.D04TaskList
import andrew.organiser.my_head_v5.features.data_manipulation.D05SubTaskList
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentActivity
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar
import java.util.TimeZone


class Gen06TaskAddEditListeners {

    companion object {


        // --- Global variables used throughout the active lifecycle of the application --- //
        private var currentContext = ""
        private var firstLoad = true

        // --- Task Add Edit Listeners --- //
        fun main(c: Context, f: FragmentActivity?, cmd: String, viewList: ArrayList<View>, taskObjectId: Int?){
            println("=== Gen06 - Task Add Edit Listeners: $cmd ===")
            when(cmd){
                "setSeekbarListener" -> setOnSeekbarChangeListener(viewList[0] as SeekBar, viewList[1] as TextView)
                "setContextListener" -> setContextSpinnerListener(c, viewList[0] as Spinner, viewList[1] as Spinner, taskObjectId)
                "setDateListeners" -> setDateListeners(f, viewList)
                "setTimeListeners" -> setTimeListeners(f, viewList)
                "setRepeatRadioListeners" -> setRepeatRadioListeners(viewList)
                "setCompleteFlag" -> setCompletedFlagListener(viewList[0] as CheckBox, viewList[1] as EditText)
                "setChecklistFlag" -> setChecklistFlagListener(c, f!!, viewList[0] as CheckBox, viewList[1] as ConstraintLayout, viewList[2] as NestedScrollView, taskObjectId)
                "setRepeatFlag" -> setRepeatFlagListener(viewList)
                "setRepeatType" -> setRepeatTypeListener(viewList)
                "setConditionListener" -> setConditionSpinnerListener(viewList[0] as Spinner, viewList[1] as ConstraintLayout)
            }
        }

        private fun setContextSpinnerListener(c: Context, contextSpinner: Spinner, conditionSpinner: Spinner, taskObjectId: Int?){
            contextSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    //If context is changed, adapt the condition task list accordingly
                    val newContextName = contextSpinner.selectedItem.toString()
                    if(newContextName != currentContext && !firstLoad){
                        Gen07TaskAddEditPopulation.populateConditionSpinner(c, newContextName, conditionSpinner, taskObjectId)
                        currentContext = newContextName
                    }
                    else if(firstLoad){
                        firstLoad = false
                        currentContext = newContextName
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        private fun setDateListeners(activity: FragmentActivity?, editTextList: ArrayList<View>){
            for(editText in editTextList.withIndex()){
                if(editText.index > 1){
                    editText.value.setOnClickListener {

                        val calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())

                        val dialog = DatePickerDialog(activity!!,
                            { _, selectedYear, selectedMonth, selectedDay ->
                                //Set the text to include 2 digits for month and day
                                val actualMonth = selectedMonth + 1
                                val dayText = if(selectedDay < 10) "0$selectedDay" else selectedDay.toString()
                                val monthText = if(actualMonth < 10) "0$actualMonth" else actualMonth.toString()
                                val newDisplayedDate = "$dayText/$monthText/${selectedYear-2000}"
                                (editText.value as EditText).setText(newDisplayedDate)

                                //Check start date if end date and do not accept any date before start
                                warningRedStateToDateTime(editTextList, true)
                            },
                            calendar[Calendar.YEAR], calendar[Calendar.MONTH],
                            calendar[Calendar.DAY_OF_MONTH]
                        )
                        dialog.show()
                    }
                }
            }
        }

        private fun setTimeListeners(activity: FragmentActivity?, editTextList: ArrayList<View>){
            for(editText in editTextList.withIndex()){
                if(editText.index == 0 || editText.index == 1){
                    editText.value.setOnClickListener {

                        val time = LocalTime.now()

                        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                            val displayHour = if(hour == 0) "12" else if(hour < 13) "$hour" else "${hour - 12}"
                            val displayMinute = if(minute < 10) "0$minute" else "$minute"
                            val displayAMPM = if(hour < 12) "AM" else "PM"
                            val newDisplayedTime = "$displayHour:$displayMinute $displayAMPM"
                            (editText.value as EditText).setText(newDisplayedTime)

                            //Check start and end time if dates are equal
                            warningRedStateToDateTime(editTextList, false)
                        }

                        TimePickerDialog(activity, timeSetListener, time.hour, time.minute, false).show()

                    }
                }
            }
        }

        private fun warningRedStateToDateTime(editTextList: ArrayList<View>, validateDate: Boolean){
            if(editTextList.size > 3){
                val startTimeEdit = editTextList[0] as EditText; val endTimeEdit = editTextList[1] as EditText
                val startDateEdit = editTextList[2] as EditText; val endDateEdit = editTextList[3] as EditText
                val timeTextColor = if(D04TaskList.validateTime(startTimeEdit, endTimeEdit, startDateEdit, endDateEdit)) "#8694B1" else "#ff0000"
                startTimeEdit.setTextColor(Color.parseColor(timeTextColor))
                endTimeEdit.setTextColor(Color.parseColor(timeTextColor))

                if(validateDate){
                    val dateTextColor = if(D04TaskList.validateDate(startDateEdit, endDateEdit)) "#8694B1" else "#ff0000"
                    startDateEdit.setTextColor(Color.parseColor(dateTextColor))
                    endDateEdit.setTextColor(Color.parseColor(dateTextColor))
                }
            }
        }

        private fun setOnSeekbarChangeListener(seekBar: SeekBar, valueText: TextView){
            //Set value text progress amount as a string in UI
            var seekbarProgress = seekBar.progress + 1
            valueText.text = seekbarProgress.toString()

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    // React to the value being set in seekBar
                    seekbarProgress = progress + 1
                    valueText.text = seekbarProgress.toString()
                }
                override fun onStartTrackingTouch(seekBar: SeekBar) { }

                override fun onStopTrackingTouch(seekBar: SeekBar) { }
            })
        }

        private fun setRepeatRadioListeners(radioButtons: ArrayList<View>){
            //Set the listeners so that clicking one button, clears the status of the others
            (radioButtons[0] as RadioButton).setOnCheckedChangeListener { _, isChecked ->
                if(isChecked) {
                    (radioButtons[1] as RadioButton).isChecked = false
                    (radioButtons[2] as RadioButton).isChecked = false
                }
            }
            (radioButtons[1] as RadioButton).setOnCheckedChangeListener { _, isChecked ->
                if(isChecked) {
                    (radioButtons[0] as RadioButton).isChecked = false
                    (radioButtons[2] as RadioButton).isChecked = false
                }
            }
            (radioButtons[2] as RadioButton).setOnCheckedChangeListener { _, isChecked ->
                if(isChecked) {
                    (radioButtons[0] as RadioButton).isChecked = false
                    (radioButtons[1] as RadioButton).isChecked = false
                }
            }
        }

        private fun setCompletedFlagListener(checkBox: CheckBox, completedDate: EditText){
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                val visibility = if(isChecked) View.VISIBLE else View.GONE
                val dateText = if(isChecked) LocalDate.now().format(MainActivity.DATE_FORMAT) else ""
                completedDate.visibility = visibility
                completedDate.setText(dateText)
            }
        }

        private fun setChecklistFlagListener(c: Context, f: FragmentActivity, checkBox: CheckBox, layout: ConstraintLayout, scrollView: NestedScrollView, taskObjectId: Int?){
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                // Clear all views from layout and add all buttons from contents
                layout.removeAllViews()

                if(isChecked) {
                    scrollView.visibility = View.VISIBLE

                    //Display all subtasks applicable to this list
                    val subtaskList = if(taskObjectId != null) D05SubTaskList.read(taskObjectId) else null
                    Gen09SubTaskUIList.createSubTaskList(c, f, layout, subtaskList, taskObjectId)
                }
                else {
                    scrollView.visibility = View.GONE
                }
            }
        }

        private fun setRepeatFlagListener(viewList: ArrayList<View>){
            val checkBox = viewList[0] as CheckBox
            val repeatActiveLayout = viewList[1] as ConstraintLayout
            repeatActiveLayout.visibility = if(checkBox.isChecked) View.VISIBLE else View.GONE
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                repeatActiveLayout.visibility = if(checkBox.isChecked) View.VISIBLE else View.GONE
            }
        }

        private fun setRepeatTypeListener(viewList: ArrayList<View>){
            //Set the listeners so that the radio buttons for type control the next 2 layouts
            val radioDay = viewList[0] as RadioButton
            val radioOther = viewList[1] as RadioButton

            val repeatDayLayout = viewList[2] as ConstraintLayout
            repeatDayLayout.visibility = if(radioDay.isChecked) View.VISIBLE else View.GONE
            val repeatOtherLayout = viewList[3] as ConstraintLayout
            repeatOtherLayout.visibility = if(radioOther.isChecked) View.VISIBLE else View.GONE

            radioDay.setOnCheckedChangeListener { _, isChecked ->
                repeatDayLayout.visibility = if(isChecked) View.VISIBLE else View.GONE
            }
            radioOther.setOnCheckedChangeListener { _, isChecked ->
                repeatOtherLayout.visibility = if(isChecked) View.VISIBLE else View.GONE
            }
        }

        private fun setConditionSpinnerListener(conditionSpinner: Spinner, changeStartLayout: ConstraintLayout){
            conditionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    //If condition has been changed to not be None, then change visibility of change start layout
                    val newConditionName = conditionSpinner.selectedItem.toString()
                    changeStartLayout.visibility = if(newConditionName != "None") View.VISIBLE else View.GONE
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }
}
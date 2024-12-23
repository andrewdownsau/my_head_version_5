package andrew.organiser.myhead_v3.features.ui_generation

import andrew.organiser.myhead_v3.MainActivity
import andrew.organiser.myhead_v3.R
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import java.time.LocalDate
import java.util.Calendar
import java.util.TimeZone


class TaskAddEditListeners {

    companion object {

        // --- Task Add Edit Listeners --- //
        fun main(c: Context, cmd: String, activity: FragmentActivity?, viewList: ArrayList<View>, taskObjectId: Int?){
            println("=== Gen08 - Task Add Edit Listeners ===")
            when(cmd){
                "setSeekbarListener" -> setOnSeekbarChangeListener(viewList[0] as SeekBar, viewList[1] as TextView)
                "setContextListener" -> setContextSpinnerListener(c, viewList[0] as Spinner, viewList[1] as Spinner, taskObjectId)
                "setDateListeners" -> setDateListeners(activity, viewList)
                "setRepeatRadioListeners" -> setRepeatRadioListeners(viewList)
            }
        }

        private fun setDateListeners(activity: FragmentActivity?, editTextList: ArrayList<View>){
            for(editText in editTextList.withIndex()){
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
                            if(editTextList.size > 1) validateDate(editTextList[0] as EditText, editTextList[1] as EditText)
                        },
                        calendar[Calendar.YEAR], calendar[Calendar.MONTH],
                        calendar[Calendar.DAY_OF_MONTH]
                    )
                    dialog.show()
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

        private fun setContextSpinnerListener(c: Context, contextSpinner: Spinner, conditionSpinner: Spinner, taskObjectId:Int?){
            contextSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val newContextName = contextSpinner.selectedItem.toString()
                    val newContextId = TaskAddEditPopulation.getContextIdFromName(newContextName)
                    val newConditionListPair = TaskAddEditPopulation.getConditionNameList(c, newContextId, null, taskObjectId)
                    TaskAddEditPopulation.populateSpinner(c, conditionSpinner, newConditionListPair.first, newConditionListPair.second)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
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

        fun validateDate(startDateEdit: EditText, dueDateEdit: EditText) : Boolean{
            //Compare start date to due date and validate
            println("--- ValidateDate TaskAddEdit ---")
            try{
                val startDate = LocalDate.parse(startDateEdit.text, MainActivity.DATE_FORMAT)
                val dueDate = LocalDate.parse(dueDateEdit.text, MainActivity.DATE_FORMAT)
                println("--- ValidateDate startDate: $startDate to dueDate: $dueDate ---")

                if(startDate.isBefore(dueDate) || startDate.isEqual(dueDate)){
                    startDateEdit.setTextColor(Color.parseColor("#8694B1"))
                    dueDateEdit.setTextColor(Color.parseColor("#8694B1"))
                    return true
                }
                else{
                    startDateEdit.setTextColor(Color.parseColor("#ff0000"))
                    dueDateEdit.setTextColor(Color.parseColor("#ff0000"))
                    return false
                }

            }catch (e: Exception){
                println("___ Error: $e ___")
            }

            return false
        }
    }
}
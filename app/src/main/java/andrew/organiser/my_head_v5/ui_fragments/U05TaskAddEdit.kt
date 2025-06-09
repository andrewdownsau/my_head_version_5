package andrew.organiser.my_head_v5.ui_fragments

import andrew.organiser.my_head_v5.MainActivity
import andrew.organiser.my_head_v5.R
import andrew.organiser.my_head_v5.SettingsButtonStateManager
import andrew.organiser.my_head_v5.databinding.TaskAddEditBinding
import andrew.organiser.my_head_v5.features.data_manipulation.D02SettingsList
import andrew.organiser.my_head_v5.features.data_manipulation.D03ContextList
import andrew.organiser.my_head_v5.features.data_manipulation.D04TaskList
import andrew.organiser.my_head_v5.features.data_manipulation.D05SubTaskList
import andrew.organiser.my_head_v5.features.data_manipulation.D06RepeatRefresh
import andrew.organiser.my_head_v5.features.ui_generation.Gen03ProtectionDialogue
import andrew.organiser.my_head_v5.features.ui_generation.Gen09SubTaskUIList
import andrew.organiser.my_head_v5.features.ui_generation.Gen06TaskAddEditListeners
import andrew.organiser.my_head_v5.features.ui_generation.Gen07TaskAddEditPopulation
import andrew.organiser.my_head_v5.data_objects.TaskObject
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.time.LocalDate


class U05TaskAddEdit : Fragment() {

    //Global binding to xml layout
    private var _binding: TaskAddEditBinding? = null
    private val binding get() = _binding!!

    //Global boolean variables and handles
    private var editMode = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        println("--- Task Add Edit: onCreateView ---")
        (activity as? SettingsButtonStateManager)?.setVisible("Task_Add_Edit")
        _binding = TaskAddEditBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentContext = requireContext().applicationContext

        val nestedSVLayoutParams: ViewGroup.LayoutParams = binding.taskAddEditScrollview.layoutParams
        val fullNestedSVHeight =  nestedSVLayoutParams.height

        //Listen for keyboard events and adjust height of nestled view
        activity?.window?.decorView?.rootView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { _, insets ->
                val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
                val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                if(_binding != null){
                    val layoutParams: ViewGroup.LayoutParams = binding.taskAddEditScrollview.layoutParams
                    layoutParams.height = if(imeVisible) imeHeight + 70
                    else fullNestedSVHeight
                    binding.taskAddEditScrollview.layoutParams = layoutParams
                }
                insets
            }
        }

        //Close button redirect
        binding.btnClose.setOnClickListener {
            findNavController().popBackStack()
        }

        //Determine the add or edit type based on information provided and set action bar as needed
        val contextName = arguments?.getString("contextName")
        val taskObjectId = arguments?.getInt("taskObjectId")
        editMode = taskObjectId != 0 && taskObjectId != null
        val originalTaskName = if(editMode) D04TaskList.getNameFromId(taskObjectId!!) else ""

        //Set the widget layouts for the task add edit page based on features used
        binding.motivationLayout.visibility =  if(D02SettingsList.getTaskFeatureStatus("Motivation")) View.VISIBLE else View.GONE
        binding.complexityLayout.visibility =  if(D02SettingsList.getTaskFeatureStatus("Complexity")) View.VISIBLE else View.GONE
        binding.checklistLayout.visibility =  if(D02SettingsList.getTaskFeatureStatus("Checklist")) View.VISIBLE else View.GONE
        binding.repeatLayout.visibility =  if(D02SettingsList.getTaskFeatureStatus("Repeating")) View.VISIBLE else View.GONE
        binding.conditionLayout.visibility =  if(D02SettingsList.getTaskFeatureStatus("Conditions")) View.VISIBLE else View.GONE

        //Create spinner array for populating and initialising
        val taskSpinners: ArrayList<Spinner> = ArrayList()
        taskSpinners.add(binding.contextSpinner)
        taskSpinners.add(binding.frequencySpinner)
        taskSpinners.add(binding.conditionSpinner)

        //Set task add edit widget listeners
        Gen06TaskAddEditListeners.main(fragmentContext, null, "setContextListener", arrayListOf(binding.contextSpinner, binding.conditionSpinner), taskObjectId)

        if(binding.motivationLayout.visibility == View.VISIBLE)
            Gen06TaskAddEditListeners.main(fragmentContext, null, "setSeekbarListener", arrayListOf(binding.seekbarMotivation, binding.motivationValue), null)

        if(binding.complexityLayout.visibility == View.VISIBLE)
            Gen06TaskAddEditListeners.main(fragmentContext, null, "setSeekbarListener", arrayListOf(binding.seekbarComplexity, binding.complexityValue), null)


        //Set date and time listeners by an array of edit text views
        val dateEditTextList: ArrayList<View> = arrayListOf(
            binding.editStartDate, binding.editEndDate, binding.editCompletedDate, binding.editRepeatUntil)
        Gen06TaskAddEditListeners.main(fragmentContext, activity, "setDateListeners", dateEditTextList, null)
        val timeEditTextList: ArrayList<View> = arrayListOf(binding.editStartTime, binding.editEndTime)
        Gen06TaskAddEditListeners.main(fragmentContext, activity, "setTimeListeners", timeEditTextList, null)

        //Set checkbox listeners
        Gen06TaskAddEditListeners.main(fragmentContext, null, "setCompleteFlag" , arrayListOf(binding.completedCheck, binding.editCompletedDate), null)
        if(binding.repeatLayout.visibility == View.VISIBLE){
            val repeatWidgetArray = arrayListOf(
                binding.repeatCheck, binding.repeatActiveLayout
            )
            Gen06TaskAddEditListeners.main(fragmentContext, null,"setRepeatFlag", repeatWidgetArray, null)

            val repeatTypeWidgetArray = arrayListOf(
                binding.radioRepeatDay, binding.radioRepeatOther, binding.repeatDayLayout, binding.repeatOtherLayout
            )
            Gen06TaskAddEditListeners.main(fragmentContext, null,"setRepeatType", repeatTypeWidgetArray, null)

            //Radio button listeners
            val radioButtonList: ArrayList<View> = arrayListOf(
                binding.radioRepeatForever, binding.radioRepeatUntil, binding.radioRepeatAfter)
            Gen06TaskAddEditListeners.main(fragmentContext, null, "setRepeatRadioListeners", radioButtonList, null)
        }

        if(binding.checklistLayout.visibility == View.VISIBLE)
            Gen06TaskAddEditListeners.main(fragmentContext, activity, "setChecklistFlag", arrayListOf(binding.checklistCheck, binding.subtaskLayout, binding.subtaskScrollView), taskObjectId)

        //Populate spinners and other widgets
        if(!editMode){
            val pageTitle = if(contextName != null) "New $contextName task" else "New task"
            (activity as AppCompatActivity).supportActionBar?.title = pageTitle
            Gen07TaskAddEditPopulation.main(fragmentContext, taskSpinners, null, contextName)
            binding.editStartDate.setText(LocalDate.now().format(MainActivity.DATE_FORMAT))
            binding.editEndDate.setText(LocalDate.now().format(MainActivity.DATE_FORMAT))
        }
        else{
            //Extract task object using task id
            val taskObject = D04TaskList.getById(taskObjectId)
            (activity as AppCompatActivity).supportActionBar?.title = "Edit: ${taskObject?.name}"

            //Populate spinners and set values of all widget for edit
            Gen07TaskAddEditPopulation.main(fragmentContext, taskSpinners, taskObject, contextName)
            populateEditTaskWidgets(taskObject)

        }


        //Set save and delete button functionality depending on mode
        if(!editMode){
            //Save button creates database task entry and redirects to task list
            binding.btnSave.setOnClickListener {
                if(validateTaskSave()){
                    saveTask(fragmentContext, null, 0)
                    findNavController().popBackStack()
                }
            }

            //Delete button is disabled and has no onClick function
            binding.btnDelete.setBackgroundResource(R.drawable.shadow_button_disabled)
            binding.btnDelete.setTextColor(Color.parseColor("#555555"))
        }
        else{
            //Save button updates database entry and redirects to task list
            binding.btnSave.setOnClickListener {
                if(validateTaskSave()){
                    saveTask(fragmentContext, originalTaskName, taskObjectId!!)
                    findNavController().popBackStack()
                }
            }
            //Delete button deletes entry then redirects back to task list
            binding.btnDelete.setOnClickListener {
                Gen03ProtectionDialogue.main(fragmentContext, this, activity, "TaskDelete", originalTaskName, R.layout.delete_dialog)
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // --- Setting population of task object values if in edit mode --- //
    private fun populateEditTaskWidgets(taskObject: TaskObject?){
        if(taskObject != null){
            //Populate the values from the task object
            try{
                binding.editTaskName.setText(taskObject.name)
                binding.editMotiveName.setText(taskObject.motive)
                binding.seekbarComplexity.progress = taskObject.complexity
                binding.seekbarMotivation.progress = taskObject.motivation
                binding.editStartDate.setText(taskObject.startDate)
                binding.editStartTime.setText(taskObject.startTime)
                binding.editEndDate.setText(taskObject.dueDate)
                binding.editEndTime.setText(taskObject.dueTime)
                binding.completedCheck.isChecked = taskObject.completedFlag
                if (taskObject.completedDate != "") binding.editCompletedDate.setText(taskObject.completedDate)
                binding.repeatCheck.isChecked = taskObject.repeat
                if (taskObject.frequency != null && taskObject.frequency!!.contains("_")) {
                    binding.radioRepeatOther.isChecked = true
                    binding.editRepeatEvery.setText(taskObject.frequency!!.split("_")[0])
                }
                if (taskObject.frequency != null && taskObject.frequency!!.contains("Days:")) setRepeatDays(taskObject.frequency!!)
                binding.radioRepeatForever.isChecked = taskObject.repeatClause.contains("Forever") || (!taskObject.repeatClause .contains("Until") && !taskObject.repeatClause.contains("After"))
                binding.radioRepeatUntil.isChecked = taskObject.repeatClause.contains("Until")
                if (binding.radioRepeatUntil.isChecked) binding.editRepeatUntil.setText(taskObject.repeatClauseValue)
                binding.radioRepeatAfter.isChecked = taskObject.repeatClause.contains("After")
                if (binding.radioRepeatAfter.isChecked) binding.editRepeatAfter.setText(taskObject.repeatClauseValue)
                binding.checklistCheck.isChecked = taskObject.checklist
                binding.editNotes.setText(taskObject.notes)

            }catch (e: Exception){
                println("~~~ Error: $e ~~~")
            }
        }
    }

    private fun setRepeatDays(frequencyStr: String){
        binding.toggleMon.isChecked = frequencyStr.contains("M")
        binding.toggleTue.isChecked = frequencyStr.contains("Tu")
        binding.toggleWed.isChecked = frequencyStr.contains("W")
        binding.toggleThu.isChecked = frequencyStr.contains("Th")
        binding.toggleFri.isChecked = frequencyStr.contains("F")
        binding.toggleSat.isChecked = frequencyStr.contains("Sa")
        binding.toggleSun.isChecked = frequencyStr.contains("Su")
    }

    private fun getRepeatClause():String{
        return if(!binding.repeatCheck.isChecked) ""
        else if(binding.radioRepeatForever.isChecked) "Forever"
        else if(binding.radioRepeatUntil.isChecked) "Until"
        else if(binding.radioRepeatAfter.isChecked)  "After"
        else ""
    }

    private fun getRepeatFrequencyValue():String?{
        var repeatFrequency:String? = null
        if(binding.repeatCheck.isChecked) {
            if(binding.radioRepeatOther.isChecked){
                repeatFrequency = "${binding.editRepeatEvery.text}_${binding.frequencySpinner.selectedItem}"
            }
            else{
                //Check to see which days have been selected (validation: Must include at least 1)
                repeatFrequency = "Days:"
                val dayList: ArrayList<ToggleButton> = arrayListOf(binding.toggleMon, binding.toggleTue, binding.toggleWed, binding.toggleThu, binding.toggleFri, binding.toggleSat, binding.toggleSun)
                var numberDays = 0
                for(day in dayList){
                    if(day.isChecked){
                        numberDays++
                        if(numberDays == 1) repeatFrequency += "${day.textOn}"
                        else repeatFrequency += ",${day.textOn}"
                    }
                }
            }
        }
        return repeatFrequency
    }

    private fun getRepeatClauseValue():String{
        return if(binding.radioRepeatUntil.isChecked) binding.editRepeatUntil.text.toString()
        else if(binding.radioRepeatAfter.isChecked) binding.editRepeatAfter.text.toString()
        else ""
    }

    private fun validateTaskSave(): Boolean{
        //Check character limit and date and repeat clause before saving
        var warningVisible = true
        if(binding.editTaskName.text.toString().length in 1..59){
            if(D04TaskList.validateDate(binding.editStartDate, binding.editEndDate)) {
                if(binding.repeatCheck.isChecked){
                    if(binding.radioRepeatDay.isChecked && getRepeatFrequencyValue() == "Days:") binding.labelWarning.text = getString(R.string.repeat_frequency_warning)
                    else if(binding.radioRepeatOther.isChecked && binding.editRepeatEvery.text.isEmpty()) binding.labelWarning.text = getString(R.string.repeat_frequency_warning)
                    else if(binding.radioRepeatAfter.isChecked && binding.editRepeatAfter.text.isEmpty()) binding.labelWarning.text = getString(R.string.repeat_after_warning)
                    else if(binding.radioRepeatUntil.isChecked && binding.editRepeatUntil.text.isEmpty()) binding.labelWarning.text = getString(R.string.repeat_until_warning)
                    else warningVisible = false
                }
                else{ warningVisible = false }
            }
            else binding.labelWarning.text = getString(R.string.date_warning)
        }
        else
            binding.labelWarning.text = getString(R.string.character_warning)

        binding.labelWarning.visibility = if(warningVisible) View.VISIBLE else View.GONE
        return !warningVisible
    }

    private fun saveTask(c: Context, taskOriginalName:String?, taskId: Int) {
        //Get the context id value for the spinner selection made
        val contextId = D03ContextList.idFromName(binding.contextSpinner.selectedItem.toString())
        var conditionId: Int? = D04TaskList.getIdFromName(binding.conditionSpinner.selectedItem.toString())
        if(conditionId == 0) conditionId = null
        var conditionStatus:Boolean? = null
        if(conditionId != null){ conditionStatus = !D04TaskList.getCompleteFlag(conditionId) }

        //Create new task modal based on UI inputs
        val newTask = TaskObject(
            taskId, contextId, binding.editTaskName.text.toString(), binding.editMotiveName.text.toString(),
            binding.seekbarComplexity.progress, binding.seekbarMotivation.progress, binding.editStartDate.text.toString(),
            binding.editStartTime.text.toString(), binding.editEndDate.text.toString(), binding.editEndTime.text.toString(), binding.checklistCheck.isChecked,
            Gen09SubTaskUIList.getEarliestChecklistDate(binding.subtaskLayout, binding.editEndDate),
            binding.repeatCheck.isChecked, getRepeatClause(), getRepeatClauseValue(), getRepeatFrequencyValue(), conditionId, conditionStatus,
            binding.editNotes.text.toString(), binding.completedCheck.isChecked, binding.editCompletedDate.text.toString())

        if(D04TaskList.save(c, newTask, taskOriginalName)){
            //Update condition status to match completed flag
            D04TaskList.updateConditionStatuses(c, taskId, newTask.completedFlag, !newTask.repeat)

            //Update subtask list with all available info and save into database
            val updatedSubtaskList = Gen09SubTaskUIList.getSaveSubtaskList(binding.subtaskLayout, taskId)
            if(D05SubTaskList.save(c, updatedSubtaskList, taskId))
                D05SubTaskList.initialise(c)

            //Initialise repeat refresh if task has been completed at an earlier date
            if(binding.repeatCheck.isChecked && binding.completedCheck.isChecked){
                println("Debug: Completed Date: ${binding.editCompletedDate.text}")
                val completedDate = LocalDate.parse(binding.editCompletedDate.text.toString(), MainActivity.DATE_FORMAT)
                D06RepeatRefresh.flagChange("write", completedDate.isBefore(LocalDate.now()))
            }
        }
    }
}
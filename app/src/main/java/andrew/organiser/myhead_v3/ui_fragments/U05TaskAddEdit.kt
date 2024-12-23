package andrew.organiser.myhead_v3.ui_fragments

import andrew.organiser.myhead_v3.MainActivity
import andrew.organiser.myhead_v3.R
import andrew.organiser.myhead_v3.SettingsButtonStateManager
import andrew.organiser.myhead_v3.databinding.TaskAddEditBinding
import andrew.organiser.myhead_v3.features.crud.SubTaskListCURD
import andrew.organiser.myhead_v3.features.crud.TaskListCURD
import andrew.organiser.myhead_v3.features.ui_generation.GenerateProtectionDialogue
import andrew.organiser.myhead_v3.features.ui_generation.GenerateTaskUIList
import andrew.organiser.myhead_v3.features.ui_generation.SubTaskUIList
import andrew.organiser.myhead_v3.features.ui_generation.TaskAddEditCheckBoxes
import andrew.organiser.myhead_v3.features.ui_generation.TaskAddEditFeatures
import andrew.organiser.myhead_v3.features.ui_generation.TaskAddEditListeners
import andrew.organiser.myhead_v3.features.ui_generation.TaskAddEditPopulation
import andrew.organiser.myhead_v3.modals.TaskModal
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
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
        var originalTaskName = ""
        editMode = taskObjectId != 0 && taskObjectId != null

        //Set the widget layouts for the task add edit page based on features used
        val taskWidgetLayouts: ArrayList<ConstraintLayout> = ArrayList()
        taskWidgetLayouts.add(binding.motivationLayout)
        taskWidgetLayouts.add(binding.complexityLayout)
        taskWidgetLayouts.add(binding.checklistLayout)
        taskWidgetLayouts.add(binding.repeatLayout)
        taskWidgetLayouts.add(binding.conditionLayout)
        TaskAddEditFeatures.main(fragmentContext, taskWidgetLayouts)

        //Create spinner array for populating and initialising
        val taskSpinners: ArrayList<Spinner> = ArrayList()
        taskSpinners.add(binding.contextSpinner)
        taskSpinners.add(binding.frequencySpinner)
        taskSpinners.add(binding.conditionSpinner)

        //Set task add edit widget listeners
        TaskAddEditListeners.main(fragmentContext, "setSeekbarListener", null, arrayListOf(binding.seekbarComplexity, binding.complexityValue), null)
        TaskAddEditListeners.main(fragmentContext, "setSeekbarListener", null, arrayListOf(binding.seekbarMotivation, binding.motivationValue), null)
        TaskAddEditListeners.main(fragmentContext, "setContextListener", null, arrayListOf(binding.contextSpinner, binding.conditionSpinner), taskObjectId)

        //Set date listeners by an array of edit text views
        val dateEditTextList: ArrayList<View> = arrayListOf(
            binding.editStartDate, binding.editEndDate, binding.editCompletedDate, binding.editRepeatUntil)
        TaskAddEditListeners.main(fragmentContext, "setDateListeners", activity, dateEditTextList, null)

        //Set checkbox listeners
        val repeatWidgetArray = arrayListOf(
            binding.frequencySpinner, binding.labelFrequency, binding.labelRepeatClause, binding.radioRepeatForever,
            binding.radioRepeatUntil, binding.editRepeatUntil, binding.radioRepeatAfter, binding.editRepeatAfter,
            binding.labelRepeatAfter
        )
        TaskAddEditCheckBoxes.main(fragmentContext, null, binding.completedCheck, arrayListOf(binding.editCompletedDate), null)
        TaskAddEditCheckBoxes.main(fragmentContext, activity, binding.checklistCheck, arrayListOf(binding.subtaskLayout, binding.subtaskScrollView), taskObjectId)
        TaskAddEditCheckBoxes.main(fragmentContext, null, binding.repeatCheck, repeatWidgetArray, null)

        //Radio button listeners
        val radioButtonList: ArrayList<View> = arrayListOf(
            binding.radioRepeatForever, binding.radioRepeatUntil, binding.radioRepeatAfter)
        TaskAddEditListeners.main(fragmentContext, "setRepeatRadioListeners", null, radioButtonList, null)


        //Populate spinners and other widgets
        if(!editMode){
            val pageTitle = if(contextName != null) "New $contextName task" else "New task"
            (activity as AppCompatActivity).supportActionBar?.title = pageTitle
            TaskAddEditPopulation.main(fragmentContext, taskSpinners, null, contextName)
            binding.editStartDate.setText(LocalDate.now().format(MainActivity.DATE_FORMAT))
            binding.editEndDate.setText(LocalDate.now().format(MainActivity.DATE_FORMAT))
        }
        else{
            //Extract task object using task id
            val taskListRead = TaskListCURD.main(fragmentContext, "Read", null, null, null)
            if(taskListRead.first && !taskListRead.second.isNullOrEmpty()){
                val taskObject = taskListRead.second!!.filter { it.id == taskObjectId }[0]
                originalTaskName = taskObject.getTaskName()
                (activity as AppCompatActivity).supportActionBar?.title = "Edit: $originalTaskName"

                //Populate spinners and set values of all widget for edit
                TaskAddEditPopulation.main(fragmentContext, taskSpinners, taskObject, null)
                populateEditTaskWidgets(taskObject)
            }
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
                GenerateProtectionDialogue.main(fragmentContext, this, activity, "TaskDelete", originalTaskName, R.layout.delete_dialog)
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // --- Setting population of task object values if in edit mode --- //
    private fun populateEditTaskWidgets(taskObject: TaskModal){
        //Populate the values from the task object
        try{
            binding.editTaskName.setText(taskObject.getTaskName())
            binding.editMotiveName.setText(taskObject.getTaskMotive())
            binding.seekbarComplexity.progress = taskObject.getTaskComplexity()
            binding.seekbarMotivation.progress = taskObject.getTaskMotivation()
            binding.editStartDate.setText(taskObject.getTaskStartDate())
            binding.editEndDate.setText(taskObject.getTaskDueDate())
            binding.completedCheck.isChecked = taskObject.getTaskCompletedFlag()
            if (taskObject.getTaskCompletedDate() != "") binding.editCompletedDate.setText(taskObject.getTaskCompletedDate())
            binding.repeatCheck.isChecked = taskObject.getTaskRepeat()
            binding.radioRepeatForever.isChecked = taskObject.getTaskRepeatClause() == "Forever"
            binding.radioRepeatUntil.isChecked = taskObject.getTaskRepeatClause() == "Until"
            if (taskObject.getTaskRepeatClause() == "Until") binding.editRepeatUntil.setText(taskObject.getTaskRepeatClauseValue())
            binding.radioRepeatAfter.isChecked = taskObject.getTaskRepeatClause() == "After"
            if (taskObject.getTaskRepeatClause() == "After") binding.editRepeatAfter.setText(taskObject.getTaskRepeatClauseValue())
            binding.checklistCheck.isChecked = taskObject.getTaskChecklist()
            binding.editNotes.setText(taskObject.getTaskNotes())

        }catch (e: Exception){
            println("~~~ Error: $e ~~~")
        }
    }

    private fun getRepeatClause():String{
        return if(binding.radioRepeatForever.isChecked) "Forever"
        else if(binding.radioRepeatUntil.isChecked) "Until"
        else if(binding.radioRepeatAfter.isChecked) "After"
        else ""
    }

    private fun getRepeatClauseValue():String{
        return if(binding.radioRepeatUntil.isChecked) binding.editRepeatUntil.text.toString()
        else if(binding.radioRepeatAfter.isChecked) binding.editRepeatAfter.text.toString()
        else ""
    }

    private fun validateTaskSave(): Boolean{
        //Check character limit and date clause before saving
        if(binding.editTaskName.text.toString().length in 1..59){
            if(TaskAddEditListeners.validateDate(binding.editStartDate, binding.editEndDate)) {
                binding.labelWarning.visibility = View.GONE
                return true
            }
            else binding.labelWarning.text = getString(R.string.date_warning)
        }
        else
            binding.labelWarning.text = getString(R.string.character_warning)

        binding.labelWarning.visibility = View.VISIBLE
        return false
    }

    private fun saveTask(c: Context, taskOriginalName:String?, taskId: Int) {
        //Get the context id value for the spinner selection made
        val contextId = TaskAddEditPopulation.getContextIdFromName(binding.contextSpinner.selectedItem.toString()).toInt()
        var conditionId: Int? = TaskAddEditPopulation.getConditionIdFromName(c, binding.conditionSpinner.selectedItem.toString())
        if(conditionId == 0) conditionId = null

        //Create new task modal based on UI inputs
        val newTask = TaskModal(
            taskId, contextId, binding.editTaskName.text.toString(), binding.editMotiveName.text.toString(),
            binding.seekbarComplexity.progress, binding.seekbarMotivation.progress, binding.editStartDate.text.toString(),
            binding.editEndDate.text.toString(), binding.checklistCheck.isChecked,
            SubTaskUIList.getEarliestChecklistDate(binding.subtaskLayout, binding.editEndDate),
            binding.repeatCheck.isChecked, getRepeatClause(), getRepeatClauseValue(), binding.frequencySpinner.selectedItem.toString(),
            conditionId, binding.editNotes.text.toString(), binding.completedCheck.isChecked, binding.editCompletedDate.text.toString())

        val cmd = if(editMode) "Update" else "Create"
        TaskListCURD.main(c, cmd, taskOriginalName, newTask, null)

        //Update condition list if task has been completed
        if(binding.completedCheck.isChecked)
            GenerateTaskUIList.updateTaskConditions(c, taskId)

        //Update subtask list with all available info and save into database
        val updatedSubtaskList = SubTaskUIList.getSaveSubtaskList(binding.subtaskLayout, taskId)
        SubTaskListCURD.main(c, "Update_List", taskId, updatedSubtaskList)

    }
}
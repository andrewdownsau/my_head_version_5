package andrew.organiser.myhead_v3

import andrew.organiser.myhead_v3.databinding.TaskAddEditBinding
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.time.LocalDate
import java.util.Calendar
import java.util.TimeZone


class FragmentTaskAddEdit : Fragment() {

    //Global binding to xml layout
    private var _binding: TaskAddEditBinding? = null
    private val binding get() = _binding!!

    //Global boolean variables and handles
    private var datesValid = true
    private var editMode = false
    private var checklistDataBaseChecked = false
    private var editChecklistName: EditText? = null
    private var editChecklistDate: EditText? = null
    private var lastChecklistId = 0
    private val subtaskWidgetIdList: MutableList<Array<Int>> = mutableListOf()
    private var subtaskList: MutableList<SubtaskModal> = mutableListOf()
    private var dbHandler: DBHandler? = null
    private var fullNestedSVHeight = 0
    private var contextList: ArrayList<ContextModal> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = TaskAddEditBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentContext = requireContext().applicationContext
        dbHandler = DBHandler(fragmentContext)
        contextList = dbHandler?.readContextList(false)!!

        val nestedSVLayoutParams: ViewGroup.LayoutParams = binding.taskAddEditScrollview.layoutParams
        fullNestedSVHeight =  nestedSVLayoutParams.height

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

        //Extract context name from either the bundle passed as a name or a task object
        val contextName = arguments?.getString("contextName")
        val taskObjectId = arguments?.getInt("taskObjectId")
        editMode = taskObjectId != 0

        println("---Task $taskObjectId Add/Edit from context: $contextName ---")
        //Populate spinners with required values
        val adapterList = populateSpinners(fragmentContext, contextName, taskObjectId)

        //Set and change seekbar values based on current position
        setOnSeekbarChangeListener(binding.seekbarComplexity, binding.complexityValue)
        setOnSeekbarChangeListener(binding.seekbarMotivation, binding.motivationValue)

        //Set DatePicker dialogue and checkbox listeners
        setContextSpinnerListener(fragmentContext, taskObjectId)
        setDateListener(binding.editStartDate)
        setDateListener(binding.editEndDate)
        setDateListener(binding.editCompletedDate)
        setDateListener(binding.editRepeatUntil)
        setCheckBoxListener(binding.completedCheck, null)
        setCheckBoxListener(binding.checklistCheck, taskObjectId)
        setCheckBoxListener(binding.repeatCheck, null)
        setRepeatClauseRadioListeners()

        //--- Edit mode if task object can be found ---//
        if(editMode){
            val taskObject = dbHandler?.readTaskList(" WHERE ${DBHandler.TASK_ID_COL} = $taskObjectId", false, false)?.get(0)
            if(taskObject != null){
                populateEditTaskWidgets(adapterList, taskObject)
                val taskOriginalName = taskObject.getTaskName()

                //Save button file write and redirect
                binding.btnSave.setOnClickListener {
                    if(validateTaskSave()){
                        saveTask(taskOriginalName, taskObjectId!!)
                        findNavController().popBackStack()
                    }
                }

                //Delete button deletes entry
                binding.btnDelete.setOnClickListener {
                    //Inflate the dialog as custom view
                    val messageBoxView = LayoutInflater.from(activity).inflate(R.layout.delete_dialog, null)
                    val messageBoxBuilder = AlertDialog.Builder(activity).setView(messageBoxView)
                    val  messageBoxInstance = messageBoxBuilder.show()

                    //Set onclick Listener for dialog box
                    messageBoxView.setOnClickListener{
                        messageBoxInstance.dismiss()
                    }

                    //Set onclick listener for close button
                    messageBoxInstance.findViewById<Button>(R.id.cancel_button).setOnClickListener{
                        messageBoxInstance.dismiss()
                    }

                    //Set onclick listener for ok button, start import
                    messageBoxInstance.findViewById<Button>(R.id.ok_button).setOnClickListener{
                        dbHandler!!.deleteTask(taskOriginalName, taskObjectId!!)
                        messageBoxInstance.dismiss()
                        findNavController().popBackStack()
                    }
                }
            }
        }

        // --- Create new mode if taskObject is null --- //
        else{
            //Set Default values for start and due date
            val defaultDate = LocalDate.now().format(MainActivity.DATE_FORMAT)
            binding.editStartDate.setText(defaultDate)
            binding.editEndDate.setText(defaultDate)

            //Save button file write and redirect
            binding.btnSave.setOnClickListener {
                if(validateTaskSave()){
                    saveTask("", 0)
                    findNavController().popBackStack()
                }
            }
            //Delete button is disabled and has no onClick function
            binding.btnDelete.setBackgroundResource(R.drawable.shadow_button_disabled)
            binding.btnDelete.setTextColor(Color.parseColor("#555555"))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //--- Setting Population for Spinners --- //
    private fun populateSpinners(c: Context, contextName: String?, taskObjectId: Int?) : List<ArrayAdapter<String>>{
        //Generate context list of names for the context spinner
        val contextNames = mutableListOf<String>()
        contextList.forEach{
            contextNames.add(it.getContextName())
        }
        val adp1 = ArrayAdapter(c, R.layout.spinner_item, contextNames)
        adp1.setDropDownViewResource(R.layout.spinner_item)
        binding.contextSpinner.setAdapter(adp1)
        if(contextName != null){
            binding.contextSpinner.post {
                binding.contextSpinner.setSelection(adp1.getPosition(contextName))
            }
        }

        //Generate the frequency options for the frequency spinner
        val frequencyList = mutableListOf("None", "Daily", "Weekly", "Fortnightly", "Monthly", "Quarterly", "Yearly")
        val adp2 = ArrayAdapter(c, R.layout.spinner_item, frequencyList)
        adp2.setDropDownViewResource(R.layout.spinner_item)
        binding.frequencySpinner.setAdapter(adp2)

        //Generate condition list of other tasks to complete before this one
        val adp3 = populateConditionSpinner(c, contextName, taskObjectId)

        return listOf(adp1, adp2, adp3)
    }

    private fun populateConditionSpinner(c: Context, contextName: String?, taskObjectId: Int?):ArrayAdapter<String>{
        val conditionList = mutableListOf("None")
        if(contextList.isNotEmpty()){
            val contextId = if(contextName != null){
                contextList.filter { it.getContextName() == contextName }[0].id
            } else{
                contextList[0].id
            }

            val taskContextList = dbHandler?.readTaskList(" WHERE " +
                    "${DBHandler.TASK_CONTEXT_ID_COL} = $contextId AND " +
                    "${DBHandler.TASK_COMPLETED_FLAG_COL} = 0", true, false)
            if(!taskContextList.isNullOrEmpty()){
                taskContextList.forEach { task ->
                    if(task.id != taskObjectId)conditionList.add(task.getTaskName())
                }
            }
        }

        val adp3 = ArrayAdapter(c, R.layout.spinner_item, conditionList)
        adp3.setDropDownViewResource(R.layout.spinner_item)
        binding.conditionSpinner.setAdapter(adp3)

        if(editMode){
            val taskObject = dbHandler?.readTaskList(" WHERE ${DBHandler.TASK_ID_COL} = $taskObjectId", false, false)?.get(0)
            if(taskObject != null) {
                binding.conditionSpinner.post {
                    var taskConditionName = "None"
                    if(taskObject.getTaskConditionId() != 0){
                        val taskConditionTaskRead = dbHandler?.readTaskList(" WHERE ${DBHandler.TASK_ID_COL} = ${taskObject.getTaskConditionId()}", false, false)
                        if(!taskConditionTaskRead.isNullOrEmpty())
                            taskConditionName = taskConditionTaskRead[0].getTaskName()
                    }
                    binding.conditionSpinner.setSelection(adp3.getPosition(taskConditionName))
                }
            }
        }

        return adp3
    }

    // --- Setting Listeners for seekbars and date pickers --- //
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

    private fun setDateListener(editText: EditText){
        editText.setOnClickListener {

            val calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())

            val dialog = DatePickerDialog(
                requireActivity(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    //Set the text to include 2 digits for month and day
                    val actualMonth = selectedMonth + 1
                    val dayText = if(selectedDay < 10) "0$selectedDay" else selectedDay.toString()
                    val monthText = if(actualMonth < 10) "0$actualMonth" else actualMonth.toString()
                    val displayedDate = "$dayText/$monthText/${selectedYear-2000}"

                    //Check start date if end date and do not accept any date before start
                    validateDate(editText, displayedDate)
                    editText.setText(displayedDate)
                },
                calendar[Calendar.YEAR], calendar[Calendar.MONTH],
                calendar[Calendar.DAY_OF_MONTH]
            )
            dialog.show()
        }
    }

    private fun setContextSpinnerListener(c: Context, taskObjectId: Int?){
        binding.contextSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                populateConditionSpinner(c, binding.contextSpinner.selectedItem.toString(), taskObjectId)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // --- Setting Checkbox listeners and their corresponding states --- //
    private fun setCheckBoxListener(checkBox: CheckBox, taskObjectId: Int?){
        //Default values based on pre-populated
        when(checkBox){
            binding.completedCheck -> setCompletedWidgetsState(checkBox.isChecked)
            binding.checklistCheck -> setChecklistWidgetsState(checkBox.isChecked, taskObjectId)
            binding.repeatCheck -> setRepeatWidgetsState(checkBox.isChecked)
        }

        //Disable frequency spinner based on current or changed state of checkbox
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            when(checkBox){
                binding.completedCheck -> setCompletedWidgetsState(isChecked)
                binding.checklistCheck -> setChecklistWidgetsState(isChecked, taskObjectId)
                binding.repeatCheck -> setRepeatWidgetsState(isChecked)
            }
        }
    }

    private fun setCompletedWidgetsState(isChecked: Boolean){
        if(isChecked) {
            binding.editCompletedDate.visibility = View.VISIBLE
            binding.editCompletedDate.setText(LocalDate.now().format(MainActivity.DATE_FORMAT))
        }
        else {
            binding.editCompletedDate.visibility = View.GONE
            binding.editCompletedDate.setText(getString(R.string.blank_label))
        }
    }

    private fun setChecklistWidgetsState(isChecked: Boolean, taskObjectId: Int?){
        val funContext = requireContext().applicationContext
        // Clear all views from layout and add all buttons from contents
        binding.subtaskLayout.removeAllViews()

        if(isChecked) {
            binding.subtaskScrollView.visibility = View.VISIBLE

            //Display the add new check list item widgets
            createSubTaskAdder(funContext, binding.subtaskLayout, taskObjectId)

            //Display all subtasks applicable to this list
            if(taskObjectId != null){
                val subtaskWhereClause = " WHERE ${DBHandler.SUBTASK_TASK_ID_COL}=$taskObjectId"
                if(!checklistDataBaseChecked){
                    subtaskList = dbHandler?.readSubTaskList(subtaskWhereClause)!!
                    checklistDataBaseChecked = true
                }
                if (subtaskList.isNotEmpty()) {
                    lastChecklistId =  createSubTaskList(funContext, binding.subtaskLayout, subtaskList, taskObjectId)
                }
            }

        }
        else {
            binding.subtaskScrollView.visibility = View.GONE
        }
    }

    private fun setRepeatWidgetsState(isChecked: Boolean){
        val visible = if(isChecked) View.VISIBLE
        else View.GONE

        binding.frequencySpinner.visibility = visible
        binding.labelFrequency.visibility = visible
        binding.labelRepeatClause.visibility = visible
        binding.radioRepeatForever.visibility = visible
        binding.radioRepeatUntil.visibility = visible
        binding.editRepeatUntil.visibility = visible
        binding.radioRepeatAfter.visibility = visible
        binding.editRepeatAfter.visibility = visible
        binding.labelRepeatAfter.visibility = visible

    }

    // --- Setting population of task object values if in edit mode --- //
    private fun populateEditTaskWidgets(adapterList: List<ArrayAdapter<String>>, taskObject: TaskModal ){
        //Populate the values from the task object
        try{
            binding.editTaskName.setText(taskObject.getTaskName())
            binding.editMotiveName.setText(taskObject.getTaskMotive())
            binding.seekbarComplexity.progress = taskObject.getTaskComplexity()
            binding.seekbarMotivation.progress = taskObject.getTaskMotivation()
            binding.editStartDate.setText(taskObject.getTaskStartDate())
            binding.editEndDate.setText(taskObject.getTaskDueDate())
            binding.completedCheck.isChecked = taskObject.getTaskCompletedFlag()
            binding.editCompletedDate.setText(taskObject.getTaskCompletedDate())
            binding.repeatCheck.isChecked = taskObject.getTaskRepeat()
            binding.radioRepeatForever.isChecked = taskObject.getTaskRepeatClause() == "Forever"
            binding.radioRepeatUntil.isChecked = taskObject.getTaskRepeatClause() == "Until"
            if (taskObject.getTaskRepeatClause() == "Until") binding.editRepeatUntil.setText(taskObject.getTaskRepeatClauseValue())
            binding.radioRepeatAfter.isChecked = taskObject.getTaskRepeatClause() == "After"
            if (taskObject.getTaskRepeatClause() == "After") binding.editRepeatAfter.setText(taskObject.getTaskRepeatClauseValue())
            binding.checklistCheck.isChecked = taskObject.getTaskChecklist()
            binding.frequencySpinner.post {
                binding.frequencySpinner.setSelection(adapterList[1].getPosition(taskObject.getTaskFrequency()))
            }
            binding.editNotes.setText(taskObject.getTaskNotes())

        }catch (e: Exception){
            println("___ Error: $e ___")
        }
    }

    private fun validateDate(checkDateEdit: EditText, checkDateStr: String){
        //Check date order based on flag
        println("--- ValidateDate TaskAddEdit ---")
        try{
            var otherDate: LocalDate? = null
            val checkDate = LocalDate.parse(checkDateStr, MainActivity.DATE_FORMAT)
            var dateValid = true
            if(checkDateEdit == binding.editEndDate){
                otherDate = LocalDate.parse(binding.editStartDate.text, MainActivity.DATE_FORMAT)
                dateValid = !otherDate.isAfter(checkDate)
            }
            else if(checkDateEdit == binding.editStartDate){
                otherDate = LocalDate.parse(binding.editEndDate.text, MainActivity.DATE_FORMAT)
                dateValid = !otherDate.isBefore(checkDate)
            }

            //Debug Logs
            println("_otherDate:$otherDate")
            println("_checkDate:$checkDate")
            println("_dateValid:$dateValid")

            if(dateValid){
                binding.editStartDate.setTextColor(Color.parseColor("#8694B1"))
                binding.editEndDate.setTextColor(Color.parseColor("#8694B1"))
            }
            else{
                binding.editStartDate.setTextColor(Color.parseColor("#ff0000"))
                binding.editEndDate.setTextColor(Color.parseColor("#ff0000"))
            }

            datesValid = dateValid
        }catch (e: Exception){
            println("___ Error: $e ___")
        }
    }

    private fun saveTask(taskOriginalName:String, taskId: Int) {
        //Get the context id value for the spinner selection made
        val contextId = contextList.filter { it.getContextName() == binding.contextSpinner.selectedItem }[0].id
        var conditionId = 0
        if(binding.conditionSpinner.selectedItem != "None")
            conditionId = dbHandler?.readTaskList("", false, false)?.filter { it.getTaskName() == binding.conditionSpinner.selectedItem }?.get(0)?.id!!

        //Add new task values and assign to task modal
        val newTaskId = dbHandler?.addEditTask(requireContext().applicationContext,taskOriginalName, TaskModal(
            taskId,
            contextId,
            binding.editTaskName.text.toString(),
            binding.editMotiveName.text.toString(),
            binding.seekbarComplexity.progress,
            binding.seekbarMotivation.progress,
            binding.editStartDate.text.toString(),
            binding.editEndDate.text.toString(),
            binding.checklistCheck.isChecked,
            extractEarliestChecklistDate(),
            binding.repeatCheck.isChecked,
            getRepeatClause(),
            getRepeatClauseValue(),
            binding.frequencySpinner.selectedItem.toString(),
            conditionId,
            binding.editNotes.text.toString(),
            binding.completedCheck.isChecked,
            binding.editCompletedDate.text.toString()), editMode)

        //Update subtask list with all available info and save into database
        val updatedSubtaskList = mutableListOf<SubtaskModal>()
        subtaskWidgetIdList.forEach { widgetIdArray ->
            var subtaskName = ""; var subtaskDate = ""; var checked = false
            widgetIdArray.forEachIndexed { index, widgetId ->
                if(binding.subtaskLayout.getViewById(widgetId) != null){
                    when(index){
                        0 -> {val widget = binding.subtaskLayout.getViewById(widgetId) as EditText
                            subtaskName = widget.text.toString()}
                        1 -> {val widget = binding.subtaskLayout.getViewById(widgetId) as EditText
                            subtaskDate = widget.text.toString()}
                        else -> {val widget = binding.subtaskLayout.getViewById(widgetId) as CheckBox
                            checked = widget.isChecked}
                    }
                }
            }
            if(subtaskName.isNotEmpty())
                updatedSubtaskList.add(SubtaskModal(0, taskId, subtaskName, subtaskDate, checked))
        }
        dbHandler?.addEditSubTasks(requireContext().applicationContext, newTaskId!!, updatedSubtaskList)

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

    // --- Crud checklist widgets --- //
    private fun setOnClickAddChecklistItem(button: Button, taskId:Int?){
        //Convert task id to default zero, cannot be null
        var usableTaskId = taskId
        if(usableTaskId == null) usableTaskId = 0

        button.setOnClickListener {
            //Add new sub task to the overall list
            val newSubtaskName = editChecklistName!!.text.toString()
            val newSubtaskDateStr = editChecklistDate!!.text.toString()
            if(newSubtaskName.isNotEmpty()){
                val newSubtaskModal = SubtaskModal(0, usableTaskId, newSubtaskName,newSubtaskDateStr, false)

                //Create new subtask list item in the layout
                lastChecklistId = createSubtaskItem(requireContext(), binding.subtaskLayout, newSubtaskModal, taskId)
                editChecklistName!!.setText(resources.getString(R.string.blank_label))
                editChecklistDate!!.setText(resources.getString(R.string.blank_label))

                //Add new subtask to list once id is confirmed
                newSubtaskModal.id = lastChecklistId
                subtaskList.add(newSubtaskModal)

                //Change the end or start date of the task to fit the subtask date entered
                if(newSubtaskDateStr.isNotEmpty()){
                    val newSubtaskDate = LocalDate.parse(newSubtaskDateStr, MainActivity.DATE_FORMAT)
                    val taskStartDate = LocalDate.parse(binding.editStartDate.text.toString(), MainActivity.DATE_FORMAT)
                    val taskDueDate = LocalDate.parse(binding.editEndDate.text.toString(), MainActivity.DATE_FORMAT)

                    if(newSubtaskDate.isBefore(taskStartDate)) binding.editStartDate.setText(newSubtaskDate.format(MainActivity.DATE_FORMAT))
                    if(newSubtaskDate.isAfter(taskDueDate)) binding.editEndDate.setText(newSubtaskDate.format(MainActivity.DATE_FORMAT))
                }
            }
        }
    }

    private fun setOnClickRemoveChecklistItem(layout: ConstraintLayout, button: Button, nameEditText: EditText, dateEditText: EditText, checkBox: CheckBox, taskId:Int?){
        //Use the button tag to determine which subtask to remove from the list and layout
        button.setOnClickListener {
            //Get the ids of the subtasks either above or below (if not only item)
            var topId = 0; var bottomId = 0
            val subtaskIdList = mutableListOf<Int>()
            subtaskList.forEach { subtask -> subtaskIdList.add(subtask.id) }
            val indexBtnTag = subtaskIdList.indexOf(button.tag)
            if(indexBtnTag > 0){ topId = subtaskIdList[indexBtnTag-1] }
            if(indexBtnTag != subtaskIdList.size-1){ bottomId = subtaskIdList[indexBtnTag+1] }
            subtaskList.remove(subtaskList.filter { it.id == button.tag }[0])

            //If this is the only item then there is no need to preserve the other checklist items
            if(topId == 0 && bottomId == 0){
                setChecklistWidgetsState(true, taskId)
            }
            //Otherwise, remove line and shift constraints to still include the remaining items
            else {
                //If not the topmost item, determine if the bottommost
                if(topId != 0){
                    if(bottomId == 0) bottomId = editChecklistName!!.id
                }

                //Shift the constraints of the name edit text widgets of the top and bottom items
                val constraintSet = ConstraintSet()
                constraintSet.clone(layout)
                if(topId == 0){
                    constraintSet.connect(bottomId, ConstraintSet.TOP, layout.id, ConstraintSet.TOP, 0)
                }
                else{
                    constraintSet.connect(bottomId, ConstraintSet.TOP, topId, ConstraintSet.BOTTOM, 30)
                }

                constraintSet.applyTo(layout)

                //Remove line of widgets from layout
                subtaskWidgetIdList.remove(arrayOf(nameEditText.id,dateEditText.id,checkBox.id))
                layout.removeView(nameEditText)
                layout.removeView(dateEditText)
                layout.removeView(checkBox)
                layout.removeView(button)
            }
        }
    }

    private fun createSubTaskAdder(c: Context, layout: ConstraintLayout, taskId: Int?){
        println("+++ createSubTaskAdder +++")

        //Set parameters for item name
        editChecklistName = createGenericSubtaskNameEditText(c)
        layout.addView(editChecklistName)

        //Set parameters for item due date
        editChecklistDate = createGenericSubtaskDateEditText(c, editChecklistName!!.id)
        layout.addView(editChecklistDate)

        //Set parameters for item completed check
        val completedCheckbox = createGenericSubtaskCheckBox(c, false, editChecklistName!!.id)
        layout.addView(completedCheckbox)

        //Set parameters for add button
        val editCheckAdd = createGenericSubtaskButton(c, R.drawable.ic_menu_add, editChecklistName!!.id)
        layout.addView(editCheckAdd)
        setOnClickAddChecklistItem(editCheckAdd, taskId)

        setSubtaskConstraints(layout, editChecklistName!!, editChecklistDate!!, completedCheckbox, editCheckAdd, false)

    }

    private fun createSubTaskList(c: Context, layout: ConstraintLayout, subtaskList: List<SubtaskModal>, taskId: Int?) : Int{
        println("+++ createSubtaskButtonList: $subtaskList +++")

        subtaskList.forEach {
            lastChecklistId = createSubtaskItem(c, layout, it, taskId)
            it.id = lastChecklistId
            println("_lastWidgetId: $lastChecklistId")
        }

        return lastChecklistId
    }

    private fun createSubtaskItem(c: Context, layout: ConstraintLayout, subtaskObject: SubtaskModal, taskId: Int?) : Int {
        println("+++ createSubtaskItem: \n${subtaskObject.getSubtaskName()} +++")

        //Set parameters for item name
        val checkItemName = createGenericSubtaskNameEditText(c)
        checkItemName.setText(subtaskObject.getSubtaskName())
        layout.addView(checkItemName)

        //Set parameters for item due date
        val checkItemDate = createGenericSubtaskDateEditText(c, checkItemName.id)
        checkItemDate.setText(subtaskObject.getSubtaskDueDate())
        layout.addView(checkItemDate)

        //Set parameters for item completed check
        val completedCheckbox = createGenericSubtaskCheckBox(c, true, checkItemName.id)
        completedCheckbox.isChecked = subtaskObject.getSubtaskCompletedFlag()
        layout.addView(completedCheckbox)

        //Set parameters for add button
        val editCheckRemove = createGenericSubtaskButton(c, R.drawable.ic_menu_delete, checkItemName.id)
        layout.addView(editCheckRemove)
        setOnClickRemoveChecklistItem(layout, editCheckRemove, checkItemName, checkItemDate, completedCheckbox, taskId)

        setSubtaskConstraints(layout, checkItemName, checkItemDate, completedCheckbox, editCheckRemove, true)

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
        checkItemName.gravity = Gravity.BOTTOM
        checkItemName.setRawInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
        checkItemName.setTextColor(c.getColor(R.color.sub_content))

        return checkItemName
    }

    private fun createGenericSubtaskDateEditText(c: Context, nameEditTextId: Int) : EditText{
        //Set parameters for item date
        val checkItemDate = EditText(c)
        checkItemDate.id = View.generateViewId()
        val params = ConstraintLayout.LayoutParams(260, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        checkItemDate.setLayoutParams(params)
        checkItemDate.hint = "Due date"
        checkItemDate.setTextColor(c.getColor(R.color.sub_content))
        checkItemDate.focusable = View.NOT_FOCUSABLE
        checkItemDate.tag = nameEditTextId
        setDateListener(checkItemDate)

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

    private fun createGenericSubtaskButton(c: Context, backgroundResource: Int, nameEditTextId: Int) : Button{
        //Set parameters for add button
        val subtaskButton = Button(c)
        subtaskButton.id = View.generateViewId()
        val params4 = ConstraintLayout.LayoutParams(100, 100)
        subtaskButton.setLayoutParams(params4)
        subtaskButton.background = ContextCompat.getDrawable(c, backgroundResource)
        subtaskButton.setTextColor(c.getColor(R.color.sub_content))
        subtaskButton.tag = nameEditTextId

        return subtaskButton
    }

    private fun setSubtaskConstraints(layout: ConstraintLayout, nameEditText: EditText, dateEditText: EditText, checkBox: CheckBox, button: Button, shiftAdderFlag: Boolean){
        println("+++ Setting subtask constraints with lastChecklistId = $lastChecklistId +++")
        val constraintSet = ConstraintSet()
        constraintSet.clone(layout)
        if(shiftAdderFlag) constraintSet.connect(editChecklistName!!.id, ConstraintSet.TOP, nameEditText.id, ConstraintSet.BOTTOM, 30)
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
        if(shiftAdderFlag)
            subtaskWidgetIdList.add(arrayOf(nameEditText.id,dateEditText.id,checkBox.id))
    }

    private fun extractEarliestChecklistDate() : String{
        var subtaskDate = LocalDate.now()
        var subtaskDateValid = false
        var subtaskChecked = false
        var earliestSubtaskDate = LocalDate.parse(binding.editEndDate.text.toString(), MainActivity.DATE_FORMAT)
        subtaskWidgetIdList.forEach { widgetIdArray ->
            widgetIdArray.forEachIndexed { index, widgetId ->
                if(binding.subtaskLayout.getViewById(widgetId) != null){
                    when(index){
                        1 -> {
                            val widget = binding.subtaskLayout.getViewById(widgetId) as EditText
                            val subtaskDateStr = widget.text.toString()
                            subtaskDateValid = subtaskDateStr.isNotEmpty()
                            if(subtaskDateValid)
                                subtaskDate = LocalDate.parse(subtaskDateStr, MainActivity.DATE_FORMAT)
                        }
                        2 -> {
                            val widget = binding.subtaskLayout.getViewById(widgetId) as CheckBox
                            subtaskChecked = widget.isChecked
                        }
                    }
                }
            }
            if(subtaskDate < earliestSubtaskDate && subtaskDateValid && !subtaskChecked) earliestSubtaskDate = subtaskDate
        }
        return earliestSubtaskDate.format(MainActivity.DATE_FORMAT)
    }

    private fun setRepeatClauseRadioListeners(){
        //Set the listeners so that clicking one button, clears the status of the others
        binding.radioRepeatForever.setOnClickListener {
            binding.radioRepeatUntil.isChecked = false
            binding.radioRepeatAfter.isChecked = false
        }

        binding.radioRepeatUntil.setOnClickListener {
            binding.radioRepeatForever.isChecked = false
            binding.radioRepeatAfter.isChecked = false
        }

        binding.radioRepeatAfter.setOnClickListener {
            binding.radioRepeatForever.isChecked = false
            binding.radioRepeatUntil.isChecked = false
        }


        /*binding.radioRepeatForever.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                binding.radioRepeatUntil.isChecked = false
                binding.radioRepeatAfter.isChecked = false
            }
        }
        binding.radioRepeatUntil.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                binding.radioRepeatForever.isChecked = false
                binding.radioRepeatAfter.isChecked = false
            }
        }
        binding.radioRepeatAfter.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                binding.radioRepeatUntil.isChecked = false
                binding.radioRepeatForever.isChecked = false
            }
        }*/
    }

    private fun validateTaskSave(): Boolean{
        //Check character limit and date clause before saving
        if(binding.editTaskName.text.toString().length < 60){
            if(datesValid) {
                binding.labelWarning.visibility = View.GONE
                return true
            }
            else binding.labelWarning.text = getString(R.string.date_warning)
        }
        else
            binding.labelWarning.text = getString(R.string.max_character_warning)

        binding.labelWarning.visibility = View.VISIBLE
        return false
    }
}
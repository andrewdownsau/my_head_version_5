package andrew.organiser.my_head_v5.ui_fragments

import andrew.organiser.my_head_v5.DBHandler
import andrew.organiser.my_head_v5.R
import andrew.organiser.my_head_v5.SettingsButtonStateManager
import andrew.organiser.my_head_v5.databinding.SettingsBinding
import andrew.organiser.my_head_v5.features.data_manipulation.D02SettingsList
import andrew.organiser.my_head_v5.features.ui_generation.Gen03ProtectionDialogue
import andrew.organiser.my_head_v5.features.ui_generation.Gen08SettingsOrderList
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment


/*
 * Settings Fragment, to set format of tasks and import and export app database
 */
class U03Settings : Fragment() {

    private var _binding: SettingsBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private var dbHandler: DBHandler? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = SettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        println("--- Settings: onViewCreated ---")
        super.onViewCreated(view, savedInstanceState)
        (activity as? SettingsButtonStateManager)?.setVisible("Settings")

        val c = requireContext().applicationContext
        dbHandler = DBHandler(c)

        //Save task features button DB write
        binding.btnSaveTaskFeatures.setOnClickListener {
            //Convert the toggle points to Arraylist
            val newFeatureList: MutableMap<String, Boolean> = mutableMapOf()
            newFeatureList["Motivation"] = binding.featureMotivation.isChecked
            newFeatureList["Complexity"] = binding.featureComplexity.isChecked
            newFeatureList["Checklist"] = binding.featureChecklist.isChecked
            newFeatureList["Repeating"] = binding.featureRepeat.isChecked
            newFeatureList["Conditions"] = binding.featureCondition.isChecked
            newFeatureList["TimeProgress"] = binding.featureTimeProgress.isChecked
            newFeatureList["ChecklistProgress"] = binding.featureChecklistProgress.isChecked

            if(D02SettingsList.saveFeatures(c, newFeatureList)){
                D02SettingsList.initialise(c)
                Gen08SettingsOrderList.main(c, binding.taskSortOrderList)
            }
        }

        //Save task sort order list
        binding.btnSaveTaskSorting.setOnClickListener {
            D02SettingsList.saveSortOrder(c, Gen08SettingsOrderList.getCurrentList())
        }

        //Incomplete and Pending default status buttons
        Gen08SettingsOrderList.setLowHighChangeListener(c, binding.buttonIncompleteStatus, binding.labelIncompleteStatus)
        Gen08SettingsOrderList.setLowHighChangeListener(c, binding.buttonPendingStatus, binding.labelPendingStatus)


        //Save task color settings
        binding.btnSaveColours.setOnClickListener {
            //Convert the color strings into usable array
            val newColorList: MutableMap<String, String> = mutableMapOf()
            newColorList["overdue"] = binding.editColourOverdue.text.toString()
            newColorList["today"] = binding.editColourToday.text.toString()
            newColorList["tomorrow"] = binding.editColourTomorrow.text.toString()
            newColorList["threeDays"] = binding.editColourThreeDays.text.toString()
            newColorList["week"] = binding.editColourWeek.text.toString()
            newColorList["weekPlus"] = binding.editColourWeekPlus.text.toString()
            newColorList["conditional"] = binding.editColourConditional.text.toString()
            newColorList["pending"] = binding.editColourPending.text.toString()
            newColorList["startToday"] = binding.editColourStartToday.text.toString()
            newColorList["completed"] = binding.editColourComplete.text.toString()
            newColorList["timePB"] = binding.editColourTimePB.text.toString()
            newColorList["checklistPB"] = binding.editColourChecklistPB.text.toString()

            if(D02SettingsList.saveUIColors(c, newColorList)){ D02SettingsList.initialise(c) }
        }

        //Save Archive delete function settings
        binding.btnSaveArchiveDelete.setOnClickListener {
            //Check whether any value has been inputted if after days has been selected
            if(binding.radioDeleteAfter.isChecked && binding.editDeleteAfter.text.isNotEmpty()){
                val afterDays = binding.editDeleteAfter.text.toString().toInt()
                Gen03ProtectionDialogue.main(c, this, activity, "ArchiveDelete", "After $afterDays days", R.layout.archive_delete_dialog)
            }
            else if(binding.radioDeleteNever.isChecked){
                D02SettingsList.saveArchiveDelete(c, "never")
            }
            else{
                Toast.makeText(c, "Archive delete setting must include number of days", Toast.LENGTH_SHORT).show()
            }

        }

        //Reset settings to defaults
        binding.btnDefaultReset.setOnClickListener {
            Gen03ProtectionDialogue.main(c, this, activity, "ResetSettings", null, R.layout.reset_dialog)
        }
    }

    override fun onStart() {
        super.onStart()
        val fragmentContext = requireContext().applicationContext
        setTaskFeatures()
        setTaskSortOrder(fragmentContext)
        setTaskColors()
        setArchiveRadioListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setTaskFeatures(){
        println("Process: Set Task Features") //Process Line
        //Set the current values for task features
        binding.featureMotivation.isChecked = D02SettingsList.getTaskFeatureStatus("Motivation")
        binding.featureComplexity.isChecked = D02SettingsList.getTaskFeatureStatus("Complexity")
        binding.featureChecklist.isChecked = D02SettingsList.getTaskFeatureStatus("Checklist")
        binding.featureRepeat.isChecked = D02SettingsList.getTaskFeatureStatus("Repeating")
        binding.featureCondition.isChecked = D02SettingsList.getTaskFeatureStatus("Conditions")
        binding.featureTimeProgress.isChecked = D02SettingsList.getTaskFeatureStatus("TimeProgress")
        binding.featureChecklistProgress.isChecked = D02SettingsList.getTaskFeatureStatus("ChecklistProgress")

        //Set listener for checklist toggle
        if(!binding.featureChecklist.isChecked) binding.featureChecklistProgress.visibility = View.GONE
        else binding.featureChecklistProgress.visibility = View.VISIBLE
        binding.featureChecklist.setOnClickListener {
            if(!binding.featureChecklist.isChecked){
                binding.featureChecklistProgress.isChecked = false
                binding.featureChecklistProgress.visibility = View.GONE
            }
            else binding.featureChecklistProgress.visibility = View.VISIBLE
        }
    }

    private fun setTaskSortOrder(c: Context){
        //Set the current values for task sort order
        Gen08SettingsOrderList.main(c, binding.taskSortOrderList)

        //Incomplete and Pending default button and textview states
        val incompleteSort = "Incomplete: ${D02SettingsList.getOrderTypeFromName("Incomplete")}"
        val pendingSort = "Pending: ${D02SettingsList.getOrderTypeFromName("Pending")}"
        binding.labelIncompleteStatus.text = incompleteSort
        binding.labelPendingStatus.text = pendingSort
        val btnResInComplete = if(incompleteSort.contains("High")) R.mipmap.icon_high else R.mipmap.icon_low
        val btnResPending = if(pendingSort.contains("High")) R.mipmap.icon_high else R.mipmap.icon_low
        binding.buttonIncompleteStatus.background = ContextCompat.getDrawable(c, btnResInComplete)
        binding.buttonPendingStatus.background = ContextCompat.getDrawable(c, btnResPending)
    }

    private fun setTaskColors(){
        //println("Process: Set Task Colors") //Process line
        //Set the current values for task colors
        val colorList = D02SettingsList.getUIColorList()
        for(color in colorList){
            when(color.key){
                "overdue" -> initiateEditColorGroup(binding.editColourOverdue, binding.buttonColourOverdue, color.value)
                "today" -> initiateEditColorGroup(binding.editColourToday, binding.buttonColourToday, color.value)
                "tomorrow" -> initiateEditColorGroup(binding.editColourTomorrow, binding.buttonColourTomorrow, color.value)
                "threeDays" -> initiateEditColorGroup(binding.editColourThreeDays, binding.buttonColourThreeDays, color.value)
                "week" -> initiateEditColorGroup(binding.editColourWeek, binding.buttonColourWeek, color.value)
                "weekPlus" -> initiateEditColorGroup(binding.editColourWeekPlus, binding.buttonColourWeekPlus, color.value)
                "conditional" -> initiateEditColorGroup(binding.editColourConditional, binding.buttonColourConditional, color.value)
                "pending" -> initiateEditColorGroup(binding.editColourPending, binding.buttonColourPending, color.value)
                "startToday" -> initiateEditColorGroup(binding.editColourStartToday, binding.buttonColourStartToday, color.value)
                "completed" -> initiateEditColorGroup(binding.editColourComplete, binding.buttonColourComplete, color.value)
                "timePB" -> initiateEditColorGroup(binding.editColourTimePB, binding.buttonColourTimePB, color.value)
                "checklistPB" -> initiateEditColorGroup(binding.editColourChecklistPB, binding.buttonColourChecklistPB, color.value)
            }
        }
    }

    private fun initiateEditColorGroup(editText: EditText, button: Button, colorVal: String){
        editText.setText(colorVal)
        button.setBackgroundColor(Color.parseColor(colorVal))
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                try{
                    val editTextColor = Color.parseColor(s.toString())
                    //println("Debug: Attempting to change color setting background to: $s") //Debug line
                    button.setBackgroundColor(editTextColor)
                }catch(_:Exception){}
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setArchiveRadioListeners(){
        //Populate radio values from settings
        val archiveDeleteSetting = D02SettingsList.getArchiveDeleteSetting()
        binding.radioDeleteNever.isChecked = archiveDeleteSetting == "never"
        binding.radioDeleteAfter.isChecked = !binding.radioDeleteNever.isChecked

        if(binding.radioDeleteAfter.isChecked){
            binding.editDeleteAfter.setText(archiveDeleteSetting.split(" ")[1])
        }

        //Set the listeners so that clicking one button, clears the status of the others
        binding.radioDeleteNever.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) binding.radioDeleteAfter.isChecked = false
        }
        binding.radioDeleteAfter.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) binding.radioDeleteNever.isChecked = false
        }

    }
}
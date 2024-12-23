package andrew.organiser.myhead_v3.ui_fragments

import andrew.organiser.myhead_v3.DBHandler
import andrew.organiser.myhead_v3.R
import andrew.organiser.myhead_v3.SettingsButtonStateManager
import andrew.organiser.myhead_v3.databinding.SettingsBinding
import andrew.organiser.myhead_v3.features.crud.SettingsListCURD
import andrew.organiser.myhead_v3.features.settings.TaskColors
import andrew.organiser.myhead_v3.features.settings.TaskFeatures
import andrew.organiser.myhead_v3.features.settings.TaskSortOrder
import andrew.organiser.myhead_v3.features.ui_generation.GenerateProtectionDialogue
import andrew.organiser.myhead_v3.features.ui_generation.GenerateTaskOrderList
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

        val fragmentContext = requireContext().applicationContext
        dbHandler = DBHandler(fragmentContext)

        //Save task features button DB write
        binding.btnSaveTaskFeatures.setOnClickListener {
            //Convert the toggle points to Arraylist
            val newFeatureList: ArrayList<Pair<String, Boolean>> = ArrayList()
            newFeatureList.add(Pair("Motivation", binding.featureMotivation.isChecked))
            newFeatureList.add(Pair("Complexity", binding.featureComplexity.isChecked))
            newFeatureList.add(Pair("Checklist", binding.featureChecklist.isChecked))
            newFeatureList.add(Pair("Repeating", binding.featureRepeat.isChecked))
            newFeatureList.add(Pair("Conditions", binding.featureCondition.isChecked))
            newFeatureList.add(Pair("TimeProgress", binding.featureTimeProgress.isChecked))
            newFeatureList.add(Pair("ChecklistProgress", binding.featureChecklistProgress.isChecked))

            TaskFeatures.updateTaskFeatures(fragmentContext, newFeatureList)
            setTaskSortOrder(fragmentContext, newFeatureList)

            Toast.makeText(fragmentContext, "Task features saved", Toast.LENGTH_SHORT).show()
        }

        //Save task sort order list
        binding.btnSaveTaskSorting.setOnClickListener {
            TaskSortOrder.main(fragmentContext, "Update", GenerateTaskOrderList.getSortOrderList())
            Toast.makeText(fragmentContext, "Task sort order saved", Toast.LENGTH_SHORT).show()
        }

        //Incomplete and Pending default status buttons
        binding.buttonIncompleteStatus.setOnClickListener {
            GenerateTaskOrderList.setSortOrderValue(fragmentContext, binding.buttonIncompleteStatus, binding.labelIncompleteStatus)
        }
        binding.buttonPendingStatus.setOnClickListener {
            GenerateTaskOrderList.setSortOrderValue(fragmentContext, binding.buttonPendingStatus, binding.labelPendingStatus)
        }

        //Save task color settings
        binding.btnSaveColours.setOnClickListener {
            //Convert the color strings into usable array
            val newColorList: ArrayList<Pair<String, String>> = ArrayList()
            newColorList.add(Pair("overdue", binding.editColourOverdue.text.toString()))
            newColorList.add(Pair("today", binding.editColourToday.text.toString()))
            newColorList.add(Pair("tomorrow", binding.editColourTomorrow.text.toString()))
            newColorList.add(Pair("threeDays", binding.editColourThreeDays.text.toString()))
            newColorList.add(Pair("week", binding.editColourWeek.text.toString()))
            newColorList.add(Pair("weekPlus", binding.editColourWeekPlus.text.toString()))
            newColorList.add(Pair("conditional", binding.editColourConditional.text.toString()))
            newColorList.add(Pair("pending", binding.editColourPending.text.toString()))
            newColorList.add(Pair("completed", binding.editColourComplete.text.toString()))

            TaskColors.main(fragmentContext, "Update", newColorList)
            Toast.makeText(fragmentContext, "Task colors saved", Toast.LENGTH_SHORT).show()
        }

        //Save Archive delete function settings
        binding.btnSaveArchiveDelete.setOnClickListener {
            //Check whether any value has been inputted if after days has been selected
            if(binding.radioDeleteAfter.isChecked && binding.editDeleteAfter.text.isNotEmpty()){
                val afterDays = binding.editDeleteAfter.text.toString().toInt()
                GenerateProtectionDialogue.main(fragmentContext, this, activity, "ArchiveDelete", "After $afterDays days", R.layout.archive_delete_dialog)
            }
            else if(binding.radioDeleteNever.isChecked){
                SettingsListCURD.main(fragmentContext, "Update", "Archive_Delete", "never")
                Toast.makeText(fragmentContext, "Task archive auto delete saved", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(fragmentContext, "Archive delete setting must include number of days", Toast.LENGTH_SHORT).show()
            }

        }

        //Reset settings to defaults
        binding.btnDefaultReset.setOnClickListener {
            GenerateProtectionDialogue.main(fragmentContext, this, activity, "ResetSettings", null, R.layout.reset_dialog)
        }
    }

    override fun onStart() {
        super.onStart()
        val fragmentContext = requireContext().applicationContext
        val featureList = setTaskFeatures(fragmentContext)
        setTaskSortOrder(fragmentContext, featureList)
        setTaskColors(fragmentContext)
        setArchiveRadioListeners(fragmentContext)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setTaskFeatures(c: Context): ArrayList<Pair<String, Boolean>>?{
        println("--- Set Task Features ---")
        //Set the current values for task features
        val featureList = TaskFeatures.readTaskFeatureList(c).second

        if (featureList != null) {
            for(feature in featureList){
                when(feature.first){
                    "Motivation" -> binding.featureMotivation.isChecked = feature.second
                    "Complexity" -> binding.featureComplexity.isChecked = feature.second
                    "Checklist" -> binding.featureChecklist.isChecked = feature.second
                    "Repeating" -> binding.featureRepeat.isChecked = feature.second
                    "Conditions" -> binding.featureCondition.isChecked = feature.second
                    "TimeProgress" -> binding.featureTimeProgress.isChecked = feature.second
                    "ChecklistProgress" -> binding.featureChecklistProgress.isChecked = feature.second
                }
            }

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

        return featureList
    }

    private fun setTaskSortOrder(c: Context, featureList: ArrayList<Pair<String, Boolean>>?){
        println("--- Set Task Sort Order ---")
        //Set the current values for task sort order
        val orderList = TaskSortOrder.main(c, "Read", null)
        GenerateTaskOrderList.main(c, binding.taskSortOrderList, orderList, featureList)

        //Incomplete and Pending default button and textview states
        binding.labelIncompleteStatus.text = orderList.second!![0].first
        binding.labelPendingStatus.text = orderList.second!![1].first
        val btnResInComplete = if(orderList.second!![0].first.contains("High")) R.mipmap.ic_menu_high else R.mipmap.ic_menu_low
        val btnResPending = if(orderList.second!![1].first.contains("High")) R.mipmap.ic_menu_high else R.mipmap.ic_menu_low
        binding.buttonIncompleteStatus.background = ContextCompat.getDrawable(c, btnResInComplete)
        binding.buttonPendingStatus.background = ContextCompat.getDrawable(c, btnResPending)
    }

    private fun setTaskColors(c: Context){
        println("--- Set Task Colors ---")
        //Set the current values for task colors
        val readColorSettings = TaskColors.main(c, "Read", null)
        if(readColorSettings.first){
            val colorList = readColorSettings.second
            if (colorList != null) {
                for(color in colorList){
                    when(color.first){
                        "overdue" -> initiateEditColorGroup(binding.editColourOverdue, binding.buttonColourOverdue, color.second)
                        "today" -> initiateEditColorGroup(binding.editColourToday, binding.buttonColourToday, color.second)
                        "tomorrow" -> initiateEditColorGroup(binding.editColourTomorrow, binding.buttonColourTomorrow, color.second)
                        "threeDays" -> initiateEditColorGroup(binding.editColourThreeDays, binding.buttonColourThreeDays, color.second)
                        "week" -> initiateEditColorGroup(binding.editColourWeek, binding.buttonColourWeek, color.second)
                        "weekPlus" -> initiateEditColorGroup(binding.editColourWeekPlus, binding.buttonColourWeekPlus, color.second)
                        "conditional" -> initiateEditColorGroup(binding.editColourConditional, binding.buttonColourConditional, color.second)
                        "pending" -> initiateEditColorGroup(binding.editColourPending, binding.buttonColourPending, color.second)
                        "completed" -> initiateEditColorGroup(binding.editColourComplete, binding.buttonColourComplete, color.second)
                        "timePB" -> initiateEditColorGroup(binding.editColourTimePB, binding.buttonColourTimePB, color.second)
                        "checklistPB" -> initiateEditColorGroup(binding.editColourChecklistPB, binding.buttonColourChecklistPB, color.second)
                    }
                }
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
                    button.setBackgroundColor(editTextColor)
                }catch(_:Exception){}
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setArchiveRadioListeners(c:Context){
        //Populate radio values from settings
        val archiveDeleteSetting = SettingsListCURD.main(c, "Read", "Archive_Delete", null).second!![0]
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
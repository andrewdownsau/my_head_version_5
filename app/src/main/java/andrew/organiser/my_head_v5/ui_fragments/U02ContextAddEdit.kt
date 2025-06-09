package andrew.organiser.my_head_v5.ui_fragments

import andrew.organiser.my_head_v5.R
import andrew.organiser.my_head_v5.SettingsButtonStateManager
import andrew.organiser.my_head_v5.databinding.ContextAddEditBinding
import andrew.organiser.my_head_v5.features.data_manipulation.D03ContextList
import andrew.organiser.my_head_v5.features.ui_generation.Gen03ProtectionDialogue
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController


class U02ContextAddEdit : Fragment() {

    //Global binding to xml layout
    private var _binding: ContextAddEditBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ContextAddEditBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        println("--- UI Context Add/Edit: onViewCreated ---")
        super.onViewCreated(view, savedInstanceState)
        (activity as? SettingsButtonStateManager)?.setVisible("Context_Add_Edit")
        val fragmentContext = requireContext().applicationContext

        //Close button redirect
        binding.btnClose.setOnClickListener {
            findNavController().popBackStack()
        }

        //Setting name by lookup if in edit mode
        val originalName = arguments?.getString("contextName")
        val excluded = if(originalName != null) D03ContextList.excludedFromName(originalName) else false
        binding.checkboxContextExclude.isChecked = excluded

        //Saving functionality
        binding.btnSave.setOnClickListener {
            val newName = binding.editContextName.text.toString()
            if(newName.length in 1..59){
                if(D03ContextList.save(fragmentContext, newName, originalName, binding.checkboxContextExclude.isChecked))
                    findNavController().popBackStack()
            }
            else binding.labelWarning.visibility = View.VISIBLE
        }

        //--- Delete button enabled dependant on create and edit mode
        if(originalName == null){
            //Delete button is disabled and has no onClick function
            binding.btnDelete.setBackgroundResource(R.drawable.shadow_button_disabled)
            binding.btnDelete.setTextColor(Color.parseColor("#555555"))
        }
        // --- Edit Mode ---
        else{
            binding.editContextName.setText(originalName)

            //Delete button deletes entry and tasks attached to context
            binding.btnDelete.setOnClickListener {
                Gen03ProtectionDialogue.main(fragmentContext, this, activity, "ContextDelete", originalName, R.layout.delete_dialog)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
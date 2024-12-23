package andrew.organiser.myhead_v3.ui_fragments

import andrew.organiser.myhead_v3.R
import andrew.organiser.myhead_v3.SettingsButtonStateManager
import andrew.organiser.myhead_v3.databinding.ContextAddEditBinding
import andrew.organiser.myhead_v3.features.crud.ContextListCURD
import andrew.organiser.myhead_v3.features.ui_generation.GenerateProtectionDialogue
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

        //--- Create New Mode ---
        if(originalName == null){
            //Save button file write and redirect
            binding.btnSave.setOnClickListener {
                if(binding.editContextName.text.toString().length in 1..59){
                    ContextListCURD.main(fragmentContext, "Create", null, binding.editContextName.text.toString())
                    findNavController().popBackStack()
                }
                else binding.labelWarning.visibility = View.VISIBLE
            }

            //Delete button is disabled and has no onClick function
            binding.btnDelete.setBackgroundResource(R.drawable.shadow_button_disabled)
            binding.btnDelete.setTextColor(Color.parseColor("#555555"))
        }
        // --- Edit Mode ---
        else{
            binding.editContextName.setText(originalName)

            //Save button file write and redirect
            binding.btnSave.setOnClickListener {
                //Save edited entry to context list
                if(binding.editContextName.text.toString().length in 1..59){
                    ContextListCURD.main(fragmentContext, "Update", originalName, binding.editContextName.text.toString())
                    findNavController().popBackStack()
                }
                else binding.labelWarning.visibility = View.VISIBLE
            }

            //Delete button deletes entry and tasks attached to context
            binding.btnDelete.setOnClickListener {
                GenerateProtectionDialogue.main(fragmentContext, this, activity, "ContextDelete", originalName, R.layout.delete_dialog)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
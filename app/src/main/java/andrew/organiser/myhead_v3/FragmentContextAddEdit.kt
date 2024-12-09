package andrew.organiser.myhead_v3

import andrew.organiser.myhead_v3.databinding.ContextAddEditBinding
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController


class FragmentContextAddEdit : Fragment() {

    //Global binding to xml layout
    private var _binding: ContextAddEditBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ContextAddEditBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("---Context Add/Edit ---")
        val fragmentContext = requireContext().applicationContext
        val dbHandler = DBHandler(fragmentContext)

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
                dbHandler.addNewContext(fragmentContext,binding.editContextName.text.toString())
                findNavController().popBackStack()
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
                dbHandler.updateContext(fragmentContext, originalName, binding.editContextName.text.toString())
                findNavController().popBackStack()

            }

            //Delete button deletes entry and tasks attached to context
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
                    dbHandler.deleteContext(originalName)
                    messageBoxInstance.dismiss()
                    findNavController().popBackStack()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package andrew.organiser.my_head_v5.ui_fragments

import andrew.organiser.my_head_v5.R
import andrew.organiser.my_head_v5.SettingsButtonStateManager
import andrew.organiser.my_head_v5.databinding.ContextListBinding
import andrew.organiser.my_head_v5.features.data_manipulation.D01ActiveData
import andrew.organiser.my_head_v5.features.data_manipulation.D03ContextList
import andrew.organiser.my_head_v5.features.data_manipulation.D07OrderUIList
import andrew.organiser.my_head_v5.features.ui_generation.Gen02ContextUIList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

/**
 * Context List for organiser, navigate to by default and redirects if context check result is valid
 */
class U01ContextList : Fragment() {

    //Global binding to xml layout
    private var _binding: ContextListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ContextListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        println("--- UI Context List: onViewCreated ---")
        super.onViewCreated(view, savedInstanceState)
        (activity as? SettingsButtonStateManager)?.setVisible("Context_List")
        val fragmentContext = requireContext().applicationContext

        //Navigate to add/edit fragment using plus button
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_ContextList_to_ContextAddEdit)
        }

        //Navigate to master list using 2nd fab if any contexts exist
        binding.fabUrgentList.setOnClickListener {
            if(D03ContextList.read().isNotEmpty()){
                findNavController().navigate(R.id.action_ContextList_to_TaskList)
            }
            else{ Toast.makeText(fragmentContext, "Cannot access master task list without at least 1 context", Toast.LENGTH_LONG).show()}
        }
    }

    override fun onStart() {
        super.onStart()
        println("--- UI Context List: onStart ---")
        val fragmentContext = requireContext().applicationContext

        //Initialise or refresh the active data
        D01ActiveData.activeDataCheck(fragmentContext)

        //Check context file, create list if items exist
        val contextList = D03ContextList.read()
        if(contextList.isEmpty()) {
            binding.subtitleText.visibility = View.VISIBLE
        }
        else {
            binding.subtitleText.visibility = View.GONE

            //Sort and add background color status
            val sortedContextList = D07OrderUIList.sortContextList(contextList)
            Gen02ContextUIList.main(fragmentContext, this, binding.contextListLayout, sortedContextList)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
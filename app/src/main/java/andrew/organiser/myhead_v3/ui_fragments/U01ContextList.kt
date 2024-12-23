package andrew.organiser.myhead_v3.ui_fragments

import andrew.organiser.myhead_v3.DBHandler
import andrew.organiser.myhead_v3.R
import andrew.organiser.myhead_v3.SettingsButtonStateManager
import andrew.organiser.myhead_v3.databinding.ContextListBinding
import andrew.organiser.myhead_v3.features.crud.ContextListCURD
import andrew.organiser.myhead_v3.features.settings.TaskFeatures
import andrew.organiser.myhead_v3.features.ui_generation.ArchiveClear
import andrew.organiser.myhead_v3.features.ui_generation.GenerateContextUIList
import andrew.organiser.myhead_v3.features.ui_generation.GenerateTaskUIList
import andrew.organiser.myhead_v3.features.ui_generation.RepeatRefresh
import andrew.organiser.myhead_v3.features.ui_generation.SortUIList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.Companion
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
            val contextRead = ContextListCURD.main(fragmentContext, "Read", null, null)
            if(contextRead.first && !contextRead.second.isNullOrEmpty()){
                findNavController().navigate(R.id.action_ContextList_to_TaskList)
            }
            else{ Toast.makeText(fragmentContext, "Cannot access master task list without at least 1 context", Toast.LENGTH_LONG).show()}
        }
    }

    override fun onStart() {
        super.onStart()
        println("--- UI Context List: onStart ---")
        val fragmentContext = requireContext().applicationContext

        //Check if any tasks that have been archived need to be refreshed if repeat is active
        RepeatRefresh.repeatRefreshCheck(fragmentContext)
        ArchiveClear.archiveClearCheck(fragmentContext)

        //Check context file, create list if items exist
        val contextList = ContextListCURD.main(fragmentContext, "Read", null, null).second
        if(contextList.isNullOrEmpty()) {
            binding.subtitleText.visibility = View.VISIBLE
        }
        else {
            binding.subtitleText.visibility = View.GONE

            //Sort and add background color status
            val sortedContextList = SortUIList.main(fragmentContext, "sortContextList", contextList, null).first
            if (sortedContextList != null) { GenerateContextUIList.main(fragmentContext, this, binding.contextListLayout, sortedContextList) }
            else{ GenerateContextUIList.main(fragmentContext, this, binding.contextListLayout, contextList) }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
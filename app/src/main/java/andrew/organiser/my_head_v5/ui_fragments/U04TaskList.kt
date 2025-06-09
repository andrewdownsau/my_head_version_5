package andrew.organiser.my_head_v5.ui_fragments

import andrew.organiser.my_head_v5.DBHandler
import andrew.organiser.my_head_v5.R
import andrew.organiser.my_head_v5.SettingsButtonStateManager
import andrew.organiser.my_head_v5.databinding.TaskListBinding
import andrew.organiser.my_head_v5.features.data_manipulation.D01ActiveData
import andrew.organiser.my_head_v5.features.ui_generation.Gen04TaskUIList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class U04TaskList : Fragment() {

    private var _binding: TaskListBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private var dbHandler: DBHandler? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        println("--- Task List: onCreateView ---")
        (activity as? SettingsButtonStateManager)?.setVisible("Task_List")
        _binding = TaskListBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onStart() {
        super.onStart()
        println("--- Task List: onStart ---")
        val c = requireContext().applicationContext
        dbHandler = DBHandler(c)

        //Initialise or refresh the active data
        D01ActiveData.activeDataCheck(c)

        //Extract context bundle values from parent bundle
        val contextId = arguments?.getInt("contextId")
        val contextName = arguments?.getString("contextName")
        if (contextId != null && contextName != null) {
            //Set actionbar title to context name
            (activity as AppCompatActivity).supportActionBar?.title = "$contextName Tasks"

            //Check task file, create list if items exist
            Gen04TaskUIList.main(c, this, binding.taskListLayout, binding.subtitleTaskText, "Context_Active", contextId)

            //Navigate to add edit fragment using plus button
            val contextBundle = bundleOf("contextName" to contextName)
            binding.fabTaskAdd.setOnClickListener {
                findNavController().navigate(R.id.action_TaskList_to_TaskAddEdit, contextBundle)
            }

            //Navigate to archive using 2nd fab
            binding.fabTaskArchive.setOnClickListener {
                Gen04TaskUIList.main(c, this, binding.taskListLayout, binding.subtitleTaskText, "Context_Archive", contextId)
                binding.fabTaskArchive.visibility = View.GONE
                binding.fabTaskReturn.visibility = View.VISIBLE
            }

            //Navigate back to normal task list with return
            binding.fabTaskReturn.setOnClickListener {
                Gen04TaskUIList.main(c, this, binding.taskListLayout, binding.subtitleTaskText, "Context_Active", contextId)
                binding.fabTaskReturn.visibility = View.GONE
                binding.fabTaskArchive.visibility = View.VISIBLE
            }
        }
        //Else this is the master task list that contains all the non-completed tasks
        else{
            //Set actionbar title to master task list
            (activity as AppCompatActivity).supportActionBar?.title = "Master Task List"

            //Check task file, create list if items exist
            Gen04TaskUIList.main(c, this, binding.taskListLayout, binding.subtitleTaskText, "Master_Active", contextId)

            //Navigate to add edit fragment using plus button
            binding.fabTaskAdd.setOnClickListener {
                findNavController().navigate(R.id.action_TaskList_to_TaskAddEdit)
            }

            //Navigate to archive using 2nd fab
            binding.fabTaskArchive.setOnClickListener {
                Gen04TaskUIList.main(c, this, binding.taskListLayout, binding.subtitleTaskText, "Master_Archive", contextId)
                binding.fabTaskArchive.visibility = View.GONE
                binding.fabTaskReturn.visibility = View.VISIBLE
            }

            //Navigate back to normal task list with return
            binding.fabTaskReturn.setOnClickListener {
                Gen04TaskUIList.main(c, this, binding.taskListLayout, binding.subtitleTaskText, "Master_Active", contextId)
                binding.fabTaskReturn.visibility = View.GONE
                binding.fabTaskArchive.visibility = View.VISIBLE
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
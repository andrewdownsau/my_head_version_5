package andrew.organiser.myhead_v3.ui_fragments

import andrew.organiser.myhead_v3.DBHandler
import andrew.organiser.myhead_v3.R
import andrew.organiser.myhead_v3.SettingsButtonStateManager
import andrew.organiser.myhead_v3.databinding.TaskListBinding
import andrew.organiser.myhead_v3.features.crud.TaskListCURD
import andrew.organiser.myhead_v3.features.settings.TaskFeatures
import andrew.organiser.myhead_v3.features.ui_generation.ArchiveClear
import andrew.organiser.myhead_v3.features.ui_generation.GenerateTaskUIList
import andrew.organiser.myhead_v3.features.ui_generation.RepeatRefresh
import andrew.organiser.myhead_v3.features.ui_generation.SortUIList
import android.content.Context
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
        val fragmentContext = requireContext().applicationContext
        dbHandler = DBHandler(fragmentContext)

        //Check if any tasks that have been archived need to be refreshed if repeat is active
        RepeatRefresh.repeatRefreshCheck(fragmentContext)
        ArchiveClear.archiveClearCheck(fragmentContext)

        //Extract context bundle values from parent bundle
        val contextId = arguments?.getInt("contextId")
        val contextName = arguments?.getString("contextName")
        if (contextId != null && contextName != null) {
            //Set actionbar title to context name
            (activity as AppCompatActivity).supportActionBar?.title = "$contextName Tasks"

            //Check task file, create list if items exist
            createTaskListFromDB(fragmentContext, contextId.toString())

            //Navigate to add edit fragment using plus button
            val contextBundle = bundleOf("contextName" to contextName)
            binding.fabTaskAdd.setOnClickListener {
                findNavController().navigate(R.id.action_TaskList_to_TaskAddEdit, contextBundle)
            }

            //Navigate to archive using 2nd fab
            binding.fabTaskArchive.setOnClickListener {
                createTaskListFromDB(fragmentContext, "${contextId}_Archive")
                binding.fabTaskArchive.visibility = View.GONE
                binding.fabTaskReturn.visibility = View.VISIBLE
            }

            //Navigate back to normal task list with return
            binding.fabTaskReturn.setOnClickListener {
                createTaskListFromDB(fragmentContext, contextId.toString())
                binding.fabTaskReturn.visibility = View.GONE
                binding.fabTaskArchive.visibility = View.VISIBLE
            }
        }
        //Else this is the master task list that contains all the non-completed tasks
        else{
            //Set actionbar title to master task list
            (activity as AppCompatActivity).supportActionBar?.title = "Master Task List"

            //Check task file, create list if items exist
            createTaskListFromDB(fragmentContext, "Incomplete")

            //Navigate to add edit fragment using plus button
            binding.fabTaskAdd.setOnClickListener {
                findNavController().navigate(R.id.action_TaskList_to_TaskAddEdit)
            }

            //Navigate to archive using 2nd fab
            binding.fabTaskArchive.setOnClickListener {
                createTaskListFromDB(fragmentContext, "null_Archive")
                binding.fabTaskArchive.visibility = View.GONE
                binding.fabTaskReturn.visibility = View.VISIBLE
            }

            //Navigate back to normal task list with return
            binding.fabTaskReturn.setOnClickListener {
                createTaskListFromDB(fragmentContext, "Incomplete")
                binding.fabTaskReturn.visibility = View.GONE
                binding.fabTaskArchive.visibility = View.VISIBLE
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createTaskListFromDB(c: Context, filter: String?){
        val taskList = TaskListCURD.main(c, "Read", null, null, filter).second
        if (taskList.isNullOrEmpty()) {
            val subtitleText = binding.subtitleTaskText
            binding.taskListLayout.removeAllViews()
            binding.taskListLayout.addView(subtitleText)
            subtitleText.visibility = View.VISIBLE
        } else {
            binding.subtitleTaskText.visibility = View.GONE

            //Sort and add background color status
            val sortedTaskList = SortUIList.main(c, "sortTaskList", null, taskList).second
            if (sortedTaskList != null) { GenerateTaskUIList.main(c, this, binding.taskListLayout, sortedTaskList) }
            else{ GenerateTaskUIList.main(c, this, binding.taskListLayout, taskList) }
        }
    }
}
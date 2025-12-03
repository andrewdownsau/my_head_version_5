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
    private var taskListTypeActive:Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        println("--- Task List: onCreateView ---")
        (activity as? SettingsButtonStateManager)?.setVisible("Task_List")
        _binding = TaskListBinding.inflate(inflater, container, false)
        onSaveInstanceState(bundleOf())

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("taskListTypeActive", taskListTypeActive)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        println("--- Task List: onViewStateRestored ---")
        super.onViewStateRestored(savedInstanceState)
        if(savedInstanceState?.getBoolean("taskListTypeActive") != null && savedInstanceState.containsKey("taskListTypeActive")){
            taskListTypeActive = savedInstanceState.getBoolean("taskListTypeActive")
        }
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

        //Set filter key and title based on task list type and bundle
        var filterKey = if(contextId != null && contextName != null) "Context" else "Master"
        filterKey += if(taskListTypeActive) "_Active" else "_Archive"
        val listTitle = if(contextId != null && contextName != null) "$contextName Tasks" else "Master Task List"

        //Set actionbar title to context name
        (activity as AppCompatActivity).supportActionBar?.title = listTitle

        //Check task file, create list if items exist
        Gen04TaskUIList.main(c, this, binding.taskListLayout, binding.subtitleTaskText, filterKey, contextId)
        setFabInputs()

        //Navigate to add edit fragment using plus button
        val navigationBundle = bundleOf()
        if(contextId != null && contextName != null) navigationBundle.putString("contextName", contextName)
        binding.fabTaskAdd.setOnClickListener {
            onSaveInstanceState(bundleOf("taskListTypeActive" to taskListTypeActive))
            findNavController().navigate(R.id.action_TaskList_to_TaskAddEdit, navigationBundle)
        }

        //Navigate to archive using 2nd fab
        binding.fabTaskArchive.setOnClickListener {
            taskListTypeActive = false
            filterKey = filterKey.split("_")[0] + "_Archive"
            Gen04TaskUIList.main(c, this, binding.taskListLayout, binding.subtitleTaskText, filterKey, contextId)
            setFabInputs()
        }

        //Navigate back to normal task list with return
        binding.fabTaskReturn.setOnClickListener {
            taskListTypeActive = true
            filterKey = filterKey.split("_")[0] + "_Active"
            Gen04TaskUIList.main(c, this, binding.taskListLayout, binding.subtitleTaskText, filterKey, contextId)
            setFabInputs()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setFabInputs(){
        binding.fabTaskReturn.visibility = if(taskListTypeActive) View.GONE else View.VISIBLE
        binding.fabTaskArchive.visibility = if(taskListTypeActive) View.VISIBLE else View.GONE
        binding.fabTaskAdd.visibility = if(taskListTypeActive) View.VISIBLE else View.GONE
    }
}
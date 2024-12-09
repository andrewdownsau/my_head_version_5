package andrew.organiser.myhead_v3

import andrew.organiser.myhead_v3.databinding.TaskListBinding
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.time.LocalDate

class FragmentTaskList : Fragment() {

    private var _binding: TaskListBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private var dbHandler: DBHandler? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = TaskListBinding.inflate(inflater, container, false)

        return binding.root

    }

    override fun onStart() {
        super.onStart()
        val fragmentContext = requireContext().applicationContext
        dbHandler = DBHandler(fragmentContext)
        dbHandler?.repeatRefreshCheck(fragmentContext)
        generateTaskList(fragmentContext)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun generateTaskList(c: Context){
        //Extract context id and name from fragment bundle
        val contextId = arguments?.getInt("contextId")
        val contextName = arguments?.getString("contextName")
        if(contextId != null && contextName != null){
            println("--- Task List for: $contextName ---")
            val contextBundle = bundleOf("contextId" to contextId, "contextName" to contextName)

            //Set actionbar title
            (activity as AppCompatActivity).supportActionBar?.title = "$contextName Tasks"

            //Navigate to add edit fragment using plus button
            binding.fabTaskAdd.setOnClickListener {
                findNavController().navigate(R.id.action_TaskList_to_TaskAddEdit, contextBundle)
            }

            //Navigate to archive using 2nd fab
            binding.fabTaskArchive.setOnClickListener {
                findNavController().navigate(R.id.action_TaskList_to_TaskArchive, contextBundle)
            }

            //Lookup tasks that are children of context that are not archived as completed
            val taskListWhereClause = " WHERE (${DBHandler.TASK_COMPLETED_FLAG_COL} = 0" +
                    " OR ${DBHandler.TASK_COMPLETED_DATE_COL} = '${LocalDate.now().format(MainActivity.DATE_FORMAT)}')"+
                    " AND ${DBHandler.TASK_CONTEXT_ID_COL}=$contextId"
            val taskList = dbHandler?.readTaskList(taskListWhereClause, true, false)
            if(!taskList.isNullOrEmpty()){
                binding.subtitleTaskText.visibility = View.GONE

                //Create list of task buttons from file in order
                UIHelper.createTaskButtonList(c, binding.taskListLayout, taskList, taskListWhereClause, findNavController())
            }
            else{
                binding.subtitleTaskText.visibility = View.VISIBLE
            }
        }
    }
}
package andrew.organiser.myhead_v3

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import andrew.organiser.myhead_v3.databinding.MasterListBinding
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import java.time.LocalDate

/**
 * Context List for organiser, navigates by default and redirects if context check result is valid
 */
class FragmentMasterList : Fragment() {

    private var _binding: MasterListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var dbHandler: DBHandler? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = MasterListBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onStart() {
        println("--- Master List: onStart ---")
        super.onStart()
        val fragmentContext = requireContext().applicationContext
        dbHandler = DBHandler(fragmentContext)

        //Check if any tasks that have been archived need to be refreshed
        dbHandler?.repeatRefreshCheck(fragmentContext)

        //Lookup all tasks in table
        val taskListWhereClause = " WHERE (${DBHandler.TASK_COMPLETED_FLAG_COL} = 0 OR ${DBHandler.TASK_COMPLETED_DATE_COL} = '${LocalDate.now().format(MainActivity.DATE_FORMAT)}')"
        val taskList = dbHandler?.readTaskList(taskListWhereClause, true, false)
        if(!taskList.isNullOrEmpty()){
            binding.subtitleText.visibility = View.GONE

            //Create list of task buttons from file in order
            UIHelper.createTaskButtonList(fragmentContext, binding.masterListLayout, taskList, taskListWhereClause, findNavController())

            //Navigate to add edit fragment using plus button
            val taskBundle = bundleOf("taskObjectId" to 0)
            binding.fabTaskAdd.setOnClickListener {
                findNavController().navigate(R.id.action_MasterList_to_TaskAddEdit, taskBundle)
            }
        }
        else{
            binding.subtitleText.visibility = View.VISIBLE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        println("--- Master List: onViewCreated ---")
        super.onViewCreated(view, savedInstanceState)
        
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
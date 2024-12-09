package andrew.organiser.myhead_v3

import andrew.organiser.myhead_v3.databinding.TaskArchiveBinding
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import java.time.LocalDate

class FragmentTaskArchive : Fragment() {

    private var _binding: TaskArchiveBinding? = null
    private val binding get() = _binding!!
    private var dbHandler: DBHandler? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = TaskArchiveBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val fragmentContext = requireContext().applicationContext
        dbHandler = DBHandler(fragmentContext)
        dbHandler?.repeatRefreshCheck(fragmentContext)
        generateArchiveList(fragmentContext)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun generateArchiveList(c: Context){
        //Extract context id and name from fragment bundle
        val contextId = arguments?.getInt("contextId")
        val contextName = arguments?.getString("contextName")
        if(contextId != 0 && contextName != null){
            println("--- Archive List for: $contextId - $contextName ---")

            (activity as AppCompatActivity).supportActionBar?.title = "$contextName Archive"

            //Lookup tasks that are children of context that are archived as completed
            val archiveListWhereClause = " WHERE ${DBHandler.TASK_CONTEXT_ID_COL}=$contextId AND " +
                    "NOT ${DBHandler.TASK_COMPLETED_FLAG_COL} = 0 AND " +
                    "NOT ${DBHandler.TASK_COMPLETED_DATE_COL} = '${LocalDate.now().format(MainActivity.DATE_FORMAT)}'"
            val archiveList = dbHandler?.readTaskList(archiveListWhereClause, false, true)
            if(!archiveList.isNullOrEmpty()){
                binding.subtitleTaskText.visibility = View.GONE

                //Create list of task buttons from file in order
                UIHelper.createTaskButtonList(c, binding.taskListLayout, archiveList, archiveListWhereClause, findNavController())
            }
            else{
                binding.subtitleTaskText.visibility = View.VISIBLE
            }
        }
    }
}
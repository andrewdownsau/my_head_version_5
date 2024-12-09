package andrew.organiser.myhead_v3

import andrew.organiser.myhead_v3.databinding.ContextListBinding
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import java.time.LocalDate

/**
 * Context List for organiser, navigate to by default and redirects if context check result is valid
 */
class FragmentContextList : Fragment() {

    //Global binding to xml layout
    private var _binding: ContextListBinding? = null
    private val binding get() = _binding!!

    //Global access to database instance and activity context
    private var dbHandler: DBHandler? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ContextListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val fragmentContext = requireContext().applicationContext
        dbHandler = DBHandler(fragmentContext)
        //Check if any tasks that have been archived need to be refreshed
        dbHandler?.repeatRefreshCheck(fragmentContext)

        //Check context file, create list if items exist
        val contextList = dbHandler?.readContextList(true)
        if(contextList.isNullOrEmpty()) {
            binding.subtitleText.visibility = View.VISIBLE
        }
        else {
            binding.subtitleText.visibility = View.GONE
            generateContextList(fragmentContext, binding.contextListLayout, contextList)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        println("--- Context List: onViewCreated ---")
        super.onViewCreated(view, savedInstanceState)

        //Navigate to add/edit fragment using plus button
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_ContextList_to_ContextAddEdit)
        }

        //Navigate to urgent list using 2nd fab
        binding.fabUrgentList.setOnClickListener {
            findNavController().navigate(R.id.action_ContextList_to_MasterList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun generateContextList(c: Context, layout: ConstraintLayout, contextList: ArrayList<ContextModal>){
        println("+++ generateContextList  +++")
        var lastBtnId = 0

        // Clear all views from layout and add all buttons from contents
        layout.removeAllViews()

        contextList.forEach {
            val contextId = it.id
            val contextName = it.getContextName()
            if (contextName.isNotEmpty()) {
                lastBtnId = createContextButton(c, layout, lastBtnId, contextId, contextName, findNavController())
            }
        }
    }

    private fun createContextButton(c: Context, layout: ConstraintLayout, lastBtnId: Int, contextId: Int, contextName: String, navCtrl: NavController) : Int {
        println("+++ createContextButton: $contextName +++")
        val dynamicButton = UIHelper.createGenericButton(c, contextName)
        val contextTaskList = dbHandler?.readTaskList(" WHERE ${DBHandler.TASK_CONTEXT_ID_COL}=$contextId" +
                " AND (${DBHandler.TASK_COMPLETED_FLAG_COL} = 0 " +
                " OR ${DBHandler.TASK_COMPLETED_DATE_COL} = '${LocalDate.now().format(MainActivity.DATE_FORMAT)}')", true, false)
        if(!contextTaskList.isNullOrEmpty()) UIHelper.setButtonBackground(c, contextTaskList[0],dynamicButton)
        else UIHelper.setButtonBackground(c, null, dynamicButton)
        UIHelper.setButtonToLayout(layout, dynamicButton, lastBtnId)

        //Set on long click listener to trigger edit page function for non-tasks
        val contextBundle = bundleOf("contextId" to contextId, "contextName" to contextName)
        dynamicButton.setOnLongClickListener {
            navCtrl.navigate(R.id.action_ContextList_to_ContextAddEdit, contextBundle)
            true
        }
        //Set onclick listener to send to task list fragment
        dynamicButton.setOnClickListener {
            navCtrl.navigate(R.id.action_ContextList_to_TaskList, contextBundle)
        }

        //Return button id for next button position
        return dynamicButton.id
    }
}
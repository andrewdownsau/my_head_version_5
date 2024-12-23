package andrew.organiser.myhead_v3.ui_fragments

import andrew.organiser.myhead_v3.DBHandler
import andrew.organiser.myhead_v3.R
import andrew.organiser.myhead_v3.SettingsButtonStateManager
import andrew.organiser.myhead_v3.databinding.HelpGuideBinding
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment


/*
 * Help Guide Fragment
 */
class U06HelpGuide : Fragment() {

    private var _binding: HelpGuideBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private var dbHandler: DBHandler? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = HelpGuideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        println("--- Help Guide: onViewCreated ---")
        super.onViewCreated(view, savedInstanceState)
        (activity as? SettingsButtonStateManager)?.setVisible("Help_Guide")

        val fragmentContext = requireContext().applicationContext
        dbHandler = DBHandler(fragmentContext)

        //Expand the body textView when any part of the title area is clicked
        setLayoutTopicClickListener(fragmentContext, binding.layoutGeneralIntro, binding.layoutBodyGeneralIntro, binding.collapseGeneralIntro)
        setLayoutTopicClickListener(fragmentContext, binding.layoutCrudContext, binding.layoutBodyCrudContext, binding.collapseCrudContext)
        setLayoutTopicClickListener(fragmentContext, binding.layoutCrudTasks, binding.layoutBodyCrudTasks, binding.collapseCrudTasks)
        setLayoutTopicClickListener(fragmentContext, binding.layoutCompletingTasks, binding.layoutBodyCompletingTasks, binding.collapseCompletingTasks)
        setLayoutTopicClickListener(fragmentContext, binding.layoutTaskArchive, binding.layoutBodyTaskArchive, binding.collapseTaskArchive)
        setLayoutTopicClickListener(fragmentContext, binding.layoutTaskStatus, binding.layoutBodyTaskStatus, binding.collapseTaskStatus)
        setLayoutTopicClickListener(fragmentContext, binding.layoutTaskSliders, binding.layoutBodyTaskSliders, binding.collapseTaskSliders)
        setLayoutTopicClickListener(fragmentContext, binding.layoutTaskChecklist, binding.layoutBodyTaskChecklist, binding.collapseTaskChecklist)
        setLayoutTopicClickListener(fragmentContext, binding.layoutRepeatingTasks, binding.layoutBodyRepeatingTasks, binding.collapseRepeatingTasks)
        setLayoutTopicClickListener(fragmentContext, binding.layoutConditionalTasks, binding.layoutBodyConditionalTasks, binding.collapseConditionalTasks)
        setLayoutTopicClickListener(fragmentContext, binding.layoutTaskOrder, binding.layoutBodyTaskOrder, binding.collapseTaskOrder)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setLayoutTopicClickListener(c: Context, layout: ConstraintLayout, bodyLayout: ConstraintLayout, button: Button){
        layout.setOnClickListener{
            val layoutCollapsed = bodyLayout.visibility == View.GONE
            if(layoutCollapsed){
                button.background = ContextCompat.getDrawable(c, R.drawable.ic_menu_down)
                bodyLayout.visibility = View.VISIBLE
            }
            else{
                button.background = ContextCompat.getDrawable(c, R.drawable.ic_menu_up)
                bodyLayout.visibility = View.GONE
            }
        }
    }

}
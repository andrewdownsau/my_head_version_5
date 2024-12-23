package andrew.organiser.myhead_v3.features.ui_generation


import andrew.organiser.myhead_v3.modals.ContextModal
import andrew.organiser.myhead_v3.R
import android.content.Context
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController


class GenerateContextUIList {

    companion object {

        // --- Generate Context UI Button List --- //
        fun main(c: Context, f:Fragment, layout: ConstraintLayout, contextList: ArrayList<ContextModal>){
            println("=== Gen02 - Generate Context UI Button List ===")
            var lastBtnId = 0

            // Clear all views from layout and add all buttons from contents
            layout.removeAllViews()

            //Use last button ID to set the top constraint of the next button
            contextList.forEach {
                val contextId = it.id
                val contextName = it.getContextName()
                if (contextName.isNotEmpty()) {
                    lastBtnId = generateContextButton(c, f, layout, lastBtnId, contextId, contextName)
                }
            }
        }

        private fun generateContextButton(c: Context, f: Fragment, layout: ConstraintLayout, lastBtnId: Int, contextId: Int, contextName: String) : Int{
            println("__Generate Context Button: ${contextName}__")
            val dynamicButtonLayout = GenerateDynamicUIButton.createUIButtonLayout(c, layout, contextName, lastBtnId, null)
            UIButtonColorAssign.main(c, "ContextColorAssign", null, contextId.toString(), dynamicButtonLayout)

            //Set on long click listener to trigger edit page function for non-tasks
            val contextBundle = bundleOf("contextId" to contextId, "contextName" to contextName)
            val dynamicButton = dynamicButtonLayout.getChildAt(0) as Button
            dynamicButton.setOnLongClickListener {
                findNavController(f).navigate(R.id.action_ContextList_to_ContextAddEdit, contextBundle)
                true
            }
            //Set onclick listener to send to task list fragment
            dynamicButton.setOnClickListener {
                findNavController(f).navigate(R.id.action_ContextList_to_TaskList, contextBundle)
            }

            //Return button id for next button position
            return dynamicButtonLayout.id
        }


    }
}
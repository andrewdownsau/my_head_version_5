package andrew.organiser.my_head_v5.features.ui_generation


import andrew.organiser.my_head_v5.data_objects.ContextObject
import andrew.organiser.my_head_v5.R
import android.content.Context
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController


class Gen02ContextUIList {

    companion object {

        // --- Generate Context UI Button List --- //
        fun main(c: Context, f:Fragment, layout: ConstraintLayout, contextList: ArrayList<ContextObject>){
            println("=== Gen02 - Generate Context UI Button List ===")
            var lastBtnId = 0

            // Clear all views from layout and add all buttons from contents
            layout.removeAllViews()

            //Use last button ID to set the top constraint of the next button
            contextList.forEach {
                val contextId = it.id
                val contextName = it.name
                if (contextName.isNotEmpty()) {
                    lastBtnId = genContextButton(c, f, layout, lastBtnId, contextId, contextName)
                }
            }
        }

        private fun genContextButton(c: Context, f: Fragment, layout: ConstraintLayout, lastBtnId: Int, contextId: Int, contextName: String) : Int{
            val dynamicButtonLayout = Gen01DynamicUIButton.createUIButtonLayout(c, layout, contextName, lastBtnId, null)
            Gen05UIButtonColorAssign.main(c, "ContextColorAssign", null, contextId, dynamicButtonLayout)

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
package andrew.organiser.myhead_v3.features.ui_generation

import andrew.organiser.myhead_v3.R
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat

class GenerateTaskOrderList {

    companion object {

        //Global Order List for extraction//
        private var globalSortOrderList: ArrayList<Pair<String, Int>> = ArrayList()
        private val globalNameIdList: ArrayList<Int> = ArrayList()

        // --- Generate Task Order List --- //
        fun main(c: Context, layout: ConstraintLayout, taskSortOrderRead: Pair<Boolean, ArrayList<Pair<String, Int>>?>, taskFeatureList: ArrayList<Pair<String, Boolean>>?){
            println("=== Gen13 - Generate Task Order List ===")
            var lastTaskOrderId = 0

            // Clear all views from layout and add all buttons from contents
            layout.removeAllViews()


            //Use last button ID to set the top constraint of the next button
            if (taskSortOrderRead.first && !taskSortOrderRead.second.isNullOrEmpty()) {
                val taskOrderListRaw = taskSortOrderRead.second

                //Reset global order list with defaults
                globalSortOrderList = taskOrderListRaw!!
                globalNameIdList.clear()

                //Create order layout and add sorted orders
                val taskOrderListOrdered = sortTaskOrderList(taskOrderListRaw, taskFeatureList)
                if(!taskOrderListOrdered.isNullOrEmpty()){
                    for((index,taskOrderItem)  in taskOrderListOrdered.withIndex()){
                        lastTaskOrderId = createTaskOrderItem(c, layout, taskOrderItem.first, lastTaskOrderId, index==(taskOrderListOrdered.size-1), index+3)
                    }
                }
            }
        }

        fun getSortOrderList(): ArrayList<Pair<String, Int>>{
            return globalSortOrderList
        }

        fun setSortOrderValue(c: Context, button: Button, textView: TextView){
            val valueIndex = if(textView.text.contains("Incomplete")) 1 else 2
            var newValueStr = ""

            //Change the button background and textview text as appropriate
            if(textView.text.contains("High")){
                newValueStr = "${textView.text.split(":")[0]}: Low"
                button.background = ContextCompat.getDrawable(c, R.mipmap.ic_menu_low)
            }
            else if(textView.text.contains("Low")){
                newValueStr = "${textView.text.split(":")[0]}: High"
                button.background = ContextCompat.getDrawable(c, R.mipmap.ic_menu_high)
            }

            //Set textview and change global list for update
            textView.text = newValueStr
            globalSortOrderList[valueIndex-1] = Pair(newValueStr, valueIndex)
        }

        private fun sortTaskOrderList(unorderedList: ArrayList<Pair<String, Int>>?, taskFeatureList: ArrayList<Pair<String, Boolean>>?) : ArrayList<Pair<String, Int>>?{
            println("__Sort Task Order List__")
            if(!unorderedList.isNullOrEmpty() && taskFeatureList != null){
                //Filtered list contains only the default task properties and any features that have been set to true
                val filteredList = unorderedList.filter { it.first.contains("Due Date")} as ArrayList<Pair<String, Int>>

                for(feature in taskFeatureList){
                    if(feature.second && (feature.first == "Motivation" || feature.first == "Complexity")){
                        val unorderedItem = unorderedList.filter { it.first.contains(feature.first)}
                        if(unorderedItem.isNotEmpty()){
                            filteredList.add(unorderedItem[0])
                        }
                    }
                }


                //Sort the order list so that items are prioritized by pair number
                val orderedList: ArrayList<Pair<String, Int>> = ArrayList()
                var loopIndex = 1
                while(orderedList.size < filteredList.size){
                    for(item in filteredList){
                        if(item.second == loopIndex){
                            println("__Adding filtered order: ${item.first} as ${item.second}__")
                            orderedList.add(item)
                        }
                    }
                    loopIndex++
                }
                return orderedList
            }
            return unorderedList
        }

        private fun createTaskOrderItem(c: Context, layout: ConstraintLayout, itemName: String, lastTaskOrderId: Int, lastItem: Boolean, orderIndex: Int) : Int {
            println("__Create Task Order Item__")

            //Set parameters for item name
            val taskOrderName = createTaskOrderName(c, itemName)
            layout.addView(taskOrderName)

            //Set parameters for item type button
            val orderTypeButton = createTaskOrderTypeButton(c, itemName, taskOrderName, orderIndex)
            layout.addView(orderTypeButton)

            //Set parameters for item move up
            var upButtonRes = R.mipmap.ic_menu_up
            if(lastTaskOrderId == 0) upButtonRes = R.mipmap.ic_menu_up_disabled
            val moveUpButton = createOrderDirectionButton(c, layout, "Up", upButtonRes, taskOrderName.id)
            layout.addView(moveUpButton)

            //Set parameters for item move down
            var downButtonRes = R.mipmap.ic_menu_down
            if(lastItem) downButtonRes = R.mipmap.ic_menu_down_disabled
            val moveDownButton = createOrderDirectionButton(c, layout, "Down", downButtonRes, taskOrderName.id)
            layout.addView(moveDownButton)

            setTaskOrderConstraints(layout, orderTypeButton, taskOrderName, moveUpButton, moveDownButton, lastTaskOrderId)

            return taskOrderName.id
        }

        private fun createTaskOrderName(c: Context, orderName: String) : TextView{
            println("__Create Task Order Name__")
            //Set parameters for item name
            val taskItemName = TextView(c)
            taskItemName.id = View.generateViewId()
            globalNameIdList.add(taskItemName.id)
            val params = ConstraintLayout.LayoutParams(0, 80)
            taskItemName.setLayoutParams(params)
            taskItemName.setTextColor(c.getColor(R.color.sub_content))
            taskItemName.textSize = 16F
            taskItemName.text = orderName

            return taskItemName
        }

        private fun createTaskOrderButton(c: Context, backgroundResource: Int, taskItemNameId: Int) : Button {
            println("__Create Task Order Button__")
            //Set parameters for direction button
            val taskItemButton = Button(c)
            taskItemButton.id = View.generateViewId()
            val params = ConstraintLayout.LayoutParams(80, 80)
            taskItemButton.setLayoutParams(params)
            taskItemButton.background = ContextCompat.getDrawable(c, backgroundResource)
            taskItemButton.tag = taskItemNameId

            return taskItemButton
        }

        private fun createTaskOrderTypeButton(c: Context, itemName: String, taskItemName: TextView, orderIndex: Int) : Button {
            println("__Create Task Order Type Button__")
            //Set background resource based on Item name order type
            var backgroundResource = R.mipmap.ic_menu_up
            if(itemName.contains("Descending")) backgroundResource = R.mipmap.ic_menu_down

            val taskOrderButton =  createTaskOrderButton(c, backgroundResource, taskItemName.id)
            taskOrderButton.setOnClickListener {
                val newOrderOption = if(taskItemName.text.contains("Ascending")) "Descending" else "Ascending"
                val newOrderButton = if(taskItemName.text.contains("Ascending")) R.mipmap.ic_menu_down else R.mipmap.ic_menu_up
                val newItemName = "${taskItemName.text.split(":")[0]}: $newOrderOption"
                taskItemName.text = newItemName
                taskOrderButton.background = ContextCompat.getDrawable(c, newOrderButton)
                globalSortOrderList[orderIndex-1] = Pair(newItemName, orderIndex)
            }

            return taskOrderButton
        }

        private fun createOrderDirectionButton(c: Context, layout: ConstraintLayout, buttonType: String,  backgroundResource: Int, taskItemNameId: Int) : Button {
            println("__Create Task Order Direction Button__")
            //Set background resource based on type and create generic button
            val moveButton = createTaskOrderButton(c, backgroundResource, taskItemNameId)

            //Set button to shift all orders depending on button pressed and values above/below
            if(backgroundResource != R.mipmap.ic_menu_up_disabled && backgroundResource != R.mipmap.ic_menu_down_disabled){
                moveButton.setOnClickListener {
                    println("__Direction $buttonType Button Pressed__")
                    //Use the global list and the current task name id to figure out current position value
                    val currentPosition = globalNameIdList.indexOf(taskItemNameId)+1
                    val currentTextView = layout.getViewById(taskItemNameId) as TextView
                    val currentName = currentTextView.text.toString()
                    println("__Current Item Name: ${currentName}__")

                    //Get the textView instances of switched values
                    val shiftedPosition = if(buttonType == "Down") currentPosition+1 else currentPosition-1
                    val shiftedTextViewId = globalNameIdList[shiftedPosition-1]
                    val shiftedTextView = layout.getViewById(shiftedTextViewId) as TextView
                    val shiftedName = shiftedTextView.text.toString()

                    //Swap the text in each view and update the global list
                    currentTextView.text = shiftedName
                    shiftedTextView.text = currentName

                    globalSortOrderList[currentPosition+1] = Pair(shiftedName, currentPosition+2)
                    globalSortOrderList[shiftedPosition+1] = Pair(currentName, shiftedPosition+2)

                }
            }

            return moveButton
        }



        private fun setTaskOrderConstraints(layout: ConstraintLayout, orderTypeButton: Button, taskOrderName: TextView, moveUpButton: Button, moveDownButton: Button, lastTaskOrderId:Int){
            println("__Create Task Order Constraints__")

            //Clone layout constraints and apply widget spread for textview and buttons
            val constraintSet = ConstraintSet()
            constraintSet.clone(layout)
            constraintSet.createHorizontalChain(ConstraintSet.PARENT_ID, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, intArrayOf(orderTypeButton.id, taskOrderName.id, moveUpButton.id, moveDownButton.id), floatArrayOf(0F,1F,0F,0F), ConstraintSet.CHAIN_SPREAD)

            //Constrain the type button to either the parent layout, or the last order item id (depending on whether topmost)
            if(lastTaskOrderId == 0){
                constraintSet.connect(orderTypeButton.id, ConstraintSet.TOP, layout.id, ConstraintSet.TOP, 0)
            }
            else{
                constraintSet.connect(orderTypeButton.id, ConstraintSet.TOP, lastTaskOrderId, ConstraintSet.BOTTOM, 30)
            }
            constraintSet.connect(orderTypeButton.id, ConstraintSet.START, layout.id, ConstraintSet.START, 0)
            constraintSet.connect(orderTypeButton.id, ConstraintSet.END, taskOrderName.id, ConstraintSet.START, 0)

            //Constrain the textview top to order button, left of order button and right of down button
            constraintSet.connect(taskOrderName.id, ConstraintSet.TOP, orderTypeButton.id, ConstraintSet.TOP, 0)
            constraintSet.connect(taskOrderName.id, ConstraintSet.START, orderTypeButton.id, ConstraintSet.END, 0)
            constraintSet.connect(taskOrderName.id, ConstraintSet.END, moveDownButton.id, ConstraintSet.START, 0)

            //Constrain the down button to be aligned to top of order button, right of text view and left of up button
            constraintSet.connect(moveDownButton.id, ConstraintSet.TOP, orderTypeButton.id, ConstraintSet.TOP, 0)
            constraintSet.connect(moveDownButton.id, ConstraintSet.START, taskOrderName.id, ConstraintSet.END, 0)
            constraintSet.connect(moveDownButton.id, ConstraintSet.END, moveUpButton.id, ConstraintSet.START, 0)

            //Constrain the up button to be aligned to top of order button, right of down button and left of parent
            constraintSet.connect(moveUpButton.id, ConstraintSet.TOP, orderTypeButton.id, ConstraintSet.TOP, 0)
            constraintSet.connect(moveUpButton.id, ConstraintSet.START, moveDownButton.id, ConstraintSet.END, 0)
            constraintSet.connect(moveUpButton.id, ConstraintSet.END, layout.id, ConstraintSet.END, 0)


            constraintSet.applyTo(layout)
        }
    }
}
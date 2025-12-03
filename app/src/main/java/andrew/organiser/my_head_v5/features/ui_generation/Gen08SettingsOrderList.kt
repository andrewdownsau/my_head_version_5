package andrew.organiser.my_head_v5.features.ui_generation

import andrew.organiser.my_head_v5.R
import andrew.organiser.my_head_v5.features.data_manipulation.D02SettingsList
import andrew.organiser.my_head_v5.data_objects.SortOrderObject
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat

class Gen08SettingsOrderList {

    companion object {

        //Global Order List for extraction//
        private var globalSortOrderList: ArrayList<SortOrderObject> = ArrayList()
        private val globalNameIdList: ArrayList<Int> = ArrayList()

        // --- Generate Task Order List --- //
        fun main(c: Context, layout: ConstraintLayout){
            println("=== Gen08 - Generate Settings Order List ===")
            var lastOrderId = 0

            // Clear all views from layout and add all buttons from contents
            layout.removeAllViews()

            //Reset global order list with defaults
            globalSortOrderList = D02SettingsList.getFullOrderList()
            globalNameIdList.clear()

            //Create order layout and add sorted orders
            val orderListSorted = getOrderListSorted()
            if(orderListSorted.isNotEmpty()){
                val lastIndex = orderListSorted.last().index
                for(sortOrderItem in orderListSorted){
                    lastOrderId = createSortOrderItem(c, layout, "${sortOrderItem.name}: ${sortOrderItem.type}", lastOrderId, lastIndex, sortOrderItem.index)
                }
            }
        }

        fun setLowHighChangeListener(c: Context, button: Button, textView: TextView){
            button.setOnClickListener{
                val textViewName = if(textView.text.contains("Incomplete")) "Incomplete" else "Pending"
                var newTextViewType = "High"

                //Change the button background and textview text as appropriate
                if(textView.text.contains("High")){
                    newTextViewType = "Low"
                    button.background = ContextCompat.getDrawable(c, R.mipmap.ic_menu_low)
                }
                else {
                    button.background = ContextCompat.getDrawable(c, R.mipmap.ic_menu_high)
                }

                //Set textview and change global list for update
                val updatedFullStr = "$textViewName: $newTextViewType"
                textView.text = updatedFullStr
                updateGlobalList(textViewName, newTextViewType, null )
            }
        }

        fun getCurrentList(): ArrayList<SortOrderObject>{ return globalSortOrderList }

        private fun getOrderListSorted() : ArrayList<SortOrderObject>{
            println("Process: Get Order List Sorted") //Process line
            //Filtered list contains only the default task properties and any features that have been set to true
            val filteredList = globalSortOrderList.filter { it.name.contains("End Date")} as ArrayList<SortOrderObject>

            for(feature in D02SettingsList.getFullFeatureList()){
                if(feature.value && (feature.key == "Motivation" || feature.key == "Complexity")){
                    val sortOrder = globalSortOrderList.filter { it.name.contains(feature.key)}
                    if(sortOrder.isNotEmpty()){ filteredList.add(sortOrder[0]) }
                }
            }

            //Sort the order list so that items are prioritized by index
            val sortedOrderList: ArrayList<SortOrderObject> = ArrayList()
            var loopIndex = 3
            while(sortedOrderList.size < filteredList.size){
                for(item in filteredList){
                    if(item.index == loopIndex){
                        //println("Debug: Adding filtered order[${item.index}]: ${item.name} - ${item.type}") //Debug line
                        sortedOrderList.add(item)
                    }
                }
                loopIndex++
            }
            return sortedOrderList
        }

        private fun createSortOrderItem(c: Context, layout: ConstraintLayout, itemText: String, lastOrderId: Int, lastIndex: Int, orderIndex: Int) : Int {
            println("Process: Create Sort Order Item: $itemText") //Process line

            //Set parameters for item name
            val sortOrderName = createSortOrderName(c, itemText)
            layout.addView(sortOrderName)

            //Set parameters for item type button
            val orderTypeButton = createSortOrderTypeButton(c, itemText, sortOrderName)
            layout.addView(orderTypeButton)

            //Set parameters for item move up
            var upButtonRes = R.mipmap.ic_menu_up
            if(lastOrderId == 0) upButtonRes = R.mipmap.icon_up_disabled
            val moveUpButton = createOrderDirectionButton(c, layout, "Up", upButtonRes, sortOrderName.id)
            layout.addView(moveUpButton)

            //Set parameters for item move down
            var downButtonRes = R.mipmap.ic_menu_down
            if(orderIndex == lastIndex) downButtonRes = R.mipmap.icon_down_disabled
            val moveDownButton = createOrderDirectionButton(c, layout, "Down", downButtonRes, sortOrderName.id)
            layout.addView(moveDownButton)

            setTaskOrderConstraints(layout, orderTypeButton, sortOrderName, moveUpButton, moveDownButton, lastOrderId)

            return sortOrderName.id
        }

        private fun createSortOrderName(c: Context, orderName: String) : TextView{
            //println("SubProcess: Create Sort Order Name") //SubProcess line
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

        private fun createSortOrderButton(c: Context, backgroundResource: Int, taskItemNameId: Int) : Button {
            //println("--SubProcess: Create Generic Sort Order Button") //SubProcess line
            //Set parameters for direction button
            val taskItemButton = Button(c)
            taskItemButton.id = View.generateViewId()
            val params = ConstraintLayout.LayoutParams(80, 80)
            taskItemButton.setLayoutParams(params)
            taskItemButton.background = ContextCompat.getDrawable(c, backgroundResource)
            taskItemButton.tag = taskItemNameId

            return taskItemButton
        }

        private fun createSortOrderTypeButton(c: Context, itemName: String, taskItemName: TextView) : Button {
            //println("SubProcess: Create Sort Order Type Button") //SubProcess line
            //Set background resource based on Item name order type
            var backgroundResource = R.mipmap.ic_menu_up
            if(itemName.contains("Descending")) backgroundResource = R.mipmap.ic_menu_down

            val taskOrderButton =  createSortOrderButton(c, backgroundResource, taskItemName.id)
            taskOrderButton.setOnClickListener {
                val newOrderOption = if(taskItemName.text.contains("Ascending")) "Descending" else "Ascending"
                val newOrderButton = if(taskItemName.text.contains("Ascending")) R.mipmap.ic_menu_down else R.mipmap.ic_menu_up
                val newItemName = "${taskItemName.text.split(":")[0]}: $newOrderOption"
                taskItemName.text = newItemName
                taskOrderButton.background = ContextCompat.getDrawable(c, newOrderButton)
                updateGlobalList(itemName.split(": ")[0], newItemName.split(": ")[1], null )
            }

            return taskOrderButton
        }

        private fun createOrderDirectionButton(c: Context, layout: ConstraintLayout, buttonType: String,  backgroundResource: Int, taskItemNameId: Int) : Button {
            //println("SubProcess: Create Order Direction Button: $buttonType") //SubProcess line
            //Set background resource based on type and create generic button
            val moveButton = createSortOrderButton(c, backgroundResource, taskItemNameId)

            //Set button to shift all orders depending on button pressed and values above/below
            if(backgroundResource != R.mipmap.icon_up_disabled && backgroundResource != R.mipmap.icon_down_disabled){
                moveButton.setOnClickListener {
                    println("__Direction $buttonType Button Pressed__")
                    //Use the global list and the current task name id to figure out current position value
                    val currentPosition = globalNameIdList.indexOf(taskItemNameId)+1
                    val currentTextView = layout.getViewById(taskItemNameId) as TextView
                    val currentText = currentTextView.text.toString()
                    val currentName = currentText.split(": ")[0]
                    val currentIndex = globalSortOrderList.filter { it.name == currentName }[0].index
                    println("__Current Item Name: ${currentName}__")

                    //Get the textView instances of switched values
                    val switchedPosition = if(buttonType == "Down") currentPosition+1 else currentPosition-1
                    val switchedTextViewId = globalNameIdList[switchedPosition-1]
                    val switchedTextView = layout.getViewById(switchedTextViewId) as TextView
                    val switchedText = switchedTextView.text.toString()
                    val switchedName = switchedText.split(": ")[0]
                    val switchedIndex = globalSortOrderList.filter { it.name == switchedName }[0].index

                    //Swap the text in each view and update the global list
                    currentTextView.text = switchedText
                    switchedTextView.text = currentText

                    updateGlobalList(currentName, null, switchedIndex )
                    updateGlobalList(switchedName, null, currentIndex )
                }
            }

            return moveButton
        }



        private fun setTaskOrderConstraints(layout: ConstraintLayout, orderTypeButton: Button, taskOrderName: TextView, moveUpButton: Button, moveDownButton: Button, lastTaskOrderId:Int){
            //println("SubProcess: Create Order Layout Constraints") //SubProcess line

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

        private fun updateGlobalList(sortOrderName: String, type: String?, index: Int? ){
            println("Process: updating global list item: $sortOrderName to type: $type index: $index")
            //Change the updated item based on values provided
            val updatedOrderItem = globalSortOrderList.filter { it.name == sortOrderName }[0]
            if(type != null)
                updatedOrderItem.type = type
            else if(index != null)
                updatedOrderItem.index = index

            //Remove old updated item from list and re-add
            globalSortOrderList = globalSortOrderList.filter { it.name != sortOrderName } as ArrayList<SortOrderObject>
            globalSortOrderList.add(updatedOrderItem)
        }
    }
}
package andrew.organiser.my_head_v5.features.data_manipulation


import andrew.organiser.my_head_v5.DBHandler
import andrew.organiser.my_head_v5.DBHandler.Companion.CONTEXT_EXCLUDE_COL
import andrew.organiser.my_head_v5.DBHandler.Companion.CONTEXT_NAME_COL
import andrew.organiser.my_head_v5.DBHandler.Companion.CONTEXT_TABLE_NAME
import andrew.organiser.my_head_v5.data_objects.ContextObject
import andrew.organiser.my_head_v5.data_objects.TaskObject
import android.content.Context
import android.widget.Toast
import androidx.core.content.contentValuesOf

class D03ContextList {

    companion object {

        // --- Global variables used throughout the active lifecycle of the application --- //
        private var contextList: ArrayList<ContextObject> = ArrayList()
        private var excludedContextIdList: ArrayList<Int> = ArrayList()
        private var contextListChanged = false

        // --- Settings List CRUD Functionality --- //
        fun initialise(c: Context){
            if(contextList.isEmpty() || contextListChanged){
                println("=== D03 - Initial read of all Contexts ===")
                contextList.clear()
                excludedContextIdList.clear()
                val rawContextList = DBHandler(c).readDBTable(CONTEXT_TABLE_NAME)
                if(rawContextList.isNotEmpty()){
                    for(contextLine in rawContextList){
                        try{
                            println("Debug: Adding Context String Line: $contextLine") //Debug line
                            val contextParams = contextLine.split("\t")
                            val excluded = contextParams[2].toInt() == 1
                            contextList.add(ContextObject(contextParams[0].toInt(), contextParams[1], excluded))
                            if(excluded) excludedContextIdList.add(contextParams[0].toInt())

                        }catch(e: Exception){
                            println("~~~ Error: $e ~~~")
                        }
                    }
                }
                //Reset flag to not initialise until changed again
                contextListChanged = false
            }
        }

        fun read(): ArrayList<ContextObject>{ return contextList }

        fun save(c: Context, newName:String, originalName:String?, excluded: Boolean) : Boolean{
            println("__Saving Context: ${newName}__")
            val values = contentValuesOf(Pair(CONTEXT_NAME_COL, newName), Pair(CONTEXT_EXCLUDE_COL, excluded))

            //Update if original name is given, otherwise create new item
            if(originalName != null){
                if(!DBHandler(c).updateEntry(CONTEXT_TABLE_NAME, "$CONTEXT_NAME_COL='${originalName}'", values)){
                    Toast.makeText(c, "Edit context failed:\nContext name must be unique", Toast.LENGTH_SHORT).show()
                } else { contextListChanged = true }
            }
            else{
                if(!DBHandler(c).newEntry(CONTEXT_TABLE_NAME, values)){
                    Toast.makeText(c, "Create new context failed:\nContext name must be unique", Toast.LENGTH_SHORT).show()
                } else { contextListChanged = true }
            }

            return contextListChanged
        }

        fun delete(c: Context, originalName: String){
            println("__Deleting Context: ${originalName}__")
            contextListChanged = DBHandler(c).deleteEntry(CONTEXT_TABLE_NAME, "$CONTEXT_NAME_COL=?", arrayOf(originalName))
            D04TaskList.deleteWithContext(contextListChanged)
        }

        fun idFromName(name:String): Int { return contextList.first{it.name == name}.id }
        fun nameFromId(id:Int): String { return contextList.first{it.id == id}.name }
        fun excludedFromName(name:String): Boolean { return contextList.first{it.name == name}.excluded }
        fun taskIsNotExcluded(taskObject: TaskObject): Boolean {
            println("Debug: taskIsNotExcluded for : ${taskObject.name}")
            for(excludedContextId in excludedContextIdList){
                println("Debug: excludedContextId = $excludedContextId and taskObject.contextId = ${taskObject.contextId}")
                if(taskObject.contextId == excludedContextId) return false
            }
            return true
        }
    }
}
package andrew.organiser.myhead_v3.features.crud


import andrew.organiser.myhead_v3.modals.ContextModal
import andrew.organiser.myhead_v3.DBHandler
import andrew.organiser.myhead_v3.DBHandler.Companion.CONTEXT_ID_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.CONTEXT_NAME_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.CONTEXT_TABLE_NAME
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_CONTEXT_ID_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_TABLE_NAME
import android.content.ContentValues
import android.content.Context
import android.widget.Toast


class ContextListCURD {

    companion object {

        // --- Context List CRUD Functionality --- //
        fun main(c: Context, cmd: String, originalContextName: String?, newContextName: String?) :
                Pair<Boolean, ArrayList<ContextModal>?>{
            println("=== C01 - Context List CRUD: $cmd ===")
            when(cmd){
                "Create" -> return Pair(createContext(c, newContextName), null)
                "Read" -> return readContextList(c)
                "Update" -> return Pair(updateContext(c, originalContextName, newContextName), null)
                "Delete" -> return Pair(deleteContext(c, originalContextName), null)
            }

            return Pair(false, null)
        }

        private fun createContext(c: Context, newContextName: String?) : Boolean{
            println("__Create Context: ${newContextName}__")
            if(!newContextName.isNullOrEmpty()){
                val values = ContentValues()
                values.put(CONTEXT_NAME_COL, newContextName)

                if(!DBHandler(c).createNewDBEntry(CONTEXT_TABLE_NAME, values)){
                    Toast.makeText(c, "Create new context failed:\nContext name must be unique", Toast.LENGTH_LONG).show()
                } else { return true }

            }
            return false
        }

        private fun readContextList(c: Context): Pair<Boolean, ArrayList<ContextModal>>{
            println("__Read Context List__")
            val readTablePair = DBHandler(c).readDBTable(CONTEXT_TABLE_NAME)
            var returnFlag = readTablePair.first
            val contextListStr = readTablePair.second

            val contextModalArrayList: ArrayList<ContextModal> = ArrayList()
            for(contextStr in contextListStr){
                println("__Context String Line: ${contextStr}__")
                try{
                    contextModalArrayList.add(
                        ContextModal(
                            contextStr.substring(0, contextStr.indexOf("\t")).toInt(),
                            contextStr.substring(contextStr.indexOf("\t")+1))
                    )
                }catch(e: Exception){
                    println("~~~ Error: $e ~~~")
                    returnFlag = false
                }
            }
            return Pair(returnFlag, contextModalArrayList)
        }

        private fun updateContext(c: Context, originalContextName: String?, newContextName: String?) : Boolean{
            if(originalContextName == null || newContextName == null){ println("~~~ Error: Update Context Modal Null ~~~")}
            else{
                println("__Update Context: $originalContextName to ${newContextName}__")
                val newValues = ContentValues()
                newValues.put(CONTEXT_NAME_COL, newContextName)
                if(!DBHandler(c).updateDBEntry(CONTEXT_TABLE_NAME, "$CONTEXT_NAME_COL='${originalContextName}'", newValues)){
                    Toast.makeText(c, "Edit context failed:\nContext name must be unique", Toast.LENGTH_SHORT).show()
                } else { return true }

                return false
            }
            return false
        }

        private fun deleteContext(c: Context, contextName: String?) : Boolean {
            if(contextName == null){ println("~~~ Error: Delete Context Modal Null ~~~")}
            else{
                val contextId = readContextList(c).second.filter { it.getContextName() == contextName }[0].id
                println("__Delete Context: $contextId ${contextName}__")
                return DBHandler(c).deleteDBEntry(CONTEXT_TABLE_NAME, "$CONTEXT_ID_COL=$contextId")
            }
            return false
        }
    }
}
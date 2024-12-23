package andrew.organiser.myhead_v3.features.crud


import andrew.organiser.myhead_v3.DBHandler
import andrew.organiser.myhead_v3.DBHandler.Companion.CONTEXT_NAME_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.CONTEXT_TABLE_NAME
import andrew.organiser.myhead_v3.DBHandler.Companion.SETTINGS_ARCHIVE_DELETE
import andrew.organiser.myhead_v3.DBHandler.Companion.SETTINGS_TABLE_NAME
import andrew.organiser.myhead_v3.DBHandler.Companion.SETTINGS_TASK_FEATURES
import andrew.organiser.myhead_v3.DBHandler.Companion.SETTINGS_TASK_SORT_ORDER
import andrew.organiser.myhead_v3.DBHandler.Companion.SETTINGS_UI_COLORS
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_CHECKLIST_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_CHECKLIST_DATE_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_COMPLETED_DATE_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_COMPLETED_FLAG_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_COMPLEXITY_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_CONDITION_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_CONTEXT_ID_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_DUE_DATE_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_FREQUENCY_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_ID_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_MOTIVATION_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_MOTIVE_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_NAME_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_NOTES_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_REPEAT_CLAUSE_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_REPEAT_CLAUSE_VALUE_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_REPEAT_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_START_DATE_COL
import andrew.organiser.myhead_v3.DBHandler.Companion.TASK_TABLE_NAME
import andrew.organiser.myhead_v3.MainActivity
import andrew.organiser.myhead_v3.modals.TaskModal
import android.content.ContentValues
import android.content.Context
import android.widget.Toast
import androidx.core.content.contentValuesOf
import java.time.LocalDate


class SettingsListCURD {

    companion object {

        //Global settingTypeKeyArray variable
        private val settingTypeKeys = arrayOf("Task_Features", "Task_Sort_Order", "UI_Colors", "Archive_Delete")

        // --- Settings List CRUD Functionality --- //
        fun main(c: Context, cmd: String, settingType:String?, newSettingString: String?) : Pair<Boolean, ArrayList<String>?>{
            println("=== C04 - Settings List CRUD: $cmd ===")
            when(cmd){
                "Read" -> return readSettingsTable(c, settingType)
                "Update" -> return Pair(updateSettings(c, settingType, newSettingString), null)
                "ResetDefault" -> return (Pair(resetDefaultSettings(c), null))
            }

            return Pair(false, null)
        }

        private fun readSettingsTable(c: Context, settingType: String?): Pair<Boolean, ArrayList<String>?>{
            println("__Read Settings: ${settingType}__")
            val readTablePair = DBHandler(c).readDBTable(SETTINGS_TABLE_NAME)
            var returnSettingList: ArrayList<String>? = null
            if(readTablePair.first){
                val allSettings = readTablePair.second[0].split("\t") as ArrayList<String>

                //If settings type is specified, return setting of type
                returnSettingList = if(settingType != null && settingType != "Archive_Delete")
                    (allSettings[settingTypeKeys.indexOf(settingType) + 1].split(",")) as ArrayList<String>
                else if(settingType == "Archive_Delete") arrayListOf(allSettings[settingTypeKeys.indexOf(settingType) + 1])
                else allSettings
            }
            return Pair(readTablePair.first, returnSettingList)
        }

        private fun updateSettings(c: Context, settingType: String?, newSettingString: String?) : Boolean{
            if(settingType == null || newSettingString == null){ println("~~~ Error: Update Task Modal Null ~~~")}
            else{
                println("__Update Settings: $settingType to ${newSettingString}__")
                val settingTypeCol = when(settingType){
                    "Task_Features" -> SETTINGS_TASK_FEATURES
                    "Task_Sort_Order" -> SETTINGS_TASK_SORT_ORDER
                    "UI_Colors" -> SETTINGS_UI_COLORS
                    "Archive_Delete" -> SETTINGS_ARCHIVE_DELETE
                    else -> ""
                }
                val newValues = ContentValues()
                newValues.put(settingTypeCol, newSettingString)
                val returnFlag = DBHandler(c).updateDBEntry(SETTINGS_TABLE_NAME, null, newValues)
                if(returnFlag && settingType == "Archive_Delete") Toast.makeText(c, "Task archive auto delete saved", Toast.LENGTH_SHORT).show()
                return returnFlag
            }
            return false
        }

        private fun resetDefaultSettings(c: Context): Boolean{
            //Define default values
            val defaultValues = contentValuesOf(
                Pair(SETTINGS_TASK_FEATURES, "Motivation:false,Complexity:false,Checklist:false,Repeating:false,Conditions:false,TimeProgress:false,ChecklistProgress:false"),
                Pair(SETTINGS_TASK_SORT_ORDER, "Incomplete: High_1,Pending: Low_2,Due Date: Ascending_3,Complexity: Ascending_4,Motivation: Ascending_5"),
                Pair(SETTINGS_UI_COLORS, "overdue_#B30000,today_#FD4A4A,tomorrow_#FC722D,threeDays_#F7AB43,week_#F9E07B,weekPlus_#C0FCA7,conditional_#A6C8FF,pending_#7F93F9,completed_#AAAAAA,timePB_#34AAFB,checklistPB_#34FB82"),
                Pair(SETTINGS_ARCHIVE_DELETE, "never")
            )

            //Reset Settings to their default value
            return DBHandler(c).updateDBEntry(SETTINGS_TABLE_NAME, null, defaultValues)
        }
    }
}
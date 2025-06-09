package andrew.organiser.my_head_v5.features.data_manipulation

import andrew.organiser.my_head_v5.MainActivity
import android.content.Context
import java.time.LocalDate


class D01ActiveData {

    companion object {

        // --- Global variables used throughout the active lifecycle of the application --- //
        private var currentStoredDate: LocalDate = LocalDate.parse("01/01/01", MainActivity.DATE_FORMAT)

        // --- These functions check to see if all active data is available and up-to-date --- //
        fun activeDataCheck(c: Context){
            println("=== D01 - Active Data Check ===")

            //Initialise all active database table lists
            D02SettingsList.initialise(c)
            D03ContextList.initialise(c)
            D04TaskList.initialise(c)
            D05SubTaskList.initialise(c)

            //Refresh archive data if date has changed from stored value
            val todayDate = LocalDate.now()
            if(todayDate != currentStoredDate || D06RepeatRefresh.flagChange("read", false)){
                currentStoredDate = todayDate
                //Complete repeat refresh check if feature is enabled
                if(D02SettingsList.getTaskFeatureStatus("Repeating")){
                    D06RepeatRefresh.check(c)
                }
                //Complete archive delete if not set to never delete
                if(D02SettingsList.getArchiveDeleteSetting() != "never"){
                    D08ArchiveClear.main(c)
                }
            }
        }
    }
}
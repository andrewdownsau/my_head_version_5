package andrew.organiser.my_head_v5.features.data_manipulation

import andrew.organiser.my_head_v5.MainActivity
import android.content.Context
import java.time.LocalDate

class D08ArchiveClear {

    companion object {

        // --- Archive Clear functionality --- //
        fun main(c:Context){
            println("=== D08 - Archive Clear ===")
            //Get all archived tasks, then delete any older than the after days value
            val archivedList = D04TaskList.read("Master_Archive", null)
            val afterDays = D02SettingsList.getArchiveDeleteSetting().split(" ")[1].toLong()

            if (archivedList.isNotEmpty()) {
                for(oldTask in archivedList){
                    val dayBeforeDate = LocalDate.now().minusDays(afterDays)
                    if(LocalDate.parse(oldTask.completedDate, MainActivity.DATE_FORMAT) < dayBeforeDate){
                        println("Process: Deleting archived task: ${oldTask.name}") //Process line
                        D04TaskList.delete(c, oldTask.name)
                    }
                }
            }
        }
    }
}
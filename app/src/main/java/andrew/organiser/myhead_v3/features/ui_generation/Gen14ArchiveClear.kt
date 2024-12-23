package andrew.organiser.myhead_v3.features.ui_generation


import andrew.organiser.myhead_v3.MainActivity
import andrew.organiser.myhead_v3.features.crud.SettingsListCURD
import andrew.organiser.myhead_v3.features.crud.TaskListCURD
import andrew.organiser.myhead_v3.features.settings.TaskFeatures
import andrew.organiser.myhead_v3.modals.TaskModal
import android.content.Context
import java.time.LocalDate


class ArchiveClear {

    companion object {

        // --- Archive Clear functionality --- //
        fun archiveClearCheck(c:Context){
            println("=== Gen14 - Archive Clear check ===")
            //Check the archive delete settings if it is set to never or not
            val archiveDeleteSetting = SettingsListCURD.main(c, "Read", "Archive_Delete", null).second!![0]
            if(archiveDeleteSetting != "never")
                clearArchivedTasks(c, archiveDeleteSetting.split(" ")[1].toLong())
        }

        private fun clearArchivedTasks(c: Context, afterDays: Long){
            //Get all archived tasks, then delete any older than the after days value
            val archivedList = TaskListCURD.main(c, "Read", null, null, "null_Archive").second

            if (!archivedList.isNullOrEmpty()) {
                for(oldTask in archivedList){
                    val dayBeforeDate = LocalDate.now().minusDays(afterDays)
                    if(LocalDate.parse(oldTask.getTaskCompletedDate(), MainActivity.DATE_FORMAT) < dayBeforeDate){
                        println("__Deleting archived task: ${oldTask.getTaskName()}__")
                        TaskListCURD.main(c, "Delete", oldTask.getTaskName(), null, null)
                    }
                }
            }
        }
    }
}
package andrew.organiser.myhead_v3.features.ui_generation


import andrew.organiser.myhead_v3.R
import andrew.organiser.myhead_v3.features.crud.ContextListCURD
import andrew.organiser.myhead_v3.features.crud.SettingsListCURD
import andrew.organiser.myhead_v3.features.crud.TaskListCURD
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController


class GenerateProtectionDialogue {

    companion object {

        // --- Function called to create check dialogue box when deleting --- //
        fun main(c: Context, f:Fragment, activity: FragmentActivity?, cmd:String, originalName:String?, dialogue: Int) {
            println("=== Gen03 - Generate Protection Dialogue: $cmd ===")
            //Inflate the dialog as custom view
            val messageBoxView = LayoutInflater.from(activity).inflate(dialogue, null)
            val messageBoxBuilder = AlertDialog.Builder(activity).setView(messageBoxView)
            val  messageBoxInstance = messageBoxBuilder.show()

            //Set onclick Listener for dialog box
            messageBoxView.setOnClickListener{
                messageBoxInstance.dismiss()
            }

            //Set onclick listener for close button
            messageBoxInstance.findViewById<Button>(R.id.cancel_button).setOnClickListener{
                messageBoxInstance.dismiss()
            }

            //Set onclick listener for ok button
            messageBoxInstance.findViewById<Button>(R.id.ok_button).setOnClickListener{
                when(cmd){
                    "ContextDelete" -> ContextListCURD.main(c, "Delete", originalName, null)
                    "TaskDelete" -> TaskListCURD.main(c, "Delete", originalName, null, null)
                    "ResetSettings" -> SettingsListCURD.main(c, "ResetDefault", null, null)
                    "ArchiveDelete" -> SettingsListCURD.main(c, "Update", "Archive_Delete", originalName)
                }
                messageBoxInstance.dismiss()
                f.findNavController().popBackStack()
            }

        }
    }
}
package andrew.organiser.my_head_v5.features.ui_generation


import andrew.organiser.my_head_v5.R
import andrew.organiser.my_head_v5.features.data_manipulation.D02SettingsList
import andrew.organiser.my_head_v5.features.data_manipulation.D03ContextList
import andrew.organiser.my_head_v5.features.data_manipulation.D04TaskList
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController


class Gen03ProtectionDialogue {

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
                if(originalName != null){
                    when(cmd){
                        "ContextDelete" -> D03ContextList.delete(c, originalName)
                        "TaskDelete" -> {
                            D04TaskList.delete(c, originalName)
                        }
                        "ArchiveDelete" -> D02SettingsList.saveArchiveDelete(c, originalName)
                    }
                }
                else if(cmd == "ResetSettings"){
                    D02SettingsList.resetDefaultSettings(c)
                }

                messageBoxInstance.dismiss()
                f.findNavController().popBackStack()
            }

        }
    }
}
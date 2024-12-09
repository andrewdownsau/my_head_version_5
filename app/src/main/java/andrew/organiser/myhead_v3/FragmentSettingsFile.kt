package andrew.organiser.myhead_v3

import andrew.organiser.myhead_v3.databinding.SettingsFileBinding
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream


/**
 * Settings Fragment, contains backup option for storing text data into phone
 */
class FragmentSettingsFile : Fragment() {

    private var _binding: SettingsFileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var dbHandler: DBHandler? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = SettingsFileBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        println("--- Context List: onViewCreated ---")
        super.onViewCreated(view, savedInstanceState)
        val fragmentContext = requireContext().applicationContext
        dbHandler = DBHandler(fragmentContext)

        //Enable download button to export data file
        binding.buttonDownloadExport.setOnClickListener{
            if(binding.editExportName.text.isNotEmpty()){
                writeToFile(fragmentContext, getDBExportString())
            }
        }

        //Generate warning dialog for import trigger
        binding.buttonUploadImport.setOnClickListener{
            //Inflate the dialog as custom view
            val messageBoxView = LayoutInflater.from(activity).inflate(R.layout.import_dialog, null)
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

            //Set onclick listener for ok button, start import
            messageBoxInstance.findViewById<Button>(R.id.ok_button).setOnClickListener{
                importDBFromString(fragmentContext)
                messageBoxInstance.dismiss()
                findNavController().popBackStack()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getDBExportString(): String{
        var dbExportStr = ""
        //Read all contexts in table
        val contextModalList = dbHandler?.readContextList(false)
        if(!contextModalList.isNullOrEmpty()){
            dbExportStr = "Context_List:${DBHandler.CONTEXT_TABLE_NAME}\t\n"
            contextModalList.forEach { contextModal ->
                dbExportStr += "${DBHandler.CONTEXT_ID_COL}:${contextModal.id}\t" +
                        "${DBHandler.CONTEXT_NAME_COL}:${contextModal.getContextName()}\t\n"
            }

            //Read all tasks in table
            val taskModalList = dbHandler?.readTaskList("",false, false)
            if(!taskModalList.isNullOrEmpty()){
                dbExportStr += "Task_List:${DBHandler.TASK_TABLE_NAME}\t\n"
                taskModalList.forEach { taskModal ->
                    dbExportStr += "${DBHandler.TASK_ID_COL}:${taskModal.id}\t" +
                            "${DBHandler.TASK_CONTEXT_ID_COL}:${taskModal.getTaskContextId()}\t" +
                            "${DBHandler.TASK_NAME_COL}:${taskModal.getTaskName()}\t" +
                            "${DBHandler.TASK_MOTIVE_COL}:${taskModal.getTaskMotive()}\t" +
                            "${DBHandler.TASK_COMPLEXITY_COL}:${taskModal.getTaskComplexity()}\t" +
                            "${DBHandler.TASK_MOTIVATION_COL}:${taskModal.getTaskMotivation()}\t" +
                            "${DBHandler.TASK_START_DATE_COL}:${taskModal.getTaskStartDate()}\t" +
                            "${DBHandler.TASK_DUE_DATE_COL}:${taskModal.getTaskDueDate()}\t" +
                            "${DBHandler.TASK_CHECKLIST_COL}:${taskModal.getTaskChecklist()}\t" +
                            "${DBHandler.TASK_CHECKLIST_DATE_COL}:${taskModal.getTaskChecklistDate()}\t" +
                            "${DBHandler.TASK_REPEAT_COL}:${taskModal.getTaskRepeat()}\t" +
                            "${DBHandler.TASK_REPEAT_CLAUSE_COL}:${taskModal.getTaskRepeatClause()}\t" +
                            "${DBHandler.TASK_REPEAT_CLAUSE_VALUE_COL}:${taskModal.getTaskRepeatClauseValue()}\t" +
                            "${DBHandler.TASK_FREQUENCY_COL}:${taskModal.getTaskFrequency()}\t" +
                            "${DBHandler.TASK_CONDITION_COL}:${taskModal.getTaskConditionId()}\t" +
                            "${DBHandler.TASK_NOTES_COL}:${taskModal.getTaskNotes()}\t" +
                            "${DBHandler.TASK_COMPLETED_FLAG_COL}:${taskModal.getTaskCompletedFlag()}\t" +
                            "${DBHandler.TASK_COMPLETED_DATE_COL}:${taskModal.getTaskCompletedDate()}\t" +
                            "\n"
                }
            }

            //Read all subtasks in table
            val subtaskModalList = dbHandler?.readSubTaskList("")
            if(!subtaskModalList.isNullOrEmpty()){
                dbExportStr += "Subtask_List:${DBHandler.SUBTASK_TABLE_NAME}\t\n"
                subtaskModalList.forEach { subtaskModal ->
                    dbExportStr += "${DBHandler.SUBTASK_ID_COL}:${subtaskModal.id}\t" +
                            "${DBHandler.SUBTASK_TASK_ID_COL}:${subtaskModal.taskId}\t" +
                            "${DBHandler.SUBTASK_NAME_COL}:${subtaskModal.getSubtaskName()}\t" +
                            "${DBHandler.SUBTASK_DUE_DATE_COL}:${subtaskModal.getSubtaskDueDate()}\t" +
                            "${DBHandler.SUBTASK_COMPLETED_COL}:${subtaskModal.getSubtaskCompletedFlag()}\t" +
                            "\n"
                }
            }
        }

        return dbExportStr
    }

    private fun writeToFile(c: Context, data: String) {
        try {
            val resolver = c.contentResolver
            val values = ContentValues()
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, "${binding.editExportName.text}.txt")
            values.put(MediaStore.MediaColumns.MIME_TYPE, "text")
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            val uri = resolver.insert(MediaStore.Files.getContentUri("external"), values)
            println("~~~URI Export: $uri")

            val os: OutputStream? = uri?.let { resolver.openOutputStream(it,"wt") }

            if (os != null) {
                Toast.makeText(c,"Download Started...", Toast.LENGTH_SHORT).show()
                os.write(data.toByteArray())
                os.flush()
                os.close()
                Toast.makeText(c,"Download Complete!", Toast.LENGTH_SHORT).show()
            }

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun importDBFromString(c: Context) {
        try {
            val importFileName = binding.editImportName.text
            println("importDBFromString: $importFileName.txt")
            val stringBuilder = StringBuilder()
            val resolver = c.contentResolver
            val uri = getUriFromPath(c, "$importFileName.txt")
            println("~~~URI Import: $uri")
            if(uri != null){
                val inputStream: InputStream? = resolver.openInputStream(uri)

                if (inputStream != null) {
                    //Reset the database if override file has been correctly found
                    dbHandler?.resetDatabase()
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        var line: String? = reader.readLine()
                        while (line != null) {
                            println("~~~$line")
                            if(line.startsWith(DBHandler.CONTEXT_ID_COL)) {
                                dbHandler?.importContext(convertStringToContext(line))
                            }
                            else if(line.startsWith(DBHandler.TASK_ID_COL)) {
                                dbHandler?.importTask(convertStringToTask(line))
                            }
                            else if(line.startsWith(DBHandler.SUBTASK_ID_COL)) {
                                dbHandler?.importSubTask(convertStringToSubTask(line))
                            }
                            else if(line.startsWith(DBHandler.SETTINGS_ID_COL)) {
                                dbHandler?.importSettings(convertStringToSettings(line))
                            }
                            stringBuilder.append(line)
                            line = reader.readLine()
                        }
                    }
                    Toast.makeText(c,"Upload Complete!", Toast.LENGTH_SHORT).show()
                }
            }
            else
                Toast.makeText(c,"URI failed to convert from path", Toast.LENGTH_SHORT).show()

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            Toast.makeText(c,"Import File Not Found", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(c,"IO Exception", Toast.LENGTH_SHORT).show()
        }
    }

    private fun convertStringToContext(lineStr : String) : ContextModal{
        val contextId = extractIntValue(lineStr, DBHandler.CONTEXT_ID_COL)
        val contextName = extractStringValue(lineStr, DBHandler.CONTEXT_NAME_COL)
        return ContextModal(contextId, contextName)
    }

    private fun convertStringToTask(lineStr : String) : TaskModal{
        val taskId = extractIntValue(lineStr, DBHandler.TASK_ID_COL)
        val taskContextId = extractIntValue(lineStr, DBHandler.TASK_CONTEXT_ID_COL)
        val taskName = extractStringValue(lineStr, DBHandler.TASK_NAME_COL)
        val taskMotive = extractStringValue(lineStr, DBHandler.TASK_MOTIVE_COL)
        val taskComplexity = extractIntValue(lineStr, DBHandler.TASK_COMPLEXITY_COL)
        val taskMotivation = extractIntValue(lineStr, DBHandler.TASK_MOTIVATION_COL)
        val taskStartDate = extractStringValue(lineStr, DBHandler.TASK_START_DATE_COL)
        val taskDueDate = extractStringValue(lineStr, DBHandler.TASK_DUE_DATE_COL)
        val taskChecklist = extractStringValue(lineStr, DBHandler.TASK_CHECKLIST_COL)
        val taskChecklistDate = extractStringValue(lineStr, DBHandler.TASK_CHECKLIST_DATE_COL)
        val taskRepeat = extractStringValue(lineStr, DBHandler.TASK_REPEAT_COL)
        val taskRepeatClause = extractStringValue(lineStr, DBHandler.TASK_REPEAT_CLAUSE_COL)
        val taskRepeatClauseValue = extractStringValue(lineStr, DBHandler.TASK_REPEAT_CLAUSE_VALUE_COL)
        val taskFrequency = extractStringValue(lineStr, DBHandler.TASK_FREQUENCY_COL)
        val taskCondition = extractIntValue(lineStr, DBHandler.TASK_CONDITION_COL)
        val taskNotes = extractStringValue(lineStr, DBHandler.TASK_NOTES_COL)
        val taskCompletedFlag = extractStringValue(lineStr, DBHandler.TASK_COMPLETED_FLAG_COL)
        val taskCompletedDate = extractStringValue(lineStr, DBHandler.TASK_COMPLETED_DATE_COL)
        return TaskModal(taskId, taskContextId, taskName, taskMotive, taskComplexity, taskMotivation, taskStartDate, taskDueDate,taskChecklist=="true", taskChecklistDate, taskRepeat=="true", taskRepeatClause, taskRepeatClauseValue ,taskFrequency, taskCondition, taskNotes, taskCompletedFlag=="true", taskCompletedDate)
    }

    private fun convertStringToSubTask(lineStr : String) : SubtaskModal{
        val subtaskId = extractIntValue(lineStr, DBHandler.SUBTASK_ID_COL)
        val subtaskTaskId = extractIntValue(lineStr, DBHandler.SUBTASK_TASK_ID_COL)
        val subtaskName = extractStringValue(lineStr, DBHandler.SUBTASK_NAME_COL)
        val subtaskDate = extractStringValue(lineStr, DBHandler.SUBTASK_DUE_DATE_COL)
        val subtaskCompleted = extractStringValue(lineStr, DBHandler.SUBTASK_COMPLETED_COL)
        return SubtaskModal(subtaskId, subtaskTaskId, subtaskName, subtaskDate, subtaskCompleted =="true")
    }

    private fun convertStringToSettings(lineStr : String) : SettingsModal{
        val overdue = extractStringValue(lineStr, DBHandler.SETTINGS_OVERDUE)
        val today = extractStringValue(lineStr, DBHandler.SETTINGS_TODAY)
        val tomorrow = extractStringValue(lineStr, DBHandler.SETTINGS_TOMORROW)
        val threeDays = extractStringValue(lineStr, DBHandler.SETTINGS_THREE_DAYS)
        val week = extractStringValue(lineStr, DBHandler.SETTINGS_WEEK)
        val weekPlus = extractStringValue(lineStr, DBHandler.SETTINGS_WEEK_PLUS)
        val conditional = extractStringValue(lineStr, DBHandler.SETTINGS_CONDITIONAL)
        val pending = extractStringValue(lineStr, DBHandler.SETTINGS_PENDING_SOON)
        val completed = extractStringValue(lineStr, DBHandler.SETTINGS_COMPLETED)

        return SettingsModal(overdue, today, tomorrow, threeDays, week, weekPlus, conditional, pending, completed)
    }

    private fun extractStringValue(lineStr: String, key:String) : String{
        try{
            if(lineStr.contains(key)){
                val valueStr = lineStr.drop(lineStr.indexOf("\t$key:") + key.length + 2)
                return valueStr.take(valueStr.indexOf("\t"))
            }
            else{
                return ""
            }
        }
        catch (e:Exception){
            println("___Error: $e")
            return ""
        }
    }

    private fun extractIntValue(lineStr: String, key:String) : Int{
        try{
            val valueStr = lineStr.drop(lineStr.indexOf("\t$key:") + key.length + 2)
            return valueStr.take(valueStr.indexOf("\t")).toInt()
        }
        catch (e:Exception){
            println("___Error: $e")
            return 0
        }
    }

    @SuppressLint("Range")
    fun getUriFromPath(context: Context, filePath: String): Uri? {
        val cursor = context.contentResolver.query(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Downloads._ID),
            MediaStore.Downloads.DISPLAY_NAME + "=? ",
            arrayOf(filePath), null
        )
        if (cursor != null && cursor.moveToFirst()) {
            println("_Cursor has found a file matching path...")
            val id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
            cursor.close()
            return Uri.withAppendedPath(MediaStore.Downloads.EXTERNAL_CONTENT_URI, "" + id)
        }
        return null
    }
}
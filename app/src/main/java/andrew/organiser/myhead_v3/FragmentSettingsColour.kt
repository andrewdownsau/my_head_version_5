package andrew.organiser.myhead_v3

import andrew.organiser.myhead_v3.databinding.SettingsColourBinding
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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
 * Settings Fragment, contains color setting menu
 */
class FragmentSettingsColour : Fragment() {

    private var _binding: SettingsColourBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var dbHandler: DBHandler? = null
    private var colorSettings: SettingsModal? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = SettingsColourBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        println("--- Settings Colours: onViewCreated ---")
        super.onViewCreated(view, savedInstanceState)
        val fragmentContext = requireContext().applicationContext
        dbHandler = DBHandler(fragmentContext)

        //Populating current settings values
        colorSettings = dbHandler?.readSettings()
        if(colorSettings != null)
            populateCurrentColours()

        //Close button redirect
        binding.btnCancelColours.setOnClickListener {
            findNavController().popBackStack()
        }

        //Save button file write and redirect
        binding.btnSaveColours.setOnClickListener {
            //Extract all color settings from edit text values
            if (colorSettings != null) {
                dbHandler!!.updateSettings(fragmentContext, colorSettings!!)
            }
            findNavController().popBackStack()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    //Color settings functions
    private fun populateCurrentColours(){
        println("--- populateCurrentColours() ---")
        initiateEditColorGroup(binding.editColourOverdue, binding.buttonColourOverdue, "overdue")
        initiateEditColorGroup(binding.editColourToday, binding.buttonColourToday, "today")
        initiateEditColorGroup(binding.editColourTomorrow, binding.buttonColourTomorrow, "tomorrow")
        initiateEditColorGroup(binding.editColourThreeDays, binding.buttonColourThreeDays, "threeDays")
        initiateEditColorGroup(binding.editColourWeek, binding.buttonColourWeek, "week")
        initiateEditColorGroup(binding.editColourWeekPlus, binding.buttonColourWeekPlus, "weekPlus")
        initiateEditColorGroup(binding.editColourConditional, binding.buttonColourConditional, "conditional")
        initiateEditColorGroup(binding.editColourPending, binding.buttonColourPending, "pending")
        initiateEditColorGroup(binding.editColourComplete, binding.buttonColourComplete, "completed")
    }

    private fun initiateEditColorGroup(editText: EditText, button: Button, color: String){
        val extractedColor = colorSettings?.getColor(color)
        editText.setText(extractedColor)
        button.setBackgroundColor(Color.parseColor(extractedColor))
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                try{
                    val editTextColor = Color.parseColor(s.toString())
                    button.setBackgroundColor(editTextColor)
                    colorSettings?.setColor(s.toString(), color)
                }catch(_:Exception){}
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }
}
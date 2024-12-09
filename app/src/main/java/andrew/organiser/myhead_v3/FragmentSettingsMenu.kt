package andrew.organiser.myhead_v3

import andrew.organiser.myhead_v3.databinding.SettingsMenuBinding
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
 * Settings Fragment, contains backup option for storing text data into phone
 */
class FragmentSettingsMenu : Fragment() {

    private var _binding: SettingsMenuBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var dbHandler: DBHandler? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = SettingsMenuBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        println("--- Settings Menu: onViewCreated ---")
        super.onViewCreated(view, savedInstanceState)
        val fragmentContext = requireContext().applicationContext
        dbHandler = DBHandler(fragmentContext)

        //Navigate to import/export settings
        binding.btnImportExport.setOnClickListener {
            findNavController().navigate(R.id.action_SettingsMenu_to_SettingsFile)
        }

        //Navigate to colour settings
        binding.btnUiColours.setOnClickListener {
            findNavController().navigate(R.id.action_SettingsMenu_to_SettingsColour)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
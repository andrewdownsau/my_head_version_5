package andrew.organiser.my_head_v5

import andrew.organiser.my_head_v5.databinding.ActivityMainBinding
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : AppCompatActivity(), SettingsButtonStateManager {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navHostFragment = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment?)!!
        navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.action_settings -> navController.navigate(R.id.navigate_to_Settings)
            R.id.action_help_guide -> navController.navigate(R.id.navigate_to_Help_Guide)
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        //Public global variables
        val SIMPLE_DF =  SimpleDateFormat("dd/MM/yy", Locale.ENGLISH)
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yy")
        val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)

    }

    override fun setVisible(currentPage:String) {
        //Hide menu options if already in page
        val settingsAction = binding.toolbar.menu.findItem(R.id.action_settings)
        val helpAction = binding.toolbar.menu.findItem(R.id.action_help_guide)

        settingsAction.setVisible(currentPage != "Settings")
        helpAction.setVisible(currentPage != "Help_Guide")
    }
}

//Set visible status of settings button
interface SettingsButtonStateManager {
    fun setVisible(currentPage:String)
}

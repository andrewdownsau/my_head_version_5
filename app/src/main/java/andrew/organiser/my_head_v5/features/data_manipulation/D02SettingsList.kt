package andrew.organiser.my_head_v5.features.data_manipulation


import andrew.organiser.my_head_v5.DBHandler
import andrew.organiser.my_head_v5.data_objects.SortOrderObject
import android.content.ContentValues
import android.content.Context
import android.widget.Toast
import androidx.core.content.contentValuesOf


class D02SettingsList {

    companion object {

        // --- Global variables used throughout the active lifecycle of the application --- //
        private val settingKeys = arrayOf(DBHandler.TASK_FEATURES, DBHandler.TASK_SORT_ORDER, DBHandler.UI_COLORS, DBHandler.ARCHIVE_DELETE)
        private var settingsList: MutableMap<String, String> = mutableMapOf()
        private var taskFeatureList: MutableMap<String, Boolean> = mutableMapOf()
        private var taskSortList: ArrayList<SortOrderObject> = ArrayList()
        private var uiColorList: MutableMap<String, String> = mutableMapOf()
        private var archiveDelSetting = "never"
        private var settingsChanged = false

        // --- Settings List CRUD Functionality --- //
        fun initialise(c: Context){
            if(settingsList.isEmpty() || settingsChanged){
                println("=== D02 - Initial read of all Settings ===")
                settingsList.clear(); taskFeatureList.clear(); taskSortList.clear(); uiColorList.clear()
                val rawSettingsList = DBHandler(c).readDBTable(DBHandler.SETTINGS_TABLE)
                if(rawSettingsList.isNotEmpty()){
                    val extractedSettings = rawSettingsList[0].split("\t")
                    for(settingKey in settingKeys.withIndex()){
                        for(setting in extractedSettings.drop(1 + settingKey.index)){
                            if(!settingsList.containsKey(settingKey.value)){
                                //println("___Adding new setting to global list: ${settingKey.value} - $setting") //Debug Line
                                settingsList[settingKey.value] = setting

                                //Adding all task feature settings
                                if(settingKey.value == DBHandler.TASK_FEATURES){
                                    for(taskFeature in setting.split(",")){
                                        val featureKey = taskFeature.split(":")[0]
                                        val featureValue = taskFeature.split(":")[1].toBoolean()
                                        //println("Debug: Adding task feature: [${featureKey}] : $featureValue") //Debug line
                                        taskFeatureList[featureKey] = featureValue
                                    }
                                }
                                //Adding all task sort settings
                                if(settingKey.value == DBHandler.TASK_SORT_ORDER){
                                    for(taskSort in setting.split(",")){
                                        val sortIndex = taskSort.split("_")[1].toInt()
                                        val sortName = taskSort.split(":")[0]
                                        val sortType = taskSort.split(": ")[1].split("_")[0]
                                        taskSortList.add(SortOrderObject(sortIndex,sortName,sortType))
                                    }
                                }
                                //Adding all UI color settings
                                if(settingKey.value == DBHandler.UI_COLORS){
                                    for(uiColor in setting.split(",")){
                                        uiColorList[uiColor.split(":")[0]] = uiColor.split(":")[1]
                                    }
                                }
                                //Adding archive delete settings
                                if(settingKey.value == DBHandler.ARCHIVE_DELETE){ archiveDelSetting = setting }
                            }
                        }
                    }
                }
                //Reset flag to not initialise until changed again
                settingsChanged = false
            }
        }

        fun saveFeatures(c: Context, featureList: MutableMap<String, Boolean> ) : Boolean{
            println("Process: Saving Feature Settings") //Process line

            //Convert the feature map into correct string format and place in values
            var newFeaturesStr = ""
            for(feature in featureList){
                newFeaturesStr += "${feature.key}:${feature.value},"
            }
            newFeaturesStr = newFeaturesStr.dropLast(1)  //Remove final comma
            val values = contentValuesOf(Pair(DBHandler.TASK_FEATURES, newFeaturesStr))
            return save(c, "Task Features", values)
        }
        fun getTaskFeatureStatus(featureKey: String): Boolean{ return taskFeatureList.getValue(featureKey) }
        fun getFullFeatureList(): MutableMap<String, Boolean> { return taskFeatureList }

        //Save new order list
        fun saveSortOrder(c: Context, sortOrderList: ArrayList<SortOrderObject> ) : Boolean{
            println("Process: Sort Order Settings") //Process line

            //Convert the sort modals into correct string format and place in values
            var newSortStr = ""
            for(sortOrder in sortOrderList){
                newSortStr += "${sortOrder.getModalAsString()},"
            }
            newSortStr = newSortStr.dropLast(1)  //Remove final comma
            val values = contentValuesOf(Pair(DBHandler.TASK_SORT_ORDER, newSortStr))
            return save(c, "Sort Order", values)
        }
        fun getOrderTypeFromName(name: String): String{ return taskSortList.filter { it.name == name }[0].type }
        fun getFullOrderList(): ArrayList<SortOrderObject>{ return taskSortList }

        fun saveUIColors(c: Context, colorList: MutableMap<String, String> ) : Boolean{
            println("Process: Saving Color Settings") //Process line

            //Convert the color map into correct string format and place in values
            var newColorsStr = ""
            for(color in colorList){
                newColorsStr += "${color.key}:${color.value},"
            }
            newColorsStr = newColorsStr.dropLast(1)  //Remove final comma
            val values = contentValuesOf(Pair(DBHandler.UI_COLORS, newColorsStr))
            return save(c, "UI Colors", values)
        }
        fun getUIColor(colorKey: String): String{ return uiColorList.getValue(colorKey) }
        fun getUIColorList(): MutableMap<String, String>{ return uiColorList }

        //Save new archive delete setting
        fun saveArchiveDelete(c: Context, archiveSetting: String ) : Boolean{
            println("Process: Saving Archive Delete Settings") //Process line
            val values = contentValuesOf(Pair(DBHandler.ARCHIVE_DELETE, archiveSetting))
            return save(c, "Archive Delete", values)
        }
        fun getArchiveDeleteSetting(): String {return archiveDelSetting}

        fun resetDefaultSettings(c: Context){
            println("=== Reset Default Settings ===")
            val values = contentValuesOf(
                Pair(DBHandler.TASK_FEATURES, DBHandler.DEFAULT_SETTING_FEATURES),
                Pair(DBHandler.TASK_SORT_ORDER, DBHandler.DEFAULT_SETTING_SORT),
                Pair(DBHandler.UI_COLORS, DBHandler.DEFAULT_SETTING_COLORS),
                Pair(DBHandler.ARCHIVE_DELETE, DBHandler.DEFAULT_SETTING_ARCHIVE))
            save(c, "All", values)
        }

        private fun save(c: Context, type:String, values: ContentValues): Boolean{
            if(!DBHandler(c).updateEntry(DBHandler.SETTINGS_TABLE, "", values)){
                Toast.makeText(c, "Updating $type settings did not succeed", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(c, "$type settings saved", Toast.LENGTH_SHORT).show()
                settingsChanged = true
            }
            return settingsChanged
        }
    }
}
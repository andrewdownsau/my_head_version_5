package andrew.organiser.myhead_v3

class SettingsModal (
    private var overdue: String,
    private var today: String,
    private var tomorrow: String,
    private var threeDays: String,
    private var week: String,
    private var weekPlus: String,
    private var conditional: String,
    private var pendingSoon: String,
    private var completed: String,
    ){

    // creating getter and setter methods
    fun getColor(type:String): String {
        when(type){
            "overdue" -> return overdue
            "today" -> return today
            "tomorrow" -> return tomorrow
            "threeDays" -> return threeDays
            "week" -> return week
            "weekPlus" -> return weekPlus
            "conditional" -> return conditional
            "pending" -> return pendingSoon
            "completed" -> return completed
        }
        return ""
    }

    fun setColor(color:String, type:String) {
        when(type){
            "overdue" -> this.overdue = color
            "today" -> this.today = color
            "tomorrow" -> this.tomorrow = color
            "threeDays" -> this.threeDays = color
            "week" -> this.week = color
            "weekPlus" -> this.weekPlus = color
            "conditional" -> this.conditional = color
            "pending" -> this.pendingSoon = color
            "completed" -> this.completed = color
        }
    }
}


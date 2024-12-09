package andrew.organiser.myhead_v3

class SubtaskModal (
    var id: Int,
    var taskId: Int,
    private var name: String,
    private var dueDate: String,
    private var completedFlag: Boolean){

    // creating getter and setter methods
    fun getSubtaskName():String { return name }
    fun getSubtaskDueDate():String { return dueDate }
    fun getSubtaskCompletedFlag():Boolean { return completedFlag }

    fun setSubtaskName(subtaskName:String) { this.name = subtaskName }
    fun setSubtaskDueDate(subtaskDate:String) { this.dueDate = subtaskDate }
    fun setSubtaskCompletedFlag(completedFlag: Boolean) { this.completedFlag = completedFlag }
}


package andrew.organiser.myhead_v3.modals

class TaskModal (
    var id: Int,
    var contextId: Int,
    private var name: String,
    private var motive: String,
    private var complexity: Int,
    private var motivation: Int,
    private var startDate: String,
    private var dueDate: String,
    private var checklist: Boolean,
    private var checklistDate: String,
    private var repeat: Boolean,
    private var repeatClause: String,
    private var repeatClauseValue: String,
    private var frequency: String,
    private var conditionId: Int?,
    private var notes: String,
    private var completedFlag: Boolean,
    private var completedDate: String){

    // creating getter and setter methods
    fun getTaskContextId():Int { return contextId }
    fun getTaskName():String { return name }
    fun getTaskMotive():String { return motive }
    fun getTaskComplexity():Int { return complexity }
    fun getTaskMotivation():Int { return motivation }
    fun getTaskStartDate():String { return startDate }
    fun getTaskDueDate():String { return dueDate }
    fun getTaskChecklist():Boolean { return checklist }
    fun getTaskChecklistDate():String { return checklistDate }
    fun getTaskRepeat():Boolean { return repeat }
    fun getTaskRepeatClause():String { return repeatClause }
    fun getTaskRepeatClauseValue():String { return repeatClauseValue }
    fun getTaskFrequency():String { return frequency }
    fun getTaskConditionId():Int? { return if(conditionId != 0) conditionId else null }
    fun getTaskNotes():String { return notes }
    fun getTaskCompletedFlag():Boolean { return completedFlag }
    fun getTaskCompletedDate():String { return completedDate }


    fun setTaskStartDate(taskStartDate:String) { this.startDate = taskStartDate}
    fun setTaskDueDate(taskDueDate:String) { this.dueDate = taskDueDate}
    fun setTaskChecklistDate(taskChecklistDate:String) { this.checklistDate = taskChecklistDate}
    fun setTaskRepeatClauseValue(taskRepeatClauseValue:String) { this.repeatClauseValue = taskRepeatClauseValue}
    fun setTaskConditionId(taskConditionId:Int) { this.conditionId = taskConditionId}
    fun setTaskCompletedFlag(taskCompletedFlag:Boolean) { this.completedFlag = taskCompletedFlag}
    fun setTaskCompletedDate(taskCompletedDate:String) { this.completedDate = taskCompletedDate}
}


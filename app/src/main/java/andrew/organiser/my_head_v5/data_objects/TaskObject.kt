package andrew.organiser.my_head_v5.data_objects

class TaskObject (
    var id: Int,
    var contextId: Int,
    var name: String,
    var motive: String,
    var complexity: Int,
    var motivation: Int,
    var startDate: String,
    var startTime: String,
    var dueDate: String,
    var dueTime: String,
    var checklist: Boolean,
    var checklistDate: String,
    var repeat: Boolean,
    var repeatClause: String,
    var repeatClauseValue: String,
    var frequency: String?,
    var conditionId: Int?,
    var conditionStatus: Boolean?,
    var notes: String,
    var completedFlag: Boolean,
    var completedDate: String){

    fun getTaskModalAsString(): String{
        return "[$id][$contextId] $name: $motive, $complexity, $motivation, $startDate, $startTime, $dueDate, $dueTime, $checklist, $checklistDate, $repeat, " +
                "$repeatClause, $repeatClauseValue, $frequency, $conditionId, $conditionStatus, $notes, $completedFlag, $completedDate"
    }
}


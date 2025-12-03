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
    var endDate: String,
    var endTime: String,
    var checklistFlag: Boolean,
    var earliestEndDate: String,
    var repeatFlag: Boolean,
    var repeatClause: String,
    var frequencyClause: String,
    var conditionIdRef: Int?,
    var changeStartFlag: Boolean?,
    var conditionActiveFlag: Boolean?,
    var notes: String,
    var completedFlag: Boolean,
    var completedDate: String){

    fun getTaskModalAsString(): String{
        return "[$id][$contextId] $name: $motive, $complexity, $motivation, $startDate, $startTime, $endDate, $endTime, $checklistFlag, $earliestEndDate, $repeatFlag, " +
                "$repeatClause, $frequencyClause, $conditionIdRef, $changeStartFlag, $conditionActiveFlag, $notes, $completedFlag, $completedDate"
    }
}


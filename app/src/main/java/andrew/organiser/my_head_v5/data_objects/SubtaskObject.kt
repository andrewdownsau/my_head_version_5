package andrew.organiser.my_head_v5.data_objects

class SubtaskObject (
    var id: Int,
    var taskId: Int,
    var name: String,
    var endDate: String,
    var completedFlag: Boolean){

    fun getSubTaskModalAsString(): String{
        return "[$id][$taskId] $name: $endDate, $completedFlag"
    }

}


package andrew.organiser.my_head_v5.data_objects

class SortOrderObject (var index: Int, var name: String, var type: String){

    fun getModalAsString():String{
        return "$name: ${type}_${index}"
    }
}
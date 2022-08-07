package info.sergeikolinichenko.closednotepad.repositories

interface PreferencesRepository {

    fun setOrderViewNoteList(order: String)

    fun getOrderViewNoteList(): String

    fun setDefColorIndex(index: Int)

    fun getDefColorIndex(): Int
}
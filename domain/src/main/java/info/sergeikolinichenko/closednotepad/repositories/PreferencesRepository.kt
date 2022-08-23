package info.sergeikolinichenko.closednotepad.repositories

interface PreferencesRepository {

    fun setPrefOrderNoteList(order: String)

    fun getPrefOrderNoteList(): String

    fun setPrefColorIndex(index: Int)

    fun getPrefColorIndex(): Int

    fun getPrefAutoDelReNote(): Int

    fun setPrefAutoDelReNote(days: Int)
}
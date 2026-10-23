package info.sergeikolinichenko.closednotepad.security

import info.sergeikolinichenko.closednotepad.models.NoteItem
import org.json.JSONArray
import org.json.JSONObject

// What a locked note hides: its text and its list items. Type, title, color and dates
// stay in their columns, so lists, sorting and search by title keep working.
internal data class SealedContent(val body: String, val items: List<NoteItem>) {

    fun toJson(): String = JSONObject()
        .put(BODY, body)
        .put(ITEMS, JSONArray().apply {
            items.forEach { item ->
                put(
                    JSONObject()
                        .put(ID, item.id)
                        .put(TEXT, item.text)
                        .put(CHECKED, item.isChecked)
                        .put(QUANTITY, item.quantity)
                        .put(SECTION, item.section)
                        .apply { item.price?.let { put(PRICE, it) } }
                )
            }
        })
        .toString()

    companion object {
        private const val BODY = "body"
        private const val ITEMS = "items"
        private const val ID = "id"
        private const val TEXT = "text"
        private const val CHECKED = "checked"
        private const val QUANTITY = "quantity"
        private const val SECTION = "section"
        private const val PRICE = "price"

        fun fromJson(json: String): SealedContent {
            val root = JSONObject(json)
            val array = root.getJSONArray(ITEMS)
            val items = (0 until array.length()).map { i ->
                val item = array.getJSONObject(i)
                NoteItem(
                    id = item.getString(ID),
                    text = item.getString(TEXT),
                    isChecked = item.getBoolean(CHECKED),
                    quantity = item.getString(QUANTITY),
                    section = item.getString(SECTION),
                    price = if (item.has(PRICE)) item.getDouble(PRICE) else null
                )
            }
            return SealedContent(root.getString(BODY), items)
        }
    }
}

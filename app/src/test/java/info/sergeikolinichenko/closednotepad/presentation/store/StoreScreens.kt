package info.sergeikolinichenko.closednotepad.presentation.store

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.NoteItem
import info.sergeikolinichenko.closednotepad.models.NoteType
import info.sergeikolinichenko.closednotepad.models.NotesLayout
import info.sergeikolinichenko.closednotepad.presentation.MainActivity
import info.sergeikolinichenko.closednotepad.presentation.NotesApp
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.util.concurrent.TimeUnit

// Screens of the app with demo notes for the Google Play listing. Skipped unless
// STORE_ASSETS_DIR is set: STORE_ASSETS_DIR=/some/dir ./gradlew :app:testDebugUnitTest --tests '*store*'
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class StoreScreens(private val lang: String, private val night: Boolean = false) {

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private val outDir = System.getenv("STORE_ASSETS_DIR")?.let { File(it, "raw") }
    private val component get() = (compose.activity.application as NotesApp).component
    private val demo get() = DemoNotes.forLanguage(lang)

    @Before
    fun seed() {
        assumeTrue(outDir != null)
        runBlocking {
            val now = System.currentTimeMillis()
            demo.notes(now).forEach { component.notesRepository().createNote(it) }
        }
    }

    private fun string(id: Int) = compose.activity.getString(id)

    private fun waitForText(text: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun save(name: String) {
        compose.mainClock.advanceTimeBy(2_000)
        compose.waitForIdle()
        // captureToImage() waits for a frame that never comes under Robolectric, so draw the window by hand
        val bitmap = compose.runOnUiThread {
            val view = compose.activity.window.decorView
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888).also { view.draw(Canvas(it)) }
        }
        val suffix = if (night) "-night" else ""
        val file = File(outDir, "$lang/$name$suffix.png").apply { parentFile?.mkdirs() }
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test
    fun homeGrid() {
        waitForText(demo.shoppingTitle)
        save("home_grid")
    }

    @Test
    fun homeList() {
        component.preferencesRepository().setLayout(NotesLayout.LIST)
        waitForText(demo.shoppingTitle)
        save("home_list")
    }

    @Test
    fun shopping() {
        waitForText(demo.shoppingTitle)
        compose.onAllNodesWithText(demo.shoppingTitle).onFirst().performClick()
        waitForText(demo.shoppingItem)
        save("shopping")
        compose.onNodeWithContentDescription(string(R.string.store_mode)).performClick()
        waitForText(string(R.string.finish_shopping))
        save("store_mode")
    }

    @Test
    fun checklist() {
        waitForText(demo.checklistTitle)
        compose.onAllNodesWithText(demo.checklistTitle).onFirst().performClick()
        waitForText(demo.checklistItem)
        save("checklist")
    }

    @Test
    fun text() {
        waitForText(demo.shoppingTitle)
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(demo.textTitle))
        compose.onAllNodesWithText(demo.textTitle).onFirst().performClick()
        waitForText(demo.textTitle)
        compose.mainClock.advanceTimeBy(TimeUnit.SECONDS.toMillis(1))
        save("text")
    }
}

@Config(sdk = [34], qualifiers = "ru-rRU-w360dp-h800dp-xxhdpi")
class StoreScreensRu : StoreScreens("ru")

@Config(sdk = [34], qualifiers = "en-rUS-w360dp-h800dp-xxhdpi")
class StoreScreensEn : StoreScreens("en")

@Config(sdk = [34], qualifiers = "bg-rBG-w360dp-h800dp-xxhdpi")
class StoreScreensBg : StoreScreens("bg")

@Config(sdk = [34], qualifiers = "ru-rRU-w360dp-h800dp-night-xxhdpi")
class StoreScreensRuNight : StoreScreens("ru", night = true)

@Config(sdk = [34], qualifiers = "en-rUS-w360dp-h800dp-night-xxhdpi")
class StoreScreensEnNight : StoreScreens("en", night = true)

@Config(sdk = [34], qualifiers = "bg-rBG-w360dp-h800dp-night-xxhdpi")
class StoreScreensBgNight : StoreScreens("bg", night = true)

// Believable notes in each language, spread over the last weeks so the date groups show
class DemoNotes(
    val shoppingTitle: String,
    val shoppingItem: String,
    private val sections: List<Pair<String, List<Triple<String, String, Double?>>>>,
    val checklistTitle: String,
    val checklistItem: String,
    private val checklist: List<Pair<String, Boolean>>,
    val textTitle: String,
    private val textBody: String,
    private val others: List<Triple<String, String, Int>>,
    private val lockedTitles: List<String>
) {
    fun notes(now: Long): List<Note> {
        val hour = TimeUnit.HOURS.toMillis(1)
        val day = TimeUnit.DAYS.toMillis(1)
        var id = 0
        fun itemId() = "item${id++}"
        val shopping = Note(
            type = NoteType.SHOPPING,
            title = shoppingTitle,
            items = sections.flatMapIndexed { s, (section, items) ->
                items.mapIndexed { i, (name, qty, price) ->
                    NoteItem(itemId(), name, isChecked = s == 0 && i == 0, quantity = qty, section = section, price = price)
                }
            },
            colorIndex = 3, isPinned = true, createdAt = now - 3 * day, updatedAt = now - hour
        )
        val list = Note(
            type = NoteType.CHECKLIST,
            title = checklistTitle,
            items = checklist.map { (text, done) -> NoteItem(itemId(), text, isChecked = done) },
            colorIndex = 1, createdAt = now - 2 * day, updatedAt = now - 2 * hour
        )
        val text = Note(
            type = NoteType.TEXT, title = textTitle, body = textBody,
            colorIndex = 4, isPinned = true, createdAt = now - 5 * day, updatedAt = now - day - hour
        )
        val ages = listOf(3 * hour, 5 * hour, day + 3 * hour, 9 * day, 40 * day)
        val rest = others.mapIndexed { i, (title, body, color) ->
            Note(
                type = NoteType.TEXT, title = title, body = body, colorIndex = color,
                createdAt = now - ages[i % ages.size] - day, updatedAt = now - ages[i % ages.size]
            )
        }
        val locked = lockedTitles.mapIndexed { i, title ->
            Note(
                // The first one is pinned, so the seal shows at the top of every list
                type = NoteType.TEXT, title = title, body = "—", colorIndex = 6, isLocked = true, isPinned = i == 0,
                createdAt = now - 20 * day, updatedAt = if (i == 0) now - hour / 2 else now - 2 * day
            )
        }
        return listOf(shopping, list, text) + rest + locked
    }

    companion object {
        fun forLanguage(lang: String): DemoNotes = when (lang) {
            "ru" -> DemoNotes(
                shoppingTitle = "Продукты",
                shoppingItem = "Молоко",
                sections = listOf(
                    "Молочное" to listOf(
                        Triple("Молоко", "2 л", 2.4), Triple("Творог", "400 г", 3.1), Triple("Сыр", "", 6.5)
                    ),
                    "Овощи и фрукты" to listOf(
                        Triple("Помидоры", "1 кг", 3.0), Triple("Яблоки", "6 шт", null), Triple("Укроп", "", 0.8)
                    ),
                    "Бакалея" to listOf(Triple("Гречка", "2 уп", 2.2), Triple("Кофе", "", 9.9))
                ),
                checklistTitle = "В отпуск",
                checklistItem = "Паспорта и билеты",
                checklist = listOf(
                    "Паспорта и билеты" to true, "Зарядки и пауэрбанк" to true, "Аптечка" to false,
                    "Крем от солнца" to false, "Купальники" to false, "Отдать ключи соседям" to false
                ),
                textTitle = "Подарки",
                textBody = "Маме — тёплый плед и книга рецептов.\n\nПапе — набор отвёрток, давно хотел.\n\n" +
                    "Устроить пикник у озера, если будет солнце. Позвать Олю и Сашу, взять гитару.",
                others = listOf(
                    Triple("Пирог с яблоками", "Мука 300 г, сахар 150 г, 3 яйца, 4 яблока. 40 минут при 180°.", 0),
                    Triple("", "Позвонить в сервис насчёт машины в четверг", 7),
                    Triple("Книги на осень", "«Мастер и Маргарита», «Сто лет одиночества», «Шантарам»", 2),
                    Triple("Тренировки", "Пн, Ср, Пт — бассейн в 7:30", 5),
                    Triple("Мысли", "Меньше обещать, больше делать.", 6)
                ),
                lockedTitles = listOf("Коды и пароли", "Дневник")
            )
            "bg" -> DemoNotes(
                shoppingTitle = "Пазар",
                shoppingItem = "Мляко",
                sections = listOf(
                    "Млечни" to listOf(
                        Triple("Мляко", "2 л", 2.4), Triple("Кисело мляко", "3 бр", 3.1), Triple("Сирене", "", 6.5)
                    ),
                    "Плодове и зеленчуци" to listOf(
                        Triple("Домати", "1 кг", 3.0), Triple("Ябълки", "6 бр", null), Triple("Копър", "", 0.8)
                    ),
                    "Бакалия" to listOf(Triple("Ориз", "2 оп", 2.2), Triple("Кафе", "", 9.9))
                ),
                checklistTitle = "Почивка",
                checklistItem = "Паспорти и билети",
                checklist = listOf(
                    "Паспорти и билети" to true, "Зарядни и батерия" to true, "Аптечка" to false,
                    "Слънцезащитен крем" to false, "Бански" to false, "Ключовете на съседите" to false
                ),
                textTitle = "Подаръци",
                textBody = "За мама — топло одеяло и готварска книга.\n\nЗа татко — комплект отвертки.\n\n" +
                    "Пикник край езерото, ако е слънчево. Да поканим Мария и Иван, да вземем китарата.",
                others = listOf(
                    Triple("Ябълков пай", "Брашно 300 г, захар 150 г, 3 яйца, 4 ябълки. 40 минути на 180°.", 0),
                    Triple("", "Да се обадя на сервиза за колата в четвъртък", 7),
                    Triple("Книги за есента", "„Под игото“, „Тютюн“, „Време разделно“", 2),
                    Triple("Тренировки", "Пн, Ср, Пт — басейн в 7:30", 5),
                    Triple("Мисли", "По-малко обещания, повече дела.", 6)
                ),
                lockedTitles = listOf("Кодове и пароли", "Дневник")
            )
            else -> DemoNotes(
                shoppingTitle = "Groceries",
                shoppingItem = "Milk",
                sections = listOf(
                    "Dairy" to listOf(Triple("Milk", "2 l", 2.4), Triple("Yogurt", "3 pcs", 3.1), Triple("Cheese", "", 6.5)),
                    "Fruit and veg" to listOf(
                        Triple("Tomatoes", "1 kg", 3.0), Triple("Apples", "6 pcs", null), Triple("Dill", "", 0.8)
                    ),
                    "Pantry" to listOf(Triple("Rice", "2 packs", 2.2), Triple("Coffee", "", 9.9))
                ),
                checklistTitle = "Trip",
                checklistItem = "Passports and tickets",
                checklist = listOf(
                    "Passports and tickets" to true, "Chargers and power bank" to true, "First aid kit" to false,
                    "Sunscreen" to false, "Swimsuits" to false, "Leave the keys with neighbours" to false
                ),
                textTitle = "Gifts",
                textBody = "Mum — a warm blanket and a cookbook.\n\nDad — the screwdriver set he wanted.\n\n" +
                    "A picnic by the lake if it's sunny. Invite Anna and Tom, bring the guitar.",
                others = listOf(
                    Triple("Apple pie", "Flour 300 g, sugar 150 g, 3 eggs, 4 apples. 40 minutes at 180°.", 0),
                    Triple("", "Call the garage about the car on Thursday", 7),
                    Triple("Books for autumn", "The Master and Margarita, One Hundred Years of Solitude", 2),
                    Triple("Workouts", "Mon, Wed, Fri — pool at 7:30", 5),
                    Triple("Thoughts", "Promise less, do more.", 6)
                ),
                lockedTitles = listOf("Codes and passwords", "Diary")
            )
        }
    }
}

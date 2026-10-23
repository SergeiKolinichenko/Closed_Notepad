package info.sergeikolinichenko.closednotepad.presentation.store

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.presentation.ui.theme.SansFamily
import info.sergeikolinichenko.closednotepad.presentation.ui.theme.SerifFamily
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

// Google Play graphics made from the screens StoreScreens captured: poster screenshots
// (1080x1920), the feature graphic (1024x500) and the store icon (512x512)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class StoreGraphics {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    protected val root = System.getenv("STORE_ASSETS_DIR")?.let(::File)

    // setContent works once per test, so later pages swap what it shows
    private var page by mutableStateOf<(@Composable () -> Unit)?>(null)
    private var contentSet = false

    protected fun render(file: File, content: @Composable () -> Unit) {
        page = content
        if (!contentSet) {
            compose.setContent { page?.invoke() }
            contentSet = true
        }
        compose.waitForIdle()
        val bitmap = compose.runOnUiThread {
            val view = compose.activity.window.decorView
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888).also { view.draw(Canvas(it)) }
        }
        file.parentFile?.mkdirs()
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    protected fun screen(lang: String, name: String) =
        BitmapFactory.decodeFile(File(root, "raw/$lang/$name.png").path).asImageBitmap()
}

private val Paper = Color(0xFFF3EFE7)
private val Ink = Color(0xFF22201E)
private val InkSoft = Color(0xFF6B645B)
private val Seal = Color(0xFFB5412F)
private val Night = Color(0xFF1A1917)
private val NightInk = Color(0xFFECE6DC)
private val NightInkSoft = Color(0xFFA39B90)
private val NightSeal = Color(0xFFD9604A)

private class Poster(val screen: String, val dark: Boolean, val title: String, val subtitle: String)

private val posters = mapOf(
    "ru" to listOf(
        Poster("home_grid", false, "Все заметки\nна одном листе", "Тексты, списки дел и покупки"),
        Poster("shopping", false, "Покупки\nпо отделам", "Количество, цена и примерная сумма"),
        Poster("store_mode", false, "Режим\n«В магазине»", "Экран не гаснет, одно касание — и товар в корзине"),
        Poster("home_list-night", true, "Закрытые\nзаметки", "Открываются только по отпечатку или паролю экрана"),
        Poster("checklist", false, "Списки дел", "Отмечайте сделанное, порядок меняйте перетаскиванием"),
        Poster("text-night", true, "Без рекламы\nи интернета", "Заметки хранятся только на вашем телефоне")
    ),
    "en" to listOf(
        Poster("home_grid", false, "All your notes\non one sheet", "Texts, to-do lists and shopping lists"),
        Poster("shopping", false, "Shopping by\nstore section", "Quantities, prices and a running total"),
        Poster("store_mode", false, "In-store\nmode", "The screen stays on, one tap puts it in the cart"),
        Poster("home_list-night", true, "Locked\nnotes", "Open only with your fingerprint or screen lock"),
        Poster("checklist", false, "To-do lists", "Check things off, drag to reorder"),
        Poster("text-night", true, "No ads.\nNo internet.", "Your notes stay on your phone")
    ),
    "bg" to listOf(
        Poster("home_grid", false, "Всички бележки\nна един лист", "Текстове, списъци със задачи и за пазаруване"),
        Poster("shopping", false, "Пазаруване\nпо щандове", "Количество, цена и приблизителна сума"),
        Poster("store_mode", false, "Режим\n„В магазина“", "Екранът не угасва, едно докосване — и е в количката"),
        Poster("home_list-night", true, "Заключени\nбележки", "Отварят се само с пръстов отпечатък или заключването на екрана"),
        Poster("checklist", false, "Списъци\nсъс задачи", "Отмятайте свършеното, подреждайте с плъзгане"),
        Poster("text-night", true, "Без реклами\nи без интернет", "Бележките остават само на вашия телефон")
    )
)

private class Feature(val name: String, val tagline: String)

private val features = mapOf(
    "ru" to Feature("Закрытый блокнот", "Заметки, списки дел и покупки.\nБез рекламы и без интернета."),
    "en" to Feature("Closed Notepad", "Notes, to-do and shopping lists.\nNo ads. No internet."),
    "bg" to Feature("Заключен бележник", "Бележки, задачи и пазаруване.\nБез реклами и без интернет.")
)

@Composable
private fun Phone(image: androidx.compose.ui.graphics.ImageBitmap, width: Dp, dark: Boolean, modifier: Modifier = Modifier) {
    val bezel = width * 0.026f
    val radius = width * 0.12f
    val height = width * (2400f / 1080f) + bezel * 2
    Box(
        modifier
            .requiredWidth(width + bezel * 2)
            .requiredHeight(height)
            .shadow(24.dp, RoundedCornerShape(radius), ambientColor = Color.Black, spotColor = Color.Black)
            .clip(RoundedCornerShape(radius))
            .background(if (dark) Color(0xFF3A3632) else Ink)
            .padding(bezel)
    ) {
        Image(
            image,
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(radius - bezel))
        )
    }
}

@Composable
private fun PosterPage(poster: Poster, image: androidx.compose.ui.graphics.ImageBitmap) {
    val ink = if (poster.dark) NightInk else Ink
    val soft = if (poster.dark) NightInkSoft else InkSoft
    Box(Modifier.fillMaxSize().background(if (poster.dark) Night else Paper)) {
        Column(Modifier.padding(start = 30.dp, end = 30.dp, top = 40.dp)) {
            Box(Modifier.size(12.dp).clip(CircleShape).background(if (poster.dark) NightSeal else Seal))
            Spacer(Modifier.height(14.dp))
            Text(
                poster.title,
                style = TextStyle(fontFamily = SerifFamily, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 37.sp, color = ink)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                poster.subtitle,
                style = TextStyle(fontFamily = SansFamily, fontSize = 17.sp, lineHeight = 23.sp, color = soft)
            )
        }
        Phone(
            image, width = 262.dp, dark = poster.dark,
            modifier = Modifier.align(Alignment.TopCenter).offset(y = 236.dp).wrapContentSize(unbounded = true)
        )
    }
}

@Composable
private fun StoreIcon(modifier: Modifier = Modifier) {
    Box(modifier) {
        Image(painterResource(R.drawable.ic_notebook_icon_background), null, Modifier.fillMaxSize())
        // The launcher layer leaves room for the mask; the store icon is shown whole, so the sheet can be bigger
        Image(painterResource(R.drawable.ic_notebook_icon_foreground), null, Modifier.fillMaxSize().scale(1.4f))
    }
}

abstract class StorePosters(private val lang: String) : StoreGraphics() {

    @Test
    fun posters() {
        assumeTrue(root != null)
        posters.getValue(lang).forEachIndexed { i, poster ->
            render(File(root, "play/$lang/screenshot_${i + 1}.png")) { PosterPage(poster, screen(lang, poster.screen)) }
        }
    }
}

@Config(sdk = [34], qualifiers = "w360dp-h640dp-xxhdpi")
class StorePostersRu : StorePosters("ru")

@Config(sdk = [34], qualifiers = "w360dp-h640dp-xxhdpi")
class StorePostersEn : StorePosters("en")

@Config(sdk = [34], qualifiers = "w360dp-h640dp-xxhdpi")
class StorePostersBg : StorePosters("bg")

@Config(sdk = [34], qualifiers = "w1024dp-h500dp-mdpi")
class StoreFeatureGraphic : StoreGraphics() {

    @Test
    fun featureGraphics() {
        assumeTrue(root != null)
        features.forEach { (lang, feature) ->
            render(File(root, "play/$lang/feature_graphic.png")) {
                Box(Modifier.fillMaxSize().background(Paper)) {
                    Column(Modifier.padding(start = 64.dp, top = 72.dp).width(470.dp)) {
                        StoreIcon(Modifier.size(96.dp).clip(RoundedCornerShape(24.dp)))
                        Spacer(Modifier.height(28.dp))
                        Text(
                            feature.name,
                            style = TextStyle(fontFamily = SerifFamily, fontWeight = FontWeight.SemiBold, fontSize = 50.sp, lineHeight = 56.sp, color = Ink)
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            feature.tagline,
                            style = TextStyle(fontFamily = SansFamily, fontSize = 23.sp, lineHeight = 32.sp, color = InkSoft)
                        )
                    }
                    Row(Modifier.align(Alignment.TopEnd).padding(end = 56.dp, top = 52.dp)) {
                        Phone(screen(lang, "home_grid"), 196.dp, dark = false, modifier = Modifier.rotate(-4f).wrapContentSize(unbounded = true))
                        Spacer(Modifier.width(22.dp))
                        Phone(
                            screen(lang, "shopping-night"), 196.dp, dark = true,
                            modifier = Modifier.offset(y = 40.dp).rotate(4f).wrapContentSize(unbounded = true)
                        )
                    }
                }
            }
        }
    }
}

@Config(sdk = [34], qualifiers = "w512dp-h512dp-mdpi")
class StoreIconGraphic : StoreGraphics() {

    @Test
    fun icon() {
        assumeTrue(root != null)
        render(File(root, "play/icon_512.png")) { StoreIcon(Modifier.fillMaxSize()) }
    }
}

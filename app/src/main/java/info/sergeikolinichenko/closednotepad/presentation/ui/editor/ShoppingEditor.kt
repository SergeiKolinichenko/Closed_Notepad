package info.sergeikolinichenko.closednotepad.presentation.ui.editor

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.NoteItem
import info.sergeikolinichenko.closednotepad.presentation.ui.common.ProgressLine
import info.sergeikolinichenko.closednotepad.presentation.ui.home.progressText
import info.sergeikolinichenko.closednotepad.presentation.ui.theme.NoteColorScheme
import info.sergeikolinichenko.closednotepad.presentation.ui.theme.SansFamily
import java.text.NumberFormat

@Composable
fun ShoppingEditor(note: Note, colors: NoteColorScheme, viewModel: EditorViewModel) {
    val items = note.items.filter { it.text.isNotBlank() }
    val open = items.filter { !it.isChecked }
    val bought = items.filter { it.isChecked }
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    var input by rememberSaveable { mutableStateOf("") }
    var showBought by rememberSaveable { mutableStateOf(true) }
    var editing by remember { mutableStateOf<NoteItem?>(null) }
    val sections = items.map { it.section.trim() }.filter { it.isNotEmpty() }.distinct()
    val inList = items.mapTo(HashSet()) { it.text.lowercase() }
    val shownSuggestions = suggestions
        .filter { it.lowercase() !in inList && (input.isBlank() || it.startsWith(input.trim(), ignoreCase = true)) }
        .take(12)

    fun add(text: String) {
        viewModel.addShoppingInput(text, section = "")
        input = ""
    }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 48.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item(key = "header") {
            Column {
                TitleField(note.title, colors, viewModel::setTitle)
                ProgressLine(if (items.isEmpty()) 0f else bought.size / items.size.toFloat(), colors.accent)
                Spacer(Modifier.height(4.dp))
                Row {
                    Text(
                        progressText(note.type, bought.size, items.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.contentVariant,
                        modifier = Modifier.weight(1f)
                    )
                    totalPrice(items)?.let { total ->
                        Text(
                            stringResource(R.string.total_price, total),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.contentVariant
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                AddItemField(input, colors, onInput = { input = it }, onAdd = { add(input) }, onVoice = ::add)
                if (shownSuggestions.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        items(shownSuggestions) { suggestion ->
                            AssistChip(
                                onClick = { add(suggestion) },
                                label = { Text(suggestion) },
                                colors = AssistChipDefaults.assistChipColors(labelColor = colors.content)
                            )
                        }
                    }
                }
            }
        }
        groupBySection(open).forEach { (section, sectionItems) ->
            if (section != null) {
                item(key = "section-$section") {
                    Text(
                        section.ifEmpty { stringResource(R.string.no_section) },
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.contentVariant,
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                    )
                }
            }
            items(sectionItems, key = { it.id }) { item ->
                ShoppingRow(
                    item, colors,
                    onToggle = { viewModel.toggleItem(item.id) },
                    onEdit = { editing = item },
                    modifier = Modifier.animateItem()
                )
            }
        }
        if (bought.isNotEmpty()) {
            item(key = "bought-header") {
                CheckedHeader(
                    text = stringResource(R.string.in_cart_count, bought.size),
                    expanded = showBought,
                    colors = colors,
                    onClick = { showBought = !showBought }
                )
            }
            if (showBought) {
                items(bought, key = { it.id }) { item ->
                    ShoppingRow(
                        item, colors,
                        onToggle = { viewModel.toggleItem(item.id) },
                        onEdit = { editing = item },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }

    editing?.let { item ->
        ItemEditSheet(
            item = item,
            sections = sections,
            onDismiss = { editing = null },
            onSave = { viewModel.replaceItem(it); editing = null },
            onRemove = { viewModel.removeItem(item.id); editing = null }
        )
    }
}

// Sections in the order they first appear; items without one go first.
// No headers at all when nobody uses sections.
private fun groupBySection(items: List<NoteItem>): List<Pair<String?, List<NoteItem>>> {
    val groups = items.groupBy { it.section.trim() }
    if (groups.keys.all { it.isEmpty() }) return listOf(null to items)
    return groups.entries
        .sortedBy { if (it.key.isEmpty()) 0 else 1 }
        .map { it.key to it.value }
}

private fun totalPrice(items: List<NoteItem>): String? {
    val prices = items.mapNotNull { it.price }
    if (prices.isEmpty()) return null
    return NumberFormat.getNumberInstance().apply { maximumFractionDigits = 2 }.format(prices.sum())
}

@Composable
private fun AddItemField(
    input: String,
    colors: NoteColorScheme,
    onInput: (String) -> Unit,
    onAdd: () -> Unit,
    onVoice: (String) -> Unit
) {
    val context = LocalContext.current
    val speechIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    }
    val canSpeak = remember { speechIntent.resolveActivity(context.packageManager) != null }
    val voice = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let(onVoice)
        }
    }
    Surface(
        color = colors.line,
        contentColor = colors.content,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Add, contentDescription = null, tint = colors.contentVariant)
            Spacer(Modifier.width(8.dp))
            BasicTextField(
                value = input,
                onValueChange = onInput,
                singleLine = true,
                textStyle = TextStyle(fontFamily = SansFamily, fontSize = 17.sp, color = colors.content),
                cursorBrush = SolidColor(colors.content),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onAdd() }),
                modifier = Modifier.weight(1f).padding(vertical = 14.dp),
                decorationBox = { inner ->
                    if (input.isEmpty()) {
                        Text(stringResource(R.string.add_product_hint), fontSize = 17.sp, color = colors.contentVariant)
                    }
                    inner()
                }
            )
            if (input.isNotBlank()) {
                TextButton(onClick = onAdd) { Text(stringResource(R.string.add), color = colors.content) }
            } else if (canSpeak) {
                IconButton(onClick = { voice.launch(speechIntent) }) {
                    Icon(Icons.Outlined.Mic, contentDescription = stringResource(R.string.voice_input))
                }
            }
        }
    }
}

@Composable
private fun ShoppingRow(
    item: NoteItem,
    colors: NoteColorScheme,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val label = item.text
    Row(modifier.fillMaxWidth().clickable(onClick = onEdit), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = colors.content,
                uncheckedColor = colors.contentVariant,
                checkmarkColor = colors.container
            ),
            modifier = Modifier.semantics { contentDescription = label }
        )
        Text(
            item.text,
            fontSize = 17.sp,
            color = if (item.isChecked) colors.contentVariant else colors.content,
            textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
            modifier = Modifier.weight(1f).padding(vertical = 12.dp)
        )
        if (item.quantity.isNotBlank()) {
            Surface(color = colors.line, contentColor = colors.content, shape = RoundedCornerShape(8.dp)) {
                Text(
                    item.quantity,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemEditSheet(
    item: NoteItem,
    sections: List<String>,
    onDismiss: () -> Unit,
    onSave: (NoteItem) -> Unit,
    onRemove: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf(item.text) }
    var quantity by rememberSaveable { mutableStateOf(item.quantity) }
    var section by rememberSaveable { mutableStateOf(item.section) }
    var price by rememberSaveable { mutableStateOf(item.price?.let { formatPrice(it) }.orEmpty()) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.edit_product), style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.product_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text(stringResource(R.string.quantity)) },
                    placeholder = { Text(stringResource(R.string.quantity_hint)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text(stringResource(R.string.price)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = section,
                onValueChange = { section = it },
                label = { Text(stringResource(R.string.section)) },
                placeholder = { Text(stringResource(R.string.section_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (sections.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(sections) { existing ->
                        FilterChip(
                            selected = existing == section.trim(),
                            onClick = { section = existing },
                            label = { Text(existing) }
                        )
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onRemove) { Text(stringResource(R.string.remove_item)) }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
                TextButton(
                    enabled = name.isNotBlank(),
                    onClick = {
                        onSave(
                            item.copy(
                                text = name.trim(),
                                quantity = quantity.trim(),
                                section = section.trim(),
                                price = price.replace(',', '.').trim().toDoubleOrNull()
                            )
                        )
                    }
                ) { Text(stringResource(R.string.save)) }
            }
        }
    }
}

private fun formatPrice(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()


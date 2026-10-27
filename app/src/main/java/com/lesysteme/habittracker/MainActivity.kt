package com.lesysteme.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

private val ComponentActivity.dataStore by preferencesDataStore(name = "system_save")
private val XP_KEY = intPreferencesKey("total_xp")
private val DONE_KEY = stringPreferencesKey("done_today")
private val EARNED_KEY = stringPreferencesKey("earned_today")

// Niveau 12 · 850 / 1 200 XP au premier lancement (valeurs de la maquette)
private const val START_XP = 11 * 1200 + 850

sealed interface Overlay {
    data class Detail(val habitId: Int) : Overlay
    data object Calendar : Overlay
    data object Portals : Overlay
    data object Achievements : Overlay
    data object Appearance : Overlay
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val scope = rememberCoroutineScope()
            var xp by remember { mutableIntStateOf(START_XP) }
            var done by remember { mutableStateOf(setOf<Int>()) }
            var earned by remember { mutableStateOf(setOf<Int>()) }
            var tab by remember { mutableIntStateOf(0) }
            var overlay by remember { mutableStateOf<Overlay?>(null) }
            var custom by remember { mutableStateOf(emptyList<Habit>()) }
            var showAdd by remember { mutableStateOf(false) }
            val habits = seedHabits + custom
            val today = LocalDate.now().toString()

            LaunchedEffect(Unit) {
                val p = dataStore.data.first()
                xp = p[XP_KEY] ?: START_XP
                fun load(raw: String?): Set<Int> {
                    val parts = (raw ?: "").split("|", limit = 2)
                    return if (parts.size == 2 && parts[0] == today)
                        parts[1].split(",").mapNotNull { it.toIntOrNull() }.toSet() else emptySet()
                }
                done = load(p[DONE_KEY]); earned = load(p[EARNED_KEY])
            }

            fun toggle(h: Habit) {
                val was = h.id in done
                done = if (was) done - h.id else done + h.id
                if (!was && h.id !in earned) { xp += h.xp; earned = earned + h.id }
                scope.launch {
                    dataStore.edit {
                        it[XP_KEY] = xp
                        it[DONE_KEY] = "$today|" + done.sorted().joinToString(",")
                        it[EARNED_KEY] = "$today|" + earned.sorted().joinToString(",")
                    }
                }
            }

            val barSel = when (val o = overlay) {
                null -> tab
                is Overlay.Portals -> 2
                is Overlay.Achievements -> 4
                is Overlay.Appearance -> 0
                else -> 1
            }
            BackHandler(enabled = overlay != null) { overlay = null }

            MaterialTheme(colorScheme = darkColorScheme(primary = Accent, background = BgBot, surface = CardBg)) {
                Column(Modifier.fillMaxSize().background(BgBot).statusBarsPadding()) {
                    Box(Modifier.weight(1f)) {
                        ScreenBackground {
                            when (val o = overlay) {
                                is Overlay.Detail -> habits.firstOrNull { it.id == o.habitId }?.let { h ->
                                    DetailScreen(h, h.id in done, { toggle(h) }, { overlay = null }, { overlay = Overlay.Calendar }, { overlay = null; tab = 3 })
                                }
                                Overlay.Calendar -> CalendarScreen(habits, done)
                                Overlay.Portals -> PortalsScreen()
                                Overlay.Achievements -> AchievementsScreen()
                                Overlay.Appearance -> AppearanceScreen { overlay = null }
                                null -> when (tab) {
                                    0 -> HomeScreen(habits, done, xp, ::toggle, { overlay = Overlay.Detail(it.id) }, { overlay = Overlay.Appearance })
                                    1 -> HabitsScreen(habits, { overlay = Overlay.Detail(it.id) }, { showAdd = true })
                                    2 -> QuestsScreen(done.size, { overlay = Overlay.Portals })
                                    3 -> StatsScreen(habits)
                                    else -> ProfileScreen(xp, { overlay = Overlay.Achievements })
                                }
                            }
                        }
                    }
                    BottomBar(barSel) { tab = it; overlay = null }
                }

                if (showAdd) {
                    var name by remember { mutableStateOf("") }
                    AlertDialog(
                        onDismissRequest = { showAdd = false },
                        title = { Text("Nouvelle habitude") },
                        text = { OutlinedTextField(name, { name = it }, singleLine = true, label = { Text("Nom") }) },
                        confirmButton = {
                            TextButton(onClick = {
                                if (name.isNotBlank()) {
                                    val id = (habits.maxOfOrNull { it.id } ?: 0) + 1
                                    custom = custom + Habit(id, name.trim(), "15 min / jour", Icons.Filled.Star, Violet, "Perso", 0, 0, 15, 0, "Moyenne", "15 minutes")
                                }
                                showAdd = false
                            }) { Text("Ajouter") }
                        },
                        dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Annuler") } }
                    )
                }
            }
        }
    }
}

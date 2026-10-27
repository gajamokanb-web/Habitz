package com.lesysteme.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ComponentActivity.dataStore by preferencesDataStore(name = "system_save")
private val XP_KEY = intPreferencesKey("total_xp")
private val DONE_KEY = stringPreferencesKey("done_today")
private val EARNED_KEY = stringPreferencesKey("earned_today")
private val PURPLE = Color(0xFFAA78FF)
private val BG = Color(0xFF070A13)
private val PANEL = Color(0xFF111626)
private val MUTED = Color(0xFF9BA4BA)
private val TEXT = Color(0xFFF5F3FF)
private val PINK = Color(0xFFFF4F9A)
private val CYAN = Color(0xFF4DCBFF)
private val GREEN = Color(0xFF55E6B0)

data class Habit(
    val id: Int, val title: String, val subtitle: String, val icon: String,
    val xp: Int, val color: Color, val category: String
)

private val seedHabits = listOf(
    Habit(1, "Sport", "30 minutes", "⚔", 20, Color(0xFFFF5579), "Santé"),
    Habit(2, "Lire", "20 minutes", "▤", 15, Color(0xFFFFB454), "Savoir"),
    Habit(3, "Boire de l'eau", "2 L / jour", "♢", 10, CYAN, "Santé"),
    Habit(4, "Méditer", "10 minutes", "✺", 15, PURPLE, "Discipline"),
    Habit(5, "Apprendre", "1 session", "⌘", 20, Color(0xFF8D8BFF), "Savoir"),
    Habit(6, "Pas du jour", "10 000 pas", "➤", 15, GREEN, "Vitalité"),
    Habit(7, "Saxo / Piano", "30 minutes", "♫", 20, Color(0xFFFFB454), "Créativité")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val scope = rememberCoroutineScope()
            var xp by remember { mutableIntStateOf(0) }
            var done by remember { mutableStateOf(setOf<Int>()) }
            var earnedToday by remember { mutableStateOf(setOf<Int>()) }
            var tab by remember { mutableIntStateOf(0) }
            var showAdd by remember { mutableStateOf(false) }
            var customHabits by remember { mutableStateOf(emptyList<Habit>()) }
            var newName by remember { mutableStateOf("") }
            var newMinutes by remember { mutableStateOf("15 minutes") }
            val habits = seedHabits + customHabits
            val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

            LaunchedEffect(Unit) {
                val prefs = dataStore.data.first()
                xp = prefs[XP_KEY] ?: 0
                val saved = prefs[DONE_KEY] ?: ""
                val parts = saved.split("|", limit = 2)
                done = if (parts.size == 2 && parts[0] == today) {
                    parts[1].split(",").filter { it.isNotBlank() }.mapNotNull { it.toIntOrNull() }.toSet()
                } else emptySet()
                val earnedSaved = prefs[EARNED_KEY] ?: ""
                val earnedParts = earnedSaved.split("|", limit = 2)
                earnedToday = if (earnedParts.size == 2 && earnedParts[0] == today) {
                    earnedParts[1].split(",").filter { it.isNotBlank() }.mapNotNull { it.toIntOrNull() }.toSet()
                } else emptySet()
            }

            fun toggleHabit(h: Habit) {
                val wasDone = h.id in done
                done = if (wasDone) done - h.id else done + h.id
                if (!wasDone && h.id !in earnedToday) {
                    xp += h.xp
                    earnedToday = earnedToday + h.id
                }
                scope.launch {
                    dataStore.edit { prefs ->
                        prefs[XP_KEY] = xp
                        prefs[DONE_KEY] = "$today|" + done.sorted().joinToString(",")
                        prefs[EARNED_KEY] = "$today|" + earnedToday.sorted().joinToString(",")
                    }
                }
            }

            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = PURPLE, secondary = PINK, background = BG,
                    surface = PANEL, onBackground = TEXT, onSurface = TEXT
                )
            ) {
                Box(Modifier.fillMaxSize().background(BG)) {
                    Column(Modifier.fillMaxSize()) {
                        when (tab) {
                            0 -> Dashboard(habits, done, xp, ::toggleHabit, { showAdd = true })
                            1 -> HabitsScreen(habits, done, xp, ::toggleHabit, { showAdd = true })
                            2 -> QuestsScreen(habits, done, xp)
                            3 -> StatsScreen(habits, done, xp)
                            else -> ProfileScreen(xp, done.size, habits.size)
                        }
                        Spacer(Modifier.height(76.dp))
                    }
                    NavigationBar(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        containerColor = Color(0xFF0B0F1B),
                        tonalElevation = 0.dp
                    ) {
                        val labels = listOf("Accueil", "Habitudes", "Quêtes", "Stats", "Profil")
                        val icons = listOf(Icons.Default.Home, Icons.Default.CheckCircle, Icons.Default.Flag, Icons.Default.BarChart, Icons.Default.Person)
                        labels.forEachIndexed { i, label ->
                            NavigationBarItem(
                                selected = tab == i,
                                onClick = { tab = i },
                                icon = { Icon(icons[i], contentDescription = label) },
                                label = { Text(label, fontSize = 10.sp, maxLines = 1) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PURPLE,
                                    selectedTextColor = PURPLE,
                                    unselectedIconColor = MUTED,
                                    unselectedTextColor = MUTED,
                                    indicatorColor = Color(0xFF24183D)
                                )
                            )
                        }
                    }
                    if (showAdd) {
                        AlertDialog(
                            onDismissRequest = { showAdd = false },
                            containerColor = PANEL,
                            title = { Text("Nouvelle habitude", color = TEXT) },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = newName, onValueChange = { newName = it },
                                        label = { Text("Nom") }, singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = newMinutes, onValueChange = { newMinutes = it },
                                        label = { Text("Objectif") }, singleLine = true
                                    )
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    if (newName.isNotBlank()) {
                                        val id = (habits.maxOfOrNull { it.id } ?: 0) + 1
                                        customHabits = customHabits + Habit(id, newName.trim(), newMinutes.trim(), "✦", 15, PURPLE, "Personnalisé")
                                        newName = ""; newMinutes = "15 minutes"; showAdd = false
                                    }
                                }) { Text("Créer") }
                            },
                            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Annuler") } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Dashboard(
    habits: List<Habit>, done: Set<Int>, xp: Int,
    onToggle: (Habit) -> Unit, onAdd: () -> Unit
) {
    val level = levelFor(xp)
    val progress = progressFor(xp)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Box(Modifier.fillMaxWidth().height(235.dp).clip(RoundedCornerShape(26.dp))) {
                Image(
                    painter = painterResource(R.drawable.fantasy_backdrop),
                    contentDescription = "Illustration dark fantasy",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color(0x22070A13), Color(0xEE070A13)))
                ))
                Column(Modifier.align(Alignment.BottomStart).padding(18.dp)) {
                    Text(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH)).replaceFirstChar { it.uppercase() },
                        color = Color.White.copy(alpha = .8f), fontSize = 12.sp)
                    Text("LE SYSTÈME", fontSize = 29.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 2.sp)
                    Text("Deviens une meilleure version de toi.", color = Color(0xFFD9D1FF), fontSize = 13.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("RANG ${rankFor(level)}  ·  Niveau $level", color = PURPLE, fontWeight = FontWeight.Bold)
                            Text("$xp XP au total", color = Color.White, fontSize = 12.sp)
                        }
                        Text("${(progress * 100).toInt()}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    }
                    Spacer(Modifier.height(7.dp))
                    ProgressBar(progress, PURPLE)
                }
            }
        }
        item {
            SectionHeader("Progression du jour", "${done.size}/${habits.size}")
            Panel {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("${if (habits.isEmpty()) 0 else done.size * 100 / habits.size}%", fontSize = 34.sp, fontWeight = FontWeight.Bold)
                        Text("Objectifs accomplis", color = MUTED, fontSize = 12.sp)
                        Spacer(Modifier.height(10.dp))
                        Text("Continue, chasseur.", color = PURPLE, fontSize = 13.sp)
                    }
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(94.dp)) {
                        CircularProgressIndicator(
                            progress = { if (habits.isEmpty()) 0f else done.size.toFloat() / habits.size },
                            color = PURPLE, trackColor = Color(0xFF2A2940), strokeWidth = 8.dp
                        )
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = PURPLE, modifier = Modifier.size(30.dp))
                    }
                }
            }
        }
        item { SectionHeader("Mes habitudes", "Aujourd'hui") }
        items(habits) { habit -> HabitRow(habit, habit.id in done, onToggle) }
        item {
            OutlinedButton(onClick = onAdd, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Ajouter une habitude")
            }
        }
    }
}

@Composable
private fun HabitsScreen(habits: List<Habit>, done: Set<Int>, xp: Int, onToggle: (Habit) -> Unit, onAdd: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        ScreenTitle("Habitudes", "Chaque action compte.")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${habits.size} habitudes actives", color = MUTED, fontSize = 13.sp)
            TextButton(onClick = onAdd) { Icon(Icons.Default.Add, null); Text("Ajouter") }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 18.dp)) {
            items(habits) { HabitRow(it, it.id in done, onToggle) }
        }
    }
}

@Composable
private fun QuestsScreen(habits: List<Habit>, done: Set<Int>, xp: Int) {
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        ScreenTitle("Quêtes", "Les missions du chasseur.")
        Panel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WbSunny, null, tint = Color(0xFFFFB454), modifier = Modifier.size(34.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Routine quotidienne", fontWeight = FontWeight.Bold)
                    Text("Valide 4 habitudes aujourd'hui", color = MUTED, fontSize = 12.sp)
                }
                Text("+25 XP", color = PURPLE, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            ProgressBar((done.size / 4f).coerceIn(0f, 1f), PURPLE)
            Text("${done.size.coerceAtMost(4)}/4 actions", color = MUTED, fontSize = 11.sp)
        }
        Spacer(Modifier.height(12.dp))
        Panel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, null, tint = CYAN, modifier = Modifier.size(34.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Portail de la discipline", fontWeight = FontWeight.Bold)
                    Text("Réalise 20 habitudes cette semaine", color = MUTED, fontSize = 12.sp)
                }
                Text("+100 XP", color = PURPLE, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            ProgressBar((done.size / 20f).coerceIn(0f, 1f), CYAN)
            Text("${done.size}/20 validations aujourd'hui (aperçu MVP)", color = MUTED, fontSize = 11.sp)
        }
        Spacer(Modifier.height(12.dp))
        Panel {
            Text("Règle du Système", fontWeight = FontWeight.Bold, color = PURPLE)
            Spacer(Modifier.height(6.dp))
            Text("Les quêtes affichent ici les défis de départ. Les récompenses de quête ne sont pas encore créditées automatiquement dans cette version.", color = MUTED, fontSize = 13.sp)
        }
    }
}

@Composable
private fun StatsScreen(habits: List<Habit>, done: Set<Int>, xp: Int) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ScreenTitle("Statistiques", "Observe ta progression.") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("XP totale", "$xp", "Énergie gagnée", Modifier.weight(1f), PURPLE)
                StatCard("Niveau", "${levelFor(xp)}", "Rang ${rankFor(levelFor(xp))}", Modifier.weight(1f), CYAN)
            }
        }
        item {
            Panel {
                Text("Taux de réussite du jour", color = MUTED, fontSize = 12.sp)
                Text("${if (habits.isEmpty()) 0 else done.size * 100 / habits.size}%", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = GREEN)
                ProgressBar(if (habits.isEmpty()) 0f else done.size.toFloat() / habits.size, GREEN)
                Text("${done.size} habitudes validées sur ${habits.size}", color = MUTED, fontSize = 12.sp)
            }
        }
        item { Text("Performance par habitude", fontWeight = FontWeight.Bold, fontSize = 17.sp) }
        items(habits) { h ->
            Panel {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(h.icon, color = h.color, fontSize = 22.sp, modifier = Modifier.width(35.dp))
                    Text(h.title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                    Text(if (h.id in done) "Réussie" else "À faire", color = if (h.id in done) GREEN else MUTED, fontSize = 11.sp)
                }
            }
        }
        item { Text("Les graphiques historiques et les tendances seront ajoutés dans une prochaine version.", color = MUTED, fontSize = 12.sp) }
    }
}

@Composable
private fun ProfileScreen(xp: Int, doneToday: Int, totalHabits: Int) {
    val level = levelFor(xp)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { ScreenTitle("Personnage", "Ton évolution, ton histoire.") }
        item {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(
                Brush.linearGradient(listOf(Color(0xFF26164A), Color(0xFF111626), Color(0xFF1A1029)))
            ).padding(22.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(Modifier.size(86.dp).clip(CircleShape).background(Color(0xFF312054)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = PURPLE, modifier = Modifier.size(54.dp))
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("CHASSEUR", color = PURPLE, letterSpacing = 3.sp, fontSize = 11.sp)
                    Text("Niveau $level · Rang ${rankFor(level)}", fontSize = 23.sp, fontWeight = FontWeight.Bold)
                    Text("$xp XP cumulés", color = MUTED)
                    Spacer(Modifier.height(14.dp))
                    ProgressBar(progressFor(xp), PURPLE)
                }
            }
        }
        item {
            Text("ATTRIBUTS", color = MUTED, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, fontSize = 12.sp)
        }
        item { AttributeRow("Force", "Sport et mouvement", 68, Color(0xFFFF5579)) }
        item { AttributeRow("Vitalité", "Santé et récupération", 74, GREEN) }
        item { AttributeRow("Intelligence", "Lecture et apprentissage", 52, CYAN) }
        item { AttributeRow("Discipline", "Régularité", 81, PURPLE) }
        item {
            Panel {
                Text("Aujourd'hui", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("$doneToday / $totalHabits habitudes accomplies", color = MUTED, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun HabitRow(habit: Habit, completed: Boolean, onToggle: (Habit) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(if (completed) Color(0xFF151B2B) else PANEL)
            .clickable { onToggle(habit) }.padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(44.dp).clip(RoundedCornerShape(13.dp)).background(habit.color.copy(alpha = .13f)), contentAlignment = Alignment.Center) {
            Text(habit.icon, color = habit.color, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(habit.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(habit.subtitle, color = MUTED, fontSize = 12.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("+${habit.xp} XP", color = habit.color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            Box(
                Modifier.size(28.dp).clip(CircleShape)
                    .background(if (completed) PURPLE else Color.Transparent)
                    .then(if (!completed) Modifier.background(Color.Transparent) else Modifier),
                contentAlignment = Alignment.Center
            ) {
                if (completed) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                else Icon(Icons.Default.RadioButtonUnchecked, null, tint = MUTED, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
private fun ScreenTitle(title: String, subtitle: String) {
    Column(Modifier.padding(top = 10.dp, bottom = 8.dp)) {
        Text(title, fontSize = 28.sp, fontWeight = FontWeight.Black, letterSpacing = (-.5).sp)
        Text(subtitle, color = MUTED, fontSize = 13.sp)
    }
}

@Composable
private fun SectionHeader(title: String, trailing: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(trailing, color = PURPLE, fontSize = 12.sp)
    }
}

@Composable
private fun Panel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
            .background(PANEL).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        content = content
    )
}

@Composable
private fun ProgressBar(progress: Float, color: Color) {
    Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(Color(0xFF282B3B))) {
        Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).fillMaxHeight().clip(CircleShape).background(
            Brush.horizontalGradient(listOf(color.copy(alpha = .65f), color))
        ))
    }
}

@Composable
private fun StatCard(title: String, value: String, subtitle: String, modifier: Modifier, color: Color) {
    Column(modifier.clip(RoundedCornerShape(18.dp)).background(PANEL).padding(15.dp)) {
        Text(title, color = MUTED, fontSize = 12.sp)
        Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
        Text(subtitle, color = MUTED, fontSize = 11.sp)
    }
}

@Composable
private fun AttributeRow(name: String, subtitle: String, value: Int, color: Color) {
    Panel {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold)
                Text(subtitle, color = MUTED, fontSize = 11.sp)
            }
            Text("$value", color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        ProgressBar(value / 100f, color)
    }
}

private fun levelFor(totalXp: Int): Int {
    var remaining = totalXp.coerceAtLeast(0)
    var level = 1
    var next = 100
    while (remaining >= next && level < 999) {
        remaining -= next
        level++
        next = 100 + 25 * (level - 1)
    }
    return level
}

private fun progressFor(totalXp: Int): Float {
    var remaining = totalXp.coerceAtLeast(0)
    var level = 1
    var next = 100
    while (remaining >= next && level < 999) {
        remaining -= next
        level++
        next = 100 + 25 * (level - 1)
    }
    return (remaining.toFloat() / next).coerceIn(0f, 1f)
}

private fun rankFor(level: Int): String = when {
    level >= 55 -> "S"
    level >= 35 -> "A"
    level >= 20 -> "B"
    level >= 10 -> "C"
    level >= 5 -> "D"
    else -> "E"
}

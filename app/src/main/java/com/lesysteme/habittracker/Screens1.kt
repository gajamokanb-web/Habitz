package com.lesysteme.habittracker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val XP_PER_LEVEL = 1200

fun levelOf(total: Int) = total / XP_PER_LEVEL + 1
fun xpInLevel(total: Int) = total % XP_PER_LEVEL
fun rankOf(level: Int) = when {
    level < 5 -> "E"; level < 10 -> "D"; level < 20 -> "C"; level < 35 -> "B"; level < 50 -> "A"; else -> "S"
}

// ───────────────────────── 1. ACCUEIL ─────────────────────────
@Composable
fun HomeScreen(habits: List<Habit>, done: Set<Int>, totalXp: Int, onToggle: (Habit) -> Unit, onOpen: (Habit) -> Unit, onSettings: () -> Unit) {
    val level = levelOf(totalXp)
    val cur = xpInLevel(totalXp)
    val date = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE d MMM yyyy", Locale.FRENCH))
        .replaceFirstChar { it.uppercase() }
    val doneCount = habits.count { it.id in done }
    val frac = if (habits.isEmpty()) 0f else doneCount.toFloat() / habits.size

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
        item {
            Box(Modifier.fillMaxWidth().height(150.dp)) {
                Image(
                    painterResource(R.drawable.hero_home), null,
                    Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alignment = Alignment.TopEnd
                )
                Box(Modifier.matchParentSize().background(Brush.horizontalGradient(0f to BgTop, 0.35f to BgTop.copy(alpha = 0.85f), 1f to Color.Transparent)))
                Box(Modifier.matchParentSize().background(Brush.verticalGradient(0.6f to Color.Transparent, 1f to BgTop)))
                Column(Modifier.padding(start = 20.dp, top = 28.dp)) {
                    Text("Le Système", color = TextMain, fontFamily = FontFamily.Serif, fontSize = 32.sp, letterSpacing = 1.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(date, color = TextMuted, fontSize = 13.sp)
                }
                Box(Modifier.align(Alignment.TopEnd).padding(top = 28.dp, end = 20.dp)) {
                    SquareButton(Icons.Filled.Settings, "Apparence", onSettings)
                }
            }
        }
        item {
            Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(
                    Modifier.size(72.dp).card(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                ) {
                    Text("RANG", color = TextMuted, fontSize = 10.sp, letterSpacing = 1.sp)
                    Text(rankOf(level), color = Green, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                }
                Column(Modifier.padding(start = 16.dp)) {
                    Text("Niveau $level", color = TextMain, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("$cur / 1 200 XP", color = TextMuted, fontSize = 13.sp)
                }
            }
            Bar(cur / XP_PER_LEVEL.toFloat(), Modifier.padding(horizontal = 20.dp, vertical = 10.dp).fillMaxWidth(), 10.dp)
            Box(Modifier.padding(horizontal = 20.dp).fillMaxWidth().card(12.dp, Accent.copy(alpha = 0.5f)).padding(12.dp), contentAlignment = Alignment.Center) {
                Text("« Chaque petite action te rend plus fort. »", color = TextMuted, fontSize = 13.sp, textAlign = TextAlign.Center)
            }
        }
        item {
            Row(
                Modifier.padding(20.dp).fillMaxWidth().card(16.dp).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Progression du jour", color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text("$doneCount / ${habits.size}", color = TextMain, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
                Ring(frac, 72.dp, 8.dp, Accent) {
                    Text("${(frac * 100).toInt()}%", color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        items(habits, key = { it.id }) { h ->
            Row(
                Modifier.padding(horizontal = 20.dp, vertical = 5.dp).fillMaxWidth().card(14.dp)
                    .clickable { onOpen(h) }.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconTile(h.icon, h.color)
                Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                    Text(h.name, color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text(h.sub.removeSuffix(" / jour"), color = TextMuted, fontSize = 13.sp)
                }
                CheckCircle(h.id in done) { onToggle(h) }
            }
        }
    }
}

// ───────────────────────── 2. HABITUDES ─────────────────────────
@Composable
fun HabitsScreen(habits: List<Habit>, onOpen: (Habit) -> Unit, onAdd: () -> Unit) {
    var filter by remember { mutableIntStateOf(0) }
    val labels = listOf("Toutes", "Santé", "Productivité", "Perso")
    val shown = if (filter == 0) habits else habits.filter { it.category == labels[filter] }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Title("Habitudes", Modifier.weight(1f))
            SquareButton(Icons.Filled.Add, "Ajouter une habitude", onAdd)
        }
        PillTabs(labels, filter, { filter = it }, equal = false)
        Spacer(Modifier.height(12.dp))
        LazyColumn(contentPadding = PaddingValues(bottom = 12.dp)) {
            items(shown, key = { it.id }) { h ->
                Row(
                    Modifier.padding(horizontal = 20.dp, vertical = 5.dp).fillMaxWidth().card(14.dp)
                        .clickable { onOpen(h) }.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconTile(h.icon, h.color, 50.dp)
                    Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                        Text(h.name, color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text(h.sub, color = TextMuted, fontSize = 13.sp)
                    }
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        listOf(8.dp, 12.dp, 16.dp, 20.dp).forEach { Box(Modifier.width(4.dp).height(it).clip(RoundedCornerShape(1.dp)).background(Green)) }
                    }
                    Text("${h.streak} jours", color = TextMuted, fontSize = 13.sp, modifier = Modifier.padding(start = 8.dp))
                    Icon(Icons.Filled.MoreVert, null, tint = TextMuted, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ───────────────────────── 3. DÉTAIL ─────────────────────────
@Composable
fun DetailScreen(h: Habit, isDone: Boolean, onToggle: () -> Unit, onBack: () -> Unit, onHistory: () -> Unit, onStats: () -> Unit) {
    var tab by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(150.dp)) {
            Image(painterResource(R.drawable.hero_profile), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.matchParentSize().background(Brush.verticalGradient(0f to Color(0x66000000), 0.55f to Color.Transparent, 1f to BgTop)))
            Row(Modifier.padding(horizontal = 20.dp, vertical = 14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SquareButton(Icons.Filled.ArrowBack, "Retour", onBack)
                Spacer(Modifier.weight(1f))
                SquareButton(Icons.Filled.Edit, "Modifier") {}
                SquareButton(Icons.Filled.CalendarMonth, "Planning", onHistory)
            }
            Row(Modifier.align(Alignment.BottomStart).padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                IconTile(h.icon, h.color, 62.dp)
                Column(Modifier.padding(start = 14.dp)) {
                    Text(h.name, color = TextMain, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("${h.objective} par jour", color = TextMuted, fontSize = 14.sp)
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        PillTabs(listOf("Vue d'ensemble", "Historique", "Statistiques"), tab, {
            tab = it; if (it == 1) onHistory() else if (it == 2) onStats()
        })
        Spacer(Modifier.height(14.dp))
        Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f).card(14.dp).padding(14.dp)) {
                Text("Série actuelle", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Whatshot, null, tint = Orange, modifier = Modifier.size(32.dp))
                    Text("${h.streak}", color = TextMain, fontSize = 30.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
                    Text(" jours", color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 10.dp))
                }
            }
            Column(Modifier.weight(1f).card(14.dp).padding(14.dp)) {
                Text("Meilleure série", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.EmojiEvents, null, tint = Orange, modifier = Modifier.size(32.dp))
                    Text("${h.best}", color = TextMain, fontSize = 30.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
                    Text(" jours", color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 10.dp))
                }
            }
        }
        Row(Modifier.padding(20.dp).fillMaxWidth().card(14.dp).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Taux de réussite", color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("(30 derniers jours)", color = TextMuted, fontSize = 11.sp)
                Spacer(Modifier.height(10.dp))
                val bars = listOf(.4f, .5f, .35f, .6f, .55f, .7f, .5f, .8f, .65f, .9f, .75f, 1f)
                Row(Modifier.height(54.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    bars.forEach { Box(Modifier.width(6.dp).fillMaxHeight(it).clip(RoundedCornerShape(2.dp)).background(Green)) }
                }
            }
            Ring(h.rate / 100f, 84.dp, 9.dp, Green) {
                Text("${h.rate}%", color = TextMain, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
        val rows = listOf(
            Triple(Icons.Filled.Timer, "Objectif", h.objective),
            Triple(Icons.Filled.Repeat, "Fréquence", "Tous les jours"),
            Triple(Icons.Filled.Alarm, "Rappel", "Chaque jour à 18:00"),
            Triple(Icons.Filled.Category, "Catégorie", h.category),
            Triple(Icons.Filled.Speed, "Difficulté", h.difficulty),
            Triple(Icons.Filled.AutoAwesome, "Récompense", "+${h.xp} XP")
        )
        Column(Modifier.padding(horizontal = 20.dp)) {
            rows.forEach { (ic, k, v) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(ic, null, tint = TextMain, modifier = Modifier.size(20.dp))
                    Text(k, color = TextMain, fontSize = 14.sp, modifier = Modifier.padding(start = 14.dp).weight(1f))
                    Text(v, color = TextMuted, fontSize = 14.sp)
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(CardBorder))
            }
        }
        Box(
            Modifier.padding(20.dp).fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp))
                .background(Brush.horizontalGradient(listOf(Accent.copy(alpha = 0.55f), Accent)))
                .border(1.dp, Accent, RoundedCornerShape(14.dp))
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                Text(if (isDone) "Fait ✓ (annuler)" else "Marquer comme fait", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

// ───────────────────────── 4. CALENDRIER ─────────────────────────
@Composable
fun CalendarScreen(habits: List<Habit>, done: Set<Int>) {
    var ym by remember { mutableStateOf(java.time.YearMonth.now()) }
    val today = LocalDate.now()
    val first = ym.atDay(1)
    val offset = first.dayOfWeek.value - 1
    val days = ym.lengthOfMonth()
    val rows = (offset + days + 6) / 7
    val ringColors = listOf(Red, Green, Orange, Violet)
    val monthName = ym.month.getDisplayName(java.time.format.TextStyle.FULL, Locale.FRENCH).replaceFirstChar { it.uppercase() }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Title("Calendrier", Modifier.padding(start = 20.dp, top = 24.dp, bottom = 14.dp))
        Row(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.ChevronLeft, "Mois précédent", tint = TextMain, modifier = Modifier.size(28.dp).clickable { ym = ym.minusMonths(1) })
            Text("$monthName ${ym.year}", color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.ChevronRight, "Mois suivant", tint = TextMain, modifier = Modifier.size(28.dp).clickable { ym = ym.plusMonths(1) })
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
            listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim").forEach {
                Text(it, color = TextMuted, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(8.dp))
        Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (r in 0 until rows) {
                Row(Modifier.fillMaxWidth()) {
                    for (c in 0 until 7) {
                        val day = r * 7 + c - offset + 1
                        Box(Modifier.weight(1f).height(40.dp), contentAlignment = Alignment.Center) {
                            if (day in 1..days) {
                                val date = ym.atDay(day)
                                val isToday = date == today
                                val past = date.isBefore(today)
                                val ratio = if (isToday) 0f else ((day * 37 + 11) % 100) / 100f
                                val col = ringColors[day % 4]
                                Box(
                                    Modifier.size(36.dp).clip(CircleShape)
                                        .background(if (isToday) Blue else Color(0xFF14172B))
                                        .border(1.dp, if (isToday) Blue else CardBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (past) Canvas(Modifier.fillMaxSize()) {
                                        drawArc(col, -90f, 360f * (0.35f + ratio * 0.65f), false, style = Stroke(3.dp.toPx()))
                                    }
                                    Text("$day", color = if (past || isToday) TextMain else TextMuted, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
        Text("Habitudes", color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 20.dp, top = 22.dp, bottom = 8.dp))
        habits.forEach { h ->
            val filled = (h.id * 3 + 4) % 10 + 1
            Row(Modifier.padding(horizontal = 20.dp, vertical = 4.dp).fillMaxWidth().card(12.dp).padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconTile(h.icon, h.color, 30.dp)
                Text(h.name, color = TextMain, fontSize = 14.sp, modifier = Modifier.padding(start = 10.dp).width(80.dp), maxLines = 1)
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                    for (i in 0 until 10) {
                        val on = i < filled || (i == filled && h.id in done)
                        Box(Modifier.size(11.dp).clip(CircleShape).background(if (on) h.color else Color(0xFF1E2138)))
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

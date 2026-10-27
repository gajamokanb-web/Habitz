package com.lesysteme.habittracker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ───────────────────────── 5. STATISTIQUES ─────────────────────────
@Composable
fun StatsScreen(habits: List<Habit>) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Title("Statistiques", Modifier.weight(1f))
            Icon(Icons.Filled.Search, "Rechercher", tint = TextMain, modifier = Modifier.size(24.dp))
        }
        Row(Modifier.padding(horizontal = 20.dp).fillMaxWidth().card(12.dp).padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("30 derniers jours", color = TextMain, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.KeyboardArrowDown, null, tint = TextMuted)
        }
        Column(Modifier.padding(20.dp).fillMaxWidth().card(16.dp).padding(16.dp)) {
            Text("Taux de réussite global", color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("78%", color = TextMain, fontSize = 40.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Icon(Icons.Filled.KeyboardArrowUp, null, tint = Green, modifier = Modifier.size(18.dp))
                Text("+12%", color = Green, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            val pts = listOf(.35f, .42f, .38f, .5f, .45f, .55f, .5f, .6f, .52f, .62f, .58f, .7f, .66f, .75f, .72f, .8f, .78f, .9f)
            Canvas(Modifier.fillMaxWidth().height(90.dp).padding(top = 8.dp)) {
                val w = size.width; val h = size.height
                val path = Path(); val fill = Path()
                pts.forEachIndexed { i, v ->
                    val x = w * i / (pts.size - 1); val y = h * (1 - v)
                    if (i == 0) { path.moveTo(x, y); fill.moveTo(x, h); fill.lineTo(x, y) } else { path.lineTo(x, y); fill.lineTo(x, y) }
                }
                fill.lineTo(w, h); fill.close()
                drawPath(fill, Brush.verticalGradient(listOf(Accent.copy(alpha = 0.45f), Color.Transparent)))
                drawPath(path, Accent, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
            }
        }
        Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Habitudes réalisées", Icons.Filled.TaskAlt, Blue, "187 / 240", Modifier.weight(1f))
            StatCard("Série actuelle", Icons.Filled.Whatshot, Orange, "12 jours", Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Meilleure série", Icons.Filled.EmojiEvents, Orange, "28 jours", Modifier.weight(1f))
            StatCard("Jours parfaits", Icons.Filled.Star, Orange, "8 jours", Modifier.weight(1f))
        }
        Text("Performance par habitude", color = TextMain, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 20.dp, top = 22.dp, bottom = 8.dp))
        habits.forEach { h ->
            Row(Modifier.padding(horizontal = 20.dp, vertical = 6.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconTile(h.icon, h.color, 32.dp)
                Text(h.name, color = TextMain, fontSize = 14.sp, modifier = Modifier.padding(start = 12.dp).width(110.dp), maxLines = 1)
                Bar(h.rate / 100f, Modifier.weight(1f), 8.dp)
                Text("${h.rate}%", color = TextMain, fontSize = 13.sp, textAlign = TextAlign.End, modifier = Modifier.width(44.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun StatCard(label: String, icon: ImageVector, tint: Color, value: String, modifier: Modifier) {
    Column(modifier.card(14.dp).padding(14.dp)) {
        Text(label, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(30.dp))
            Text(value, color = TextMain, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 10.dp))
        }
    }
}

// ───────────────────────── 6. QUÊTES ─────────────────────────
@Composable
fun QuestsScreen(doneCount: Int, onSpecial: () -> Unit) {
    var tab by remember { mutableIntStateOf(0) }
    data class Q(val icon: ImageVector, val color: Color, val title: String, val cur: Int, val max: Int, val xp: Int)
    val quests = when (tab) {
        0 -> listOf(
            Q(Icons.Filled.WbSunny, Orange, "Compléter 4 habitudes aujourd'hui", minOf(doneCount, 4), 4, 25),
            Q(Icons.Filled.SportsMartialArts, Red, "Faire 1 séance de sport", 0, 1, 20),
            Q(Icons.Filled.MenuBook, Orange, "Lire 20 minutes", 0, 1, 15),
            Q(Icons.Filled.WaterDrop, Blue, "Boire 2 L d'eau", 1, 2, 10)
        )
        else -> listOf(
            Q(Icons.Filled.WbSunny, Orange, "Compléter 4 habitudes aujourd'hui", minOf(doneCount, 4), 4, 25),
            Q(Icons.Filled.SportsMartialArts, Red, "Faire 3 séances de sport cette semaine", 2, 3, 100),
            Q(Icons.Filled.MenuBook, Orange, "Lire 5 jours cette semaine", 2, 5, 75),
            Q(Icons.Filled.WaterDrop, Blue, "Boire 14 L d'eau cette semaine", 8, 14, 70)
        )
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("Quêtes", color = TextMain, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 28.dp, bottom = 16.dp))
        PillTabs(listOf("Quotidiennes", "Hebdomadaires", "Spéciales"), tab, { if (it == 2) onSpecial() else tab = it })
        Spacer(Modifier.height(14.dp))
        quests.forEach { q ->
            Row(Modifier.padding(horizontal = 20.dp, vertical = 6.dp).fillMaxWidth().card(14.dp).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                IconTile(q.icon, q.color, 52.dp)
                Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                    Text(q.title, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Bar(q.cur / q.max.toFloat(), Modifier.weight(1f), 6.dp)
                        Text("${q.cur}/${q.max}", color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(start = 8.dp))
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.AutoAwesome, null, tint = Accent, modifier = Modifier.size(26.dp))
                    Text("+${q.xp} XP", color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ───────────────────────── 7. PORTAILS ─────────────────────────
@Composable
fun PortalsScreen() {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("Portails", color = TextMain, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 28.dp, bottom = 16.dp))
        PortalCard(R.drawable.portal_discipline, Icons.Filled.Shield, Red, "Portail de la Discipline", "30 jours", "+500 XP")
        Spacer(Modifier.height(14.dp))
        PortalCard(R.drawable.portal_savoir, Icons.Filled.Psychology, Blue, "Portail du Savoir", "30 jours", "+500 XP")
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun PortalCard(img: Int, icon: ImageVector, tint: Color, name: String, duration: String, xp: String) {
    Column(Modifier.padding(horizontal = 20.dp).fillMaxWidth().card(16.dp, Accent.copy(alpha = 0.5f))) {
        Image(painterResource(img), null, Modifier.fillMaxWidth().height(120.dp), contentScale = ContentScale.Crop)
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconTile(icon, tint, 44.dp)
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(name, color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(duration, color = TextMuted, fontSize = 12.sp)
            }
            Text(xp, color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ───────────────────────── 8. PROFIL ─────────────────────────
@Composable
fun ProfileScreen(totalXp: Int, onAchievements: () -> Unit) {
    val level = levelOf(totalXp); val cur = xpInLevel(totalXp)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(170.dp)) {
            Image(painterResource(R.drawable.hero_profile), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.matchParentSize().background(Brush.verticalGradient(0.5f to Color.Transparent, 1f to BgTop)))
            Text("Profil", color = TextMain, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 20.dp, top = 24.dp))
        }
        Text("Niveau $level", color = TextMain, fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text("Rang ${rankOf(level)} · Chasseur confirmé", color = TextMuted, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 2.dp))
        Bar(cur / 1200f, Modifier.padding(horizontal = 20.dp, vertical = 10.dp).fillMaxWidth(), 10.dp)
        Text("$cur / 1 200 XP", color = TextMuted, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(14.dp))
        data class A(val icon: ImageVector, val color: Color, val name: String, val v: Int)
        listOf(
            A(Icons.Filled.FitnessCenter, Red, "Force", 68),
            A(Icons.Filled.Spa, Green, "Vitalité", 74),
            A(Icons.Filled.Psychology, Blue, "Intelligence", 52),
            A(Icons.Filled.Shield, Violet, "Discipline", 81),
            A(Icons.Filled.DirectionsRun, Orange, "Agilité", 46)
        ).forEach { a ->
            Row(Modifier.padding(horizontal = 20.dp, vertical = 7.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconTile(a.icon, a.color, 34.dp)
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(a.name, color = TextMain, fontSize = 14.sp)
                    Spacer(Modifier.height(5.dp))
                    Bar(a.v / 100f, Modifier.fillMaxWidth(), 6.dp, a.color)
                }
                Text("${a.v}", color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Row(Modifier.padding(20.dp).fillMaxWidth().card(14.dp).clickable(onClick = onAchievements).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.EmojiEvents, null, tint = Orange, modifier = Modifier.size(26.dp))
            Text("Succès", color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f).padding(start = 12.dp))
            Icon(Icons.Filled.ChevronRight, null, tint = TextMuted)
        }
    }
}

// ───────────────────────── 9. SUCCÈS ─────────────────────────
@Composable
fun AchievementsScreen() {
    var tab by remember { mutableIntStateOf(0) }
    data class B(val img: Int, val name: String, val desc: String, val unlocked: Boolean)
    val all = listOf(
        B(R.drawable.badge_1, "Premiers pas", "Termine ta 1ère journée", true),
        B(R.drawable.badge_2, "Série de 7 jours", "7 jours consécutifs", true),
        B(R.drawable.badge_3, "Lecteur assidu", "Lire 50 fois", true),
        B(R.drawable.badge_4, "Guerrier", "100 séances de sport", false),
        B(R.drawable.badge_5, "Hydraté", "Boire 100 L d'eau", false),
        B(R.drawable.badge_6, "Méditation", "50 sessions", false)
    )
    val shown = when (tab) { 1 -> all.filter { it.unlocked }; 2 -> all.filter { !it.unlocked }; else -> all }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("Succès", color = TextMain, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 28.dp, bottom = 16.dp))
        PillTabs(listOf("Tous", "Débloqués", "En cours"), tab, { tab = it })
        Spacer(Modifier.height(14.dp))
        shown.chunked(3).forEach { row ->
            Row(Modifier.padding(horizontal = 20.dp, vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { b ->
                    Column(Modifier.weight(1f).card(14.dp).padding(horizontal = 6.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(painterResource(b.img), null, Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)))
                        Text(b.name, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
                        Text(b.desc, color = TextMuted, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 2.dp))
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

// ───────────────────────── 10. APPARENCE ─────────────────────────
@Composable
fun AppearanceScreen(onBack: () -> Unit) {
    var theme by remember { mutableIntStateOf(0) }
    var wall by remember { mutableIntStateOf(0) }
    val themes = listOf(R.drawable.theme_1 to "Sombre", R.drawable.theme_2 to "Clair", R.drawable.theme_3 to "Arène", R.drawable.theme_4 to "Forêt")
    val walls = listOf(R.drawable.wall_1, R.drawable.wall_2, R.drawable.wall_3, R.drawable.wall_4)
    val accents = listOf(Color(0xFF8B5CF6), Color(0xFFEF4444), Color(0xFF3B82F6), Color(0xFF22C55E), Color(0xFFF59E0B), Color(0xFFEC4899))
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.ArrowBack, "Retour", tint = TextMain, modifier = Modifier.align(Alignment.CenterStart).padding(start = 20.dp).size(24.dp).clickable(onClick = onBack))
            Text("Apparence", color = TextMain, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        }
        Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            themes.forEachIndexed { i, (img, label) ->
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painterResource(img), null,
                        Modifier.fillMaxWidth().height(70.dp).clip(RoundedCornerShape(10.dp))
                            .border(if (i == theme) 2.dp else 1.dp, if (i == theme) Accent else CardBorder, RoundedCornerShape(10.dp))
                            .clickable { theme = i },
                        contentScale = ContentScale.Crop
                    )
                    Text(label, color = if (i == theme) TextMain else TextMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
        Text("Couleur d'accent", color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 12.dp))
        Row(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            accents.forEach { c ->
                val sel = c == Accent
                Box(
                    Modifier.size(if (sel) 50.dp else 40.dp).clip(CircleShape)
                        .then(if (sel) Modifier.border(2.dp, c, CircleShape).padding(5.dp) else Modifier)
                        .clip(CircleShape).background(c).clickable { Accent = c },
                    contentAlignment = Alignment.Center
                ) { if (sel) Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
            }
        }
        Text("Fond d'écran", color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 12.dp))
        Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            walls.forEachIndexed { i, img ->
                Image(
                    painterResource(img), null,
                    Modifier.weight(1f).height(110.dp).clip(RoundedCornerShape(10.dp))
                        .border(if (i == wall) 2.dp else 1.dp, if (i == wall) Accent else CardBorder, RoundedCornerShape(10.dp))
                        .clickable { wall = i },
                    contentScale = ContentScale.Crop
                )
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

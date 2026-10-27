package com.lesysteme.habittracker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.drawscope.Stroke

val BgTop = Color(0xFF0E0B22)
val BgBot = Color(0xFF070812)
val CardBg = Color(0xFF111426)
val CardBorder = Color(0xFF242842)
val TextMain = Color(0xFFF4F2FF)
val TextMuted = Color(0xFF9AA0B8)
val Green = Color(0xFF34D399)
val Red = Color(0xFFEF4466)
val Orange = Color(0xFFFFB020)
val Blue = Color(0xFF3B9CFF)
val Violet = Color(0xFF9B6DFF)
val Indigo = Color(0xFF7D7BFF)

var Accent by mutableStateOf(Color(0xFF8B5CF6))

data class Habit(
    val id: Int, val name: String, val sub: String, val icon: ImageVector,
    val color: Color, val category: String, val streak: Int, val best: Int,
    val xp: Int, val rate: Int, val difficulty: String, val objective: String
)

val seedHabits = listOf(
    Habit(1, "Sport", "30 min / jour", Icons.Filled.SportsMartialArts, Red, "Santé", 12, 28, 20, 83, "Moyenne", "30 minutes"),
    Habit(2, "Lire", "20 min / jour", Icons.Filled.MenuBook, Orange, "Productivité", 28, 28, 15, 90, "Facile", "20 minutes"),
    Habit(3, "Boire de l'eau", "2 L / jour", Icons.Filled.WaterDrop, Blue, "Santé", 15, 22, 10, 76, "Facile", "2 litres"),
    Habit(4, "Méditer", "10 min / jour", Icons.Filled.SelfImprovement, Violet, "Santé", 8, 19, 15, 60, "Moyenne", "10 minutes"),
    Habit(5, "Apprendre", "1 session / jour", Icons.Filled.School, Indigo, "Productivité", 21, 21, 20, 53, "Difficile", "1 session"),
    Habit(6, "Pas du jour", "10 000 pas", Icons.Filled.DirectionsRun, Green, "Santé", 5, 14, 15, 72, "Moyenne", "10 000 pas"),
    Habit(7, "Saxo / Piano", "30 min / jour", Icons.Filled.MusicNote, Orange, "Perso", 9, 16, 20, 68, "Moyenne", "30 minutes")
)

fun Modifier.card(radius: Dp = 16.dp, border: Color = CardBorder): Modifier =
    this.clip(RoundedCornerShape(radius))
        .background(CardBg)
        .border(1.dp, border, RoundedCornerShape(radius))

@Composable
fun ScreenBackground(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgTop, BgBot, BgBot))),
        content = content
    )
}

@Composable
fun IconTile(icon: ImageVector, color: Color, size: Dp = 46.dp) {
    Box(
        Modifier.size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.16f))
            .border(1.5.dp, color, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(size * 0.54f))
    }
}

@Composable
fun SquareButton(icon: ImageVector, desc: String, onClick: () -> Unit) {
    Box(
        Modifier.size(38.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(Color(0x33FFFFFF).copy(alpha = 0.06f))
            .border(1.dp, Accent.copy(alpha = 0.6f), RoundedCornerShape(11.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icon, desc, tint = TextMain, modifier = Modifier.size(20.dp)) }
}

@Composable
fun PillTabs(labels: List<String>, selected: Int, onSel: (Int) -> Unit, equal: Boolean = true, modifier: Modifier = Modifier) {
    val rowMod = if (equal) modifier.fillMaxWidth() else modifier.horizontalScroll(rememberScrollState())
    Row(rowMod.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        labels.forEachIndexed { i, l ->
            val sel = i == selected
            val shape = RoundedCornerShape(12.dp)
            val m = (if (equal) Modifier.weight(1f) else Modifier)
                .height(38.dp)
                .clip(shape)
                .background(if (sel) Accent.copy(alpha = 0.22f) else CardBg)
                .border(1.dp, if (sel) Accent else CardBorder, shape)
                .clickable { onSel(i) }
                .padding(horizontal = if (equal) 4.dp else 18.dp)
            Box(m, contentAlignment = Alignment.Center) {
                Text(l, color = if (sel) TextMain else TextMuted, fontSize = 13.sp, fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal, maxLines = 1)
            }
        }
    }
}

@Composable
fun Bar(frac: Float, modifier: Modifier = Modifier, height: Dp = 8.dp, color: Color? = null) {
    val c = color ?: Accent
    Box(modifier.height(height).clip(CircleShape).background(Color(0xFF1B1E33))) {
        Box(
            Modifier.fillMaxHeight().fillMaxWidth(frac.coerceIn(0f, 1f)).clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(c.copy(alpha = 0.75f), c)))
        )
    }
}

@Composable
fun Ring(progress: Float, size: Dp, stroke: Dp, color: Color, content: @Composable BoxScope.() -> Unit) {
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val s = stroke.toPx()
            val d = Size(this.size.width - s, this.size.height - s)
            drawArc(Color(0xFF1E2138), 0f, 360f, false, Offset(s / 2, s / 2), d, style = Stroke(s))
            drawArc(color, -90f, 360f * progress.coerceIn(0f, 1f), false, Offset(s / 2, s / 2), d, style = Stroke(s, cap = StrokeCap.Round))
        }
        content()
    }
}

@Composable
fun CheckCircle(done: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(28.dp).clip(CircleShape)
            .then(if (done) Modifier.background(Accent) else Modifier.border(1.5.dp, Color(0xFF5A5F7C), CircleShape))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { if (done) Icon(Icons.Filled.Check, "Fait", tint = Color.White, modifier = Modifier.size(18.dp)) }
}

@Composable
fun Title(text: String, modifier: Modifier = Modifier) {
    Text(text, color = TextMain, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = modifier)
}

@Composable
fun BottomBar(selected: Int, onSel: (Int) -> Unit) {
    val items = listOf(
        "Accueil" to Icons.Filled.Home,
        "Habitudes" to Icons.Filled.GridView,
        "Quêtes" to Icons.Filled.Shield,
        "Statistiques" to Icons.Filled.BarChart,
        "Profil" to Icons.Filled.Person
    )
    Column {
        Box(Modifier.fillMaxWidth().height(1.dp).background(CardBorder))
        Row(Modifier.fillMaxWidth().background(Color(0xFF090A15)).padding(top = 8.dp, bottom = 10.dp)) {
            items.forEachIndexed { i, (label, icon) ->
                val sel = i == selected
                Column(
                    Modifier.weight(1f).clickable { onSel(i) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(icon, label, tint = if (sel) Accent else TextMuted, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.height(3.dp))
                    Text(label, fontSize = 10.sp, color = if (sel) Accent else TextMuted, fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal, maxLines = 1)
                }
            }
        }
    }
}

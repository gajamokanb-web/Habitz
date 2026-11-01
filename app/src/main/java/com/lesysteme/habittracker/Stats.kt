package com.lesysteme.habittracker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.math.sin

private val PDELTA = mapOf(7 to 12, 30 to 6, 90 to 14, 365 to 31)
private val POFF = mapOf(7 to 2, 30 to 0, 90 to -3, 365 to -8)
private val PERIODS = listOf(7, 30, 90, 365)

class Series(val vals: List<Float>, val labels: List<String>)

private fun seriesFor(st: AppState, p: Int, ratio: Float): Series {
    val vals = ArrayList<Float>()
    val labels = ArrayList<String>()
    if (p == 7 || p == 30) {
        for (d in -(p - 1)..0) {
            vals.add(if (d < 0) HEAT[33 + d] / 4f * 100f else ratio * 100f)
            labels.add(if (p == 7) DOW[st.idxOf(d)] else st.dateAt(d).dayOfMonth.toString())
        }
    } else {
        val n = if (p == 90) 13 else 12
        val step = if (p == 90) 7 else 30
        for (i in 0 until n) {
            val base = if (p == 90) 60f + 20f * i / (n - 1) else 46f + 34f * i / (n - 1)
            val phase = if (p == 90) 0.0 else 1.0
            val v = if (FRESH) 0 else (base + 8.0 * sin(i * 1.7 + phase)).roundToInt().coerceIn(20, 100)
            vals.add(v.toFloat())
            val dt = st.dateAt(-step * (n - 1 - i))
            labels.add(if (p == 90) "${dt.dayOfMonth} ${MSHORT[dt.monthValue - 1]}" else MSHORT[dt.monthValue - 1])
        }
    }
    return Series(vals, labels)
}

@Composable
fun StatsScreen(st: AppState) {
    val due = st.dueToday()
    val doneN = due.count { st.done.contains(it.id) }
    val ratio = if (due.isEmpty()) 0f else doneN.toFloat() / due.size
    val sr = seriesFor(st, st.period, ratio)
    val avg = sr.vals.average().roundToInt()
    val streak = (if (FRESH) 0 else 20) + (if (doneN > 0) 1 else 0)
    val lv = levelInfo(st.total())

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        /* Bannière */
        Box(Modifier.fillMaxWidth().height(150.dp)) {
            Image(
                painterResource(R.drawable.hero), null, Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop, alignment = BiasAlignment(0.6f, -0.3f),
            )
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xE607041A), Color(0x7307041A), Color.Transparent))))
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0x1A07041A), C.Bg.copy(alpha = 0.9f)))))
            Column(Modifier.align(Alignment.BottomStart).padding(horizontal = 16.dp, vertical = 14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.BarChart, null, tint = C.Violet, modifier = Modifier.size(34.dp))
                    Spacer(Modifier.width(10.dp))
                    T("Stats", 40, weight = FontWeight.SemiBold)
                }
                T("Analyse de tes habitudes", 19, C.Violet, italic = true)
            }
        }

        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Segmented(listOf("7 jours", "30 jours", "3 mois", "1 an"), PERIODS.indexOf(st.period)) { st.period = PERIODS[it] }

            /* Anneau + niveau */
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Panel(Modifier.weight(1f), cut = 10f, padding = 12.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Ring(avg / 100f, Modifier.size(96.dp), stroke = 7.dp) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                T("$avg%", 24, weight = FontWeight.SemiBold)
                                T("Complétion", 10, C.Muted)
                                if (!FRESH) T("▲ +${PDELTA[st.period]} pts", 10, C.Green)
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            MiniKpi(Icons.Filled.LocalFireDepartment, C.Ember, "Streak actuel", "$streak jours")
                            MiniKpi(Icons.Filled.Star, C.Violet, "Meilleur streak", if (FRESH) "0 jour" else "34 jours")
                        }
                    }
                }
                Panel(Modifier.width(92.dp), cut = 10f, padding = 8.dp) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.SportsMartialArts, null, tint = C.Violet, modifier = Modifier.size(22.dp))
                        T("Niveau", 13, C.Muted)
                        T(lv.l.toString(), 40, weight = FontWeight.Bold)
                    }
                }
            }

            /* Courbe */
            Panel {
                T("Évolution de la complétion", 21, weight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                LineChart(sr)
            }

            /* Catégories + répartition côte à côte */
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Panel(Modifier.weight(1f), padding = 12.dp) {
                    T("Par catégorie", 19, weight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    val used = CATS.filter { c -> st.habits.any { it.cat == c.key } }
                    if (used.isEmpty()) T("Aucune quête.", 14, C.Muted)
                    used.forEach { c ->
                        val v = if (FRESH) 0 else ((RATES[c.key] ?: 75) + (POFF[st.period] ?: 0)).coerceIn(0, 100)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CatIcon(c.key, 24.dp)
                            Spacer(Modifier.width(6.dp))
                            T(c.name.substringBefore(" "), 12, modifier = Modifier.weight(1f), maxLines = 1)
                            T("$v%", 12, C.Muted)
                        }
                        Spacer(Modifier.height(3.dp))
                        BarFill(v / 100f, c.color, height = 7.dp)
                        Spacer(Modifier.height(10.dp))
                    }
                }
                Panel(Modifier.weight(1f), padding = 12.dp) {
                    T("Répartition", 19, weight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    Donut(st)
                }
            }

            /* Succès récents */
            Panel {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    T("Succès récents", 21, weight = FontWeight.SemiBold)
                    Box(
                        Modifier.clip(RoundedCornerShape(10.dp))
                            .clickable(role = Role.Button) { st.tab = 3; st.scrollToAch = true }
                            .padding(horizontal = 8.dp, vertical = 12.dp)
                    ) { T("Tout voir ›", 14, C.Violet) }
                }
                Spacer(Modifier.height(8.dp))
                val rec = st.achs.filter { it.ok }.sortedBy { it.ago ?: 0 }.take(4)
                if (rec.isEmpty()) T("Aucun succès débloqué.", 14, C.Muted)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    rec.forEach { a ->
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            AchArt(a.art, locked = false, width = 62.dp, height = 56.dp)
                            Spacer(Modifier.height(6.dp))
                            T(a.n, 14, align = TextAlign.Center, weight = FontWeight.SemiBold, maxLines = 2)
                            T(agoText(a.ago ?: 0), 12, C.Muted, align = TextAlign.Center, maxLines = 1)
                        }
                    }
                }
            }

            /* Calendrier de chaleur */
            Panel {
                T("Calendrier des 5 dernières semaines", 21, weight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                HeatGrid(ratio)
            }

            GhostButton("Réinitialiser la démo") { st.resetDemo() }
        }
    }
}

fun agoText(n: Int): String = when (n) {
    0 -> "Aujourd’hui"
    1 -> "Hier"
    else -> "Il y a $n j"
}

@Composable
private fun MiniKpi(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(6.dp))
        Column {
            T(label, 11, C.Muted, maxLines = 1)
            T(value, 17, weight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}

@Composable
private fun LineChart(sr: Series) {
    val n = sr.vals.size
    val labelCount = if (n <= 7) n else 6
    val picks = if (n <= 7) (0 until n).toList() else (0 until labelCount).map { (it * (n - 1).toFloat() / (labelCount - 1)).roundToInt() }
    Column {
        Row(Modifier.fillMaxWidth().height(150.dp)) {
            Column(Modifier.width(38.dp).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.End) {
                listOf(100, 75, 50, 25, 0).forEach { T("$it%", 11, C.Muted) }
            }
            Spacer(Modifier.width(6.dp))
            Canvas(Modifier.weight(1f).fillMaxHeight()) {
                val w = size.width
                val h = size.height
                for (k in 0..4) {
                    val y = h * k / 4f
                    drawLine(Color(0x24A98BFF), Offset(0f, y), Offset(w, y), strokeWidth = 1.dp.toPx())
                }
                val pts = sr.vals.mapIndexed { i, v ->
                    Offset(if (n > 1) w * i / (n - 1) else 0f, h * (1f - v / 100f))
                }
                val line = Path().apply { pts.forEachIndexed { i, p -> if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y) } }
                val area = Path().apply {
                    moveTo(0f, h)
                    pts.forEach { lineTo(it.x, it.y) }
                    lineTo(w, h)
                    close()
                }
                drawPath(area, Brush.verticalGradient(listOf(Color(0x8C8B6CFF), Color.Transparent)))
                drawPath(
                    line, Brush.horizontalGradient(listOf(Color(0xFFCBB8FF), Color(0xFFB9A4FF))),
                    style = Stroke(2.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
                val dots = if (n <= 14) pts else listOf(pts.last())
                dots.forEach {
                    drawCircle(Color(0xFFF4EEFF), 3.6.dp.toPx(), it)
                    drawCircle(Color(0xFF8B6CFF), 3.6.dp.toPx(), it, style = Stroke(1.5.dp.toPx()))
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth().padding(start = 44.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            picks.forEach { T(sr.labels[it], 11, C.Muted, maxLines = 1) }
        }
    }
}

@Composable
private fun Donut(st: AppState) {
    val cnt = LinkedHashMap<String, Int>()
    var tot = 0
    st.habits.forEach { h ->
        val o = when (h.freq.type) {
            "daily" -> 7
            "days" -> h.freq.days.size
            else -> h.freq.times
        }
        cnt[h.cat] = (cnt[h.cat] ?: 0) + o
        tot += o
    }
    val sorted = cnt.entries.sortedByDescending { it.value }
    val parts = ArrayList<Triple<String, Color, Int>>()
    sorted.take(4).forEach { parts.add(Triple(catOf(it.key).name.substringBefore(" "), catOf(it.key).color, it.value)) }
    val rest = sorted.drop(4).sumOf { it.value }
    if (rest > 0) parts.add(Triple("Autre", Color(0xFFFFB84D), rest))

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(112.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val sw = 13.dp.toPx()
                val sz = Size(size.width - sw, size.height - sw)
                val tl = Offset(sw / 2, sw / 2)
                drawArc(Color(0x1FA98BFF), 0f, 360f, false, tl, sz, style = Stroke(sw))
                var start = -90f
                if (tot > 0) parts.forEach { (_, col, v) ->
                    val sweep = 360f * v / tot
                    drawArc(col, start, (sweep - 3f).coerceAtLeast(1f), false, tl, sz, style = Stroke(sw))
                    start += sweep
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                T(tot.toString(), 26, weight = FontWeight.SemiBold)
                T("quêtes / sem.", 10, C.Muted)
            }
        }
        Spacer(Modifier.height(12.dp))
        parts.forEach { (name, col, v) ->
            Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(col))
                Spacer(Modifier.width(6.dp))
                T(name, 13, modifier = Modifier.weight(1f), maxLines = 1)
                T((if (tot > 0) (100f * v / tot).roundToInt() else 0).toString() + "%", 13, C.Muted)
            }
        }
    }
}

private val HEAT_COLORS = listOf(Color(0x12A98BFF), Color(0xFF2D1A70), Color(0xFF5B34C8), Color(0xFF9A6BFF), Color(0xFFE0D2FF))

@Composable
private fun HeatGrid(ratio: Float) {
    Row(Modifier.fillMaxWidth()) {
        "LMMJVSD".forEach { T(it.toString(), 12, C.Muted, Modifier.weight(1f), align = TextAlign.Center) }
    }
    Spacer(Modifier.height(4.dp))
    for (r in 0 until 5) {
        Row(Modifier.fillMaxWidth()) {
            for (c in 0 until 7) {
                val i = r * 7 + c
                val shape = RoundedCornerShape(6.dp)
                val m = Modifier.weight(1f).aspectRatio(1f).padding(3.dp).clip(shape)
                when {
                    i < 33 -> Box(m.background(HEAT_COLORS[HEAT[i]]))
                    i == 33 -> Box(m.background(HEAT_COLORS[ceil(4f * ratio).toInt().coerceIn(0, 4)]).border(2.dp, C.Text, shape))
                    else -> Box(m.border(1.5.dp, C.Violet.copy(alpha = 0.25f), shape))
                }
            }
        }
    }
    Spacer(Modifier.height(10.dp))
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        T("Vide", 12, C.Muted)
        HEAT_COLORS.forEach { Box(Modifier.size(16.dp).clip(RoundedCornerShape(4.dp)).background(it)) }
        T("Journée parfaite", 12, C.Muted)
    }
}

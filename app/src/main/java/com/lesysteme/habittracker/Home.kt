package com.lesysteme.habittracker

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

private fun dayLabel(st: AppState, d: Int): String {
    val dt = st.dateAt(d)
    val w = FULL[st.idxOf(d)]
    return w.replaceFirstChar { it.uppercase() } + " " + dt.dayOfMonth + " " + MSHORT[dt.monthValue - 1]
}

@Composable
fun HomeScreen(st: AppState) {
    var actionFor by remember { mutableStateOf<Habit?>(null) }
    val hs = st.dayList(st.selDiff)
    val due = st.dueToday()
    val doneN = due.count { st.done.contains(it.id) }
    val ratio = if (due.isEmpty()) 0f else doneN.toFloat() / due.size
    val past = HEAT.takeLast(5)
    val pct = (100f * (past.sum() / 4f + ratio) / 6f).roundToInt()
    val perfect = past.count { it == 4 } + (if (due.isNotEmpty() && doneN == due.size) 1 else 0)
    val streak = (if (FRESH) 0 else 20) + (if (doneN > 0) 1 else 0)
    val lv = levelInfo(st.total())
    val dayDone = hs.count { st.isDoneOn(it, st.selDiff) }
    val hidden = if (st.selDiff >= 0) st.habits.filter { st.hidden(it, st.selDiff) } else emptyList()

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { DateBar(st) }
        item { Banner(st, lv) }
        item {
            Panel(Modifier.padding(horizontal = 16.dp), padding = 16.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Ring(ratioOrPct(pct), Modifier.size(112.dp), stroke = 8.dp) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            T("$pct%", 32, weight = FontWeight.SemiBold)
                            T("Cette semaine", 12, C.Muted)
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Box(Modifier.width(1.dp).height(90.dp).background(C.Line))
                    Spacer(Modifier.width(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Kpi(Icons.Filled.LocalFireDepartment, C.Ember, "Streak global", "$streak jours")
                        Kpi(Icons.Filled.Star, C.Violet, "Journées parfaites", "$perfect / 7")
                    }
                }
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                T(if (st.selDiff == 0) "Quêtes du jour" else dayLabel(st, st.selDiff), 26, weight = FontWeight.SemiBold)
                T("$dayDone / ${hs.size}", 15, C.Violet)
            }
        }
        if (st.selDiff != 0) {
            item {
                T(
                    if (st.selDiff < 0) "Jour passé : tu peux corriger tes validations (sans XP)."
                    else "Jour à venir : tu peux valider en avance (sans XP).",
                    14, C.Muted, Modifier.padding(horizontal = 18.dp),
                )
            }
        }
        if (hs.isEmpty()) {
            item {
                Panel(Modifier.padding(horizontal = 16.dp)) {
                    T("Aucune quête prévue ce jour.", 16, C.Muted)
                }
            }
        }
        items(CATS.filter { c -> hs.any { it.cat == c.key } }, key = { it.key }) { cat ->
            CatAccordion(st, cat, hs.filter { it.cat == cat.key }) { actionFor = it }
        }
        if (hidden.isNotEmpty()) {
            item {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    T("Ignorées ou décalées", 21, C.Muted, weight = FontWeight.SemiBold)
                    hidden.forEach { h ->
                        val mv = st.moves["${h.id}|${st.selDiff}"]
                        val has = st.moves.containsKey("${h.id}|${st.selDiff}")
                        val label = when {
                            !has -> "Ignorée ce jour"
                            mv == null -> "Retirée"
                            else -> "Décalée au " + dayLabel(st, mv).lowercase()
                        }
                        Row(
                            Modifier.fillMaxWidth().chamfer(10f).padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                T(h.t, 18, C.Muted, weight = FontWeight.SemiBold)
                                T(label, 13, C.Muted)
                            }
                            GhostButton("Rétablir") { st.restore(h) }
                        }
                    }
                }
            }
        }
    }

    actionFor?.let { h -> ActionDialog(st, h) { actionFor = null } }
}

private fun ratioOrPct(pct: Int): Float = pct / 100f

@Composable
private fun Kpi(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            T(label, 12, C.Muted)
            T(value, 22, weight = FontWeight.SemiBold)
        }
    }
}

/* ───────────── Barre de dates + calendrier ───────────── */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DateBar(st: AppState) {
    val strip = rememberLazyListState(initialFirstVisibleItemIndex = (st.selDiff + RANGE - 3).coerceIn(0, 2 * RANGE))
    val scope = rememberCoroutineScope()
    var showCal by remember { mutableStateOf(false) }
    val centerDiff by remember { derivedStateOf { strip.firstVisibleItemIndex + 3 - RANGE } }
    val dt = st.dateAt(centerDiff.coerceIn(-RANGE, RANGE))

    Column(Modifier.padding(horizontal = 16.dp).padding(top = 10.dp)) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconBtn(Icons.Filled.ChevronLeft, "Semaine précédente", tint = C.Violet) {
                scope.launch { strip.animateScrollToItem((strip.firstVisibleItemIndex - 7).coerceAtLeast(0)) }
            }
            Row(
                Modifier.clip(RoundedCornerShape(12.dp))
                    .clickable(role = Role.Button, onClickLabel = "Ouvrir le calendrier") { showCal = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                T(MONTHS[dt.monthValue - 1] + " " + dt.year, 26, weight = FontWeight.SemiBold)
                Icon(Icons.Filled.ExpandMore, null, tint = C.Violet, modifier = Modifier.size(20.dp))
            }
            IconBtn(Icons.Filled.ChevronRight, "Semaine suivante", tint = C.Violet) {
                scope.launch { strip.animateScrollToItem((strip.firstVisibleItemIndex + 7).coerceAtMost(2 * RANGE)) }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(C.Line))
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val w = maxWidth / 7
            LazyRow(state = strip, flingBehavior = rememberSnapFlingBehavior(strip)) {
                items(2 * RANGE + 1) { i ->
                    val d = i - RANGE
                    DayCell(st, d, st.selDiff == d, Modifier.width(w)) { st.selDiff = d }
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(C.Line))
    }

    if (showCal) {
        CalendarDialog(
            st,
            onPick = { d ->
                st.selDiff = d
                scope.launch { strip.animateScrollToItem((d + RANGE - 3).coerceIn(0, 2 * RANGE)) }
                showCal = false
            },
            onDismiss = { showCal = false },
        )
    }
}

@Composable
private fun DayCell(st: AppState, d: Int, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val dt = st.dateAt(d)
    Column(
        modifier
            .heightIn(min = 68.dp)
            .padding(2.dp)
            .then(if (selected) Modifier.chamfer(9f) else Modifier)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        T(DOW[st.idxOf(d)], 14, if (selected) C.Violet else C.Muted)
        T(
            dt.dayOfMonth.toString(), 28,
            if (d < 0) Color(0xFFCDC3EC) else C.Text,
            weight = FontWeight.SemiBold,
        )
        if (d == 0) Box(Modifier.size(5.dp).clip(CircleShape).background(C.Violet))
    }
}

@Composable
fun CalendarDialog(st: AppState, onPick: (Int) -> Unit, onDismiss: () -> Unit) {
    var ym by remember { mutableStateOf(YearMonth.from(st.dateAt(st.selDiff))) }
    val today = LocalDate.now()
    fun diffOf(date: LocalDate) = ChronoUnit.DAYS.between(today, date).toInt()
    val lead = ym.atDay(1).dayOfWeek.value - 1
    val days = ym.lengthOfMonth()
    val rows = (lead + days + 6) / 7

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Panel(Modifier.padding(horizontal = 24.dp), cut = 18f, padding = 16.dp) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconBtn(Icons.Filled.ChevronLeft, "Mois précédent", tint = C.Violet) {
                    if (diffOf(ym.atDay(1).minusDays(1)) >= -RANGE) ym = ym.minusMonths(1)
                }
                T(MONTHS[ym.monthValue - 1] + " " + ym.year, 24, align = TextAlign.Center, weight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                IconBtn(Icons.Filled.ChevronRight, "Mois suivant", tint = C.Violet) {
                    if (diffOf(ym.plusMonths(1).atDay(1)) <= RANGE) ym = ym.plusMonths(1)
                }
            }
            Row(Modifier.fillMaxWidth()) {
                DOW.forEach { T(it.take(1), 13, C.Muted, Modifier.weight(1f), align = TextAlign.Center) }
            }
            for (r in 0 until rows) {
                Row(Modifier.fillMaxWidth()) {
                    for (c in 0 until 7) {
                        val n = r * 7 + c - lead + 1
                        if (n < 1 || n > days) {
                            Spacer(Modifier.weight(1f).height(44.dp))
                        } else {
                            val date = ym.atDay(n)
                            val d = diffOf(date)
                            val enabled = d in -RANGE..RANGE
                            val sel = d == st.selDiff
                            Box(
                                Modifier.weight(1f).height(44.dp).padding(2.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .then(
                                        if (sel) Modifier.background(Brush.linearGradient(listOf(C.Light, C.Violet2)))
                                        else if (d == 0) Modifier.border(1.5.dp, C.Violet, RoundedCornerShape(12.dp))
                                        else Modifier
                                    )
                                    .clickable(enabled = enabled, role = Role.Button) { onPick(d) },
                                contentAlignment = Alignment.Center,
                            ) {
                                T(
                                    n.toString(), 17,
                                    if (sel) C.OnAccent else if (enabled) C.Text else C.Muted.copy(alpha = 0.3f),
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                GhostButton("Aujourd’hui") { onPick(0) }
                GhostButton("Fermer") { onDismiss() }
            }
        }
    }
}

/* ───────────── Bannière niveau ───────────── */

@Composable
fun Banner(st: AppState, lv: Lv) {
    Box(Modifier.fillMaxWidth().height(147.dp)) {
        Image(
            painter = painterResource(R.drawable.hero),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = BiasAlignment(0.6f, -0.45f),
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(listOf(Color(0xE607041A), Color(0x7307041A), Color.Transparent))
            )
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color(0x1A07041A), C.Bg.copy(alpha = 0.9f)))
            )
        )
        Row(
            Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Crest(lv.l, lv.r.toFloat() / lv.n, 88.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                T(if (st.profileSaved) st.profileName else "Aventurier", 28, weight = FontWeight.SemiBold, maxLines = 1)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    T("XP ${fmt(lv.r)} / ${fmt(lv.n)}", 13, Color(0xFFC9B8FF), maxLines = 1)
                    T("encore ${fmt(lv.n - lv.r)} XP", 13, C.Muted, maxLines = 1)
                }
                Spacer(Modifier.height(5.dp))
                BarFill(lv.r.toFloat() / lv.n, C.Violet2, height = 10.dp)
            }
        }
    }
}

fun fmt(n: Int): String = String.format(java.util.Locale.FRANCE, "%,d", n)

/* ───────────── Quêtes par catégorie ───────────── */

@Composable
private fun CatAccordion(st: AppState, cat: Cat, items: List<Habit>, onMore: (Habit) -> Unit) {
    val open = st.openCats.contains(cat.key)
    val dn = items.count { st.isDoneOn(it, st.selDiff) }
    Panel(Modifier.padding(horizontal = 16.dp), padding = 0.dp) {
        Row(
            Modifier.fillMaxWidth()
                .clickable(role = Role.Button, onClickLabel = if (open) "Replier" else "Déplier") { st.toggleCat(cat.key) }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CatIcon(cat.key, 48.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    T(cat.name, 19, weight = FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines = 1)
                    T("$dn / ${items.size}", 15, C.Muted)
                }
                Spacer(Modifier.height(6.dp))
                BarFill(dn.toFloat() / items.size, cat.color)
            }
            Icon(if (open) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null, tint = C.Violet)
        }
        if (open) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(C.Line))
            Column(Modifier.padding(horizontal = 16.dp)) {
                items.forEach { h -> QuestRow(st, h, onMore) }
            }
        }
    }
}

@Composable
private fun QuestRow(st: AppState, h: Habit, onMore: (Habit) -> Unit) {
    val dn = st.isDoneOn(h, st.selDiff)
    var sub = h.s
    if (h.freq.type == "weekly") {
        sub += " · " + (h.wk + (if (dn && st.selDiff == 0) 1 else 0)) + "/" + h.freq.times + " cette semaine"
    }
    val mk = st.movedInKey(h, st.selDiff)
    if (mk != null) sub += " · décalée depuis " + dayLabel(st, mk.substringAfter("|").toInt()).lowercase()
    Row(Modifier.fillMaxWidth().heightIn(min = 68.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier.weight(1f)
                .clickable(role = Role.Checkbox, onClickLabel = if (dn) "Retirer la validation" else "Valider") { st.toggleQuest(h.id) }
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                T(
                    h.t, 17, if (dn) C.Muted else C.Text,
                    decoration = if (dn) TextDecoration.LineThrough else null,
                )
                T(sub, 13, C.Muted)
            }
            Spacer(Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.End) {
                T("+${h.xp} XP", 15, C.Violet, maxLines = 1)
                if (h.streak > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocalFireDepartment, null, tint = C.Ember, modifier = Modifier.size(12.dp))
                        T("${h.streak} j", 12, C.Muted)
                    }
                } else T("Nouvelle", 12, C.Muted)
            }
            Spacer(Modifier.width(12.dp))
            Tick(dn)
        }
        if (st.selDiff >= 0 && !dn) {
            IconBtn(Icons.Filled.MoreHoriz, "Ignorer ou décaler ${h.t}") { onMore(h) }
        }
    }
}

/* ───────────── Ignorer / décaler ───────────── */

@Composable
fun ActionDialog(st: AppState, h: Habit, onDismiss: () -> Unit) {
    val movedIn = st.movedInKey(h, st.selDiff) != null
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Panel(Modifier.padding(horizontal = 24.dp), cut = 18f, padding = 20.dp) {
            T(h.t, 24, weight = FontWeight.SemiBold)
            T(dayLabel(st, st.selDiff), 14, C.Muted)
            Spacer(Modifier.height(14.dp))
            ActBtn("Ignorer ce jour") { st.skipDay(h); onDismiss() }
            Spacer(Modifier.height(8.dp))
            ActBtn("Décaler à demain") { st.moveTo(h, st.selDiff + 1); onDismiss() }
            if (movedIn) {
                Spacer(Modifier.height(8.dp))
                ActBtn("Remettre à son jour d’origine") { st.backToOrigin(h); onDismiss() }
            }
            Spacer(Modifier.height(14.dp))
            T("Ou décaler à un autre jour", 14, C.Muted)
            Spacer(Modifier.height(8.dp))
            val targets = (2..9).map { st.selDiff + it }.filter { it <= RANGE }
            targets.chunked(4).forEach { rowDays ->
                Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowDays.forEach { d ->
                        Column(
                            Modifier.weight(1f).heightIn(min = 56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, C.Line, RoundedCornerShape(12.dp))
                                .clickable(role = Role.Button) { st.moveTo(h, d); onDismiss() },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            T(DOW[st.idxOf(d)], 13, C.Muted)
                            T(st.dateAt(d).dayOfMonth.toString(), 22, weight = FontWeight.SemiBold)
                        }
                    }
                    repeat(4 - rowDays.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                GhostButton("Annuler") { onDismiss() }
            }
        }
    }
}

@Composable
private fun ActBtn(text: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        Modifier.fillMaxWidth().heightIn(min = 48.dp)
            .clip(shape)
            .border(1.dp, C.Line, shape)
            .background(Color(0x0FA98BFF))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.CenterStart,
    ) { T(text, 16) }
}

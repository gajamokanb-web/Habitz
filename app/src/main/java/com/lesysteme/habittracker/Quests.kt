package com.lesysteme.habittracker

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.background
import kotlinx.coroutines.delay

private fun nextLabel(st: AppState, h: Habit): String {
    if (h.freq.type != "days") return "Chaque jour"
    for (k in 1..7) {
        val i = (st.todayIdx() + k) % 7
        if (i in h.freq.days) return "Prochaine : " + (if (k == 1) "demain" else FULL[i])
    }
    return ""
}

@Composable
fun QuestsScreen(st: AppState, onEdit: (Habit) -> Unit, onNew: () -> Unit) {
    var armed by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(armed) {
        if (armed != null) {
            delay(4000)
            armed = null
        }
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    T("Mes quêtes", 30, weight = FontWeight.SemiBold)
                    T("${st.habits.size} actives", 15, C.Violet)
                }
                Spacer(Modifier.height(6.dp))
                T("Touche le bouton + pour forger une nouvelle quête et choisir sa fréquence.", 14, C.Muted)
            }
        }
        if (st.habits.isEmpty()) {
            item {
                Panel {
                    T("Aucune quête pour l’instant.", 16, C.Muted)
                    Spacer(Modifier.height(12.dp))
                    GlowButton("Créer une quête") { onNew() }
                }
            }
        }
        for (cat in CATS) {
            val items = st.habits.filter { it.cat == cat.key }
            if (items.isEmpty()) continue
            item(key = cat.key) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CatIcon(cat.key, 42.dp)
                        Spacer(Modifier.width(12.dp))
                        T(cat.name, 22, weight = FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines = 1)
                        T(items.size.toString(), 15, C.Muted)
                    }
                    items.forEach { h ->
                        val isArmed = armed == h.id
                        val today = st.occurs(h, 0)
                        Row(
                            Modifier.fillMaxWidth().chamfer(10f).padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                T(h.t, 18, weight = FontWeight.SemiBold)
                                T(freqLabel(h), 14, C.Muted)
                                Spacer(Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Chip("+${h.xp} XP", C.Violet)
                                    Chip("Rang ${h.rank}", C.Muted)
                                    if (today) Chip("Aujourd’hui", C.Green) else Chip(nextLabel(st, h), C.Muted)
                                }
                            }
                            IconBtn(Icons.Filled.Edit, "Modifier ${h.t}") { onEdit(h) }
                            if (isArmed) {
                                GhostButton("Supprimer ?", color = C.Ember, border = C.Ember.copy(alpha = 0.6f)) {
                                    armed = null
                                    st.deleteHabit(h.id)
                                }
                            } else {
                                IconBtn(Icons.Filled.Delete, "Supprimer ${h.t}") { armed = h.id }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Chip(text: String, color: Color) {
    val shape = RoundedCornerShape(99.dp)
    Box(
        Modifier.clip(shape).border(1.dp, color.copy(alpha = 0.5f), shape).padding(horizontal = 9.dp, vertical = 3.dp)
    ) { T(text, 12, color, maxLines = 1) }
}

/* ───────────── Formulaire de quête (création / modification) ───────────── */

@Composable
fun HabitFormDialog(st: AppState, editing: Habit?, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(editing?.t ?: "") }
    var desc by remember {
        mutableStateOf(if (editing != null && editing.s != freqLabel(editing)) editing.s else "")
    }
    var cat by remember { mutableStateOf(editing?.cat ?: "sport") }
    var rank by remember { mutableStateOf(editing?.rank ?: "D") }
    var ftype by remember { mutableStateOf(editing?.freq?.type ?: "daily") }
    var days by remember { mutableStateOf(editing?.freq?.takeIf { it.type == "days" }?.days?.toSet() ?: setOf(0, 2, 4)) }
    var times by remember { mutableStateOf(editing?.freq?.times ?: 3) }
    var ddOpen by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Panel(
            Modifier.padding(horizontal = 20.dp, vertical = 24.dp).verticalScroll(rememberScrollState()),
            cut = 18f, padding = 20.dp,
        ) {
            T(if (editing == null) "Nouvelle quête" else "Modifier la quête", 30, italic = true, weight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            AppField("Nom de l'habitude", name, { name = it; error = null }, "Ex. Marcher 8 000 pas", maxLen = 40)
            Spacer(Modifier.height(14.dp))
            AppField("Objectif concret (facultatif)", desc, { desc = it }, "Ex. Après le déjeuner")
            Spacer(Modifier.height(14.dp))
            CategoryDropdown(cat, ddOpen, { ddOpen = !ddOpen }) { cat = it; ddOpen = false }
            Spacer(Modifier.height(14.dp))
            T("Difficulté", 14, C.Muted)
            Spacer(Modifier.height(6.dp))
            Segmented(DIFFS.map { it.label }, DIFFS.indexOfFirst { it.rank == rank }.coerceAtLeast(0)) { rank = DIFFS[it].rank }
            Spacer(Modifier.height(4.dp))
            T("Récompense : +${DIFFS.first { it.rank == rank }.xp} XP", 13, C.Violet)
            Spacer(Modifier.height(14.dp))
            T("Fréquence", 14, C.Muted)
            Spacer(Modifier.height(6.dp))
            val types = listOf("daily", "days", "weekly")
            Segmented(listOf("Chaque jour", "Jours précis", "Par semaine"), types.indexOf(ftype)) { ftype = types[it] }
            if (ftype == "days") {
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DOW.forEachIndexed { i, d ->
                        val on = i in days
                        val s = RoundedCornerShape(12.dp)
                        Box(
                            Modifier.weight(1f).heightIn(min = 46.dp).clip(s)
                                .border(1.dp, if (on) C.Violet else C.Line, s)
                                .then(if (on) Modifier.background(C.Violet.copy(alpha = 0.18f)) else Modifier)
                                .clickable(role = Role.Checkbox) { days = if (on) days - i else days + i; error = null },
                            contentAlignment = Alignment.Center,
                        ) { T(d.take(1), 16, if (on) C.Text else C.Muted, align = TextAlign.Center) }
                    }
                }
            }
            if (ftype == "weekly") {
                Spacer(Modifier.height(10.dp))
                Stepper(times, 1, 7, "fois par semaine") { times = it }
            }
            error?.let {
                Spacer(Modifier.height(10.dp))
                T(it, 15, C.Ember)
            }
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GhostButton("Annuler") { onDismiss() }
                GlowButton(if (editing == null) "Forger la quête" else "Enregistrer", Modifier.weight(1f)) {
                    val n = name.trim()
                    if (n.isEmpty()) { error = "Donne un nom à ta quête."; return@GlowButton }
                    if (ftype == "days" && days.isEmpty()) { error = "Choisis au moins un jour."; return@GlowButton }
                    val freq = Freq(ftype, if (ftype == "days") days.sorted() else emptyList(), times)
                    val xp = DIFFS.first { it.rank == rank }.xp
                    val base = Habit(
                        id = editing?.id ?: ("h" + System.currentTimeMillis()),
                        cat = cat, rank = rank, t = n, s = "", xp = xp,
                        streak = editing?.streak ?: 0, wk = editing?.wk ?: 0, freq = freq,
                    )
                    st.upsertHabit(base.copy(s = desc.trim().ifEmpty { freqLabel(base) }), editing != null)
                    onDismiss()
                }
            }
        }
    }
}

package com.lesysteme.habittracker

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun AchArt(code: String, locked: Boolean, width: androidx.compose.ui.unit.Dp, height: androidx.compose.ui.unit.Dp) {
    val grey = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
    Image(
        painter = painterResource(artRes(code)),
        contentDescription = null,
        modifier = Modifier.size(width, height).clip(RoundedCornerShape(10.dp)).alpha(if (locked) 0.5f else 1f),
        contentScale = ContentScale.Fit,
        colorFilter = if (locked) grey else null,
    )
}

@Composable
fun ProfileScreen(st: AppState) {
    val ctx = LocalContext.current
    val scroll = rememberScrollState()
    var achY by remember { mutableIntStateOf(0) }
    var nameEdit by remember { mutableStateOf(st.profileName) }
    var confirmDelete by remember { mutableStateOf(false) }
    var showAdd by remember { mutableStateOf(false) }
    val lv = levelInfo(st.total())

    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            try {
                ctx.contentResolver.openOutputStream(uri)?.use { it.write(st.exportJson().toByteArray(Charsets.UTF_8)) }
                st.toast("Sauvegarde exportée")
            } catch (e: Exception) {
                st.toast("Export impossible")
            }
        }
    }

    LaunchedEffect(st.scrollToAch) {
        if (st.scrollToAch) {
            scroll.animateScrollTo(achY)
            st.scrollToAch = false
        }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(scroll).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        T("Profil", 34, weight = FontWeight.SemiBold)

        /* Bloc profil */
        Panel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Crest(lv.l, progressOf(st), 72.dp)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    T(if (st.profileSaved && st.profileName.isNotBlank()) st.profileName else "Aucun profil", 24, weight = FontWeight.SemiBold, maxLines = 1)
                    T("Niveau ${lv.l} · ${fmt(st.total())} XP", 14, C.Muted)
                }
                Icon(Icons.Filled.Person, null, tint = C.Violet, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.height(14.dp))
            AppField("Nom du ranger", nameEdit, { nameEdit = it }, "Ton nom", maxLen = 24)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlowButton("Enregistrer le nom", Modifier.weight(1f)) {
                    st.renameProfile(nameEdit)
                    st.toast("Nom enregistré")
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GhostButton("Exporter la sauvegarde", Modifier.weight(1f), color = C.Text, border = C.Violet) {
                    exporter.launch("habitz-sauvegarde.json")
                }
                if (confirmDelete) {
                    GhostButton("Confirmer ?", color = C.Ember, border = C.Ember.copy(alpha = 0.7f)) {
                        confirmDelete = false
                        st.deleteProfile()
                        nameEdit = ""
                    }
                } else {
                    GhostButton("Supprimer", color = C.Ember, border = C.Ember.copy(alpha = 0.5f)) { confirmDelete = true }
                }
            }
        }

        /* Succès */
        Column(
            Modifier.onGloballyPositioned { achY = it.positionInParent().y.toInt() },
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    T("Succès", 28, weight = FontWeight.SemiBold)
                    T("${st.achs.count { it.ok }} / ${st.achs.size} débloqués", 14, C.Violet)
                }
                GhostButton("+ Ajouter", color = C.Text, border = C.Violet) { showAdd = true }
            }
            val filters = listOf("all", "done", "todo")
            Segmented(listOf("Tous", "Débloqués", "En cours"), filters.indexOf(st.achFilter).coerceAtLeast(0)) { st.achFilter = filters[it] }

            val shown = st.achs.filter {
                when (st.achFilter) {
                    "done" -> it.ok
                    "todo" -> !it.ok
                    else -> true
                }
            }
            if (shown.isEmpty()) T("Rien à afficher ici.", 15, C.Muted)
            shown.forEach { a ->
                Row(
                    Modifier.fillMaxWidth().chamfer(10f).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AchArt(a.art, locked = !a.ok, width = 64.dp, height = 58.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        T(a.n, 18, weight = FontWeight.SemiBold, maxLines = 1)
                        T(a.d, 13, C.Muted, maxLines = 2)
                        Spacer(Modifier.height(6.dp))
                        if (a.ok) {
                            T("Débloqué · " + agoText(a.ago ?: 0), 13, C.Green)
                        } else {
                            BarFill((a.p.toFloat() / a.g).coerceIn(0f, 1f), C.Violet, height = 7.dp)
                            Spacer(Modifier.height(3.dp))
                            T("${a.p} / ${a.g}", 12, C.Muted)
                        }
                    }
                    if (!a.ok) {
                        Spacer(Modifier.width(8.dp))
                        GhostButton("+1", color = C.Text, border = C.Violet) { st.achPlusOne(a.id) }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }

    if (showAdd) AddAchDialog(st) { showAdd = false }
}

private fun progressOf(st: AppState): Float {
    val lv = levelInfo(st.total())
    return if (lv.n <= 0) 0f else (lv.r.toFloat() / lv.n).coerceIn(0f, 1f)
}

@Composable
private fun AddAchDialog(st: AppState, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var art by remember { mutableStateOf(allArtCodes().first()) }
    var goal by remember { mutableIntStateOf(5) }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Panel(Modifier.padding(horizontal = 20.dp, vertical = 24.dp).verticalScroll(rememberScrollState()), cut = 18f, padding = 20.dp) {
            T("Nouveau succès", 28, italic = true, weight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            AppField("Nom du succès", name, { name = it; error = null }, "Ex. Marcheur infatigable", maxLen = 30)
            Spacer(Modifier.height(12.dp))
            AppField("Description (facultatif)", desc, { desc = it }, "Ex. 50 sorties de marche")
            Spacer(Modifier.height(12.dp))
            T("Illustration", 14, C.Muted)
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(allArtCodes()) { code ->
                    val on = code == art
                    val s = RoundedCornerShape(12.dp)
                    Box(
                        Modifier.clip(s)
                            .border(2.dp, if (on) C.Violet else Color.Transparent, s)
                            .clickable(role = Role.RadioButton) { art = code }
                            .padding(3.dp)
                    ) { AchArt(code, locked = false, width = 56.dp, height = 52.dp) }
                }
            }
            Spacer(Modifier.height(12.dp))
            T("Objectif", 14, C.Muted)
            Spacer(Modifier.height(6.dp))
            Stepper(goal, 1, 365, "validations") { goal = it }
            error?.let {
                Spacer(Modifier.height(10.dp))
                T(it, 15, C.Ember)
            }
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GhostButton("Annuler") { onDismiss() }
                GlowButton("Ajouter", Modifier.weight(1f)) {
                    if (name.isBlank()) {
                        error = "Donne un nom au succès."
                        return@GlowButton
                    }
                    st.addAch(name.trim(), desc.trim(), art, goal)
                    onDismiss()
                }
            }
        }
    }
}

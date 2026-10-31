package com.lesysteme.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var state: AppState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        state = AppState(applicationContext)
        state.load()
        setContent { HabitzApp(state) }
    }

    override fun onPause() {
        super.onPause()
        state.save()
    }
}

@Composable
fun HabitzApp(st: AppState) {
    var formOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Habit?>(null) }

    LaunchedEffect(st.toastId) {
        if (st.toastMsg != null) {
            delay(2200)
            st.toastMsg = null
        }
    }

    Box(Modifier.fillMaxSize().background(C.Bg)) {
        FogBackground(Modifier.fillMaxSize())
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f).fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars)) {
                when (st.tab) {
                    0 -> HomeScreen(st)
                    1 -> QuestsScreen(st, onEdit = { editing = it; formOpen = true }, onNew = { editing = null; formOpen = true })
                    2 -> StatsScreen(st)
                    else -> ProfileScreen(st)
                }
            }
            BottomBar(st) { editing = null; formOpen = true }
        }

        st.toastMsg?.let { msg ->
            Box(
                Modifier.align(Alignment.BottomCenter).windowInsetsPadding(WindowInsets.navigationBars).padding(bottom = 96.dp, start = 24.dp, end = 24.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xF21B1046))
                    .border(1.dp, C.Violet, RoundedCornerShape(14.dp))
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) { T(msg, 15, align = TextAlign.Center) }
        }
    }

    if (formOpen) HabitFormDialog(st, editing) { formOpen = false; editing = null }

    st.levelUp?.let { lvl ->
        Dialog(onDismissRequest = { st.levelUp = null }) {
            Panel(cut = 18f, padding = 24.dp) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    T("NIVEAU SUPÉRIEUR", 14, C.Violet)
                    Spacer(Modifier.height(8.dp))
                    T(lvl.toString(), 64, weight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    T("Ta puissance grandit, ranger.", 16, C.Muted, align = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    GlowButton("Continuer") { st.levelUp = null }
                }
            }
        }
    }
}

@Composable
private fun BottomBar(st: AppState, onAdd: () -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xE60E0A26), Color(0xFF07041A))))
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavItem(Icons.Filled.Home, "Accueil", st.tab == 0) { st.tab = 0 }
        NavItem(Icons.Filled.Checklist, "Quêtes", st.tab == 1) { st.tab = 1 }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Box(
                Modifier.size(56.dp).clip(CircleShape)
                    .background(Brush.linearGradient(listOf(C.Light, C.Violet2)))
                    .clickable(role = Role.Button, onClickLabel = "Nouvelle quête", onClick = onAdd),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Filled.Add, "Nouvelle quête", tint = C.OnAccent, modifier = Modifier.size(30.dp)) }
        }
        NavItem(Icons.Filled.BarChart, "Stats", st.tab == 2) { st.tab = 2 }
        NavItem(Icons.Filled.Person, "Profil", st.tab == 3) { st.tab = 3 }
    }
}

@Composable
private fun RowScope.NavItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val tint = if (selected) C.Violet else C.Muted
    Column(
        Modifier.weight(1f).heightIn(min = 52.dp).clip(RoundedCornerShape(12.dp)).clickable(role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp))
        T(label, 12, tint, maxLines = 1)
    }
}

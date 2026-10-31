package com.lesysteme.habittracker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    maxLen: Int = 50,
) {
    Column(modifier.fillMaxWidth()) {
        T(label, 14, C.Muted)
        Box(Modifier.size(6.dp))
        val shape = RoundedCornerShape(12.dp)
        BasicTextField(
            value = value,
            onValueChange = { if (it.length <= maxLen) onChange(it) },
            singleLine = true,
            textStyle = TextStyle(color = C.Text, fontSize = 17.sp, fontFamily = FontFamily.Serif),
            cursorBrush = SolidColor(C.Violet),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                Box(
                    Modifier.fillMaxWidth()
                        .heightIn(min = 50.dp)
                        .clip(shape)
                        .border(1.dp, C.Line, shape)
                        .background(Color(0x0FA98BFF))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) T(placeholder, 17, C.Muted.copy(alpha = 0.6f), maxLines = 1)
                    inner()
                }
            },
        )
    }
}

@Composable
fun Stepper(value: Int, min: Int, max: Int, suffix: String, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        StepBtn("−", "Moins") { onChange((value - 1).coerceAtLeast(min)) }
        Box(Modifier.width(56.dp), contentAlignment = Alignment.Center) {
            T(value.toString(), 34, C.Violet, weight = FontWeight.SemiBold)
        }
        StepBtn("+", "Plus") { onChange((value + 1).coerceAtMost(max)) }
        Spacer(Modifier.width(12.dp))
        T(suffix, 15, C.Muted)
    }
}

@Composable
private fun StepBtn(symbol: String, desc: String, onClick: () -> Unit) {
    Box(
        Modifier.size(46.dp)
            .clip(CircleShape)
            .border(1.dp, C.Line, CircleShape)
            .background(Color(0x0FA98BFF))
            .clickable(role = Role.Button, onClickLabel = desc, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { T(symbol, 24) }
}

/** Liste déroulante des catégories (badges). */
@Composable
fun CategoryDropdown(selected: String, expanded: Boolean, onToggle: () -> Unit, onPick: (String) -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Column(Modifier.fillMaxWidth()) {
        T("Catégorie", 14, C.Muted)
        Box(Modifier.size(6.dp))
        Row(
            Modifier.fillMaxWidth()
                .heightIn(min = 56.dp)
                .clip(shape)
                .border(1.dp, if (expanded) C.Violet else C.Line, shape)
                .background(Color(0x0FA98BFF))
                .clickable(role = Role.DropdownList, onClickLabel = "Choisir la catégorie", onClick = onToggle)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CatIcon(selected, 38.dp)
            Spacer(Modifier.width(12.dp))
            T(catOf(selected).name, 17, modifier = Modifier.weight(1f), maxLines = 1)
            Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null, tint = C.Violet)
        }
        if (expanded) {
            Box(Modifier.size(6.dp))
            Column(
                Modifier.fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .clip(shape)
                    .border(1.dp, C.Violet.copy(alpha = 0.5f), shape)
                    .background(Color(0xFF170F38))
                    .verticalScroll(rememberScrollState())
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                CATS.forEach { c ->
                    Row(
                        Modifier.fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .then(if (c.key == selected) Modifier.background(c.color.copy(alpha = 0.16f)) else Modifier)
                            .clickable(role = Role.Button) { onPick(c.key) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CatIcon(c.key, 38.dp)
                        Spacer(Modifier.width(12.dp))
                        T(c.name, 16)
                    }
                }
            }
        }
    }
}

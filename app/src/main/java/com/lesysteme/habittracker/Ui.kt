package com.lesysteme.habittracker

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/* ───────────── Palette violette ───────────── */

object C {
    val Bg = Color(0xFF07041A)
    val Panel1 = Color(0xE61B1046)
    val Panel2 = Color(0xEB0E0A26)
    val Violet = Color(0xFFA98BFF)
    val Violet2 = Color(0xFF7C4DFF)
    val Orchid = Color(0xFFC58CFF)
    val Light = Color(0xFFD8C8FF)
    val Text = Color(0xFFF1ECFF)
    val Muted = Color(0xFFA79FCB)
    val Ember = Color(0xFFFF8A5C)
    val Green = Color(0xFF4ADE80)
    val Line = Color(0x40A98BFF)
    val OnAccent = Color(0xFF0C0620)
}

/* ───────────── Texte ───────────── */

@Composable
fun T(
    text: String,
    size: Int = 16,
    color: Color = C.Text,
    modifier: Modifier = Modifier,
    weight: FontWeight? = null,
    italic: Boolean = false,
    align: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    decoration: TextDecoration? = null,
) {
    Text(
        text = text,
        color = color,
        fontSize = size.sp,
        fontFamily = FontFamily.Serif,
        fontWeight = weight,
        fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
        textAlign = align,
        textDecoration = decoration,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

/* ───────────── Blocs biseautés ───────────── */

class ChamferShape(private val cutDp: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val c = cutDp * density.density
        val p = Path().apply {
            moveTo(c, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height - c)
            lineTo(size.width - c, size.height)
            lineTo(0f, size.height)
            lineTo(0f, c)
            close()
        }
        return Outline.Generic(p)
    }
}

fun Modifier.chamfer(cut: Float = 14f): Modifier {
    val shape = ChamferShape(cut)
    return this
        .clip(shape)
        .background(Brush.linearGradient(listOf(C.Panel1, C.Panel2)))
        .border(
            1.2.dp,
            Brush.linearGradient(listOf(Color(0xF2BFA0FF), Color(0x667C4DFF), Color(0x99C58CFF))),
            shape,
        )
}

@Composable
fun Panel(
    modifier: Modifier = Modifier,
    cut: Float = 14f,
    padding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth().chamfer(cut).padding(padding), content = content)
}

/* ───────────── Boutons ───────────── */

@Composable
fun GlowButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier
            .heightIn(min = 48.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(C.Light, C.Violet2)))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) { T(text, 16, C.OnAccent, align = TextAlign.Center) }
}

@Composable
fun GhostButton(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = C.Muted,
    border: Color = C.Line,
    onClick: () -> Unit,
) {
    Box(
        modifier
            .heightIn(min = 48.dp)
            .clip(CircleShape)
            .border(1.dp, border, CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) { T(text, 15, color, align = TextAlign.Center) }
}

@Composable
fun Segmented(options: List<String>, selected: Int, modifier: Modifier = Modifier, onSelect: (Int) -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, C.Line, shape)
            .background(Color(0xB307041A))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEachIndexed { i, label ->
            val on = i == selected
            val s = RoundedCornerShape(11.dp)
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp)
                    .clip(s)
                    .then(
                        if (on) Modifier
                            .background(Brush.linearGradient(listOf(Color(0xFF3A2290), Color(0xFF291866))))
                            .border(1.dp, C.Violet, s)
                        else Modifier
                    )
                    .clickable(role = Role.Tab) { onSelect(i) },
                contentAlignment = Alignment.Center,
            ) { T(label, 15, C.Text, maxLines = 1, align = TextAlign.Center) }
        }
    }
}

@Composable
fun CatIcon(key: String, size: Dp = 40.dp, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(catOf(key).res),
        contentDescription = null,
        modifier = modifier.size(size).clip(RoundedCornerShape(size / 4)),
    )
}

@Composable
fun BarFill(frac: Float, color: Color, modifier: Modifier = Modifier, height: Dp = 9.dp) {
    val shape = RoundedCornerShape(99.dp)
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .border(1.dp, C.Line, shape)
            .background(Color(0xA607041A))
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(frac.coerceIn(0.001f, 1f))
                .clip(shape)
                .background(Brush.horizontalGradient(listOf(color, C.Light)))
        )
    }
}

/* ───────────── Anneau de progression ───────────── */

@Composable
fun Ring(
    progress: Float,
    modifier: Modifier = Modifier,
    stroke: Dp = 8.dp,
    content: @Composable BoxScope.() -> Unit = {},
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val w = stroke.toPx()
            val inset = w / 2
            val sz = Size(size.width - w, size.height - w)
            drawArc(
                color = Color(0x29A98BFF),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = sz,
                style = Stroke(w),
            )
            drawArc(
                brush = Brush.linearGradient(listOf(Color(0xFFE6DCFF), Color(0xFF7C4DFF))),
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = sz,
                style = Stroke(w, cap = StrokeCap.Round),
            )
        }
        content()
    }
}

/* ───────────── Blason de niveau ───────────── */

@Composable
fun Crest(level: Int, progress: Float, size: Dp, modifier: Modifier = Modifier) {
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Ring(progress, Modifier.fillMaxSize(), stroke = 5.dp)
        Canvas(Modifier.fillMaxSize().padding(size * 0.14f)) {
            val cx = this.size.width / 2
            val cy = this.size.height / 2
            val r = this.size.minDimension / 2
            val hex = Path().apply {
                for (i in 0 until 6) {
                    val a = (PI / 180.0 * (60 * i - 90)).toFloat()
                    val x = cx + r * cos(a)
                    val y = cy + r * sin(a)
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            drawPath(hex, Color(0xFF0E0A26))
            drawPath(hex, Color(0xFFCBB8FF), style = Stroke(2.dp.toPx()))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            T("NIV.", 9, C.Violet)
            T(level.toString(), (size.value * 0.34f).toInt(), C.Text, weight = FontWeight.Bold)
        }
    }
}

/* ───────────── Brouillard et particules ───────────── */

@Composable
fun FogBackground(modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "fog")
    val t by inf.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(40000, easing = LinearEasing), RepeatMode.Restart),
        label = "t",
    )
    val seeds = remember {
        List(26) { Triple(Math.random().toFloat(), Math.random().toFloat(), Math.random().toFloat()) }
    }
    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val a = (t * 2.0 * PI).toFloat()
        fun fog(cx: Float, cy: Float, r: Float, col: Color) {
            drawCircle(
                brush = Brush.radialGradient(listOf(col, Color.Transparent), center = Offset(cx, cy), radius = r),
                radius = r,
                center = Offset(cx, cy),
            )
        }
        fog(w * (0.25f + 0.25f * sin(a)), h * 0.18f, w * 0.85f, Color(0x4D9664FF))
        fog(w * (0.85f - 0.25f * sin(a + 1f)), h * 0.5f, w * 0.8f, Color(0x40BE5AFF))
        fog(w * (0.5f + 0.3f * cos(a)), h * 0.92f, w * 0.95f, Color(0x4D7850FF))
        seeds.forEach { (px, speed, sz) ->
            val k = 1 + (speed * 3).toInt()
            val p = (t * k + px) % 1f
            val x = px * w + sin(a * k + px * 6f) * 18f
            val y = h * (1f - p)
            val alpha = sin(PI.toFloat() * p) * 0.7f
            val r = (1.2f + sz * 2f) * density
            drawCircle(Color(0xFFBE96FF).copy(alpha = alpha * 0.25f), r * 4f, Offset(x, y))
            drawCircle(Color(0xFFF1E8FF).copy(alpha = alpha), r, Offset(x, y))
        }
    }
}

@Composable
fun IconBtn(icon: ImageVector, desc: String, modifier: Modifier = Modifier, tint: Color = C.Muted, onClick: () -> Unit) {
    Box(
        modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClickLabel = desc, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, contentDescription = desc, tint = tint, modifier = Modifier.size(22.dp)) }
}

@Composable
fun Tick(done: Boolean) {
    Box(
        Modifier
            .size(34.dp)
            .clip(CircleShape)
            .then(
                if (done) Modifier.background(Brush.linearGradient(listOf(C.Light, C.Violet2)))
                else Modifier.border(2.dp, C.Violet.copy(alpha = 0.55f), CircleShape)
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (done) Icon(androidx.compose.material.icons.Icons.Filled.Check, null, tint = C.OnAccent, modifier = Modifier.size(20.dp))
    }
}

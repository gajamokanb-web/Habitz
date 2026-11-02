package com.lesysteme.habittracker

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import kotlin.math.roundToInt

/* ───────────── Catégories (15 badges) ───────────── */

data class Cat(val key: String, val name: String, val color: Color, val res: Int)

val CATS = listOf(
    Cat("sport", "Sport & Santé", Color(0xFFFF5A4D), R.drawable.cat_sport),
    Cat("apprentissage", "Apprentissage", Color(0xFF4F8BFF), R.drawable.cat_apprentissage),
    Cat("productivite", "Productivité & Travail", Color(0xFFA070FF), R.drawable.cat_productivite),
    Cat("finance", "Finance", Color(0xFFF5BD4F), R.drawable.cat_finance),
    Cat("nutrition", "Nutrition", Color(0xFF4CD964), R.drawable.cat_nutrition),
    Cat("bienetre", "Bien-être & Mental", Color(0xFF3FE0E0), R.drawable.cat_bienetre),
    Cat("relations", "Relations & Social", Color(0xFFFF6FB5), R.drawable.cat_relations),
    Cat("sommeil", "Sommeil & Récupération", Color(0xFF8F78FF), R.drawable.cat_sommeil),
    Cat("objectifs", "Objectifs & Projets", Color(0xFFFFA93A), R.drawable.cat_objectifs),
    Cat("voyage", "Voyage & Découverte", Color(0xFF4AA8FF), R.drawable.cat_voyage),
    Cat("creativite", "Créativité & Arts", Color(0xFFB36BFF), R.drawable.cat_creativite),
    Cat("maison", "Maison & Organisation", Color(0xFF2FD0D0), R.drawable.cat_maison),
    Cat("habitudes", "Habitudes Quotidiennes", Color(0xFF46B6FF), R.drawable.cat_habitudes),
    Cat("medical", "Santé Médicale", Color(0xFFFF4D5E), R.drawable.cat_medical),
    Cat("autre", "Autre (personnalisé)", Color(0xFF7A86FF), R.drawable.cat_autre),
)

fun catOf(key: String): Cat = CATS.firstOrNull { it.key == key } ?: CATS.last()

val ACH_ART: Map<String, Int> = linkedMapOf(
    "marathonien" to R.drawable.ach_marathonien,
    "lecteur" to R.drawable.ach_lecteur,
    "deepwork" to R.drawable.ach_deepwork,
    "equilibre" to R.drawable.ach_equilibre,
    "chef" to R.drawable.ach_chef,
    "objectif" to R.drawable.ach_objectif,
    "lien" to R.drawable.ach_lien,
    "finances" to R.drawable.ach_finances,
    "explorateur" to R.drawable.ach_explorateur,
    "sommeil" to R.drawable.ach_sommeil,
)

/** Code d'illustration : "a:<clé>" (illustration de succès) ou "c:<clé>" (badge de catégorie). */
fun artRes(code: String): Int {
    val parts = code.split(":")
    return if (parts.size == 2 && parts[0] == "a") ACH_ART[parts[1]] ?: R.drawable.ach_marathonien
    else catOf(parts.getOrElse(1) { "autre" }).res
}

fun allArtCodes(): List<String> = ACH_ART.keys.map { "a:$it" } + CATS.map { "c:${it.key}" }

/* ───────────── Habitudes ───────────── */

val DOW = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")
val FULL = listOf("lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi", "dimanche")
val MONTHS = listOf(
    "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
    "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
)
val MSHORT = listOf(
    "janv.", "févr.", "mars", "avr.", "mai", "juin",
    "juil.", "août", "sept.", "oct.", "nov.", "déc."
)

/** type : "daily" | "days" | "weekly" */
data class Freq(val type: String, val days: List<Int> = emptyList(), val times: Int = 3)

data class Habit(
    val id: String,
    val cat: String,
    val rank: String,
    val t: String,
    val s: String,
    val xp: Int,
    val streak: Int = 0,
    val wk: Int = 0,
    val freq: Freq = Freq("daily"),
)

data class Diff(val rank: String, val label: String, val xp: Int)

val DIFFS = listOf(Diff("E", "Facile", 50), Diff("D", "Moyen", 100), Diff("C", "Difficile", 150), Diff("B", "Épique", 250))

fun freqLabel(h: Habit): String {
    val f = h.freq
    if (f.type == "daily") return "Chaque jour"
    if (f.type == "weekly") return "${f.times} fois par semaine"
    val d = f.days.sorted()
    if (d.size == 7) return "Chaque jour"
    if (d == listOf(0, 1, 2, 3, 4)) return "En semaine"
    if (d == listOf(5, 6)) return "Le week-end"
    return d.joinToString(" · ") { DOW[it] + "." }
}

fun initialHabits(): List<Habit> = listOf(
    Habit("sport", "sport", "B", "Sport · 30 min", "Séance complète", 250, 8, 2, Freq("weekly", times = 4)),
    Habit("eau", "sport", "E", "Boire 2 L d’eau", "Tout au long de la journée", 50, 21, 0, Freq("daily")),
    Habit("coucher", "sommeil", "D", "Coucher avant 23 h", "Écrans éteints à 22 h 30", 100, 9, 0, Freq("daily")),
    Habit("deep", "productivite", "C", "Deep work · 90 min", "Sans notifications", 150, 14, 0, Freq("days", listOf(0, 1, 2, 3, 4, 5))),
    Habit("lecture", "apprentissage", "D", "Lire 20 pages", "Finance ou stratégie", 100, 15, 0, Freq("days", listOf(0, 2, 4, 5))),
    Habit("revue", "productivite", "D", "Revue des priorités", "Préparer la semaine", 100, 6, 0, Freq("days", listOf(5))),
    Habit("medit", "bienetre", "D", "Méditation · 10 min", "Respiration guidée", 100, 5, 0, Freq("daily")),
    Habit("proche", "relations", "E", "Appeler un proche", "Prendre des nouvelles", 50, 3, 0, Freq("days", listOf(5, 6))),
    Habit("journal", "bienetre", "D", "Journal du soir", "3 lignes sur la journée", 100, 12, 0, Freq("daily")),
)

val INIT_DONE = setOf("sport", "eau", "deep", "lecture", "medit", "proche")

/** Historique des 33 derniers jours (0 = vide … 4 = journée parfaite), le dernier = hier. */
val HEAT_DEMO = listOf(3, 2, 4, 0, 3, 3, 2, 4, 4, 1, 0, 0, 3, 4, 3, 3, 2, 3, 4, 2, 2, 3, 4, 3, 2, 4, 4, 3, 3, 4, 2, 3, 4)

/** Vrai après suppression du profil : tout repart de zéro (niveau 1, aucun historique). */
var FRESH by mutableStateOf(false)

val HEAT: List<Int> get() = if (FRESH) List(33) { 0 } else HEAT_DEMO

val RATES = mapOf("sport" to 84, "apprentissage" to 72, "productivite" to 77, "bienetre" to 81, "relations" to 79, "sommeil" to 68)

const val BASE_XP = 20720
const val RANGE = 120

/* ───────────── Succès ───────────── */

data class Ach(
    val id: String,
    val n: String,
    val d: String,
    val art: String,
    val p: Int,
    val g: Int,
    val ago: Int? = null,
) {
    val ok: Boolean get() = p >= g
}

fun initialAchs(): List<Ach> {
    val raw = listOf(
        listOf("Marathonien", "30 séances de sport", "a:marathonien", 30, 30, 2),
        listOf("Lecteur assidu", "7 jours de lecture d’affilée", "a:lecteur", 7, 7, 5),
        listOf("Deep Work Maître", "10 sessions de travail profond", "a:deepwork", 6, 10, null),
        listOf("Équilibre retrouvé", "5 jours de méditation d’affilée", "a:equilibre", 5, 5, 1),
        listOf("Chef Maison", "Cuisiner 20 repas maison", "a:chef", 12, 20, null),
        listOf("Objectif atteint", "Atteindre 3 objectifs", "a:objectif", 3, 3, 14),
        listOf("Lien renforcé", "Appeler un proche 10 fois", "a:lien", 10, 10, 20),
        listOf("Finances en progrès", "Suivre son budget 30 jours", "a:finances", 18, 30, null),
        listOf("Explorateur", "Planifier 3 sorties", "a:explorateur", 1, 3, null),
        listOf("Sommeil optimal", "Coucher avant 23 h, 14 jours", "a:sommeil", 9, 14, null),
        listOf("Série de feu", "21 jours de série", "c:habitudes", 21, 21, 0),
        listOf("Brise-chaînes", "50 séances de force", "c:sport", 34, 50, null),
        listOf("Esprit créatif", "10 sessions créatives", "c:creativite", 4, 10, null),
        listOf("Maison en ordre", "Ranger 15 fois", "c:maison", 15, 15, 27),
        listOf("Santé de fer", "Rendez-vous santé à jour", "c:medical", 2, 2, 40),
        listOf("Source de vie", "Boire 2 L pendant 14 jours", "c:nutrition", 14, 14, 33),
        listOf("Rituel du matin", "Routine du matin 30 jours", "c:habitudes", 12, 30, null),
        listOf("Érudit des Ombres", "20 séances d’apprentissage", "c:apprentissage", 12, 20, null),
        listOf("Esprit serein", "21 jours de méditation", "c:bienetre", 9, 21, null),
        listOf("Lame d’Ébène", "100 jours de série", "c:objectifs", 62, 100, null),
        listOf("Premier pas", "Valider sa première quête", "c:autre", 1, 1, 60),
    )
    return raw.mapIndexed { i, r ->
        Ach("a$i", r[0] as String, r[1] as String, r[2] as String, r[3] as Int, r[4] as Int, r[5] as Int?)
    }
}

/* ───────────── Niveau ───────────── */

data class Lv(val l: Int, val r: Int, val n: Int)

fun needFor(l: Int) = if (FRESH) 100 + (l - 1) * 50 else 22000 + (l - 34) * 800

fun levelInfo(total: Int): Lv {
    var l = if (FRESH) 1 else 34
    var r = total
    while (r >= needFor(l)) {
        r -= needFor(l)
        l++
    }
    return Lv(l, r, needFor(l))
}

/* ───────────── État de l'application ───────────── */

class AppState(private val ctx: Context) {
    var habits by mutableStateOf(initialHabits())
    var done by mutableStateOf(INIT_DONE)
    var skips by mutableStateOf(setOf<String>())
    var moves by mutableStateOf(mapOf<String, Long?>())
    var marks by mutableStateOf(mapOf<String, Boolean>())
    var selDiff by mutableStateOf(0)
    var openCats by mutableStateOf(setOf("sport"))
    var tab by mutableStateOf(0)
    var xpBase by mutableStateOf(BASE_XP)
    var profileName by mutableStateOf("Gajamokan")
    var profileSaved by mutableStateOf(true)
    var achs by mutableStateOf(initialAchs())
    var toastMsg by mutableStateOf<String?>(null)
    var toastId by mutableStateOf(0)
    var levelUp by mutableStateOf<Int?>(null)
    var period by mutableStateOf(7)
    var achFilter by mutableStateOf("all")
    var scrollToAch by mutableStateOf(false)
    private var doneDate: Long = LocalDate.now().toEpochDay()

    private val prefs get() = ctx.getSharedPreferences("habitz", Context.MODE_PRIVATE)

    /* ----- calendrier ----- */
    fun todayIdx(): Int = LocalDate.now().dayOfWeek.value - 1
    fun idxOf(diff: Int): Int = (((todayIdx() + diff) % 7) + 7) % 7
    fun dateAt(diff: Int): LocalDate = LocalDate.now().plusDays(diff.toLong())

    /* ----- règles de planification ----- */
    fun isDueOn(h: Habit, idx: Int): Boolean = if (h.freq.type == "days") idx in h.freq.days else true
    fun baseDue(h: Habit, d: Int) = isDueOn(h, idxOf(d))
    /** Clés absolues (jour calendaire) : les ignorées, décalées et validations survivent au changement de jour. */
    fun ek(id: String, d: Int): String = "$id|${dateAt(d).toEpochDay()}"
    fun moveDiff(v: Long?): Int? = v?.let { (it - LocalDate.now().toEpochDay()).toInt() }

    fun hidden(h: Habit, d: Int) =
        baseDue(h, d) && (skips.contains(ek(h.id, d)) || moves.containsKey(ek(h.id, d)))

    /** Clé de déplacement si la quête a été décalée vers le jour d. */
    fun movedInKey(h: Habit, d: Int): String? =
        moves.entries.firstOrNull { it.key.startsWith(h.id + "|") && it.value == dateAt(d).toEpochDay() }?.key

    fun occurs(h: Habit, d: Int) = (baseDue(h, d) && !hidden(h, d)) || movedInKey(h, d) != null
    fun dayList(d: Int): List<Habit> = habits.filter { occurs(it, d) }
    fun dueToday(): List<Habit> = dayList(0)

    fun lvlFor(diff: Int): Int {
        if (diff >= 0) return 0
        val k = 33 + diff
        return if (k >= 0) HEAT[k] else if (FRESH) 0 else 2
    }

    /** Niveau de la journée d (0 à 4), calculé depuis les validations réelles. */
    fun dayLevel(d: Int): Int {
        val hs = dayList(d)
        if (hs.isEmpty()) return 0
        val n = hs.count { isDoneOn(it, d) }
        return kotlin.math.ceil(4f * n / hs.size).toInt().coerceIn(0, 4)
    }

    /** Série en cours : jours consécutifs avec au moins une quête validée (jours sans quête neutres). */
    fun streak(): Int {
        val lim = if (FRESH) 365 else 33
        var d = if (dayList(0).isNotEmpty() && dayLevel(0) == 0) -1 else 0
        var n = 0
        while (d >= -lim) {
            if (dayList(d).isNotEmpty()) {
                if (dayLevel(d) > 0) n++ else break
            }
            d--
        }
        return n
    }

    fun isDoneOn(h: Habit, d: Int): Boolean {
        if (d == 0) return done.contains(h.id)
        val m = marks[ek(h.id, d)]
        if (m != null) return m
        if (d < 0) {
            val hs = dayList(d)
            val p = hs.indexOfFirst { it.id == h.id }
            val k = (lvlFor(d) / 4f * hs.size).roundToInt()
            return p in 0 until k
        }
        return false
    }

    fun doneXp(): Int = dueToday().filter { done.contains(it.id) }.sumOf { it.xp }
    fun total(): Int = xpBase + doneXp()

    /* ----- actions ----- */
    fun toast(msg: String) {
        toastMsg = msg
        toastId++
    }

    fun toggleQuest(id: String) {
        val h = habits.firstOrNull { it.id == id } ?: return
        if (selDiff != 0) {
            val k = ek(id, selDiff)
            val was = isDoneOn(h, selDiff)
            marks = marks + (k to !was)
            toast(if (was) "Validation retirée" else "Quête validée")
        } else {
            val before = levelInfo(total()).l
            val was = done.contains(id)
            done = if (was) done - id else done + id
            if (was) toast("−${h.xp} XP · quête rouverte") else toast("+${h.xp} XP")
            val after = levelInfo(total()).l
            if (after > before) levelUp = after
        }
        save()
    }

    fun toggleCat(key: String) {
        openCats = if (openCats.contains(key)) openCats - key else openCats + key
    }

    fun skipDay(h: Habit) {
        val mk = movedInKey(h, selDiff)
        if (mk != null) moves = moves + (mk to null) else skips = skips + ek(h.id, selDiff)
        done = done - h.id
        toast("Quête ignorée pour ce jour")
        save()
    }

    fun moveTo(h: Habit, target: Int) {
        val mk = movedInKey(h, selDiff)
        if (mk != null) {
            val from = mk.substringAfter("|").toLong()
            val tgt = dateAt(target).toEpochDay()
            moves = if (tgt == from) moves - mk else moves + (mk to tgt)
        } else {
            skips = skips - ek(h.id, selDiff)
            moves = moves + (ek(h.id, selDiff) to dateAt(target).toEpochDay())
        }
        done = done - h.id
        toast("Quête décalée")
        save()
    }

    fun backToOrigin(h: Habit) {
        val mk = movedInKey(h, selDiff) ?: return
        moves = moves - mk
        toast("Quête remise à son jour")
        save()
    }

    fun restore(h: Habit) {
        val k = ek(h.id, selDiff)
        skips = skips - k
        moves = moves - k
        toast("Quête rétablie")
        save()
    }

    fun upsertHabit(h: Habit, editing: Boolean) {
        habits = if (editing) habits.map { if (it.id == h.id) h else it } else habits + h
        openCats = openCats + h.cat
        toast(if (editing) "Quête modifiée" else "Quête créée")
        save()
    }

    fun deleteHabit(id: String) {
        habits = habits.filter { it.id != id }
        done = done - id
        skips = skips.filter { !it.startsWith("$id|") }.toSet()
        moves = moves.filterKeys { !it.startsWith("$id|") }
        marks = marks.filterKeys { !it.startsWith("$id|") }
        toast("Quête supprimée")
        save()
    }

    fun achPlusOne(id: String) {
        achs = achs.map {
            if (it.id == id) {
                val np = it.p + 1
                val un = np >= it.g
                if (un) toast("Succès débloqué · ${it.n}")
                it.copy(p = np, ago = if (un) 0 else it.ago)
            } else it
        }
        save()
    }

    fun addAch(name: String, desc: String, art: String, goal: Int) {
        achs = achs + Ach("u" + System.currentTimeMillis(), name, desc.ifBlank { "$goal validations" }, art, 0, goal)
        achFilter = "todo"
        toast("Succès ajouté · $name")
        save()
    }

    fun renameProfile(name: String) {
        if (name.isNotBlank()) {
            profileName = name.trim()
            profileSaved = true
            save()
        }
    }

    fun deleteProfile() {
        profileName = ""
        profileSaved = false
        FRESH = true
        habits = emptyList()
        done = emptySet()
        skips = emptySet()
        moves = emptyMap()
        marks = emptyMap()
        selDiff = 0
        openCats = emptySet()
        xpBase = 0
        achs = achs.map { it.copy(p = 0, ago = null) }
        levelUp = null
        toast("Profil supprimé")
        save()
    }

    fun resetDemo() {
        habits = initialHabits()
        done = INIT_DONE
        skips = emptySet()
        moves = emptyMap()
        marks = emptyMap()
        selDiff = 0
        openCats = setOf("sport")
        FRESH = false
        xpBase = BASE_XP
        achs = initialAchs()
        profileName = "Gajamokan"
        profileSaved = true
        toast("Démo réinitialisée")
        save()
    }

    /* ----- sauvegarde ----- */
    fun exportJson(): String {
        val root = JSONObject()
        root.put("app", "Habitz")
        root.put("version", 1)
        root.put("exportedAt", java.time.Instant.now().toString())
        root.put("profile", JSONObject().put("name", profileName).put("saved", profileSaved))
        root.put("totalXp", total())
        root.put("level", levelInfo(total()).l)
        root.put("xpBase", xpBase)
        root.put("fresh", FRESH)
        root.put("doneDate", doneDate)
        root.put("habits", JSONArray().also { arr -> habits.forEach { arr.put(habitToJson(it)) } })
        root.put("doneToday", JSONArray(done.toList()))
        root.put("skips", JSONArray(skips.toList()))
        root.put("moves", JSONObject().also { o -> moves.forEach { (k, v) -> o.put(k, v ?: JSONObject.NULL) } })
        root.put("marks", JSONObject().also { o -> marks.forEach { (k, v) -> o.put(k, v) } })
        root.put("achievements", JSONArray().also { arr -> achs.forEach { arr.put(achToJson(it)) } })
        return root.toString(2)
    }

    fun save() {
        prefs.edit().putString("state", exportJson()).apply()
    }

    fun load() {
        val raw = prefs.getString("state", null) ?: return
        try {
            val o = JSONObject(raw)
            val p = o.getJSONObject("profile")
            profileName = p.getString("name")
            profileSaved = p.getBoolean("saved")
            FRESH = o.optBoolean("fresh", false)
            xpBase = o.getInt("xpBase")
            habits = o.getJSONArray("habits").let { a -> (0 until a.length()).map { habitFromJson(a.getJSONObject(it)) } }
            achs = o.getJSONArray("achievements").let { a -> (0 until a.length()).map { achFromJson(a.getJSONObject(it)) } }
            val stored = o.getLong("doneDate")
            val today = LocalDate.now().toEpochDay()
            val ids = o.getJSONArray("doneToday").let { a -> (0 until a.length()).map { a.getString(it) }.toSet() }
            skips = o.getJSONArray("skips").let { a -> (0 until a.length()).map { a.getString(it) }.toSet() }
            moves = o.getJSONObject("moves").let { m ->
                m.keys().asSequence().associateWith { k -> if (m.isNull(k)) null else m.getLong(k) }
            }
            marks = o.getJSONObject("marks").let { m -> m.keys().asSequence().associateWith { k -> m.getBoolean(k) } }
            if (stored == today) {
                done = ids
            } else {
                // Nouveau jour : on archive la journée passée (validées ou non) et on verrouille son XP.
                val past = LinkedHashMap(marks)
                habits.forEach { h -> past["${h.id}|$stored"] = h.id in ids }
                marks = past
                xpBase += habits.filter { it.id in ids }.sumOf { it.xp }
                // Quêtes validées en avance pour aujourd'hui.
                done = habits.filter { marks["${it.id}|$today"] == true }.map { it.id }.toSet()
                doneDate = today
            }
        } catch (e: Exception) {
            // Sauvegarde illisible : on garde les valeurs de démo.
        }
    }

    private fun habitToJson(h: Habit) = JSONObject()
        .put("id", h.id).put("cat", h.cat).put("rank", h.rank).put("t", h.t).put("s", h.s)
        .put("xp", h.xp).put("streak", h.streak).put("wk", h.wk)
        .put("freqType", h.freq.type).put("freqDays", JSONArray(h.freq.days)).put("freqTimes", h.freq.times)

    private fun habitFromJson(o: JSONObject): Habit {
        val days = o.getJSONArray("freqDays").let { a -> (0 until a.length()).map { a.getInt(it) } }
        return Habit(
            o.getString("id"), o.getString("cat"), o.getString("rank"), o.getString("t"), o.getString("s"),
            o.getInt("xp"), o.getInt("streak"), o.getInt("wk"),
            Freq(o.getString("freqType"), days, o.getInt("freqTimes")),
        )
    }

    private fun achToJson(a: Ach) = JSONObject()
        .put("id", a.id).put("n", a.n).put("d", a.d).put("art", a.art).put("p", a.p).put("g", a.g)
        .put("ago", a.ago ?: JSONObject.NULL)

    private fun achFromJson(o: JSONObject) = Ach(
        o.getString("id"), o.getString("n"), o.getString("d"), o.getString("art"),
        o.getInt("p"), o.getInt("g"), if (o.isNull("ago")) null else o.getInt("ago"),
    )
}

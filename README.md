# Habitz — Habit Tracker RPG

Application Android native (Kotlin + Jetpack Compose) : un suivi d'habitudes au style dark fantasy RPG, thème violet, brouillard et particules animés.

## Fonctionnalités
- **Accueil** : bande de dates (glisser / calendrier), niveau et XP, progression de la semaine, quêtes classées par catégorie, ignorer / décaler une quête, validation des jours passés ou à venir (sans XP).
- **Quêtes** : création / modification / suppression, catégories (15 badges), difficulté, fréquence (chaque jour, jours précis, par semaine).
- **Stats** : taux de complétion par période, courbe, répartition par catégorie, succès récents, calendrier de chaleur.
- **Profil** : nom du ranger, export de la sauvegarde (JSON), suppression du profil, succès (consultation, progression, ajout).
- Sauvegarde locale automatique (SharedPreferences).

## Build
- JDK 17, Android Studio récent (AGP 8.7, Kotlin 2.0), minSdk 26.
- GitHub Actions (`.github/workflows/build.yml`) construit l'APK debug à chaque push sur `main`.

Aucun compte, serveur ni accès Internet requis.

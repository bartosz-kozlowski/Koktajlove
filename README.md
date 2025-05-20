# 🍹 Koktajlove – Twój osobisty asystent barmański

Aplikacja mobilna napisana w Kotlinie z użyciem **Jetpack Compose**, **Firebase** i **Gemini API (AI)**. Pozwala przeglądać koktajle, zarządzać ulubionymi, a także generować nowe przepisy na podstawie podanych składników.

## 📱 Funkcje aplikacji

- ✅ Lista koktajli z podziałem na kategorie (alkoholowe / bezalkoholowe)
- ✅ Szczegóły koktajlu: obraz, składniki, sposób przygotowania
- ✅ Obsługa ulubionych koktajli z zapisem w Firebase Firestore
- ✅ Integracja z **Gemini AI** – generowanie przepisu na podstawie składników
- ✅ Tryb tabletowy z widokiem split-screen
- ✅ Obsługa logowania (Firebase Auth)
- ✅ Obsługa offline (komunikaty o braku Internetu)

## 🧠 Technologia

- **Język:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Baza danych:** Firebase Firestore
- **AI:** Gemini API (Google Generative AI)

## 🔐 Konfiguracja klucza API (Gemini)

1. W katalogu głównym projektu stwórz plik `local.properties`:

    ```properties
    GEMINI_API_KEY=tu_wklej_swoj_klucz
    ```

2. Klucz zostanie automatycznie wczytany do `BuildConfig.GEMINI_API_KEY` i użyty przez aplikację.

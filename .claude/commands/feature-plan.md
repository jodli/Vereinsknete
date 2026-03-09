Du planst ein neues Feature fuer die YogaKnete Android App (Single-User Yoga-Rechnungs-App, deutsche UI).

## Feature-Idee

$ARGUMENTS

## Vorbereitung

1. Lies `CLAUDE.md` fuer den Projekt-Ueberblick
2. Lies die Guidelines in `android/docs/`:
   - `android/docs/ux-guidelines.md`
   - `android/docs/design-guidelines.md`
   - `android/docs/architecture-guidelines.md`

## Phase 1: Experten-Research

Starte **drei parallele Sub-Agents** (Agent tool). Jeder Experte recherchiert die Codebase, liest seine Guideline, bewertet das Feature und gibt konkrete Empfehlungen.

### Usability-Experte
- Liest `android/docs/ux-guidelines.md`
- Recherchiert bestehende Screens und Flows in `presentation/screens/`
- Bewertet: Intuitiv fuer eine einzelne Nutzerin? Minimale Schritte? Passt zum bestehenden Flow?
- Prueft: Dialog-Typen, Titel, Aktions-Gruppierung, destruktive Aktionen
- Schlaegt Guideline-Ergaenzungen vor wenn sinnvoll (knapp halten, kuerzen wenn zu lang)

### Design-Experte
- Liest `android/docs/design-guidelines.md`
- Recherchiert Theme (`ui/theme/`), bestehende Screens, Material 3 Nutzung
- Bewertet: Visuell konsistent? Material 3 konform? Deutsche UI-Texte passend?
- Prueft: Farben, Icons, Spacing, Typografie, visuelle Hierarchie
- Schlaegt Guideline-Ergaenzungen vor wenn sinnvoll (knapp halten, kuerzen wenn zu lang)

### Architektur-Experte
- Liest `android/docs/architecture-guidelines.md` und `CLAUDE.md`
- Recherchiert: DI Module, Repositories, DAOs, ViewModels, Worker, Services
- Bewertet: Passt zur MVVM + Clean Architecture? Bestehende Patterns wiederverwendbar?
- Prueft: Dependency Rule, State Management, Data Flow, DI, Navigation
- Schlaegt Guideline-Ergaenzungen vor wenn sinnvoll (knapp halten, kuerzen wenn zu lang)

## Phase 2: Feature-Plan erstellen

Fasse die Ergebnisse aller drei Experten in einem uebersichtlichen Plan zusammen:

```
## Feature: [Name]

### Was
[1-2 Saetze]

### Warum
[Mehrwert fuer die Nutzerin]

### Wie (Uebersicht)
- Neue/geaenderte Dateien und Komponenten
- Implementierungsphasen mit Abhaengigkeiten

### Usability-Bewertung
[Zusammenfassung + konkrete Empfehlungen]

### Design-Bewertung
[Zusammenfassung + konkrete Empfehlungen]

### Architektur-Bewertung
[Zusammenfassung + konkrete Empfehlungen]

### Offene Fragen
[Falls vorhanden]
```

Zeige den Plan dem User und warte auf Feedback.

## Phase 3: Iteration

**KRITISCH - Experten bleiben aktiv durch den gesamten Planungsprozess:**

Bei JEDER Iterations-Runde:
1. User gibt Feedback oder stellt Fragen
2. Aktualisiere den Plan basierend auf dem Feedback
3. Starte die drei Experten-Agents ERNEUT (parallel, via Agent tool) mit dem aktualisierten Plan
4. Jeder Experte bewertet die Aenderungen aus seiner Perspektive und gibt neues Feedback
5. Zeige den aktualisierten Plan MIT den neuen Experten-Meinungen

Die Experten sind nicht nur fuer die initiale Bewertung da — sie reviewen jede Aenderung am Plan!
Wiederhole bis der User den Plan explizit abnimmt.

## STOP-Regel

KEINE Implementierung und KEIN Code bevor der Plan nicht explizit vom User abgenommen wurde.
Erst bei klarer Freigabe ("passt", "umsetzen", "plan steht", o.ae.) geht es weiter.

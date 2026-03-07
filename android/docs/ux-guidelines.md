# UX-Richtlinien — YogaKnete

Praktische UX-Regeln für Dialoge, Aktionen und Interaktionsmuster in der App.
Basiert auf [Material Design 3](https://m3.material.io/) und den bestehenden Patterns im Projekt.

---

## 1. Dialog-Typen: Wann was verwenden

### Entscheidungshilfe

```
Brauche ich eine Bestätigung (Ja/Nein)?           → AlertDialog
Hat der Nutzer 3+ Aktionen zur Auswahl?            → ModalBottomSheet
Ist es ein Formular mit 2–4 Feldern?               → AlertDialog
Ist es ein Formular mit 5+ Feldern oder Scrolling? → Full-Screen Dialog
```

### AlertDialog

Für **kurze Entscheidungen** (max. 2 Buttons) und **einfache Formulare** (3–4 Felder).

**Regeln:**
- Max. 2 Action-Buttons: `dismissButton` (links) + `confirmButton` (rechts)
- Wenn mehr als 2 Aktionen nötig → ModalBottomSheet verwenden
- Beispiele: `AddClassDialog`, `EditClassDialog`, Löschbestätigungen

### ModalBottomSheet

Für **Aktionsmenüs** mit 3+ Optionen, die sich auf ein angetipptes Element beziehen.

**Regeln:**
- `skipPartiallyExpanded = true` bei Aktionsmenüs
- `ListItem`-Composables für konsistente Touch-Targets
- Destruktive Aktionen am Ende, durch `HorizontalDivider` getrennt
- Unten genug Padding für die Navigationsleiste

### Full-Screen Dialog (`Dialog` + `Scaffold`)

Für **komplexe Formulare** mit vielen Feldern und ggf. Scrolling (z.B. `StudioEditDialog`).

**Regeln:**
- `DialogProperties(usePlatformDefaultWidth = false)`
- `TopAppBar` mit Schließen-Icon (X) und Speichern-Button
- Scrollable Content via `verticalScroll`

---

## 2. Dialog-Titel

- **Bestätigungen:** Frage-Form — `"Studio löschen?"`, `"Kurs absagen?"`
- **Formulare (Neu):** Substantiv — `"Neuer Yoga-Kurs"`, `"Neue Vorlage"`
- **Formulare (Bearbeiten):** Verb + Objekt — `"Kurs bearbeiten"`, `"Vorlage bearbeiten"`
- **Aktionsmenüs:** Kein Titel nötig, oder Objekt-Name als Headline
- **Info/Statistik:** Beschreibend — `"Wochenübersicht"`, `"Monatsübersicht"`

**Wichtig:** Der Nutzer muss aus Titel + Buttons allein verstehen, was passiert. Keine generischen Titel wie `"Warnung"` oder `"Achtung"`.

---

## 3. Destruktive Aktionen

- **Immer sichtbar**, unabhängig vom Status des Elements
- **Position:** Am Ende der Aktionsliste, durch `HorizontalDivider` visuell getrennt
- **Farbe:** Icon + Text in `MaterialTheme.colorScheme.error`
- **Bestätigungsdialog** für jede irreversible Aktion:

```
Titel:  "[Element] löschen?"
Text:   "Möchtest du [Name] wirklich löschen?
         Diese Aktion kann nicht rückgängig gemacht werden."
Confirm: "Löschen" (error-Farbe)
Dismiss: "Abbrechen"
```

**Referenz:** `StudiosManagementScreen` — sauberes DropdownMenu → Bestätigung → Aktion.

---

## 4. Aktions-Gruppierung

### In Aktionsmenüs (BottomSheet / DropdownMenu)

Aktionen nach Kategorie gruppieren, mit `HorizontalDivider` zwischen Gruppen:

```
1. Primäre Aktionen (häufigste zuerst)
   ─────────────────────────────────
2. Sekundäre Aktionen
   ─────────────────────────────────
3. Destruktive Aktionen (immer zuletzt)
```

### In AlertDialog-Buttons

- Max. 2 Buttons: Dismiss (links) + Confirm (rechts)
- Wenn Confirm destruktiv → error-Farbe
- Button-Text = konkretes Verb (`"Löschen"`, `"Speichern"`) — nicht `"OK"` oder `"Ja"`

---

## 5. Auswahloptionen im Formular

### Entscheidungshilfe

```
2–3 Optionen, immer sichtbar, kurze Labels?  → SingleChoiceSegmentedButtonRow
4+ Optionen oder lange Labels?                → ExposedDropdownMenuBox (Dropdown)
Boolean (An/Aus)?                             → Switch
```

### Dropdown (`ExposedDropdownMenuBox`)

Standard für Auswahllisten. Bestehende Beispiele: Studio-Auswahl, Wochentag-Auswahl.

**Regeln:**
- `OutlinedTextField` mit `readOnly = true` und `label`
- `.menuAnchor(MenuAnchorType.PrimaryNotEditable)` setzen
- `ExposedDropdownMenuDefaults.TrailingIcon` als Icon
- Immer einen sinnvollen Default vorbelegen

### Segmented Button (`SingleChoiceSegmentedButtonRow`)

Für **wenige gleichwertige Optionen** (2–3), die auf einen Blick sichtbar sein sollen.

**Regeln:**
- Max. 3 Segmente (bei 4+ wird's zu eng)
- Kurze Labels (1–2 Wörter)
- Gleiche Breite pro Segment
- Unter einem `label`-Text wie bei anderen Formularfeldern platzieren
- Häufigster Wert als Default vorauswählen

### Switch

Für **Boolean-Optionen** (An/Aus). Bestehend: Auto-Schedule Toggle.

---

## 6. Defaults & Progressive Disclosure

- **Sinnvolle Defaults:** Jedes Formularfeld hat einen Default-Wert, der den häufigsten Anwendungsfall abdeckt
- **Weniger ist mehr:** Nur Felder anzeigen, die der Nutzer regelmäßig braucht. Seltene Optionen hinter "Erweitert" oder als Sekundär-Option verstecken
- **Opt-in statt Opt-out:** Erweiterte Features (wie Auto-Schedule) standardmäßig aus, der Nutzer aktiviert sie bewusst

---

## 7. Checkliste für neue Dialoge

- [ ] Richtiger Dialog-Typ? (AlertDialog / BottomSheet / Full-Screen)
- [ ] Titel passt zu den verfügbaren Aktionen?
- [ ] Max. 2 Buttons bei AlertDialog?
- [ ] Button-Text = konkretes Verb?
- [ ] Destruktive Aktionen am Ende, mit Divider getrennt, in error-Farbe?
- [ ] Bestätigungsdialog für irreversible Aktionen?
- [ ] Sinnvolle Defaults für alle Felder?
- [ ] Dismiss links, Confirm rechts?

---

*Quellen: [M3 Dialogs](https://m3.material.io/components/dialogs/guidelines), [M3 Bottom Sheets](https://m3.material.io/components/bottom-sheets/guidelines), [M3 Segmented Buttons](https://m3.material.io/components/segmented-buttons/guidelines)*

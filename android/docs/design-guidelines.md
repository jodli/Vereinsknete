# YogaKnete Design Guidelines

Visuelle Richtlinien für konsistente UI-Komponenten. Kompakt gehalten — bei Fragen: Codebase als Referenz nutzen.

---

## 1. Farbsystem

### Theme (Material 3, Lila/Purple-basiert)

| Rolle                | Light                      | Dark                       |
|----------------------|----------------------------|----------------------------|
| `primary`            | `#6B4EE6` (YogaPurple)    | `#9C86FF` (YogaPurple60)  |
| `primaryContainer`   | `#EFE9FF` (YogaPurple90)  | `#3B2B5B` (YogaPurple30)  |
| `surface`            | `#FFFFFF`                  | `#241B3D` (YogaPurple20)  |
| `surfaceVariant`     | `#F7F4FF` (YogaPurple95)  | `#3B2B5B` (YogaPurple30)  |
| `error`              | `#D32F2F`                  | gleich                     |
| `secondary`          | `#FFB300` (YogaGold)      | gleich                     |

### Status-Farben

| Status    | Farbe              | Container              | Icon                        |
|-----------|--------------------|-----------------------|-----------------------------|
| SCHEDULED | `onSurfaceVariant` | `surfaceVariant`      | `Icons.Outlined.Event`      |
| COMPLETED | `primary`          | `primaryContainer`    | `Icons.Default.CheckCircle` |
| CANCELLED | `error`            | `errorContainer`      | `Icons.Default.Cancel`      |

**Regel:** Farben immer aus `MaterialTheme.colorScheme.*`, nie direkt als Hex/Constant in Compose.

### Auswahl-Zustand (Selected State)

- **Aktiv:** Container `alpha = 0.6f` + `BorderStroke(2.dp, themeColor)` + `FontWeight.Bold` + Checkmark
- **Inaktiv:** Container `alpha = 0.15f`, kein Border, normaler Font

---

## 2. Komponenten

### Dialog-Typen

| Typ               | Composable        | Wann                                    |
|-------------------|-------------------|------------------------------------------|
| Einfacher Dialog  | `AlertDialog`     | Bestätigungen, kurze Formulare           |
| Formular-Dialog   | `Dialog` + `Card` | Komplexe Formulare (5+ Felder)          |
| Datepicker        | `DatePickerDialog` | Datumsauswahl                           |

Formular-Dialoge: `fillMaxWidth(0.95f)` + `fillMaxHeight(0.85f)` + `TopAppBar` mit Close + Speichern.

### Cards als Aktions-Buttons

Für auswählbare Aktionen (z.B. Status-Buttons): `Card(onClick = ...)` mit Status-Farbe.

```kotlin
Card(
    onClick = { ... },
    colors = CardDefaults.cardColors(
        containerColor = statusColor.copy(alpha = if (selected) 0.6f else 0.15f)
    ),
    border = if (selected) BorderStroke(2.dp, accentColor) else null
)
// Innerer Aufbau: Row(16.dp) { Icon + Spacer(16) + Text(titleMedium) + if(selected) Checkmark }
```

### Destruktive Aktionen

- Visuell abgetrennt durch `HorizontalDivider(padding(vertical = 8.dp))`
- `TextButton` mit `error`-Farbe, nie als Card
- Icon (`Delete`, 18.dp) + Text kombiniert

### Segmented Buttons

Für 2–3 gleichwertige Optionen (z.B. Wiederholungsintervall):

```kotlin
SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
    options.forEachIndexed { index, option ->
        SegmentedButton(
            selected = selectedIndex == index,
            onClick = { onSelect(index) },
            shape = SegmentedButtonDefaults.itemShape(index, options.size)
        ) {
            Text(option.label)
        }
    }
}
```

**Regeln:**
- Max. 3 Segmente, kurze Labels
- Über dem SegmentedButton ein `Text(label, style = bodySmall)` als Feldbezeichnung
- Gleiche vertikale Abstände wie andere Formularfelder

### Dropdown

Standard für 4+ Optionen. Bestehende Referenzen: Studio-Dropdown, Wochentag-Dropdown in `TemplateEditDialog`.

---

## 3. Icons

| Aktion       | Icon                              | Stil     |
|--------------|-----------------------------------|----------|
| Durchgeführt | `CheckCircle`/`CheckCircleOutline`| filled/outlined |
| Ausgefallen  | `Cancel`                          | filled   |
| Bearbeiten   | `Edit`                            | filled   |
| Löschen      | `Delete`                          | filled   |
| Schließen    | `Close`                           | filled   |
| Hinzufügen   | `Add`                             | filled   |
| Datum        | `Event`                           | outlined |
| Uhrzeit      | `Schedule`                        | outlined |
| Dauer        | `Timer`                           | filled   |
| Wiederholung | `Repeat`                          | filled   |

- **Filled** für aktive Zustände, **Outlined** für inaktive/sekundäre
- Icon-Tint folgt Status: `primary` (positiv), `error` (negativ), `onSurfaceVariant` (neutral)

---

## 4. Spacing

| Element                          | Wert    |
|----------------------------------|---------|
| Dialog-Content Padding           | 16.dp   |
| Zwischen Gruppen (vertikal)      | 16.dp   |
| Innerhalb Gruppen (vertikal)     | 8.dp (`spacedBy(8.dp)`) |
| Zwischen Formularfeldern         | 12.dp (`spacedBy(12.dp)`) |
| Card-Innenpadding                | 12–16.dp |
| Icon-zu-Text (Action-Cards)      | 16.dp   |
| Icon-zu-Text (Buttons)           | 8.dp    |
| Divider-Padding                  | `padding(vertical = 8.dp)` |

---

## 5. Typografie in Dialogen

| Ebene         | Style           | Zusatz             |
|---------------|----------------|--------------------|
| Dialog-Titel  | `headlineSmall` | —                  |
| Section-Header| `titleMedium`   | `FontWeight.Bold`  |
| Aktions-Text  | `titleMedium`   | —                  |
| Body/Details  | `bodyMedium`    | —                  |
| Labels        | `labelLarge`    | —                  |
| Feld-Label    | `bodySmall`     | `onSurfaceVariant` |
| Hint/Caption  | `bodySmall`     | `alpha = 0.7f`     |

---

## 6. Allgemeine Regeln

- Keine hardcodierten Farben — immer `MaterialTheme.colorScheme.*`
- Alpha-Werte: `0.15f` (inaktiv), `0.3f` (neutral), `0.6f` (aktiv)
- Deutsche Labels in der UI, englische Kommentare im Code
- `HorizontalDivider` als Trenner zwischen Aktionsgruppen, nie zwischen gleichwertigen Aktionen
- Selektionszustand: Border + Bold + Checkmark (3 visuelle Signale)

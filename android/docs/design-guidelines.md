# Design Guidelines — YogaKnete

Visual guidelines for consistent UI components and dialogs.

---

## 1. Color System

### Theme
Purple-based Material 3 theme with light and dark variants.

### Status Colors

| Status | Color | Container | Icon style |
|--------|-------|-----------|------------|
| SCHEDULED | onSurfaceVariant | surfaceVariant | Outlined |
| COMPLETED | primary | primaryContainer | Filled |
| CANCELLED | error | errorContainer | Filled |

**Rules:**
- Always use `MaterialTheme.colorScheme`, never hardcoded colors
- Semantic colors (YogaSuccess, YogaError) only for HTML generation, not in Compose

### Selected State
- **Active:** Container alpha 0.6 + border (2dp) + bold + checkmark icon
- **Inactive:** Container alpha 0.15, no border, normal font weight

---

## 2. Components

### Dialog Types

| Type | When |
|------|------|
| AlertDialog | Action selection, confirmations, short forms |
| Dialog + Card | Complex forms with TopAppBar (X left, Save right) |

### Cards as Action Buttons
For selectable actions (status buttons), use `Card(onClick)`:
- Icon left, action text center, checkmark right (when active)
- Container color by status with alpha transparency
- Border only in active state

### Segmented Buttons
For 2–3 equal options (e.g., recurrence interval):

```kotlin
Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text("Label", style = bodySmall, color = onSurfaceVariant)
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = selectedIndex == index,
                onClick = { onSelect(index) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size)
            ) { Text(option.label) }
        }
    }
}
```

Max 3 segments, short labels, same spacing as other form fields.

### Dropdown
Standard for 4+ options. `ExposedDropdownMenuBox` with `OutlinedTextField(readOnly = true)`.

### Destructive Actions
- Visually separated by `HorizontalDivider`
- `TextButton` with error color, never as Card
- Icon (18dp) + text combined

---

## 3. Icons

| Concept | Icon | Style |
|---------|------|-------|
| Completed | CheckCircle / CheckCircleOutline | Filled / Outlined |
| Cancelled | Cancel | Filled |
| Edit | Edit | Filled |
| Delete | Delete | Filled |
| Close | Close | Filled |
| Add | Add | Filled |
| Date | Event | Outlined |
| Time | Schedule | Outlined |
| Duration | Timer | Filled |
| Recurrence | Repeat | Filled |

- **Filled** for active states, **Outlined** for inactive/secondary
- Tint follows semantics: primary (positive), error (negative), onSurfaceVariant (neutral)

---

## 4. Spacing

| Element | Value |
|---------|-------|
| Between groups | 16dp |
| Within groups | 8dp |
| Form fields | 12dp spacing |
| Card inner padding | 12–16dp |
| Icon to text | 16dp (cards), 8dp (buttons) |
| Divider padding | 8dp vertical |

---

## 5. Typography

| Level | Style | Usage |
|-------|-------|-------|
| Dialog title | headlineSmall | Main heading |
| Section header | titleMedium + Bold | Form sections |
| Action text | titleMedium | Card buttons |
| Details | bodyMedium | Descriptions, info |
| Field label | bodySmall + onSurfaceVariant | Above SegmentedButtons |
| Hint/caption | bodySmall + 0.7 alpha | Timestamps, hints |

---

## 6. Visual Hierarchy for Actions

Three tiers in dialogs:
1. **Primary** — Colored cards with status semantics (top)
2. **Secondary** — Neutral cards in surfaceVariant (middle)
3. **Destructive** — HorizontalDivider + TextButton in error color (bottom)

---

## 7. General Rules

- No hardcoded colors — always `MaterialTheme.colorScheme.*`
- Alpha values: 0.15 (inactive), 0.3 (neutral), 0.6 (active)
- German labels in UI, English comments in code
- HorizontalDivider between action groups only, never between equivalent actions
- Selection state: border + bold + checkmark (3 signals)

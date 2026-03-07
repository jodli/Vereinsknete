# UX Guidelines — YogaKnete

Practical UX rules for dialogs, actions, and interaction patterns.
Based on Material Design 3 and established patterns in the project.

---

## 1. Dialog Types

| Type | When to use |
|------|-------------|
| **AlertDialog** | Confirmations (yes/no), simple forms (2–4 fields), info dialogs |
| **Full-screen Dialog** | Complex forms with 5+ fields and scrolling |

**Decision guide:**
- Confirmation or short form? → AlertDialog
- Form with 5+ fields? → Full-screen Dialog with TopAppBar (X + Save)

**Rules:**
- AlertDialog: Max 2 buttons — Cancel (left) + Confirm (right)
- Full-screen Dialog: Close icon (X) left, Save button right in TopAppBar

---

## 2. Dialog Titles

| Context | Title form | Example |
|---------|-----------|---------|
| Confirmation/decision | Question | "Studio löschen?" |
| Create new element | Noun | "Neuer Yoga-Kurs" |
| Edit element | Verb + object | "Kurs bearbeiten" |
| Action menu | Neutral label | "Kurs-Aktionen" |
| Info/statistics | Descriptive | "Wochenübersicht" |

- Title must match **all** available actions in the dialog
- User should understand what happens from title + buttons alone
- No generic titles like "Warnung" or "Achtung"

---

## 3. Destructive Actions (Delete)

- Delete is **always visible** — never hidden behind state conditions
- Position: End of action list, visually separated by divider
- Color: Icon and text in error color
- Every irreversible action **must** have a confirmation dialog:
  - Title: "[Element] löschen?"
  - Text: "Möchtest du [name] wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden."
  - Confirm button in error color with concrete verb ("Löschen")

---

## 4. Action Grouping

Group actions by category with dividers between groups:

1. **Primary actions** — most frequent first (e.g., status changes)
2. **Secondary actions** — edit, reschedule
3. **Destructive actions** — always last, visually separated

**Dialog buttons:** Max 2 — Cancel (left) + Confirm (right). Concrete verbs only ("Löschen", "Speichern"), never "OK" or "Ja". Destructive confirms in error color.

---

## 5. Selection Options in Forms

| Scenario | Component |
|----------|-----------|
| 2–3 options, short labels, always visible | `SingleChoiceSegmentedButtonRow` |
| 4+ options or long labels | `ExposedDropdownMenuBox` (Dropdown) |
| Boolean on/off | `Switch` |

**SegmentedButton rules:**
- Max 3 segments, 1–2 word labels
- "Wiederholung" label in `bodySmall` above the row
- Pre-select the most common option as default

**Dropdown rules:**
- `OutlinedTextField` with `readOnly = true` + label
- `.menuAnchor(MenuAnchorType.PrimaryNotEditable)`
- Always pre-fill a sensible default

---

## 6. Defaults & Progressive Disclosure

- Every form field has a default covering the most common case
- Only show fields the user needs regularly — hide rare options
- Advanced features (auto-schedule) off by default, user opts in

---

## 7. Checklist for New Dialogs

- [ ] Correct dialog type? (AlertDialog / Full-screen)
- [ ] Title matches all available actions?
- [ ] Max 2 buttons for AlertDialog?
- [ ] Button text = concrete verb?
- [ ] Destructive actions at end, separated, in error color?
- [ ] Confirmation dialog for irreversible actions?
- [ ] Sensible defaults for all fields?

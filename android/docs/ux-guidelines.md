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

### Visibility
- Delete is **always visible** — never hidden behind state conditions
- Position: End of action list, visually separated by divider
- Color: Icon and text in error color

### Confirmation
Every destructive, irreversible action **must** have a confirmation dialog:
- Title: "[Element] löschen?"
- Text: "Möchtest du [concrete name] wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden."
- Confirm button in error color with concrete verb ("Löschen")
- Cancel button

---

## 4. Action Grouping

Group actions by category, with dividers between groups:

1. **Primary actions** — most frequent first (e.g., status changes)
2. **Secondary actions** — edit, reschedule
3. **Destructive actions** — always last, visually separated

### Dialog Buttons
- Max 2 buttons: Cancel (left) + Confirm (right)
- Destructive confirmations in error color
- Button text = concrete verb ("Löschen", "Speichern") — never "OK" or "Ja"

---

## 5. Checklist for New Dialogs

- [ ] Correct dialog type? (AlertDialog / Full-screen)
- [ ] Title matches all available actions?
- [ ] Max 2 buttons for AlertDialog?
- [ ] Button text = concrete verb?
- [ ] Destructive actions at the end, separated by divider?
- [ ] Destructive actions in error color?
- [ ] Confirmation dialog for irreversible actions?
- [ ] Confirmation text names concrete element + consequence?
- [ ] Sensible defaults for all fields?

---

## 8. Notifications

- **Content:** Title provides context (class name + studio), body is a short question with the start time
- **Tap behavior:** Opens the relevant existing dialog — no new UI patterns for notification actions
- **Importance:** IMPORTANCE_DEFAULT — the app is a tool, not a messenger. No sound, no vibration
- **No retry:** Ignored notifications are not resent. The user can still update status manually
- **Channel naming:** German, descriptive (e.g., "Kurs-Erinnerungen")
- **Precondition check:** Always verify the current state before showing a notification (e.g., is the class still SCHEDULED?)

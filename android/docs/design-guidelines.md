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
For selectable actions (status buttons), use Card with onClick:
- Icon left, action text center, checkmark right (when active)
- Container color by status with alpha transparency
- Border only in active state

### Destructive Actions
- Always visually separated by HorizontalDivider
- TextButton with error color, never as Card
- Icon (18dp) + text combined

### Standard Buttons
- Confirm: right side of dialog
- Cancel: left side of dialog
- Form dialogs: Save in TopAppBar

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

**Rules:**
- Filled for active states and primary actions
- Outlined for inactive states and secondary info
- Icon tint follows semantics: primary (positive), error (negative), onSurfaceVariant (neutral)

---

## 4. Spacing

| Element | Value |
|---------|-------|
| Between groups | 16dp |
| Within groups | 8dp |
| Card inner padding | 12–16dp |
| Icon to text | 16dp (cards), 8dp (buttons) |
| Divider padding | 8dp vertical |
| Form fields | 16dp spacing |

---

## 5. Typography

| Level | Style | Usage |
|-------|-------|-------|
| Dialog title | headlineSmall | Main heading |
| Section header | titleMedium + Bold | Form sections |
| Action text | titleMedium | Card buttons |
| Details | bodyMedium | Descriptions, info |
| Hint/caption | bodySmall + 0.7 alpha | Timestamps, hints |

---

## 6. Visual Hierarchy for Actions

Three tiers in dialogs:

1. **Primary actions** — Colored cards with status semantics (top)
2. **Secondary actions** — Neutral cards in surfaceVariant (middle)
3. **Destructive actions** — HorizontalDivider + TextButton in error color (bottom)

---

## 7. General Rules

- No hardcoded colors — always use MaterialTheme
- Alpha values: 0.15 (inactive), 0.3 (neutral), 0.6 (active)
- German labels in UI, English comments in code
- HorizontalDivider only between action groups, never between equivalent actions
- Selection state always communicated via 3 signals: border + bold + checkmark

---

## 8. Notifications

| Property | Value |
|----------|-------|
| Small Icon | `ic_yoga_pose`, tinted by system |
| Accent color | YogaPrimary (#6B4EE6) via `setColor()` |
| Large Icon | None |
| Importance | DEFAULT (silent) |
| AutoCancel | Always true |
| Tap action | Deep-link to relevant screen/dialog |
| Channel naming | German, descriptive (e.g., "Kurs-Erinnerungen") |

**Rules:**
- No custom layouts — use standard `NotificationCompat.Builder` templates
- Accent color via `setColor()`, never hardcoded in notification layout
- Always set `setAutoCancel(true)` so the notification dismisses on tap

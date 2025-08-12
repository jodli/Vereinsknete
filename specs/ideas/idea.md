# Feature Ideas

## Batch Input for Sessions

### Problem
Currently, session input requires individual form entry for each session. When a user needs to input multiple sessions (10-20 at once), they have to:
- Repeatedly select the same client
- Enter date, start time, and end time for each session
- Risk making mistakes during repetitive data entry

### Proposed Solution
Add a batch input feature that allows users to input multiple sessions using a simple text format:

```
client: clientA
04.05.2025 06:00 PM 07:00 PM Yoga Session
07.05.2025 06:00 PM 07:00 PM Yoga Session
12.05.2025 06:00 PM 07:00 PM Yoga Session
23.05.2025 06:00 PM 07:00 PM Yoga Session
```

### Format Specification
- **Client declaration**: `client: [client_name]` - sets the client for all following sessions
- **Session format**: `DD.MM.YYYY HH:MM AM/PM HH:MM AM/PM [Description]`
  - Date in German format (DD.MM.YYYY)
  - Start time in 12-hour format with AM/PM
  - End time in 12-hour format with AM/PM
  - Optional description/notes

### Benefits
- Significantly faster input for multiple sessions
- Reduced risk of data entry errors
- More efficient workflow for bulk session creation
- Maintains existing individual session entry for single sessions

### Implementation Considerations
- Add new "Batch Input" button/tab on sessions page
- Text area for batch input with format validation
- Preview of parsed sessions before confirmation
- Error handling for invalid formats
- Integration with existing session creation API
- Support for multiple clients in same batch (multiple client: declarations)

### User Experience
1. User clicks "Batch Input" on sessions page
2. Text area opens with format example/help
3. User pastes or types session data
4. System parses and shows preview of sessions to be created
5. User confirms and sessions are created in bulk
6. Success message with count of created sessions

## Alternative UI Approaches

### 1. Spreadsheet-Style Grid Input
**Concept**: Excel-like table interface for rapid data entry
- Editable grid with columns: Date, Start Time, End Time, Description, Client
- Tab navigation between cells for quick input
- Copy/paste functionality from actual spreadsheets
- Auto-fill down for repeated values (client, time patterns)
- Keyboard shortcuts (Ctrl+D to duplicate, Enter to move down)

**Pros**:
- Familiar interface for users comfortable with Excel
- Very fast data entry with keyboard navigation
- Easy to see all sessions at once
- Can handle complex patterns and variations

**Cons**:
- More complex to implement
- Requires good keyboard UX design
- May be overwhelming for simple use cases

### 2. Smart Duplication Form
**Concept**: Enhanced single session form with intelligent duplication
- Standard session form with "Add Another" button
- Form remembers previous inputs (client, times, description)
- Quick-change buttons for common variations (different dates, times)
- "Duplicate with Changes" modal for pattern-based creation

**Pros**:
- Builds on existing familiar form UI
- Gradual learning curve
- Less risk of bulk errors

**Cons**:
- Still somewhat repetitive
- Not as fast as other approaches for large batches

### 3. Template-Based Quick Entry
**Concept**: Pre-defined session templates for common patterns
- Create reusable templates (e.g., "Weekly Yoga - Monday 6PM")
- Apply template to multiple dates via calendar picker
- Template includes client, duration, description, default times
- Quick modify template values before applying

**Pros**:
- Perfect for recurring sessions
- Minimal typing required
- Reduces errors through standardization

**Cons**:
- Requires initial template setup
- Less flexible for one-off variations

### 4. Calendar-Based Batch Input
**Concept**: Visual calendar interface for session placement
- Month/week view calendar
- Click dates to open quick-add session modal
- Drag-and-drop to copy sessions to other dates
- Multi-select dates for bulk session creation
- Visual indicators for existing sessions

**Pros**:
- Very intuitive and visual
- Great for planning around existing sessions
- Easy to see schedule conflicts

**Cons**:
- More development complexity
- Mobile experience challenges
- May require multiple clicks per session

### 5. Hybrid Quick-Entry Modal
**Concept**: Streamlined modal combining best of multiple approaches
- Client selection at top (sticky for batch)
- Quick date picker with "next occurrence" shortcuts
- Time slots as clickable buttons (6PM-7PM, 7PM-8PM, etc.)
- Description auto-complete from previous sessions
- "Add Another" keeps modal open with smart defaults

**Pros**:
- Balance of speed and usability
- Learns from user patterns
- Mobile-friendly
- Moderate development complexity

**Cons**:
- Still requires multiple clicks per session
- May need refinement based on usage patterns

## Recommendation

For **Phase 1**: Implement the **Hybrid Quick-Entry Modal** as it provides the best balance of user experience and development effort.

For **Phase 2**: Consider adding the **Spreadsheet-Style Grid** for power users who need to input 20+ sessions regularly.

The text-based approach could remain as a "power user" option for those who prefer keyboard-only input or need to import from external sources.

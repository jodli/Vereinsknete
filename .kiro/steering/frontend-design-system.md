# Frontend Design System Guidelines

This document outlines the design system, visual standards, and styling patterns for the VereinsKnete frontend application.

## Enhanced Color Palette

### Primary Colors
- **Brand Primary**: `blue-600` (#2563eb), `blue-700` (#1d4ed8) for main actions
- **Brand Secondary**: `indigo-600` (#4f46e5), `indigo-700` (#4338ca) for accents
- **Brand Gradient**: `from-blue-600 to-indigo-600` for hero elements

### Semantic Colors
- **Success**: `emerald-600` (#059669), `emerald-100` (#d1fae5) for positive states
- **Warning**: `amber-600` (#d97706), `amber-100` (#fef3c7) for pending states
- **Danger**: `red-600` (#dc2626), `red-100` (#fee2e2) for destructive actions
- **Info**: `sky-600` (#0284c7), `sky-100` (#e0f2fe) for informational content

### Neutral Palette
- **Text Primary**: `gray-900` (#111827) for headings and important text
- **Text Secondary**: `gray-600` (#4b5563) for body text
- **Text Muted**: `gray-500` (#6b7280) for less important text
- **Borders**: `gray-200` (#e5e7eb) for subtle borders
- **Backgrounds**: `gray-50` (#f9fafb) for page backgrounds, `white` for cards

### Status-Specific Colors
- **Paid**: `emerald-600` with `emerald-50` background
- **Pending**: `amber-600` with `amber-50` background  
- **Overdue**: `red-600` with `red-50` background
- **Draft**: `gray-600` with `gray-50` background

## Typography Scale

### Heading Hierarchy
- **Display**: `text-4xl font-bold` (36px) for hero sections
- **H1**: `text-3xl font-bold` (30px) for page titles
- **H2**: `text-2xl font-semibold` (24px) for major sections
- **H3**: `text-xl font-semibold` (20px) for subsections
- **H4**: `text-lg font-medium` (18px) for card titles

### Body Text
- **Body Large**: `text-base` (16px) for important body text
- **Body**: `text-sm` (14px) for regular content
- **Small**: `text-xs` (12px) for captions and metadata
- **Labels**: `text-sm font-medium text-gray-700` for form labels

## Advanced Layout System

### Container Patterns
```tsx
// Page container with max width
<div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">

// Card with subtle shadow and hover effect
<div className="bg-white rounded-xl shadow-sm hover:shadow-md transition-shadow border border-gray-100">

// Section spacing
<div className="space-y-8"> // Large sections
<div className="space-y-6"> // Medium sections  
<div className="space-y-4"> // Small sections
```

### Grid Systems
```tsx
// Dashboard metrics grid
<div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 xl:grid-cols-5 gap-6">

// Two-column layout with sidebar
<div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
  <div className="lg:col-span-3"> // Main content
  <div className="lg:col-span-1"> // Sidebar

// Card grid for items
<div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
```

### Spacing Scale
- **Micro**: `gap-1` (4px) for tight elements
- **Small**: `gap-2` (8px) for related items
- **Medium**: `gap-4` (16px) for form elements
- **Large**: `gap-6` (24px) for sections
- **XLarge**: `gap-8` (32px) for major sections

## Responsive Design

### Breakpoint Strategy
- **Mobile-first**: Start with mobile layout, enhance for larger screens
- **Breakpoints**: Use Tailwind's `sm:`, `md:`, `lg:`, `xl:` prefixes
- **Sidebar**: Collapsible on desktop, overlay on mobile
- **Tables**: Horizontal scroll on mobile with `overflow-x-auto`

### Grid Layouts
```tsx
<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6">
  {/* Responsive grid items */}
</div>
```

### Touch-Friendly Design
- **Minimum touch targets**: 44px for interactive elements
- **Spacing**: Adequate spacing between clickable elements
- **Hover states**: Only apply on devices that support hover

## Micro-Interactions & Animations

### Button Interactions
```tsx
// Button hover states
<button className="transform hover:scale-105 transition-transform duration-200">

// Loading states
<button className="disabled:opacity-50 disabled:cursor-not-allowed transition-opacity">
```

### Card Interactions
```tsx
// Card hover effects  
<div className="hover:shadow-lg hover:-translate-y-1 transition-all duration-300">
```

### Focus States
```tsx
// Input focus states
<input className="focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-all">
```

### Loading & Skeleton States
```tsx
// Skeleton for cards
<div className="animate-pulse">
  <div className="bg-gray-200 rounded-lg h-32 mb-4"></div>
  <div className="bg-gray-200 rounded h-4 mb-2"></div>
  <div className="bg-gray-200 rounded h-4 w-3/4"></div>
</div>

// Loading spinner
<div className="flex items-center justify-center p-8">
  <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
</div>
```

## Form Layout Patterns

### Multi-column Forms
```tsx
// Multi-column form
<form className="space-y-6">
  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
    <Input label="First Name" />
    <Input label="Last Name" />
  </div>
  
  <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
    <Input label="City" className="md:col-span-2" />
    <Input label="ZIP" />
  </div>
</form>
```

### Fieldset Grouping
```tsx
// Fieldset grouping
<fieldset className="border border-gray-200 rounded-lg p-6">
  <legend className="text-lg font-medium text-gray-900 px-2">
    Contact Information
  </legend>
  <div className="space-y-4 mt-4">
    {/* Form fields */}
  </div>
</fieldset>
```

### Advanced Input Styling
```tsx
// Input with icon
<div className="relative">
  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
    <EnvelopeIcon className="h-5 w-5 text-gray-400" />
  </div>
  <input className="pl-10 ..." />
</div>

// Input with validation states
<Input 
  label="Amount"
  type="number"
  step="0.01"
  min="0"
  rightAddon="€"
  helpText="Enter amount in euros"
  success={isValid}
  error={validationError}
/>
```

## Tailwind Configuration

### Extended Theme
```js
// tailwind.config.js
module.exports = {
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#eff6ff',
          500: '#3b82f6',
          600: '#2563eb',
          700: '#1d4ed8',
        }
      },
      animation: {
        'fade-in': 'fadeIn 0.5s ease-in-out',
        'slide-up': 'slideUp 0.3s ease-out',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%': { transform: 'translateY(10px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        }
      }
    }
  }
}
```

## Progressive Enhancement

### Mobile-First Approach
- Design for mobile, enhance for desktop
- Use progressive disclosure for complex interfaces
- Prioritize core functionality on smaller screens

### Performance Considerations
- Use CSS transforms for animations (GPU acceleration)
- Minimize layout shifts with proper sizing
- Optimize images with proper formats and sizing

## Accessibility in Design

### Color Contrast
- Ensure WCAG AA compliance (4.5:1 ratio for normal text)
- Test with color blindness simulators
- Don't rely solely on color to convey information

### Focus Indicators
- Visible focus rings for keyboard navigation
- Consistent focus styling across components
- Skip links for keyboard users

### Typography Accessibility
- Minimum 16px font size for body text
- Adequate line height (1.5 or greater)
- Sufficient color contrast for all text

This document should be referenced when working on visual design, styling, layout, or responsive behavior in the VereinsKnete application.
---
name: Editorial Desk
colors:
  surface: '#faf8ff'
  surface-dim: '#d2d9f4'
  surface-bright: '#faf8ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f3ff'
  surface-container: '#eaedff'
  surface-container-high: '#e2e7ff'
  surface-container-highest: '#dae2fd'
  on-surface: '#131b2e'
  on-surface-variant: '#444651'
  inverse-surface: '#283044'
  inverse-on-surface: '#eef0ff'
  outline: '#757682'
  outline-variant: '#c5c5d3'
  surface-tint: '#4059aa'
  primary: '#00236f'
  on-primary: '#ffffff'
  primary-container: '#1e3a8a'
  on-primary-container: '#90a8ff'
  inverse-primary: '#b6c4ff'
  secondary: '#b02d29'
  on-secondary: '#ffffff'
  secondary-container: '#ff665c'
  on-secondary-container: '#690007'
  tertiary: '#1d2b3e'
  on-tertiary: '#ffffff'
  tertiary-container: '#334155'
  on-tertiary-container: '#9eadc5'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dce1ff'
  primary-fixed-dim: '#b6c4ff'
  on-primary-fixed: '#00164e'
  on-primary-fixed-variant: '#264191'
  secondary-fixed: '#ffdad6'
  secondary-fixed-dim: '#ffb4ac'
  on-secondary-fixed: '#410002'
  on-secondary-fixed-variant: '#8e1214'
  tertiary-fixed: '#d5e3fd'
  tertiary-fixed-dim: '#b9c7e0'
  on-tertiary-fixed: '#0d1c2f'
  on-tertiary-fixed-variant: '#3a485c'
  background: '#faf8ff'
  on-background: '#131b2e'
  surface-variant: '#dae2fd'
typography:
  headline-xl:
    fontFamily: Merriweather
    fontSize: 2rem
    fontWeight: '700'
    lineHeight: 2.5rem
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Merriweather
    fontSize: 1.5rem
    fontWeight: '700'
    lineHeight: 2rem
    letterSpacing: -0.015em
  headline-md:
    fontFamily: Merriweather
    fontSize: 1.25rem
    fontWeight: '400'
    lineHeight: 1.75rem
  headline-sm:
    fontFamily: Merriweather
    fontSize: 1.125rem
    fontWeight: '400'
    lineHeight: 1.5rem
  title-md:
    fontFamily: Inter
    fontSize: 0.9375rem
    fontWeight: '600'
    lineHeight: 1.375rem
    letterSpacing: -0.01em
  title-sm:
    fontFamily: Inter
    fontSize: 0.8125rem
    fontWeight: '600'
    lineHeight: 1.25rem
    letterSpacing: 0.02em
  body-lg:
    fontFamily: Merriweather
    fontSize: 1rem
    fontWeight: '400'
    lineHeight: 1.75rem
  body-md:
    fontFamily: Inter
    fontSize: 0.875rem
    fontWeight: '400'
    lineHeight: 1.375rem
  body-sm:
    fontFamily: Inter
    fontSize: 0.75rem
    fontWeight: '400'
    lineHeight: 1.125rem
  label-md:
    fontFamily: Inter
    fontSize: 0.75rem
    fontWeight: '500'
    lineHeight: 1rem
    letterSpacing: 0.01em
  label-sm:
    fontFamily: Inter
    fontSize: 0.6875rem
    fontWeight: '600'
    lineHeight: 0.875rem
    letterSpacing: 0.04em
  mono-sm:
    fontFamily: JetBrains Mono
    fontSize: 0.6875rem
    fontWeight: '500'
    lineHeight: 0.875rem
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  gutter: 1rem
  margin: 1.25rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.75rem
  space-lg: 1.25rem
  space-xl: 1.75rem
---

## Brand & Style

This design system is tailored for a professional desktop editorial and publishing workflow application. The design language bridges classical literary craftsmanship with high-density, performance-driven desktop software patterns (influenced by modern native environments like JetBrains Fleet, Linear, Ulysses, and AtlantaFX).

### Personality & Tone
- **Authoritative & Scholarly:** Grounded in publishing heritage, typography respects long-form hierarchy and editorial precision.
- **Instrumental & Quiet:** The software behaves as an unobtrusive workspace. Chrome recedes to ensure the author’s or editor’s content remains the primary visual focal point.
- **Tactile Desktop Precision:** Optimized for high-density information architecture, sub-millisecond perceived latency, predictable keyboard navigation, and explicit visual boundaries.

### Visual Style
- **Editorial Utility Minimal:** High typographic contrast between literary serif headlines and functional utilitarian sans-serif UI elements.
- **Crisp Structural Delimitation:** Strict reliance on fine 1px structural dividers, high-contrast surface containment, subtle tonal shifts, and restrained elevation rather than diffuse blurs or exaggerated dropshadows.

## Colors

The palette establishes an authentic print-meets-terminal environment: deep ink blue as the primary executive driver, an editorial burgundy accent for alerts and expressive flags, and neutral slates that preserve visual calm during intensive multi-hour reviewing sessions.

### Application Roles
- **Canvas Base (`#F8FAFC`):** The neutral backdrop for window frames, toolbars, and inactive panels.
- **Surface Layer (`#FFFFFF`):** Work surfaces, editor viewports, modals, and data grid rows.
- **Ink Primary (`#1E3A8A` / active state `#2563EB`):** High-priority desktop interactions, selected navigation tree nodes, focus rings, and primary action triggers.
- **Editorial Accent (`#991B1B`):** Bookmarking, highlighted revisions, high-priority review notes, and critical destructive actions.
- **Structural Chrome Border (`#E2E8F0`):** Fine 1px partition between split-panes, dock headers, toolbars, and data table columns.
- **Text & Hierarchy:**
  - **Primary Ink (`#0F172A`):** Article titles, table cell primary values, and modal headings.
  - **Muted Slate (`#64748B`):** Metadata, keyboard shortcut indicators, timestamps, and column header labels.
  - **De-emphasized (`#94A3B8`):** Disabled states, structural grid rulers, and placeholder values.

### Status Tokens
- **Pending Review (Ожидает модерации):** Foreground `#D97706` | Background `#FEF3C7` | Border `#FDE68A`
- **In Review (Модерируется):** Foreground `#2563EB` | Background `#DBEAFE` | Border `#BFDBFE`
- **Published (Опубликовано):** Foreground `#059669` | Background `#D1FAE5` | Border `#A7F3D0`
- **Rejected / Blocked (Отклонено):** Foreground `#DC2626` | Background `#FEE2E2` | Border `#FECACA`

## Typography

The typographic architecture balances dual functions: literary immersion for articles and content reviews, versus strict, compact readability for high-density desktop management.

### Hierarchy & Usage
- **Merriweather (Display & Editor):** Used for document titles, publication headers, editorial content viewports, and modal review panes. It lends weight, prestige, and print authority to text content.
- **Inter (Application UI & System Controls):** Used for navigation trees, data tables, split-pane headers, toolbars, buttons, form controls, and status pills. Tight tracking, explicit apertures, and clear numeric distinctions optimize Inter for dense enterprise layouts.
- **JetBrains Mono (Technical Metadata & Shortcuts):** Used for keybinding badges (`Ctrl+K`), article IDs, word/character count displays, and document revisions.

## Layout & Spacing

The layout model adheres to desktop multi-pane ergonomics: a fixed-left collapsible navigation rail, persistent window title ribbon, customizable secondary split-pane (for directory trees or document hierarchies), and a fluid central viewport.

### Panel Constraints & Densities
- **Window Title Bar:** Fixed height of 36px with embedded application title, global breadcrumbs, and right-aligned system minimize/maximize/close triggers.
- **Sidebar Rail:** Collapsible between compact 48px (icon-only mode) and default 240px width.
- **Secondary Drawer / Inspector:** Fixed 320px width for document metadata, review logs, and publishing checklists.
- **Content Viewport:** Fluid layout bounded with 64ch max-width reading tracks during composition view, or unconstrained full-width fluid layouts during tabular archive audits.
- **Vertical Rhythm:** 4px baseline sub-grid standard. UI components align rigorously to 24px, 28px, and 32px height tiers for standard desktop pointer targets.

## Elevation & Depth

Visual plane differentiation avoids theatrical drop shadows in favor of low-contrast outlines, micro-borders, and tonal desktop tiering.

### Surface Tiers
- **Tier 0 (Window Foundation):** `#0F172A` title bar and frame edge separators.
- **Tier 1 (Application Shell & Toolbars):** `#F8FAFC` base backing, dock headers, and status bars.
- **Tier 2 (Workspaces & Panels):** `#FFFFFF` surfaces nested within active split panes, bounded by a uniform `1px solid #E2E8F0` border.
- **Tier 3 (Floating Controls, Menus & Tooltips):** Popover dropdowns, editor formatting ribbons, and autocomplete overlays. Supported by a crisp structural border (`#CBD5E1`) and a disciplined ambient shadow: `0 4px 12px -2px rgba(15, 23, 42, 0.08), 0 2px 4px -1px rgba(15, 23, 42, 0.04)`.
- **Tier 4 (System Modals & Critical Overlays):** Centered dialog frames with a direct `0 20px 25px -5px rgba(15, 23, 42, 0.15), 0 8px 10px -6px rgba(15, 23, 42, 0.1)` drop shadow and a `rgba(15, 23, 42, 0.4)` scrim.

## Shapes

The shape system employs disciplined, compact corner radii (`roundedness: 1`), communicating architectural precision, structural stability, and desktop density.

### Radius Implementations
- **Form Controls & Buttons:** 4px (`0.25rem`) corner radius. Maintains clean horizontal alignment in dense toolbars and ribbon arrays.
- **Cards, Panels & Split Views:** 6px to 8px (`0.375rem` to `0.5rem`) for window dialogs, editor cards, and floating inspector modules.
- **Badges & Status Tags:** 4px radius with strict 1px inset borders to avoid the toy-like appearance of capsule/pill shapes in an editorial desktop workflow.
- **Keyboard Shortcut Indicators (`Kbd`):** 3px corner radius with an anchoring lower 1px border profile.

## Components

### Buttons
- **Primary:** Background `#1E3A8A`, foreground `#FFFFFF`, border `1px solid #1E3A8A`. Hover: `#2563EB`. Active: `#1D4ED8`. Focus: outline `2px solid #2563EB` with `1px` offset.
- **Secondary / Outline:** Background `#FFFFFF`, foreground `#0F172A`, border `1px solid #E2E8F0`. Hover: `#F8FAFC`, border `#CBD5E1`.
- **Ghost (Toolbar Icon Buttons):** Transparent background, foreground `#475569`. Hover: `#F1F5F9`, foreground `#0F172A`. Height 28px for toolbar actions.
- **Destructive:** Background `#FFFFFF`, foreground `#DC2626`, border `1px solid #FECACA`. Hover: `#FEF2F2`.

### Inputs & Desktop Command Controls
- **Search Field with Shortcut (Ctrl+K):** Height 32px, background `#FFFFFF`, border `1px solid #E2E8F0`, padding `0 8px`. Embedded trailing badge: JetBrains Mono, `#64748B`, background `#F1F5F9`, border `1px solid #E2E8F0`, radius 3px.
- **Multi-Select Dropdown:** Anchored floating pane with integrated text search filter, checkbox list rows, and summary tag chips. Selected rows highlight with `#EFF6FF` and text `#1E3A8A`.

### Data Table
- **Header:** Height 32px, background `#F8FAFC`, bottom border `1px solid #E2E8F0`. Typography: `label-sm`, uppercase, color `#64748B`.
- **Sort Indicators:** Directional chevron indicators adjacent to text, tinting `#1E3A8A` on active sorted column.
- **Rows:** Height 36px (compact) / 44px (default). Row bottom border `1px solid #F1F5F9`. Hover state: background `#F8FAFC`. Selected state: `#F1F5F9` with a persistent 2px left accent bar `#1E3A8A`.
- **Cell Content:** Text truncation with trailing ellipsis; numeric data right-aligned using tabular figures.

### Status Badges
- **Structure:** Height 20px, inline-flex, items-center, horizontal padding 6px.
- **Typography:** `label-sm`, font-weight 600.
- **Color Mapping:**
  - *Ожидает модерации:* `#D97706` on `#FEF3C7`, border `1px solid #FDE68A`.
  - *Модерируется:* `#2563EB` on `#DBEAFE`, border `1px solid #BFDBFE`.
  - *Опубликовано:* `#059669` on `#D1FAE5`, border `1px solid #A7F3D0`.
  - *Отклонено:* `#DC2626` on `#FEE2E2`, border `1px solid #FECACA`.

### Editorial Rich Text Ribbon & Toolbar
- **Container:** Persistent dock header directly above the editorial canvas, height 38px, background `#FFFFFF`, border-bottom `1px solid #E2E8F0`.
- **Control Groups:** Separated by vertical dividers (height 18px, `1px solid #E2E8F0`, horizontal margin 6px).
- **Controls:** Toggle-buttons for bold, italic, blockquote, serif/sans preview switcher, header selector dropdown, and footnote injection. Active toggle state: background `#E2E8F0`, foreground `#0F172A`.

### Desktop Modal Dialogs
- **Shell:** Fixed width (480px confirmation, 720px editor preview), background `#FFFFFF`, border `1px solid #CBD5E1`, border-radius 6px.
- **Header:** Height 44px, border-bottom `1px solid #E2E8F0`, padding `0 16px`, includes title and close button (`Esc`).
- **Footer:** Height 52px, background `#F8FAFC`, border-top `1px solid #E2E8F0`, padding `0 16px`, right-aligned action cluster with secondary button left of primary button.
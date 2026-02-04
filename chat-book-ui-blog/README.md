# Blog Frontend Refactoring

This project has been refactored to adopt a B-end minimalistic design style, improve code quality, and add responsive layout support.

## 🎨 Design System

We have introduced a centralized design system using CSS variables.

- **Theme File**: `src/styles/variables.css`
- **Primary Color**: Purple (`#6754f8`)
- **Typography**: System fonts with standardized sizes.
- **Spacing**: 4px/8px grid system.

### Key Variables
- `--color-primary`: Brand color.
- `--bg-color-base`: Page background.
- `--container-width-lg`: Max width for large screens (1200px).

## 📱 Features

### 1. Responsive Layout
- **Grid Layout**: The home page recommendation section uses a responsive grid that adjusts columns from 5 (desktop) down to 1 (mobile).
- **Flexible Navigation**: The header adapts to smaller screens (hiding non-essential links).
- **Mobile-First**: Components like `ArticleCard` stack content vertically on small screens.

### 2. Component Refactoring
- **CommonHeader**: Cleaned up layout, removed hardcoded styles, added responsive logic.
- **Home**: Implemented a responsive container, grid system for recommendations, and flexible sidebar layout.
- **ArticleCard**: Modernized UI with better spacing, typography, and hover effects.

## 🛠 Project Setup

### Install Dependencies
```bash
npm install
```

### Run Development Server
```bash
npm run dev
```

### Build for Production
```bash
npm run build
```

## 📁 Directory Structure

- `src/styles/`: Global styles and variables.
- `src/components/`: Reusable UI components.
- `src/views/`: Page views.
- `src/assets/`: Static assets.

## 📝 TODOs
- [ ] Implement a mobile hamburger menu for the header.
- [ ] Refactor other views (Article detail, Creative center) to match the new design system.
- [ ] Add dark mode support using the CSS variables.

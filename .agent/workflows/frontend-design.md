---
description: Apply the frontend-design skill to create distinctive interfaces.
---
 
# Frontend Design Workflow
 
This workflow guides the creation of distinctive, production-grade frontend interfaces, avoiding generic "AI aesthetics".
 
## 1. Analyze Requirements
 
- **Context**: Understand the user's request (component, page, app).
- **Goal**: Identify the problem to solve and the target audience.
 
## 2. Adopt Design Persona
 
- **Tone**: Choose a bold aesthetic (e.g., Brutalist, Glassmorphism, Neobrutalism, Minimalist Luxury).
- **Differentiation**: What makes this design unforgettable?
- **Constraint**: Respect technical constraints (Vue/VitePress in this project).
 
## 3. Aesthetic Guidelines (The "Pro Max" Standard)
 
- **Typography**: Use distinctive font weights and tracking. Avoid generic system font stacks if possible, or style them uniquely.
- **Color**: Use cohesive, bold palettes. High contrast or subtle sophisticated gradients.
- **Motion**:
  - Use `transition-all duration-xxx` for smooth state changes.
  - Add entrance animations (e.g., `animate-fade-in`).
  - Micro-interactions on hover/active states.
- **Composition**:
  - Use generous negative space.
  - Break the grid where appropriate.
  - Use depth (shadows, layers, blur).
- **Details**:
  - Add texture (noise, gradients).
  - Custom cursors or scrollbars if appropriate.
  - Glassmorphism (`backdrop-blur`, `bg-opacity`).
 
## 4. Implementation Steps
 
1.  **Scaffold**: Create the file structure.
2.  **Style**: Apply Tailwind classes for the chosen aesthetic.
3.  **Interact**: Add Vue logic for state and animations.
4.  **Refine**: Review against the "Generic AI" trap. Is it too boring? Add more "juice".
 
## 5. Review Checklist
 
- [ ] Is the typography distinct?
- [ ] Are there micro-interactions?
- [ ] Is the layout responsive?
- [ ] Does it feel "Premium"?
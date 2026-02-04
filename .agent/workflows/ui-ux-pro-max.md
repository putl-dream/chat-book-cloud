---
description: Generate a professional design system using UI UX Pro Max skill.
---
 
# UI UX Pro Max Workflow
 
This workflow uses the globally installed `ui-ux-pro-max-skill` to generate a comprehensive design system and implementation guidelines.
 
## 1. Identify Requirements
 
- **Query**: What is the user trying to build? (e.g., "SaaS landing page", "Crypto dashboard").
- **Stack**: What is the tech stack? (Default: `vue` or `html-tailwind` based on project).
 
## 2. Generate Design System
 
- Run the search script to generate the design system.
- **Command**:
  ```bash
  python3 /Users/weiz/.gemini/antigravity/skills/ui-ux-pro-max-skill/src/ui-ux-pro-max/scripts/search.py "[User Query]" --design-system --format markdown --stack vue
  ```
  _(Replace `[User Query]` with the actual request)_
 
## 3. Apply Design
 
- Read the output from the script.
- Use the generated colors, typography, and patterns to implement the user's request.
- **Critical**: Follow the "Anti-patterns" section to avoid generic AI mistakes.
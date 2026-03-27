export const THEME_STORAGE_KEY = "chat_book_admin_theme";

export const themeTokenLayers = [
  {
    id: "layout",
    label: "Layout",
    description: "Cross-theme shell sizing and stacking tokens.",
  },
  {
    id: "surfaces",
    label: "Surfaces",
    description: "Page, card, sidebar and text color tokens.",
  },
  {
    id: "borders",
    label: "Borders",
    description: "Border, radius and shape tokens.",
  },
  {
    id: "feedback",
    label: "Feedback",
    description: "Success, warning and error semantic tokens.",
  },
  {
    id: "elevation",
    label: "Elevation",
    description: "Shadow and backdrop tokens.",
  },
  {
    id: "componentAliases",
    label: "Component Aliases",
    description: "Theme-facing aliases for hero, controls, nav and dialog surfaces.",
  },
] as const;

export const themeOptions = [
  { value: "linear", label: "Linear", subtitle: "Default console baseline" },
  { value: "glassmorphism", label: "Glass", subtitle: "Lightweight frosted glass" },
  { value: "charcoal", label: "Charcoal", subtitle: "Dark industrial texture" },
  { value: "playful", label: "Playful", subtitle: "Playful rounded accent" },
  { value: "minimal", label: "Minimal", subtitle: "Soft business minimal" },
] as const;

export type ThemeOption = (typeof themeOptions)[number];
export type ThemeName = ThemeOption["value"];

export const DEFAULT_THEME: ThemeName = "linear";

const themeNameSet = new Set<ThemeName>(themeOptions.map((option) => option.value));

export function isThemeName(value: string | null | undefined): value is ThemeName {
  if (!value) {
    return false;
  }

  return themeNameSet.has(value as ThemeName);
}

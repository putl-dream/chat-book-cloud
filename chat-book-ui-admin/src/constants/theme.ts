export const THEME_COLORS = ["primary", "neutral", "success", "warning", "error"] as const;

export type ThemeColor = (typeof THEME_COLORS)[number];

export const SPACING_SCALE = {
  1: "4px",
  2: "8px",
  3: "12px",
  4: "16px",
  5: "20px",
  6: "24px",
  8: "32px",
  10: "40px",
  12: "48px",
  16: "64px",
} as const;

export const RADIUS = {
  sm: "6px",
  md: "8px",
  lg: "16px",
} as const;

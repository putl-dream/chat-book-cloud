import type { Config } from "tailwindcss";

const config: Config = {
  darkMode: ["class", '[data-theme="charcoal"]'],
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      // Preserve the default spacing scale while keeping the project's 4px steps.
      spacing: {
        0: "0px",
        px: "1px",
        0.5: "2px",
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
        20: "80px",
      },
      colors: {
        cb: {
          bg: {
            main: "var(--cb-bg-main)",
            card: "var(--cb-bg-card)",
            sidebar: "var(--cb-bg-sidebar)",
          },
          text: {
            primary: "var(--cb-text-primary)",
            secondary: "var(--cb-text-secondary)",
            inverse: "var(--cb-text-inverse)",
          },
          primary: {
            DEFAULT: "var(--cb-primary)",
            hover: "var(--cb-primary-hover)",
          },
          nav: {
            active: {
              bg: "var(--cb-nav-active-bg)",
              text: "var(--cb-nav-active-text)",
            },
            icon: "var(--cb-icon-color)",
          },
          border: "var(--cb-border-subtle)",
          tag: {
            success: { bg: "var(--cb-tag-success-bg)", text: "var(--cb-tag-success-text)" },
            warning: { bg: "var(--cb-tag-warning-bg)", text: "var(--cb-tag-warning-text)" },
            error: { bg: "var(--cb-tag-error-bg)", text: "var(--cb-tag-error-text)" },
          },
        },
      },
      borderRadius: {
        DEFAULT: "var(--cb-radius-base)",
        card: "var(--cb-radius-card)",
      },
      boxShadow: {
        card: "var(--cb-shadow-card)",
        sidebar: "var(--cb-shadow-sidebar)",
      },
      fontSize: {
        h1: ["36px", { lineHeight: "44px", fontWeight: "700" }],
        h2: ["24px", { lineHeight: "32px", fontWeight: "600" }],
        body: ["14px", { lineHeight: "20px" }],
        sub: ["12px", { lineHeight: "18px" }],
      },
    },
  },
  plugins: [],
};

export default config;

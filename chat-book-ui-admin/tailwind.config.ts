import type { Config } from "tailwindcss";

const config: Config = {
  darkMode: ["class", '[data-theme="charcoal"]'],
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    // 1. 定义间距步进 (非常重要! 禁止随意写px)
    spacing: {
      0: "0px",
      px: "1px",
      0.5: "2px", // 0.5 * 4
      1: "4px", // 1 * 4
      2: "8px", // 2 * 4
      3: "12px", // 3 * 4
      4: "16px", // 4 * 4
      5: "20px", // 5 * 4
      6: "24px", // 6 * 4
      8: "32px", // 8 * 4
      10: "40px", // 10 * 4
      12: "48px", // 12 * 4
      16: "64px", // 16 * 4
      20: "80px", // 20 * 4
    },
    extend: {
      // 2. 映射颜色变量
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
      // 3. 映射结构变量
      borderRadius: {
        DEFAULT: "var(--cb-radius-base)",
        card: "var(--cb-radius-card)",
      },
      boxShadow: {
        card: "var(--cb-shadow-card)",
        sidebar: "var(--cb-shadow-sidebar)",
      },
      // 4. (可选) 字体层级定义，保证统一
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

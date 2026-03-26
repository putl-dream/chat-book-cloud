import type { Metadata } from "next";
import { ThemeProvider } from "@/components/theme-provider";
import "@/styles/globals.css";

export const metadata: Metadata = {
  title: "Chat Book Cloud Admin",
  description: "Chat Book Cloud 后台管理系统框架",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="zh-CN" suppressHydrationWarning>
      <body>
        <ThemeProvider 
          attribute="data-theme" 
          defaultTheme="linear" 
          enableSystem={false}
          themes={["linear", "glassmorphism", "charcoal", "playful", "retro", "minimal"]}
        >
          {children}
        </ThemeProvider>
      </body>
    </html>
  );
}

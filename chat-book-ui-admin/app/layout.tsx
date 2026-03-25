import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Chat Book Cloud Admin",
  description: "Chat Book Cloud 后台管理系统框架"
};

export default function RootLayout({
  children
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="zh-CN">
      <body>{children}</body>
    </html>
  );
}

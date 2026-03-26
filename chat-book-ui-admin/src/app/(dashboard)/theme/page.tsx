"use client";

import React, { useEffect, useState } from "react";
import { useTheme } from "next-themes";
import { Card } from "@/components/ui/card";
import { cn } from "@/lib/utils";

export default function ThemePage() {
  const [mounted, setMounted] = useState(false);
  const { theme, setTheme } = useTheme();

  // useEffect only runs on the client, so now we can safely show the UI
  useEffect(() => {
    setMounted(true);
  }, []);

  if (!mounted) {
    return (
      <div className="flex flex-col gap-6">
        <div className="flex flex-col gap-2">
          <h1 className="text-cb-text-primary text-2xl font-bold tracking-tight">主题设置</h1>
          <p className="text-cb-text-secondary text-sm">选择你喜欢的系统主题风格。</p>
        </div>
        <div className="grid grid-cols-1 gap-6 md:grid-cols-3">
          <Card className="bg-cb-bg-card h-48 animate-pulse" />
          <Card className="bg-cb-bg-card h-48 animate-pulse" />
          <Card className="bg-cb-bg-card h-48 animate-pulse" />
        </div>
      </div>
    );
  }

  const currentTheme = theme || "linear";

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-2">
        <h1 className="text-cb-text-primary text-2xl font-bold tracking-tight">主题设置</h1>
        <p className="text-cb-text-secondary text-sm">选择你喜欢的系统主题风格。</p>
      </div>

      <div className="grid grid-cols-1 gap-6 md:grid-cols-3">
        {/* Theme 1: Linear / Vercel Style */}
        <Card
          className={cn(
            "hover:border-cb-primary cursor-pointer transition-all hover:shadow-md",
            currentTheme === "linear" ? "border-cb-primary ring-cb-primary/20 ring-2" : ""
          )}
          onClick={() => setTheme("linear")}
        >
          <div className="flex flex-col gap-4">
            <div className="relative flex h-32 items-center justify-center overflow-hidden rounded-lg border border-[#E2E8F0] bg-[#F8FAFC]">
              <div className="absolute top-0 bottom-0 left-0 w-1/4 border-r border-[#E2E8F0] bg-[#FFFFFF]"></div>
              <div className="z-10 w-3/4 max-w-[200px] rounded-xl border border-[#E2E8F0] bg-white p-4 shadow-sm">
                <div className="mb-2 h-2 w-1/2 rounded bg-black"></div>
                <div className="h-2 w-full rounded bg-[#F1F5F9]"></div>
                <div className="mt-2 h-2 w-3/4 rounded bg-[#F1F5F9]"></div>
              </div>
            </div>
            <div>
              <div className="flex items-center justify-between">
                <h3 className="text-cb-text-primary text-lg font-semibold tracking-tight">
                  极简果粉风
                </h3>
                {currentTheme === "linear" && (
                  <span className="text-cb-primary rounded-md bg-black/5 px-2 py-1 text-xs font-semibold">
                    当前使用
                  </span>
                )}
              </div>
              <p className="text-cb-text-secondary mt-1 text-sm">
                追求极致的专业感和代码感，纯白背景与超细边框。
              </p>
            </div>
          </div>
        </Card>

        {/* Theme 2: Glassmorphism Light */}
        <Card
          className={cn(
            "hover:border-cb-primary cursor-pointer transition-all hover:shadow-md",
            currentTheme === "glassmorphism" ? "border-cb-primary ring-cb-primary/20 ring-2" : ""
          )}
          onClick={() => setTheme("glassmorphism")}
        >
          <div className="flex flex-col gap-4">
            <div
              className="relative flex h-32 items-center justify-center overflow-hidden rounded-lg border border-[#E2E8F0]"
              style={{ background: "linear-gradient(135deg, #f3e7e9 0%, #e3eeff 100%)" }}
            >
              <div className="absolute top-0 bottom-0 left-0 w-1/4 border-r border-white/50 bg-white/40 backdrop-blur-md"></div>
              <div className="z-10 w-3/4 max-w-[200px] rounded-xl border border-white/50 bg-white/70 p-4 shadow-lg backdrop-blur-md">
                <div className="mb-2 h-2 w-1/2 rounded bg-[#8b5cf6]"></div>
                <div className="h-2 w-full rounded bg-black/5"></div>
                <div className="mt-2 h-2 w-3/4 rounded bg-black/5"></div>
              </div>
            </div>
            <div>
              <div className="flex items-center justify-between">
                <h3 className="text-cb-text-primary text-lg font-semibold tracking-tight">
                  玻璃拟态 2.0
                </h3>
                {currentTheme === "glassmorphism" && (
                  <span className="text-cb-primary rounded-md bg-[#8b5cf6]/10 px-2 py-1 text-xs font-semibold">
                    当前使用
                  </span>
                )}
              </div>
              <p className="text-cb-text-secondary mt-1 text-sm">
                色彩渐变与毛玻璃效果，卡片浮在云雾背景上，轻盈梦幻。
              </p>
            </div>
          </div>
        </Card>

        {/* Theme 3: The Linear Deep Charcoal */}
        <Card
          className={cn(
            "hover:border-cb-primary cursor-pointer transition-all hover:shadow-md",
            currentTheme === "charcoal" ? "border-cb-primary ring-cb-primary/20 ring-2" : ""
          )}
          onClick={() => setTheme("charcoal")}
        >
          <div className="flex flex-col gap-4">
            <div className="relative flex h-32 items-center justify-center overflow-hidden rounded-lg border border-[#27272A] bg-[#0A0A0A]">
              <div className="absolute top-0 bottom-0 left-0 w-1/4 border-r border-[#27272A] bg-[#0A0A0A]/80 backdrop-blur-md"></div>
              <div className="z-10 w-3/4 max-w-[200px] rounded-xl border border-[#27272A] bg-[#171717] p-4 shadow-md">
                <div className="mb-2 h-2 w-1/2 rounded bg-[#EDEDED]"></div>
                <div className="h-2 w-full rounded bg-white/10"></div>
                <div className="mt-2 h-2 w-3/4 rounded bg-white/10"></div>
              </div>
            </div>
            <div>
              <div className="flex items-center justify-between">
                <h3 className="text-cb-text-primary text-lg font-semibold tracking-tight">
                  高级工业灰
                </h3>
                {currentTheme === "charcoal" && (
                  <span className="text-cb-primary rounded-md bg-white/10 px-2 py-1 text-xs font-semibold">
                    当前使用
                  </span>
                )}
              </div>
              <p className="text-cb-text-secondary mt-1 text-sm">
                深色极客风，通过不同灰度层叠实现区分，对眼睛极度友好。
              </p>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
}

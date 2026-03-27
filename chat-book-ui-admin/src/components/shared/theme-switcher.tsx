"use client";

import { Monitor, Sparkles, Smile, Wind } from "lucide-react";
import { useTheme } from "next-themes";
import { useEffect, useState } from "react";
import { cn } from "@/lib/utils";

const themeOptions = [
  { value: "minimal", label: "Minimal", icon: Wind },
  { value: "glassmorphism", label: "Glass", icon: Sparkles },
  { value: "charcoal", label: "Charcoal", icon: Monitor },
  { value: "playful", label: "Playful", icon: Smile },
] as const;

export function ThemeSwitcher() {
  const { theme, setTheme } = useTheme();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  if (!mounted) {
    return (
      <div className="header-chip">
        <span className="text-cb-text-secondary text-xs">主题加载中</span>
      </div>
    );
  }

  return (
    <div className="header-segment" aria-label="主题切换">
      {themeOptions.map((option) => {
        const Icon = option.icon;
        const isActive = (theme || "minimal") === option.value;

        return (
          <button
            key={option.value}
            type="button"
            className={cn("header-segment-button", isActive && "is-active")}
            onClick={() => setTheme(option.value)}
          >
            <Icon className="h-4 w-4" />
            <span>{option.label}</span>
          </button>
        );
      })}
    </div>
  );
}

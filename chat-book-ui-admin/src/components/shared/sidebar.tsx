"use client";

import * as React from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";

// Assuming we move admin-config or define props.
// For now, making it a generic sidebar layout component.
export interface NavItem {
  href: string;
  label: string;
  icon?: React.ReactNode;
}

export interface NavGroup {
  title: string;
  items: NavItem[];
}

export interface SidebarProps extends React.HTMLAttributes<HTMLElement> {
  groups: NavGroup[];
}

export function Sidebar({ groups, className, ...props }: SidebarProps) {
  const pathname = usePathname();

  return (
    <aside
      className={cn(
        "border-cb-border flex w-56 flex-col gap-6 border-r px-4 py-6 z-20",
        "bg-cb-bg-sidebar text-cb-text-inverse",
        className
      )}
      style={{
        backdropFilter: "var(--cb-sidebar-backdrop)",
        WebkitBackdropFilter: "var(--cb-sidebar-backdrop)"
      }}
      {...props}
    >
      <div className="flex items-center px-2">
        <h1 className="text-cb-primary text-xl font-semibold tracking-tight">Admin Console</h1>
      </div>

      <nav className="flex flex-1 flex-col gap-6 overflow-y-auto">
        {groups.map((group, idx) => (
          <div key={idx} className="flex flex-col gap-2">
            <h2 className="text-cb-text-inverse/60 px-2 text-xs font-semibold tracking-wider uppercase">
              {group.title}
            </h2>
            <div className="flex flex-col gap-1">
              {group.items.map((item) => {
                const isActive = pathname === item.href;
                return (
                  <Link
                    key={item.href}
                    href={item.href}
                    className={cn(
                      "group flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-all duration-200 ease-in-out",
                      isActive
                        ? "bg-cb-nav-active-bg text-cb-nav-active-text shadow-sm"
                        : "text-cb-text-inverse/70 hover:bg-black/5 dark:hover:bg-white/5 hover:text-cb-text-inverse"
                    )}
                  >
                    <div className={cn(
                      "flex h-5 w-5 items-center justify-center transition-colors",
                      isActive ? "text-cb-nav-active-text" : "text-cb-nav-icon group-hover:text-cb-nav-icon/80"
                    )}>
                      {item.icon}
                    </div>
                    <span className="tracking-tight">{item.label}</span>
                    {isActive && (
                      <span className="ml-auto h-1.5 w-1.5 rounded-full bg-cb-primary" />
                    )}
                  </Link>
                );
              })}
            </div>
          </div>
        ))}
      </nav>
    </aside>
  );
}

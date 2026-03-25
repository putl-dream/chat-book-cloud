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
        "border-subtle bg-card text-card-foreground flex w-64 flex-col gap-6 border-r px-4 py-6",
        className
      )}
      {...props}
    >
      <div className="flex items-center px-2">
        <h1 className="text-primary text-xl font-bold tracking-tight">Admin Console</h1>
      </div>

      <nav className="flex flex-1 flex-col gap-6 overflow-y-auto">
        {groups.map((group, idx) => (
          <div key={idx} className="flex flex-col gap-2">
            <h2 className="text-neutral/60 px-2 text-xs font-semibold tracking-wider uppercase">
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
                      "flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors",
                      isActive
                        ? "bg-primary/10 text-primary"
                        : "text-card-foreground/70 hover:bg-neutral/5 hover:text-card-foreground"
                    )}
                  >
                    {item.icon}
                    {item.label}
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

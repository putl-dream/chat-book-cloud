import * as React from "react";
import { cn } from "@/lib/utils";

export interface HeaderProps extends React.HTMLAttributes<HTMLHeadElement> {}

export function Header({ className, children, ...props }: HeaderProps) {
  return (
    <header
      className={cn(
        "border-subtle bg-card flex h-16 items-center justify-between border-b px-6 shadow-sm",
        className
      )}
      {...props}
    >
      <div className="flex items-center gap-4">
        {/* Left side (e.g. mobile menu trigger, breadcrumbs) */}
      </div>
      <div className="flex items-center gap-4">
        {/* Right side (e.g. user profile, theme toggle) */}
        {children}
      </div>
    </header>
  );
}

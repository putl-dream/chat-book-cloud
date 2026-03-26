import * as React from "react";
import { cn } from "@/lib/utils";

export interface HeaderProps extends React.HTMLAttributes<HTMLHeadElement> {}

export function Header({
  className,
  children,
  style,
  ...props
}: HeaderProps & { style?: React.CSSProperties }) {
  return (
    <header
      className={cn(
        "border-cb-border bg-cb-bg-card z-10 flex h-16 items-center justify-between border-b px-6 shadow-sm",
        className
      )}
      style={{
        backdropFilter: "var(--cb-card-backdrop)",
        WebkitBackdropFilter: "var(--cb-card-backdrop)",
        ...style,
      }}
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

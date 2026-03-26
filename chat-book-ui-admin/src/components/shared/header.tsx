import * as React from "react";
import { cn } from "@/lib/utils";

export interface HeaderProps extends React.HTMLAttributes<HTMLElement> {
  left?: React.ReactNode;
  right?: React.ReactNode;
}

export function Header({
  className,
  left,
  right,
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
      <div className="flex min-w-0 flex-1 items-center gap-4">{left}</div>
      <div className="flex items-center gap-3">{right ?? children}</div>
    </header>
  );
}

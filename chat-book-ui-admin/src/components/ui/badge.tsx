import * as React from "react";
import { cn } from "@/lib/utils";

export interface BadgeProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: "primary" | "neutral" | "success" | "warning" | "error" | "outline";
}

function Badge({ className, variant = "primary", ...props }: BadgeProps) {
  return (
    <div
      className={cn(
        "focus:ring-primary inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors focus:ring-2 focus:ring-offset-2 focus:outline-none",
        {
          "bg-primary/10 text-primary border-transparent": variant === "primary",
          "bg-neutral/10 text-neutral border-transparent": variant === "neutral",
          "bg-success/10 text-success border-transparent": variant === "success",
          "bg-warning/10 text-warning border-transparent": variant === "warning",
          "bg-error/10 text-error border-transparent": variant === "error",
          "text-card-foreground border-subtle": variant === "outline",
        },
        className
      )}
      {...props}
    />
  );
}

export { Badge };

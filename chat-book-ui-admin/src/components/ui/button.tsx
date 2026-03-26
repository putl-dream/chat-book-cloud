import * as React from "react";
import { cn } from "@/lib/utils";

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "primary" | "neutral" | "success" | "warning" | "error" | "outline" | "ghost";
  size?: "sm" | "md" | "lg";
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = "primary", size = "md", ...props }, ref) => {
    return (
      <button
        ref={ref}
        className={cn(
          "ring-offset-main focus-visible:ring-primary inline-flex items-center justify-center rounded-sm text-sm font-medium whitespace-nowrap transition-colors focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:outline-none disabled:pointer-events-none disabled:opacity-50",
          {
            "bg-primary text-primary-foreground hover:bg-primary/90": variant === "primary",
            "bg-neutral text-neutral-foreground hover:bg-neutral/90": variant === "neutral",
            "bg-success text-success-foreground hover:bg-success/90": variant === "success",
            "bg-warning text-warning-foreground hover:bg-warning/90": variant === "warning",
            "bg-error text-error-foreground hover:bg-error/90": variant === "error",
            "border-subtle hover:bg-card hover:text-card-foreground border bg-transparent":
              variant === "outline",
            "hover:bg-card hover:text-card-foreground": variant === "ghost",
            "h-8 px-3": size === "sm",
            "h-10 px-4 py-2": size === "md",
            "h-12 px-8": size === "lg",
          },
          className
        )}
        {...props}
      />
    );
  }
);
Button.displayName = "Button";

export { Button };

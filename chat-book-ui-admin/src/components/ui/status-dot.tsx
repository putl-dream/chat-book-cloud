import * as React from "react";
import { cn } from "@/lib/utils";

export interface StatusDotProps extends React.HTMLAttributes<HTMLSpanElement> {
  status?: "primary" | "neutral" | "success" | "warning" | "error";
  animate?: boolean;
}

function StatusDot({ className, status = "neutral", animate = false, ...props }: StatusDotProps) {
  return (
    <span className="relative flex h-3 w-3" {...props}>
      {animate && (
        <span
          className={cn("absolute inline-flex h-full w-full animate-ping rounded-full opacity-75", {
            "bg-primary": status === "primary",
            "bg-neutral": status === "neutral",
            "bg-success": status === "success",
            "bg-warning": status === "warning",
            "bg-error": status === "error",
          })}
        />
      )}
      <span
        className={cn(
          "relative inline-flex h-3 w-3 rounded-full",
          {
            "bg-primary": status === "primary",
            "bg-neutral": status === "neutral",
            "bg-success": status === "success",
            "bg-warning": status === "warning",
            "bg-error": status === "error",
          },
          className
        )}
      />
    </span>
  );
}

export { StatusDot };

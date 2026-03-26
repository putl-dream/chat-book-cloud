import { cn } from "@/lib/utils";
import React from "react";

type BadgeType = "success" | "warning" | "error" | "default";

interface BadgeProps extends React.HTMLAttributes<HTMLSpanElement> {
  children?: React.ReactNode;
  type?: BadgeType;
  variant?: string; // for backward compatibility with old code
}

const typeMap: Record<BadgeType, string> = {
  success: "bg-cb-tag-success-bg text-cb-tag-success-text",
  warning: "bg-cb-tag-warning-bg text-cb-tag-warning-text",
  error: "bg-cb-tag-error-bg text-cb-tag-error-text",
  default: "bg-cb-bg-main text-cb-text-secondary border border-cb-border",
};

export const Badge: React.FC<BadgeProps> = ({
  children,
  type = "default",
  className,
  variant,
  ...props
}) => {
  return (
    <span
      className={cn(
        "text-sub inline-flex items-center gap-1.5 rounded-full px-3 py-1 font-medium", // 精致的小胶囊样式
        typeMap[type],
        variant === "outline" ? "border-cb-border border bg-transparent" : "",
        className
      )}
      {...props}
    >
      {/* (可选) 在这里加入一个小小的状态圆点图标 */}
      {children}
    </span>
  );
};

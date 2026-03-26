import { cn } from "@/lib/utils"; // 一个常用的工具函数，用于合并 className
import React from "react";

interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  children?: React.ReactNode;
}

export const Card: React.FC<CardProps> = ({ children, className, style, ...props }) => {
  return (
    <div
      className={cn(
        "bg-cb-bg-card border-cb-border rounded-card shadow-card border p-6", // 应用所有 Tokens
        className
      )}
      style={{
        borderWidth: "var(--cb-border-width, 1px)",
        borderStyle: "var(--cb-border-style, solid)",
        backdropFilter: "var(--cb-card-backdrop)",
        WebkitBackdropFilter: "var(--cb-card-backdrop)",
        ...style,
      }}
      {...props}
    >
      {children}
    </div>
  );
};

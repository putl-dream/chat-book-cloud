import * as React from "react";
import { cn } from "@/lib/utils";

export interface AdminPageLayoutProps extends Omit<React.HTMLAttributes<HTMLDivElement>, "title"> {
  title: React.ReactNode;
  description?: React.ReactNode;
  extra?: React.ReactNode;
  stats?: React.ReactNode;
}

export function AdminPageLayout({
  title,
  description,
  extra,
  stats,
  children,
  className,
  ...props
}: AdminPageLayoutProps) {
  return (
    <div className={cn("flex flex-col gap-6", className)} {...props}>
      {/* Page Header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-card-foreground text-2xl font-bold tracking-tight">{title}</h1>
          {description && <p className="text-neutral-foreground/70 mt-1 text-sm">{description}</p>}
        </div>
        {extra && <div className="flex items-center gap-3">{extra}</div>}
      </div>

      {/* KPI Stats Area */}
      {stats && <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">{stats}</div>}

      {/* Main Content */}
      <div className="flex-1">{children}</div>
    </div>
  );
}

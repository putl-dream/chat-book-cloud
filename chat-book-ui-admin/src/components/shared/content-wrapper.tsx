import * as React from "react";
import { cn } from "@/lib/utils";

export interface ContentWrapperProps extends React.HTMLAttributes<HTMLDivElement> {}

export function ContentWrapper({ className, children, ...props }: ContentWrapperProps) {
  return (
    <main className={cn("bg-main flex-1 overflow-y-auto p-6 lg:p-8", className)} {...props}>
      <div className="mx-auto max-w-7xl">{children}</div>
    </main>
  );
}

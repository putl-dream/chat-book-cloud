"use client";

import * as React from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { adminNavigation, findCurrentNav } from "@/lib/admin-config";
import { Sidebar } from "@/components/shared/sidebar";
import { Header } from "@/components/shared/header";
import { ContentWrapper } from "@/components/shared/content-wrapper";
import { Badge } from "@/components/ui/badge";
import { StatusDot } from "@/components/ui/status-dot";

export function AdminShell({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const pathname = usePathname();
  const currentNav = findCurrentNav(pathname);

  // Convert adminNavigation to match the new Sidebar groups format
  const sidebarGroups = adminNavigation.map((group) => ({
    title: group.title,
    items: group.items.map((item) => ({
      href: item.href,
      label: item.label,
      description: item.description,
    })),
  }));

  return (
    <div className="bg-cb-bg-main flex h-screen w-full overflow-hidden">
      <Sidebar groups={sidebarGroups} />

      <div className="flex flex-1 flex-col overflow-hidden">
        <Header>
          <Badge variant="outline" className="gap-2 px-3 py-1">
            <StatusDot status="success" animate />
            Next App Router + React
          </Badge>
        </Header>

        <ContentWrapper>{children}</ContentWrapper>
      </div>
    </div>
  );
}

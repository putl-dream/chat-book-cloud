"use client";

import * as React from "react";
import { Bell, ChevronRight, ShieldCheck } from "lucide-react";
import { usePathname } from "next/navigation";
import { adminNavigation, buildBreadcrumbs, findCurrentNav } from "@/lib/admin-config";
import { Sidebar } from "@/components/shared/sidebar";
import { Header } from "@/components/shared/header";
import { ContentWrapper } from "@/components/shared/content-wrapper";
import { LogoutButton } from "@/components/shared/logout-button";
import { ThemeSwitcher } from "@/components/shared/theme-switcher";
import type { CurrentAdminUser } from "@/lib/types";

export function AdminShell({
  children,
  currentUser,
}: Readonly<{
  children: React.ReactNode;
  currentUser: CurrentAdminUser;
}>) {
  const pathname = usePathname();
  const currentNav = findCurrentNav(pathname);
  const breadcrumbs = buildBreadcrumbs(pathname);

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
        <Header
          left={
            <div className="min-w-0">
              <p className="header-kicker">Admin Control Plane</p>
              <div className="breadcrumb-row">
                {breadcrumbs.map((item, index) => (
                  <React.Fragment key={item.href}>
                    {index > 0 ? <ChevronRight className="h-3.5 w-3.5 opacity-40" /> : null}
                    <span className={index === breadcrumbs.length - 1 ? "is-current" : ""}>
                      {item.label}
                    </span>
                  </React.Fragment>
                ))}
              </div>
              {currentNav ? <p className="header-subtitle">{currentNav.description}</p> : null}
            </div>
          }
          right={
            <>
              <button className="header-icon-button" type="button" title="告警中心待接入">
                <Bell className="h-4 w-4" />
              </button>
              <ThemeSwitcher />
              <div className="header-profile">
                <div className="header-avatar">
                  <ShieldCheck className="h-4 w-4" />
                </div>
                <div className="min-w-0">
                  <strong>{currentUser.username}</strong>
                  <span>
                    {currentUser.role === "admin" ? "管理员" : "受限账号"} · UID {currentUser.userId}
                  </span>
                </div>
              </div>
              <LogoutButton />
            </>
          }
        />

        <ContentWrapper>{children}</ContentWrapper>
      </div>
    </div>
  );
}

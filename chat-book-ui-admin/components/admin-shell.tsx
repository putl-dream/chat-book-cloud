"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { adminNavigation, findCurrentNav } from "@/lib/admin-config";

export function AdminShell({
  children
}: Readonly<{
  children: React.ReactNode;
}>) {
  const pathname = usePathname();
  const currentNav = findCurrentNav(pathname);

  return (
    <div className="admin-shell">
      <aside className="sidebar">
        <div className="brand-block">
          <p className="brand-kicker">Chat Book Cloud</p>
          <h1>Admin Console</h1>
          <p className="brand-copy">
            面向内容平台运营、审核和系统接入的后台框架。
          </p>
        </div>

        <nav className="nav-stack" aria-label="后台导航">
          {adminNavigation.map((group) => (
            <section className="nav-group" key={group.title}>
              <p className="nav-group-title">{group.title}</p>
              <div className="nav-items">
                {group.items.map((item) => {
                  const active = pathname === item.href;
                  return (
                    <Link
                      key={item.href}
                      href={item.href}
                      className={active ? "nav-item nav-item-active" : "nav-item"}
                    >
                      <span className="nav-item-label">{item.label}</span>
                      <span className="nav-item-copy">{item.description}</span>
                    </Link>
                  );
                })}
              </div>
            </section>
          ))}
        </nav>

        <div className="sidebar-footnote">
          <p>当前模式</p>
          <strong>Framework / Mock Ready</strong>
          <span>已为真实接口接入预留数据适配层。</span>
        </div>
      </aside>

      <main className="workspace">
        <header className="workspace-header">
          <div>
            <p className="workspace-kicker">后台管理系统</p>
            <h2>{currentNav?.label ?? "平台概览"}</h2>
          </div>
          <div className="workspace-badge">
            <span className="badge-dot" />
            <span>Next App Router + React</span>
          </div>
        </header>
        <div className="workspace-body">{children}</div>
      </main>
    </div>
  );
}

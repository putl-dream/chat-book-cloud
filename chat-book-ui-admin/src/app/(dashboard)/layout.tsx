import { AdminShell } from "@/components/admin-shell";
import { RequestStatePanel } from "@/components/shared/request-state-panel";
import { AdminApiError, requireAdminSession } from "@/lib/admin-api";
import type { CurrentAdminUser } from "@/lib/types";

// 强制动态渲染：此 Layout 依赖 Cookie（admin session），不能被静态预渲染
// requireAdminSession 已用 React cache() 包装，同一渲染树内多次调用只发一次后端请求
export const dynamic = "force-dynamic";

const fallbackAdminUser: CurrentAdminUser = {
  id: 0,
  userId: 0,
  username: "Admin",
  email: "unavailable@example.com",
  role: "admin",
  photo: null,
  profile: "Session validation unavailable",
};

export default async function DashboardLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  try {
    const session = await requireAdminSession();

    return <AdminShell currentUser={session.user}>{children}</AdminShell>;
  } catch (error) {
    if (error instanceof AdminApiError) {
      return (
        <AdminShell currentUser={fallbackAdminUser}>
          <>
            <RequestStatePanel
              title="Admin session validation is unavailable"
              description={error.message}
              tone="warning"
            />
            {children}
          </>
        </AdminShell>
      );
    }

    throw error;
  }
}

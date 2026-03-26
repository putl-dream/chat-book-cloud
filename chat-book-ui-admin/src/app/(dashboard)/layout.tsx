import { AdminShell } from "@/components/admin-shell";
import { RequestStatePanel } from "@/components/shared/request-state-panel";
import { AdminApiError, requireAdminSession } from "@/lib/admin-api";
import type { CurrentAdminUser } from "@/lib/types";

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

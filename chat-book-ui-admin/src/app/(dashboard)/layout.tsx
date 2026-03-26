import { AdminShell } from "@/components/admin-shell";
import { requireAdminSession } from "@/lib/admin-api";

export default async function DashboardLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const session = await requireAdminSession();

  return <AdminShell currentUser={session.user}>{children}</AdminShell>;
}

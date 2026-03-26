"use client";

import { LogOut } from "lucide-react";
import { useRouter } from "next/navigation";
import { clearAdminSession } from "@/lib/auth";

export function LogoutButton() {
  const router = useRouter();

  return (
    <button
      type="button"
      className="header-action-button"
      onClick={() => {
        clearAdminSession();
        router.replace("/login");
        router.refresh();
      }}
    >
      <LogOut className="h-4 w-4" />
      <span>退出登录</span>
    </button>
  );
}

import Link from "next/link";
import { ShieldAlert } from "lucide-react";

export default function ForbiddenPage() {
  return (
    <main className="centered-shell">
      <section className="state-panel state-panel-danger max-w-xl">
        <div className="state-panel-icon">
          <ShieldAlert className="h-5 w-5" />
        </div>
        <div className="space-y-3">
          <p className="eyebrow">403 Forbidden</p>
          <h1>当前账号没有后台访问权限</h1>
          <p>
            后台仅允许管理员进入。若账号已调整权限，请重新登录；若本应具备权限，需要先补齐后端管理员校验链路。
          </p>
          <Link className="state-panel-link" href="/login?reason=forbidden">
            返回登录页
          </Link>
        </div>
      </section>
    </main>
  );
}

"use client";

import { ArrowRight, LockKeyhole, ShieldCheck } from "lucide-react";
import { useRouter } from "next/navigation";
import { FormEvent, useEffect, useState } from "react";
import { clearAdminSession, normalizeNextPath, readClientToken, saveAdminSession } from "@/lib/auth";
import { BrowserApiError, getCurrentAdminUser, loginAdmin } from "@/lib/admin-browser-api";

function getReasonMessage(reason: string | null) {
  if (reason === "session-expired") {
    return "登录态已失效，请重新登录。";
  }

  if (reason === "forbidden") {
    return "当前账号没有后台访问权限。";
  }

  return "";
}

export interface LoginFormProps {
  nextPath?: string;
  reason?: string;
}

export function LoginForm({ nextPath, reason }: LoginFormProps) {
  const router = useRouter();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [checking, setChecking] = useState(true);
  const [errorMessage, setErrorMessage] = useState(getReasonMessage(reason ?? null));
  const safeNextPath = normalizeNextPath(nextPath ?? null);

  useEffect(() => {
    let cancelled = false;

    async function checkExistingSession() {
      const token = readClientToken();

      if (!token) {
        if (!cancelled) {
          setChecking(false);
        }
        return;
      }

      try {
        const user = await getCurrentAdminUser();

        if (cancelled) {
          return;
        }

        if (user.role === "admin") {
          router.replace(safeNextPath);
          router.refresh();
          return;
        }

        clearAdminSession();
        setErrorMessage("当前账号不是管理员，无法进入后台。");
      } catch {
        clearAdminSession();
      } finally {
        if (!cancelled) {
          setChecking(false);
        }
      }
    }

    checkExistingSession();

    return () => {
      cancelled = true;
    };
  }, [router, safeNextPath]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    setSubmitting(true);
    setErrorMessage("");

    try {
      const token = await loginAdmin(username.trim(), password);
      saveAdminSession(token);

      const user = await getCurrentAdminUser();

      if (user.role !== "admin") {
        clearAdminSession();
        setErrorMessage("当前账号不是管理员，无法进入后台。");
        return;
      }

      router.replace(safeNextPath);
      router.refresh();
    } catch (error) {
      clearAdminSession();

      if (error instanceof BrowserApiError) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("登录失败，请检查网关地址、账号或密码。");
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="login-shell">
      <section className="login-showcase">
        <div className="login-showcase-badge">
          <ShieldCheck className="h-4 w-4" />
          <span>管理员访问控制</span>
        </div>
        <h1>Chat Book 后台管理台</h1>
        <p>
          先补权限与登录态，再接通真实数据。当前已对接用户统计、待审核列表、标签 CRUD
          和管理员会话基线。
        </p>

        <div className="login-showcase-grid">
          <article>
            <strong>T0 已落地</strong>
            <span>登录页、路由守卫、401/403 兜底、退出登录</span>
          </article>
          <article>
            <strong>T1 首批接通</strong>
            <span>概览、用户、审核、标签改为真实后台请求</span>
          </article>
          <article>
            <strong>T2 待后端补齐</strong>
            <span>审核动作、用户治理、全站内容治理与告警中心</span>
          </article>
        </div>
      </section>

      <section className="login-panel">
        <div className="login-panel-head">
          <div className="login-panel-icon">
            <LockKeyhole className="h-5 w-5" />
          </div>
          <div>
            <p className="eyebrow">Admin Login</p>
            <h2>使用管理员账号进入后台</h2>
          </div>
        </div>

        <form className="login-form" onSubmit={handleSubmit}>
          <label className="field">
            <span>用户名</span>
            <input
              autoComplete="username"
              disabled={submitting || checking}
              name="username"
              placeholder="请输入管理员用户名"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
            />
          </label>

          <label className="field">
            <span>密码</span>
            <input
              autoComplete="current-password"
              disabled={submitting || checking}
              name="password"
              placeholder="请输入密码"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
            />
          </label>

          {errorMessage ? <p className="form-message error">{errorMessage}</p> : null}
          {!errorMessage && checking ? <p className="form-message">正在校验现有登录态...</p> : null}

          <button
            className="login-submit"
            disabled={submitting || checking || !username.trim() || !password}
            type="submit"
          >
            <span>{submitting ? "登录中..." : "进入后台"}</span>
            <ArrowRight className="h-4 w-4" />
          </button>
        </form>
      </section>
    </div>
  );
}

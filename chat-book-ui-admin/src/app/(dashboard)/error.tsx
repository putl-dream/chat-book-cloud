"use client";

import { useEffect } from "react";
import { RequestStatePanel } from "@/components/shared/request-state-panel";

export default function DashboardError({
  error,
  reset,
}: Readonly<{
  error: Error & { digest?: string };
  reset: () => void;
}>) {
  useEffect(() => {
    console.error(error);
  }, [error]);

  return (
    <section className="page-shell">
      <RequestStatePanel
        title="后台页面渲染失败"
        description={error.message || "请求已发送，但页面在渲染阶段发生异常。"}
        tone="warning"
      />
      <button className="panel-action-button w-fit" type="button" onClick={() => reset()}>
        重试加载
      </button>
    </section>
  );
}

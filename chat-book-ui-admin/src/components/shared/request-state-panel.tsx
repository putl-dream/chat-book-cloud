"use client";

import Link from "next/link";
import { AlertTriangle, CircleSlash, ShieldAlert } from "lucide-react";
import { cn } from "@/lib/utils";

type PanelTone = "neutral" | "danger" | "warning";

const panelToneMap: Record<PanelTone, { icon: typeof CircleSlash; className: string }> = {
  neutral: {
    icon: CircleSlash,
    className: "state-panel-neutral",
  },
  warning: {
    icon: AlertTriangle,
    className: "state-panel-warning",
  },
  danger: {
    icon: ShieldAlert,
    className: "state-panel-danger",
  },
};

export interface RequestStatePanelProps {
  title: string;
  description: string;
  tone?: PanelTone;
  actionLabel?: string;
  actionHref?: string;
}

export function RequestStatePanel({
  title,
  description,
  tone = "neutral",
  actionLabel,
  actionHref,
}: RequestStatePanelProps) {
  const toneConfig = panelToneMap[tone];
  const Icon = toneConfig.icon;

  return (
    <section className={cn("state-panel", toneConfig.className)}>
      <div className="state-panel-icon">
        <Icon className="h-5 w-5" />
      </div>
      <div className="space-y-2">
        <h3>{title}</h3>
        <p>{description}</p>
        {actionLabel && actionHref ? (
          <Link className="state-panel-link" href={actionHref}>
            {actionLabel}
          </Link>
        ) : null}
      </div>
    </section>
  );
}

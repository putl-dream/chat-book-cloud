import type { HighlightCard, NavGroup, ServiceReadiness } from "@/types/admin";

export const adminNavigation: NavGroup[] = [
  {
    title: "总览",
    items: [
      {
        href: "/dashboard",
        label: "平台概览",
        description: "查看平台关键指标、服务接入状态和当前治理重点。",
      },
    ],
  },
  {
    title: "业务治理",
    items: [
      {
        href: "/users",
        label: "用户管理",
        description: "按关键词、角色和状态治理账号，并执行角色调整、禁用与恢复。",
      },
      {
        href: "/users/audit",
        label: "操作审计",
        description: "查看管理员治理动作、追溯操作对象和执行时间。",
      },
      {
        href: "/articles/review",
        label: "文章审核",
        description: "处理待审核文章，维护内容准入流程。",
      },
      {
        href: "/articles/content",
        label: "内容管理",
        description: "查看已发布内容、草稿状态和内容资产分布。",
      },
      {
        href: "/tags",
        label: "标签管理",
        description: "维护主题、技术栈与学习路径三类标签体系。",
      },
      {
        href: "/interactions",
        label: "评论治理",
        description: "巡检全站评论，处理屏蔽、删除、恢复和异常评论统计。",
      },
    ],
  },
  {
    title: "系统规划",
    items: [
      {
        href: "/system",
        label: "系统接入",
        description: "梳理微服务接入状态、接口缺口和后续改造计划。",
      },
      {
        href: "/theme",
        label: "主题设置",
        description: "切换后台界面的视觉主题。",
      },
    ],
  },
];

export const articleStatusMap: Record<number, string> = {
  [-1]: "已删除",
  0: "草稿",
  1: "待审核",
  2: "已发布",
};

export const articleCategoryMap: Record<number, string> = {
  0: "后端",
  1: "前端",
  2: "MySQL",
  3: "算法",
  4: "其他",
};

export const contentTypeMap: Record<number, string> = {
  0: "学习 / 教程",
  1: "实战 / 项目",
};

export const tagTypeMap: Record<number, string> = {
  3: "主题标签",
  1: "技术栈",
  2: "学习路径",
};

export const actionTypeMap: Record<string, string> = {
  PRAISE: "点赞",
  COLLECT: "收藏",
  COMMENT: "评论",
  BROWSE: "浏览",
};

export const dashboardHighlights: HighlightCard[] = [
  {
    title: "用户治理主链路已经闭环",
    description: "首页统计、用户筛选、角色调整、禁用恢复和管理员操作审计都已接入真实接口。",
    status: "stable",
  },
  {
    title: "文章审核已从流程闭环推进到信息闭环",
    description: "待审列表、审核动作、内容类型和标签信息已接通，正文详情与审核历史仍可继续增强。",
    status: "partial",
  },
  {
    title: "互动后台当前聚焦评论治理",
    description: "评论分页、状态操作与治理统计已经可用，通知中心、异常行为监控和统一告警仍属下一阶段。",
    status: "partial",
  },
];

export const dashboardServices: ServiceReadiness[] = [
  {
    service: "chat-book-cloud-user",
    responsibility: "用户统计、后台筛选、账号治理和管理员操作审计",
    currentApi: "/user/admin/count, /user/admin/user, /user/admin/operation-log/page, /user/admin/{id}/*",
    backendGap: "主链路已接通；后续增强点是批量治理、风控规则与更细粒度审计检索。",
    priority: "medium",
  },
  {
    service: "chat-book-cloud-article",
    responsibility: "待审核文章、内容治理、标签体系和文章统计",
    currentApi: "/page/adminArticlePage, /article/queryPendingReviewCount, /article/admin/review/*, /article/admin/page, /author-tag/*, /system-tag/*, /tag-map/*",
    backendGap: "正文详情、审核记录查询和映射批量治理仍待继续增强。",
    priority: "high",
  },
  {
    service: "chat-book-cloud-interaction",
    responsibility: "评论治理、异常评论统计和状态处置",
    currentApi: "/interaction/admin/review/page, /interaction/admin/review/stats, /interaction/admin/review/{id}/*",
    backendGap: "通知中心、异常行为监控和统一告警中心仍未建设。",
    priority: "medium",
  },
];

export const themeOptions = [
  { value: "linear", label: "Linear", subtitle: "默认线性风格" },
  { value: "glassmorphism", label: "Glass", subtitle: "轻盈玻璃拟态" },
  { value: "charcoal", label: "Charcoal", subtitle: "深色工业质感" },
  { value: "playful", label: "Playful", subtitle: "轻松活泼的高饱和主题" },
  { value: "minimal", label: "Minimal", subtitle: "克制柔和的商务风格" },
] as const;

export function findCurrentNav(pathname: string) {
  return adminNavigation.flatMap((group) => group.items).find((item) => item.href === pathname);
}

export function buildBreadcrumbs(pathname: string) {
  const current = findCurrentNav(pathname);

  if (!current) {
    return [
      { href: "/dashboard", label: "后台首页" },
      { href: pathname, label: "当前页面" },
    ];
  }

  return [
    { href: "/dashboard", label: "后台首页" },
    { href: current.href, label: current.label },
  ];
}

export function getPriorityTone(priority: "high" | "medium" | "low") {
  if (priority === "high") return "danger";
  if (priority === "medium") return "warn";
  return "safe";
}

export function getStatusTone(status: "stable" | "partial" | "gap") {
  if (status === "stable") return "safe";
  if (status === "partial") return "warn";
  return "danger";
}

export function getRoleLabel(role: string) {
  return role === "admin" ? "管理员" : "普通用户";
}

export function getRoleTone(role: string) {
  return role === "admin" ? "safe" : "neutral";
}

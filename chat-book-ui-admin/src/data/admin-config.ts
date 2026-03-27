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
        description: "查看后台用户列表、角色信息和账号资料。",
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
        description: "维护技术栈与学习路径两类标签体系。",
      },
      {
        href: "/interactions",
        label: "互动监控",
        description: "查看评论、通知和行为事件的治理入口。",
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
    title: "用户与标签模块已经具备优先接入真实后端的条件",
    description: "用户统计、分页查询和标签 CRUD 都已有对应接口，适合优先接入。",
    status: "stable",
  },
  {
    title: "文章审核链路已经形成基础闭环",
    description: "待审列表、通过、驳回和批量处理已经具备，详情字段仍待后端补齐。",
    status: "partial",
  },
  {
    title: "互动治理仍停留在监控入口阶段",
    description: "评论、通知与异常行为缺少管理员视角的聚合接口，当前仅保留治理占位。",
    status: "gap",
  },
];

export const dashboardServices: ServiceReadiness[] = [
  {
    service: "chat-book-cloud-user",
    responsibility: "用户统计、后台分页和管理员身份识别",
    currentApi: "/user/admin/count, /user/admin/user, /user/bySelf",
    backendGap: "缺少角色调整、禁用 / 恢复和操作审计接口",
    priority: "high",
  },
  {
    service: "chat-book-cloud-article",
    responsibility: "待审核文章、标签体系和内容运营入口",
    currentApi: "/page/adminArticlePage, /article/admin/review/*, /tag/page, /tag/list, /tag/*",
    backendGap: "缺少全站内容分页、详情字段和审核记录查询接口",
    priority: "high",
  },
  {
    service: "chat-book-cloud-interaction",
    responsibility: "评论治理、通知抽查和异常行为聚合",
    currentApi: "仅面向前台用户视角接口",
    backendGap: "缺少后台评论分页、屏蔽 / 删除和告警中心接口",
    priority: "high",
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

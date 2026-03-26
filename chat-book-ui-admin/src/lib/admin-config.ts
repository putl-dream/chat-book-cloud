import type { HighlightCard, NavGroup, ServiceReadiness } from "@/lib/types";

export const adminNavigation: NavGroup[] = [
  {
    title: "总览",
    items: [
      {
        href: "/dashboard",
        label: "平台概览",
        description: "查看平台核心指标、服务接入状态和待办事项。",
      },
    ],
  },
  {
    title: "业务管理",
    items: [
      {
        href: "/users",
        label: "用户管理",
        description: "管理用户列表、角色和账号资料。",
      },
      {
        href: "/articles/review",
        label: "文章审核",
        description: "处理待审核文章、建立内容准入流程。",
      },
      {
        href: "/articles/content",
        label: "内容管理",
        description: "查看已发布内容、草稿和下架文章。",
      },
      {
        href: "/tags",
        label: "标签管理",
        description: "维护技术栈、学习路径等标签体系。",
      },
      {
        href: "/interactions",
        label: "互动监控",
        description: "查看评论、通知和行为数据治理入口。",
      },
    ],
  },
  {
    title: "系统规划",
    items: [
      {
        href: "/system",
        label: "系统接入",
        description: "梳理微服务接入状态、接口缺口与后续改造计划。",
      },
      {
        href: "/theme",
        label: "主题设置",
        description: "切换系统主题风格。",
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
  0: "学习/教程",
  1: "实战/项目",
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
    title: "用户与标签模块可以优先转真实后台",
    description: "用户统计、分页查询和标签 CRUD 已有现成接口，适合作为第一批真实接入。",
    status: "stable",
  },
  {
    title: "审核链路已形成基础闭环",
    description: "管理员已可处理待审核文章的通过、驳回和批量审核，详情字段与审计查询仍待补齐。",
    status: "partial",
  },
  {
    title: "互动治理仍停留在监控入口",
    description: "评论、通知与异常行为仍缺少管理员视角的聚合接口，首页仅保留未接入提示。",
    status: "gap",
  },
];

export const dashboardServices: ServiceReadiness[] = [
  {
    service: "chat-book-cloud-user",
    responsibility: "用户统计、后台分页、管理员身份识别",
    currentApi: "/user/admin/count, /user/admin/user, /user/bySelf",
    backendGap: "缺少角色调整、禁用/恢复、操作审计",
    priority: "high",
  },
  {
    service: "chat-book-cloud-article",
    responsibility: "待审核文章队列、标签体系、内容运营入口",
    currentApi: "/page/adminArticlePage, /article/admin/review/*, /tag/page, /tag/list, /tag/*",
    backendGap: "待补全站内容分页、详情字段与审核记录查询",
    priority: "high",
  },
  {
    service: "chat-book-cloud-interaction",
    responsibility: "评论治理、通知抽查、异常行为聚合",
    currentApi: "仅前台用户视角接口",
    backendGap: "缺少后台评论分页、屏蔽/删除、告警中心接口",
    priority: "high",
  },
];

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

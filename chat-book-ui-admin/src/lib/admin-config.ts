import type { NavGroup } from "@/lib/types";

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

export function findCurrentNav(pathname: string) {
  return adminNavigation.flatMap((group) => group.items).find((item) => item.href === pathname);
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

import {
  adminTags,
  adminUsers,
  contentArticles,
  dashboardSnapshot,
  interactionEvents,
  reviewArticles
} from "@/lib/mock-data";
import type {
  AdminArticle,
  AdminTag,
  AdminUser,
  DashboardSnapshot,
  InteractionEvent
} from "@/lib/types";

function clone<T>(value: T): T {
  return JSON.parse(JSON.stringify(value)) as T;
}

export async function getDashboardSnapshot(): Promise<DashboardSnapshot> {
  // 当前建议对接:
  // - GET /user/admin/count
  // - POST /page/adminArticlePage
  // 其余统计暂用 mock 占位，后续补充 interaction/admin/* 接口。
  return clone(dashboardSnapshot);
}

export async function getUsers(): Promise<AdminUser[]> {
  // 当前建议对接:
  // - GET /user/admin/user?page=1&size=20
  return clone(adminUsers);
}

export async function getReviewArticles(): Promise<AdminArticle[]> {
  // 当前建议对接:
  // - POST /page/adminArticlePage
  // - GET /article/query?id=*
  return clone(reviewArticles);
}

export async function getContentArticles(): Promise<AdminArticle[]> {
  // 当前建议对接:
  // - POST /page/userArticlePage
  // - POST /page/userDraftArticlePage
  // 后续需要新增管理员全站内容分页接口。
  return clone(contentArticles);
}

export async function getTags(): Promise<AdminTag[]> {
  // 当前建议对接:
  // - POST /tag/page
  // - GET /tag/list
  // - POST /tag/create
  // - POST /tag/update
  // - DELETE /tag/delete
  return clone(adminTags);
}

export async function getInteractionEvents(): Promise<InteractionEvent[]> {
  // 当前后端只有面向当前登录用户的通知与评论接口。
  // 后续需要补充后台管理员可用的全站互动聚合接口。
  return clone(interactionEvents);
}

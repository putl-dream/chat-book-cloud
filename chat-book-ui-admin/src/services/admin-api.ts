import { dashboardHighlights, dashboardServices } from "@/data/admin-config";
import {
  clearAdminSession,
  getBrowserApiBaseUrl,
  normalizeLoginVO,
  readAccessToken,
  readRefreshToken,
  saveAdminSession,
} from "@/services/auth";
import type { LoginVO } from "@/services/auth";
import type {
  AdminArticle,
  AdminCount,
  AdminTag,
  AdminTagFormValues,
  AdminUser,
  ArticleReviewResult,
  CommonApiResponse,
  ContentArticle,
  ContentPageParams,
  CurrentAdminUser,
  DashboardSnapshot,
  InteractionReview,
  InteractionReviewPage,
  PaginatedResult,
  ReviewAction,
  ReviewArticle,
} from "@/types/admin";

type BackendUserPage<T> = {
  records?: T[];
  total?: number;
  current?: number;
  size?: number;
  pages?: number;
};

type BackendPageResult<T> = {
  list?: T[];
  total?: number;
};

type BackendReviewArticle = {
  id: number;
  userId: number;
  title: string;
  cover?: string | null;
  abstractText?: string | null;
  userName: string;
  authorAvatar?: string | null;
  category: number;
  praiseCount?: number | null;
  commentCount?: number | null;
  viewCount?: number | null;
  collectCount?: number | null;
  createTime?: string | null;
};

function clone<T>(value: T): T {
  return JSON.parse(JSON.stringify(value)) as T;
}

function formatCount(value: number) {
  return new Intl.NumberFormat("zh-CN").format(value);
}

function toNumber(value: number | string | null | undefined) {
  if (typeof value === "number") {
    return Number.isFinite(value) ? value : 0;
  }

  if (typeof value === "string" && value.trim()) {
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : 0;
  }

  return 0;
}

async function parseResponseBody<T>(response: Response) {
  const text = await response.text();

  if (!text) {
    return null as T | null;
  }

  try {
    return JSON.parse(text) as T;
  } catch {
    return text as T;
  }
}

export class BrowserApiError extends Error {
  status: number;
  code?: number;

  constructor(message: string, status: number, code?: number) {
    super(message);
    this.name = "BrowserApiError";
    this.status = status;
    this.code = code;
  }
}

// ==================== Token 刷新状态 ====================
let isRefreshing = false;
let refreshingPromise: Promise<boolean> | null = null;

/**
 * 尝试刷新 Token，成功返回 true
 */
async function tryRefreshToken(): Promise<boolean> {
  if (isRefreshing && refreshingPromise) {
    return refreshingPromise;
  }

  const refreshToken = readRefreshToken();
  if (!refreshToken) {
    return false;
  }

  isRefreshing = true;

  refreshingPromise = (async () => {
    try {
      const response = await fetch(
        `${getBrowserApiBaseUrl()}/auth/account/refresh`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ refreshToken }),
        }
      );
      const body = await parseResponseBody<CommonApiResponse<LoginVO> | { msg?: string }>(response);
      const data = body && typeof body === 'object' && 'data' in body ? (body as CommonApiResponse<LoginVO>).data : null;
      const loginVO = normalizeLoginVO(data);
      if (loginVO?.accessToken) {
        saveAdminSession(loginVO);
        return true;
      }
      return false;
    } catch {
      return false;
    } finally {
      isRefreshing = false;
      refreshingPromise = null;
    }
  })();

  return refreshingPromise;
}

async function requestBrowser<T>(
  path: string,
  init?: RequestInit,
  options?: {
    auth?: boolean;
    redirectOnUnauthorized?: boolean;
    refreshOnUnauthorized?: boolean;
    _retryCount?: number;
  }
): Promise<T> {
  const authEnabled = options?.auth ?? true;
  const redirectOnUnauthorized = options?.redirectOnUnauthorized ?? true;
  const refreshOnUnauthorized = options?.refreshOnUnauthorized ?? authEnabled;
  const token = readAccessToken();
  const headers = new Headers(init?.headers);

  if (!headers.has("Content-Type") && init?.body) {
    headers.set("Content-Type", "application/json");
  }

  if (authEnabled && token) {
    headers.set("Authorization", `Bearer ${token}`);
    headers.set("token", token);
  }

  const response = await fetch(`${getBrowserApiBaseUrl()}${path}`, {
    ...init,
    headers,
  });

  const body = await parseResponseBody<CommonApiResponse<T> | { msg?: string }>(response);
  const result =
    body && typeof body === "object" && "code" in body
      ? (body as CommonApiResponse<T>)
      : ({ data: body as T, code: response.ok ? 200 : response.status } satisfies CommonApiResponse<T>);

  if (!response.ok || (typeof result.code === "number" && result.code !== 200 && result.code !== 0)) {
    const error = new BrowserApiError(
      result.msg || `请求失败 (${response.status})`,
      response.status,
      result.code
    );

    if (error.status === 401 && refreshOnUnauthorized) {
      const refreshed = await tryRefreshToken();
      if (refreshed && (options?._retryCount ?? 0) === 0) {
        // 刷新成功，重试原请求（最多一次）
        return requestBrowser(path, init, { ...options, _retryCount: (options?._retryCount ?? 0) + 1 });
      }
    }

    if (error.status === 401) {
      clearAdminSession();
      if (redirectOnUnauthorized && typeof window !== "undefined" && window.location.pathname !== "/login") {
        window.location.href = `/login?reason=session-expired`;
      }
      throw error;
    }

    if (error.status === 403 && redirectOnUnauthorized) {
      if (typeof window !== "undefined" && window.location.pathname !== "/forbidden") {
        window.location.href = "/forbidden";
      }
      throw error;
    }

    throw error;
  }

  return result.data as T;
}

function mapUserPage(page: BackendUserPage<AdminUser>): PaginatedResult<AdminUser> {
  const pageNo = toNumber(page.current) || 1;
  const pageSize = toNumber(page.size) || 20;
  const total = toNumber(page.total);
  const totalPages = toNumber(page.pages) || Math.max(1, Math.ceil(total / pageSize));

  return {
    list: page.records ?? [],
    total,
    pageNo,
    pageSize,
    totalPages,
  };
}

function mapPageResult<T>(
  page: BackendPageResult<T>,
  pageNo: number,
  pageSize: number
): PaginatedResult<T> {
  const total = toNumber(page.total);

  return {
    list: page.list ?? [],
    total,
    pageNo,
    pageSize,
    totalPages: Math.max(1, Math.ceil(total / pageSize) || 1),
  };
}

function mapReviewArticle(article: BackendReviewArticle): ReviewArticle {
  return {
    id: article.id,
    userId: article.userId,
    title: article.title,
    cover: article.cover,
    summary: article.abstractText || "接口尚未返回文章摘要。",
    userName: article.userName,
    authorAvatar: article.authorAvatar,
    category: article.category,
    createdAt: article.createTime || "时间未返回",
    praiseCount: toNumber(article.praiseCount),
    commentCount: toNumber(article.commentCount),
    viewCount: toNumber(article.viewCount),
    collectCount: toNumber(article.collectCount),
  };
}

export function loginAdmin(username: string, password: string) {
  return requestBrowser<LoginVO>(
    "/auth/account/login/password",
    {
      method: "POST",
      body: JSON.stringify({
        username,
        password,
      }),
    },
    { auth: false, redirectOnUnauthorized: false }
  ).then((result) => {
    const loginVO = normalizeLoginVO(result);
    if (!loginVO) {
      throw new BrowserApiError("登录接口未返回 refresh token，无法启用 JWT 刷新机制。", 200);
    }
    return loginVO;
  });
}

export function getCurrentAdminUser(options?: {
  redirectOnUnauthorized?: boolean;
  refreshOnUnauthorized?: boolean;
}) {
  return requestBrowser<CurrentAdminUser>("/user/bySelf", { method: "GET" }, options);
}

export async function getDashboardSnapshot(): Promise<DashboardSnapshot> {
  const [count, reviewPage]: [AdminCount, BackendPageResult<BackendReviewArticle>] = await Promise.all([
    requestBrowser<AdminCount>("/user/admin/count", { method: "GET" }),
    requestBrowser<BackendPageResult<BackendReviewArticle>>("/page/adminArticlePage", {
      method: "POST",
      body: JSON.stringify({ pageNo: 1, pageSize: 8 }),
    }),
  ]);

  return {
    metrics: [
      {
        label: "平台用户",
        value: formatCount(toNumber(count.userCount)),
        detail: "来自 /user/admin/count 的真实用户统计",
        trend: "实时",
      },
      {
        label: "文章总量",
        value: formatCount(toNumber(count.articleCount)),
        detail: "来自 /user/admin/count 的真实文章数据",
        trend: "实时",
      },
      {
        label: "待审核文章",
        value: formatCount(toNumber(reviewPage.total)),
        detail: "来自 /page/adminArticlePage 的待审队列",
        trend: "实时",
      },
      {
        label: "互动告警",
        value: "--",
        detail: "聚合接口尚未补齐，当前保留占位视图",
        trend: "待接入",
      },
    ],
    highlights: dashboardHighlights,
    services: dashboardServices,
  };
}

export async function getUsersPage(params?: {
  page?: number;
  size?: number;
}): Promise<PaginatedResult<AdminUser>> {
  const page = params?.page ?? 1;
  const size = params?.size ?? 20;
  const searchParams = new URLSearchParams({
    page: String(page),
    size: String(size),
  });

  const result = await requestBrowser<BackendUserPage<AdminUser>>(
    `/user/admin/user?${searchParams.toString()}`,
    { method: "GET" }
  );

  return mapUserPage(result);
}

export async function getReviewArticlesPage(params?: {
  page?: number;
  size?: number;
}): Promise<PaginatedResult<ReviewArticle>> {
  const page = params?.page ?? 1;
  const size = params?.size ?? 8;
  const result = await requestBrowser<BackendPageResult<BackendReviewArticle>>(
    "/page/adminArticlePage",
    {
      method: "POST",
      body: JSON.stringify({ pageNo: page, pageSize: size }),
    }
  );

  const mappedPage = mapPageResult(result, page, size);

  return {
    ...mappedPage,
    list: mappedPage.list.map(mapReviewArticle),
  };
}

export async function getTagsPage(params?: {
  page?: number;
  size?: number;
  type?: number;
}): Promise<PaginatedResult<AdminTag>> {
  const page = params?.page ?? 1;
  const size = params?.size ?? 12;
  const result = await requestBrowser<BackendPageResult<AdminTag>>(
    "/tag/page",
    {
      method: "POST",
      body: JSON.stringify({
        pageNo: page,
        pageSize: size,
        type: params?.type ?? undefined,
      }),
    }
  );

  return mapPageResult(result, page, size);
}

export function getTagList() {
  return requestBrowser<AdminTag[]>("/tag/list", { method: "GET" });
}

export function createTag(values: AdminTagFormValues) {
  return requestBrowser<AdminTag>(
    "/tag/create",
    {
      method: "POST",
      body: JSON.stringify(values),
    },
    { redirectOnUnauthorized: true }
  );
}

export function updateTag(values: AdminTagFormValues) {
  return requestBrowser<void>(
    "/tag/update",
    {
      method: "POST",
      body: JSON.stringify(values),
    },
    { redirectOnUnauthorized: true }
  );
}

export function deleteTag(tagId: number) {
  return requestBrowser<void>(`/tag/delete?tagId=${tagId}`, { method: "DELETE" }, { redirectOnUnauthorized: true });
}

export function approveReviewArticle(articleId: number) {
  return requestBrowser<ArticleReviewResult>(
    "/article/admin/review/approve",
    {
      method: "POST",
      body: JSON.stringify({ articleId }),
    },
    { redirectOnUnauthorized: true }
  );
}

export function rejectReviewArticle(articleId: number, reason: string) {
  return requestBrowser<ArticleReviewResult>(
    "/article/admin/review/reject",
    {
      method: "POST",
      body: JSON.stringify({ articleId, reason }),
    },
    { redirectOnUnauthorized: true }
  );
}

export function batchReviewArticles(articleIds: number[], action: ReviewAction, reason?: string) {
  return requestBrowser<ArticleReviewResult[]>(
    "/article/admin/review/batch",
    {
      method: "POST",
      body: JSON.stringify({ articleIds, action, reason }),
    },
    { redirectOnUnauthorized: true }
  );
}

export async function getContentArticlesPage(params?: ContentPageParams): Promise<PaginatedResult<ContentArticle>> {
  const pageNo = params?.pageNo ?? 1;
  const pageSize = params?.pageSize ?? 10;
  const searchParams = new URLSearchParams();
  searchParams.set("pageNo", String(pageNo));
  searchParams.set("pageSize", String(pageSize));
  if (params?.status != null) searchParams.set("status", String(params.status));
  if (params?.category != null) searchParams.set("category", String(params.category));
  if (params?.contentType != null) searchParams.set("contentType", String(params.contentType));
  if (params?.userId != null) searchParams.set("userId", String(params.userId));
  if (params?.keyword) searchParams.set("keyword", params.keyword);
  if (params?.orderDirection) searchParams.set("orderDirection", params.orderDirection);

  const result = await requestBrowser<{ list: ContentArticle[]; total: number }>(
    `/article/admin/page?${searchParams.toString()}`,
    { method: "GET" }
  );

  return mapPageResult(result, pageNo, pageSize);
}

export async function publishArticle(articleId: number) {
  return requestBrowser<void>(`/article/admin/${articleId}/publish`, { method: "PUT" }, { redirectOnUnauthorized: true });
}

export async function unpublishArticle(articleId: number) {
  return requestBrowser<void>(`/article/admin/${articleId}/unpublish`, { method: "PUT" }, { redirectOnUnauthorized: true });
}

export async function deleteArticle(articleId: number) {
  return requestBrowser<void>(`/article/admin/${articleId}`, { method: "DELETE" }, { redirectOnUnauthorized: true });
}

export async function restoreArticle(articleId: number) {
  return requestBrowser<void>(`/article/admin/${articleId}/restore`, { method: "PUT" }, { redirectOnUnauthorized: true });
}

export async function batchPublish(articleIds: number[]) {
  return requestBrowser<void>("/article/admin/batch/publish", {
    method: "PUT",
    body: JSON.stringify(articleIds),
  }, { redirectOnUnauthorized: true });
}

export async function batchUnpublish(articleIds: number[]) {
  return requestBrowser<void>("/article/admin/batch/unpublish", {
    method: "PUT",
    body: JSON.stringify(articleIds),
  }, { redirectOnUnauthorized: true });
}

export async function batchDeleteArticles(articleIds: number[]) {
  return requestBrowser<void>("/article/admin/batch", {
    method: "DELETE",
    body: JSON.stringify(articleIds),
  }, { redirectOnUnauthorized: true });
}

export async function getInteractionReviewsPage(params?: {
  page?: number;
  size?: number;
  articleId?: number | null;
  userId?: number | null;
  keyword?: string | null;
  status?: number | null;
  startTime?: string | null;
  endTime?: string | null;
}): Promise<PaginatedResult<InteractionReview>> {
  const page = params?.page ?? 1;
  const size = params?.size ?? 10;
  const searchParams = new URLSearchParams();
  searchParams.set("page", String(page));
  searchParams.set("size", String(size));
  if (params?.articleId != null) searchParams.set("articleId", String(params.articleId));
  if (params?.userId != null) searchParams.set("userId", String(params.userId));
  if (params?.keyword) searchParams.set("keyword", params.keyword);
  if (params?.status != null) searchParams.set("status", String(params.status));
  if (params?.startTime) searchParams.set("startTime", params.startTime);
  if (params?.endTime) searchParams.set("endTime", params.endTime);

  const result = await requestBrowser<InteractionReviewPage>(
    `/interaction/admin/review/page?${searchParams.toString()}`,
    { method: "GET" }
  );

  const pageNo = toNumber(result.current) || 1;
  const pageSize = toNumber(result.size) || size;
  const total = toNumber(result.total);
  const totalPages = toNumber(result.pages) || Math.max(1, Math.ceil(total / pageSize));

  return {
    list: result.records ?? [],
    total,
    pageNo,
    pageSize,
    totalPages,
  };
}

export async function deleteReview(reviewId: number) {
  return requestBrowser<void>(`/interaction/admin/review/${reviewId}`, { method: "DELETE" }, { redirectOnUnauthorized: true });
}

export async function hideReview(reviewId: number) {
  return requestBrowser<void>(`/interaction/admin/review/${reviewId}/hide`, { method: "PUT" }, { redirectOnUnauthorized: true });
}

export async function restoreReview(reviewId: number) {
  return requestBrowser<void>(`/interaction/admin/review/${reviewId}/restore`, { method: "PUT" }, { redirectOnUnauthorized: true });
}

export async function updateUserRole(userId: number, role: string) {
  return requestBrowser<void>(`/user/admin/${userId}/role`, {
    method: "PUT",
    body: JSON.stringify({ role }),
  }, { redirectOnUnauthorized: true });
}

export async function disableUser(userId: number) {
  return requestBrowser<void>(`/user/admin/${userId}/disable`, { method: "PUT" }, { redirectOnUnauthorized: true });
}

export async function enableUser(userId: number) {
  return requestBrowser<void>(`/user/admin/${userId}/enable`, { method: "PUT" }, { redirectOnUnauthorized: true });
}

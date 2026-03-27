import "server-only";

import { cache } from "react";
import { cookies, headers } from "next/headers";
import { redirect } from "next/navigation";
import { ADMIN_SESSION_COOKIE } from "@/lib/auth";
import { dashboardHighlights, dashboardServices } from "@/lib/admin-config";
import { contentArticles, interactionEvents } from "@/lib/mock-data";
import type {
  AdminArticle,
  AdminCount,
  AdminSession,
  AdminTag,
  AdminUser,
  CommonApiResponse,
  CurrentAdminUser,
  DashboardSnapshot,
  InteractionEvent,
  PaginatedResult,
  ReviewArticle,
} from "@/lib/types";

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

function normalizeApiBase(baseUrl: string) {
  const normalized = baseUrl.replace(/\/$/, "");
  return normalized.endsWith("/api") ? normalized : `${normalized}/api`;
}

function readForwardedValue(value: string | null) {
  return value?.split(",")[0]?.trim() ?? "";
}

function normalizeHost(host: string) {
  return host.replace(/^[a-z]+:\/\//i, "").trim();
}

// ─── API Base URL: 进程级缓存，避免每次请求重复读 headers() ───────────────────
// 仅在没有配置 API_BASE_URL 环境变量时才需要从 headers 推断，生产环境几乎不会走到该分支。
let _resolvedApiBaseUrl: string | null = null;

async function getServerApiBaseUrl() {
  // 如果环境变量已配置（通常情况），直接返回，无需任何异步 I/O
  const configuredBase =
    process.env.API_BASE_URL?.trim() ?? process.env.NEXT_PUBLIC_API_BASE_URL?.trim() ?? "";

  if (configuredBase) {
    if (!_resolvedApiBaseUrl) {
      _resolvedApiBaseUrl = normalizeApiBase(configuredBase);
    }
    return _resolvedApiBaseUrl;
  }

  // Fallback: 从请求 headers 推断（开发环境未配置环境变量时）
  // 这里不缓存，因为 host 可能在不同请求中不同（反向代理场景）
  const headerStore = await headers();
  const host = normalizeHost(
    readForwardedValue(headerStore.get("x-forwarded-host")) ||
      readForwardedValue(headerStore.get("host")) ||
      "localhost:3000"
  );
  const forwardedProtocol = readForwardedValue(headerStore.get("x-forwarded-proto"));
  const protocol =
    forwardedProtocol === "http" || forwardedProtocol === "https"
      ? forwardedProtocol
      : host.includes("localhost") || host.startsWith("127.0.0.1") || host.startsWith("[::1]")
        ? "http"
        : "https";

  try {
    return normalizeApiBase(new URL(`${protocol}://${host}`).origin);
  } catch {
    return "http://localhost:3000/api";
  }
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

export class AdminApiError extends Error {
  status: number;
  code?: number;

  constructor(message: string, status: number, code?: number) {
    super(message);
    this.name = "AdminApiError";
    this.status = status;
    this.code = code;
  }
}

// ─── 核心请求函数 ─────────────────────────────────────────────────────────────
// revalidate: undefined = 遵循 Next.js 默认（静态缓存）
// revalidate: 0         = 每次请求都重新获取（等同于原先的 cache: "no-store"，但允许 dedupe）
// revalidate: N         = N 秒后重新验证
async function requestServer<T>(
  path: string,
  init?: RequestInit,
  options?: { token?: string; auth?: boolean; revalidate?: number }
) {
  const authEnabled = options?.auth ?? true;
  const cookieStore = await cookies();
  const token = options?.token ?? cookieStore.get(ADMIN_SESSION_COOKIE)?.value ?? "";
  const apiBaseUrl = await getServerApiBaseUrl();
  const headersInit = new Headers(init?.headers);

  headersInit.set("Content-Type", headersInit.get("Content-Type") ?? "application/json");

  if (authEnabled && token) {
    headersInit.set("Authorization", `Bearer ${token}`);
    headersInit.set("token", token);
  }

  // 构建 next 缓存配置
  // revalidate=0 表示"每次都重新获取但允许同一渲染内去重"，比 cache:"no-store" 更优
  const nextConfig: { revalidate?: number } = {};
  if (options?.revalidate !== undefined) {
    nextConfig.revalidate = options.revalidate;
  }

  let response: Response;

  try {
    response = await fetch(`${apiBaseUrl}${path}`, {
      ...init,
      headers: headersInit,
      // 使用 next.revalidate 替代 cache:"no-store"，允许同一渲染周期内请求去重
      next: Object.keys(nextConfig).length > 0 ? nextConfig : { revalidate: 0 },
    });
  } catch (error) {
    const message =
      error instanceof Error && error.message
        ? `Admin API request failed: ${error.message}`
        : "Admin API request failed. Please verify the gateway URL and backend availability.";

    throw new AdminApiError(message, 503);
  }

  const body = await parseResponseBody<CommonApiResponse<T> | { msg?: string }>(response);
  const result =
    body && typeof body === "object" && "code" in body
      ? (body as CommonApiResponse<T>)
      : ({ data: body as T, code: response.ok ? 200 : response.status } satisfies CommonApiResponse<T>);

  if (!response.ok) {
    throw new AdminApiError(
      result.msg || `请求失败 (${response.status})`,
      response.status,
      result.code
    );
  }

  if (typeof result.code === "number" && result.code !== 200 && result.code !== 0) {
    throw new AdminApiError(result.msg || "接口返回异常", response.status, result.code);
  }

  return result.data as T;
}

// ─── 数据映射 ─────────────────────────────────────────────────────────────────

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
    summary: article.abstractText || "接口未返回文章摘要",
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

// ─── 业务 API ─────────────────────────────────────────────────────────────────

/**
 * requireAdminSession
 * 使用 React cache() 包装：在同一次 SSR 渲染树中，无论被调用多少次（Layout + Page 各一次），
 * 只向后端发起一次请求，彻底消除重复验证开销。
 */
export const requireAdminSession = cache(async (): Promise<AdminSession> => {
  const cookieStore = await cookies();
  const token = cookieStore.get(ADMIN_SESSION_COOKIE)?.value;

  if (!token) {
    redirect("/login");
  }

  try {
    const user = await requestServer<CurrentAdminUser>(
      "/user/bySelf",
      { method: "GET" },
      // session 验证必须实时，不缓存
      { token, revalidate: 0 }
    );

    if (user.role !== "admin") {
      redirect("/forbidden");
    }

    return { token: token ?? "", user };
  } catch (error) {
    if (error instanceof AdminApiError && error.status === 401) {
      redirect("/login?reason=session-expired");
    }

    if (error instanceof AdminApiError && error.status === 403) {
      redirect("/forbidden");
    }

    throw error;
  }
});

/**
 * getDashboardSnapshot
 * 首页统计数据：30 秒缓存，统计数字轻微延迟影响不大，但大幅减少后端压力和等待时间。
 */
export async function getDashboardSnapshot(token?: string): Promise<DashboardSnapshot> {
  const [count, reviewPage] = await Promise.all([
    requestServer<AdminCount>(
      "/user/admin/count",
      { method: "GET" },
      { token, revalidate: 30 }
    ),
    requestServer<BackendPageResult<BackendReviewArticle>>(
      "/page/adminArticlePage",
      {
        method: "POST",
        body: JSON.stringify({ pageNo: 1, pageSize: 8 }),
      },
      { token, revalidate: 30 }
    ),
  ]);

  return {
    metrics: [
      {
        label: "平台用户",
        value: formatCount(toNumber(count.userCount)),
        detail: "来自 `/user/admin/count` 的真实用户统计",
        trend: "实时",
      },
      {
        label: "文章总量",
        value: formatCount(toNumber(count.articleCount)),
        detail: "来自 `/user/admin/count` 的真实文章总数",
        trend: "实时",
      },
      {
        label: "待审核文章",
        value: formatCount(toNumber(reviewPage.total)),
        detail: "来自 `/page/adminArticlePage` 的真实待审队列",
        trend: "实时",
      },
      {
        label: "互动告警",
        value: "--",
        detail: "后台聚合接口未补齐，当前仅保留未接入标记",
        trend: "未接入",
      },
    ],
    highlights: dashboardHighlights,
    services: dashboardServices,
  };
}

/**
 * getUsersPage
 * 用户列表：10 秒缓存，分页数据变化不频繁，可接受轻微延迟。
 */
export async function getUsersPage(params?: {
  page?: number;
  size?: number;
  token?: string;
}): Promise<PaginatedResult<AdminUser>> {
  const page = params?.page ?? 1;
  const size = params?.size ?? 20;
  const searchParams = new URLSearchParams({
    page: String(page),
    size: String(size),
  });

  const result = await requestServer<BackendUserPage<AdminUser>>(
    `/user/admin/user?${searchParams.toString()}`,
    { method: "GET" },
    { token: params?.token, revalidate: 10 }
  );

  return mapUserPage(result);
}

/**
 * getReviewArticlesPage
 * 审核队列：实时，不缓存（revalidate: 0）。审核结果必须即时反映。
 */
export async function getReviewArticlesPage(params?: {
  page?: number;
  size?: number;
  token?: string;
}): Promise<PaginatedResult<ReviewArticle>> {
  const page = params?.page ?? 1;
  const size = params?.size ?? 10;
  const result = await requestServer<BackendPageResult<BackendReviewArticle>>(
    "/page/adminArticlePage",
    {
      method: "POST",
      body: JSON.stringify({ pageNo: page, pageSize: size }),
    },
    { token: params?.token, revalidate: 0 }
  );

  const mappedPage = mapPageResult(result, page, size);

  return {
    ...mappedPage,
    list: mappedPage.list.map(mapReviewArticle),
  };
}

/**
 * getTagsPage / getTagList
 * 标签数据：60 秒缓存，标签很少变动，可以大胆缓存。
 */
export async function getTagsPage(params?: {
  page?: number;
  size?: number;
  type?: number;
  token?: string;
}): Promise<PaginatedResult<AdminTag>> {
  const page = params?.page ?? 1;
  const size = params?.size ?? 12;
  const result = await requestServer<BackendPageResult<AdminTag>>(
    "/tag/page",
    {
      method: "POST",
      body: JSON.stringify({
        pageNo: page,
        pageSize: size,
        type: params?.type ?? undefined,
      }),
    },
    { token: params?.token, revalidate: 60 }
  );

  return mapPageResult(result, page, size);
}

export async function getTagList(token?: string): Promise<AdminTag[]> {
  return requestServer<AdminTag[]>(
    "/tag/list",
    { method: "GET" },
    { token, revalidate: 60 }
  );
}

export async function getContentArticles(): Promise<AdminArticle[]> {
  return clone(contentArticles);
}

export async function getInteractionEvents(): Promise<InteractionEvent[]> {
  return clone(interactionEvents);
}

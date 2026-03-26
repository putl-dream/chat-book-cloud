import "server-only";

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

async function getServerOrigin() {
  const headerStore = await headers();
  const host =
    headerStore.get("x-forwarded-host") ?? headerStore.get("host") ?? "localhost:3000";
  const protocol =
    headerStore.get("x-forwarded-proto") ??
    (host.includes("localhost") || host.startsWith("127.0.0.1") ? "http" : "https");

  return `${protocol}://${host}`;
}

async function getServerApiBaseUrl() {
  const configuredBase =
    process.env.API_BASE_URL?.trim() ?? process.env.NEXT_PUBLIC_API_BASE_URL?.trim() ?? "";

  if (configuredBase) {
    return normalizeApiBase(configuredBase);
  }

  return normalizeApiBase(await getServerOrigin());
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

async function requestServer<T>(
  path: string,
  init?: RequestInit,
  options?: { token?: string; auth?: boolean }
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

  const response = await fetch(`${apiBaseUrl}${path}`, {
    ...init,
    headers: headersInit,
    cache: "no-store",
  });

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

export async function requireAdminSession(): Promise<AdminSession> {
  const cookieStore = await cookies();
  const token = cookieStore.get(ADMIN_SESSION_COOKIE)?.value;

  if (!token) {
    redirect("/login");
  }

  try {
    const user = await requestServer<CurrentAdminUser>("/user/bySelf", { method: "GET" }, { token });

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
}

export async function getDashboardSnapshot(token?: string): Promise<DashboardSnapshot> {
  const [count, reviewPage] = await Promise.all([
    requestServer<AdminCount>("/user/admin/count", { method: "GET" }, { token }),
    requestServer<BackendPageResult<BackendReviewArticle>>(
      "/page/adminArticlePage",
      {
        method: "POST",
        body: JSON.stringify({ pageNo: 1, pageSize: 8 }),
      },
      { token }
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
    { token: params?.token }
  );

  return mapUserPage(result);
}

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
    { token: params?.token }
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
    { token: params?.token }
  );

  return mapPageResult(result, page, size);
}

export async function getTagList(token?: string): Promise<AdminTag[]> {
  return requestServer<AdminTag[]>("/tag/list", { method: "GET" }, { token });
}

export async function getContentArticles(): Promise<AdminArticle[]> {
  return clone(contentArticles);
}

export async function getInteractionEvents(): Promise<InteractionEvent[]> {
  return clone(interactionEvents);
}

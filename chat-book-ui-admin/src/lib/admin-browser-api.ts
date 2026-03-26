import { clearAdminSession, readClientToken } from "@/lib/auth";
import type {
  ArticleReviewResult,
  AdminTag,
  AdminTagFormValues,
  CommonApiResponse,
  CurrentAdminUser,
  ReviewAction,
} from "@/lib/types";

function normalizeApiBase(baseUrl: string) {
  const normalized = baseUrl.replace(/\/$/, "");
  return normalized.endsWith("/api") ? normalized : `${normalized}/api`;
}

function getBrowserApiBaseUrl() {
  const configuredBase = process.env.NEXT_PUBLIC_API_BASE_URL?.trim() ?? "";
  return configuredBase ? normalizeApiBase(configuredBase) : "/api";
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

async function requestBrowser<T>(
  path: string,
  init?: RequestInit,
  options?: { auth?: boolean; redirectOnUnauthorized?: boolean }
) {
  const authEnabled = options?.auth ?? true;
  const redirectOnUnauthorized = options?.redirectOnUnauthorized ?? true;
  const token = readClientToken();
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

    if ((error.status === 401 || error.status === 403) && redirectOnUnauthorized) {
      clearAdminSession();

      if (typeof window !== "undefined" && window.location.pathname !== "/login") {
        const reason = error.status === 403 ? "forbidden" : "session-expired";
        window.location.href = `/login?reason=${reason}`;
      }
    }

    throw error;
  }

  return result.data as T;
}

export function loginAdmin(username: string, password: string) {
  return requestBrowser<string>(
    "/auth/account/login",
    {
      method: "POST",
      body: JSON.stringify({
        username,
        password,
        loginMethod: "PASSWORD",
      }),
    },
    { auth: false, redirectOnUnauthorized: false }
  );
}

export function getCurrentAdminUser() {
  return requestBrowser<CurrentAdminUser>("/user/bySelf", { method: "GET" });
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

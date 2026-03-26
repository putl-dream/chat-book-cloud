import Link from "next/link";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { cn } from "@/lib/utils";

type QueryValue = string | number | undefined | null;

export interface PaginationControlsProps {
  pathname: string;
  page: number;
  pageSize: number;
  total: number;
  totalPages: number;
  query?: Record<string, QueryValue>;
}

function buildHref(
  pathname: string,
  targetPage: number,
  pageSize: number,
  query?: Record<string, QueryValue>
) {
  const searchParams = new URLSearchParams();

  Object.entries(query ?? {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      searchParams.set(key, String(value));
    }
  });

  if (targetPage > 1) {
    searchParams.set("page", String(targetPage));
  }

  if (pageSize !== 20) {
    searchParams.set("size", String(pageSize));
  }

  const queryString = searchParams.toString();
  return queryString ? `${pathname}?${queryString}` : pathname;
}

export function PaginationControls({
  pathname,
  page,
  pageSize,
  total,
  totalPages,
  query,
}: PaginationControlsProps) {
  if (total <= 0) {
    return null;
  }

  return (
    <div className="pagination-bar">
      <p className="text-cb-text-secondary text-sm">
        第 <strong>{page}</strong> / <strong>{totalPages}</strong> 页，共 <strong>{total}</strong> 条
      </p>

      <div className="pagination-actions">
        <Link
          aria-disabled={page <= 1}
          className={cn("pagination-button", page <= 1 && "is-disabled")}
          href={buildHref(pathname, Math.max(1, page - 1), pageSize, query)}
          prefetch={false}
        >
          <ChevronLeft className="h-4 w-4" />
          <span>上一页</span>
        </Link>

        <Link
          aria-disabled={page >= totalPages}
          className={cn("pagination-button", page >= totalPages && "is-disabled")}
          href={buildHref(pathname, Math.min(totalPages, page + 1), pageSize, query)}
          prefetch={false}
        >
          <span>下一页</span>
          <ChevronRight className="h-4 w-4" />
        </Link>
      </div>
    </div>
  );
}

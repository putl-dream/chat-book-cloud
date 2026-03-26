"use client";

import Link from "next/link";
import { type FormEvent, startTransition, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { PaginationControls } from "@/components/shared/pagination-controls";
import {
  approveReviewArticle,
  batchReviewArticles,
  BrowserApiError,
  rejectReviewArticle,
} from "@/lib/admin-browser-api";
import { articleCategoryMap } from "@/lib/admin-config";
import type { PaginatedResult, ReviewAction, ReviewArticle } from "@/lib/types";

export interface ReviewQueueProps {
  initialPage: PaginatedResult<ReviewArticle>;
  focusedId?: number;
}

type RejectDialogState =
  | {
      mode: "single" | "batch";
      articleIds: number[];
      title: string;
    }
  | null;

function buildReviewHref(page: number, pageSize: number, focus?: number) {
  const searchParams = new URLSearchParams();

  if (focus && focus > 0) {
    searchParams.set("focus", String(focus));
  }

  if (page > 1) {
    searchParams.set("page", String(page));
  }

  if (pageSize !== 8) {
    searchParams.set("size", String(pageSize));
  }

  const queryString = searchParams.toString();
  return queryString ? `/articles/review?${queryString}` : "/articles/review";
}

function calculateTotalPages(total: number, pageSize: number) {
  return Math.max(1, Math.ceil(total / pageSize));
}

export function ReviewQueue({ initialPage, focusedId }: ReviewQueueProps) {
  const router = useRouter();
  const [reviewPage, setReviewPage] = useState(initialPage);
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const [rejectDialog, setRejectDialog] = useState<RejectDialogState>(null);
  const [rejectReason, setRejectReason] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState("");
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    setReviewPage(initialPage);
    setSelectedIds([]);
  }, [initialPage]);

  function clearFeedback() {
    setMessage("");
    setErrorMessage("");
  }

  function toggleSelection(articleId: number) {
    setSelectedIds((current) =>
      current.includes(articleId)
        ? current.filter((id) => id !== articleId)
        : [...current, articleId]
    );
  }

  function applyReviewedArticles(articleIds: number[]) {
    const removedIds = new Set(articleIds);
    let nextPageNumber = reviewPage.pageNo;

    setReviewPage((current) => {
      const nextList = current.list.filter((article) => !removedIds.has(article.id));
      const removedCount = current.list.length - nextList.length;
      const nextTotal = Math.max(0, current.total - removedCount);
      const shouldStepBack = nextList.length === 0 && current.pageNo > 1 && nextTotal > 0;
      nextPageNumber = shouldStepBack ? current.pageNo - 1 : current.pageNo;

      return {
        ...current,
        list: nextList,
        total: nextTotal,
        pageNo: nextPageNumber,
        totalPages: calculateTotalPages(nextTotal, current.pageSize),
      };
    });

    setSelectedIds((current) => current.filter((id) => !removedIds.has(id)));

    startTransition(() => {
      const nextFocus = focusedId && !removedIds.has(focusedId) ? focusedId : undefined;
      router.replace(buildReviewHref(nextPageNumber, reviewPage.pageSize, nextFocus));
      router.refresh();
    });
  }

  async function runReviewAction(articleIds: number[], action: ReviewAction, reason?: string) {
    setSubmitting(true);
    clearFeedback();

    try {
      if (articleIds.length === 1) {
        if (action === "APPROVE") {
          await approveReviewArticle(articleIds[0]);
          setMessage(`文章 #${articleIds[0]} 已审核通过。`);
        } else {
          await rejectReviewArticle(articleIds[0], reason ?? "");
          setMessage(`文章 #${articleIds[0]} 已驳回并退回草稿。`);
        }
      } else {
        await batchReviewArticles(articleIds, action, reason);
        setMessage(
          action === "APPROVE"
            ? `已批量通过 ${articleIds.length} 篇文章。`
            : `已批量驳回 ${articleIds.length} 篇文章，并退回草稿。`
        );
      }

      applyReviewedArticles(articleIds);
      setRejectDialog(null);
      setRejectReason("");
    } catch (error) {
      setErrorMessage(
        error instanceof BrowserApiError ? error.message : "审核操作失败，请稍后重试。"
      );
    } finally {
      setSubmitting(false);
    }
  }

  async function handleApprove(articleId: number) {
    if (!window.confirm(`确认通过文章 #${articleId} 吗？`)) {
      return;
    }

    await runReviewAction([articleId], "APPROVE");
  }

  async function handleBatchApprove() {
    if (selectedIds.length === 0) {
      setErrorMessage("请先选择要批量处理的文章。");
      return;
    }

    if (!window.confirm(`确认批量通过选中的 ${selectedIds.length} 篇文章吗？`)) {
      return;
    }

    await runReviewAction(selectedIds, "APPROVE");
  }

  function openSingleReject(article: ReviewArticle) {
    clearFeedback();
    setRejectReason("");
    setRejectDialog({
      mode: "single",
      articleIds: [article.id],
      title: `驳回文章 #${article.id}`,
    });
  }

  function openBatchReject() {
    if (selectedIds.length === 0) {
      setErrorMessage("请先选择要批量驳回的文章。");
      return;
    }

    clearFeedback();
    setRejectReason("");
    setRejectDialog({
      mode: "batch",
      articleIds: selectedIds,
      title: `批量驳回 ${selectedIds.length} 篇文章`,
    });
  }

  async function handleRejectSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!rejectDialog) {
      return;
    }

    if (!rejectReason.trim()) {
      setErrorMessage("请输入驳回原因，便于后续追溯与作者修改。");
      return;
    }

    const confirmMessage =
      rejectDialog.mode === "single"
        ? `确认驳回文章 #${rejectDialog.articleIds[0]} 吗？`
        : `确认批量驳回选中的 ${rejectDialog.articleIds.length} 篇文章吗？`;

    if (!window.confirm(confirmMessage)) {
      return;
    }

    await runReviewAction(rejectDialog.articleIds, "REJECT", rejectReason.trim());
  }

  const allSelected =
    reviewPage.list.length > 0 && reviewPage.list.every((article) => selectedIds.includes(article.id));

  return (
    <>
      <div className="stack-list">
        <div className="review-bulk-bar">
          <label className="review-select-toggle">
            <input
              checked={allSelected}
              disabled={submitting || reviewPage.list.length === 0}
              type="checkbox"
              onChange={() => {
                setSelectedIds(allSelected ? [] : reviewPage.list.map((article) => article.id));
              }}
            />
            <span>当前页全选</span>
          </label>

          <div className="inline-actions">
            <span className="pill pill-neutral">已选 {selectedIds.length} 篇</span>
            <button
              className="panel-action-button"
              disabled={submitting || selectedIds.length === 0}
              type="button"
              onClick={() => setSelectedIds([])}
            >
              清空选择
            </button>
            <button
              className="panel-action-button primary"
              disabled={submitting || selectedIds.length === 0}
              type="button"
              onClick={handleBatchApprove}
            >
              {submitting ? "处理中..." : "批量通过"}
            </button>
            <button
              className="panel-action-button"
              disabled={submitting || selectedIds.length === 0}
              type="button"
              onClick={openBatchReject}
            >
              批量驳回
            </button>
          </div>
        </div>

        {message ? <p className="form-message success">{message}</p> : null}
        {errorMessage ? <p className="form-message error">{errorMessage}</p> : null}

        {reviewPage.list.map((article) => (
          <article className="review-card" id={`review-${article.id}`} key={article.id}>
            <div className="review-card-top">
              <div>
                <h4>{article.title}</h4>
                <p className="meta-line">
                  作者 {article.userName} / 分类 {articleCategoryMap[article.category] || "未分类"} / 互动 V{" "}
                  {article.viewCount} · C {article.commentCount} · P {article.praiseCount}
                </p>
              </div>
              <span className="pill pill-warn">待审核</span>
            </div>

            <p className="review-summary">{article.summary}</p>

            <div className="chip-row">
              <span className="chip">标签字段待接口补齐</span>
              <span className="chip">内容类型待接口补齐</span>
              {focusedId === article.id ? <span className="chip">当前聚焦文章</span> : null}
            </div>

            <div className="review-card-actions">
              <label className="review-select-toggle">
                <input
                  checked={selectedIds.includes(article.id)}
                  disabled={submitting}
                  type="checkbox"
                  onChange={() => toggleSelection(article.id)}
                />
                <span>纳入批量处理</span>
              </label>

              <div className="inline-actions">
                <button
                  className="table-action-button"
                  disabled={submitting}
                  type="button"
                  onClick={() => handleApprove(article.id)}
                >
                  通过
                </button>
                <button
                  className="table-action-button danger"
                  disabled={submitting}
                  type="button"
                  onClick={() => openSingleReject(article)}
                >
                  驳回
                </button>
                <Link
                  className="inline-link"
                  href={`/articles/review?page=${reviewPage.pageNo}&size=${reviewPage.pageSize}&focus=${article.id}#review-${article.id}`}
                  prefetch={false}
                >
                  查看详情占位
                </Link>
              </div>
            </div>

            <div className="review-footer">
              <span className="mono">Article #{article.id}</span>
              <span className="mono">{article.createdAt}</span>
            </div>
          </article>
        ))}

        <PaginationControls
          page={reviewPage.pageNo}
          pageSize={reviewPage.pageSize}
          pathname="/articles/review"
          query={focusedId ? { focus: focusedId } : undefined}
          total={reviewPage.total}
          totalPages={reviewPage.totalPages}
        />
      </div>

      {rejectDialog ? (
        <div className="dialog-backdrop" role="presentation">
          <section aria-modal="true" className="dialog-panel" role="dialog">
            <div className="panel-header">
              <div>
                <p className="section-kicker">Reject Review</p>
                <h3>{rejectDialog.title}</h3>
              </div>
              <span className="pill pill-danger">需记录驳回原因</span>
            </div>

            <form className="dialog-form" onSubmit={handleRejectSubmit}>
              <label className="field">
                <span>驳回原因</span>
                <textarea
                  disabled={submitting}
                  placeholder="请填写结构化驳回原因，例如：封面与内容不符、摘要过短、存在违规描述。"
                  rows={5}
                  value={rejectReason}
                  onChange={(event) => setRejectReason(event.target.value)}
                />
              </label>

              <p className="form-message">
                驳回后文章会退回作者草稿态，审核日志会记录审核人、时间、结论和原因。
              </p>

              <div className="dialog-actions">
                <button
                  className="panel-action-button"
                  disabled={submitting}
                  type="button"
                  onClick={() => {
                    setRejectDialog(null);
                    setRejectReason("");
                  }}
                >
                  取消
                </button>
                <button className="panel-action-button primary" disabled={submitting} type="submit">
                  {submitting ? "提交中..." : "确认驳回"}
                </button>
              </div>
            </form>
          </section>
        </div>
      ) : null}
    </>
  );
}

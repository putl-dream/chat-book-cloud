"use client";

import { type FormEvent, startTransition, useEffect, useState } from "react";
import { Pencil, Plus, RefreshCw, Trash2, X } from "lucide-react";
import { useRouter } from "next/navigation";
import { BrowserApiError, createTag, deleteTag, updateTag } from "@/lib/admin-browser-api";
import { tagTypeMap } from "@/lib/admin-config";
import { cn } from "@/lib/utils";
import type { AdminTag, AdminTagFormValues, PaginatedResult } from "@/lib/types";

type DialogMode = "create" | "edit";

export interface TagManagerProps {
  initialPage: PaginatedResult<AdminTag>;
  allTags: AdminTag[];
  currentType?: number;
}

const initialFormState: AdminTagFormValues = {
  name: "",
  type: 1,
  color: "#0f766e",
  sort: 100,
};

function buildHref(page: number, pageSize: number, type?: number) {
  const searchParams = new URLSearchParams();

  if (page > 1) {
    searchParams.set("page", String(page));
  }

  if (pageSize !== 12) {
    searchParams.set("size", String(pageSize));
  }

  if (type) {
    searchParams.set("type", String(type));
  }

  const queryString = searchParams.toString();
  return queryString ? `/tags?${queryString}` : "/tags";
}

function calculateTotalPages(total: number, pageSize: number) {
  return Math.max(1, Math.ceil(total / pageSize));
}

export function TagManager({ initialPage, allTags, currentType }: TagManagerProps) {
  const router = useRouter();
  const [tagPage, setTagPage] = useState(initialPage);
  const [tagPool, setTagPool] = useState(allTags);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [dialogMode, setDialogMode] = useState<DialogMode>("create");
  const [formValues, setFormValues] = useState<AdminTagFormValues>(initialFormState);
  const [message, setMessage] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    setTagPage(initialPage);
  }, [initialPage]);

  useEffect(() => {
    setTagPool(allTags);
  }, [allTags]);

  const techCount = tagPool.filter((tag) => tag.type === 1).length;
  const pathCount = tagPool.filter((tag) => tag.type === 2).length;

  function openCreateDialog() {
    setDialogMode("create");
    setFormValues(initialFormState);
    setMessage("");
    setErrorMessage("");
    setDialogOpen(true);
  }

  function openEditDialog(tag: AdminTag) {
    setDialogMode("edit");
    setFormValues({
      id: tag.id,
      name: tag.name,
      type: tag.type === 2 ? 2 : 1,
      color: tag.color,
      sort: tag.sort,
    });
    setMessage("");
    setErrorMessage("");
    setDialogOpen(true);
  }

  function navigate(page: number, type = currentType) {
    startTransition(() => {
      router.push(buildHref(page, tagPage.pageSize, type));
    });
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!formValues.name.trim()) {
      setErrorMessage("标签名称不能为空。");
      return;
    }

    setSubmitting(true);
    setErrorMessage("");

    try {
      if (dialogMode === "create") {
        const created = await createTag({
          ...formValues,
          name: formValues.name.trim(),
        });

        setTagPool((current) => [created, ...current.filter((tag) => tag.id !== created.id)]);
        setTagPage((current) => {
          const total = current.total + 1;
          const list =
            current.pageNo === 1 &&
            (!currentType || currentType === created.type) &&
            current.list.length < current.pageSize
              ? [created, ...current.list]
              : current.list;

          return {
            ...current,
            list,
            total,
            totalPages: calculateTotalPages(total, current.pageSize),
          };
        });
        setMessage("标签已创建，当前页数据已同步。");
      } else {
        await updateTag({
          ...formValues,
          name: formValues.name.trim(),
        });

        setTagPool((current) =>
          current.map((tag) =>
            tag.id === formValues.id
              ? {
                  ...tag,
                  name: formValues.name.trim(),
                  type: formValues.type,
                  color: formValues.color,
                  sort: formValues.sort,
                }
              : tag
          )
        );
        setTagPage((current) => ({
          ...current,
          list: current.list.map((tag) =>
            tag.id === formValues.id
              ? {
                  ...tag,
                  name: formValues.name.trim(),
                  type: formValues.type,
                  color: formValues.color,
                  sort: formValues.sort,
                }
              : tag
          ),
        }));
        setMessage("标签已更新。");
      }

      setDialogOpen(false);
    } catch (error) {
      setErrorMessage(
        error instanceof BrowserApiError ? error.message : "标签操作失败，请稍后重试。"
      );
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDelete(tag: AdminTag) {
    if (!window.confirm(`确认删除标签「${tag.name}」吗？`)) {
      return;
    }

    setMessage("");
    setErrorMessage("");
    setSubmitting(true);

    try {
      await deleteTag(tag.id);
      setTagPool((current) => current.filter((item) => item.id !== tag.id));
      setTagPage((current) => {
        const total = Math.max(0, current.total - 1);
        const list = current.list.filter((item) => item.id !== tag.id);

        return {
          ...current,
          list,
          total,
          totalPages: calculateTotalPages(total, current.pageSize),
        };
      });
      setMessage("标签已删除。");

      if (tagPage.list.length === 1 && tagPage.pageNo > 1) {
        navigate(tagPage.pageNo - 1);
      }
    } catch (error) {
      setErrorMessage(
        error instanceof BrowserApiError ? error.message : "删除失败，请稍后重试。"
      );
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <div className="metric-grid compact-grid">
        <article className="metric-card">
          <p className="metric-label">技术栈标签</p>
          <h2>{techCount}</h2>
          <p className="metric-detail">真实数量来自 `/tag/list`</p>
        </article>
        <article className="metric-card">
          <p className="metric-label">学习路径标签</p>
          <h2>{pathCount}</h2>
          <p className="metric-detail">支持按类型筛选和分页查询</p>
        </article>
        <article className="metric-card">
          <p className="metric-label">当前分页</p>
          <h2>
            {tagPage.pageNo}/{tagPage.totalPages}
          </h2>
          <p className="metric-detail">总计 {tagPage.total} 个标签</p>
        </article>
      </div>

      <section className="panel">
        <div className="panel-header">
          <div>
            <p className="section-kicker">Operations</p>
            <h3>标签筛选与 CRUD</h3>
          </div>
          <div className="inline-actions">
            <button className="panel-action-button" type="button" onClick={() => router.refresh()}>
              <RefreshCw className="h-4 w-4" />
              <span>刷新</span>
            </button>
            <button className="panel-action-button primary" type="button" onClick={openCreateDialog}>
              <Plus className="h-4 w-4" />
              <span>新增标签</span>
            </button>
          </div>
        </div>

        <div className="toolbar-grid">
          <label className="field">
            <span>标签类型</span>
            <select
              defaultValue={currentType ? String(currentType) : "all"}
              onChange={(event) => {
                const nextType =
                  event.target.value === "all" ? undefined : Number(event.target.value);
                navigate(1, nextType);
              }}
            >
              <option value="all">全部类型</option>
              <option value="1">技术栈</option>
              <option value="2">学习路径</option>
            </select>
          </label>

          <label className="field">
            <span>搜索预留</span>
            <input disabled placeholder="后续可接名称关键词查询" />
          </label>

          <label className="field">
            <span>提交反馈</span>
            <input disabled placeholder="操作成功后已在当前页即时同步" />
          </label>
        </div>

        {message ? <p className="form-message success">{message}</p> : null}
        {errorMessage ? <p className="form-message error">{errorMessage}</p> : null}

        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>名称</th>
                <th>类型</th>
                <th>颜色</th>
                <th>排序权重</th>
                <th>ID</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              {tagPage.list.length === 0 ? (
                <tr>
                  <td className="text-cb-text-secondary py-8 text-center" colSpan={6}>
                    当前筛选条件下没有标签记录。
                  </td>
                </tr>
              ) : (
                tagPage.list.map((tag) => (
                  <tr key={tag.id}>
                    <td>
                      <div className="title-cell">
                        <strong>{tag.name}</strong>
                        <span>颜色与排序已可直接编辑</span>
                      </div>
                    </td>
                    <td>
                      <span className="pill pill-neutral">{tagTypeMap[tag.type] || "未知类型"}</span>
                    </td>
                    <td>
                      <div className="color-chip">
                        <span className="tag-dot" style={{ backgroundColor: tag.color }} />
                        <span className="mono">{tag.color}</span>
                      </div>
                    </td>
                    <td className="mono">{tag.sort}</td>
                    <td className="mono">#{tag.id}</td>
                    <td>
                      <div className="inline-actions">
                        <button
                          className="table-action-button"
                          type="button"
                          onClick={() => openEditDialog(tag)}
                        >
                          <Pencil className="h-4 w-4" />
                          <span>编辑</span>
                        </button>
                        <button
                          className={cn("table-action-button danger", submitting && "opacity-60")}
                          disabled={submitting}
                          type="button"
                          onClick={() => handleDelete(tag)}
                        >
                          <Trash2 className="h-4 w-4" />
                          <span>删除</span>
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        <div className="pagination-bar">
          <p className="text-cb-text-secondary text-sm">
            第 <strong>{tagPage.pageNo}</strong> / <strong>{tagPage.totalPages}</strong> 页，共{" "}
            <strong>{tagPage.total}</strong> 条
          </p>
          <div className="pagination-actions">
            <button
              className={cn("pagination-button", tagPage.pageNo <= 1 && "is-disabled")}
              disabled={tagPage.pageNo <= 1}
              type="button"
              onClick={() => navigate(tagPage.pageNo - 1)}
            >
              上一页
            </button>
            <button
              className={cn(
                "pagination-button",
                tagPage.pageNo >= tagPage.totalPages && "is-disabled"
              )}
              disabled={tagPage.pageNo >= tagPage.totalPages}
              type="button"
              onClick={() => navigate(tagPage.pageNo + 1)}
            >
              下一页
            </button>
          </div>
        </div>
      </section>

      <section className="panel">
        <div className="panel-header">
          <div>
            <p className="section-kicker">API Mapping</p>
            <h3>标签后台当前能力边界</h3>
          </div>
        </div>
        <div className="stack-list">
          <article className="stack-item">
            <div className="stack-title-row">
              <h4>已接通</h4>
              <span className="pill pill-safe">查询 + CRUD</span>
            </div>
            <p>分页查询、全量列表、新增、编辑、删除均已接入真实接口。</p>
          </article>
          <article className="stack-item">
            <div className="stack-title-row">
              <h4>仍需后端补齐</h4>
              <span className="pill pill-warn">权限与日志</span>
            </div>
            <p>管理员权限收口、操作日志和标签关联文章数仍需后端补充。</p>
          </article>
        </div>
      </section>

      {dialogOpen ? (
        <div className="dialog-backdrop" role="presentation" onClick={() => setDialogOpen(false)}>
          <section
            aria-modal="true"
            className="dialog-panel"
            role="dialog"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="panel-header">
              <div>
                <p className="section-kicker">
                  {dialogMode === "create" ? "Create Tag" : "Edit Tag"}
                </p>
                <h3>{dialogMode === "create" ? "新增标签" : "编辑标签"}</h3>
              </div>
              <button className="header-icon-button" type="button" onClick={() => setDialogOpen(false)}>
                <X className="h-4 w-4" />
              </button>
            </div>

            <form className="dialog-form" onSubmit={handleSubmit}>
              <label className="field">
                <span>标签名称</span>
                <input
                  value={formValues.name}
                  onChange={(event) =>
                    setFormValues((current) => ({ ...current, name: event.target.value }))
                  }
                />
              </label>

              <label className="field">
                <span>标签类型</span>
                <select
                  value={String(formValues.type)}
                  onChange={(event) =>
                    setFormValues((current) => ({
                      ...current,
                      type: Number(event.target.value) === 2 ? 2 : 1,
                    }))
                  }
                >
                  <option value="1">技术栈</option>
                  <option value="2">学习路径</option>
                </select>
              </label>

              <div className="dialog-grid">
                <label className="field">
                  <span>颜色</span>
                  <input
                    value={formValues.color}
                    onChange={(event) =>
                      setFormValues((current) => ({ ...current, color: event.target.value }))
                    }
                  />
                </label>

                <label className="field">
                  <span>排序权重</span>
                  <input
                    min={0}
                    type="number"
                    value={formValues.sort}
                    onChange={(event) =>
                      setFormValues((current) => ({
                        ...current,
                        sort: Number(event.target.value) || 0,
                      }))
                    }
                  />
                </label>
              </div>

              {errorMessage ? <p className="form-message error">{errorMessage}</p> : null}

              <div className="dialog-actions">
                <button className="panel-action-button" type="button" onClick={() => setDialogOpen(false)}>
                  取消
                </button>
                <button className="panel-action-button primary" disabled={submitting} type="submit">
                  {submitting ? "提交中..." : dialogMode === "create" ? "创建标签" : "保存修改"}
                </button>
              </div>
            </form>
          </section>
        </div>
      ) : null}
    </>
  );
}

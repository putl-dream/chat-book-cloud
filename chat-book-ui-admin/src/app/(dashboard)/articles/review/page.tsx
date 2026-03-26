import Link from "next/link";
import { PaginationControls } from "@/components/shared/pagination-controls";
import { RequestStatePanel } from "@/components/shared/request-state-panel";
import { AdminApiError, getReviewArticlesPage } from "@/lib/admin-api";
import { articleCategoryMap } from "@/lib/admin-config";

type ReviewPageProps = {
  searchParams?: Promise<{
    page?: string;
    size?: string;
    focus?: string;
  }>;
};

function parsePositiveInt(value: string | undefined, fallback: number) {
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback;
}

export default async function ArticleReviewPage({ searchParams }: ReviewPageProps) {
  const resolvedSearchParams = (await searchParams) ?? {};
  const page = parsePositiveInt(resolvedSearchParams.page, 1);
  const size = parsePositiveInt(resolvedSearchParams.size, 8);
  const focusedId = parsePositiveInt(resolvedSearchParams.focus, 0);

  try {
    const reviewPage = await getReviewArticlesPage({ page, size });
    const focusedArticle = reviewPage.list.find((article) => article.id === focusedId);

    return (
      <section className="page-shell">
        <div className="page-hero compact">
          <p className="eyebrow">Editorial Review</p>
          <h1>文章审核工作台</h1>
          <p className="hero-copy">
            当前项目已经存在管理员待审核文章列表能力，因此后台应优先把审核工作流沉淀下来。
            下一步重点是补齐通过、驳回、批量处理、违规标记和审核记录追踪。
          </p>
        </div>

        <div className="metric-grid compact-grid">
          <article className="metric-card">
            <p className="metric-label">待审核文章</p>
            <h2>{reviewPage.total}</h2>
            <p className="metric-detail">真实数据来自 `/page/adminArticlePage`</p>
          </article>
          <article className="metric-card">
            <p className="metric-label">当前页作者</p>
            <h2>{new Set(reviewPage.list.map((article) => article.userId)).size}</h2>
            <p className="metric-detail">可联动用户治理与风险巡检</p>
          </article>
          <article className="metric-card">
            <p className="metric-label">审核动作接口</p>
            <h2>0</h2>
            <p className="metric-detail">通过 / 驳回 / 批量处理仍需后端新增接口</p>
          </article>
        </div>

        {focusedArticle ? (
          <section className="panel">
            <div className="panel-header">
              <div>
                <p className="section-kicker">Detail Placeholder</p>
                <h3>文章详情入口已预留</h3>
              </div>
              <span className="pill pill-neutral">Article #{focusedArticle.id}</span>
            </div>
            <div className="stack-list">
              <article className="stack-item">
                <div className="stack-title-row">
                  <h4>{focusedArticle.title}</h4>
                  <span className="pill pill-warn">待补详情接口</span>
                </div>
                <p>
                  当前列表已接通真实待审核队列，但后端尚未返回标签、内容类型和详情正文。后续可对接
                  `/article/query?id=*` 或新增管理员详情接口。
                </p>
              </article>
            </div>
          </section>
        ) : null}

        <div className="content-grid two-column">
          <section className="panel">
            <div className="panel-header">
              <div>
                <p className="section-kicker">Pending Queue</p>
                <h3>待审核内容队列</h3>
              </div>
            </div>

            {reviewPage.list.length === 0 ? (
              <RequestStatePanel
                title="当前没有待审核文章"
                description="接口已接通，但当前队列为空。后续审核动作接口补齐后，这里会成为真正的审核工作台。"
              />
            ) : (
              <>
                <div className="stack-list">
                  {reviewPage.list.map((article) => (
                    <article className="review-card" id={`review-${article.id}`} key={article.id}>
                      <div className="review-card-top">
                        <div>
                          <h4>{article.title}</h4>
                          <p className="meta-line">
                            作者 {article.userName} / 分类 {articleCategoryMap[article.category] || "未分类"} /
                            互动 V {article.viewCount} · C {article.commentCount} · P{" "}
                            {article.praiseCount}
                          </p>
                        </div>
                        <span className="pill pill-warn">待审核</span>
                      </div>
                      <p className="review-summary">{article.summary}</p>
                      <div className="chip-row">
                        <span className="chip">标签字段待接口补齐</span>
                        <span className="chip">内容类型待接口补齐</span>
                      </div>
                      <div className="review-footer">
                        <span className="mono">Article #{article.id}</span>
                        <span className="mono">{article.createdAt}</span>
                        <Link
                          className="inline-link"
                          href={`/articles/review?page=${reviewPage.pageNo}&size=${reviewPage.pageSize}&focus=${article.id}#review-${article.id}`}
                          prefetch={false}
                        >
                          查看详情占位
                        </Link>
                      </div>
                    </article>
                  ))}
                </div>

                <PaginationControls
                  page={reviewPage.pageNo}
                  pageSize={reviewPage.pageSize}
                  pathname="/articles/review"
                  query={focusedId ? { focus: focusedId } : undefined}
                  total={reviewPage.total}
                  totalPages={reviewPage.totalPages}
                />
              </>
            )}
          </section>

          <section className="panel">
            <div className="panel-header">
              <div>
                <p className="section-kicker">Review Policy</p>
                <h3>后台需要承接的审核动作</h3>
              </div>
            </div>
            <div className="stack-list">
              <article className="stack-item">
                <div className="stack-title-row">
                  <h4>内容预审</h4>
                  <span className="pill pill-safe">已接通真实列表</span>
                </div>
                <p>当前已展示真实待审核文章标题、作者、分类、时间和互动统计。</p>
              </article>
              <article className="stack-item">
                <div className="stack-title-row">
                  <h4>审核结论提交</h4>
                  <span className="pill pill-danger">缺失</span>
                </div>
                <p>需要新增管理员通过、驳回、补充驳回原因和批量处理接口。</p>
              </article>
              <article className="stack-item">
                <div className="stack-title-row">
                  <h4>详情字段补齐</h4>
                  <span className="pill pill-warn">部分缺失</span>
                </div>
                <p>当前接口未返回标签、内容类型和正文详情，页面已预留详情入口占位。</p>
              </article>
            </div>
          </section>
        </div>
      </section>
    );
  } catch (error) {
    const description =
      error instanceof AdminApiError
        ? error.message
        : "审核列表读取失败，请确认网关地址和文章后台接口是否可访问。";

    return (
      <section className="page-shell">
        <RequestStatePanel title="文章审核页暂时不可用" description={description} tone="warning" />
      </section>
    );
  }
}

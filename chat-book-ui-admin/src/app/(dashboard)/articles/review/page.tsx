import { articleCategoryMap, contentTypeMap } from "@/lib/admin-config";
import { getReviewArticles } from "@/lib/admin-api";

export default async function ArticleReviewPage() {
  const articles = await getReviewArticles();

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
          <h2>{articles.length}</h2>
          <p className="metric-detail">对接 `/page/adminArticlePage`</p>
        </article>
        <article className="metric-card">
          <p className="metric-label">涉及作者</p>
          <h2>{new Set(articles.map((article) => article.userId)).size}</h2>
          <p className="metric-detail">可联动用户治理与风险巡检</p>
        </article>
        <article className="metric-card">
          <p className="metric-label">审核动作接口</p>
          <h2>0</h2>
          <p className="metric-detail">当前需要后端新增审核通过/驳回 API</p>
        </article>
      </div>

      <div className="content-grid two-column">
        <section className="panel">
          <div className="panel-header">
            <div>
              <p className="section-kicker">Pending Queue</p>
              <h3>待审核内容队列</h3>
            </div>
          </div>
          <div className="stack-list">
            {articles.map((article) => (
              <article className="review-card" key={article.id}>
                <div className="review-card-top">
                  <div>
                    <h4>{article.title}</h4>
                    <p className="meta-line">
                      作者 {article.userName} / 分类 {articleCategoryMap[article.category]} / 类型{" "}
                      {contentTypeMap[article.contentType]}
                    </p>
                  </div>
                  <span className="pill pill-warn">待审核</span>
                </div>
                <p className="review-summary">{article.summary}</p>
                <div className="chip-row">
                  {article.tags.map((tag) => (
                    <span className="chip" key={tag}>
                      {tag}
                    </span>
                  ))}
                </div>
                <div className="review-footer">
                  <span className="mono">Article #{article.id}</span>
                  <span className="mono">{article.createdAt}</span>
                </div>
              </article>
            ))}
          </div>
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
                <span className="pill pill-safe">可直接接入</span>
              </div>
              <p>列表、文章详情、标签和作者信息已能从现有服务拼装出来。</p>
            </article>
            <article className="stack-item">
              <div className="stack-title-row">
                <h4>审核结论提交</h4>
                <span className="pill pill-danger">缺失</span>
              </div>
              <p>需要新增管理员通过、驳回、补充驳回原因、批量处理等接口。</p>
            </article>
            <article className="stack-item">
              <div className="stack-title-row">
                <h4>违规追踪</h4>
                <span className="pill pill-danger">缺失</span>
              </div>
              <p>需要审核日志、风险标签、重复违规作者画像和通知联动。</p>
            </article>
          </div>
        </section>
      </div>
    </section>
  );
}

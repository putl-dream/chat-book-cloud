import { articleCategoryMap, articleStatusMap, contentTypeMap } from "@/lib/admin-config";
import { getContentArticles } from "@/lib/admin-api";

export default async function ContentPage() {
  const articles = await getContentArticles();
  const publishedCount = articles.filter((article) => article.status === 2).length;
  const draftCount = articles.filter((article) => article.status === 0).length;

  return (
    <section className="page-shell">
      <div className="page-hero compact">
        <p className="eyebrow">Content Operations</p>
        <h1>内容管理与存量运营</h1>
        <p className="hero-copy">
          审核通过后的文章还需要长期治理。后台内容管理模块负责全站内容检索、上下架、人工巡检、数据追踪和专题运营入口。
        </p>
      </div>

      <div className="metric-grid compact-grid">
        <article className="metric-card">
          <p className="metric-label">已发布</p>
          <h2>{publishedCount}</h2>
          <p className="metric-detail">用于首页推荐、热度监控和专题运营</p>
        </article>
        <article className="metric-card">
          <p className="metric-label">草稿</p>
          <h2>{draftCount}</h2>
          <p className="metric-detail">可作为创作者召回和创作漏斗分析依据</p>
        </article>
        <article className="metric-card">
          <p className="metric-label">内容检索 API</p>
          <h2>待补</h2>
          <p className="metric-detail">管理员视角还缺少全站分页和筛选接口</p>
        </article>
      </div>

      <section className="panel">
        <div className="panel-header">
          <div>
            <p className="section-kicker">Content Table</p>
            <h3>内容资产视图</h3>
          </div>
        </div>
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>标题</th>
                <th>作者</th>
                <th>分类 / 类型</th>
                <th>状态</th>
                <th>互动数据</th>
                <th>时间</th>
              </tr>
            </thead>
            <tbody>
              {articles.map((article) => (
                <tr key={article.id}>
                  <td>
                    <div className="title-cell">
                      <strong>{article.title}</strong>
                      <span>{article.summary}</span>
                    </div>
                  </td>
                  <td>
                    <div className="user-cell">
                      <strong>{article.userName}</strong>
                      <span className="mono">UID {article.userId}</span>
                    </div>
                  </td>
                  <td>
                    {articleCategoryMap[article.category]} / {contentTypeMap[article.contentType]}
                  </td>
                  <td>
                    <span
                      className={
                        article.status === 2
                          ? "pill pill-safe"
                          : article.status === 1
                            ? "pill pill-warn"
                            : article.status === 0
                              ? "pill pill-neutral"
                              : "pill pill-danger"
                      }
                    >
                      {articleStatusMap[article.status]}
                    </span>
                  </td>
                  <td className="mono">
                    V {article.viewCount} / C {article.commentCount} / P {article.praiseCount}
                  </td>
                  <td className="mono">{article.createdAt}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </section>
  );
}

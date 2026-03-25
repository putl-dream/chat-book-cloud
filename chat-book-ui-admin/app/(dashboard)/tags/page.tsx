import { tagTypeMap } from "@/lib/admin-config";
import { getTags } from "@/lib/admin-api";

export default async function TagsPage() {
  const tags = await getTags();
  const techCount = tags.filter((tag) => tag.type === 1).length;
  const pathCount = tags.filter((tag) => tag.type === 2).length;

  return (
    <section className="page-shell">
      <div className="page-hero compact">
        <p className="eyebrow">Tag Taxonomy</p>
        <h1>标签体系与内容组织</h1>
        <p className="hero-copy">
          当前项目的标签分为技术栈和学习路径两类，后台应负责标签命名规范、颜色统一、排序权重和与内容分发策略的联动。
        </p>
      </div>

      <div className="metric-grid compact-grid">
        <article className="metric-card">
          <p className="metric-label">技术栈标签</p>
          <h2>{techCount}</h2>
          <p className="metric-detail">用于文章横向聚类和专题入口</p>
        </article>
        <article className="metric-card">
          <p className="metric-label">学习路径标签</p>
          <h2>{pathCount}</h2>
          <p className="metric-detail">用于知识路径与阶段性推荐</p>
        </article>
        <article className="metric-card">
          <p className="metric-label">CRUD 能力</p>
          <h2>已具备</h2>
          <p className="metric-detail">现有 `/tag/*` 接口可以直接接入后台</p>
        </article>
      </div>

      <div className="card-grid">
        {tags.map((tag) => (
          <article className="tag-card" key={tag.id}>
            <div className="tag-card-top">
              <div className="tag-dot" style={{ backgroundColor: tag.color }} />
              <span className="pill pill-neutral">{tagTypeMap[tag.type]}</span>
            </div>
            <h3>{tag.name}</h3>
            <p>排序权重 {tag.sort}</p>
            <div className="tag-meta">
              <span className="mono">ID {tag.id}</span>
              <strong>{tag.relatedArticles} 篇文章</strong>
            </div>
          </article>
        ))}
      </div>

      <section className="panel">
        <div className="panel-header">
          <div>
            <p className="section-kicker">API Mapping</p>
            <h3>标签后台可直接接入的现有接口</h3>
          </div>
        </div>
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>能力</th>
                <th>接口</th>
                <th>说明</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>分页查询</td>
                <td className="mono">POST /tag/page</td>
                <td>支持标签类型筛选，可作为后台列表页数据源。</td>
              </tr>
              <tr>
                <td>全量列表</td>
                <td className="mono">GET /tag/list</td>
                <td>适合下拉选项、批量操作前预加载。</td>
              </tr>
              <tr>
                <td>新增 / 更新 / 删除</td>
                <td className="mono">POST /tag/create, /tag/update, DELETE /tag/delete</td>
                <td>后台表单可直接对接，建议补充操作日志。</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </section>
  );
}

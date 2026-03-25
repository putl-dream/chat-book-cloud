import { adminNavigation, getPriorityTone, getStatusTone } from "@/lib/admin-config";
import { getDashboardSnapshot } from "@/lib/admin-api";

export default async function DashboardPage() {
  const snapshot = await getDashboardSnapshot();

  return (
    <section className="page-shell">
      <div className="page-hero">
        <p className="eyebrow">Platform Overview</p>
        <h1>围绕当前业务能力搭建后台主控台</h1>
        <p className="hero-copy">
          当前项目已覆盖博客内容、创作台、评论互动、社交关系、聊天和统一鉴权。后台的首要任务不是重复前台功能，
          而是为这些能力补上运营、审核、治理和系统接入的控制面。
        </p>
      </div>

      <div className="metric-grid">
        {snapshot.metrics.map((metric) => (
          <article className="metric-card" key={metric.label}>
            <p className="metric-label">{metric.label}</p>
            <h2>{metric.value}</h2>
            <p className="metric-detail">{metric.detail}</p>
            <span className="metric-trend">{metric.trend}</span>
          </article>
        ))}
      </div>

      <div className="content-grid two-column">
        <section className="panel">
          <div className="panel-header">
            <div>
              <p className="section-kicker">运营视角</p>
              <h3>当前后台建设判断</h3>
            </div>
          </div>
          <div className="stack-list">
            {snapshot.highlights.map((item) => (
              <article className="stack-item" key={item.title}>
                <div className="stack-title-row">
                  <h4>{item.title}</h4>
                  <span className={`pill pill-${getStatusTone(item.status)}`}>
                    {item.status === "stable"
                      ? "已具备"
                      : item.status === "partial"
                        ? "部分具备"
                        : "待补齐"}
                  </span>
                </div>
                <p>{item.description}</p>
              </article>
            ))}
          </div>
        </section>

        <section className="panel">
          <div className="panel-header">
            <div>
              <p className="section-kicker">模块地图</p>
              <h3>本次已落地的后台栏目</h3>
            </div>
          </div>
          <div className="stack-list">
            {adminNavigation.flatMap((group) => group.items).map((item) => (
              <article className="stack-item" key={item.href}>
                <div className="stack-title-row">
                  <h4>{item.label}</h4>
                  <span className="pill pill-neutral">{item.href}</span>
                </div>
                <p>{item.description}</p>
              </article>
            ))}
          </div>
        </section>
      </div>

      <section className="panel">
        <div className="panel-header">
          <div>
            <p className="section-kicker">Service Matrix</p>
            <h3>微服务接入状态与后台职责</h3>
          </div>
        </div>
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>服务</th>
                <th>后台负责什么</th>
                <th>当前接口</th>
                <th>缺口</th>
                <th>优先级</th>
              </tr>
            </thead>
            <tbody>
              {snapshot.services.map((service) => (
                <tr key={service.service}>
                  <td className="mono">{service.service}</td>
                  <td>{service.responsibility}</td>
                  <td className="mono">{service.currentApi}</td>
                  <td>{service.backendGap}</td>
                  <td>
                    <span className={`pill pill-${getPriorityTone(service.priority)}`}>
                      {service.priority === "high"
                        ? "高"
                        : service.priority === "medium"
                          ? "中"
                          : "低"}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </section>
  );
}

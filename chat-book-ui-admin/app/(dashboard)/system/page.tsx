import { getPriorityTone } from "@/lib/admin-config";
import { getDashboardSnapshot } from "@/lib/admin-api";

const launchChecklist = [
  "接入管理员身份认证与路由守卫。",
  "把用户、标签、文章审核列表切到真实接口。",
  "补齐审核动作、评论治理、系统日志接口。",
  "增加操作日志和审计追踪。",
  "落地搜索、筛选、分页和空状态规范。"
];

export default async function SystemPage() {
  const snapshot = await getDashboardSnapshot();

  return (
    <section className="page-shell">
      <div className="page-hero compact">
        <p className="eyebrow">System Readiness</p>
        <h1>后台接入策略与改造清单</h1>
        <p className="hero-copy">
          这套后台框架已经把管理端路由、布局、数据适配层和页面职责拆开。后续接入时，优先替换 `lib/admin-api.ts`
          即可逐步把 mock 数据切成真实接口。
        </p>
      </div>

      <div className="content-grid two-column">
        <section className="panel">
          <div className="panel-header">
            <div>
              <p className="section-kicker">Launch Checklist</p>
              <h3>正式接入前必须完成</h3>
            </div>
          </div>
          <div className="stack-list">
            {launchChecklist.map((item) => (
              <article className="stack-item" key={item}>
                <div className="stack-title-row">
                  <h4>{item}</h4>
                  <span className="pill pill-neutral">Todo</span>
                </div>
              </article>
            ))}
          </div>
        </section>

        <section className="panel">
          <div className="panel-header">
            <div>
              <p className="section-kicker">Architecture</p>
              <h3>当前框架分层</h3>
            </div>
          </div>
          <div className="stack-list">
            <article className="stack-item">
              <div className="stack-title-row">
                <h4>App Router 页面层</h4>
                <span className="pill pill-safe">已完成</span>
              </div>
              <p>按后台功能拆分为概览、用户、审核、内容、标签、互动、系统接入七个页面。</p>
            </article>
            <article className="stack-item">
              <div className="stack-title-row">
                <h4>管理端布局层</h4>
                <span className="pill pill-safe">已完成</span>
              </div>
              <p>侧边导航、顶部信息和响应式布局已经成型，可继续承载权限控制。</p>
            </article>
            <article className="stack-item">
              <div className="stack-title-row">
                <h4>数据适配层</h4>
                <span className="pill pill-safe">已完成</span>
              </div>
              <p>真实接口接入只需替换 `admin-api.ts`，页面不必重写。</p>
            </article>
          </div>
        </section>
      </div>

      <section className="panel">
        <div className="panel-header">
          <div>
            <p className="section-kicker">Service Priority</p>
            <h3>推荐的后端补口顺序</h3>
          </div>
        </div>
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>服务</th>
                <th>应先补的后台接口</th>
                <th>优先级</th>
              </tr>
            </thead>
            <tbody>
              {snapshot.services.map((service) => (
                <tr key={service.service}>
                  <td className="mono">{service.service}</td>
                  <td>{service.backendGap}</td>
                  <td>
                    <span className={`pill pill-${getPriorityTone(service.priority)}`}>
                      {service.priority}
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

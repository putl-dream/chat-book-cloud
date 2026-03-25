import { actionTypeMap } from "@/lib/admin-config";
import { getInteractionEvents } from "@/lib/admin-api";

export default async function InteractionsPage() {
  const events = await getInteractionEvents();
  const commentCount = events.filter((event) => event.actionType === "COMMENT").length;

  return (
    <section className="page-shell">
      <div className="page-hero compact">
        <p className="eyebrow">Interaction Governance</p>
        <h1>评论、通知与互动巡检</h1>
        <p className="hero-copy">
          interaction-service 已承载点赞、收藏、评论、浏览和通知，但后台还缺少全局视角。
          这一页先完成监控入口骨架，便于后续扩展评论治理、异常行为识别和告警归因。
        </p>
      </div>

      <div className="metric-grid compact-grid">
        <article className="metric-card">
          <p className="metric-label">互动事件样本</p>
          <h2>{events.length}</h2>
          <p className="metric-detail">当前以 mock 数据承载后台聚合入口</p>
        </article>
        <article className="metric-card">
          <p className="metric-label">评论治理</p>
          <h2>{commentCount}</h2>
          <p className="metric-detail">后续需要管理员评论分页和处置接口</p>
        </article>
        <article className="metric-card">
          <p className="metric-label">全局通知</p>
          <h2>待补</h2>
          <p className="metric-detail">当前 `/interaction/foot/getNotifications` 只面向登录用户</p>
        </article>
      </div>

      <div className="content-grid two-column">
        <section className="panel">
          <div className="panel-header">
            <div>
              <p className="section-kicker">Event Feed</p>
              <h3>互动事件流</h3>
            </div>
          </div>
          <div className="timeline">
            {events.map((event) => (
              <article className="timeline-item" key={event.id}>
                <div className="timeline-dot" />
                <div className="timeline-body">
                  <div className="stack-title-row">
                    <h4>
                      {event.senderName} {actionTypeMap[event.actionType]}了《{event.articleTitle}》
                    </h4>
                    <span className="pill pill-neutral">{event.scope}</span>
                  </div>
                  <p>{event.summary}</p>
                  <span className="mono">{event.createdAt}</span>
                </div>
              </article>
            ))}
          </div>
        </section>

        <section className="panel">
          <div className="panel-header">
            <div>
              <p className="section-kicker">Missing Admin APIs</p>
              <h3>后台互动治理待补接口</h3>
            </div>
          </div>
          <div className="stack-list">
            <article className="stack-item">
              <div className="stack-title-row">
                <h4>评论分页与处置</h4>
                <span className="pill pill-danger">高优先级</span>
              </div>
              <p>需要支持全站评论分页、删除、屏蔽、恢复、敏感词命中记录。</p>
            </article>
            <article className="stack-item">
              <div className="stack-title-row">
                <h4>异常行为聚合</h4>
                <span className="pill pill-danger">高优先级</span>
              </div>
              <p>需要识别刷赞、刷浏览、短时批量收藏等异常模式。</p>
            </article>
            <article className="stack-item">
              <div className="stack-title-row">
                <h4>运营告警中心</h4>
                <span className="pill pill-warn">中优先级</span>
              </div>
              <p>建议把互动、文章审核、用户风险事件聚合到统一告警中心。</p>
            </article>
          </div>
        </section>
      </div>
    </section>
  );
}

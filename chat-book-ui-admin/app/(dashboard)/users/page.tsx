import { getUsers } from "@/lib/admin-api";

export default async function UsersPage() {
  const users = await getUsers();
  const adminCount = users.filter((user) => user.role === "admin").length;
  const draftCount = users.filter((user) => user.status === "draft").length;

  return (
    <section className="page-shell">
      <div className="page-hero compact">
        <p className="eyebrow">User Management</p>
        <h1>用户与账号治理</h1>
        <p className="hero-copy">
          当前后端已提供管理员分页查询用户能力，因此用户模块可以优先进入真实接入阶段。
          后续应继续补足角色调整、禁用账号、批量导出和登录审计等后台动作。
        </p>
      </div>

      <div className="metric-grid compact-grid">
        <article className="metric-card">
          <p className="metric-label">用户总数</p>
          <h2>{users.length}</h2>
          <p className="metric-detail">对接 `/user/admin/user` 分页查询</p>
        </article>
        <article className="metric-card">
          <p className="metric-label">管理员</p>
          <h2>{adminCount}</h2>
          <p className="metric-detail">角色来源于 user-service 的 `role` 字段</p>
        </article>
        <article className="metric-card">
          <p className="metric-label">待完善资料</p>
          <h2>{draftCount}</h2>
          <p className="metric-detail">可作为运营触达、资料补全提醒依据</p>
        </article>
      </div>

      <div className="content-grid two-column">
        <section className="panel">
          <div className="panel-header">
            <div>
              <p className="section-kicker">User Table</p>
              <h3>当前用户数据视图</h3>
            </div>
          </div>
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>用户</th>
                  <th>邮箱</th>
                  <th>角色</th>
                  <th>状态</th>
                  <th>简介</th>
                  <th>创建时间</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr key={user.userId}>
                    <td>
                      <div className="user-cell">
                        <strong>{user.username}</strong>
                        <span className="mono">UID {user.userId}</span>
                      </div>
                    </td>
                    <td>{user.email}</td>
                    <td>
                      <span className={user.role === "admin" ? "pill pill-safe" : "pill pill-neutral"}>
                        {user.role}
                      </span>
                    </td>
                    <td>
                      <span
                        className={
                          user.status === "active"
                            ? "pill pill-safe"
                            : user.status === "draft"
                              ? "pill pill-warn"
                              : "pill pill-danger"
                        }
                      >
                        {user.status}
                      </span>
                    </td>
                    <td>{user.profile}</td>
                    <td className="mono">{user.createdAt}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>

        <section className="panel">
          <div className="panel-header">
            <div>
              <p className="section-kicker">Backlog</p>
              <h3>用户后台还要负责的动作</h3>
            </div>
          </div>
          <div className="stack-list">
            <article className="stack-item">
              <div className="stack-title-row">
                <h4>角色管理</h4>
                <span className="pill pill-danger">待补接口</span>
              </div>
              <p>当前只能查询角色，缺少管理员升降权、重置账号和操作审计。</p>
            </article>
            <article className="stack-item">
              <div className="stack-title-row">
                <h4>账号状态控制</h4>
                <span className="pill pill-danger">待补接口</span>
              </div>
              <p>需要支持禁用、冻结、恢复和异常登录封控。</p>
            </article>
            <article className="stack-item">
              <div className="stack-title-row">
                <h4>运营工具</h4>
                <span className="pill pill-warn">待扩展</span>
              </div>
              <p>可补充用户画像筛选、批量导出和资料完善提醒。</p>
            </article>
          </div>
        </section>
      </div>
    </section>
  );
}

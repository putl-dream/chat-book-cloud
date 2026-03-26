import { PaginationControls } from "@/components/shared/pagination-controls";
import { RequestStatePanel } from "@/components/shared/request-state-panel";
import { AdminApiError, getUsersPage } from "@/lib/admin-api";
import { getRoleLabel, getRoleTone } from "@/lib/admin-config";

type UsersPageProps = {
  searchParams?: Promise<{
    page?: string;
    size?: string;
  }>;
};

function parsePositiveInt(value: string | undefined, fallback: number) {
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback;
}

export default async function UsersPage({ searchParams }: UsersPageProps) {
  const resolvedSearchParams = (await searchParams) ?? {};
  const page = parsePositiveInt(resolvedSearchParams.page, 1);
  const size = parsePositiveInt(resolvedSearchParams.size, 20);

  try {
    const userPage = await getUsersPage({ page, size });
    const adminCount = userPage.list.filter((user) => user.role === "admin").length;

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
            <h2>{userPage.total}</h2>
            <p className="metric-detail">真实数据来自 `/user/admin/user?page&size`</p>
          </article>
          <article className="metric-card">
            <p className="metric-label">当前页管理员</p>
            <h2>{adminCount}</h2>
            <p className="metric-detail">角色映射来源于 user-service 的 `role` 字段</p>
          </article>
          <article className="metric-card">
            <p className="metric-label">分页状态</p>
            <h2>
              {userPage.pageNo}/{userPage.totalPages}
            </h2>
            <p className="metric-detail">单页 {userPage.pageSize} 条，支持后台分页切换</p>
          </article>
        </div>

        <section className="panel">
          <div className="panel-header">
            <div>
              <p className="section-kicker">Filters</p>
              <h3>搜索与筛选结构预留</h3>
            </div>
            <span className="pill pill-neutral">接口未开放搜索参数</span>
          </div>
          <div className="toolbar-grid">
            <label className="field">
              <span>用户名 / 邮箱</span>
              <input disabled placeholder="后续对接用户名、邮箱关键词搜索" />
            </label>
            <label className="field">
              <span>角色筛选</span>
              <select disabled defaultValue="all">
                <option value="all">全部角色</option>
                <option value="admin">管理员</option>
                <option value="user">普通用户</option>
              </select>
            </label>
            <label className="field">
              <span>账号状态</span>
              <input disabled placeholder="状态字段当前接口未返回" />
            </label>
          </div>
        </section>

        <section className="panel">
          <div className="panel-header">
            <div>
              <p className="section-kicker">User Table</p>
              <h3>当前用户数据视图</h3>
            </div>
          </div>

          {userPage.list.length === 0 ? (
            <RequestStatePanel
              title="当前没有用户数据"
              description="接口已接通，但本页暂无返回记录。后续可以继续补充搜索、筛选和批量操作。"
            />
          ) : (
            <>
              <div className="table-wrap">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>用户</th>
                      <th>邮箱</th>
                      <th>角色</th>
                      <th>状态</th>
                      <th>简介</th>
                      <th>用户标识</th>
                    </tr>
                  </thead>
                  <tbody>
                    {userPage.list.map((user) => (
                      <tr key={user.userId}>
                        <td>
                          <div className="user-cell">
                            <strong>{user.username}</strong>
                            <span>{user.photo ? "已配置头像" : "未配置头像"}</span>
                          </div>
                        </td>
                        <td>{user.email}</td>
                        <td>
                          <span className={`pill pill-${getRoleTone(user.role)}`}>
                            {getRoleLabel(user.role)}
                          </span>
                        </td>
                        <td>
                          <span className="pill pill-neutral">接口未返回</span>
                        </td>
                        <td>{user.profile || "暂无简介"}</td>
                        <td className="mono">UID {user.userId}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <PaginationControls
                page={userPage.pageNo}
                pageSize={userPage.pageSize}
                pathname="/users"
                total={userPage.total}
                totalPages={userPage.totalPages}
              />
            </>
          )}
        </section>

        <section className="panel">
          <div className="panel-header">
            <div>
              <p className="section-kicker">Backlog</p>
              <h3>用户后台仍需后端补齐的动作</h3>
            </div>
          </div>
          <div className="stack-list">
            <article className="stack-item">
              <div className="stack-title-row">
                <h4>角色管理</h4>
                <span className="pill pill-danger">待补接口</span>
              </div>
              <p>当前只能读取角色，缺少管理员升降权、重置账号和操作审计。</p>
            </article>
            <article className="stack-item">
              <div className="stack-title-row">
                <h4>账号状态控制</h4>
                <span className="pill pill-danger">待补接口</span>
              </div>
              <p>当前接口未返回状态字段，后续需要支持禁用、冻结、恢复和异常登录封控。</p>
            </article>
            <article className="stack-item">
              <div className="stack-title-row">
                <h4>运营工具</h4>
                <span className="pill pill-warn">待扩展</span>
              </div>
              <p>可继续补充画像筛选、批量导出和资料完善提醒。</p>
            </article>
          </div>
        </section>
      </section>
    );
  } catch (error) {
    const description =
      error instanceof AdminApiError
        ? error.message
        : "用户列表读取失败，请确认网关地址和管理员接口是否可访问。";

    return (
      <section className="page-shell">
        <RequestStatePanel title="用户管理页暂时不可用" description={description} tone="warning" />
      </section>
    );
  }
}

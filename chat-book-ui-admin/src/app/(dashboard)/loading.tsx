export default function DashboardLoading() {
  return (
    <section className="page-shell">
      <div className="page-hero compact skeleton-block h-48" />
      <div className="metric-grid compact-grid">
        {Array.from({ length: 3 }).map((_, index) => (
          <article className="metric-card skeleton-block h-32" key={index} />
        ))}
      </div>
      <section className="panel skeleton-block h-80" />
    </section>
  );
}

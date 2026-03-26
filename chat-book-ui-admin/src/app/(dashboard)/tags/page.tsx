import { TagManager } from "@/components/tags/tag-manager";
import { RequestStatePanel } from "@/components/shared/request-state-panel";
import { AdminApiError, getTagList, getTagsPage } from "@/lib/admin-api";

type TagsPageProps = {
  searchParams?: Promise<{
    page?: string;
    size?: string;
    type?: string;
  }>;
};

function parsePositiveInt(value: string | undefined, fallback: number) {
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback;
}

export default async function TagsPage({ searchParams }: TagsPageProps) {
  const resolvedSearchParams = (await searchParams) ?? {};
  const page = parsePositiveInt(resolvedSearchParams.page, 1);
  const size = parsePositiveInt(resolvedSearchParams.size, 12);
  const type = resolvedSearchParams.type ? parsePositiveInt(resolvedSearchParams.type, 0) : 0;
  const currentType = type === 1 || type === 2 ? type : undefined;

  try {
    const [tagPage, allTags] = await Promise.all([
      getTagsPage({ page, size, type: currentType }),
      getTagList(),
    ]);

    return (
      <section className="page-shell">
        <div className="page-hero compact">
          <p className="eyebrow">Tag Taxonomy</p>
          <h1>标签体系与内容组织</h1>
          <p className="hero-copy">
            当前项目的标签分为技术栈和学习路径两类，后台应负责标签命名规范、颜色统一、排序权重和与内容分发策略的联动。
          </p>
        </div>

        <TagManager allTags={allTags} currentType={currentType} initialPage={tagPage} />
      </section>
    );
  } catch (error) {
    const description =
      error instanceof AdminApiError
        ? error.message
        : "标签数据读取失败，请确认网关地址和标签接口是否可访问。";

    return (
      <section className="page-shell">
        <RequestStatePanel title="标签管理页暂时不可用" description={description} tone="warning" />
      </section>
    );
  }
}

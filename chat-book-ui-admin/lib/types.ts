export type NavItem = {
  href: string;
  label: string;
  description: string;
};

export type NavGroup = {
  title: string;
  items: NavItem[];
};

export type MetricCard = {
  label: string;
  value: string;
  detail: string;
  trend: string;
};

export type HighlightCard = {
  title: string;
  description: string;
  status: "stable" | "partial" | "gap";
};

export type ServiceReadiness = {
  service: string;
  responsibility: string;
  currentApi: string;
  backendGap: string;
  priority: "high" | "medium" | "low";
};

export type DashboardSnapshot = {
  metrics: MetricCard[];
  highlights: HighlightCard[];
  services: ServiceReadiness[];
};

export type AdminUser = {
  id: number;
  userId: number;
  username: string;
  email: string;
  role: "admin" | "user";
  profile: string;
  status: "active" | "draft" | "disabled";
  createdAt: string;
};

export type AdminArticle = {
  id: number;
  title: string;
  userName: string;
  userId: number;
  category: number;
  contentType: number;
  status: -1 | 0 | 1 | 2;
  summary: string;
  tags: string[];
  createdAt: string;
  viewCount: number;
  commentCount: number;
  praiseCount: number;
  collectCount: number;
};

export type AdminTag = {
  id: number;
  name: string;
  type: 1 | 2;
  color: string;
  sort: number;
  relatedArticles: number;
};

export type InteractionEvent = {
  id: number;
  senderName: string;
  actionType: "PRAISE" | "COLLECT" | "COMMENT" | "BROWSE";
  articleTitle: string;
  articleId: number;
  scope: string;
  summary: string;
  createdAt: string;
};

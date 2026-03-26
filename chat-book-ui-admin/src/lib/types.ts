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

export type CommonApiResponse<T> = {
  code?: number;
  data?: T;
  msg?: string;
};

export type PaginatedResult<T> = {
  list: T[];
  total: number;
  pageNo: number;
  pageSize: number;
  totalPages: number;
};

export type AdminSession = {
  token: string;
  user: CurrentAdminUser;
};

export type AdminUser = {
  id: number;
  userId: number;
  username: string;
  email: string;
  photo?: string | null;
  role: "admin" | "user" | string;
  profile?: string | null;
  status?: "active" | "draft" | "disabled";
  createdAt?: string;
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

export type ReviewArticle = {
  id: number;
  userId: number;
  title: string;
  cover?: string | null;
  summary: string;
  userName: string;
  authorAvatar?: string | null;
  category: number;
  createdAt: string;
  praiseCount: number;
  commentCount: number;
  viewCount: number;
  collectCount: number;
};

export type AdminTag = {
  id: number;
  name: string;
  type: 1 | 2 | number;
  color: string;
  sort: number;
  relatedArticles?: number;
};

export type AdminTagFormValues = {
  id?: number;
  name: string;
  type: 1 | 2;
  color: string;
  sort: number;
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

export type CurrentAdminUser = {
  id: number;
  userId: number;
  username: string;
  email: string;
  photo?: string | null;
  profile?: string | null;
  role: "admin" | "user" | string;
};

export type AdminCount = {
  userCount: number;
  articleCount: number;
  reviewCount: number;
};

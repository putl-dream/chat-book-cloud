export type CreateArticleDraftInput = {
  title: string;
  summary?: string;
  content: string;
  instruction?: string;
};

export type CreateArticleDraftResult = {
  draftId: number;
  versionNo: number;
  status: string;
};

export type CommonResult<T> = {
  code: number;
  data: T | null;
  msg?: string;
};

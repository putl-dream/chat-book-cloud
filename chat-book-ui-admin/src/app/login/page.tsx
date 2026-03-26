import { LoginForm } from "@/components/login/login-form";

type LoginPageProps = {
  searchParams?: Promise<{
    next?: string;
    reason?: string;
  }>;
};

export default async function LoginPage({ searchParams }: LoginPageProps) {
  const resolvedSearchParams = (await searchParams) ?? {};

  return <LoginForm nextPath={resolvedSearchParams.next} reason={resolvedSearchParams.reason} />;
}

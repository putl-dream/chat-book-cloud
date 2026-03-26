function normalizeApiBase(baseUrl) {
  const trimmed = (baseUrl || "").trim().replace(/\/$/, "");

  if (!trimmed) {
    return "http://localhost:8080/api";
  }

  return trimmed.endsWith("/api") ? trimmed : `${trimmed}/api`;
}

/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  async rewrites() {
    const apiBaseUrl = normalizeApiBase(
      process.env.API_BASE_URL || process.env.NEXT_PUBLIC_API_BASE_URL
    );

    return [
      {
        source: "/api/:path*",
        destination: `${apiBaseUrl}/:path*`,
      },
    ];
  },
};

export default nextConfig;

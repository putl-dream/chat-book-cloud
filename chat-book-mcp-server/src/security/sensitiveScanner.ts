export type SensitiveFinding = {
  field: string;
  type: string;
};

type PatternRule = {
  type: string;
  pattern: RegExp;
};

const RULES: PatternRule[] = [
  { type: "OpenAI-style API key", pattern: /\bsk-[A-Za-z0-9_-]{20,}\b/ },
  { type: "Bearer token", pattern: /\bBearer\s+[A-Za-z0-9._~+/=-]{20,}\b/i },
  { type: "AWS access key", pattern: /\bAKIA[0-9A-Z]{16}\b/ },
  { type: "Private key block", pattern: /-----BEGIN [A-Z ]*PRIVATE KEY-----/ },
  { type: "JDBC connection string", pattern: /\bjdbc:[a-z]+:\/\/[^\s'"`]+/i },
  { type: "Redis connection string", pattern: /\bredis:\/\/[^\s'"`]+/i },
  { type: "MongoDB connection string", pattern: /\bmongodb(?:\+srv)?:\/\/[^\s'"`]+/i },
  { type: "Inline secret assignment", pattern: /\b(password|passwd|pwd|secret|token|api[_-]?key)\s*[:=]\s*['"]?[^'"\s]{6,}/i }
];

export function scanSensitiveFields(fields: Record<string, string | undefined>): SensitiveFinding[] {
  const findings: SensitiveFinding[] = [];
  for (const [field, value] of Object.entries(fields)) {
    if (!value) {
      continue;
    }
    for (const rule of RULES) {
      if (rule.pattern.test(value)) {
        findings.push({ field, type: rule.type });
      }
    }
  }
  return findings;
}

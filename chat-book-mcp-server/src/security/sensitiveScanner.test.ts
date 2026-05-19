import assert from "node:assert/strict";
import test from "node:test";
import { scanSensitiveFields } from "./sensitiveScanner.js";

test("scanSensitiveFields reports likely secrets without returning secret text", () => {
  const findings = scanSensitiveFields({
    content: "Authorization: Bearer abcdefghijklmnopqrstuvwxyz123456"
  });

  assert.deepEqual(findings, [{ field: "content", type: "Bearer token" }]);
});

test("scanSensitiveFields allows normal draft content", () => {
  const findings = scanSensitiveFields({
    content: "This is a normal Markdown draft."
  });

  assert.deepEqual(findings, []);
});

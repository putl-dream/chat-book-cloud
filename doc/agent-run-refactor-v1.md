# Agent Run Refactor V1

## Goal

Unify the first version of the Agent creation runtime around a shared streaming contract while keeping persistence on the current final-state model.

This iteration focuses on:

- making `/creative/agent` chat actually stream
- reducing duplicated WebSocket event definitions between `AgentStudio` and the editor
- aligning chat and draft generation around the same runtime vocabulary
- avoiding database schema migration in V1

This iteration does **not** focus on:

- converting persisted messages into event-sourcing
- replacing the existing session/draft tables
- fully migrating UI rendering to a `message parts / blocks` architecture

## Current Problems

### Protocol split

- chat and draft generation use separate event names and payload conventions
- event constants are duplicated in multiple frontend modules
- chat transport uses WebSocket but still behaves as final-result delivery

### State split

- `AgentStudio` maintains chat flags and draft-stream flags independently
- the editor maintains another draft-generation state machine
- preview extraction and event consumption logic are scattered

### Rendering split

- chat is modeled as final `messageType + content + payload`
- draft preview is modeled as `buffer + preview + status text`
- both flows solve similar streaming problems with different abstractions

## V1 Design

### Persistence strategy

Keep backend persistence as final state:

- messages are still stored as final assistant/user/system rows
- drafts are still stored as final generated or adopted versions

The runtime may stream intermediate events, but the database remains canonical-final.

### Shared runtime vocabulary

Use a shared runtime distinction:

- `chat stream`
- `draft stream`
- `final message`
- `final artifact`

In V1 this is implemented through shared event constants and shared preview helpers, not through a full database redesign.

### WebSocket contract direction

#### Chat

- `AGENT_CHAT_START`
- `AGENT_CHAT_DELTA`
- `AGENT_CHAT_DONE`
- `AGENT_CHAT_ERROR`

Chat deltas stream preview text only.
The final `DONE` event still carries the canonical parsed assistant message.

#### Draft generation

Keep the current draft events in V1:

- `AGENT_DRAFT_GENERATE_START`
- `AGENT_DRAFT_GENERATE_STATUS`
- `AGENT_DRAFT_GENERATE_DELTA`
- `AGENT_DRAFT_GENERATE_DONE`
- `AGENT_DRAFT_GENERATE_ERROR`
- `AGENT_DRAFT_GENERATE_STOPPED`

V1 does not rename draft events on the wire. It only centralizes their definitions and consumption.

## Implementation Plan

1. Add a shared stream-constants module for the frontend and remove duplicated literal event names.
2. Add backend stream constants and reuse them from chat and draft services.
3. Make chat tasks support streaming through the Anthropic execution engine.
4. Stream chat preview deltas from the backend by extracting partial `content` from the structured JSON response.
5. Keep `AGENT_CHAT_DONE` as the final canonical payload used for persistence hydration and card rendering.
6. Update `AgentStudio` to consume the shared constants and streamed chat deltas.
7. Update editor draft-generation logic to consume the same shared constants module.
8. Add targeted tests for chat WebSocket streaming behavior.

## Acceptance Criteria

- `/creative/agent` chat no longer waits for the full assistant reply before showing text.
- interactive-form replies still render correctly on final completion.
- editor and `AgentStudio` do not duplicate Agent WebSocket event names.
- existing persisted session detail APIs still work without schema changes.
- draft generation behavior remains functionally unchanged.

## Migration Boundary

If V1 stabilizes, V2 can move toward:

- unified `run/message/artifact` frontend state
- block/part-based message rendering
- generic runtime events such as `message.started`, `message.delta`, `artifact.completed`

That work is intentionally deferred until V1 proves the shared streaming layer.

## V2 Optimization Plan

### Why V2 is needed

V1 proves the shared streaming contract, but it still leaves two visible UX gaps:

- chat preview deltas are derived from partial structured JSON, so update granularity is still coarse
- the frontend still renders streamed chat through whole-message markdown-to-HTML refresh, so content appears to "jump" instead of grow naturally

V2 should optimize the runtime around real incremental rendering instead of only shared event naming.

### V2 Goals

- make `/creative/agent` chat feel continuously streamed instead of paragraph-level refreshed
- separate `stream preview` from `final canonical message` more explicitly
- unify Agent Studio and editor around one runtime state model
- prepare the frontend for message parts / blocks without forcing a database migration

### V2 Non-goals

- replacing current persisted `session / message / draft` tables
- introducing event sourcing on the backend
- redesigning all published article rendering around streaming blocks

### V2 Runtime Direction

#### Shared runtime objects

Introduce a frontend runtime distinction:

- `run`: one active generation lifecycle bound to a session and a task kind
- `stream preview`: raw incremental text shown immediately during generation
- `final message`: structured assistant payload committed on completion
- `artifact preview`: draft/article preview derived from stream data
- `final artifact`: persisted draft/article version committed on completion

This keeps persistence canonical-final, while making runtime state explicitly incremental.

#### Recommended event shape

V2 should move toward generic runtime events while preserving backward compatibility during migration:

- `message.started`
- `message.delta`
- `message.completed`
- `message.failed`
- `artifact.started`
- `artifact.delta`
- `artifact.status`
- `artifact.completed`
- `artifact.failed`
- `artifact.stopped`

Existing `AGENT_CHAT_*` and `AGENT_DRAFT_GENERATE_*` events can remain as compatibility aliases during rollout.

### Key V2 Optimizations

#### 1. Backend: stream raw preview text for chat

For chat text replies, stream preview should no longer depend on extracting `content` from a partially completed structured JSON envelope.

Recommended direction:

- streaming path emits raw assistant preview text as soon as the model returns text chunks
- completion path still parses the full structured response into the canonical assistant message
- if the final structured parse fails, the raw preview should still be available for fallback error handling and diagnostics

This avoids the current coarse-grained preview behavior caused by partial JSON field extraction.

#### 2. Frontend: split streaming renderer from final renderer

Chat UI should not render stream updates through final `RichTextViewer` semantics on every delta.

Recommended direction:

- during streaming, render a lightweight `StreamingMessageRenderer`
- renderer supports plain text first, then limited markdown affordances such as line breaks, lists, and inline code
- on `message.completed`, replace the preview renderer with the final structured rich-text renderer

This prevents whole-message `v-html` replacement on each delta and makes the text grow naturally.

#### 3. Frontend: message parts / blocks

Move chat rendering from a single `content` string toward message parts:

- `text`
- `status`
- `interactive_form`
- `draft_hint`
- future: `citation`, `tool_result`, `outline`, `selection_patch`

V2 does not need full persistence migration for parts. Parts can remain runtime-only and collapse back into the final canonical message on completion.

#### 4. Shared run state

Replace separated flags such as `chatting`, `generatingDraft`, `draftStreamingBuffer`, and editor-local generation flags with a shared runtime shape:

- `runId`
- `runKind`
- `status`
- `sessionId`
- `previewText`
- `previewParts`
- `artifactBuffer`
- `artifactPreview`
- `finalMessage`
- `finalArtifact`

This reduces duplicated state machines between `AgentStudio` and the editor.

#### 5. Completion reconciliation

When a stream completes:

- preview text is frozen
- final structured message replaces preview-only runtime fields
- scene metadata and notebook updates are committed once
- history hydration always uses the final canonical payload, not the preview payload

This keeps runtime streaming flexible without polluting persistence semantics.

### V2 Implementation Plan

1. Add a `run-runtime` frontend module that models `run`, `message preview`, and `artifact preview`.
2. Introduce a lightweight `StreamingMessageRenderer` for chat preview and use it only while a message is still streaming.
3. Keep `RichTextViewer` for completed assistant messages and interactive cards.
4. Refactor `AgentStudio` store to store preview state separately from final message state.
5. Refactor editor draft-generation state to reuse the same runtime concepts and helper functions.
6. Add backend support for raw preview-text streaming in chat flows.
7. Keep final structured parsing on `DONE`, and persist only the canonical assistant message.
8. Add compatibility mapping from old event names to new runtime event categories.
9. Add fallback handling for parse mismatch between preview text and final structured message.
10. Add telemetry for stream start, first delta latency, completion latency, cancellation, and parse failures.
11. Add targeted tests for incremental frontend rendering, not just event delivery.
12. Remove duplicated preview extraction logic once both chat and draft flows run through the shared runtime layer.

### Suggested Delivery Phases Inside V2

#### V2.1 Chat UX correction

- backend emits finer chat preview deltas
- frontend stops using final rich-text rendering for in-flight chat messages
- acceptance focus: streamed text no longer appears all at once

#### V2.2 Shared runtime extraction

- unify Agent Studio and editor around shared run state and helpers
- remove duplicated stream buffering logic
- acceptance focus: both chat and draft flows use the same runtime vocabulary

#### V2.3 Parts-based rendering

- introduce runtime-only message parts / blocks
- support richer assistant outputs without overloading `content + payload`
- acceptance focus: interactive forms, text preview, and future artifacts coexist in one render model

### Acceptance Criteria for V2

- `/creative/agent` shows visibly incremental chat growth rather than whole-message jumps
- frontend no longer re-renders in-flight assistant chat as full rich HTML on every delta
- backend chat preview streaming no longer depends on partial structured JSON field extraction as the primary preview source
- `DONE` still carries the canonical parsed assistant payload used for history hydration and persisted message display
- editor draft generation and Agent Studio chat share a consistent runtime model
- interactive-form messages still complete correctly and do not regress

### Migration Notes

- V2 should be implemented behind compatibility adapters rather than as a flag day rewrite
- persisted APIs and database schema remain unchanged
- old WebSocket event names may remain until both frontend surfaces have migrated

### Recommended Priority

If only one V2 slice is implemented first, prioritize:

1. chat preview source optimization on the backend
2. streaming-only renderer on the frontend
3. shared runtime extraction

This order gives the biggest user-visible improvement with the lowest schema and persistence risk.

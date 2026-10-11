# Implementation notes

## Requirement mapping

- Product philosophy: dashboard, Today, planner, focus, learning, projects, goals, reports and analytics are represented as separate modules.
- Nuxt UI: UI primitives are built with Nuxt UI components instead of custom primitive components.
- Responsive: desktop sidebar collapses; mobile uses a drawer.
- RTL: Persian-first layout and typography.
- Theme: light/dark/system through Nuxt color mode.
- Task data: status, priority, estimated/actual duration, category and tags are represented.
- Planner: day/week/month selector and time-block workspace are present; drag/resize is intentionally reserved for the next stable phase.
- Plan recovery: recovery suggestion is shown without silently changing data.
- Focus: presets, pause, reset and distraction logging are implemented in the demo UI.
- Learning: courses and topic progress are represented.
- Goals/projects: linked domain concepts are represented in the data model and UI.
- Reports: daily summary inputs and daily/weekly/monthly selector are represented.
- Analytics: meaningful metrics, trend visualization and transparent score are represented.
- Insights: rule-based examples are shown and marked by kind.
- Notifications: center, preferences and quiet hours UI are represented; PWA config is included.
- Search/command palette: application shell reserves the global command/search entry; full keyboard command palette integration is next phase.
- Database: normalized PostgreSQL/Drizzle schema includes the core domains requested.
- Security: Zod server validation and server-side DB utility boundaries are included. Production auth/session/authorization must be completed before public deployment.
- Resilience: focus API calculates elapsed time from timestamps; UI demo timer remains local for responsiveness.
- Testing: unit test starter included for analytics. Critical-flow E2E is next phase.

## Intentional boundaries

The supplied specification explicitly says not to over-engineer and to build incrementally. This archive therefore does not pretend that a complete production authentication, push infrastructure, full drag/resize calendar engine, E2E suite and every persistence mutation are already finished. It provides a coherent Phase 0 + application shell + functional demo foundation and a database/API boundary that can be extended without rewriting the UI.

## Next production phase

1. Add authentication/session middleware and user isolation to every server query.
2. Replace demo composable mutations with repositories backed by Drizzle.
3. Add migration + seed execution.
4. Add complete task/project/goal/course CRUD.
5. Implement real planner drag/resize.
6. Make focus completion persist automatically to FocusSession + TimeEntry.
7. Add daily/weekly/monthly aggregation services.
8. Add real browser notification permission + service-worker scheduling where supported.
9. Add Ctrl/Cmd+K CommandPalette with global search.
10. Add Playwright E2E for the ten critical flows.
11. Run typecheck/build/lint in a network-enabled Node environment and fix dependency-version-specific issues.

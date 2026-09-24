# Agent skills managed by Microsoft APM

The source of truth is `apm.yml`: original upstream repositories, skill subpaths,
and pinned commits. `apm.lock.yaml` records resolved content and deployment ownership.
Do not edit the installed `.agents/skills` files or recreate a local generic
`koku-engineering-skills` package. APM necessarily downloads local runtime files;
these are installations, not independently maintained sources.

## Reproduce and verify

From the repository root, with APM on PATH:

```powershell
# Avoid Windows path-length failures in the desktop app's long default cache path.
$env:APM_CACHE_DIR = Join-Path (Get-Location) 'apm_modules/.cache'
apm install --frozen --target codex
apm audit --ci
```

The cache is under the already ignored `apm_modules` directory. No global environment
or Git setting is required. Change upstream refs deliberately in `apm.yml`, then run
`apm install --target codex` and audit again. Do not hand-edit the lockfile.

## Coverage (2026-09-24)

31 upstream dependencies; package count is not a claim of complete topic coverage.

| Requested area | Installed skills | Original repository |
| --- | --- | --- |
| Angular | angular-developer | angular/skills |
| DaisyUI / Tailwind | daisyui, including bundled usage/config/component guides | saadeghi/daisyui |
| Accessibility, web performance | accessibility, performance | addyosmani/web-quality-skills |
| DDD | ddd-best-practices | luckys/agent-skills |
| Software architecture | architecture-designer | Jeffallan/claude-skills |
| Architecture decisions | create-architectural-decision-record | github/awesome-copilot |
| SonarQube / SonarCloud | sonar-fix-issue, sonar-list-issues, sonar-quality-gate | SonarSource/sonarqube-agent-plugins |
| Database | postgres-pro | Jeffallan/claude-skills |
| Spring Kafka | 314-frameworks-spring-kafka | jabrena/plinth |
| Kafka diagnostics | kafka-consumer-lag | lensesio/agentic-engineering-for-apache-kafka |
| Maven | 110-java-maven-best-practices | jabrena/plinth |
| Java design and test strategy | 121-java-object-oriented-design, 130-java-testing-strategies | jabrena/plinth |
| Spring Boot, Boot 4 tests, Testcontainers | java-springboot, spring-boot-testing | github/awesome-copilot |
| OpenAPI | openapi-spec-generation | wshobson/agents |
| Docker builds | multi-stage-dockerfile | github/awesome-copilot |
| Docker/Compose, CI/CD, deployments, incidents | devops-engineer | Jeffallan/claude-skills |
| Observability | monitoring-expert | Jeffallan/claude-skills |
| Code review | code-reviewer | Jeffallan/claude-skills |
| GitHub issues, releases, Actions | github-issues, github-release, github-actions-hardening, github-actions-efficiency | github/awesome-copilot |
| Security, privacy | security-review, gdpr-compliant | github/awesome-copilot |
| Browser tests | playwright-generate-test | github/awesome-copilot |
| Local webapp testing | webapp-testing | anthropics/skills |

The previous `.github/skills/code-review` dependency was specific to maintaining
Awesome Copilot itself; it has been replaced with a general code-review skill.

## Limitations and remaining gaps

- Enterprise-wide architecture/governance is only partially covered by software
  architecture, DDD and ADRs. No dedicated enterprise-architecture skill is installed.
- No dedicated replacements yet for Keycloak, Git worktree workflows, full backup /
  disaster recovery, or timezone/calendar engineering.
- Dependency management has Maven coverage; a dedicated cross-stack dependency-upgrade
  workflow is still missing.
- Sonar skills are installed, but the `sonar` CLI was not found on PATH during setup.
  Live queries require a separately configured/authenticated compatible CLI or MCP.
  No credentials, MCP servers, hooks or external service settings were changed here.
- Kafka live lag inspection needs an appropriate MCP connection; the skill supports
  codebase-only inspection when unavailable. Spring Kafka development guidance does
  not require that connection.
- GitHub issue and Playwright-generation workflows reference their respective MCP
  tools. Installing a skill does not install those tools; webapp-testing offers a
  separate script-based local Playwright workflow.
- Upstream examples are guidance, not verified Koku changes. Resolve versions and APIs
  against the actual project. In particular, project-specific Boot 4 and generated
  OpenAPI-client constraints in AGENTS.md remain authoritative. Do not retrofit an
  unrelated error schema, persistence model, deployment stack or serializer.
- APM audit checks package integrity/drift; it does not certify technical correctness
  or safety of every upstream example. The optional organization-policy lookup for
  `domschmidt/.github-private` currently fails, so no such policy is enforced.

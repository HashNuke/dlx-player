# DLX Player

The notes directory is named `dlx-player`.

## Project hygiene

Work in small, coherent checkpoints that leave the project building and usable.
Keep the implementation, focused tests, and relevant documentation for a
checkpoint together.

Use red-green-refactor for behavior changes:

1. Write the smallest focused test that describes the desired externally
   observable behavior, then run it and confirm it fails for the expected reason.
2. Implement the smallest coherent change that makes the test pass.
3. Refactor only after the test is green, while keeping the focused test and the
   broader relevant test suite green.

Do not write the implementation first and backfill tests. Documentation-only,
comment-only, configuration-only, and mechanical changes with no runtime behavior
may skip the initial red test, but still require proportionate verification.

Do not add tests for behavior guaranteed and tested by Kotlin, the platform SDKs,
or another dependency. Test project-owned contracts, integration boundaries,
state handling, failure handling, and protocols.

Keep tests in the module that owns the behavior (for example `shared` for common
logic). Keep tests that need network access or a live Jellyfin server out of
the default test run, and mark them clearly as integration tests.

## Git and commit hygiene

- Inspect `git status --short` and the relevant diffs before and after changes.
  Preserve user changes and unrelated work already in the worktree.
- Do not create commits unless the user asks. When asked, make each commit one
  small, coherent, usable checkpoint and include its implementation, tests,
  documentation, and relevant lockfile or version-catalog changes together.
- Prefer several small commits over one large commit. Each commit should build
  and pass tests on its own.
- Avoid WIP, fixup, and vague commits. Use a concise imperative subject. Use the
  body to record motivation, architectural decisions, migration concerns, and
  verification when those details are not obvious from the diff.
- Stage exact paths and inspect `git diff --cached` before committing. Do not use
  broad staging such as `git add -A` in a dirty worktree.
- Never commit credentials, secrets, personally identifiable data, local absolute
  paths, `local.properties`, `build/` output, IDE caches, or release artifacts.
- Do not amend, rebase, reset, force-push, delete branches, or undo existing
  commits unless the user explicitly requests that exact operation.

## Completion checks

For code or dependency changes, run these from the project root and fix
project-owned failures before finishing:

```shell
./gradlew spotlessCheck
./gradlew detekt
./gradlew build
```

Use `./gradlew spotlessApply` to fix formatting. For documentation-only changes,
verify the changed documentation instead.

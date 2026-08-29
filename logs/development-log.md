# Recall — Development Log (AI-Assisted Software Engineering)

This log summarises the prompts and interactions with an AI assistant during the
development of Recall, organised by phase. The summaries were generated with AI
assistance and then verified by me against the actual work.

---

## Planning & Design (pre-implementation)

- **Ideation.** Asked the assistant for project ideas that would meet the CS2103T
  iP complexity bar while using AI in the process. Options included a BJJ log, a
  job-application tracker, a budget tracker, and a spaced-repetition flashcard
  tool. I chose an **Anki-style SM-2 flashcard app**.
- **iP analysis.** Shared my CS2103T iP repository. The assistant cloned and read
  the Yoda task-manager code to establish the complexity bar and identify what
  could be reused (Gradle setup, the `Launcher`/`MainApp` bootstrap, the
  parser→command pattern).
- **Concept clarifications.** Asked for plain-language explanations of the **SM-2
  algorithm**, **FXML**, and **Gradle**, to make sure I understood the tools
  before building.
- **Key pivot.** I originally planned a chatbot interface (mirroring the iP), then
  realised the spec only requires a *Java desktop app*, not a chatbot. Switched to
  a **menu-driven JavaFX GUI with multiple decks**, which fits a flashcard app far
  better. This changed the hardest design problem from parsing to managing GUI
  state.
- **Timeline.** Mapped an 8-phase plan: setup → model → scheduler → GUI → breadth
  → docs → buffer → submit.

---

## Phase 0 — Setup & walking skeleton

- **Goal.** Stand up the repo and a walking skeleton: an empty main-menu window
  that builds and runs from a clean repository.
- **Key interaction.** I was confused about what to copy from the iP. The
  assistant clarified three buckets: copy *verbatim* (the Gradle wrapper); copy
  and *edit* (`build.gradle`, `.gitignore`); and *create new* (the GUI files) —
  and noted that the earlier "reuse the gui package wholesale" advice no longer
  applied after the pivot to a GUI, since the chatbot UI files are replaced.
- **Work done.** Created `CS3227-2610-MP1`; ported the wrapper; edited
  `build.gradle` (`mainClass` → `recall.gui.Launcher`, shadow base name →
  `recall`, removed the chatbot stdin block); wrote `settings.gradle`, a cleaned
  `.gitignore`, and a README; added `Launcher`, `MainApp`, a placeholder
  `MainMenuController`, and `MainMenu.fxml`.
- **Verification.** FXML-to-controller wiring cross-checked (controller ref,
  `fx:id`, and `onAction` all resolve). JavaFX itself could not be compiled in the
  assistant's sandbox, so `./gradlew run` was confirmed on my machine.

---

## Phase 1 — Model layer

- **Goal.** Build the pure, testable domain and persistence, with no GUI.
- **Work done.** `Flashcard` (front/back plus SM-2 state, with save-line
  serialisation), `Deck` (named card list with due-filtering and search),
  `Storage` (one text file per deck, base directory injected for testability), and
  `DeckRepository` (in-memory collection, name validation, case-insensitive
  duplicate rejection).
- **Decisions.** Pipe-delimited save format **with escaping**, so a card
  containing `|`, a backslash, or a newline round-trips correctly. **One file per
  deck** under `data/decks/`. Deck names restricted to a filename-safe set and
  treated case-insensitively.
- **Verification.** Compiled with `javac`; a standalone smoke test exercised
  serialisation (including the escape edge cases), due-filtering, multi-deck
  save/reload, and the repository operations — **14/14 checks passed** against real
  files. JUnit tests written: `FlashcardTest`, `DeckTest`, `StorageTest`,
  `DeckRepositoryTest`.

---

## Phase 2 — SM-2 scheduler (test-first)

- **Goal.** Implement the SM-2 algorithm as the app's algorithmic core.
- **Key interaction (verification-first).** Rather than trust a generated
  implementation, I prompted the assistant to **verify the exact SM-2 spec against
  the canonical SuperMemo source first**. This surfaced a genuine ambiguity: the
  spec says to adjust the ease factor after *every* repetition, yet also says a
  failed card restarts "without changing the E-Factor". I made a **documented
  decision** to adjust ease on every grade (matching the common reference
  implementations) and recorded the alternative and its "ease-hell" trade-off in
  the code.
- **Work done.** `Grade` enum (AGAIN=2, HARD=3, GOOD=4, EASY=5),
  `SchedulingStrategy` interface, `Sm2Scheduler`, and `ReviewSession` (a
  single-pass walk over a deck's due cards).
- **Verification.** Compiled; a smoke test pinned the exact numbers — interval
  schedule 1 → 6 → 15, ease adjustments for each grade, the 1.3 floor, and the
  full session lifecycle — **16/16 checks passed**. JUnit tests written:
  `Sm2SchedulerTest`, `ReviewSessionTest`.

---

## Phase 3 — GUI

- **Goal.** Build the three screens and the navigation between them.
- **Key interaction (stateless → stateful).** The chatbot design had been
  stateless request→response; a GUI must hold state. The assistant proposed a
  `Navigator` (owns the window, swaps the scene root, passes the selected deck
  between screens) and reused `ReviewSession` for review state. A framework
  subtlety I had to steer: JavaFX controllers can't take dependencies via their
  constructor because the `@FXML` fields only exist after the FXML loads, so
  dependencies are injected through an `init(...)` method called right after
  loading.
- **Work done.** `Navigator`, an updated `MainApp`, and three controller+FXML
  pairs: `MainMenu` (deck list, create/open/delete), `DeckView` (card list, add,
  delete, review), and `ReviewView` (front → show answer → four rating buttons).
- **Decision / scope.** Deferred **edit-card** to Phase 4 to keep this
  (largest, untestable-in-sandbox) phase focused.
- **Verification.** All three screens' wiring machine-checked (every `fx:id`,
  `onAction`, and `fx:controller` resolves). JavaFX could not be run in the
  sandbox; I ran `./gradlew run` and confirmed the full flow (create deck → add
  cards → review → grade → due count drops → persists across restart).

---

## Phase 4 — Breadth & robustness

- **Goal.** Add statistics, the deferred edit-card feature, and harden error
  handling.
- **Work done.** A `DeckStats` record and `Deck.stats(today)` classifying cards as
  new / young / mature (using Anki's **21-day maturity convention**, verified by
  search) plus average ease; an **edit-card dialog** and a **stats dialog** on the
  deck view; and **malformed-line skipping** in `Storage` so one corrupt line
  warns and is skipped instead of crashing the whole load.
- **Verification.** Stats and storage resilience smoke-tested — **11/11 checks
  passed**, including the interval-21 boundary landing in "mature" and a corrupt
  line being skipped. New JUnit tests: `DeckStatsTest` plus a malformed-line test
  in `StorageTest`. The two new buttons were wiring-checked.
- **Debugging interaction.** One test (`loadDeck_skipsMalformedLinesButKeepsValidCards`)
  kept failing. I pasted only the stack trace and the file I *thought* was current.
  The assistant noticed the trace's line number didn't match that file — the
  failing `fromSaveLine` call was on a line that, in the fixed version, sits inside
  a try/catch — and **reproduced my exact error by reverting that one method**,
  proving my compiled code was still the old version: my edit hadn't been saved to
  the file the build compiled. Fix: re-apply the hardened `loadDeck` and
  `./gradlew clean test`.

---

## Phase 5 — Documentation

- **Goal.** Write the three required documents.
- **Work done.** `docs/UserGuide.md` (setup plus every feature with steps),
  `docs/DeveloperGuide.md` (architecture, design patterns, the SM-2 design and its
  documented decision, persistence, testing, and acknowledgements), and a drafted
  `docs/Reflections.md` with three detailed prompt examples.
- **Verification.** Before writing, the assistant grepped the code for exact button
  labels, the deck-name rule, the SM-2 quality mapping, and the maturity threshold,
  so the docs match the product precisely (the spec treats mismatches as bugs). I
  personalised the reflections with my actual prompt text and voice.

---

## Recurring themes (for the reflection)

- **Verify against a source of truth, not the model's memory** — most valuable on
  the SM-2 spec.
- **The model optimises for the happy path** — edge-case robustness (delimiter
  collisions, malformed files) had to be prompted for explicitly.
- **Framework constraints are mine to supply** — e.g. the FXML controller
  lifecycle.
- **Evidence beats assumptions when debugging** — a stack trace's line numbers
  revealed a stale file I was sure was current.
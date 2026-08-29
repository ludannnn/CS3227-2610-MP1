# Recall — Reflections on AI-Assisted Software Engineering

Throughout this project I used an AI assistant as a pair-programming partner:
for design discussion, generating code, and verifying its own output. Below are
three interactions that shaped the project — why I framed each prompt that way,
what the model got right or wrong, and what engineering judgement was still
required. My full prompt logs are in `logs/`.

## Example 1 — Making the model verify the algorithm, not just write it

**Context.** The SM-2 scheduler is the core of the app. My instinct was to ask
for an implementation directly, but I'd read that LLMs often produce SM-2 code
that is subtly wrong on the edge cases, so I framed the prompt around
verification first.

**Prompt (paraphrased — replace with your logged version).** "Before writing the
scheduler, verify the exact SM-2 spec against the canonical SuperMemo source —
especially what happens to the ease factor on a failed card — and flag any
ambiguity."

**What happened.** Instead of generating code immediately, the assistant
searched the primary SuperMemo sources and surfaced a genuine ambiguity: the
canonical description says to adjust the ease factor after *every* repetition,
but also says a failed card restarts "without changing the E-Factor" — a
contradiction that different implementations resolve differently.

**What I learned.** This would have slipped through if I'd trusted a first-pass
generation. I made a deliberate, documented decision (adjust ease on every
grade, matching the common reference implementations) and recorded the
alternative and its "ease-hell" trade-off in the code. Verifying against the
source rather than the model's memory is the most valuable habit I took from
this project.

## Example 2 — Redesigning for state when the interface changed

**Context.** I originally planned a chatbot-style interface like my CS2103T iP,
then realised a menu-driven GUI fits a flashcard app far better. That changed
the hardest design problem: a chatbot is stateless request→response, but a GUI
has to hold state — which screen, which deck, where in a review.

**Prompt (paraphrased).** "We're switching from a chat interface to a
menu-driven JavaFX GUI. How should navigation and the review session hold state
across screens?"

**What happened.** The assistant proposed a `Navigator` that owns the window and
swaps screens, plus a `ReviewSession` that holds the due-card queue and current
position. One subtlety it initially missed: JavaFX controllers can't take
dependencies through their constructor, because the `@FXML` fields only exist
after the FXML loads — so dependencies have to be injected via an `init(...)`
method called right after loading.

**What I learned.** Prompting was excellent for the overall structure, but the
model defaulted to constructor injection (the "normal" Java way) and I had to
steer it toward the FXML lifecycle. The framework-specific constraint was mine
to supply.

## Example 3 — Robustness the model didn't volunteer, and a debugging session

**Context.** I store each deck as pipe-delimited text. When I first asked for
serialisation, the generated approach implied a naive `split("|")`.

**Prompt (paraphrased).** "What happens if a card's front or back contains a `|`
or a newline? Make the save format survive that."

**What happened.** A naive split corrupts any card containing the delimiter or a
multi-line answer. The revised design escapes `\`, `|`, and newlines on save and
reverses it on load, so a line always splits into exactly six fields. I later
added skipping of malformed lines so one corrupt line can't crash the app.

**A related debugging moment.** One test kept failing
(`loadDeck_skipsMalformedLinesButKeepsValidCards`). I pasted only the stack
trace and the file I *thought* was current. Rather than taking my file at face
value, the assistant noticed the line number in the trace didn't match the file
I'd sent — the failing call sat on a line that, in the fixed version, was inside
a try/catch. It reproduced my exact error by reverting that one method, proving
my compiled code was still the old version: my edit hadn't been saved to the
file the build compiled.

**What I learned.** Two things. LLMs optimise for the happy path and won't
volunteer edge-case robustness unless asked — "what breaks this?" is on me. And
when debugging, the evidence (a stack trace's line numbers) beats assumptions,
including my own about which version of a file was running.

## Overall
Prompting was most effective for structure, boilerplate, and recalling
conventions, and least effective when a decision needed a source of truth (the
SM-2 spec) or a framework constraint (the FXML lifecycle) — there, my job was to
know what to verify and what to correct. AI accelerated the work but didn't
remove the need for judgement about correctness, edge cases, and evidence.
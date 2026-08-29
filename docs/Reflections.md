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

**Prompt:** "what exactly is the sm2 algorithm and how do we implement it, verify from the
official source and other variations of anki apps."

**What happened.** Instead of generating code immediately, the assistant
searched the primary SuperMemo sources and surfaced a genuine ambiguity: the
canonical description says to adjust the ease factor after *every* repetition,
but also says a failed card restarts "without changing the E-Factor" — a
contradiction that different implementations resolve differently.

**What I learned.** This would have slipped through if I'd trusted a first-pass
generation. I made a deliberate, documented decision (adjust ease on every
grade, matching the common reference implementations) and recorded the
alternative and its trade-off in the code. Verifying against the
source rather than the model's memory and cross referencing against other
implementations is one of the valuable habits I took from
this project. This was particularly because the lectures emphasised that 
LLMs were trained on old data and since the algorithm was created long ago, 
newer implementations may not be considered in the first pass iteration of 
the algorithm.

## Example 2 — Redesigning for state when the interface changed

**Context.** I originally planned a chatbot-style interface like my CS2103T iP,
then realised a menu-driven GUI fits a flashcard app far better. That changed
the hardest design problem: a chatbot is stateless request→response, but a GUI
has to hold state — which screen, which deck, where in a review.

**Prompt (paraphrased).** "Since we are switching from a plain chatbot to a
menu-driven app that requires a JavaFX GUI. How should navigation and the review session
hold state across screens?"

**What happened.** The assistant proposed a `Navigator` that owns the window and
swaps screens, plus a `ReviewSession` that holds the due-card queue and current
position. One subtlety it initially missed: JavaFX controllers can't take
dependencies through their constructor, because the `@FXML` fields only exist
after the FXML loads — so dependencies have to be injected via an `init(...)`
method called right after loading.

**What I learned.** Prompting was excellent for the overall structure, but the
model defaulted to constructor injection (the "normal" Java way) and I had to
steer it toward the FXML lifecycle. This was particularly eye-opening especially 
when it was emphasised in lectures that our role now has become a manager or architect
of sorts. It really became apparent how we need to make decisions on a higher level in terms
of organisation so that our code performs well.

## Example 3 — Robustness the model didn't volunteer, and a debugging session

**Context.** I store each deck as pipe-delimited text. When I first asked for
serialisation, the generated approach implied a naive `split("|")`.

**Prompt (paraphrased).** "Help me think of any problems that may arise from 
our current save state and give me some suggestions on how we can circumvent it.
Consider edge cases from the user."

**What happened.** A naive split corrupts any card containing the delimiter or a
multi-line answer. The revised design escapes `\`, `|`, and newlines on save and
reverses it on load, so a line always splits into exactly six fields. This was 
completely skipped over by me and I did not consider the fact that users may use |
characters. This was because as a user of flashcard apps myself, I have not used '|'
in my flashcards before and it did not come across as something important to factor in.
The LLM also suggested a method to better load the data so that any corrupted save lines would
not crash the whole app. 

**A related debugging moment.** One test kept failing
(`loadDeck_skipsMalformedLinesButKeepsValidCards`). I pasted only the stack
trace and the file I *thought* was current. Rather than taking my file at face
value, the assistant noticed the line number in the trace didn't match the file
I'd sent — the failing call sat on a line that, in the fixed version, was inside
a try/catch. It reproduced my exact error by reverting that one method, proving
my compiled code was still the old version: my edit hadn't been saved to the
file the build compiled.

**What I learned.** Two things. LLMs optimise for the happy path and won't
volunteer edge-case robustness unless asked "what breaks this?". I realised that a good practice 
would be to ask the LLMs what are some edge cases that I could consider. This is because without
explicitly asking, the LLM would not bring it up and i may not think about it. The main lesson was
that while LLMs are good at coming up with these edge cases, they do not prioritise it unless explicitly
asked and it is on me to point it out. I also learnt that LLMs are very good at tracing code and learning
about a code base. This ties back to the previous point on how some things are in the LLMs capability but
need to be explicitly asked for it to be shown.

## Overall
Prompting was most effective for structure, boilerplate, and recalling
conventions, and least effective when a decision needed a source of truth (the
SM-2 spec) or a framework constraint (the FXML lifecycle). There, my job was to
know what to verify and what to correct. AI accelerated the work but didn't
remove the need for high level judgement about correctness, edge cases, and evidence. It is up to 
a developer to guide and provide LLMs with effective prompts so that the LLM can be efficient
and provide useful tools for the developer. The human role is still quite integral as an 'architect'
to manage and harness LLM capabilites.
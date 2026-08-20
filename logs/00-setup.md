# Log 00 — Project setup (Phase 0)

**Date:** 2026-08-20

## Goal
Stand up the repo and a walking skeleton: an empty main-menu window that builds and runs from a clean repo.

## What I did
- Created the public repo `CS3227-2610-MP1`.
- Copied the Gradle wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/`) verbatim from my CS2103T iP.
- Copied and edited `build.gradle`: `mainClass` → `recall.gui.Launcher`, shadow jar `archiveBaseName` → `recall`,
  and removed the chatbot-only `run { standardInput = System.in }` block.
- Wrote `settings.gradle` (was empty in the iP), a cleaned `.gitignore` (now ignores `/data/`), and a `README`.
- Added the walking skeleton: `Launcher`, `MainApp`, `MainMenuController`, and `MainMenu.fxml`.

## Prompts used
- _Record the AI prompts you used here, what the model produced, and whether the output needed fixing._
  _(This is what Reflections.md is built from — don't leave it blank.)_

## Verified
- [ ] `./gradlew run` opens a window titled "Recall" with a working "New Deck" button.
- [ ] `./gradlew shadowJar` produces `build/libs/recall.jar` that runs on its own.

# Recall — User Guide

Recall is a desktop flashcard app that schedules your reviews using the SM-2
spaced-repetition algorithm. You organise cards into decks by topic, and each
day Recall shows you only the cards that are due, spacing them further apart
each time you recall one successfully.

## Supported platforms
Recall runs on **Windows**, **macOS**, and **Linux**. The pre-built JAR bundles
JavaFX for all three platforms.

## Setting up

### Quick start (recommended)
1. Install **Java 17 or later** (a JRE is sufficient). Check with `java -version`.
2. Download `release/recall.jar` from the repository.
3. Run it:

```
java -jar recall.jar
```

No other dependencies are needed.

### Building from source
If you prefer to build from source, you will need a **JDK** (not just a JRE).

From the project root:

```
./gradlew run
```

On Windows use `gradlew run`.

To produce a standalone JAR:

```
./gradlew shadowJar
```

This creates `build/libs/recall.jar`, which you can run with `java -jar`.

### Where your data lives
Decks are saved as plain-text files under `data/decks/` (one file per deck),
created automatically the first time you save a deck. Your cards persist
between runs. To **back up** or **transfer** your data, simply copy the
`data/decks/` folder.

## Features

Recall has three screens: the **main menu** (your decks), the **deck view**
(cards in one deck), and the **review** screen.

### Creating a deck
1. On the main menu, type a name into the **Deck name** box.
2. Click **Create**.

The deck appears in the list. Names may contain letters, digits, spaces,
hyphens, and underscores, and are treated case-insensitively. E.g. you cannot
create both "Spanish" and "spanish".

### Opening a deck
Select a deck in the list and click **Open**.

### Deleting a deck
Select a deck and click **Delete**. A confirmation dialog appears; click **OK**
to remove the deck and all its cards (this also deletes its file on disk).

### Adding a card
In the deck view:
1. Type the prompt in the **Front** box and the answer in the **Back** box.
2. Click **Add**.

Both fields are required. Card text may contain any characters, including
multi-line answers.

### Editing a card
1. Select a card in the list.
2. Click **Edit card**.
3. Change the front and/or back in the dialog and click **OK**.

### Deleting a card
Select a card and click **Delete card**.

### Viewing statistics
Click **Stats** in the deck view to see the total number of cards, how many are
due now, a breakdown of new / young / mature cards, and the deck's average
ease. A card is **new** until its first successful review, **mature** once its
interval reaches 21 days, and **young** in between.

### Reviewing due cards
1. In the deck view, click **Review due cards**. (If nothing is due, Recall
   tells you so.)
2. Read the front, then click **Show Answer**.
3. Rate how well you recalled it:
   - **Again** : you didn't recall it; the card resets and returns tomorrow.
   - **Hard** : recalled with difficulty.
   - **Good** : recalled correctly.
   - **Easy** : recalled effortlessly.
4. Recall schedules the card based on the score and shows the next one. When all due cards  are done, it displays "All caught up".

You can leave a review at any time with **Back to deck**; grades already given
are saved.

## How scheduling works
Each card carries an interval (days until next review), a repetition count, and
an ease factor. A correct review pushes the next review further out - 1 day,
then 6 days, then roughly interval × ease each time. Choosing **Again** sends
the card back to a 1-day interval. Cards you find hard get a lower ease, so they
return more often. The exact formulas are in the Developer Guide.

## FAQ / Troubleshooting

**Q: `java -version` shows a version older than 17.**
A: Download and install Java 17 or later from
[Adoptium](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/technologies/downloads/).
Make sure the new version is on your `PATH`.

**Q: I get a JavaFX module error when launching.**
A: Use the pre-built `release/recall.jar` — it bundles all JavaFX modules.
If you are building from source, run via `./gradlew run` rather than
`java -jar` on a plain `build/libs/` JAR, since the Shadow JAR is the one
that includes JavaFX.

**Q: The `data/decks/` folder does not exist.**
A: It is created automatically the first time you save a deck. If you have not
created any decks yet, the folder will not be there.

**Q: Double-clicking the JAR does nothing.**
A: Open a terminal and run `java -jar recall.jar` instead. This also lets you
see any error messages in the console.
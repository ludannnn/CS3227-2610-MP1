CS3227-2610-MP1/
├── build.gradle                          ✓  (main/shadow names already edited)
├── settings.gradle                       ✓
├── .gitignore                            ✓
├── README.md                             ✓
├── gradlew  gradlew.bat                  ✓  (copied verbatim from iP)
├── gradle/wrapper/
│   ├── gradle-wrapper.jar                ✓
│   └── gradle-wrapper.properties         ✓
│
├── docs/                                 [P5]
│   ├── UserGuide.md                      [P5]  every feature + setup/test steps
│   ├── DeveloperGuide.md                 [P5]  architecture, patterns, acknowledgements
│   └── Reflections.md                    [P5]  ≥3 detailed prompt examples
│
├── logs/                                     one entry per working session
│   ├── 00-setup.md                       ✓
│   ├── 01-model.md                       [P1 wrap-up]
│   ├── 02-scheduler.md                   [P2]
│   ├── 03-gui.md                         [P3]
│   ├── 04-breadth.md                     [P4]
│   └── 05-docs.md                        [P5]
│
├── data/                                 (runtime only — gitignored, NOT committed)
│   └── decks/
│       └── <deckname>.txt                (one file per deck, created as you use the app)
│
└── src/
    ├── main/
    │   ├── java/recall/
    │   │   ├── gui/
    │   │   │   ├── Launcher.java              ✓
    │   │   │   ├── MainApp.java               ✓
    │   │   │   ├── Navigator.java             [P3]  swaps scenes, passes selected deck
    │   │   │   ├── MainMenuController.java    ✓    (fleshed out in P3: real deck list)
    │   │   │   ├── DeckViewController.java    [P3]  cards in a deck, add/edit/delete
    │   │   │   └── ReviewController.java      [P3]  flip card, rating buttons
    │   │   ├── model/
    │   │   │   ├── Flashcard.java             ✓
    │   │   │   └── Deck.java                  ✓    (+ stats() added in P4)
    │   │   ├── srs/
    │   │   │   ├── Grade.java                 [P2]  AGAIN / HARD / GOOD / EASY
    │   │   │   ├── SchedulingStrategy.java    [P2]  (optional interface)
    │   │   │   ├── Sm2Scheduler.java          [P2]  the SM-2 engine
    │   │   │   └── ReviewSession.java         [P2]  due-card queue + progress
    │   │   └── storage/
    │   │       ├── Storage.java               ✓
    │   │       └── DeckRepository.java        ✓
    │   └── resources/
    │       ├── view/
    │       │   ├── MainMenu.fxml              ✓    (fleshed out in P3)
    │       │   ├── DeckView.fxml              [P3]
    │       │   └── ReviewView.fxml            [P3]
    │       └── styles.css                     [P3]  (optional, for polish)
    │
    └── test/java/recall/
        ├── model/
        │   ├── FlashcardTest.java             ✓
        │   └── DeckTest.java                  ✓
        ├── srs/
        │   ├── Sm2SchedulerTest.java          [P2]  the star test — interval/ease/reset
        │   └── ReviewSessionTest.java         [P2]
        └── storage/
            ├── StorageTest.java               ✓
            └── DeckRepositoryTest.java        ✓
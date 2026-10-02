# LLM Evaluation

Produced with an LLM using the prompt in `LLM-Evaluation-prompt.md`, then reviewed and answered by
you. **Both halves are required** — an unread LLM assessment pasted in whole is worth nothing.

---

## Assessment

**Material given to the LLM:** `AgeMonths.java`, `Animal.java`, `IntakeException.java`,
`Species.java`, all five test classes (`AgeMonthsTest`, `AgeMonthsAdditionalTest`, `AnimalTest`,
`AnimalAdditionalTest`, `SpeciesTest`), the starter commit (`382600b`) for comparison, and the
build output. **Not given: `submission/introspection.md`, which did not exist when this was run.**
The Species/introspection half of Category 6 is therefore marked *not assessable* rather than
guessed.

**Build state:** 48 tests, 0 failures. Checkstyle: 0 warnings on main, 0 on test.

### 1. Immutability and encapsulation — 25 / 25

- All four `Animal` fields are `private final` (`Animal.java:28–31`); `AgeMonths.months` is
  `private final` (`AgeMonths.java:22`). The added constant `MONTHS_PER_YEAR` is
  `private static final` (`AgeMonths.java:20`) — correctly *not* public, since nothing outside the
  class needs it.
- No setters, and no method assigns a field after construction.
- No accessor leaks a mutable reference: `name()` returns a `String`, `species()` an enum,
  `age()` an immutable `AgeMonths`, `intakeDate()` an immutable `LocalDate`. Returning the fields
  directly is safe in every case.
- Nothing is public that did not need to be. `count(int, String)` (`AgeMonths.java:137`) is
  correctly `private static`.

*Observation, no deduction:* `public class Animal` (`Animal.java:26`) is not `final`, unlike
`public final class AgeMonths`. A subclass could override `name()` or `toString()` and break
the "an Animal that exists makes sense" guarantee. However, the starter commit already declared it
`public class Animal`, and the lab's Javadoc points towards an "Animal family" in a later lab, so
this is a starter decision, not the student's.

### 2. Constructor validation — 25 / 25

Each rule checked individually (`Animal.java:55–75`):

| Rule | Present? | Line |
|---|---|---|
| Null name rejected | Yes — `"name must not be null"` | 56–58 |
| Empty / whitespace-only name rejected | Yes — `name.isBlank()` | 59–61 |
| Null species rejected | Yes — `"species must not be null"` | 62–64 |
| Null age rejected | Yes — `"age must not be null"` | 65–67 |
| Null intake date rejected | Yes — `"intake date must not be null"` | 68–70 |
| Name stored stripped | Yes — `this.name = name.strip();` | 71 |

- **Validate-then-assign:** every check runs before the first assignment at line 71. Correct.
- **Order:** the null check on `name` comes before `name.isBlank()`, so no `NullPointerException`
  can escape in place of an `IntakeException`.
- **Messages:** every message names the offending argument, and the blank-name message also quotes
  the rejected value (`"name must not be blank, was \"" + name + "\""`). None of them is generic.
- `strip()`/`isBlank()` were chosen over `trim()`, so Unicode whitespace such as an em space
  (` `) is handled, not just ASCII ≤ `' '`.

*Observation, no deduction:* `isBlank()` uses `Character.isWhitespace`, which does **not** count a
non-breaking space (` `) as whitespace. Checked in jshell: `" ".isBlank()` is `false`,
and `" Luna ".strip()` leaves the non-breaking spaces in place. So
`new Animal(" ", …)` is accepted, and its stored name looks blank. This meets the Javadoc as
written ("at least one non-whitespace character", in Java's sense), so it is not a deduction. It is
a real edge, though (see Category 4).

### 3. Correctness — 15 / 15

`AgeMonths.toString()` (`AgeMonths.java:118–128`) traced against each required value:

| Months | Branch taken | Output | Expected |
|---|---|---|---|
| 0 | `isUnderOneYear` → `count(0,"month")` | `0 months` | ✔ |
| 1 | `isUnderOneYear` → `count(1,"month")` | `1 month` | ✔ |
| 11 | `isUnderOneYear` → `count(11,"month")` | `11 months` | ✔ |
| 12 | `remainderMonths()==0` | `1 year` | ✔ |
| 23 | combined | `1 year, 11 months` | ✔ |
| 24 | `remainderMonths()==0` | `2 years` | ✔ |
| 25 | combined | `2 years, 1 month` | ✔ |

Singular/plural is handled in one place (`n == 1 ? unit : unit + "s"`), so years and months cannot
disagree. Whole years omit the months part. `Animal.toString()`
(`return name + " (" + species + ", " + age + ", intake " + intakeDate + ")"`) produces
`Luna (Cat, 1 year, 11 months, intake 2026-09-21)` exactly, and the provided test confirms it.

### 4. Testing and coverage — 14 / 15

**Coverage:** 100% line (51/51), 100% branch (22/22). As the README warns, the provided suite
already gets there, so this number says nothing about the student's tests.

What the student added (`AgeMonthsAdditionalTest`, `AnimalAdditionalTest`, 19 tests):

- **Boundaries:** 0 (via `Animal`), 11/12 (provided), `MAX_MONTHS - 1` (`oneBelowTheMaximumIsAllowed`,
  plus `toString` and `years`/`remainderMonths` at 479), `MAX_MONTHS`, `MAX_MONTHS + 1`, and
  `Integer.MIN_VALUE`/`Integer.MAX_VALUE`. Thorough.
- **Every exception case:** all five `Animal` rejection paths, plus both `AgeMonths` paths. Every
  `assertThrows` names `IntakeException.class`, never `Exception.class` or `RuntimeException.class`,
  so no exception test passes vacuously on the wrong exception type.
- **Message content:** a test for each null-argument message, the negative-age message (rule *and*
  value), and the too-old message (the limit `480`, which the provided test does not check: it
  checks only `481`).
- **Good inferred cases:** `"2 years, 2 months"` (the comment correctly notes that every combined
  case in the spec contains a 1, so a bug that always pluralised one unit would slip through),
  tab/newline stripping, the `BIRD` label, zero-padded ISO dates.

**Deduction (−1): assertions that could pass on a wrong implementation.**

- `toStringUsesTheStrippedName` asserts only `kiki.toString().startsWith("Kiki (")`. It would
  still pass if the rest of the string were wrong. The other `toString` tests use `assertEquals`
  on the full string, and this one should too.
- `theNullNameRefusalNamesTheName` and `AnimalTest.theRefusalSaysWhichArgumentWasWrong` both check
  only `contains("name")`. If the null and blank paths returned the *same* message (say, one merged
  `if (name == null || name.isBlank())`), both would still pass. Nothing distinguishes the two
  rejection reasons.

**Three specific untested cases:**

1. **A name of only non-breaking spaces**, `" "`. It is currently *accepted* (see Category 2).
   No test pins down either outcome, so a reader cannot tell whether that was a decision or never
   thought about.
2. **The blank-name message quoting the rejected value.** The code goes to the trouble of
   `was "<name>"`, but no test asserts the value appears in the message. That part could be
   deleted with every test still green.
3. **`strip()` versus `trim()` on Unicode whitespace**, e.g. `" Luna "`. The tab/newline
   tests pass under `trim()` too, so nothing would catch a regression from `strip()` to `trim()`.
   An em-space test would.

(Also untested, less important: `Animal.toString()` at the top of the age range, and
`age()` returning the stored instance via `assertSame`, the way the provided suite does for
`intakeDate()`.)

### 5. Code quality and style — 10 / 10

- Every public member has Javadoc. Most of it shipped with the starter. The student's own additions
  are documented, and say more than the name does: `MONTHS_PER_YEAR` ("The number of months in one
  year") and `count` ("pluralising the unit unless the quantity is exactly one", with examples).
  Checkstyle reports 0 warnings.
- **Delegation:** `Animal.toString()` contains neither "year" nor "month". It concatenates `age`,
  which calls `AgeMonths.toString()`, so it does not re-derive years and months. If the age format
  changes, `Animal` needs no edit. `AgeMonths.toString()` itself reuses `isUnderOneYear()`,
  `years()` and `remainderMonths()` rather than repeating the arithmetic.
- `MONTHS_PER_YEAR` replaces the three magic `12`s that `years`, `remainderMonths` and
  `isUnderOneYear` would otherwise each contain.

*Observation, no deduction:* `Animal.toString()` concatenates `species`, so it depends on
`Species.toString()` being overridden to return the label. `species.label()` would say what it
means, and it would survive someone removing or changing that override. The output is correct
either way.

*Observation, no deduction:* the class-level Javadoc on `AgeMonths`/`Animal` still addresses the
student ("you implement the rest", "Validate every argument before assigning any field"). This is
unchanged starter text, not student-authored.

### 6. Scope discipline and code walk — scope half: full marks; Species half: **not assessable**

- **Scope:** no inheritance (no subclasses, no `extends` added), no collections, and no
  `equals`/`hashCode` in either `Animal` or `AgeMonths`. `AnimalTest.twoIdenticalAnimalsAreDifferentObjects`
  passes. No violation.
- **Species / introspection:** `submission/introspection.md` was not provided, so there is no
  evidence either way about whether the student understands why `Species` is an enum with a
  `label` field, a constructor and `label()`/`toString()` methods rather than three bare constants.
  **No score is given for this half until the introspection exists.**

### Totals

| Category | Score |
|---|---|
| 1. Immutability & encapsulation | 25 / 25 |
| 2. Constructor validation | 25 / 25 |
| 3. Correctness | 15 / 15 |
| 4. Testing | 14 / 15 |
| 5. Code quality & style | 10 / 10 |
| 6. Scope & code walk | pending — introspection not provided |
| **Assessed** | **89 / 90** |

### Most important thing to do differently next time

Write tests that tell **different reasons for failure** apart, not just "it threw `IntakeException`
and the message mentions the argument". The suite covers every rejection path. Where it is weak, it
cannot tell one wrong implementation from a right one: the null-name and blank-name messages are
indistinguishable to the tests, the quoted value in the blank message is unpinned, and
`strip()`-versus-`trim()` is unpinned. Ask of each test: *what plausible bug would this fail on?*

### One thing done genuinely well

The `count(int n, String unit)` helper. Putting singular/plural in one private method means
`AgeMonths.toString()` is three short branches with no duplicated pluralisation logic. Years and
months cannot disagree about the rule, and the `"2 years, 2 months"` test shows the student knew
exactly which bug that design was guarding against.

**Coverage reported:** 100% line (51/51), 100% branch (22/22), from
`build/reports/jacoco/test/jacocoTestReport.xml`.

---

## Your response

The part that is actually marked. For each point below, a few sentences.

### Where it is right

Which criticisms do you accept? For each, say what you would change and why you agree.

I think that `toStringUsesTheStrippedName` is weaker than it should be. it only checks `startsWith("Kiki ("))`, so when using `toString` could get everything after the name wrong and the test woudl still pass. I used a partial check because I wanted the test to be only about the name, but comparing the full string costs one line and is able to catch more bugs, even tough most of them are covered by the other tests. 

I also accept that nothing in my suite checks that the blank-name message includes the rejected value. The test `theRefusalSaysWhichArgumentWasWrong` only checks that the message contains "name", so I could delete the was "<name>" part at Animal.java and every test would still pass.

### Where it is wrong

Which criticisms do you reject, and on what grounds? LLMs confidently misread code, invent
requirements that are not in the specification, and flag correct code as broken. Disagreeing with a
specific reason is worth more marks here than agreeing with everything.

I believe that the criticism about separate messages for a bank name and a numm name isn't strong. I think that it is not necessary to have a different message for each case. Testing that a null and blank name name are both refused with an `IntakeException` whose message mentions " name" is enoguh to catch the error and tell the person testing which argument was wrong.

### What it missed

What do you know is weak in your submission that the assessment did not mention? Volunteering this
costs you nothing and demonstrates you understand your own code.

Future intake dates are accepted, for example, creating with a 2999 the spec allows it, but it isn't realistic, which woudl create an animal that supposedly arrived at the shelter in the year 2999. After noticing this, i also checked wheter i could create a date like February 40 and i learned that `LocalDate.of` refuses those with a `DateTimeException`, before Animal constructor, so if using it, there is no need to check that the date is possible or not.

### What you changed

If you changed anything as a result, say what and why. If you changed nothing, say that and defend
it.

Didn't change anything, claude didn't find a core problem in the tests, only some small weaknesses.

---

## Declaration

- Which LLM and version you used: Claude Opus 5.5 (`claude-opus-5-5`), via Claude Code
- Confirm you understand every line you submitted, regardless of who or what wrote it: Yes

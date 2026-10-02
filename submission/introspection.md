# Design Introspection

Your own reflection on the design decisions you made this week. Written in your own words — this is
distinct from the LLM's assessment of your code, and distinct from your code-walk video.

Aim for a page. Cite your actual code: name the class and method you are talking about.

## 1. What you built

I completed `AgeMonths`, a value object for an age that can never be invalid: it only exists in the range 0 to 480 months and i also implemented
`years()`, `remainderMonths()`, `isUnderOneYear()` and `toString()`. I also built `Animal`, whose
constructor refuses bad data (a null or blank name, or a null species, age or intake date), so any
`Animal` that exists is one that makes sense. Finally, I added test cases that check boundary values
and the content of the exception messages.


## 2. Design decisions

Pick the two or three decisions you actually had to think about, and for each one:

- **What was the choice?** What were the alternatives you considered?
- **What did you pick, and why?** What would have gone wrong with the other option?
- **What did it cost?** Every real design decision costs something.

Good candidates: where you put a piece of behaviour and why it belongs there rather than somewhere
else; how you represented something so that an invalid version could not be built; where you chose to
delegate to existing code rather than re-deriving an answer.

I decided to create a constant, `MONTHS_PER_YEAR` , instead of writing 12 directly in the methods that use it. With a bare 12, the reader has to guess what the number means, and a typo in one the methos would be easy to miss. Didn't cost anything and improve readability.

I also strip the name once, when it's stored (`Animal`). I could have stored the raw name and stripped it inside `name()`. Stripping it at construction makes it so the original input is lost for good but it makes it so `name()` and `toString()` can never disagree, because there is only one version of the name.

## 3. Invariants

What does your code guarantee about itself, and where is each guarantee enforced?

For each type that validates its input: what must always be true of an instance once it exists, and
which line makes that true? If a guarantee is enforced in more than one place, say why — and whether
that is deliberate or duplication.

Once an AgeMonths exists, its age is always between 0 and 480 months. This is enforced only in AgeMonths.of, and because the constructor is private, there is no other way to create one. Once an Animal exists, its name is never null or blank and has no extra whitespace around it, and its species, age and intake date are never null. All the fields are private final and there are no setters, so these stay true for the whole life of the object.
 
The age might look like it is checked twice, but it isn't. Animal only checks that the age is not null and doesn't check the range again. This is on purpose: AgeMonths already guarantees the range, so the only thing that can still go wrong is someone passing null.

## 4. Testing

- Which cases did you add beyond the provided tests, and what made you think of them?
- Which test was hardest to write, and what did writing it teach you about your own design?
- What is still untested, and how would you test it if you had another hour?


I added `AgeMonthsAdditionalTest` and `AnimalAdditionalTes`t. I thought about some patterns when picking them: boundary values and so on, and the happy and refusal path for each rule. The hardest part was deciding about the exception message tests. At first I thought that they shouldn't be tested. But when I saw that the rubric and the README both ask for message content, and when I searched about it, I learned that throwing an error and explaining it are two different things, and testing the explanation is an important part. Still untested is a name made only of non-breaking spaces ("\u00A0"), which isBlank() accepts. With another hour I would write a test for it.

## 5. What you would change

Given another day, what would you do differently — and what stopped you this week? Be specific;
"write more tests" is not an answer.

I would probably change Animal's constructor to collect all validation error into one combined exception message, instead of stopping at the first one. The reason for that is that getting rejected four separate times is not convenient when you could see all the erros at once. I didn't do it this week because i followed the specs i received.

## 6. What you found hard

The honest one. What took the longest, what did you get wrong first, and what finally made it click?
This is not marked on whether you struggled — everyone does — but on whether you can say clearly
where and why.


The hardest part was AgeMonths.toString() (AgeMonths.java:118–128). It has several small cases to handle, and I spent most of the time thinking about how to make it work for all of them while keeping it easy to understand. What made it click was putting the singular/plural rule in one small helper, count() (:137–139), and then using simple if checks for the three cases: under one year, whole years, and years with months.


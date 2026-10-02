package edu.northeastern.shelter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * My own tests for {@link Animal}. These cover what the provided suite does not pin down: the
 * content of every refusal message, the boundary of what counts as a name, whitespace other than
 * plain spaces, and the {@code toString} cases the provided tests never exercise.
 */
@Tag("current")
class AnimalAdditionalTest {

  private static final LocalDate INTAKE = LocalDate.of(2026, 2, 10);

  @Test
  void theNullNameRefusalNamesTheName() {
    IntakeException nullName =
        assertThrows(
            IntakeException.class, () -> new Animal(null, Species.CAT, AgeMonths.of(1), INTAKE));
    assertTrue(
        nullName.getMessage().toLowerCase().contains("name"),
        "the message should name the offending argument");
  }

  @Test
  void theNullSpeciesRefusalNamesTheSpecies() {
    IntakeException nullSpecies =
        assertThrows(
            IntakeException.class, () -> new Animal("Nori", null, AgeMonths.of(1), INTAKE));
    assertTrue(
        nullSpecies.getMessage().toLowerCase().contains("species"),
        "the message should name the offending argument");
  }

  @Test
  void theNullAgeRefusalNamesTheAge() {
    IntakeException nullAge =
        assertThrows(IntakeException.class, () -> new Animal("Nori", Species.DOG, null, INTAKE));
    assertTrue(
        nullAge.getMessage().toLowerCase().contains("age"),
        "the message should name the offending argument");
  }

  @Test
  void theNullIntakeDateRefusalNamesTheDate() {
    IntakeException nullDate =
        assertThrows(
            IntakeException.class, () -> new Animal("Nori", Species.DOG, AgeMonths.of(1), null));
    assertTrue(
        nullDate.getMessage().toLowerCase().contains("date"),
        "the message should name the offending argument");
  }

  @Test
  void aOneCharacterNameIsAccepted() {
    assertEquals("X", new Animal("X", Species.DOG, AgeMonths.of(1), INTAKE).name());
  }

  @Test
  void aNameOfOnlyTabsAndNewlinesIsRefused() {
    assertThrows(
        IntakeException.class, () -> new Animal("\t\n", Species.DOG, AgeMonths.of(1), INTAKE));
  }

  @Test
  void tabsAndNewlinesAroundTheNameAreStripped() {
    assertEquals("Kiki", new Animal("\tKiki\n", Species.CAT, AgeMonths.of(1), INTAKE).name());
  }

  @Test
  void theAgeLimitsAreAccepted() {
    // Animal does no range check of its own, so both ends of AgeMonths' range must get through.
    Animal newborn = new Animal("Juju", Species.DOG, AgeMonths.of(0), INTAKE);
    assertEquals(0, newborn.age().months());

    Animal oldest = new Animal("Nori", Species.DOG, AgeMonths.of(AgeMonths.MAX_MONTHS), INTAKE);
    assertEquals(AgeMonths.MAX_MONTHS, oldest.age().months());
  }

  @Test
  void toStringUsesTheBirdLabel() {
    // The provided tests only build cats and dogs; this covers the third species.
    Animal juju = new Animal("Juju", Species.BIRD, AgeMonths.of(3), INTAKE);
    assertEquals("Juju (Bird, 3 months, intake 2026-02-10)", juju.toString());
  }

  @Test
  void toStringUsesTheStrippedName() {
    Animal kiki = new Animal("  Kiki  ", Species.CAT, AgeMonths.of(1), INTAKE);
    assertTrue(kiki.toString().startsWith("Kiki ("), "toString should use the stored name");
  }

  @Test
  void toStringWithWholeYearsOmitsTheMonths() {
    Animal nori = new Animal("Nori", Species.DOG, AgeMonths.of(24), INTAKE);
    assertEquals("Nori (Dog, 2 years, intake 2026-02-10)", nori.toString());
  }

  @Test
  void toStringPadsSingleDigitMonthAndDay() {
    Animal nori = new Animal("Nori", Species.DOG, AgeMonths.of(1), LocalDate.of(2000, 2, 4));
    assertEquals("Nori (Dog, 1 month, intake 2000-02-04)", nori.toString());
  }
}

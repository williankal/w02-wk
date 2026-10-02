package edu.northeastern.shelter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * My own tests for {@link AgeMonths}. These cover what the provided suite does not pin down: the
 * top of the range, the content of both refusal messages, and extreme inputs.
 */
@Tag("current")
class AgeMonthsAdditionalTest {

  @Test
  void oneBelowTheMaximumIsAllowed() {
    assertEquals(AgeMonths.MAX_MONTHS - 1, AgeMonths.of(AgeMonths.MAX_MONTHS - 1).months());
  }

  @Test
  void toStringAtTheTopOfTheRange() {
    assertEquals("39 years, 11 months", AgeMonths.of(AgeMonths.MAX_MONTHS - 1).toString());
    assertEquals("40 years", AgeMonths.of(AgeMonths.MAX_MONTHS).toString());
  }

  @Test
  void toStringPluralisesYearsAndMonthsIndependently() {
    // Every combined case in the spec above contains a 1; this one has neither.
    assertEquals("2 years, 2 months", AgeMonths.of(26).toString());
  }

  @Test
  void theNegativeRefusalNamesTheRuleAndTheValue() {
    IntakeException negative = assertThrows(IntakeException.class, () -> AgeMonths.of(-1));
    assertTrue(
        negative.getMessage().contains("negative"), "the message should say what rule was broken");
    assertTrue(
        negative.getMessage().contains("-1"), "the message should name the value that was rejected");
  }

  @Test
  void theTooOldRefusalNamesTheLimit() {
    IntakeException tooOld =
        assertThrows(IntakeException.class, () -> AgeMonths.of(AgeMonths.MAX_MONTHS + 1));
    assertTrue(
        tooOld.getMessage().contains(String.valueOf(AgeMonths.MAX_MONTHS)),
        "the message should say what the limit is, not only what was rejected");
  }

  @Test
  void yearsAndRemainderMonthsAtTheTopOfTheRange() {
    assertEquals(40, AgeMonths.of(AgeMonths.MAX_MONTHS).years());
    assertEquals(0, AgeMonths.of(AgeMonths.MAX_MONTHS).remainderMonths());
    assertEquals(39, AgeMonths.of(AgeMonths.MAX_MONTHS - 1).years());
    assertEquals(11, AgeMonths.of(AgeMonths.MAX_MONTHS - 1).remainderMonths());
  }

  @Test
  void extremeIntegersAreRefused() {
    assertThrows(IntakeException.class, () -> AgeMonths.of(Integer.MIN_VALUE));
    assertThrows(IntakeException.class, () -> AgeMonths.of(Integer.MAX_VALUE));
  }
}

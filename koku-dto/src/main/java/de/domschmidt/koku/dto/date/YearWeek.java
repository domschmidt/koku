package de.domschmidt.koku.dto.date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Value;

@Value
public class YearWeek implements Comparable<YearWeek> {

    private static final Pattern ISO_WEEK_PATTERN = Pattern.compile("^(\\d{4})-W(\\d{2})$");

    int year;
    int week;

    public YearWeek(final int year, final int week) {
        final int maxWeek = weeksInIsoYear(year);

        if (week < 1 || week > maxWeek) {
            throw new IllegalArgumentException(
                    "Invalid ISO week: %d-W%02d. Year %d has only %d ISO weeks.".formatted(year, week, year, maxWeek));
        }

        this.year = year;
        this.week = week;
    }

    @JsonCreator
    public static YearWeek parse(final String value) {
        final Matcher matcher = ISO_WEEK_PATTERN.matcher(value);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid ISO week value: " + value);
        }

        final int year = Integer.parseInt(matcher.group(1));
        final int week = Integer.parseInt(matcher.group(2));

        return new YearWeek(year, week);
    }

    public static YearWeek from(final LocalDate date) {
        return new YearWeek(date.get(IsoFields.WEEK_BASED_YEAR), date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
    }

    private static int weeksInIsoYear(final int year) {
        return LocalDate.of(year, 12, 28).get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
    }

    @JsonValue
    @Override
    public String toString() {
        return "%d-W%02d".formatted(this.year, this.week);
    }

    @Override
    public int compareTo(final YearWeek other) {
        final int yearComparison = Integer.compare(this.year, other.year);

        if (yearComparison != 0) {
            return yearComparison;
        }

        return Integer.compare(this.week, other.week);
    }
}

package tutortrack.logic.parser;

import static java.util.Objects.requireNonNull;
import static tutortrack.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static tutortrack.logic.Messages.MESSAGE_INVALID_DAY;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import tutortrack.commons.core.index.Index;
import tutortrack.commons.util.StringUtil;
import tutortrack.logic.parser.exceptions.ParseException;
import tutortrack.model.lesson.LessonPlan;
import tutortrack.model.lesson.LessonProgress;
import tutortrack.model.person.Address;
import tutortrack.model.person.Cost;
import tutortrack.model.person.DayTime;
import tutortrack.model.person.Name;
import tutortrack.model.person.Phone;
import tutortrack.model.person.SubjectLevel;
import tutortrack.model.tag.Tag;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX =
            "Invalid index. Please use a valid number from the displayed list (e.g., 1, 2, 3).";

    /**
     * Represents a pair of Index and LocalDate.
     */
    public static class IndexDatePair {
        private final Index index;
        private final LocalDate date;

        /**
         * Constructs an IndexDatePair with the specified index and date.
         *
         * @param index The index value
         * @param date The date value
         */
        public IndexDatePair(Index index, LocalDate date) {
            this.index = index;
            this.date = date;
        }

        public Index getIndex() {
            return index;
        }

        public LocalDate getDate() {
            return date;
        }
    }

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid.
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Parses a string containing an index and a date separated by whitespace.
     *
     * @param args The input string containing index and date
     * @param messageUsage The usage message for error reporting
     * @return An IndexDatePair containing the parsed Index and LocalDate
     * @throws ParseException if the format is invalid or parsing fails
     */
    public static IndexDatePair parseIndexAndDate(String args, String messageUsage) throws ParseException {
        requireNonNull(args);
        requireNonNull(messageUsage);
        String trimmedArguments = args.trim();
        String[] parts = trimmedArguments.split("\\s+");

        if (parts.length != 2) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, messageUsage));
        }

        try {
            Index index = parseIndex(parts[0]);
            LocalDate date = parseDate(parts[1]);
            assert index != null : "Parsed index should not be null";
            assert date != null : "Parsed date should not be null";
            return new IndexDatePair(index, date);
        } catch (ParseException pe) {
            throw new ParseException(pe.getMessage() + "\n" + messageUsage, pe);
        }
    }

    /**
     * Parses a {@code String dateString} into a {@code LocalDate}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given date string is invalid.
     */
    public static LocalDate parseDate(String dateString) throws ParseException {
        requireNonNull(dateString);
        String trimmedDate = dateString.trim();

        try {
            return LocalDate.parse(trimmedDate);
        } catch (DateTimeParseException e) {
            // Check if format is correct (yyyy-MM-dd)
            if (!trimmedDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                throw new ParseException("Invalid date format. Use yyyy-MM-dd (e.g., 2025-10-15).", e);
            }

            // Parse date components for detailed validation
            String[] dateParts = trimmedDate.split("-");
            int year = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]);
            int day = Integer.parseInt(dateParts[2]);

            // Check if month and day might be swapped
            if (month > 12 && day <= 12) {
                throw new ParseException(
                        "Invalid date: you may have swapped day and month. The format is yyyy-MM-dd.", e);
            }

            // Check for invalid month
            if (month < 1 || month > 12) {
                throw new ParseException("Invalid month: must be between 01 and 12.", e);
            }

            // Explicitly validate day for the given month
            try {
                return LocalDate.of(year, month, day);
            } catch (DateTimeException ex) {
                throw new ParseException("Invalid day for the given month. Please check your date.", e);
            }
        }
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        String trimmedName = name.trim();
        // Provide more specific error messages for common failure modes.
        if (Name.isBlank(trimmedName)) {
            throw new ParseException(Name.MESSAGE_BLANK);
        }
        if (Name.isTooShort(trimmedName)) {
            throw new ParseException(Name.MESSAGE_TOO_SHORT);
        }
        if (Name.hasInvalidChars(trimmedName)) {
            throw new ParseException(Name.MESSAGE_INVALID_CHARS);
        }
        // final defensive check
        if (!Name.isValidName(trimmedName)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(trimmedName);
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        requireNonNull(phone);
        String trimmedPhone = phone.trim();
        // Provide more specific error messages for phone
        if (Phone.isBlank(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_BLANK);
        }
        if (Phone.hasInvalidChars(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_INVALID_CHARS);
        }
        if (Phone.isTooShort(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_TOO_SHORT);
        }
        if (!Phone.isValidPhone(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new Phone(trimmedPhone);
    }

    /**
     * Parses a {@code String address} into an {@code Address}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code address} is invalid.
     */
    public static Address parseAddress(String address) throws ParseException {
        requireNonNull(address);
        String trimmedAddress = address.trim();
        if (!Address.isValidAddress(trimmedAddress)) {
            throw new ParseException(Address.MESSAGE_CONSTRAINTS);
        }
        return new Address(trimmedAddress);
    }

    /**
     * Parses a {@code String subjectLevel}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code subjectLevel} is invalid.
     */
    public static SubjectLevel parseSubjectLevel(String subjectLevel) throws ParseException {
        requireNonNull(subjectLevel);
        String trimmedSubjectLevel = subjectLevel.trim();
        if (SubjectLevel.isBlank(trimmedSubjectLevel)) {
            throw new ParseException(SubjectLevel.MESSAGE_BLANK);
        }
        if (SubjectLevel.hasInvalidFormat(trimmedSubjectLevel)) {
            throw new ParseException(SubjectLevel.MESSAGE_INVALID_FORMAT);
        }
        if (SubjectLevel.hasInvalidChars(trimmedSubjectLevel)) {
            throw new ParseException(SubjectLevel.MESSAGE_INVALID_CHARS);
        }
        if (!SubjectLevel.isValidSubjectLevel(trimmedSubjectLevel)) {
            throw new ParseException(SubjectLevel.MESSAGE_CONSTRAINTS);
        }
        return new SubjectLevel(trimmedSubjectLevel);
    }

    /**
     * Parses a {@code String dayTime}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code dayTime} is invalid.
     */
    public static DayTime parseDayTime(String dayTime) throws ParseException {
        requireNonNull(dayTime);
        String trimmedDayTime = dayTime.trim();
        // Basic split to provide clearer error messages: check day and time separately
        String[] parts = trimmedDayTime.split("\\s+", 2);
        if (parts.length != 2) {
            throw new ParseException(DayTime.MESSAGE_CONSTRAINTS);
        }

        String dayPart = parts[0];
        String timePart = parts[1];

        // If day is not a full weekday name, inform user explicitly
        if (!dayPart.matches("(?i)^(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday)$")) {
            throw new ParseException(MESSAGE_INVALID_DAY);
        }

        // Time must be exactly 4 digits (HHMM)
        if (!timePart.matches("\\d{4}")) {
            throw new ParseException(DayTime.MESSAGE_CONSTRAINTS);
        }

        int hour = Integer.parseInt(timePart.substring(0, 2));
        int minute = Integer.parseInt(timePart.substring(2));
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            throw new ParseException("Invalid time: '" + timePart + "' is not a valid 24-hour time (HHMM).");
        }

        return new DayTime(trimmedDayTime);
    }

    /**
     * Parses a {@code String day} into a valid day of the week.
     * Leading and trailing whitespaces will be removed from input
     *
     * @throws ParseException if the given {@code day} is not a valid day (Monday-Sunday).
     */
    public static String parseDay(String day) throws ParseException {
        requireNonNull(day);
        String trimmedDay = day.trim();

        if (!trimmedDay.matches("(?i)^(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday)$")) {
            throw new ParseException(MESSAGE_INVALID_DAY);
        }

        return trimmedDay;
    }

    /**
     * Parses a {@code String subjectLevel}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code cost} is invalid.
     */
    public static Cost parseCost(String cost) throws ParseException {
        requireNonNull(cost);
        String trimmedCost = cost.trim();
        // Provide clearer error messages for common failure modes
        if (Cost.isMissingDollar(trimmedCost)) {
            throw new ParseException(Cost.MESSAGE_MISSING_DOLLAR);
        }
        if (Cost.hasTooManyDecimalPlaces(trimmedCost)) {
            throw new ParseException(Cost.MESSAGE_TOO_MANY_DECIMALS);
        }
        if (!Cost.isValidCost(trimmedCost)) {
            throw new ParseException(Cost.MESSAGE_CONSTRAINTS);
        }

        return new Cost(trimmedCost);
    }

    /**
     * Parses a {@code String tag} into a {@code Tag}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code tag} is invalid.
     */
    public static Tag parseTag(String tag) throws ParseException {
        requireNonNull(tag);
        String trimmedTag = tag.trim();
        if (!Tag.isValidTagName(trimmedTag)) {
            throw new ParseException(Tag.MESSAGE_CONSTRAINTS);
        }
        return new Tag(trimmedTag);
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>}.
     */
    public static Set<Tag> parseTags(Collection<String> tags) throws ParseException {
        requireNonNull(tags);
        final Set<Tag> tagSet = new HashSet<>();
        for (String tagName : tags) {
            tagSet.add(parseTag(tagName));
        }
        return tagSet;
    }

    /**
     * Parses a {@code String lesson progress}.
     * Leading and trailing whitespaces will be trimmed.
     * Escape sequences like \n will be converted to actual newlines.
     */
    public static LessonProgress parseLessonProgress(String input) throws ParseException {
        requireNonNull(input);
        String[] parts = input.split("\\|", 2);

        if (parts.length < 2) {
            throw new ParseException(tutortrack.model.lesson.LessonProgress.MESSAGE_CONSTRAINTS);
        }

        String dateString = parts[0].trim();
        String progress = processEscapeSequences(parts[1].trim());

        if (progress.isEmpty() || progress.equals("null")) {
            throw new ParseException("Progress cannot be empty.");
        }

        LocalDate date = parseDate(dateString);
        return new LessonProgress(date, progress);

    }

    /**
     * Parses a {@code String lesson plan}.
     * Leading and trailing whitespaces will be trimmed.
     * Escape sequences like \n will be converted to actual newlines.
     */
    public static LessonPlan parseLessonPlan(String input) throws ParseException {
        requireNonNull(input);
        String[] parts = input.split("\\|", 2);

        if (parts.length < 2) {
            throw new ParseException(tutortrack.model.lesson.LessonPlan.MESSAGE_CONSTRAINTS);
        }

        String dateString = parts[0].trim();
        String plan = processEscapeSequences(parts[1].trim());
        if (plan.isEmpty()) {
            throw new ParseException("Plan cannot be empty.");
        }

        LocalDate date = parseDate(dateString);
        return new LessonPlan(date, plan);
    }

    /**
     * Converts escape sequences in the input string to their actual characters.
     * Supports: \n (newline), \t (tab), \\ (backslash)
     * To type literal \n or \t, use \\n or \\t
     *
     * @param input The string that may contain escape sequences
     * @return The string with escape sequences converted to actual characters
     */
    private static String processEscapeSequences(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == '\\' && i + 1 < input.length()) {
                char next = input.charAt(i + 1);
                if (next == 'n') {
                    result.append('\n');
                    i++;
                } else if (next == 't') {
                    result.append('\t');
                    i++;
                } else if (next == '\\') {
                    result.append('\\');
                    i++;
                } else {
                    result.append('\\');
                }
            } else {
                result.append(input.charAt(i));
            }
        }
        return result.toString();
    }
}

package tutortrack.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import tutortrack.commons.util.ToStringBuilder;
import tutortrack.logic.Messages;
import tutortrack.model.Model;
import tutortrack.model.person.Person;

/**
 * Finds and lists all persons in address book whose name or tags contain any of the argument keywords.
 * Keyword matching is case insensitive.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds all persons whose names or tags contain any of "
            + "the specified keywords (case-insensitive) and displays them as a list with index numbers.\n"
            + "Parameters: KEYWORD [MORE_KEYWORDS]... OR t/TAG_KEYWORD [MORE_TAG_KEYWORDS]...\n"
            + "Examples:\n"
            + COMMAND_WORD + " alice bob charlie (searches by name)\n"
            + COMMAND_WORD + " t/friend (searches by tag)";

    private final Predicate<Person> predicate;

    public FindCommand(Predicate<Person> predicate) {
        this.predicate = predicate;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);
        return new CommandResult(
                String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, model.getFilteredPersonList().size()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindCommand)) {
            return false;
        }

        FindCommand otherFindCommand = (FindCommand) other;
        return predicate.equals(otherFindCommand.predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}

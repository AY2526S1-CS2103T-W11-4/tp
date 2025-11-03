package tutortrack.logic.parser;

import static tutortrack.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static tutortrack.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static tutortrack.logic.parser.CliSyntax.PREFIX_COST;
import static tutortrack.logic.parser.CliSyntax.PREFIX_DAYTIME;
import static tutortrack.logic.parser.CliSyntax.PREFIX_NAME;
import static tutortrack.logic.parser.CliSyntax.PREFIX_NOK_CONTACT;
import static tutortrack.logic.parser.CliSyntax.PREFIX_SELF_CONTACT;
import static tutortrack.logic.parser.CliSyntax.PREFIX_SUBJECTLEVEL;
import static tutortrack.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import tutortrack.logic.commands.AddCommand;
import tutortrack.logic.parser.exceptions.ParseException;
import tutortrack.model.person.Address;
import tutortrack.model.person.Cost;
import tutortrack.model.person.DayTime;
import tutortrack.model.person.Name;
import tutortrack.model.person.Person;
import tutortrack.model.person.Phone;
import tutortrack.model.person.SubjectLevel;
import tutortrack.model.tag.Tag;

/**
 * Parses input arguments and creates a new AddCommand object
 */
public class AddCommandParser implements Parser<AddCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the AddCommand
     * and returns an AddCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format.
     */
    public AddCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_SELF_CONTACT, PREFIX_NOK_CONTACT,
                        PREFIX_SUBJECTLEVEL, PREFIX_DAYTIME, PREFIX_COST, PREFIX_ADDRESS, PREFIX_TAG);

        if (!arePrefixesPresent(argMultimap, PREFIX_NAME, PREFIX_SUBJECTLEVEL,
                PREFIX_DAYTIME, PREFIX_COST, PREFIX_ADDRESS)
                    || (!argMultimap.getValue(PREFIX_SELF_CONTACT).isPresent()
                                && !argMultimap.getValue(PREFIX_NOK_CONTACT).isPresent())
                    || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(
                PREFIX_NAME, PREFIX_SELF_CONTACT, PREFIX_NOK_CONTACT,
                PREFIX_SUBJECTLEVEL, PREFIX_DAYTIME, PREFIX_COST, PREFIX_ADDRESS);

        Name name = ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get());

        Optional<String> selfContactStr = argMultimap.getValue(PREFIX_SELF_CONTACT);
        Optional<String> nokContactStr = argMultimap.getValue(PREFIX_NOK_CONTACT);

        Phone selfContact = selfContactStr.isPresent() ? ParserUtil.parsePhone(selfContactStr.get()) : null;
        Phone nokContact = nokContactStr.isPresent() ? ParserUtil.parsePhone(nokContactStr.get()) : null;

        if (selfContact == null && nokContact == null) {
            throw new ParseException("At least one of self contact or NOK contact must be provided.");
        }
        if (selfContact != null && selfContact.equals(nokContact)) {
            throw new ParseException("Two contact numbers cannot be the same.");
        }
        SubjectLevel subjectLevel = ParserUtil.parseSubjectLevel(argMultimap.getValue(PREFIX_SUBJECTLEVEL).get());
        DayTime dayTime = ParserUtil.parseDayTime(argMultimap.getValue(PREFIX_DAYTIME).get());
        Cost cost = ParserUtil.parseCost(argMultimap.getValue(PREFIX_COST).get());
        Address address = ParserUtil.parseAddress(argMultimap.getValue(PREFIX_ADDRESS).get());
        Set<Tag> tagList = ParserUtil.parseTags(argMultimap.getAllValues(PREFIX_TAG));

        Person person = new Person(name, selfContact, nokContact,
                subjectLevel, dayTime, cost, address, tagList);

        return new AddCommand(person);
    }

    /**
     * Returns true if none of the prefixes contain empty {@code Optional} values in the given {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }
}

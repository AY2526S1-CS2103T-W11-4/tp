package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REMARK;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.RemarkCommand;

/*
 * Implemented with code given by the tutorial
 * Source: https://github.com/se-edu/addressbook-level3/commit/c423aae6e674e80461d21b1f6721556f817db892#diff-e6399ba0e95a3c101d6cfc7cab0eb7d487b7462ae1bf2dea7a1323b27c59f391
 */
public class RemarkCommandParserTest {
    private RemarkCommandParser parser = new RemarkCommandParser();
    private final String nonEmptyRemark = "Some remark.";

    @Test
    public void parse_indexSpecified_success() {
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + " " + PREFIX_REMARK + nonEmptyRemark;
        RemarkCommand expectedCommand = new RemarkCommand(INDEX_FIRST_PERSON, nonEmptyRemark);
        assertParseSuccess(parser, userInput, expectedCommand);

        userInput = targetIndex.getOneBased() + " " + PREFIX_REMARK;
        expectedCommand = new RemarkCommand(INDEX_FIRST_PERSON, "");
        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_missingCompulsoryField_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE);

        assertParseFailure(parser, RemarkCommand.COMMAND_WORD, expectedMessage);

        assertParseFailure(parser, RemarkCommand.COMMAND_WORD + " " + nonEmptyRemark, expectedMessage);
    }
}
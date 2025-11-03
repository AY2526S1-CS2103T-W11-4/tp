package tutortrack.logic.commands;

import static java.util.Objects.requireNonNull;
import static tutortrack.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static tutortrack.logic.parser.CliSyntax.PREFIX_COST;
import static tutortrack.logic.parser.CliSyntax.PREFIX_DAYTIME;
import static tutortrack.logic.parser.CliSyntax.PREFIX_NAME;
import static tutortrack.logic.parser.CliSyntax.PREFIX_NOK_CONTACT;
import static tutortrack.logic.parser.CliSyntax.PREFIX_SELF_CONTACT;
import static tutortrack.logic.parser.CliSyntax.PREFIX_SUBJECTLEVEL;
import static tutortrack.logic.parser.CliSyntax.PREFIX_TAG;
import static tutortrack.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import tutortrack.commons.core.index.Index;
import tutortrack.commons.util.CollectionUtil;
import tutortrack.commons.util.ToStringBuilder;
import tutortrack.logic.Messages;
import tutortrack.logic.commands.exceptions.CommandException;
import tutortrack.model.Model;
import tutortrack.model.person.Address;
import tutortrack.model.person.Cost;
import tutortrack.model.person.DayTime;
import tutortrack.model.person.Name;
import tutortrack.model.person.Person;
import tutortrack.model.person.Phone;
import tutortrack.model.person.SubjectLevel;
import tutortrack.model.tag.Tag;

/**
 * Edits the details of an existing person in the address book.
 */
public class EditCommand extends Command {

    public static final String COMMAND_WORD = "edit";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the details of the person identified "
            + "by the index number used in the displayed person list. "
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_NAME + "NAME] "
            + "[" + PREFIX_SELF_CONTACT + "SELF_CONTACT] "
            + "[" + PREFIX_NOK_CONTACT + "NOK_CONTACT] "
            + "[" + PREFIX_SUBJECTLEVEL + "SUBJECT_LEVEL] "
            + "[" + PREFIX_DAYTIME + "DAYTIME] "
            + "[" + PREFIX_COST + "COST] "
            + "[" + PREFIX_ADDRESS + "ADDRESS] "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_ADDRESS + "311, Clementi Ave 2, #02-25 ";

    public static final String MESSAGE_EDIT_PERSON_SUCCESS = "Edited Person: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_DUPLICATE_PERSON = "This person already exists in the address book.";

    private final Index index;
    private final EditPersonDescriptor editPersonDescriptor;

    /**
     * @param index of the person in the filtered person list to edit
     * @param editPersonDescriptor details to edit the person with
     */
    public EditCommand(Index index, EditPersonDescriptor editPersonDescriptor) {
        requireNonNull(index);
        requireNonNull(editPersonDescriptor);

        this.index = index;
        this.editPersonDescriptor = new EditPersonDescriptor(editPersonDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToEdit = lastShownList.get(index.getZeroBased());
        Person editedPerson;
        try {
            editedPerson = createEditedPerson(personToEdit, editPersonDescriptor);
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage());
        }

        if (!personToEdit.isSamePerson(editedPerson) && model.hasPerson(editedPerson)) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }

        model.setPerson(personToEdit, editedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(String.format(MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)));
    }

    /**
     * Creates and returns a {@code Person} with the details of {@code personToEdit}
     * edited with {@code editPersonDescriptor}.
     */
    private static Person createEditedPerson(Person personToEdit, EditPersonDescriptor editPersonDescriptor) {
        assert personToEdit != null;

        Name updatedName = editPersonDescriptor.getName().orElse(personToEdit.getName());
        Phone updatedSelfContact = editPersonDescriptor.getSelfContact().orElse(personToEdit.getSelfContact());
        Phone updatedNokContact = editPersonDescriptor.getNokContact().orElse(personToEdit.getNokContact());
        SubjectLevel updatedSubjectLevel = editPersonDescriptor.getSubjectLevel()
                                                   .orElse(personToEdit.getSubjectLevel());
        DayTime updatedDayTime = editPersonDescriptor.getDayTime().orElse(personToEdit.getDayTime());
        Cost updatedCost = editPersonDescriptor.getCost().orElse(personToEdit.getCost());
        Address updatedAddress = editPersonDescriptor.getAddress().orElse(personToEdit.getAddress());
        Set<Tag> updatedTags = editPersonDescriptor.getTags().orElse(personToEdit.getTags());

        if (updatedSelfContact != null && updatedSelfContact.equals(updatedNokContact)) {
            throw new IllegalArgumentException("Two contact numbers cannot be the same.");
        }
        Person editedPerson = new Person(updatedName, updatedSelfContact, updatedNokContact, updatedSubjectLevel,
                updatedDayTime, updatedCost, updatedAddress, updatedTags);

        // Preserve lesson progress and lesson plan from the original person.
        editedPerson.getLessonProgressList().addAll(personToEdit.getLessonProgressList());
        editedPerson.getLessonPlanList().addAll(personToEdit.getLessonPlanList());

        return editedPerson;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditCommand)) {
            return false;
        }

        EditCommand otherEditCommand = (EditCommand) other;
        return index.equals(otherEditCommand.index)
                && editPersonDescriptor.equals(otherEditCommand.editPersonDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("editPersonDescriptor", editPersonDescriptor)
                .toString();
    }

    /**
     * Stores the details to edit the person with. Each non-empty field value will replace the
     * corresponding field value of the person.
     */
    public static class EditPersonDescriptor {
        private Name name;
        private Phone selfContact;
        private Phone nokContact;
        private SubjectLevel subjectLevel;
        private DayTime dayTime;
        private Cost cost;
        private Address address;
        private Set<Tag> tags;

        public EditPersonDescriptor() {}

        /**
         * Copy constructor.
         * A defensive copy of {@code tags} is used internally.
         */
        public EditPersonDescriptor(EditPersonDescriptor toCopy) {
            setName(toCopy.name);
            setSelfContact(toCopy.selfContact);
            setNokContact(toCopy.nokContact);
            setSubjectLevel(toCopy.subjectLevel);
            setDayTime(toCopy.dayTime);
            setCost(toCopy.cost);
            setAddress(toCopy.address);
            setTags(toCopy.tags);
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(name, selfContact, nokContact, subjectLevel, dayTime, cost,
                    address, tags);
        }

        public void setName(Name name) {
            this.name = name;
        }

        public Optional<Name> getName() {
            return Optional.ofNullable(name);
        }

        public void setSelfContact(Phone selfContact) {
            this.selfContact = selfContact;
        }
        public Optional<Phone> getSelfContact() {
            return Optional.ofNullable(selfContact);
        }

        public void setNokContact(Phone nokContact) {
            this.nokContact = nokContact;
        }
        public Optional<Phone> getNokContact() {
            return Optional.ofNullable(nokContact);
        }

        public void setSubjectLevel(SubjectLevel subjectLevel) {
            this.subjectLevel = subjectLevel;
        }

        public Optional<SubjectLevel> getSubjectLevel() {
            return Optional.ofNullable(subjectLevel);
        }

        public void setDayTime(DayTime dayTime) {
            this.dayTime = dayTime;
        }

        public Optional<DayTime> getDayTime() {
            return Optional.ofNullable(dayTime);
        }

        public void setCost(Cost cost) {
            this.cost = cost;
        }

        public Optional<Cost> getCost() {
            return Optional.ofNullable(cost);
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public Optional<Address> getAddress() {
            return Optional.ofNullable(address);
        }

        /**
         * Sets {@code tags} to this object's {@code tags}.
         * A defensive copy of {@code tags} is used internally.
         */
        public void setTags(Set<Tag> tags) {
            this.tags = (tags != null) ? new HashSet<>(tags) : null;
        }

        /**
         * Returns an unmodifiable tag set, which throws {@code UnsupportedOperationException}
         * if modification is attempted.
         * Returns {@code Optional#empty()} if {@code tags} is null.
         */
        public Optional<Set<Tag>> getTags() {
            return (tags != null) ? Optional.of(Collections.unmodifiableSet(tags)) : Optional.empty();
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditPersonDescriptor)) {
                return false;
            }

            EditPersonDescriptor otherEditPersonDescriptor = (EditPersonDescriptor) other;
            return Objects.equals(name, otherEditPersonDescriptor.name)
                    && Objects.equals(selfContact, otherEditPersonDescriptor.selfContact)
                    && Objects.equals(nokContact, otherEditPersonDescriptor.nokContact)
                    && Objects.equals(subjectLevel, otherEditPersonDescriptor.subjectLevel)
                    && Objects.equals(dayTime, otherEditPersonDescriptor.dayTime)
                    && Objects.equals(cost, otherEditPersonDescriptor.cost)
                    && Objects.equals(address, otherEditPersonDescriptor.address)
                    && Objects.equals(tags, otherEditPersonDescriptor.tags);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("name", name)
                    .add("selfContact", selfContact)
                    .add("nokContact", nokContact)
                    .add("subjectLevel", subjectLevel)
                    .add("dayTime", dayTime)
                    .add("cost", cost)
                    .add("address", address)
                    .add("tags", tags)
                    .toString();
        }
    }
}

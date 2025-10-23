package tutortrack.model.person;

import static tutortrack.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tutortrack.commons.util.ToStringBuilder;
import tutortrack.model.lesson.LessonPlan;
import tutortrack.model.lesson.LessonProgress;
import tutortrack.model.tag.Tag;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Person {

    // Identity fields
    private final Name name;

    //Contact fields
    private final Phone selfContact;
    private final Phone nokContact;

    // Data fields
    private final SubjectLevel subjectLevel;
    private final DayTime dayTime;
    private final Cost cost;
    private final Address address;
    private final Set<Tag> tags = new HashSet<>();
    private final ObservableList<LessonProgress> lessonProgressList = FXCollections.observableArrayList();
    private final ObservableList<LessonPlan> lessonPlanList = FXCollections.observableArrayList();

    /**
     * All other fields and at least one of contacts must be present and not null.
     */
    public Person(Name name, Phone selfContact, Phone nokContact,
                  SubjectLevel subjectLevel, DayTime dayTime, Cost cost,
                  Address address, Set<Tag> tags) {
        requireAllNonNull(name, subjectLevel, dayTime, cost, address, tags);

        if (selfContact == null && nokContact == null) {
            throw new IllegalArgumentException("At least one of the contact should be provided.");
        }
        this.name = name;
        this.selfContact = selfContact;
        this.nokContact = nokContact;
        this.subjectLevel = subjectLevel;
        this.dayTime = dayTime;
        this.cost = cost;
        this.address = address;
        this.tags.addAll(tags);
    }

    public Name getName() {
        return name;
    }

    public Phone getSelfContact() {
        return selfContact;
    }

    public Phone getNokContact() {
        return nokContact;
    }

    public SubjectLevel getSubjectLevel() {
        return subjectLevel;
    }

    public DayTime getDayTime() {
        return dayTime;
    }

    public Cost getCost() {
        return cost;
    }

    public Address getAddress() {
        return address;
    }

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public void addLessonProgress(LessonProgress lp) {
        lessonProgressList.add(lp);
    }

    public void addLessonPlan(LessonPlan ll) {
        lessonPlanList.add(ll);
    }

    public ObservableList<LessonProgress> getLessonProgressList() {
        return lessonProgressList;
    }

    public ObservableList<LessonPlan> getLessonPlanList() {
        return lessonPlanList;
    }

    /**
     * Checks whether this person already has a lesson progress entry on the specified date.
     *
     * @param date the date to check for an existing lesson progress entry
     * @return {@code true} if there is a lesson progress with the same date, {@code false} otherwise
     */
    public boolean hasProgressOnDate(LocalDate date) {
        return lessonProgressList.stream()
                .anyMatch(lessonProgress -> lessonProgress.getDate().equals(date));
    }

    /**
     * Checks whether this person already has a lesson plan entry on the specified date.
     *
     * @param date the date to check for an existing lesson plan entry
     * @return {@code true} if there is a lesson plan with the same date, {@code false} otherwise
     */
    public boolean hasPlanOnDate(LocalDate date) {
        return lessonPlanList.stream()
                .anyMatch(lessonPlan -> lessonPlan.getDate().equals(date));
    }

    /**
     * Returns true if both persons have the same name.
     * This defines a weaker notion of equality between two persons.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && otherPerson.getName().equals(getName());
    }

    /**
     * Returns true if both persons have the same identity and data fields except cost.
     * This defines a stronger notion of equality between two persons.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Person)) {
            return false;
        }

        Person otherPerson = (Person) other;
        return name.equals(otherPerson.name)
                       && Objects.equals(selfContact, otherPerson.selfContact)
                       && Objects.equals(nokContact, otherPerson.nokContact)
                       && subjectLevel.equals(otherPerson.subjectLevel)
                       && dayTime.equals(otherPerson.dayTime)
                       && address.equals(otherPerson.address)
                       && tags.equals(otherPerson.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, selfContact, nokContact, subjectLevel, dayTime, cost, address, tags);
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
                .add("lesson plan", lessonPlanList)
                .add("lesson progress", lessonProgressList)
                .toString();
    }
}

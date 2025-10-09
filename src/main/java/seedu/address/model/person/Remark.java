package seedu.address.model.person;

public class Remark {
    public final String value;

    public Remark(String remark) {
        value = remark;
    }

    @Override
    public String toString() {
        return value;
    }
}

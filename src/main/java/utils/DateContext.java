package utils;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import java.time.LocalDate;

/**
 * DateContext - Global date context
 * Used to share the selected year and month across different UI components.
 */
public class DateContext {
    private static final IntegerProperty year = new SimpleIntegerProperty(LocalDate.now().getYear());
    private static final IntegerProperty month = new SimpleIntegerProperty(LocalDate.now().getMonthValue());

    public static void set(int y, int m) {
        year.set(y);
        month.set(m);
    }

    public static int getYear() {
        return year.get();
    }

    public static int getMonth() {
        return month.get();
    }

    public static IntegerProperty yearProperty() {
        return year;
    }

    public static IntegerProperty monthProperty() {
        return month;
    }

    public static LocalDate getDate() {
        return LocalDate.of(getYear(), getMonth(), 1);
    }
}

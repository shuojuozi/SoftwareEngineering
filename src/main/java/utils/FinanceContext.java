package utils;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import java.util.prefs.Preferences;

/** Globally shared: Total Assets / Savings Goal / Monthly Income */
public final class FinanceContext {
    private static final Preferences prefs = Preferences.userNodeForPackage(FinanceContext.class);

    private static final DoubleProperty totalAssets   = new SimpleDoubleProperty(prefs.getDouble("totalAssets", 10000));
    private static final DoubleProperty savingsGoal   = new SimpleDoubleProperty(prefs.getDouble("savingsGoal", 15000));
    private static final DoubleProperty monthlyIncome = new SimpleDoubleProperty(prefs.getDouble("monthlyIncome", 2500));

    private FinanceContext() {}

    /* Read/Write interface */
    public static double getTotalAssets()      { return totalAssets.get();   }
    public static double getSavingsGoal()      { return savingsGoal.get();   }
    public static double getMonthlyIncome()    { return monthlyIncome.get(); }

    public static void setTotalAssets(double v){ totalAssets.set(v); prefs.putDouble("totalAssets", v); }
    public static void setSavingsGoal(double v){ savingsGoal.set(v); prefs.putDouble("savingsGoal", v); }
    public static void setMonthlyIncome(double v){ monthlyIncome.set(v); prefs.putDouble("monthlyIncome", v); }

    /* For JavaFX bindings */
    public static DoubleProperty totalAssetsProperty()   { return totalAssets; }
    public static DoubleProperty savingsGoalProperty()   { return savingsGoal; }
    public static DoubleProperty monthlyIncomeProperty() { return monthlyIncome; }
}

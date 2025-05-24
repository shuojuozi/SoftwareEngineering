package utils;

import org.junit.jupiter.api.*;

import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FinanceContextTest {

    private static Preferences prefs;

    @BeforeAll
    public static void initPrefs() {
        prefs = Preferences.userNodeForPackage(FinanceContext.class);
    }

    @Test
    @Order(1)
    public void testInitialValues() {
        double totalAssets = FinanceContext.getTotalAssets();
        double savingsGoal = FinanceContext.getSavingsGoal();
        double monthlyIncome = FinanceContext.getMonthlyIncome();

        assertTrue(totalAssets >= 0, "Total Assets should be initialized");
        assertTrue(savingsGoal >= 0, "Savings Goal should be initialized");
        assertTrue(monthlyIncome >= 0, "Monthly Income should be initialized");
    }

    @Test
    @Order(2)
    public void testSetAndGetTotalAssets() {
        double testValue = 12345.67;
        FinanceContext.setTotalAssets(testValue);
        assertEquals(testValue, FinanceContext.getTotalAssets(), 0.001);
        assertEquals(testValue, prefs.getDouble("totalAssets", -1), 0.001);
    }

    @Test
    @Order(3)
    public void testSetAndGetSavingsGoal() {
        double testValue = 54321.00;
        FinanceContext.setSavingsGoal(testValue);
        assertEquals(testValue, FinanceContext.getSavingsGoal(), 0.001);
        assertEquals(testValue, prefs.getDouble("savingsGoal", -1), 0.001);
    }

    @Test
    @Order(4)
    public void testSetAndGetMonthlyIncome() {
        double testValue = 8888.88;
        FinanceContext.setMonthlyIncome(testValue);
        assertEquals(testValue, FinanceContext.getMonthlyIncome(), 0.001);
        assertEquals(testValue, prefs.getDouble("monthlyIncome", -1), 0.001);
    }

    @AfterAll
    public static void cleanUp() {
        // Optional: clean up test artifacts to avoid affecting user settings
        prefs.remove("totalAssets");
        prefs.remove("savingsGoal");
        prefs.remove("monthlyIncome");
    }
}

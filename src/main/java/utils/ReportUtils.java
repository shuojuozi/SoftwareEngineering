package utils;

import com.lowagie.text.pdf.BaseFont;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.xhtmlrenderer.pdf.ITextRenderer;
import pojo.Transaction;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ReportUtils {
    /** Update this to the actual path of a valid Chinese font on your system (Windows/macOS/Linux) */
    private static final String FONT_PATH = "SoftwareEngineering-main (1)\\SoftwareEngineering-main\\src\\main\\resources\\data\\msyh.ttc";

    /**
     * Preset Markdown report prompt template
     */
    private static final String REPORT_PROMPT = ""
            + "You are a professional personal finance analyst.\n"
            + "Given the following user financial profile, generate a detailed report in Markdown format with these sections:\n"
            + "1. **Financial Overview**: Summarize total assets, savings goal, monthly income, and current month's expenditure.\n"
            + "2. **Income vs. Expenses Trend**: Comment on the balance and any notable trends.\n"
            + "3. **Spending Breakdown**: List each spending category with absolute amounts and percentage of total.\n"
            + "4. **Personalized Recommendations**: Provide concrete tips to optimize spending and reach savings goals.\n\n"
            + "User Financial Profile:\n"
            + "%s\n"
            + "Please output the entire report in valid Markdown without any additional commentary.";

    /**
     * Generate a financial report and export it as a PDF file
     */
    public static void askAndExportPdf(String sessionId, File pdfFile) throws Exception {
        // 1️⃣ Build financial context and generate prompt
        String context = buildFinancialContext();
        String prompt = String.format(REPORT_PROMPT, context);
        String markdown = DeepSeek.chat(sessionId, prompt);

        // 2️⃣ Convert Markdown to HTML
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String bodyHtml = renderer.render(document);

        // Wrap into full HTML structure
        String html = generateCompleteHtml(bodyHtml);

        // 3️⃣ Render to PDF
        renderPdf(html, pdfFile);
    }

    /**
     * Build financial context string based on current transactions
     */
    private static String buildFinancialContext() {
        int year = DateContext.getYear();
        int month = DateContext.getMonth();
        List<Transaction> transactions = JsonUtils.getTransactionsByMonth(year, month);

        double totalIncome = 0.0;
        double totalExpense = 0.0;
        Map<String, Double> categorySum = new TreeMap<>();

        for (Transaction t : transactions) {
            double amount = t.getAmount();
            String type = t.getTransactionType().toLowerCase();

            if ("收入".equals(t.getIncExp())) {
                totalIncome += amount;
            } else {
                totalExpense += amount;
                categorySum.merge(type, amount, Double::sum);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Period: %d-%02d\n", year, month));
        sb.append(String.format("Total Income: ¥%.2f\n", totalIncome));
        sb.append(String.format("Total Expense: ¥%.2f\n", totalExpense));
        sb.append(String.format("Net Balance: ¥%.2f\n\n", totalIncome - totalExpense));

        sb.append("Spending by Category:\n");
        for (Map.Entry<String, Double> entry : categorySum.entrySet()) {
            sb.append(String.format("- %s: ¥%.2f\n", entry.getKey(), entry.getValue()));
        }

        return sb.toString();
    }

    /**
     * Wrap HTML body with proper XHTML headers and basic styling
     */
    private static String generateCompleteHtml(String bodyHtml) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE html>\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\"/>\n" +
                "  <style>\n" +
                "    body { font-family: sans-serif; margin: 20px; }\n" +
                "    pre { background: #f5f5f5; padding: 10px; }\n" +
                "    code { background: #eee; padding: 2px 4px; }\n" +
                "    h1, h2, h3 { color: #114d9d; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                bodyHtml + "\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * Render XHTML string to a styled PDF with Chinese font support
     */
    private static void renderPdf(String html, File pdfFile) throws Exception {
        try (OutputStream os = new FileOutputStream(pdfFile)) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.getFontResolver().addFont(
                    FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED
            );
            renderer.setDocumentFromString(html, null);
            renderer.layout();
            renderer.createPDF(os, false);
            renderer.finishPDF();
        }
    }

    /**
     * Backward-compatible method with manual prompt
     */
    public static void askAndExportPdf(String sessionId, String question, File pdfFile) throws Exception {
        String markdown = DeepSeek.chat(sessionId, question);

        // Convert to HTML
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String bodyHtml = renderer.render(document);

        // Embed HTML with basic style and UTF-8 font
        String html = "<html><head>"
                + "<meta charset=\"UTF-8\"/>"
                + "<style>body{font-family:Arial, sans-serif; padding:20px;} "
                + "pre{background:#f5f5f5; padding:10px;} "
                + "code{background:#eee; padding:2px 4px;} "
                + "</style>"
                + "</head><body>"
                + bodyHtml
                + "</body></html>";

        // Render to PDF with font support (for Chinese)
        try (OutputStream os = new FileOutputStream(pdfFile)) {
            ITextRenderer rendererPdf = new ITextRenderer();
            rendererPdf.getFontResolver().addFont(
                    "/System/Library/Fonts/STHeiti Medium.ttc", // macOS sample, update to C:/Windows/Fonts/simsun.ttc on Windows
                    BaseFont.IDENTITY_H, BaseFont.EMBEDDED
            );
            rendererPdf.setDocumentFromString(html);
            rendererPdf.layout();
            rendererPdf.createPDF(os);
        }
    }
}

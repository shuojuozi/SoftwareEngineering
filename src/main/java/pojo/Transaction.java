package pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    private String transactionTime;    // Transaction time
    private String transactionType;    // Transaction type
    private String counterparty;       // Counterparty
    private String item;               // Item name
    private String incExp;             // Income/Expense
    private double amount;             // Amount
    private String paymentMethod;      // Payment method
    private String status;             // Current status
    private String transactionId;      // Transaction ID
    private String merchantId;         // Merchant order number
    private String note;               // Note or comment
}

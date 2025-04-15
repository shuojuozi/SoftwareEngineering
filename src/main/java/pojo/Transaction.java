package pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    private String transactionTime;    // 交易时间
    private String transactionType;    // 交易类型
    private String counterparty;       // 交易对方
    private String item;               // 商品
    private String incExp;             // 收/支
    private double amount;             // 金额
    private String paymentMethod;      // 支付方式
    private String status;             // 当前状态
    private String transactionId;      // 交易单号
    private String merchantId;         // 商户单号
    private String note;               // 备注
}

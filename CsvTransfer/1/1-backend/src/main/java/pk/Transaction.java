package pk;

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

    // Jackson 反序列化需要无参构造
    public Transaction() {
    }

    /**
     * 全参构造，可根据需要选择是否全部包含
     */
    public Transaction(String transactionTime,
                       String transactionType,
                       String counterparty,
                       String item,
                       String incExp,
                       double amount,
                       String paymentMethod,
                       String status,
                       String transactionId,
                       String merchantId,
                       String note) {
        this.transactionTime = transactionTime;
        this.transactionType = transactionType;
        this.counterparty = counterparty;
        this.item = item;
        this.incExp = incExp;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.transactionId = transactionId;
        this.merchantId = merchantId;
        this.note = note;
    }

    // Getter 与 Setter

    public String getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(String transactionTime) {
        this.transactionTime = transactionTime;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(String counterparty) {
        this.counterparty = counterparty;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getIncExp() {
        return incExp;
    }

    public void setIncExp(String incExp) {
        this.incExp = incExp;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

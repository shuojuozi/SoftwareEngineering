# 💰 Smart Budget Assistant

A personal finance dashboard that integrates visual analytics with AI-powered transaction classification. This project contains **two versions**:

- 🧪 Version 1: UI and backend are separate.
- ✅ Version 2: First integrated test with a unified entry point and partial service wiring.

---

## 📌 Version 1 – UI / Backend Separated

> *Standalone front-end UI with no real data connection.*  
> Backend tools like AI classification and analytics are implemented but **not yet connected**.

### 🚀 Features

- JavaFX UI: Dashboard, Trade, Transactions, Budget, Analysis
- Each page extends `Application` and runs independently
- Static charts and hardcoded data for visualization
- Backend utilities under `utils/` (not yet used in UI)
    - `JsonUtils`, `CalcExpense`, `DeepSeek`, `ConfigUtil`
- Two empty placeholders: `SearchBudget.java`, `UserInputHandler.java`

### 🏗 Directory Layout (v1)

```
src/
└── main/
    ├── java/
    │   ├── Ui/
    │   │   ├── DashBoardUi.java
    │   │   ├── BudgetUi.java
    │   │   ├── ClassifiedUi.java
    │   │   ├── TradeUi.java
    │   │   ├── TransactionUi.java
    │   │   ├── AnalysisUi.java
    │   │   └── NavigationSuper.java
    │   ├── utils/
    │   │   ├── CalcExpense.java
    │   │   ├── JsonUtils.java
    │   │   ├── DeepSeek.java
    │   │   ├── ConfigUtil.java
    │   │   ├── SearchBudget.java
    │   │   └── UserInputHandler.java
    │   ├── pojo/
    │   │   └── Transaction.java
    │   └── App.java
    └── resources/
        └── data/
            ├── csv/
            └── transactionData.json
```

### ▶️ Operation result (v1)

![image-20250420223145006](version_picture/1.png)

![image-20250420223222013](version_picture/2.png)

![image-20250420223247349](version_picture/3.png)

![image-20250420223302742](version_picture/4.png)

![image-20250420223352480](version_picture/5.png)

![image-20250420223415053](version_picture/6.png)

---

## ✅ Version 2 – First Integrated Version

> *UI + backend merged into one executable with real data and basic service calls.*

### ✨ What's New

- Single entry point: `App.java` (only one `launch()` call)
- UI receives real-time data from `utils/`
    - Expense stats from `CalcExpense`
    - Category predictions from `DeepSeek`
- CSV Import → JSON merge → Auto-classification from UI
- WIP: `SearchBudget` and `UserInputHandler` have basic logic

### 🏗 Directory Layout (v2)

```
src/
└── main/
    ├── java/
    │   ├── app/App.java
    │   ├── ui/...
    │   ├── utils/...
    │   ├── pojo/...
    │   └── service/...
    └── resources/
        ├── config.properties
        └── data/
            ├── csv/
            └── transactionData.json
```

### ▶️ Operation result (v2)

```bash

```

### 


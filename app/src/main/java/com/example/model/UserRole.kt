package com.example.model

enum class UserRole(
    val title: String,
    val department: String,
    val defaultInitial: String,
    val defaultName: String,
    val canSignBudget: Boolean,
    val canSignAccounting: Boolean,
    val canCreateVoucher: Boolean,
    val canAudit: Boolean
) {
    ADMIN_AUDITOR(
        title = "COA / Internal Auditor",
        department = "Commission on Audit",
        defaultInitial = "COA-Auditor",
        defaultName = "Atty. Elena Reyes",
        canSignBudget = true,
        canSignAccounting = true,
        canCreateVoucher = true,
        canAudit = true
    ),
    BUDGET_OFFICER(
        title = "Budget Officer / Clerk",
        department = "Budget Section",
        defaultInitial = "BO-Santos",
        defaultName = "Ricardo Santos",
        canSignBudget = true,
        canSignAccounting = false,
        canCreateVoucher = true,
        canAudit = false
    ),
    ACCOUNTANT(
        title = "Accountant / Processing Staff",
        department = "Accounting Section",
        defaultInitial = "ACC-Mendoza",
        defaultName = "Maria Mendoza, CPA",
        canSignBudget = false,
        canSignAccounting = true,
        canCreateVoucher = true,
        canAudit = false
    ),
    CLAIMANT(
        title = "Claimant / Payee",
        department = "COS Personnel / Employee",
        defaultInitial = "JLG",
        defaultName = "Joyce L. Galea",
        canSignBudget = false,
        canSignAccounting = false,
        canCreateVoucher = true,
        canAudit = false
    )
}

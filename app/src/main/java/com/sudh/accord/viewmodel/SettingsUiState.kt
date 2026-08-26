package com.sudh.accord.viewmodel

data class SettingsUiState(
    val name: String = "",
    val email: String = "",
    val isLoadingProfile: Boolean = true,
    val profileError: String? = null,

    val budgetInput: String = "",
    val isSavingBudget: Boolean = false,
    val budgetSaveError: String? = null,
    val budgetSaved: Boolean = false,

    val walletPressureEnabled: Boolean = true,
    val sundayNarrativeEnabled: Boolean = true,
    val taskRemindersEnabled: Boolean = true,
)

sealed class SettingsEvent {
    object SignedOut : SettingsEvent()
}
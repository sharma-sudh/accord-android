package com.sudh.accord.dto

/**
 * Backing DTO for GET /api/v1/users/me — a "fetch my own profile" endpoint
 * that doesn't exist on the backend yet (only PATCH /api/v1/users does).
 * Add the matching Spring controller method/service call before this will
 * resolve to anything but a 404. monthlyBudget rides along here too so
 * SettingsSheet can pre-fill the budget field from the same round trip
 * instead of a second call.
 */
data class UserProfileDto(
    val name: String,
    val email: String,
    val monthlyBudget: Double
)
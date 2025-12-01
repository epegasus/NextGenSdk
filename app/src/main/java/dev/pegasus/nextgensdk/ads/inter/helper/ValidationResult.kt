package dev.pegasus.nextgensdk.ads.inter.helper

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val reason: String) : ValidationResult()
}
package co.edu.uniquindio.entorno.core.component

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import co.edu.uniquindio.entorno.R
import co.edu.uniquindio.entorno.core.util.AppLanguage
import co.edu.uniquindio.entorno.domain.model.Badge
import co.edu.uniquindio.entorno.domain.model.ReportCategory
import co.edu.uniquindio.entorno.domain.model.ReportStatus
import co.edu.uniquindio.entorno.domain.model.Severity
import co.edu.uniquindio.entorno.domain.model.ThemeMode
import co.edu.uniquindio.entorno.domain.model.UserLevel

/*
 * Traducción de los enums del dominio a texto visible. Los enums conservan su `label` en español
 * (no se tocan), pero la UI siempre pasa por aquí para respetar el idioma elegido.
 */

@StringRes
fun ReportStatus.citizenLabelRes(): Int = when (this) {
    ReportStatus.PENDING -> R.string.status_in_review
    ReportStatus.VERIFIED -> R.string.status_verified
    ReportStatus.REJECTED -> R.string.status_rejected
    ReportStatus.RESOLVED -> R.string.status_resolved
}

@StringRes
fun ReportStatus.moderatorLabelRes(): Int = when (this) {
    ReportStatus.PENDING -> R.string.status_pending
    else -> citizenLabelRes()
}

@Composable
fun ReportStatus.labelForCitizen(): String = stringResource(citizenLabelRes())

@Composable
fun ReportStatus.labelForModerator(): String = stringResource(moderatorLabelRes())

@StringRes
fun Severity.labelRes(): Int = when (this) {
    Severity.LOW -> R.string.severity_low
    Severity.MEDIUM -> R.string.severity_medium
    Severity.HIGH, Severity.CRITICAL -> R.string.severity_high
}

@Composable
fun Severity.label(): String = stringResource(labelRes())

@StringRes
fun ReportCategory.labelRes(): Int = when (this) {
    ReportCategory.SECURITY -> R.string.category_security
    ReportCategory.MEDICAL -> R.string.category_medical
    ReportCategory.INFRASTRUCTURE -> R.string.category_infrastructure
    ReportCategory.PETS -> R.string.category_pets
    ReportCategory.COMMUNITY -> R.string.category_community
}

@Composable
fun ReportCategory.localizedLabel(): String = stringResource(labelRes())

@StringRes
fun UserLevel.labelRes(): Int = when (this) {
    UserLevel.NOVICE -> R.string.level_novice
    UserLevel.COLLABORATOR -> R.string.level_collaborator
    UserLevel.GUARDIAN -> R.string.level_guardian
    UserLevel.COMMUNITY_HERO -> R.string.level_community_hero
}

@Composable
fun UserLevel.localizedLabel(): String = stringResource(labelRes())

@StringRes
fun Badge.labelRes(): Int = when (this) {
    Badge.FIRST_REPORT -> R.string.badge_first_report
    Badge.FIRST_COMMENT -> R.string.badge_first_comment
    Badge.TEN_VERIFIED_REPORTS -> R.string.badge_trusted_citizen
    Badge.TOP_REPORT_OF_MONTH -> R.string.badge_top_of_month
}

@StringRes
fun Badge.descriptionRes(): Int = when (this) {
    Badge.FIRST_REPORT -> R.string.badge_first_report_desc
    Badge.FIRST_COMMENT -> R.string.badge_first_comment_desc
    Badge.TEN_VERIFIED_REPORTS -> R.string.badge_trusted_citizen_desc
    Badge.TOP_REPORT_OF_MONTH -> R.string.badge_top_of_month_desc
}

@Composable
fun Badge.localizedLabel(): String = stringResource(labelRes())

@Composable
fun Badge.localizedDescription(): String = stringResource(descriptionRes())

@StringRes
fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.SYSTEM -> R.string.theme_system
    ThemeMode.LIGHT -> R.string.theme_light
    ThemeMode.DARK -> R.string.theme_dark
}

@StringRes
fun AppLanguage.labelRes(): Int = when (this) {
    AppLanguage.SYSTEM -> R.string.language_system
    AppLanguage.SPANISH -> R.string.language_spanish
    AppLanguage.ENGLISH -> R.string.language_english
}

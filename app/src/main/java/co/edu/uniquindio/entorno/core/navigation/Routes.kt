package co.edu.uniquindio.entorno.core.navigation

object Routes {
    // ---- Autenticación (sin sesión)
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val TERMS = "terms"
    const val PRIVACY = "privacy"

    // ---- App principal (con sesión)
    const val HOME = "home"
    const val MAP_BASE = "map"
    const val MAP = "map?focus={focus}"
    const val MY_REPORTS = "my_reports"
    const val PROFILE = "profile"
    const val MODERATOR_PANEL = "moderator_panel"

    const val REPORT_DETAIL = "report_detail/{reportId}"
    const val COMMENTS = "comments/{reportId}"
    const val CREATE_REPORT = "create_report"
    const val PICK_LOCATION = "pick_location"
    const val NOTIFICATIONS = "notifications"
    const val EDIT_PROFILE = "edit_profile"
    const val SETTINGS = "settings"
    const val CHANGE_PASSWORD = "change_password"
    const val HEROES = "heroes"
    const val STATS = "stats"
    const val BADGES = "badges"
    const val HEAT_MAP = "heat_map"
    const val REVIEW_REPORT = "review_report/{reportId}"

    fun reportDetail(reportId: String) = "report_detail/$reportId"
    fun comments(reportId: String) = "comments/$reportId"
    fun reviewReport(reportId: String) = "review_report/$reportId"
    fun map(focusReportId: String? = null) =
        if (focusReportId == null) MAP_BASE else "$MAP_BASE?focus=$focusReportId"
}

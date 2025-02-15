package uk.me.danielharman.kotlinspringbot.models.admin.enums

enum class Role {
    Primary,
    Global,
    InspectAdmin,
    ManageAdmin,
    Logging, // Send logs in PM (startup, restarting etc)
}

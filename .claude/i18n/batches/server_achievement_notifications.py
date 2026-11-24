TITLE = "vpin-studio-server: achievement notifications"
BASE = "vpin-studio-server/src/main/java/de/mephisto/vpin/server"
BUNDLE = "vpin-studio-server/src/main/resources/messages"
IMPORT = "import de.mephisto.vpin.server.util.ServerMessages;"
CALL = "ServerMessages."
ESCAPE_UNICODE = True

# Java code was changed by hand (AchievementsService.notifyPlayer), this batch only adds the keys.
# columns: key, en, de, es, fr, it, pt  (all values go through MessageFormat)
NEW_KEYS = [
  ("achievements.notification.unlocked_one", "{0}: Achievement Unlocked", "{0}: Achievement freigeschaltet", "{0}: Logro desbloqueado", "{0} : Succès débloqué", "{0}: Obiettivo sbloccato", "{0}: Conquista desbloqueada"),
  ("achievements.notification.unlocked_many", "{0}: {1} Achievements Unlocked", "{0}: {1} Achievements freigeschaltet", "{0}: {1} logros desbloqueados", "{0} : {1} succès débloqués", "{0}: {1} obiettivi sbloccati", "{0}: {1} conquistas desbloqueadas"),
  ("achievements.notification.more", "{0} +{1} More", "{0} +{1} weitere", "{0} +{1} más", "{0} +{1} de plus", "{0} +{1} altri", "{0} +{1} mais"),
  ("achievements.notification.badge", "{0}: Badge Unlocked", "{0}: Abzeichen freigeschaltet", "{0}: Insignia desbloqueada", "{0} : Badge débloqué", "{0}: Badge sbloccato", "{0}: Emblema desbloqueado"),
  ("achievements.notification.level_up", "{0}: Level Up!", "{0}: Levelaufstieg!", "{0}: ¡Subes de nivel!", "{0} : Niveau supérieur !", "{0}: Livello superiore!", "{0}: Subiu de nível!"),
  ("achievements.notification.level", "Level {0} – {1}", "Level {0} – {1}", "Nivel {0} – {1}", "Niveau {0} – {1}", "Livello {0} – {1}", "Nível {0} – {1}"),
]

R = []

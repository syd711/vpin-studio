TITLE = "vpin-studio-commons (fx, pausemenu, widgets)"
BASE = "vpin-studio-commons/src/main"

J = "java/de/mephisto/vpin/commons/"
F = "resources/de/mephisto/vpin/commons/"

NEW_KEYS = [
  ("dialog.close_and_lose_changes", "Do you really want to close and lose them?", "Möchten Sie wirklich schließen und die Änderungen verwerfen?", "¿Realmente desea cerrar y perderlos?", "Voulez-vous vraiment fermer et les perdre ?", "Vuoi davvero chiudere e perderle?", "Pretende mesmo fechar e perdê-las?"),
  ("pausemenu.menu.loading_menu_for", 'Loading Menu for "{0}"', 'Lade Menü für "{0}"', 'Cargando menú para "{0}"', 'Chargement du menu pour "{0}"', 'Caricamento menu per "{0}"', 'A carregar o menu para "{0}"'),
  ("pausemenu.wovp_menu_item.player_button", "{0}. Player: {1}", "{0}. Spieler: {1}", "{0}. Jugador: {1}", "{0}. Joueur : {1}", "{0}. Giocatore: {1}", "{0}. Jogador: {1}"),
  ("pausemenu.menu_submitter_view.enter_additional_message", "Enter additional message here.", "Zusätzliche Nachricht hier eingeben.", "Introduzca aquí un mensaje adicional.", "Saisissez un message supplémentaire ici.", "Inserisci qui un messaggio aggiuntivo.", "Introduza aqui uma mensagem adicional."),
  ("widgets.widget_competition.first_place", "#1 Place", "Platz 1", "Puesto n.º 1", "1re place", "1° posto", "1.º lugar"),
  ("widgets.widget_competition.remaining_days", "Remaining Days", "Verbleibende Tage", "Días restantes", "Jours restants", "Giorni rimanenti", "Dias restantes"),
  ("widgets.widget_competition.remaining_hours", "Remaining Hours", "Verbleibende Stunden", "Horas restantes", "Heures restantes", "Ore rimanenti", "Horas restantes"),
  ("widgets.widget_competition.remaining_minutes", "Remaining Minutes", "Verbleibende Minuten", "Minutos restantes", "Minutes restantes", "Minuti rimanenti", "Minutos restantes"),
  ("widgets.widget_weekly_competition_score_item.score_not_approved", "Score has not been approved yet.", "Der Score wurde noch nicht bestätigt.", "La puntuación aún no ha sido aprobada.", "Le score n'a pas encore été validé.", "Il punteggio non è ancora stato approvato.", "A pontuação ainda não foi aprovada."),
]

R = [
  (J + "fx/DialogHeaderController.java",
   'WidgetFactory.showConfirmation(stage, "You have unsaved changes.", "Do you really want to close and lose them ?")',
   'WidgetFactory.showConfirmation(stage, Messages.get("dialog.you_have_unsaved_changes"), Messages.get("dialog.close_and_lose_changes"))'),
  (J + "fx/pausemenu/MenuController.java",
   'return "Loading Menu for \\"" + game.getGameDisplayName() + "\\"";',
   'return Messages.get("pausemenu.menu.loading_menu_for", game.getGameDisplayName());'),
  (J + "fx/pausemenu/WovpMenuItemController.java",
   'playerBtn.setText((playerSelectionIndex + 1) + ". Player: " + wovpPlayer.getName());',
   'playerBtn.setText(Messages.get("pausemenu.wovp_menu_item.player_button", playerSelectionIndex + 1, wovpPlayer.getName()));'),
  (F + "fx/pausemenu/menu-submitter-view.fxml",
   'promptText="Enter additional message here."',
   'promptText="%pausemenu.menu_submitter_view.enter_additional_message"'),
  (J + "fx/widgets/WidgetCompetitionController.java", '.title("#1 Place")', '.title(Messages.get("widgets.widget_competition.first_place"))'),
  (J + "fx/widgets/WidgetCompetitionController.java", '.text("#1 Place")', '.text(Messages.get("widgets.widget_competition.first_place"))'),
  (J + "fx/widgets/WidgetCompetitionController.java", 'turnoverTile.setTitle("#1 Place");', 'turnoverTile.setTitle(Messages.get("widgets.widget_competition.first_place"));'),
  (J + "fx/widgets/WidgetCompetitionController.java", '.title("Remaining Days")', '.title(Messages.get("widgets.widget_competition.remaining_days"))'),
  (J + "fx/widgets/WidgetCompetitionController.java", 'countdownTile.setTitle("Remaining Days");', 'countdownTile.setTitle(Messages.get("widgets.widget_competition.remaining_days"));'),
  (J + "fx/widgets/WidgetCompetitionController.java", 'countdownTile.setTitle("Remaining Hours");', 'countdownTile.setTitle(Messages.get("widgets.widget_competition.remaining_hours"));'),
  (J + "fx/widgets/WidgetCompetitionController.java", 'countdownTile.setTitle("Remaining Minutes");', 'countdownTile.setTitle(Messages.get("widgets.widget_competition.remaining_minutes"));'),
  (J + "fx/widgets/WidgetWeeklyCompetitionScoreItemController.java",
   'String tt = "Score has not been approved yet.";',
   'String tt = Messages.get("widgets.widget_weekly_competition_score_item.score_not_approved");'),
]

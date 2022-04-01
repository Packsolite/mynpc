package de.packsolite.mynpc;

public class Texts {

	/**
	 * Permissions
	 */
	public static final String PERMISSION_USE = "mynpc.use";
	public static final String PERMISSION_ADMIN = "mynpc.edit";
	public static final String PERMISSION_LIMIT = "mynpc.limit.";
	public static final String PERMISSION_EMOTE_PREFIX = "mynpc.emote.";

	/**
	 * Chat messages
	 */
	public static final String PREFIX = "§8[§3§lMy§b§lNpc§8] §7";
	public static final String INVENTORY_PREFIX = "§3MyNpc §7| ";
	public static final String ITEM_PREFIX = "§8» ";
	public static final String HELP_INVALID_COMMAND = "Nutze §b/mynpc help §7für eine Liste von Befehlen!";
	public static final String HELP_HEADER = " §bMyNpc §7by §3VaroXCraft§7\n" + " §7Version: §f" + MyNpc.getInstance()
			.getDescription()
			.getVersion() + "\n \n" + " §7Befehle:\n";
	public static final String HELP_BODY_COMMAND = "  §8• §b%s §f%s";
	public static final String MULTILINE_SEPERATOR = "§8§l▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪\n";
	public static final String NO_PERMISSION = "§cDu hast nicht genügend Rechte.";
	public static final String NPC_NAME_INVALID = "§cDieser Name ist zu kurz, zu lang oder enthält ungültige Zeichen!";
	public static final String NPC_RANAMED = "Du hast den NPC §b%w §7umbenannt zu §b%s§7.";
	public static final String TYPE_MOVE_AGAIN = "Laufe jetzt zu der gewünschten Position und führe §b/mynpc move §7erneut aus!";
	public static final String WRONG_ARGS = "§7Syntax: §b%s";
	public static final String NOT_YOUR_NPC = "§cDieser NPC gehört dir nicht!";
	public static final String NPC_HEADER = "§b♦ §7NPC von §3%s §b♦";
	public static final String NPC_NOT_FOUND = "§cDiesen NPC gibt es nicht.";
	public static final String NPC_TOO_CLOSE = "§cDu befindest dich zu dicht an einem anderen NPC.";
	public static final String CANT_PLACE_HERE = "§cDu kannst hier keinen NPC setzen!";
	public static final String NO_NPC_NEARBY = "§cEs befindet sich kein NPC in deiner Nähe.";
	public static final String NPC_LINE_INVALID = "§cDer Text ist zu lang!";
	public static final String NPC_ALREADY_EXISTS = "§cDieser NPC existiert bereits.";
	public static final String NPC_LIMIT_REACHED = "§cDu hast dein Limit an NPCs erreicht.";
	public static final String NPC_CREATED = "Du hast einen neuen NPC erstellt.";
	public static final String NPC_DELETED = "Du hast den NPC gelöscht.";
	public static final String NPC_MOVED = "Du hast den NPC verschoben.";
	public static final String NPC_LINE = "Du hast die §b%z. §7Zeile §7editiert.";
	public static final String TELEPORTED = "Du hast dich zum NPC teleportiert.";
	public static final String LIST_PAGE_NOT_FOUND = "§cDiese Seite gibt es nicht!";
	public static final String TASK_CANCELED = "§cDer Vorgang wurde abgebrochen!";
	public static final String PLAYER_NOT_FOUND = "§cDieser Spieler wurde nicht gefunden.";
	public static final String CONFIRM_DELETE_IN_CHAT = "§cBestätige den Löschvorgang indem du §econfirm §cin den Chat schreibst!";
	public static final String CONFIRM_DELETE_IN_CHAT_TITLE = "§cNPC löschen?";
	public static final String CONFIRM_DELETE_IN_CHAT_SUBTITLE = "§cSchreibe §econfirm §czum bestätigen!";
	public static final String ENTER_LINE_IN_CHAT_TITLE = "§aText eingeben";
	public static final String ENTER_LINE_IN_CHAT_SUBTITLE = "Gib den Text im Chat ein!";
	public static final String ENTER_LINE_IN_CHAT = "Bitte schreibe den Text für Zeile §b%z §7in den Chat!";
	public static final String NPC_SKIN_SET = "Du hast den Skin auf §b%s §7gesetzt.";
	public static final String ENTER_SKIN_TO_CHAT_TITLE = "§aNamen eingeben";
	public static final String ENTER_SKIN_TO_CHAT_SUBTITLE = "Gib den Skinname im Chat ein!";
	public static final String ENTER_SKIN_TO_CHAT = "Bitte schreibe den Spieler dessen Skin geladen werden soll in den chat!";
	public static final String ENTER_NEW_NAME_TO_CHAT_SUBTITLE = "Gib den neuen Namen im Chat ein!";
	public static final String ENTER_NEW_NAME_TO_CHAT = "Bitte schreibe den neuen Namen in den Chat!";
	public static final String ENTER_TEXT_TO_CHAT_TITLE = "§aText eingeben";
	public static final String ENTER_TEXT_TO_CHAT_SUBTITLE = "Gib den Text im Chat ein";
	public static final String ENTER_TEXT_TO_CHAT = "Bitte schreibe den Rechtsklick-Text in den Chat! (\".\" zum deaktivieren)";
	public static final String NPC_TEXT_SET = "Der Rechtsklick-Text wurde gesetzt!";
	public static final String NPC_TEXT_DISABLED = "Der Rechtsklick-Text wurde deaktiviert!";
	public static final String NPC_SNEAK_SET = "Du hast das Sneaken auf %s §7gesetzt.";
	public static final String NPC_FOLLOW_HEAD_SET = "Du hast das Anschauen von Spielern auf %s §7gesetzt.";
	public static final String NPC_EMOTE_SET = "Du hast den Emote %s §7ausgewählt.";
	public static final String NPC_EMOTE_RESET = "Du hast den Emote zurückgesetzt.";
	public static final String SKIN_NOT_FOUND = "§cDieser Skin wurde nicht gefunden.";
	public static final String SHOW_CREATOR_DISABLED = "Der Name des Besitzers wird nicht mehr angezeigt.";
	public static final String SHOW_CREATOR_ENABLED = "§aDer Name des Besitzers wird jetzt angezeigt.";
	public static final String EMOTE_NOT_OWNED = "§cDu hast diesen Emote nicht freigeschaltet.";
	public static final String EMOTE_NOT_FOUND = "§cKein Emote mit diesem Namen gefunden.";
	public static final String NOT_A_NUMBER = "§cDas ist keine Zahl!";
	public static final String ONLY_FOR_PLAYERS = "§cDieser Befehl ist nur für Spieler.";
}
package de.packsolite.mynpc.util;

import org.bukkit.ChatColor;

import de.packsolite.mynpc.Texts;

public enum LabymodEmote {

	NONE("Kein", 0, EmoteCategory.GAMER), BACKFLIP("Backflip", 2, EmoteCategory.VIBE), DAB("Dab", 3, EmoteCategory.VIBE), HELLO("Hallo", 4, EmoteCategory.GAMER),
	BOWTHANKS("Verbeugen", 5, EmoteCategory.VIBE), HYPE("Hype", 6, EmoteCategory.VIBE), TRYINGTOFLY("Versuchen zu fliegen", 7, EmoteCategory.VIBE), ZOMBIE("Zombie", 11, EmoteCategory.CHROME),
	HULA_HOOP("Hula Hoop", 13, EmoteCategory.CHROME), CALLING("Anrufen", 14, EmoteCategory.CHROME), FACEPALM("Facepalm", 15, EmoteCategory.VIBE),
	BRUSHYOURSCHOULDER("Schulterklopfer", 18, EmoteCategory.VIBE), SPLIT("Spagat", 19, EmoteCategory.SUPREME), SALUTE("Salute", 20, EmoteCategory.CHROME),
	BALARINA("Balarina", 22, EmoteCategory.SUPREME), HANDSTAND("Handstand", 31, EmoteCategory.CHROME), HELICOPTER("Helikopter", 32, EmoteCategory.VIBE), HOLY("Heilig", 33, EmoteCategory.REAPER),
	WAVEOVER("Herüberwinken", 34, EmoteCategory.REAPER), DEEPER("Tiefer, tiefer", 36, EmoteCategory.SUPREME), KARATE("Karate", 37, EmoteCategory.REAPER),
	MOONWALK("Moonwalk", 38, EmoteCategory.REAPER), FREEZING("Frieren", 40, EmoteCategory.CHROME), JUBILATION("Jubeln", 41, EmoteCategory.SUPREME), TURTLE("Schildkröte", 43, EmoteCategory.REAPER),
	HEADSPIN("Headspin", 45, EmoteCategory.SUPREME), INFINITYDAB("Infinity Dab", 46, EmoteCategory.REAPER), CHICKEN("Huhn", 47, EmoteCategory.SUPREME), FLOSS("The Floss", 49, EmoteCategory.CHROME),
	THEMEGATHRUST("Der Mega-Schub", 50, EmoteCategory.VIBE), CLEANER("Reinigen", 51, EmoteCategory.SUPREME), BRIDGE("Brücke", 52, EmoteCategory.VIBE),
	MILKTHECOW("Milk the cow", 53, EmoteCategory.CHROME), RURIK("Rurik", 54, EmoteCategory.VIBE), WAVE("Welle", 55, EmoteCategory.CHROME), MONEYRAIN("Geldregen", 57, EmoteCategory.REAPER),
	THEPOINTER("Zeigen", 59, EmoteCategory.SUPREME), FRIGHTENING("Oh nein", 60, EmoteCategory.REAPER), SAD("Traurig", 61, EmoteCategory.CHROME), AIRGUITAR("Luftgitarre", 62, EmoteCategory.REAPER),
	WITCH("Umrühren", 63, EmoteCategory.SUPREME), LEFT("Links", 69, EmoteCategory.GAMER), RIGHT("Rechts", 70, EmoteCategory.GAMER), BUHHH("Buuuh", 74, EmoteCategory.CHROME),
	SPITTINGBARS("Stinkt", 75, EmoteCategory.SUPREME), COUNTMONEY("Geld zählen", 76, EmoteCategory.REAPER), HUG("Umarmung", 77, EmoteCategory.VIBE), APPLAUSE("Applaus", 78, EmoteCategory.SUPREME),
	BOXING("Boxing", 79, EmoteCategory.CHROME), SHOOT("Schießen", 83, EmoteCategory.SUPREME), THEPOINTINGMAN("Der zeigende Mann", 84, EmoteCategory.VIBE), HEART("Herz", 85, EmoteCategory.REAPER),
	NEARTHEFALL("Knapp", 86, EmoteCategory.VIBE), WAITING("Warten", 89, EmoteCategory.SUPREME), PRAISEYOURITEM("Item hochhalten", 92, EmoteCategory.REAPER), LOOK("Zeigen", 93, EmoteCategory.GAMER),
	ILOVEYOU("Ich liebe dich", 97, EmoteCategory.CHROME), SARCASTICCLAP("Langsames Klatschen", 98, EmoteCategory.SUPREME), YOU("Du", 101, EmoteCategory.CHROME),
	HEADONTHEWALL("Kopf an die Wand", 105, EmoteCategory.VIBE), BALENCE("Balance", 112, EmoteCategory.SUPREME), LEVELUP("Level Up", 113, EmoteCategory.REAPER),
	TAKETHEL("Nimm das 'L'", 114, EmoteCategory.REAPER), MYIDOL("Mein Idol", 121, EmoteCategory.SUPREME), AIRPLANE("Flugzeug", 122, EmoteCategory.VIBE), EAGLE("Adler", 124, EmoteCategory.CHROME),
	JOBWELLDONE("Gute Arbeit", 126, EmoteCategory.VIBE), ELEPHANT("Elefant", 128, EmoteCategory.SUPREME), PRESENT("Geschenk", 130, EmoteCategory.REAPER),
	EYESONYOU("Auge auf dich", 131, EmoteCategory.VIBE), BOWDOWN("Verbeugen", 133, EmoteCategory.SUPREME), MANEKINEKO("Maneki-neko", 134, EmoteCategory.CHROME),
	CONDUCTOR("Dirigent", 135, EmoteCategory.SUPREME), DIDCHALLENGE("Herausforderung", 136, EmoteCategory.REAPER), SNOWANGEL("Schneeengel", 137, EmoteCategory.SUPREME),
	SPRINKLER("Sprinkler", 139, EmoteCategory.CHROME), CALCULATED("Berechnet", 140, EmoteCategory.VIBE), ONEARMEDHANDSTAND("Einarmiger Handstand", 141, EmoteCategory.REAPER),
	EAT("Essen", 142, EmoteCategory.SUPREME), SHY("Schüchtern", 143, EmoteCategory.VIBE), SITUPS("Sit-Ups", 145, EmoteCategory.REAPER), BREAKDANCE("Breakdance", 146, EmoteCategory.SUPREME),
	MINDBLOW("Mindblowing", 148, EmoteCategory.CHROME), FALL("Fallen", 149, EmoteCategory.VIBE), TPOSE("T-pose", 150, EmoteCategory.CHROME), JUMPINGJACK("Jumping Jack", 153, EmoteCategory.SUPREME),
	BACKSTROKE("Rückenschwimmen", 154, EmoteCategory.VIBE), ICEHOCKEY("Eishockey", 156, EmoteCategory.SUPREME), LOOKATTHEFIREWORKS("Feuerwerk", 157, EmoteCategory.VIBE),
	FINISHTHETREE("Baum umschubsen", 158, EmoteCategory.CHROME), ICESKATING("Eislaufen", 159, EmoteCategory.REAPER), FANCYFEET("Tanzfüße", 161, EmoteCategory.VIBE),
	RONALDO("Ronaldo", 162, EmoteCategory.REAPER), TREUHEART("Wahres Herz", 163, EmoteCategory.SUPREME), PUMPERNICKEL("Pumpernickel", 164, EmoteCategory.VIBE),
	BABYSHART("Baby Shark", 166, EmoteCategory.REAPER), OPENPRESENT("Geschenk öffnen", 167, EmoteCategory.REAPER), DJ("Dj", 170, EmoteCategory.SUPREME),
	HAVETOPEE("Ich muss mal", 171, EmoteCategory.CHROME), SNEEZE("Gesundheit", 173, EmoteCategory.CHROME), CHEERLEADER("Cheerleader", 178, EmoteCategory.SUPREME),
	NARUTORUN("Naruto Run", 180, EmoteCategory.REAPER), PATIPATU("Pati Patu", 181, EmoteCategory.VIBE), AXESWING("Axt schwingen", 182, EmoteCategory.VIBE),
	FUNSIONLEFT("Fusion Left", 183, EmoteCategory.CHROME), FISHING("Angeln", 184, EmoteCategory.VIBE), FUSIONRIGHT("Fusion Right", 185, EmoteCategory.CHROME),
	BREATHLESS("Aus der Puste", 187, EmoteCategory.SUPREME), SINGER("Singen", 191, EmoteCategory.REAPER), MAGIKARP("Karpador", 192, EmoteCategory.SUPREME), RAGE("Rage", 193, EmoteCategory.VIBE),
	SLAP("Backpfeife", 194, EmoteCategory.VIBE), AIRKISSES("Luftkuss", 195, EmoteCategory.REAPER), KNOCKOUT("Knockout", 196, EmoteCategory.CHROME), MATRIX("Matrix", 197, EmoteCategory.REAPER),
	JETPACK("Jetpack", 198, EmoteCategory.SUPREME), GOLF("Golf", 200, EmoteCategory.VIBE), STADIUMWAVE("Teamarbeit", 201, EmoteCategory.SUPREME), KICKBOXER("Kickboxer", 202, EmoteCategory.CHROME),
	HANDSHAKE("Händedruck", 203, EmoteCategory.CHROME), CLEANINGTHEFLOOR("Boden putzen", 204, EmoteCategory.SUPREME);

	public static LabymodEmote getById(int id) {
		for (LabymodEmote emote : values()) {
			if (id == emote.getId()) {
				return emote;
			}
		}
		return NONE;
	}

	public static LabymodEmote getByName(String name) {
		name = name.replace(" ", "");
		for (LabymodEmote emote : values()) {
			if (emote.getName()
					.replace(" ", "")
					.equalsIgnoreCase(name) || emote.name.equalsIgnoreCase(name)) {
				return emote;
			}
		}
		return null;
	}

	private int id;
	private String name;
	private EmoteCategory categroy;

	private LabymodEmote(String name, int id, EmoteCategory categroy) {
		this.name = name;
		this.id = id;
		this.categroy = categroy;
	}

	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return ChatColor.getLastColors(categroy.getName()) + this.name;
	}

	public int getId() {
		return id;
	}

	public EmoteCategory getCategroy() {
		return categroy;
	}

	public String getPermission() {
		return this.categroy.getPermission();
	}

	public static enum EmoteCategory {
		GAMER("§7Gamer"), VIBE("§6Vibe"), CHROME("§aChrome"), SUPREME("§cSupreme"), REAPER("§eReaper");

		private String name;

		EmoteCategory(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public String getPermission() {
			return Texts.PERMISSION_EMOTE_PREFIX + this.name()
					.toLowerCase();
		}
	}
}
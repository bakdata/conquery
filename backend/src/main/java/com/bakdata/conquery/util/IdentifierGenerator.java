package com.bakdata.conquery.util;

import com.google.common.math.IntMath;

import lombok.experimental.UtilityClass;

@UtilityClass
public class IdentifierGenerator {

	public static String createIdentifier(Object obj) {
		return createIdentifier(obj.hashCode());
	}

	public static String createIdentifier(int hash) {
		int version = IntMath.mod(hash, COMBINATIONS);
		return ADJECTIVES[IntMath.mod(version, ADJECTIVES.length)] + " " + CREATURES[version / ADJECTIVES.length];
	}

	private static final String[] CREATURES = {
		"Banshee",
		"Basilisk",
		"Bigfoot",
		"Black Dog",
		"Bogeyman",
		"Bogle",
		"Browny",
		"Centaur",
		"Cerberus",
		"Charybdis",
		"Chimera",
		"Cockatrice",
		"Cyclops",
		"Demon",
		"Doppelganger",
		"Dragon",
		"Dwarf",
		"Echidna",
		"Elf",
		"Fairy",
		"Ghost",
		"Gnome",
		"Goblin",
		"Golem",
		"Gorgon",
		"Griffin",
		"Grim Reaper",
		"Hobgoblin",
		"Hydra",
		"Imp",
		"Leprechaun",
		"Manticore",
		"Medusa",
		"Mermaid",
		"Minotaur",
		"Mothman",
		"Mutant",
		"Nymph",
		"Ogre",
		"Pegasus",
		"Phoenix",
		"Pixie",
		"Sasquatch",
		"Satyr",
		"Scylla",
		"Shapeshifter",
		"Siren",
		"Sphinx",
		"Sprite",
		"Sylph",
		"Thunderbird",
		"Unicorn",
		"Valkyrie",
		"Vampire",
		"Wendigo",
		"Will-o'-wisp",
		"Werewolf",
		"Wraith",
		"Zombie" };

	private static final String[] ADJECTIVES = {
		"able",
		"abnormal",
		"absent-minded",
		"above average",
		"adventurous",
		"affectionate",
		"agile",
		"agreeable",
		"alert",
		"amazing",
		"ambitious",
		"amiable",
		"amusing",
		"analytical",
		"angelic",
		"apathetic",
		"apprehensive",
		"ardent",
		"artificial",
		"artistic",
		"assertive",
		"attentive",
		"average",
		"awesome",
		"awful",
		"balanced",
		"beautiful",
		"below average",
		"beneficent",
		"blue",
		"blunt",
		"boisterous",
		"brave",
		"bright",
		"brilliant",
		"buff",
		"callous",
		"candid",
		"cantankerous",
		"capable",
		"careful",
		"careless",
		"caustic",
		"cautious",
		"charming",
		"childish",
		"childlike",
		"cheerful",
		"chic",
		"churlish",
		"circumspect",
		"civil",
		"clean",
		"clever",
		"clumsy",
		"coherent",
		"cold",
		"competent",
		"composed",
		"conceited",
		"condescending",
		"confident",
		"confused",
		"conscientious",
		"considerate",
		"content",
		"cool",
		"cool-headed",
		"cooperative",
		"cordial",
		"courageous",
		"cowardly",
		"crabby",
		"crafty",
		"cranky",
		"crass",
		"critical",
		"cruel",
		"curious",
		"cynical",
		"dainty",
		"decisive",
		"deep",
		"deferential",
		"deft",
		"delicate",
		"demonic",
		"dependent",
		"delightful",
		"demure",
		"depressed",
		"devoted",
		"dextrous",
		"diligent",
		"direct",
		"dirty",
		"disagreeable",
		"discerning",
		"discreet",
		"disruptive",
		"distant",
		"distraught",
		"distrustful",
		"dowdy",
		"dramatic",
		"dreary",
		"drowsy",
		"drugged",
		"drunk",
		"dull",
		"dutiful",
		"eager",
		"earnest",
		"easy-going",
		"efficient",
		"egotistical",
		"elfin",
		"emotional",
		"energetic",
		"enterprising",
		"enthusiastic",
		"evasive",
		"even-tempered",
		"exacting",
		"excellent",
		"excitable",
		"experienced",
		"fabulous",
		"fastidious",
		"ferocious",
		"fervent",
		"fiery",
		"flabby",
		"flaky",
		"flashy",
		"frank",
		"friendly",
		"funny",
		"fussy",
		"generous",
		"gentle",
		"gloomy",
		"glutinous",
		"good",
		"grave",
		"great",
		"groggy",
		"grouchy",
		"guarded",
		"hateful",
		"hearty",
		"helpful",
		"hesitant",
		"hot-headed",
		"hypercritical",
		"hysterical",
		"idiotic",
		"idle",
		"illogical",
		"imaginative",
		"immature",
		"immodest",
		"impatient",
		"imperturbable",
		"impetuous",
		"impractical",
		"impressionable",
		"impressive",
		"impulsive",
		"inactive",
		"incisive",
		"incompetent",
		"inconsiderate",
		"inconsistent",
		"independent",
		"indiscreet",
		"indolent",
		"indefatigable",
		"industrious",
		"inexperienced",
		"insensitive",
		"inspiring",
		"intelligent",
		"interesting",
		"intolerant",
		"inventive",
		"irascible",
		"irritable",
		"irritating",
		"jocular",
		"jovial",
		"joyous",
		"judgmental",
		"keen",
		"kind",
		"lame",
		"lazy",
		"lean",
		"leery",
		"lethargic",
		"level-headed",
		"listless",
		"lithe",
		"lively",
		"local",
		"logical",
		"long-winded",
		"lovable",
		"love-lorn",
		"lovely",
		"maternal",
		"mature",
		"mean",
		"meddlesome",
		"mercurial",
		"methodical",
		"meticulous",
		"mild",
		"miserable",
		"modest",
		"moronic",
		"morose",
		"motivated",
		"musical",
		"naive",
		"nasty",
		"natural",
		"naughty",
		"negative",
		"nervous",
		"noisy",
		"normal",
		"nosy",
		"numb",
		"obliging",
		"obnoxious",
		"old-fashioned",
		"one-sided",
		"orderly",
		"ostentatious",
		"outgoing",
		"outspoken",
		"passionate",
		"passive",
		"paternal",
		"paternalistic",
		"patient",
		"peaceful",
		"peevish",
		"pensive",
		"persevering",
		"persnickety",
		"petulant",
		"picky",
		"plain",
		"plain-speaking",
		"playful",
		"pleasant",
		"plucky",
		"polite",
		"popular",
		"positive",
		"powerful",
		"practical",
		"prejudiced",
		"pretty",
		"proficient",
		"proud",
		"provocative",
		"prudent",
		"punctual",
		"quarrelsome",
		"querulous",
		"quick",
		"quick-tempered",
		"quiet",
		"realistic",
		"reassuring",
		"reclusive",
		"reliable",
		"reluctant",
		"resentful",
		"reserved",
		"resigned",
		"resourceful",
		"respected",
		"respectful",
		"responsible",
		"restless",
		"revered",
		"ridiculous",
		"sad",
		"sassy",
		"saucy",
		"sedate",
		"self-assured",
		"selfish",
		"sensible",
		"sensitive",
		"sentimental",
		"serene",
		"serious",
		"sharp",
		"short-tempered",
		"shrewd",
		"shy",
		"silly",
		"sincere",
		"sleepy",
		"slight",
		"sloppy",
		"slothful",
		"slovenly",
		"slow",
		"smart",
		"snazzy",
		"sneering",
		"snobby",
		"somber",
		"sober",
		"sophisticated",
		"soulful",
		"soulless",
		"sour",
		"spirited",
		"spiteful",
		"stable",
		"staid",
		"steady",
		"stern",
		"stoic",
		"striking",
		"strong",
		"stupid",
		"sturdy",
		"subtle",
		"sullen",
		"sulky",
		"supercilious",
		"superficial",
		"surly",
		"suspicious",
		"sweet",
		"tactful",
		"tactless",
		"talented",
		"testy",
		"thinking",
		"thoughtful",
		"thoughtless",
		"timid",
		"tired",
		"tolerant",
		"touchy",
		"tranquil",
		"ugly",
		"unaffected",
		"unbalanced",
		"uncertain",
		"uncooperative",
		"undependable",
		"unemotional",
		"unfriendly",
		"unguarded",
		"unhelpful",
		"unimaginative",
		"unmotivated",
		"unpleasant",
		"unpopular",
		"unreliable",
		"unsophisticated",
		"unstable",
		"unsure",
		"unthinking",
		"unwilling",
		"venal",
		"versatile",
		"vigilant",
		"warm",
		"warmhearted",
		"wary",
		"watchful",
		"weak",
		"well-behaved",
		"well-developed",
		"well-intentioned",
		"well-respected",
		"well-rounded",
		"willing",
		"wonderful",
		"volcanic",
		"vulnerable",
		"zealous" };

	private static final int COMBINATIONS = ADJECTIVES.length * CREATURES.length;
}
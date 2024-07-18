/*
	Custom DMD screen script that shows informations on the selected game (more or less the same than PinballY) using custom medias (animated company logo, table title screen):
	
	- Shows image/video from the 'dmds/manufacturers' subfolder (hardcoded in the script)
	- Shows image/video from the 'dmds/titles' subfolder if they match the media name of the table
	- Shows highscores
	- Shows statistics
	- Can check for PinballY updates and display if any on the main screen (disabled by default)

	TODO:
	- Missing animated logo for original tables
	- Move to FlexDMD API instead of UltraDMD when PinballY will fully marshall COM objects, and add more fancy animations

	CREDITS:
	- original script by vbousquet
	- images by: fr33styler: https://www.dietle.de/projekt-vpin-virtueller-flipper/
	- slight adjustments by GSadventure
*/

// Check for a new version of PinballY on each launch (default is false to limit the load on PinballY's servers)
let checkPinballYUpdate = false;

// If true, use the table rom as the game name, like VPinMame does. This allow to have the same styling of the DMD
// as in game but it also needs to release/create DMD after each table change which may lead to delay or stuttering.
// This is the reason why this option is false by default.
let useTableRom = false


// Check for new release of PinballY (taken from http://mjrnet.org/pinscape/downloads/PinballY/Help/UpdateCheckExample.html)
if (checkPinballYUpdate) {
	let request = new HttpRequest();
	request.open("GET", "http://mjrnet.org/pinscape/downloads/PinballY/VersionHistory.txt", true);
	request.send().then(reply =>
	{
		if (/^(\d\d)-(\d\d)-(\d\d\d\d) \(\d+\.\d+\.\d+ .+\)$/mi.test(reply))
		{
			let mm = +RegExp.$1 - 1, dd = +RegExp.$2, yyyy = +RegExp.$3;
			let onlineDate = new Date(Date.UTC(yyyy, mm, dd));
			if (onlineDate > systemInfo.version.buildDate)
				mainWindow.statusLines.upper.add("A new version of PinballY is available!");
		}
	}).catch(error => {
		logfile.log(
			"The Javascript version update checker ran into a problem!\nJavascript error: %s\nStack:\n%s",
			error.message, error.stack);
	});
}

// For debugging purposes
function getMethods(obj) {
  var result = [];
  for (var id in obj) {
    try {
      result.push(id + ": " + obj[id].toString() + " / frozen=" + Object.isFrozen(obj[id]) + " / sealed=" + Object.isSealed(obj[id]) + " / type=" + typeof(obj[id]));
    } catch (err) {
      result.push(id + ": inaccessible");
    }
  }
  return result;
}

Number.prototype.toHHMMSS = function () {
    var sec_num = this;
    var hours   = Math.floor(sec_num / 3600);
    var minutes = Math.floor((sec_num - (hours * 3600)) / 60);
    var seconds = sec_num - (hours * 3600) - (minutes * 60);
    if (hours   < 10) {hours   = "0"+hours;}
    if (minutes < 10) {minutes = "0"+minutes;}
    if (seconds < 10) {seconds = "0"+seconds;}
    return hours+':'+minutes+':'+seconds;
}

Number.prototype.toDDHHMMSS = function () {
    var sec_num = this;
    var days   = Math.floor(sec_num / 86400);
    var hours   = Math.floor((sec_num - (days * 86400))/ 3600);
    var minutes = Math.floor((sec_num - (days * 86400) - (hours * 3600)) / 60);
    var seconds = sec_num - (days * 86400) - (hours * 3600) - (minutes * 60);
    if (hours   < 10) {hours   = "0"+hours;}
    if (minutes < 10) {minutes = "0"+minutes;}
    if (seconds < 10) {seconds = "0"+seconds;}
    return days+"d "+hours+':'+minutes+':'+seconds;
}

// Play a video, without looping, adapting to the actual length of the video
function queueVideo(filename, transitionIn, transitionOut, transitionMargin) {
	if (filename.endsWith(".gif")) {
		let video = dmd.NewVideo(String(filename), String(filename));
		let id = udmd.RegisterVideo(2, false, filename);
		udmd.DisplayScene00(id.toString(), "", 15, "", 15, transitionIn, video.Length * 1000 - transitionMargin, transitionOut);
	} else {
		udmd.DisplayScene00(filename, "", 15, "", 15, transitionIn, 5000 - transitionMargin, transitionOut);
	}
}

// Handle DMD updates
let dmd = null;
let udmd = null;
let hiscores = {};
let info = null;
let shownInfo = null;
let loopCount = 0;
let fso = createAutomationObject("Scripting.FileSystemObject");
let updater;
let manufacturers = {
	"Aliens vs Pinball": ["./Scripts/dmds/manufacturers/Aliens vs Pinball.gif"],
	"Bally": ["./Scripts/dmds/manufacturers/bally.gif"],
	"Bethesda Pinball": ["./Scripts/dmds/manufacturers/Bethesda Pinball.gif"],
	"Capcom": ["./Scripts/dmds/manufacturers/capcom.gif"],
	"Data East": ["./Scripts/dmds/manufacturers/dataeast-1.gif", "./Scripts/dmds/manufacturers/dataeast-2.gif"],
	"Foxnext Games": ["./Scripts/dmds/manufacturers/Foxnext Games.gif"],
	"Gottlieb": ["./Scripts/dmds/manufacturers/gottlieb.gif"],
	"Jurassic Pinball": ["./Scripts/dmds/manufacturers/Jurassic Pinball.gif"],
	"Marvel": ["./Scripts/dmds/manufacturers/Marvel.gif"],
	"Midway": ["./Scripts/dmds/manufacturers/bally.gif"],
	"Premier": ["./Scripts/dmds/manufacturers/premier.gif"],
	"Rowamet": ["./Scripts/dmds/manufacturers/Rowamet.gif"],	
	"Sega": ["./Scripts/dmds/manufacturers/sega.gif"],
	"Spooky": ["./Scripts/dmds/manufacturers/Spooky.gif"],
	"Star Wars Pinball": ["./Scripts/dmds/manufacturers/Star Wars Pinball.gif"],
	"Stern": ["./Scripts/dmds/manufacturers/stern.gif"],
	"Taito": ["./Scripts/dmds/manufacturers/Taito.gif"],
	"The Walking Dead": ["./Scripts/dmds/manufacturers/The Walking Dead.gif"],
	"Universal Pinball": ["./Scripts/dmds/manufacturers/Universal Pinball.gif"],
	"Williams": ["./Scripts/dmds/manufacturers/williams.gif"],
	"WilliamsFX3Pinball": ["./Scripts/dmds/manufacturers/williams.gif"],
	"VPX": ["./Scripts/dmds/manufacturers/VPX.gif"],
	"VALVe": ["./Scripts/dmds/manufacturers/VALVe.gif"],
	"Zaccaria": ["./Scripts/dmds/manufacturers/Zaccaria.gif"],
	"Zen Studios": ["./Scripts/dmds/manufacturers/Zen Studios.gif"]
}
// logfile.log(getMethods(dmd).join("\n"));
function TestMarshalling() {
	dmd.LockRenderThread();
	let video = dmd.NewVideo("Manufacturer", "./Scripts/dmds/manufacturers/bally.gif");
	logfile.log(getMethods(video).join("\n"));
	// This will fail due to a marshalling problem
	dmd.Stage.AddActor(video);
	dmd.UnlockRenderThread();
}
function UpdateDMD() {
	if (updater !== undefined) clearTimeout(updater);
	updater = undefined;

	if (dmd == null) {
		dmd = createAutomationObject("FlexDMD.FlexDMD");
		dmd.GameName = "PinballY";
		dmd.RenderMode = 1; // 0 = Gray 4 shades, 1 = Gray 16 shades, 2 = Full color
		dmd.Width = 128;
		dmd.Height = 32;
		dmd.Show = true;
		dmd.Run = true;
		udmd = dmd.NewUltraDMD();
	}
	
	if (dmd.Run == false) return;

	if (info == null) return;

	if (udmd.IsRendering() && shownInfo != null && info.id == shownInfo.id) {
		// Add a timeout later for when the render queue will be finished
		updater = setTimeout(UpdateDMD, 1000);
		return;
	}
	
	dmd.LockRenderThread();

	if (shownInfo == null || info.id != shownInfo.id) {
		loopCount = 0;
		shownInfo = info;
	} else {
		loopCount++;
	}			

	udmd.CancelRendering();

	// This will reopen the DMD with the right ROM name, allowing for ROM customization in dmddevice.ini
	if (useTableRom && loopCount == 0) {
		let rom = info.resolveROM();
		logfile.log("> Update DMD for:");
		logfile.log("> rom: '".concat(rom.vpmRom, "'"));
		logfile.log("> manufacturer:", info.manufacturer);
		logfile.log("> title:", info.title);
		logfile.log("> year:", info.year);
		logfile.log("> Table type: ", info.tableType);
		logfile.log("> Highscore style: ", info.highScoreStyle);
		if (rom.vpmRom == null) {
			dmd.GameName = "";
		} else {
			dmd.GameName = rom.vpmRom.toString();
		}
	}
	
	// Manufacturer
    let transitionMargin = (20 * 1000) / 60;
    //little workaround for special character in Williams "TM" Pinball Problem from FX3
    let manufacturer_temp = info.manufacturer;
    // If its Williams and it has more than 8 chars
    if (manufacturer_temp != null && (manufacturer_temp.substr(0,8) == "Williams") && (manufacturer_temp.length > 8)){
        manufacturer_temp = "WilliamsFX3Pinball";
    }
    if (manufacturer_temp in manufacturers) {
        var medias = manufacturers[manufacturer_temp];
        var media = medias[Math.floor(Math.random() * medias.length)];
        queueVideo(media, 10, 8, transitionMargin);
    } else if (info.manufacturer !== undefined) {
        udmd.DisplayScene00("FlexDMD.Resources.dmds.black.png", info.manufacturer, 15, "", 15, 10, 3000, 8);
    }
	
	// Title
	var hasTitle = false;
	if (info.mediaName != null) {
		var extensions = [".gif", ".avi", ".png"];
		for (var i = 0; i < extensions.length; i++) {
			if (fso.FileExists("./Scripts/dmds/titles/" + info.mediaName + extensions[i])) {
				queueVideo("./Scripts/dmds/titles/" + info.mediaName + extensions[i], 0, 8, transitionMargin);
				hasTitle = true;
				break;
			}
		}
	}
	if (!hasTitle) {
		var name = info.title.trim();
		var subname = "";
		if (name.indexOf('(') != -1) {
			var sep = info.title.indexOf('(');
			name = info.title.slice(0, sep - 1).trim();
		}
		if (name.length >= 16) {
			var split = 16;
			for (var i = 15; i > 0; i--) {
				if (name.charCodeAt(i) == 32) {
					subname = name.slice(i).trim();
					name = name.slice(0, i).trim();
					break;
				}
			}
		}
		udmd.DisplayScene00("FlexDMD.Resources.dmds.black.png", name, 15, subname, 15, 0, 5000, 8);
	}

	// Stats
	if (info.rating >= 0)
		udmd.DisplayScene00("FlexDMD.Resources.dmds.black.png", "Played " + info.playCount + " Rating " + info.rating, 15, "Play time: " + info.playTime.toHHMMSS(), 15, 10, 3000, 8);
	else
		udmd.DisplayScene00("FlexDMD.Resources.dmds.black.png", "Played " + info.playCount + " times", 15, "Playtime " + info.playTime.toHHMMSS(), 15, 10, 3000, 8);

	// Insert Coin (every 4 loops)
	if (((loopCount + 0) & 3) == 0) {
		queueVideo("./Scripts/dmds/misc/insertcoin.gif", 10, 14, 0);
		udmd.DisplayScene00("./Scripts/dmds/misc/insertcoin.gif", "", 15, "", 15, 10, 1399, 14);
		udmd.DisplayScene00("./Scripts/dmds/misc/insertcoin.gif", "", 15, "", 15, 14, 1399, 14);
	}

	// Global stats (every 4 loops)
	if (((loopCount + 1) & 3) == 0) {
		var totalCount = 0;
		var totalTime = 0;
		var nGames = gameList.getGameCount();
		for (var i = 0; i < nGames; i++) {
			var inf = gameList.getGame(i);
			totalCount += inf.playCount;
			totalTime += inf.playTime;
		}
		udmd.DisplayScene00("FlexDMD.Resources.dmds.black.png", "Total play count:" , 15, "" + totalCount, 15, 10, 1500, 8);
		udmd.DisplayScene00("FlexDMD.Resources.dmds.black.png", "Total play time:" , 15, "" + totalTime.toDDHHMMSS(), 15, 10, 1500, 8);
	}
	
	// Drink'n drive (every 4 loops)
	if (((loopCount + 2) & 3) == 0) {
		udmd.DisplayScene00("./Scripts/dmds/misc/drink'n drive.png", "", 15, "", 15, 10, 3000, 8);
	}
	
	// Highscores
	if (hiscores[info.id] != null) {
		udmd.ScrollingCredits("", hiscores[info.id].join("|"), 15, 14, 2800 + hiscores[info.id].length * 400, 14);
	}
	
	dmd.UnlockRenderThread();
	logfile.log("< Update DMD done");

	// Add a timeout for when the queue will be finished
	updater = setTimeout(UpdateDMD, 10000);
}

gameList.on("gameselect", event => {
	logfile.log("> gameselect");
	info = event.game;
	// Delay update since we have to reset the DMD to take in account the ROM settings which can cause stutters
	if (useTableRom) {
		if (updater !== undefined) clearTimeout(updater);
		updater = setTimeout(UpdateDMD, 200);
	} else {
		UpdateDMD();
	}
});

gameList.on("highscoresready", event => {
	logfile.log("> highscoresready");
	if (event.success && event.game != null) {
		logfile.log("> scores received");
		for (var i = 0; i < event.scores.length; i++) {
			event.scores[i] = event.scores[i].replace(/\u00FF/g, ',');
		}
		hiscores[event.game.id] = event.scores;
		if (shownInfo != null && event.game.id == shownInfo.id) {
			udmd.ScrollingCredits("", hiscores[shownInfo.id].join("|"), 15, 14, 2800 + hiscores[shownInfo.id].length * 400, 14);
		}
	}
});

mainWindow.on("prelaunch", event => {
	logfile.log("> launch");
	if (dmd != null) {
		udmd.CancelRendering();
		dmd.Run = false;
	}
});

mainWindow.on("postlaunch", event => {
	logfile.log("> postlaunch");
	if (dmd != null) dmd.Run = true;
	UpdateDMD();
});

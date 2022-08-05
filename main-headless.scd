/*
 © Copyright 2019-2022 Pascal Staudt, Bruno Gola, Marcello Lussana

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/
(
var sentireFolder = thisProcess.nowExecutingPath.dirname;
var configFile = (sentireFolder +/+ "Config/config.scd");
var localConfigFile = (sentireFolder +/+ "Config/local_config.scd");
var bootTime = Date.getDate.rawSeconds;

g = g ? ();
g.bootTimeout = 5;  // timeout for server boot


if (g.window.notNil) { g.window.close };
if (g.controller.notNil) {
	g.controller.clear;
	g.controller.free;
	// s.freeAll is a hack to make sure everything is freed.
	// technically g.controller.clear should do it, but if you
	// run this twice it might get lost and some nodes are kept alive
	s.freeAll;
};

g.config = configFile.load.value();


if (localConfigFile.pathMatch.size > 0) {
	var localConfig = localConfigFile.load.value();
	"Overriding config with local_config.scd".warn;
	g.config.putAll(localConfig);

} {
	("Loading config: " + configFile).inform;
};


// Find out about Sentire version
if (File.exists(sentireFolder +/+ ".git")) {
	// is running from Git repository
	var commit = ("cd " ++ sentireFolder ++ "; git rev-parse HEAD").unixCmdGetStdOut.stripWhiteSpace;
	g.config.version = "git+" ++ commit;
} {
	if (File.exists(sentireFolder +/+ "VERSION")) {
		var versionFile = File.open(sentireFolder +/+ "VERSION", "r");
		g.config.version = versionFile.getLine.stripWhiteSpace;
		versionFile.close;
	} {
		g.config.version = "NotFound"
	};
	("Running Sentire Version" + g.config.version).inform;
};


g.config.sentireFolder = sentireFolder;
g.config.loadServerOptions(s);


s.waitForBoot({
	// This is necessary because the timeout parameter of s.waitForBoot is not working in SC 3.11
	if ((Date.getDate.rawSeconds - bootTime) < g.bootTimeout) {
		g.controller = Sentire.new(g.config);
		// load the bowls sound environment after everything else
		// TODO: fix synchronisation problems
		//s.sync;
		//(sentireFolder +/+ "Sounds/bowls.scd").load();
	};
});


fork {
	var breakBoot = false;
	while { s.serverRunning.not and: breakBoot.not } {
		if ((Date.getDate.rawSeconds - bootTime) > g.bootTimeout) {
			 "Could not start the SuperCollider server".warn;
			breakBoot = true;
		};
		0.2.wait;
	};
};

// Returns the g variable for testing
g;

)

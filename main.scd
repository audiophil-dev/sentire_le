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
var headless = thisProcess.nowExecutingPath.dirname +/+ "main-headless.scd";
var bootTime = Date.getDate.rawSeconds;
"---".inform;
"Started Sentire from main.scd".inform;
"---".inform;

"loading sentire headless...".inform;
g = headless.load;
"...loaded sentire headless".inform;
s.waitForBoot({
	// This is necessary because the timeout parameter of s.waitForBoot is not working in SC 3.11
	if ((Date.getDate.rawSeconds - bootTime) < g.bootTimeout) {
		"Building Sentire GUI...".inform;
		//Server.default.sync;
		g.window = Window('Sentire', Rect(10, 800, 500, 800));
		g.controller.gui(g.window);
		"...loaded GUI".inform;
	};
});

"...Finished loading main.scd";
)

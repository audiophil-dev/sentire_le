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
var sentireFolder = thisProcess.nowExecutingPath.dirname +/+ "..";
g = g ? ();
g.config = (sentireFolder +/+ "Config/config.scd").load.value();
g.config.sentireFolder = sentireFolder;
g.config.loadServerOptions(s);

s.waitForBoot {
	g.controller ?? { g.controller.clear };
	g.controller = Sentire.new(g.config);
	1.wait;
	g.controller.signalFlow.proximityDetection.proxy.source = {
		\in.ar(0); // ignore the input from rawSignal
		K2A.ar(MouseX.kr(0, 1))
	};
	g.window = Window('Sentire', Rect(10, 800, 500, 800));
	g.controller.gui(g.window);
};
)


// Mock the rawSignal for testing
g.controller.signalFlow.rawSignal.proxy.source = {LFNoise2.ar(1).exprange(0.02, 20)};

// Mock the proximityDetection with mouseX as control
(
g.controller.signalFlow.proximityDetection.proxy.source = {
	\in.ar(0); // ignore the input from rawSignal
	K2A.ar(MouseX.kr(0, 1))
};
);


// Play / mute sound:
g.controller.play;
g.controller.stop;
g.controller.rangeScale = 0;
g.controller.stop;

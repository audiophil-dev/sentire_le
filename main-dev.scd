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
var main = thisProcess.nowExecutingPath.dirname +/+ "main.scd";
g = main.load;
nil
);

// Choose a Sound Enviroment
g.controller.soundEnvironmentSelector.selected = \testSin
g.controller.soundEnvironmentSelector.selected = \noSound


// *** Testing ***

(
// Set the right thresholds
var temp = 0.056290987432;
g.controller.signalFlow.proximityDetection.thresholdLow = 0.00041675037209643;
g.controller.signalFlow.proximityDetection.thresholdHigh = temp;
g.controller.signalFlow.proximityDetection.thresholdTouch = 0.98;
g.controller.signalFlow.proximityDetection.signalSplitProportion = 400/400;
g.controller.signalFlow.proximityDetection.signalSplitValue = temp;
)

g.controller.reset;

// set the sensor output
g.controller.signalFlow.rawSignal.outputChannel = 2;

// *** Simulate Input ***

// With the signalProcessing with mouseX as control
(
g.controller.signalFlow.proximityDetection.proximity.proxy.source = {
	\in.ar(0); // ignore the input from rawSignal
	K2A.ar(MouseX.kr(0, 2))
};
);

// With a defined signal function
(
g.controller.signalFlow.rawSignal.proxy.source = {
	var sig, noise, modulation, dust;
	noise = WhiteNoise.ar(0.001);
	sig = SinOsc.ar(12000, 0, [-40.dbamp, -40.dbamp]) + SinOsc.ar(19000, 0, [-30.dbamp, -30.dbamp]);
	modulation = Phasor.ar(0.0, 0.05 / SampleRate.ir, start: 0.0, end: 1.0, resetPos: 0.0);
	//modulation = modulation + (Phasor.ar(0.0, 1 / SampleRate.ir, start: 0.0, end: 1.0, resetPos: 0.0) * 0.01);
	dust = Dust.ar(1, 10);
	//sig = sig * modulation;
	sig = sig + noise;
	sig = sig + dust;
	sig;
};

)

// *** reset ***
g.controller.reset;

// reset the input to use the sensor signal
g.controller.signalFlow.rawSignal.resetProxy;





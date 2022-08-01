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
s.boot;

q = q ? ();

(
SentireSoundEnvironment(\sin,
	source: {
		SinOsc.ar(\proximity.ar(0)) * \amplitude.ar(0)!2;
	},
	mapping: (
		proximity: SentireEnvelope([0.2,0.8], [100, 1200, 2900, 20], [\exp, \exp, \exp]),
		amplitude: SentireEnvelope([0.3,0.7], [0.0, 1.0, 0.0, 0.8], [\lin, \lin, \lin])
	),
).run;

SentireSoundEnvironment(\pulse,
	source: {
		LFPulse.ar(\proximity.ar(0)) * \amplitude.ar(1)!2;
	},
	mapping: (
		proximity: SentireEnvelope([0.2,0.8], [100, 12200, 200, 20], [\exp, \exp, \exp]),
		amplitude: SentireEnvelope([0.3,0.7], [0.0, 0.1, 1.0, 0.2], [\lin, \lin, -10])
	),
).run;
)

// WATCH OUT FOR HIGH PITCH FROM SentireSignal
q.flow = SentireSignalFlow.new(proximityDetection: SentireWiredProximityDetection.new,soundEnvironment: SentireSoundEnvironment(\sin));


// add a test input
q.testSignal = NodeProxy.audio(s, 1);
q.testSignal.source = { SinOsc.ar(12000) * LFNoise2.ar(0.5, 0.3) };
q.flow.proximityDetection.proximity.filteredSignal.set(\in, q.testSignal);

// change output of transmitter signal
s.meter;
q.flow.rawSignal.outputChannel = 2;

// check the scope
q.flow.scope;

// inspect the signal in other points
q.flow.rawSignal.scope;
q.flow.proximityDetection.scope;
q.flow.soundEnvironment.scope;
q.flow.effects.scope;
q.flow.master.scope;

// listen to the flow
q.flow.play;

// change the current soundEnvironment
q.flow.soundEnvironment = SentireSoundEnvironment(\pulse);

// change to sin again
q.flow.soundEnvironment = SentireSoundEnvironment(\sin);
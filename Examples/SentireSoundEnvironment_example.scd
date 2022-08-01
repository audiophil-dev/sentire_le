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

(
~sin = SentireSoundEnvironment(\sin,
	source: {
		SinOsc.ar(\proximity.ar(0)) * \amplitude.ar(0)!2;
	},
	mapping: (
		proximity: SentireEnvelope([0.2,0.8], [100, 1200, 2900, 20], [\exp, \exp, \exp]),
		amplitude: SentireEnvelope([0.3,0.7], [0.0, 1.0, 0.0, 0.8], [\lin, \lin, \lin])
	)
);
~sin.run;

~pulse = SentireSoundEnvironment(\pulse,
	source: {
		LFPulse.ar(\proximity.ar(0)) * \amplitude.ar(1)!2;
	},
	mapping: (
		proximity: SentireEnvelope([0.2,0.8], [100, 12200, 200, 20], [\exp, \exp, \exp]),
		amplitude: SentireEnvelope([0.3,0.7], [0.0, 0.1, 1.0, 0.2], [\lin, \lin, -10])
	),
);
~pulse.run;
)

// ndef to simulate signal input
Ndef(\tri, {LFTri.ar(0.1).exprange(0.001,20.0) + LFNoise1.ar(0.1).exprange(0.01, 5)});

~input = SentireSignalProcessing.new;

n = NodeChain(s);

n.scope;

n.addAfter(\input, Ndef(\tri))
n.addAfter(\processing, ~input)
n.addAfter(\sin, ~sin)

// set envelope scale

~sin.mapping.keysValuesDo { |key, envelope|
	envelope.scale = 0.5;
}

n.remove(\sin)

n.addAfter(\sin, ~pulse)

~af = NodeProxy.audio(s, 2);
~af.source = {\in.ar(0!2) * LFTri.ar(2) };

n.addAfter(\af, ~af);

n.remove(\sin);

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
q = q ? ();

s.waitForBoot {
	var scale = Scale.majorPentatonic.degreeToFreq(Scale.majorPentatonic.degrees, 53.midicps, 0);

	SentireSoundEnvironment(\touchSin,
		source: {
			RLPF.ar(
				SinOscFB.ar(\freq.ar(scale[0]), 0.5),
				\proximity.ar(0),
				0.4
			) * \amplitude.ar(0)!2;
		},

		touchSource: {
			var sig, freq;

			freq = \freq.ar(scale[0]);
			sig = SinOsc.ar(freq, 0, 0.25);
			sig = Splay.ar(sig);
			sig = LeakDC.ar(sig);
			sig = HPF.ar(sig, 40);
		},

		mapping: (
			proximity: SentireEnvelope([0.2,0.8], [100, 1200, 2900, 20], [\exp, \exp, \exp]),
			amplitude: SentireEnvelope([0.3,0.7], [0.0, 0.5, 0.0, 0.2], [\lin, \lin, \lin]),
			freq: {
				var freq = Demand.ar(
					\touch.ar(1), 0,
					Dseq([Dxrand(scale,3)] ++
						[Dstutter(Drand([1,2,3], 1), Drand(scale, 1))], inf));

				// debug touch
				SendReply.ar(\touch.ar(1), "/touch");

				// Demand returns 0 until the first trig, so we set
				// scale[0] as default until first trig.
				Select.ar(freq > 1.0, [K2A.ar(scale[0]), freq]);
			},
		),
	);

	// debug OSC receiver to see the touch
	f.free; f = OSCFunc({"touch!!".postln}, "/touch");
};
);

// Mock an input signal
q.signal = SentireSignalProcessing.new;
q.signal.proxy.source = {K2A.ar(MouseX.kr(0,1))};

SentireSoundEnvironment(\touchSin).run;
SentireSoundEnvironment(\touchSin) <<> q.signal;
SentireSoundEnvironment(\touchSin).scope;
SentireSoundEnvironment(\touchSin).play;
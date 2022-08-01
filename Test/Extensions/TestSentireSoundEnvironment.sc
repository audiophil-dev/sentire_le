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
TestSentireSoundEnvironment : UnitTest {
	var done = false;

	setUp {
		this.bootServer(Server.default);
		Server.default.sync;

		SentireSoundEnvironment(\testSoundEnvironment,
			source: {SinOsc.ar(200)},
			server: Server.default);
		SentireSoundEnvironment(\testSoundEnvironment).run;
		Server.default.sync;
	}

	tearDown {
		SentireSoundEnvironment(\testSoundEnvironment).clear;
	}

	test_reset_sentire_sound_environment_source {
		var recv, value;

		// Reset source
		SentireSoundEnvironment(\testSoundEnvironment,
			source: {
				SendReply.kr(Impulse.kr(200), '/test_responder_source');
				SinOsc.ar(100)}).run;

		recv = OSCFunc({ arg msg;
			done=true;
		}, '/test_responder_source');

		this.wait({done}, "Wait for the new source to run", 1);

		recv.free;

		// Reset touchSource
		SentireSoundEnvironment(\testSoundEnvironment, touchSource: {
			SendReply.kr(Impulse.kr(200), '/test_responder_touch');
			SinOsc.ar(1200)}).run;

		recv = OSCFunc({arg msg;
			done=true;
		}, '/test_responder_touch');

		this.wait({done}, "Wait for the new touchSource to run", 1);
		done = false;
		recv.free;

		// Reset source to use a mapping
		SentireSoundEnvironment(\testSoundEnvironment,
			source: {
				SendReply.kr(Impulse.kr(200), '/test_responder_mapping', [\freq.ar(0)]);
				SinOsc.ar(\freq.ar);
			},
			mapping: (freq: {200.0})).run;

		recv = OSCFunc({arg msg;
			value = msg[3];
		}, '/test_responder_mapping');

		this.wait({value == 200.0}, "New mapping of \freq should be 200.0", 1);
		recv.free;
	}
}



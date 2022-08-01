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
TestSentireMain : UnitTest {
	classvar sentireMain, sentire, firstMethod, waitTime=0.5;

	setUp {
		if (firstMethod.isNil and: {Server.default.hasBooted}) {
			Server.default.quit;
			this.wait({Server.default.hasBooted.not});
			firstMethod = true;
		};

		if (Server.default.hasBooted.not) {
			this.bootServer(Server.default);

			sentireMain = thisMethod.filenameSymbol.asString.dirname.dirname.dirname ++ "/main-headless.scd";
			this.assertNoException({sentire = sentireMain.load}, "Load main Sentire file");

			this.wait({sentire.controller.notNil and: {
				sentire.controller.isMonitoring}}, "Controller never started", 2);

			// dont need to monitor/send to speakers while running the test
			sentire.controller.stop;

			// Force threshold values
			sentire.controller.signalFlow.proximityDetection.thresholdLow = 0.00038;
			sentire.controller.signalFlow.proximityDetection.thresholdHigh = 0.06;
			sentire.controller.signalFlow.proximityDetection.thresholdTouch = 0.98;
			sentire.controller.signalFlow.proximityDetection.signalSplitValue = 0.06;
			sentire.controller.signalFlow.proximityDetection.signalSplitProportion = 1;

			// Wait for things to sync in
			this.wait({Server.default.sync; 3.wait; true});
		};
	}

	tearDown {
		// Resets the rawSignal and soundEnvironment
		SentireSoundEnvironment(\testSound).clear;
		sentire.controller.signalFlow.rawSignal.proxy.source = {SoundIn.ar(0)};
	}

	test_basic_signal_path_mapping {
		var measure;

		SentireSoundEnvironment(\testSound,
			{SinOsc.ar(\freq.ar(0))!2},
			nil,
			(freq: SentireEnvelope([1/3, 2/3], [0,400,800,1200], [\lin,\lin,\lin]))
		);

		measure = NodeProxy.control(Server.default, 1);
		measure.source = { Pitch.kr(sentire.controller.signalFlow.master.ar(2).sum)[0] };

		sentire.controller.signalFlow.rawSignal.proxy.source = {
			WhiteNoise.ar(0.001) + (SinOsc.ar(SampleRate.ir/4) * \amp.kr(20.neg.dbamp)) };
		sentire.controller.soundEnvironmentSelector.selected = \testSound;


		if (sentire.config.proximitySensor == \wireless) {
			// gotta wait for the value because the proximity is lagged
			"Wireless mode not supported in SE version".warn;
		} {
			// Values for wired version
			sentire.controller.signalFlow.rawSignal.proxy.set(\amp, 0.neg.dbamp);

			// gotta wait for the value because the proximity is lagged
			this.wait({measure.bus.getSynchronous < 1202.0 and: {measure.bus.getSynchronous > 1198.0}},
				"Pitch should be ~1200 when rawSignal amp is 0db", 2);
			this.wait({waitTime.wait; true});
			this.assertFloatEquals(measure.bus.getSynchronous, 1200.0,
				"Pitch should be ~1200 when rawSignal amp is 0db", 3);

			// set distance to half the way, value measured by hand accordingly to current
			// threshold values for the wired setting in the config file
			sentire.controller.signalFlow.rawSignal.proxy.set(\amp, 42.4.neg.dbamp);

			this.wait({measure.bus.getSynchronous < 605.0 and: {measure.bus.getSynchronous > 595.0}},
				"Pitch should be ~600 when rawSignal amp is -42.4db", 3);
			this.wait({waitTime.wait; true});
			this.assertFloatEquals(measure.bus.getSynchronous, 600.0,
				"Pitch should be ~600 when rawSignal amp is -42.4db", 5);
		};

		measure.clear;
	}

	test_touch_trigger {
		var measure;

		// Disables reverb to avoid conflicting with perc envelope sound
		var originalReverb = sentire.controller.signalFlow.effects.proxy.nodeMap.at(\wet20).value;
		sentire.controller.signalFlow.effects.set(\GVerb_wet, 0);

		SentireSoundEnvironment(\testSound,
			{Silent.ar},
			{arg trig; SinOsc.ar(440) * EnvGen.ar(Env.perc(0.01, 1, 0.2), trig)!2},
		);

		this.wait({waitTime.wait;true});
		sentire.controller.soundEnvironmentSelector.selected = \testSound;
		this.wait({waitTime.wait;true});

		measure = NodeProxy.control(Server.default, 1);
		measure.source = { RunningMax.kr(Amplitude.kr(sentire.controller.signalFlow.master.ar(2).sum), \t_reset.kr(0)) };
		sentire.controller.signalFlow.rawSignal.proxy.source = { SinOsc.ar(SampleRate.ir/4) * \amp.kr(0)};

		this.wait({waitTime.wait;true});

		this.assertFloatEquals(measure.bus.getSynchronous, 0,
			"Amplitude should be 0 when there is no touch");

		// Sets maximum amplitude in rawSignal to simulate touch
		if (sentire.config.proximitySensor == \wireless) {
			"Wireless mode not supported in SE version".warn;
		} {
			sentire.controller.signalFlow.rawSignal.proxy.set(\amp, 1.neg.dbamp);
		};

		this.wait({ measure.bus.getSynchronous > 0.1 },
			"Amplitude should be greater than 0.1 when there is a touch and is %".format(measure.bus.getSynchronous), 2.0);

		// wait a bit for the envelope
		this.wait({waitTime.wait; true});

		this.assert(measure.bus.getSynchronous < 0.8,
			"Touch amplitude is never greater than 0.8",
			onFailure: {"Value was %".format(measure.bus.getSynchronous).postln});

		// Waits for the envelope to be over
		this.wait({ measure.set(\t_reset, 1); measure.bus.getSynchronous < 0.001 },
			"Amplitude should be 0 again after the touch envelope is over", 3.0);

		sentire.controller.signalFlow.effects.set(\GVerb_wet, originalReverb);
		measure.clear;
	}


}

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
SentireSignalProcessing : NodeChain {
	var <testSignal, <filteredSignal,
	<envelopeSignal, <smoothedSignal, <scaledSignal, useSlew;


	*new {arg server=nil, numChannels=2,
		thresholdLow, thresholdHigh, signalSplitValue,
		signalSplitProportion, signalScale = 1.0, signalFrequency = 12000.0,
		useSlewFilter = false;

		^super.new(server, numChannels).initChild(
			thresholdLow, thresholdHigh, signalSplitValue,
			signalSplitProportion, signalScale, signalFrequency, useSlewFilter);
	}

	initChild { arg thresholdLow, thresholdHigh,
		signalSplitValue, signalSplitProportion, signalScale, signalFrequency, useSlewFilter = false;

		"init SentireSignalProcessing...".debug(1, this);
		useSlew = useSlewFilter;
		this.prInitializeNodes(thresholdLow, thresholdHigh,
			signalSplitValue, signalSplitProportion, signalScale, signalFrequency);
		"...done".debug(1, this);
	}

	prInitializeNodes { arg thresholdLow, thresholdHigh,
		signalSplitValue, signalSplitProportion, signalScale, signalFrequency;
		"init Nodes".debug(1, this);
		//testSignal ?? { this.prInitTestSignal(signalFrequency) };
		filteredSignal ?? { this.prInitFilteredSignal(signalFrequency) };
		envelopeSignal ?? { this.prInitEnvelopeFollower() };
		smoothedSignal ?? { this.prInitSmoothing() };
		scaledSignal ?? { this.prInitScaling(thresholdLow, thresholdHigh,
			signalSplitValue, signalSplitProportion, signalScale) };

		//"Add testSignal to chain".debug(1, this);
		//this.addAfter(\testSignal, testSignal);

		"Add filteredSignal to chain".debug(1, this);
		this.addAfter(\filteredSignal, filteredSignal);

		"Add envelope Detection to chain".debug(1, this);
		this.addAfter(\envelopeSignal, envelopeSignal);

		"Add signal smoothing to chain".debug(1, this);
		this.addAfter(\smoothedSignal, smoothedSignal);

		"Add scaled signal to chain".debug(1, this);
		this.addAfter(\scaledSignal, scaledSignal);
	}

	prInitTestSignal { arg signalFrequency;
		"init testSignal".debug(1, this);
		testSignal = SentireProxyInterface();
		testSignal.proxy = NodeProxy.audio(server, 1);
		{
			server.sync;
			testSignal.proxy.source = {
				var sig, noise, modulation;
				noise = WhiteNoise.ar(0.001);
				sig = SinOsc.ar(signalFrequency, 0, 0.01);
				modulation = Phasor.ar(0.0, 0.5 / SampleRate.ir, start: 0.0, end: 1.0, resetPos: 0.0);
				sig = sig * modulation;
				sig = sig + noise;
			};
		}.fork;
	}


	prInitFilteredSignal { arg signalFrequency=12000, slewUp=5000, slewDown=5000;
		"init filteredSignal".debug(1, this);
		filteredSignal = SentireProxyInterface();
		filteredSignal.proxy = NodeProxy.audio(server, 1);
		filteredSignal.proxy.set(\rq, 0.05);
		filteredSignal.proxy.addSpec(\rq, \rq);
		filteredSignal.proxy.set(\centerFreq, signalFrequency);
		filteredSignal.proxy.addSpec(\centerFreq, \freq);
		filteredSignal.proxy.set(\gainCompensationDB, 2);
		filteredSignal.proxy.addSpec(\gainCompensationDB, ControlSpec(0.ampdb, 10.ampdb, \db, units: " dB"));
		filteredSignal.proxy.addSpec(\slewUp, \lin);
		filteredSignal.proxy.set(\slewUp, slewUp);
		filteredSignal.proxy.addSpec(\slewDown, \lin);
		filteredSignal.proxy.set(\slewDown, slewDown);

		{
			server.sync;
			filteredSignal.proxy.source = {
				var filtered = \in.ar(0);
				if (useSlew) {
					filtered = Slew.ar(filtered, \slewUp.kr, \slewDown.kr);
				};
				filtered = BPF.ar(filtered, \centerFreq.kr, \rq.kr, (\gainCompensationDB.kr).dbamp);
			};
		}.fork;
	}


	prInitEnvelopeFollower {
		"init EnvelopeFollower".debug(1, this);
		envelopeSignal = SentireProxyInterface();
		envelopeSignal.proxy = NodeProxy.audio(server, 1);
		{
			server.sync;
			envelopeSignal.proxy.source = {
				var signal = \in.ar(0);
				var envelope;
				envelope = Amplitude.ar(signal, 0.01, 0.01);
			};
		}.fork;
	}


	prInitSmoothing {
		"init signal smoothing".debug(1, this);
		smoothedSignal = SentireProxyInterface();
		smoothedSignal.proxy = NodeProxy.audio(server, 1);
		smoothedSignal.proxy.addSpec(\lagTime, ControlSpec(0.01, 2, \lin, 0.01, 0.001, "s"));
		smoothedSignal.proxy.addSpec(\lagTimeMax, ControlSpec(0.01, 2, \lin, 0.01, 0.001, "s"));
		{
			server.sync;
			smoothedSignal.proxy.source = {
				//var windowSize = SampleRate.ir * \windowTime.kr;
				//var windowFactor = windowSize.reciprocal;
				//var trig = Impulse.ar((\windowTime.kr).reciprocal);
				var signal = \in.ar(0);
				var smoothed;
				var scaled = scaledSignal.proxy.ar(1);
				var lagTime = scaled.lincurve(0, 1, \lagTimeMax.kr(0.3), \lagTime.kr(0.001), -8);
				smoothed = Lag.ar(signal, lagTime, \calibrationCompensation.kr(1));

				smoothed = smoothed = Lag.ar(signal, \lagTime.kr, \calibrationCompensation.kr(1));
				//smoothed = AverageOutput.ar(signal, trig);
				//smoothed = RunningSum.ar(signal, windowSize) * windowFactor;
			};
		}.fork;
	}


	prInitScaling { arg thresholdLow, thresholdHigh,
		signalSplitValue, signalSplitProportion, signalScale;
		"init signal scaling".debug(1, this);
		scaledSignal = SentireProxyInterface();
		scaledSignal.proxy = NodeProxy.audio(server, 1);
		scaledSignal.proxy.source = this.prGetDefaultScalingFunction(
			thresholdLow, thresholdHigh, signalSplitValue,
			signalSplitProportion, signalScale);
	}

	setScalingFunction {arg func;
		scaledSignal.proxy.source = func;
	}

	prGetDefaultScalingFunction { arg thresholdLow, thresholdHigh,
		signalSplitValue, signalSplitProportion, signalScale;
		var func = {
			var signal = \in.ar(0);
			var scaled, normalized;

			normalized = signal.explin(
				\thresholdLow.kr(thresholdLow),
				\thresholdHigh.kr(thresholdHigh), 0, 1);

			// does scaling based on envelope, maybe this should be done somewhere else.
			scaled = normalized.linlin(1-\scale.kr(signalScale), 1, 0, 1);
		};
		^func;
	}

	thresholdLow_ { arg value;
		scaledSignal.proxy.set(\thresholdLow, value);
	}

	thresholdHigh_ { arg value;
		scaledSignal.proxy.set(\thresholdHigh, value);
	}

	signalSplitValue_ { arg value;
		scaledSignal.proxy.set(\signalSplitValue, value);
	}

	signalSplitProportion_ { arg value;
		scaledSignal.proxy.set(\signalSplitProportion, value);
	}

	signalScale_ { arg value;
		scaledSignal.proxy.set(\scale, value);
	}

	thresholdLow { arg value;
		^scaledSignal.proxy.get(\thresholdLow);
	}

	thresholdHigh { arg value;
		^scaledSignal.proxy.get(\thresholdHigh);
	}

	signalSplitValue { arg value;
		^scaledSignal.proxy.get(\signalSplitValue);
	}

	signalSplitProportion { arg value;
		^scaledSignal.proxy.get(\signalSplitProportion);
	}

	signalScale { arg value;
		^scaledSignal.proxy.get(\scale);
	}

	clear {
		testSignal.clear;
		filteredSignal.clear;
		envelopeSignal.clear;
		smoothedSignal.clear;
		scaledSignal.clear;
	}

}

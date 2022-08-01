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
TestSentireSignalProcessing : UnitTest {
	var thresholdMin = 0.0001;
	var thresholdMax = 0.00851;
	setUp {
		if (Server.default.hasBooted.not) {
			this.bootServer;
		};
	}

	test_scaling_proxy_function {
		// tests explin scaling
		var signalProcessing = SentireSignalProcessing(Server.default, 2,
			thresholdMin, thresholdMax);

		Ndef(\testGetter, {A2K.kr(signalProcessing.proxy.ar(1))});

		// wait for Ndefs
		this.wait({1.wait;true});

		// By default value should be 0 when there is no input
		this.assertFloatEquals(Ndef(\testGetter).bus.getSynchronous, 0.0,
			"SignalProcessing should output 0.0 when there is not input");

		signalProcessing.smoothedSignal.proxy.source = { K2A.ar(0.0) };

		this.assertFloatEquals(Ndef(\testGetter).bus.getSynchronous, thresholdMin,
			"SignalProcessing should output 0.0 when input is min");

		signalProcessing.smoothedSignal.proxy.source = { K2A.ar(thresholdMax) };
		this.wait({3.wait;true});

		this.assertFloatEquals(Ndef(\testGetter).bus.getSynchronous, 1.0,
			"SignalProcessing should output 1.0 when input is max");

		signalProcessing.smoothedSignal.proxy.source = { K2A.ar(0.5.linexp(0, 1, thresholdMin, thresholdMax)) };
		this.wait({3.wait;true});

		this.assertFloatEquals(Ndef(\testGetter).bus.getSynchronous, 0.5,
			"SignalProcessing should output 0.5 when input amplitude is mapped from lin to exp as 0.5");

		Ndef(\testGetter).clear;
	}

	test_scaling_proxy_function_change_values {
		var signalProcessing = SentireSignalProcessing(Server.default, 2,
			thresholdMin, thresholdMax);

		Ndef(\testGetter, {A2K.kr(signalProcessing.proxy.ar(1))});

		// wait for Ndefs
		this.wait({1.wait;true});

		signalProcessing.thresholdLow = 0.1;
		signalProcessing.thresholdHigh = 0.6;

		signalProcessing.smoothedSignal.proxy.source = { K2A.ar(0.05) };
		this.wait({3.wait;true});

		this.assertFloatEquals(Ndef(\testGetter).bus.getSynchronous, 0.0,
			"SignalProcessing should output 0.0 when input is lower then thresholdLow");


		signalProcessing.smoothedSignal.proxy.source = { K2A.ar(0.601) };
		this.wait({3.wait;true});

		this.assertFloatEquals(Ndef(\testGetter).bus.getSynchronous, 1.0,
			"SignalProcessing should clip at 1.0 when input above thresholdHigh");

		signalProcessing.smoothedSignal.proxy.source = { K2A.ar(0.599) };
		this.wait({3.wait;true});

		this.assert(Ndef(\testGetter).bus.getSynchronous < 1.0,
			"SignalProcessing should not be 1.0 below thesholdHigh");

		this.wait({1.wait;true});

		Ndef(\testGetter).clear;
	}
}

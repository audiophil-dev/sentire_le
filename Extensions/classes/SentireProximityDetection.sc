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
SentireProximityDetectionInterface : NodeChain {
	var <proximity, <touchProxy;

	*new { arg server=nil, numChannels=2,
		thresholdTouch=0.99, thresholdLow=0.001, thresholdHigh=0.2, signalSplitValue=0.1,
		signalSplitProportion=0.5, signalScale=1, signalFrequencies=#[12000];

		^super.new(server, numChannels).initChild(
			thresholdTouch, thresholdLow, thresholdHigh,
			signalSplitValue, signalSplitProportion, signalScale, signalFrequencies);
	}

	initChild { arg thresholdTouch, thresholdLow,
		thresholdHigh, signalSplitValue,
		signalSplitProportion, signalScale, signalFrequencies;

		this.prInitProximity(thresholdLow,
			thresholdHigh, signalSplitValue,
			signalSplitProportion, signalScale, signalFrequencies);
		this.prInitTouchProxy(thresholdTouch);
	}

	prInitProximity { arg thresholdLow,
		thresholdHigh, signalSplitValue,
		signalSplitProportion, signalScale;
		/* Should be implemented by children */
		Error("Children classes should implement prInitProximity method");
	}

	prInitTouchProxy { arg defaultThreshold;

		touchProxy = touchProxy ?? { SentireProxyInterface.new(); };
		touchProxy.proxy = touchProxy.proxy ?? { NodeProxy.audio(server, 1) };
		if (proximity.proxy.notNil) {
			touchProxy.proxy.source = { arg thresholdTouch;
				(proximity.proxy.ar(1) > Select.kr(thresholdTouch > 0, [
					defaultThreshold,
					thresholdTouch
				]));
			};
		} {
			"Proximity proxy source was not defined yet, can't init touch proxy".warn;
		};
	}

	touch {
		^touchProxy;
	}

	thresholdTouch_ { arg value;
		touchProxy.set(\thresholdTouch, value);
	}
}

SentireWiredProximityDetection : SentireProximityDetectionInterface {
	var <sensorProcessing;
	var lagTime = 0.001;
	var lagTimeMax = 0.6;

	prInitProximity { arg thresholdLow,
		thresholdHigh, signalSplitValue,
		signalSplitProportion, signalScale,
		signalFrequencies;

		var sensorInput = NodeProxy.audio(server, 1);
		// inputProxy is the head of the NodeChain
		// see NodeChain class for the definition
		sensorInput.source = {
			inputProxy.ar(1,0)
		};

		"init SentireWiredProximityDetection...".debug(1, this);
		sensorProcessing = SentireSignalProcessing(
			server,
			numChannels,
			thresholdLow,
			thresholdHigh,
			signalSplitValue,
			signalSplitProportion,
			signalScale,
			signalFrequencies[0]
		);

		"Add proximity detection to chain".debug(1, this);

		sensorProcessing.smoothedSignal.proxy.set(\lagTime, lagTime);
		sensorProcessing.smoothedSignal.proxy.set(\lagTimeMax, lagTimeMax);

		this.initProximityDetection();
		this.addAfter(\proximity, proximity);
		sensorProcessing.set(\in, sensorInput);

	}

	initProximityDetection {
		"init proximity detection".debug(1, this);
		proximity = SentireProxyInterface();
		proximity.proxy = NodeProxy.audio(server, 2);
		proximity.proxy.source = {
			sensorProcessing.proxy.ar(numChannels);
		};
	}



	thresholdLow_ { arg value;
		sensorProcessing.thresholdLow = value;
	}

	thresholdHigh_ { arg value;
		sensorProcessing.thresholdHigh = value;
	}

	signalSplitValue_ { arg value;
		sensorProcessing.signalSplitValue = value;
	}

	signalSplitProportion_ { arg value;
		sensorProcessing.signalSplitProportion = value;
	}

	signalScale_ { arg value;
		sensorProcessing.scaledSignal.proxy.set(\scale, value);
	}
}
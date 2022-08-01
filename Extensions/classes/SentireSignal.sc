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
SentireSignal : SentireProxyInterface {
	var <server, <inputChannels, <outputChannel, signalFrequency, outputProxy;

	*new { arg server = nil, inputChannels = 0, outputChannel = 0, signalFrequency = 12000;
		server = server ? Server.default;
		^this.newCopyArgs(nil, server, inputChannels, outputChannel, signalFrequency).init;
	}

	init {
		outputProxy = NodeProxy.audio(server, 1);
		proxy = NodeProxy.audio(server, inputChannels.size);
		this.resetProxy;

		outputProxy.source = {
			SinOsc.ar(signalFrequency);
		};

		this.resetProxy;
	}

	outputChannel_ { arg channel;
		outputChannel = channel;
		this.prMonitorOutputProxy;
	}

	resetProxy {
		proxy.source = {
			SoundIn.ar(\inChannels.kr(inputChannels));
		};
	}

	inputChannels_ { arg channels;
		proxy.set(\inChannels, channels);
	}

	disableOutput { arg disable;
		if (disable, {
			outputProxy.stop;
		}, {
			this.prMonitorOutputProxy;
		}
		);
	}

	enableOutput { arg enable;
		this.disableOutput(enable.not);
	}

	clear {
		if (proxy.notNil) { proxy.clear };
		if (outputProxy.notNil) { outputProxy.clear };
	}

	prMonitorOutputProxy {
		outputProxy.play(outputChannel);
	}
}

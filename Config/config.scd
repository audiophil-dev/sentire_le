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
{(
	pyOscVideoAddr: "127.0.0.1",
	pyOscVideoPort: 57220,
	pyOscVideoPlayerPort: 57221,

	enableOSCInterface: false,

	// pre-selection of the recording checkboxes
	recording: (
		video: false,
		rawSignal: true,
		audioInput: true,
		master: false,
		folder: "Recordings/",
	),

	soundEnvironments: "Sounds",

	thresholdTouch: 1.02,

	rangeScale: 0.7,

	soundEnvironment: \noSound,

	serverMemSize: 8192*2,

	currentAudioInterface: \u192,

	proximitySensor: \wired,

	audioInterfaces: (
		p68c: (
			// gain setting is set at 2pm
			thresholdLow: 0.00044,
			thresholdHigh: 0.2,
			signalSplitValue: 0.2,
			signalSplitProportion: 1,

			numInputs: 4,
			numOutputs: 4,

			audioInputChannels: [2,3],
			audioOutputChannels: [2,3],

			sensorInputChannels: [1],
			sensorOutputChannel: 0,

			blockSize: 64,
			hardwareBufferSize: 128,
			numWireBufs: 4096,
			sampleRate: 48000,
			serverLatency: 0.4
		),

		u192: (
			// gain setting is set at about 2pm
			thresholdLow: 0.00023,
			thresholdHigh: 0.05,

			numInputs: 2,
			numOutputs: 4,

			audioInputChannels: [2,3],
			audioOutputChannels: [2,3],

			sensorInputChannels: [0],
			sensorOutputChannel: 0,

			blockSize: 64,
			hardwareBufferSize: 128,
			numWireBufs: 4096,
			sampleRate: 48000,
			serverLatency: 0.4
		),

		u192_wireless: (
			thresholdLow: 0.00015572052507196,
			thresholdHigh: 0.050718050450087,

			numInputs: 4,
			numOutputs: 4,

			audioInputChannels: [2,3],
			audioOutputChannels: [0,1],

			sensorInputChannels: [0],
			sensorOutputChannel: 0,

			blockSize: 32,
			hardwareBufferSize: 128,
			numWireBufs: 4096,
			sampleRate: 48000,
            serverLatency: 0.4
		)
	),

	audioInterface: { arg self;
		self.audioInterfaces[self.currentAudioInterface];
	},

	loadServerOptions: { arg self, server;
		server.options.memSize = self.serverMemSize;
		server.options.numInputBusChannels = self.audioInterface.numInputs;
		server.options.numOutputBusChannels = self.audioInterface.numOutputs;
		server.options.blockSize = self.audioInterface.blockSize;
		server.options.hardwareBufferSize = self.audioInterface.hardwareBufferSize;
		server.options.numWireBufs = self.audioInterface.numWireBufs;
		server.options.sampleRate = self.audioInterface.sampleRate;
	},
)}

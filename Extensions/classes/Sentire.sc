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
Sentire {
	var config, numChannels, <signalFlow,
	<rangeScale, <soundEnvironmentSelector, <master,
	<server, <effects, signal;

	*new { arg config;
		^this.newCopyArgs(config).init;
	}

	init {
		var proximityDetection, proximityDetectionClass, signalFrequencies;

		"Init Sentire".debug(1, this);
		server = Server.default;

		if (server.serverRunning.not) {
			"Server is not running!".warn;
		};

		if (server.sampleRate != 48000) {
			"Can't start Sentire using sampleRate different than 48000".error;
		};

		server.latency = 2;

		rangeScale = 1.0;

		if (config.enableOSCInterface) {
			 "OSC interface not supported in LE version".warn;
		};

		// Adds the default noSound sound environment here
		// so there is at least one sound environment while
		// loading Sentire
		SentireSoundEnvironment(\noSound,
			source: {
				K2A.ar(0);
			}
		);

		"Init SentireSoundEnvironmentSelector".debug(1, this);
		soundEnvironmentSelector = SentireSoundEnvironmentSelector.new({ arg soundEnvironment;
			this.prSetSoundEnvironment(soundEnvironment);
		});

		"Init SentireSignal".debug(1, this);
		signal = SentireSignal.new(
			server, config.audioInterface.sensorInputChannels,
			config.audioInterface.sensorOutputChannel, server.sampleRate/4);

		"Init SentireProximityDetection".debug(1, this);
		if (config.proximitySensor == \wireless) {
			("Wireless not supported in SE version").warn;
		} {
			signalFrequencies = [server.sampleRate/4];
			proximityDetectionClass = SentireWiredProximityDetection;
			signal.enableOutput(true);
		};

		proximityDetection = proximityDetectionClass.new(
			server,
			2,
			config.thresholdTouch,
			config.audioInterface.thresholdLow,
			config.audioInterface.thresholdHigh,
			config.audioInterface.signalSplitValue,
			config.audioInterface.signalSplitProportion,
			rangeScale,
			signalFrequencies
		);

		soundEnvironmentSelector.selected = \noSound;

		"Init SentireEffects".debug(1, this);
		effects = SentireEffects.new(server);

		"Init SentireSignalFlow".debug(1, this);
		signalFlow = SentireSignalFlow.new(server,
			signal, proximityDetection,
			SentireSoundEnvironment.new(soundEnvironmentSelector.selected),
			effects);

		signalFlow.soundEnvironment.run;
		this.play;

		"Init SentireMaster".debug(1, this);
		master = SentireMaster.new(signalFlow, config.audioInterface.audioOutputChannels);

		this.loadSoundEnvironmentsWhenReady;
	}

	loadSoundEnvironmentsWhenReady {
		// Waits for sentire to be fully loaded and then loads sound environments
		// because some of them are pretty heavy to load while every thing
		// is initializing.

		fork {
			var soundsFolder = PathName(config.sentireFolder +/+ config.soundEnvironments);

			if ((soundsFolder.isFolder), {

			}, {
				("Sounds Path '" + soundsFolder + "is not a Folder");
			});

			while {this.isReady.not} {
				server.sync;
				0.5.wait;
			};

			soundsFolder.filesDo({arg file;
				server.sync;
				file.fullPath.load;
			});

			server.sync;
			soundEnvironmentSelector.selected = config.soundEnvironment;

			// waits a bit before setting latency back
			2.wait;
			server.latency = config.audioInterface.serverLatency;
		};

	}

	isReady {
		// Returns if the last node in the signalFlow is playing in the server side
		^(master.notNil and: { signalFlow.master.isPlaying });
	}

	guiClass {
		^SentireGui;
	}

	clear {
		if (signalFlow.notNil) { signalFlow.clear };
	}

	isMonitoring {
		^signalFlow.chain.proxy.isMonitoring;
	}

	play {
		signalFlow.playN(config.audioInterface.audioOutputChannels);
	}

	stop {
		signalFlow.stop;
	}

	reset {
	// TODO: reset all relevant config
		signalFlow.rawSignal = signal;
	}

	rangeScale_ { arg value;
		rangeScale = value;
		if (signalFlow.notNil) {
			signalFlow.proximityDetection.signalScale = rangeScale;
			signalFlow.soundEnvironment.mappingScale = rangeScale;
		};
		this.changed;
	}

	prSetSoundEnvironment { arg soundEnvironment;
		if (signalFlow.notNil) {
			var oldSoundEnvironment = signalFlow.soundEnvironment;
			soundEnvironment.run;
			signalFlow.soundEnvironment = soundEnvironment;
			signalFlow.soundEnvironment.mappingScale = rangeScale;

			if (oldSoundEnvironment.notNil) {
				oldSoundEnvironment.end;
			};
		};
		this.changed;
	}
}

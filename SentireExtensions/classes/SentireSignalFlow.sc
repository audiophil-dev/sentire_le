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
SentireSignalFlow {
	var <server, <rawSignal, <proximityDetection, <soundEnvironment, <effects, <master;
	var <chain;

	*new { arg server, rawSignal, proximityDetection, soundEnvironment, effects;
		var master;
		server = server ? Server.default;

		^this.newCopyArgs(server, rawSignal, proximityDetection, soundEnvironment, effects).init;
	}

	init {
		this.prInitializeNodes;
	}

	prInitializeNodes {
		"init Nodes".debug(1, this);
		rawSignal = rawSignal ?? { SentireSignal.new(server: server) };
		proximityDetection = proximityDetection ?? { SentireProximityDetection.new(server: server) };

		// default sound environment for tests
		// TODO: define SentireSoundEnvironment.default
		soundEnvironment = soundEnvironment ?? { SentireSoundEnvironment.default };

		effects = effects ?? { SentireEffects.new(server) };

		master = NodeProxy.audio(server, 2);
		master.source = { arg amp = 1; \in.ar(0!2) * amp };

		chain = NodeChain(server);
		"Add rawSignal to chain".debug(1, this);
		chain.addAfter(\rawSignal, rawSignal);
		"Add proximityDetection to chain".debug(1, this);
		chain.addAfter(\proximityDetection, proximityDetection);
		"Add soundEnvironment to chain".debug(1, this);
		chain.addAfter(\soundEnvironment, soundEnvironment);
		"Add effects to chain".debug(1, this);
		chain.addAfter(\effects, effects);
		"Add master to chain".debug(1, this);
		chain.addAfter(\master, master);
		"...done".debug(1, this);
	}

	rawSignal_ { arg nodeProxy;
		chain.replace(\rawSignal, nodeProxy);
		rawSignal = nodeProxy;
	}

	proximityDetection_ { arg nodeProxy;
		chain.replace(\proximityDetection, nodeProxy);
		proximityDetection = nodeProxy;
	}

	soundEnvironment_ { arg nodeProxy;
		chain.replace(\soundEnvironment, nodeProxy);
		soundEnvironment = nodeProxy;
	}

	master_ { arg nodeProxy;
		chain.replace(\master, nodeProxy);
		master = nodeProxy;
	}

	effects_ { arg nodeProxy;
		chain.replace(\effects, nodeProxy);
		effects = nodeProxy;
	}

	play { arg ...args;
		chain.play(*args);
	}

	playN { arg ...args;
		chain.playN(*args);
	}

	stop { arg ...args;
		chain.stop(*args);
	}

	scope { arg ...args;
		chain.scope(*args);
	}

	clear {
		chain.clear;
		rawSignal.clear;
		proximityDetection.clear;
		soundEnvironment.clear;
		effects.clear;
		master.clear;
	}
}

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
// TODO:
// define effects and control for effects

SentireEffects : SentireProxyInterface {
	var server, proxyChain, <knownSpecs;
	var wetDrySpecNameMap;

	*new { arg server;
		server = server ? Server.default;
		^this.newCopyArgs(nil, server).init;
	}

	init {
		wetDrySpecNameMap = ();
		this.prSetupProxyChain;
		this.prCollectSpecs;

		proxy = proxyChain.proxy;
		proxy.addDependant(this);
	}

	guiClass {
		^SentireEffectsGui;
	}

	get { arg slotName;
		slotName = wetDrySpecNameMap[slotName] ? slotName;
		^proxy.get(slotName);
	}

	set { arg slotName, value;
		slotName = wetDrySpecNameMap[slotName] ? slotName;
		^proxyChain.set(slotName, value);
	}

	clear {
		proxyChain.clear;
	}

	prCollectSpecs {
		var specs;
		// collect all known specs
		proxyChain.slotNames.collect { arg slotName;
			specs = ProxyChain.sourceDicts[slotName].specs;
		}.do { arg slotSpecs;
			if (slotSpecs.notNil) {
				specs = specs ++ slotSpecs;
			};
		};
		specs = specs ++ this.prGetWetDrySpecs;
		knownSpecs = specs;
	}

	prGetWetDrySpecs {
		var wetDrySpecs = ();
		proxyChain.slotNames.do { arg slotName;
			var slotWetName = (slotName ++ '_wet').asSymbol;

			// assume we only user \filter effects for now
			wetDrySpecNameMap[slotWetName] = (\wet ++ proxyChain.slotNames.indexOf(slotName)).asSymbol;
			wetDrySpecs[slotWetName] = \amp.asSpec;
		};
		^wetDrySpecs;
	}

	prSetupProxyChain {
		var effectsInProxy = NodeProxy.audio(server, 2);
		effectsInProxy.source = {\in.ar(0!2)};

		ProxyChain.add3(\compressor,
			\filter -> { arg in, thresh = 0.25, slopeAbove = 0.25, attack = 0.01, release = 0.1, makeup = 2;
				Compander.ar(in, in, thresh, 1, slopeAbove, attack, release, makeup);
			}, 1, [
				\thresh, [0.0, 1.0, \lin],
				\slopeAbove, [0.01, 1.0, \lin],
				\makeup, [1, 120, \exp],
				\attack, [0.01, 1.0, \lin],
				\release, [0.01, 1.0, \lin],
		]);

		ProxyChain.add3(\GVerb,
			\filter -> { arg in;
				GVerb.ar(in, 100);
		}, 1);

		ProxyChain.add3(\limiter,
			\filter -> { arg in, l_level = 0.966;
				Limiter.ar(in, l_level)
		}, 1, [\l_level, [0.0, 1.0, \lin]]);

		proxyChain = ProxyChain.from(effectsInProxy, [\compressor, \GVerb, \limiter]);
		proxyChain.add(\compressor, 1);
		proxyChain.add(\GVerb, 0);
		proxyChain.add(\limiter, 1);

	}

	update { arg ...args;
		// inspect the changes and fixes the name of the wetDry controls
		// to a better readable one
		if (args[1] == \set and: { wetDrySpecNameMap.invert[args[2][0]].notNil }) {
			args[2][0] = wetDrySpecNameMap.invert[args[2][0]];
		};
		this.changed(*args);
	}
}
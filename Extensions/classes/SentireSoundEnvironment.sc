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
SentireSoundSource {
	classvar <all;
	var <>source;

	*initClass {
		all = ();
	}

	*add { arg key, source;
		all[key] = source;
	}

	*remove { arg key;
		all[key] = nil;
	}

	*get { arg ... args;
		^all[args[0]].value(*args[1..]);
	}
}

SentireSoundEnvironment : SentireProxyInterface {
	classvar <all;
	var <key, <source, <touchSource, server, numChannels, mappingNodes, mappingScale, lastRouted;
	var touchProxy, continuousProxy, <mapping;

	*default {
		^SentireSoundEnvironment.new(\default, {\in.ar(0!2) * Silent.ar}, nil, (), Server.default, 2);
	}

	*new { arg key, source, touchSource, mapping, server = nil, numChannels = 2;
		all = all ? ();

		if (all[key].notNil) {
			if (source.notNil) {
				all[key].prSetContinuousProxy(source);
			};
			if (touchSource.notNil) {
				all[key].prSetTouchProxy(touchSource);
			};
			if (mapping.notNil) {
				all[key].mapping = mapping;
			};
			^all[key];
		};

		server = server ? Server.default;

		^this.newCopyArgs(nil, key, source, touchSource, server, numChannels).init(mapping);
	}

	init { arg mapping;
		all[key] = this;
		this.class.changed;

		mappingScale = 1;

		continuousProxy = NodeProxy.audio(server, numChannels);
		continuousProxy.awake = false;
		continuousProxy.source = source;

		touchProxy = NodeProxy.audio(server, numChannels);
		touchProxy.awake = false;
		this.touchSource = touchSource;

		proxy = NodeProxy.audio(server, numChannels);
		proxy.awake = false;
		proxy.source = { Mix.ar([continuousProxy.ar(numChannels), touchProxy.ar(numChannels)])};

		this.mapping = mapping;

		ServerQuit.add({this.clear}, server);
	}

	prSetContinuousProxy { arg source;
		continuousProxy.source = source;
	}

	prSetTouchProxy { arg source;
		touchProxy.source = source;
	}

	prGetProxies {
		^[continuousProxy, touchProxy, proxy];
	}

	run {
		this.prGetProxies.do { arg aProxy;
			aProxy.awake = true;
			aProxy.source = aProxy.source;
		};

		// init mapping
		//this.mapping = mapping;
	}

	end {
		//this.prEndMappingNodes;

		this.prGetProxies.do { arg aProxy;
			aProxy.awake = false;
			aProxy.end;
		};
	}

	touchSource_ { arg source;
		touchSource = source;
		touchProxy.source = this.prCreateTouchProxySource(touchSource);
	}

	prCreateTouchProxySource { arg sound;
		if (sound.isNil) {
			^nil;
		};

		^{
			var n_voices = 5, max_voice_time=120;
			var times = LocalIn.ar(n_voices);
			// Latch returns 0 by default before the first trig, that is why we
			// sum 1 to the index and then subtract.
			var voice = (Latch.ar(ArrayMax.ar(times)[1] + 1, \touch.ar(0))) - 1;
			var trigs = n_voices.collect {|idx|
				BinaryOpUGen('==', voice, idx);
			};

			var voices = n_voices.collect {|idx|
				sound.value( trigs[idx] );
			};

			LocalOut.ar(
				n_voices.collect {|idx|
					EnvGen.ar(Env.new([1,0,1],[0,max_voice_time]), trigs[idx]);
			});
			Mix.ar(voices);
		};
	}

	clear {
		proxy.clear;
		continuousProxy.clear;
		touchProxy.clear;
		this.prEndMappingNodes(true);
		all[key] = nil;
	}

	prEndMappingNodes { arg clear = false;
		if (mapping.isNil) { ^this };

		mapping.keysValuesDo { arg key, map;
			if(map.class == SentireEnvelope) {
				map.end;
				if (clear) { map.clear };
			} {
				if (mappingNodes.notNil) {
					mappingNodes[key].end;
					if (clear) { mappingNodes[key].clear };
				};
			};
		};
	}

	mapping_ { arg value;
		this.prEndMappingNodes;

		mapping = value;
		this.prSetMappingNodes;
		this.mappingScale = mappingScale;
		this.set(\in, lastRouted);
	}

	mappingScale_ { arg value;
		mappingScale = value;
		if (mapping.notNil) {
			mapping.values.do { arg map;
				if (map.class == SentireEnvelope) {
					map.scale = value;
				};
			};
		};
	}

	prSetMappingNodes {
		mappingNodes = mappingNodes ? ();

		if (mapping.isNil) { ^this };

		mapping.keysValuesDo { arg key, map;
			if (map.class == SentireEnvelope) {
				mappingNodes[key] = map.asNodeProxy;

			} {
				mappingNodes[key] = mappingNodes[key] ?? { NodeProxy.audio(server, 1) };
				mappingNodes[key].source = map;
			};

			continuousProxy.set(key, mappingNodes[key]);
			touchProxy.set(key, mappingNodes[key]);
		};
	}

	orderNodes { arg ...nodes;
		this.nodeOrder[0].orderNodes(*(this.nodeOrder[1..] ++ nodes));
	}

	nodeOrder {
		if (mappingNodes.notNil) {
			^mappingNodes.values.add(continuousProxy).add(touchProxy).add(proxy);
		} {
			^[continuousProxy, touchProxy, proxy];
		};
	}

	set { arg key = \in, proxyNode;
		this.perform('<<>', proxyNode, key);
	}

	<<> { arg proxyNode, key = \in;
		lastRouted = proxyNode;

		if (mappingNodes.isNil) {
			^this;
		};

		mappingNodes.keysValuesDo { arg mapKey, node;
			if (mapping[mapKey].isKindOf(SentireEnvelope)) {
				node.set(key, proxyNode);
			} {
				if (proxyNode.isKindOf(SentireProximityDetectionInterface)) {
					node.set(\touch, proxyNode.touch);
				} {
					node.set(key, proxyNode);
				}
			};
		};
		if (proxyNode.isKindOf(SentireProximityDetectionInterface)) {
			touchProxy.set(\touch, proxyNode.touch);
		};
	}
}

SentireSoundEnvironmentSelector {
	var selectAction, <selected;

	*new { arg selectAction;
		^this.newCopyArgs(selectAction).init;
	}

	init {
		selected = nil;
		SentireSoundEnvironment.addDependant(this);
	}

	guiClass {
		^SentireSoundEnvironmentSelectorGui;
	}

	all {
		if (SentireSoundEnvironment.all.notNil) {
			^SentireSoundEnvironment.all.keys.asArray.sort;
		}
		^[];
	}

	update {
		this.changed;
	}

	selected_ { arg key;
		if (SentireSoundEnvironment.all[key].isNil) {
			^Error("Invalid SentireSoundEnvironment '%'.".format(key)).throw;
		};

		selected = key;
		selectAction.value(SentireSoundEnvironment(selected));
		this.changed;
	}
}

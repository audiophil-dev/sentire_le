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
SentireProxyInterface {
	var <>proxy;

	ar { arg numChannels, offset = 0, clip = 'wrap';
		^proxy.ar(numChannels, offset, clip);
	}

	kr { arg numChannels, offset = 0, clip = 'wrap';
		^proxy.kr(numChannels, offset, clip);
	}

	numChannels {
		^proxy.numChannels;
	}

	<<> { arg proxyNode, key = \in;
		proxy.perform('<<>', proxyNode, key);
	}

	mold { arg numChannels, rate, argReshaping, fadeTime;
		^proxy.mold(numChannels, rate, argReshaping, fadeTime);
	}

	rate {
		^proxy.rate;
	}

	initBus { arg rate, numChannels;
		^proxy.initBus(rate, numChannels);
	}

	scope { arg bufsize = 4096, zoom;
		^proxy.scope(bufsize, zoom);
	}

	play { arg out;
		^proxy.play(out);
	}

	group {
		^proxy.group;
	}

	nodeID {
		^proxy.nodeID;
	}

	set { arg ... args;
		^proxy.set(*args);
	}

	asOSCArgEmbeddedArray {
		^proxy.asOSCArgEmbeddedArray;
	}

	orderNodes { arg ...nodes;
		^proxy.orderNodes(*nodes);
	}

	server {
		^proxy.server;
	}

	clear {
		^proxy.clear;
	}

	end {
		^proxy.end;
	}
}

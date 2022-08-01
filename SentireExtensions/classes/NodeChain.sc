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
NodeChain : SentireProxyInterface {
	var server, numChannels, <chainHead, <chainTail, proxyNodes, <>inputProxy;

	*new { arg server, numChannels=2;
		server = server ? Server.default;

		^super.new().initNodeChain(server, numChannels);
	}

	initNodeChain { arg myServer, myNumChannels;
		server = myServer;
		numChannels = myNumChannels;
		proxyNodes = ();

		proxy = NodeProxy.audio(server, numChannels);
		proxy.source = { \in.ar(0!numChannels) };

		inputProxy = NodeProxy.audio(server, numChannels);
		inputProxy.source = { \in.ar(0!numChannels) };

		ServerQuit.add({ this.clear }, server);
	}

	set { arg ... args;
		var newArgs = [];
		var in;
		args.pairsDo { arg key, value;
			// route any \in setting to our first proxy
			if (key == \in) {
				inputProxy.set(\in, value);
			} {
				newArgs = newArgs.add(key);
				newArgs = newArgs.add(value);
			};
		};

		if (newArgs.notEmpty) {
			super.set(*newArgs);
		};
	}

	clear {
		proxyNodes.keys.do {|key|
			if (proxyNodes[key] != chainHead) {
				// dont mess with the input of the first node of the chain
				proxyNodes[key].obj.set(\in, nil);
			};
			proxyNodes[key].next = nil;
			proxyNodes[key].prev = nil;
			proxyNodes[key] = nil;
		};
		proxyNodes = ();
		proxy.clear;
	}

	play { arg out;
		proxy.play(out);
	}

	playN { arg outs, amps, ins, vol, fadeTime, group, addAction;
		proxy.playN(outs, amps, ins, vol, fadeTime, group, addAction);
	}

	stop { arg fadeTime, reset=false;
		proxy.stop(fadeTime, reset);
	}

	scope { arg bufsize = 4096, zoom;
		proxy.scope(bufsize, zoom);
	}

	addAfter { arg key, proxy, after = nil;
		// adds proxy to the chain after the `after` key
		// if `after` is nil adds to the tail (last proxy) of the chain
		var newNode;
		("Adding Node:" + key + "after Node: " + after).debug(1, this);

		newNode = this.prUpdateNode(key, proxy);

		if (chainTail.isNil) {
			// chain is empty still;
			chainTail = newNode;
			chainHead = chainTail;
		} {
			if (after.isNil) {
				after = chainTail;
				chainTail = newNode;
			} {
				after = proxyNodes[after];
			};

			if (after.next.notNil) {
				newNode.next = after.next;
			} {
				chainTail = newNode;
			};

			after.next = newNode;
			newNode.prev = after;
		};

		this.prUpdateNodeRouting(newNode);
		this.prUpdateServerNodeOrder;
	}

	addBefore { arg key, proxy, before = nil;
		// adds proxy to the chain before the `before` key
		// if `before` is nil adds to the head (first proxy) of the chain.
		var newNode = this.prUpdateNode(key, proxy);

		if (chainHead.isNil) {
			// chain is empty still;
			chainTail = newNode;
			chainHead = chainTail;
		} {

			if (before.isNil) {
				before = chainHead;
				chainHead = newNode;
			} {
				before = proxyNodes[before];
			};

			if (before.prev.notNil) {
				newNode.prev = before.prev;
			} {
				chainHead = newNode;
			};

			before.prev = newNode;
			newNode.next = before;
		};

		this.prUpdateNodeRouting(newNode);
		this.prUpdateServerNodeOrder;
	}

	get { arg key;
		^proxyNodes[key].obj;
	}

	replace { arg key, proxy;
		var node = proxyNodes[key];
		var prev = node.prev;

		if (prev.notNil) {
			this.addAfter(key, proxy, proxyNodes.invert[prev]);
		} {
			this.addBefore(key, proxy);
		};
	}

	remove { arg key;
		var nodePrev = proxyNodes[key].prev;
		var nodeNext = proxyNodes[key].next;

		proxyNodes[key].obj.set(\in, nil);
		proxyNodes[key].prev = nil;
		proxyNodes[key].next = nil;
		proxyNodes[key] = nil;

		if (nodeNext.notNil) {
			nodeNext.prev = nodePrev;
			if (nodePrev.notNil) {
				nodePrev.next = nodeNext;
				nodeNext.obj.set(\in, nodePrev.obj);
			} {
				nodeNext.obj.set(\in, nil);
				chainHead = nodeNext;
			};
		} {
			chainTail = nodePrev;
			if (nodePrev.notNil) {
				nodePrev.next = nil;
				proxy.set(\in, nodePrev.obj);
			} {
				proxy.set(\in, nil);
			};
		};

		this.prUpdateServerNodeOrder;
	}

	prUpdateNode { arg key, proxy;
		var node = proxyNodes[key];
		var nodeProxy = proxy;
		("Update Node:" + key).debug(1, this);
		("proxy.numChannels:" + proxy.numChannels).debug(1, this);
		("this.proxy.numChannels:" + this.proxy.numChannels).debug(1, this);

		if (node.isNil) {
			// node with that key was not added yet, create a new one
			node = LinkedListNode.new;
			proxyNodes[key] = node;
		} {
			// node already exists, removes it first...
			this.remove(key);
			proxyNodes[key] = node;
		};

		if (proxy.numChannels < this.proxy.numChannels) {
			proxy.mold(this.proxy.numChannels);
		};

		node.obj = nodeProxy;
		^node
	}

	prUpdateNodeRouting { arg node;
		// order goes as: head <>> ... <>> tail
		// or: tail <<> ... <<> head

		if (node.prev.notNil) {
			node.obj.set(\in, node.prev.obj);
		} {
			// This is the head node, route the input proxy to it
			node.obj.set(\in, inputProxy);
		};

		if (node.next.notNil) {
			node.next.obj.set(\in, node.obj);
		} {
			// This is the tail node, route it to the output proxy
			proxy.set(\in, node.obj);
		};
	}

	prUpdateServerNodeOrder {
		var list, node;

		if (chainHead.isNil) {
			// chain is empty
			^this;
		};

		node = chainHead.next;
		while({ node.notNil }, {
			if (node.obj.respondsTo(\nodeOrder)) {
				// support for SentireSoundEnvironment
				list = list ++ node.obj.nodeOrder;
			} {
				list = list.add(node.obj);
			};
			node = node.next;
		});
		list = list.add(proxy);
		{
			server.sync;
			chainHead.obj.orderNodes(*list);
		}.fork;
	}

}
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
// Example for NodeChain

s.boot;

// Show server tree:

s.plotTree

// Some ndefs

(
Ndef(\input, {
	SinOsc.ar(330)!2;
});


Ndef(\tremolo, {
	\in.ar(0!2) * SinOsc.ar(2).range(0,1);
});


Ndef(\sawMix, {
	\in.ar(0!2) + LFSaw.ar(230);
});
)

// create NodeChain
~chain = NodeChain();

// add to the begining of the chain
~chain.addAfter(\aKeyName, Ndef(\input));

// add in the end of the chain, see on the node tree that the group order changes
~chain.addAfter(\mySaw, Ndef(\sawMix));

// now add a Ndef between the input and the sawMix:

~chain.addAfter(\myTremoloFx, Ndef(\tremolo), after: \aKeyName);

// you can add before or after any node, so the above is the same as:
// ~chain.addBefore(\myTremoloFx, Ndef(\tremolo), before: \mySaw);

// if after: or before: is nil, it adds to the beginning or the end of the chain,
// sowe can add an Ndef to the end of the chain to be the final mix:

Ndef(\endOfChain, {
	\in.ar(0!2);
});

~chain.addAfter(\endOfChain, Ndef(\endOfChain));

// now we can scope any point of the chain:

Ndef(\input).scope;

Ndef(\sawMix).scope;

Ndef(\endOfChain).scope;

~chain.scope;

// try removing the tremolo while seeing the scope of endOfChain:
~chain.remove(\myTremoloFx);

// now add the tremolo AFTER the saw:
~chain.addAfter(\myTremoloFx, Ndef(\tremolo), \mySaw);


// add a ProxyChain to the NodeChain

ProxyChain.add(\tremolo, \filterIn -> {
	\in.ar(0!2) * SinOsc.ar(0.5).range(0,1)
});

p = ProxyChain(\ch, [\tremolo])
p.add(\tremolo, 1)

~chain.addAfter(\ch, p.proxy, \myTremoloFx)

// you can remove the original tremolo:
~chain.remove(\myTremoloFx)

// or the ProxyChain:
~chain.remove(\ch)

// and to have access to the output proxy of the NodeChain (for recording, monitoring, etc):
~chain.proxy;

// NodeChain has the .play , .stop, .playN or .scope methods as well
~chain.play;

// it has a clear method
// but notice that it doesn't clear the nodes you added to the chain
~chain.clear;

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
SentireMaster {
	var signalFlow, <outputChannels, <volume, <bus;

	*new { arg signalFlow, outputChannels;
		^this.newCopyArgs(signalFlow, outputChannels).init;
	}

	init {
		this.volume = 1;
		bus = Bus.new('audio', index: outputChannels[0], numChannels: outputChannels.size);
	}

	volume_ {arg amp;
		signalFlow.master.set(\amp, amp);
		volume = amp;
		this.changed;
	}

	guiClass {
		^SentireMasterGui;
	}
}
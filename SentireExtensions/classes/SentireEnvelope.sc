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
SentireEnvelope {
	var <scale, server, ranges, warp, lastValue, proxy = nil;

	*new { arg middleRange, values, curves, scale = 1, flex = nil, server = nil;
		server = server ? Server.default;
		if (flex.isNil) {
			flex = [nil, nil]
		} {
			if (flex.isArray.not) {
				flex = [flex, flex];
			}
		};
		^super.newCopyArgs(scale, server).init(middleRange, values, curves, flex);
	}

	init { arg middleRange, values, curves, flex;
		ranges = [
			[0.0, middleRange[0], values[0], curves[0], flex[0]],
			[middleRange[0], middleRange[1], values[1], curves[1], \flex],
			[middleRange[1], 1.0, values[2], curves[2], flex[1]],
		];
		lastValue = values.last;
		warp = this.prGetSpec;
	}

	map { arg val;
		^warp.map(val);
	}

	clear {
		proxy.clear;
	}

	plot { arg name;
		^warp.env.plot(name: name);
	}

	scale_ { arg val;
		if (val != scale) {
			scale = val;
			warp = this.prGetSpec;
			this.prResetProxy;
		};
	}

	prResetProxy {
		proxy = proxy ?? { NodeProxy.audio(server, 1) };
		proxy.source = { IEnvGen.ar(warp.env, \in.ar(0)) };
	}

	end {
		proxy.end;
	}

	asNodeProxy {
		if (proxy.isNil) {
			this.prResetProxy;
		};
		^proxy;
	}

	prGetSpec {
		var realRanges = this.prDefineRanges;
		^SentireSegWarp(realRanges.collect {|range| [range[2], range[0], range[3]]});
	}

	prScaleRange { arg range;
		var newRange = range.deepCopy;
		var rangeSize = newRange[1] - newRange[0];
		rangeSize = rangeSize / this.scale;
		if (newRange[0] == 0) {
			newRange[1] = rangeSize;
		} {
			if (newRange[1] == 1) {
				newRange[0] = newRange[1] - rangeSize;
			} {
				newRange[0] = newRange[0] + (rangeSize/2);
				newRange[1] = newRange[1] - (rangeSize/2);
			};
		};

		if (newRange[0] < 0) {
			newRange[1] = newRange[1] + newRange[0].neg;
		} {
			if (newRange[1] > 1) {
				newRange[0] = newRange[0] - (newRange[1] - 1);
			};
		};
		newRange[0] = newRange[0].clip(0,1);
		newRange[1] = newRange[1].clip(0,1);
		^newRange;
	}

	prScaleRanges {
		var scaledRanges = [];
		var copyRanges = ranges.deepCopy;
		copyRanges.sort{|a,b| a[0] < b[0]};
		copyRanges.do {arg range;
			if (range[4] != \flex) {
				scaledRanges = scaledRanges.add(this.prScaleRange(range));
			} {
				scaledRanges = scaledRanges.add(range);
			};
		};
		^scaledRanges;
	}

	prDefineRange { arg range, previous;
		if (range[0] < previous[1]) {
			// ranges overlap, clips one so they fit
			if (previous[4] == \flex) {
				previous[1] = range[0];
				^range;
			} {
				range[0] = previous[1];
				^range;
			};
		} {
			if (range[0] > previous[1]) {
				// there is a gap between ranges, makes one range longer to connect them
				if (previous[4] == \flex) {
					previous[1] = range[0];
					^range;
				} {
					^[previous[1], range[1], range[2], range[3], range[4]];
				};
			} {
				^range;
			};
		};
	}

	prDefineRanges {
		var outcome = [];
		var scaledRanges;
		scaledRanges = this.prScaleRanges;
		scaledRanges.do {arg range, i;
			if (i > 0) {
				outcome = outcome.add(this.prDefineRange(range, outcome.last));
			} {
				outcome = outcome.add(range);
			};
		};
		outcome = outcome.add([1.0, 1.0, lastValue, \lin]);
		^outcome;
	}
}

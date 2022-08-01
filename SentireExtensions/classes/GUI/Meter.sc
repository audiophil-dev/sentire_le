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
Meter : View {
	var view, meterViews, vertLayout, horzLayout;
	var meters;
	var responder;
	var dBLow = -80;
	var updateFreq = 10;
	var startResponderFunc, serverCleanupFuncs, synthFunc;
	var numChannels, bus, server;
	var <showTitle;
	var <id;

	classvar all;

	*new { arg parent, bounds, bus = Bus.new('audio', index: 0, numChannels: 2), numChannels = 1, server = Server.default;
		^super.new(parent, bounds).init(parent, bounds, bus, numChannels, server);
	}

	init {arg parent, bounds, meteredBus, numberOfChannels, serverInstance;
		id = Meter.prNewId;
		server = serverInstance;
		numChannels = numberOfChannels;
		bus = meteredBus;
		showTitle = false;
		this.prInitViews;
		this.prSetSynthFunc;
		startResponderFunc = {this.prStartResponders};
		this.start;
	}

	*prNewId {
		var newId;
		all = all ? ();
		newId = if (all.size == 0, {0}, {
			var sortedKeyValues = all.asSortedArray;
			sortedKeyValues.last[0];
			sortedKeyValues.last[0] + 1;
		});
		all.put(newId, this);
		^newId;
	}

	showTitle_ {arg bool;
		showTitle = bool;
		this.prInitViews;
	}

	prInitViews {
		var titleText;
		vertLayout = VLayout();
		horzLayout = HLayout();

		if (showTitle, {
			titleText = StaticText(this).string_("Gain");
			vertLayout.add(titleText);
			vertLayout.add(horzLayout);
			this.layout = vertLayout;
		}, {
			this.layout = horzLayout;
		});

		this.prInitLevelIndicators;
		this.prInitdBScale;

		vertLayout.setStretch(0, 0);
		vertLayout.setStretch(1, 100);

		this.onClose_({this.stop});
	}

	prInitLevelIndicators {
		var meterWidth = 15;
		meters = Array.fill( numChannels, { arg i;
			var view = LevelIndicator( nil, Rect(0,0,meterWidth,180) )
			.warning_(0.9)
			.critical_(1.0)
			.drawsPeak_(true)
			.numTicks_(9)
			.numMajorTicks_(3)
			.minHeight_(40)
			.maxWidth_(40);
			horzLayout.add(view);
			view;
		});
	}

	prInitdBScale {
		var view = UserView(nil, Rect(0, 0, 20, 195));
		horzLayout.add(view);
		view.drawFunc = {
			Pen.font = Font("Helvetica", 10, true);
			Pen.stringCenteredIn("0", Rect(0, 0, 20, 12));
			Pen.stringCenteredIn(dBLow.asString, Rect(0, view.bounds.height - 15, 20, 12));
		};
	}

	prSetSynthFunc {

		synthFunc = {

			server.bind( {
				var synth;
				if(numChannels > 0, {
					synth = Ndef(this.prStringId.asSymbol, {
						var in = InFeedback.ar(bus.index, numChannels);
						SendPeakRMS.kr(in, updateFreq, 3, this.prOscPath)
					});
				});

				if (serverCleanupFuncs.isNil) {
					serverCleanupFuncs = IdentityDictionary.new;
				};
				serverCleanupFuncs.put(server, {
					"Server Cleanup".debug(4, this);
					synth.clear;
					ServerTree.remove(synthFunc, server);
				});
			});
		};
	}

	prOscPath {
		^"/" ++ this.prStringId;
	}

	prStringId {
		^"%_meterLevels_%-%_client_%_".format(this.id, bus.index, bus.index + numChannels - 1, server.clientID);
	}

	prStartResponders {

		if(numChannels > 0) {
			responder = OSCFunc( {|msg|
				{
					try {
						var channelCount = min(msg.size - 3 / 2, numChannels);

						channelCount.do {|channel|
							var baseIndex = 3 + (2*channel);
							var peakLevel = msg.at(baseIndex);
							var rmsValue = msg.at(baseIndex + 1);
							var meter = meters.at(channel);
							if (meter.notNil) {
								if (meter.isClosed.not) {
									meter.peakLevel = peakLevel.ampdb.linlin(dBLow, 0, 0, 1, \min);
									meter.value = rmsValue.ampdb.linlin(dBLow, 0, 0, 1);
								}
							}
						}
					} { |error|
						if(error.isKindOf(PrimitiveFailedError).not) { error.throw }
					};
				}.defer;
			}, (this.prOscPath).asSymbol, server.addr).fix;
		};
	}

	start {
		if(meterViews.isNil) {
			meterViews = IdentityDictionary.new;
		};
		if(meterViews[server].isNil) {
			meterViews.put(server, List());
		};
		if(meterViews[server].size == 0) {
			ServerTree.add(synthFunc, server);
			if(server.serverRunning, synthFunc); // otherwise starts when booted
		};
		meterViews[server].add(this);
		if (server.serverRunning) {
			this.prStartResponders
		} {
			ServerBoot.add (startResponderFunc, server)
		}
	}

	stop {
		all.removeAt(this.id);
		meterViews[server].remove(this);
		if(serverCleanupFuncs.notNil) {
			serverCleanupFuncs[server].value;
			serverCleanupFuncs.removeAt(server);
		};

		if (numChannels > 0, { responder.free; });
		ServerBoot.remove(startResponderFunc, server)
	}

}
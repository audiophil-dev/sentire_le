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
SentireGui : SentireAbstractGui {
	var nothing, numberBoxFontBig, bigNumberBoxMaxWidth, bigNumberBoxMinWidth;
	var bigNumberBoxMinHeight, bigNumberBoxMaxHeight, textLabelMinWidth, textLabelMaxHeight;
	var rangeSlider, rangeNumberBox;

	guiBody { arg parent, bounds;
		var firstRow;
		this.layout = VLayout();

		numberBoxFontBig = Font("Monaco", 24);
		bigNumberBoxMinWidth = 80;
		bigNumberBoxMaxWidth = 90;
		bigNumberBoxMinHeight = 40;
		bigNumberBoxMaxHeight = 50;
		textLabelMaxHeight = 20;
		textLabelMinWidth = 100;

		firstRow = View(this);
		firstRow.layout = HLayout();
		model.soundEnvironmentSelector.gui(firstRow);
		model.master.gui(firstRow);
		this.prMakeRangeControl;

		this.prMakeScrollingPlotView;
		this.prMakeExternalWindowControls;
		this.onClose = {model.clear};
		^this;
	}

	update {
		AppClock.sched(0, {
			rangeSlider.value = model.rangeScale;
			rangeNumberBox.value = model.rangeScale;
		});
	}

	prMakeRangeControl {
		var rangeView, sliderAndBoxView;

		rangeView = View(this);
		rangeView.layout = VLayout();

		StaticText(rangeView).string_("Range").font_(Font.default.boldVariant);

		sliderAndBoxView = View(rangeView);
		sliderAndBoxView.layout = HLayout();


		rangeSlider = Slider.new(sliderAndBoxView).orientation_(\horizontal);
		rangeSlider.minHeight_(bigNumberBoxMinHeight);

		rangeSlider.action = {|slider|
			model.rangeScale = slider.value;
		};

		rangeNumberBox = NumberBox(sliderAndBoxView)
		.minDecimals_(3)
		.step_(0.001)
		.scroll_step_(0.001)
		.align_(\center)
		.clipLo_(0)
		.clipHi_(1)
		.minWidth_(bigNumberBoxMinWidth)
		.maxWidth_(bigNumberBoxMaxWidth)
		.minHeight_(bigNumberBoxMinHeight)
		.maxHeight_(bigNumberBoxMaxHeight)
		.font_(numberBoxFontBig).refresh;

		rangeNumberBox.action = {|box|
			model.rangeScale = box.value;
		};

	}

	prMakeScrollingPlotView {
		var view, plot, pollingFunction;
		view = View(this, [0, 0, 100 ,100]);
		view.layout = VLayout();
		pollingFunction = NodeProxy.control(model.server, 1);
		pollingFunction.source = { A2K.kr(model.signalFlow.proximityDetection.proxy.ar(1)) };

		plot = ScrollingPlot.new( {
			[[pollingFunction.bus.getSynchronous]]
		},
		200,  //nr of points to plot
		2,    // channels
		0.05, // plot dtime
		1,    // write every n data points

		view
		).start;

		/* Set the Specs for the plots, so we get nice labels */
		plot.setSpecs(
			[[0.0, 1.1, \lin, 0, 0, " Proximity"]]
		);

		this.layout.setStretch(view, 2);
	}

	prMakeExternalWindowControls {
		var view = View(this), masterFxWindow, masterFxButton;
		view.layout = HLayout();

		/* Add s.meter button*/
		Button.new(view)
		.valueAction_(1)
		.maxWidth_(115)
		.string_("Server Meter")
		.action_({
			model.server.meter;
		});

		/* Add master fx control*/
		masterFxButton = Button.new(view)
		.valueAction_(1)
		.maxWidth_(115)
		.string_("Master effects")
		.action_({ arg butt;
			if(butt.value == 1, {
				masterFxWindow = Window('Master Effects');
				model.signalFlow.effects.gui(masterFxWindow);
				masterFxWindow.onClose = { masterFxButton.value = 0; };
				masterFxWindow.front;
			}, {
				masterFxWindow.close;
			})
		});
		masterFxButton.states_([
			["Master effects"],
			["Master effects", masterFxButton.palette.highlightText , masterFxButton.palette.highlight]
		]);

	}
}

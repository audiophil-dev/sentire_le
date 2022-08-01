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
SentireEffectsGui : SentireAbstractGui {

	var controls, textLabelMinWidth = 80;
	var guifiedControlSpecs;

	guiBody { arg parent, bounds;
		guifiedControlSpecs = (
			thresh: [-120, 0, \db].asSpec,
			slopeAbove: \db.asSpec,
			l_level: \db.asSpec,
		);

		this.layout = VLayout();
		controls = ();
		StaticText(this).string_("Compressor").minWidth_(textLabelMinWidth).align_(\center);
		this.prMakeControl(\makeup, "Makeup (dB)");
		this.prMakeControl( \thresh, "Threshold (dB)");
		this.prMakeControl( \slopeAbove, "Slope Above (dB)");

		StaticText(this).string_("Reverb").minWidth_(textLabelMinWidth).align_(\center);
		this.prMakeControl( \GVerb_wet, "Dry/Wet");

		StaticText(this).string_("Limiter").minWidth_(textLabelMinWidth).align_(\center);
		this.prMakeControl( \l_level, "Threshold (dB)");



		this.update;
		^this;
	}

	guifySpec { arg slotName, value;
		if (guifiedControlSpecs[slotName].notNil) {
			^value.ampdb;
		};

		^value;
	}

	unguifySpec { arg slotName, value;
		if (guifiedControlSpecs[slotName].notNil) {
			^value.dbamp;
		};

		^value;
	}

	update {
		AppClock.sched(0, {
			controls.keysValuesDo {arg key, control;
				control.value(model.get(key));
			};
		});
	}

	prMakeControl { arg slotName, labelText;
		var view, slider, numberBox, numberBoxFont = Font("Monaco", 14);
		var controlSpec = model.knownSpecs[slotName];
		view = View(this);
		view.layout = HLayout();
		view.layout.margins_([0, 0, 0, 0]);
		StaticText(view).string_(labelText).minWidth_(textLabelMinWidth);
		slider = Slider(view);
		numberBox = NumberBox(view);

		slider.orientation_(\horizontal)
		.minHeight_(15)
		.maxHeight_(20);

		slider.action = {|slider|
			var mapped = controlSpec.map(slider.value);
			model.set(slotName, mapped);
			numberBox.value = this.guifySpec(slotName, slider.value);
		};

		numberBox.minDecimals_(3)
		.step_(0.001)
		.scroll_step_(0.001)
		.maxWidth_(50)
		.minWidth_(50)
		.minHeight_(15)
		.maxHeight_(20)
		.action_({|box|
			slider.valueAction_(this.unguifySpec(slotName, box.value));
		})
		.align_(\center)
		.font_(numberBoxFont).refresh;

		controls[slotName] = { arg val;
			numberBox.value = this.guifySpec(slotName, val);
			slider.value = controlSpec.unmap(val);
		};

		view.layout.setStretch(slider, 10);
		view.layout.setStretch(numberBox, 0);
		view;
	}
}
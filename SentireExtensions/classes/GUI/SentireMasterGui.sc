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
SentireMasterGui : SentireAbstractGui {
	var slider, meter, controlSpec;

	guiBody { arg parent, bounds;
		var containerView, sliderView, sliderViewNested;
		this.layout = VLayout();
		StaticText(this).string_("Master Gain")
			.font_(Font.default.boldVariant);

		containerView = View(this);
		containerView.layout = HLayout();

		//sliderViewNested = View(containerView);
		sliderView = View(containerView);
		sliderView.layout = VLayout();
		slider = Slider(sliderView);
		slider.minWidth = 10;
		slider.maxWidth = 130;

		meter = Meter(containerView, [0, 0, 200, 400], model.bus, 2);
		meter.minWidth = 80;
		meter.maxWidth = 130;

		controlSpec = \db.asSpec;

		slider.action = {|slider|
			var mapped = controlSpec.map(slider.value);
			model.volume = mapped.dbamp;
		};
		slider.value = model.volume;
		^this;
	}

	update {
		AppClock.sched(0, {
			slider.value = controlSpec.unmap(model.volume.ampdb);
		});
	}
}
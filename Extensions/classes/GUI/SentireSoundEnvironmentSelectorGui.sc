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
SentireSoundEnvironmentSelectorGui : SentireAbstractGui {
	var listView;

	guiBody { arg parent, bounds;

		this.layout = VLayout();
		//parent.layout.add(this);

		StaticText(this).string_("Sound Environment")
			.font_(Font.default.boldVariant);

		listView = ListView(this);
		listView.action = { arg selection;
			model.selected = listView.items[selection.value];
		};

		^this;
	}

	update {
		AppClock.sched(0, {
			listView.items_(model.all);
			listView.value = listView.items.find([model.selected]);
		});
	}
}

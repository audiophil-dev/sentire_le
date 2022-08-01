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
SentireAbstractGui : View {
	var <model;

	*new { arg model;
		var new;
		new = super.new;
		new.model_(model);
		^new;
	}

	model_ { arg newModel;
		if(model.notNil, {
			model.removeDependant(this);
			model = newModel;
			model.addDependant(this);
			this.update;
		}, {
			model = newModel;
			model.addDependant(this);
		})
	}

	gui { arg parent, bounds;
		this.guiBody;
		this.guify(parent);
		this.update;
	}

	guify { arg parent, bounds;
		if(bounds.notNil, {
			bounds = bounds.asRect;
		});

		if(parent.isNil, {
			parent = Window(model.asString.copyRange(0,50), bounds);
			parent.layout = VLayout();
			parent.layout.add(this);
			parent.front;
		}, {
			if(parent.layout.isNil, {
				parent.layout = VLayout();
				parent.layout.add(this);
			}, {
				parent.layout.add(this);
			});
			parent = parent.asView();
			parent.front;
		});
	}

	guiBody {
		/* implement this method in SentireGui subclass */
	}

	update {
		/* implement this method in SentireGui subclass */
	}

	viewDidClose {
		model.removeDependant(this);
		model = nil;
		super.viewDidClose;
	}
}
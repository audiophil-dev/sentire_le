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
+Node {
	*orderNodesMsg { arg nodes;
		// Bugfix while its not in the new SC release
		// https://github.com/supercollider/supercollider/pull/5101
		var msg = [19]; // "/n_after"
		nodes.doAdjacentPairs { |first, toMoveAfter|
			msg = msg.add(toMoveAfter.nodeID);
			msg = msg.add(first.nodeID);
		};
		^msg
	}
}
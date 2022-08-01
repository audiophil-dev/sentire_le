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
(
s.waitForBoot {
    n = NodeProxy.audio(s, 1);
	n.source = {SinOsc.ar(SinOsc.ar(0.1).range(100,12000), 0, -10.dbamp) + PinkNoise.ar(0)};
    a = SentireAnalysis('sine', n, 0, 4096, 4);
    t = a.gui
}
)

// Stop the analysis
a.stop

// close the gui
t.close;

// restart analysis
a.spawn

// reopens the gui
a.gui

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
/*  ScrollingPlot
	Fork of the SenseWorld SWPlotterMonitor
	Adds instance variable for a parent window */


ScrollingPlot{

	var <>plotter;
	var <>monitor;

	*new{ |updater,length,nc=1,dt=0.1,skip=1,parent|
		^super.new.init( updater,length,nc,dt,skip,parent);
	}

	init{ |updater,length,nc=1,dt=0.1,skip=1,parent|
		var bounds = Rect(0, 0, 520, 400);
		plotter = Plotter.new( "Plotter Monitor", bounds, parent);
		plotter.value_( updater.value ); // temporary workaround!
		plotter.superpose_( false );
		plotter.findSpecs_( false) ;
		plotter.domainSpecs = [0, length*dt, \lin, 0, 0, " s"].asSpec;
		monitor = SWDataMonitor.new( updater,length,{ |data| plotter.value_( data ) }, nc, dt, skip );
	}

	setSpecs{ |specs|
		plotter.specs_( specs );
		plotter.refresh;
	}

	start{
		if( plotter.parent.isNil ) { plotter.makeWindow };
		monitor.start;
	}

	isPlaying{
		^monitor.isPlaying;
	}

	stop{
		monitor.stop;
	}

	cleanUp{
		this.stop;
		plotter.close;
	}
}



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
+RecNodeProxy {
	openWithCallback { arg argPath, headerFormat="aiff", sampleFormat="int16", callback;
		var msg, n, bundle;
		n = this.numChannels;
		if(server.serverRunning.not, { "server not running".inform; ^this  });
		if(buffer.notNil, { "already recording. use pause/unpause".inform; ^this  });
		path = argPath;
		buffer = Buffer.alloc(server, 65536, n);
		buffer.write(path, headerFormat, sampleFormat, 0, 0, true, {
			if (callback.notNil) { callback.value };
		});

		CmdPeriod.add(this);
	}

	recordNoSchedWithCallback { arg paused=true, callback;
		var bundle, n;
		if(server.serverRunning.not, { "server not running".inform; ^this  });
		if(buffer.isNil, { "not prepared. use open to prepare a file.".inform;  ^this });

		bundle = MixedBundle.new;
		if(this.isPlaying.not, {
			this.wakeUpToBundle(bundle);
		});
		recGroup = Group.basicNew(server);
		bundle.add(recGroup.newMsg(server));
		NodeWatcher.register(recGroup);
		bundle.add([9, "system_diskout_" ++ this.numChannels,
					server.nextNodeID, 1, recGroup.nodeID,
					\i_in, bus.index, \i_bufNum, buffer.bufnum
				]);
		if(paused, { bundle.add(["/n_run", recGroup.nodeID, 0]); });
		Routine.run {
			var condition = Condition.new;
			server.sync(condition, bundle.messages);
			callback.value;
		};
	}
}

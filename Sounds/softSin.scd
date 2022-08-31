(
var notes = [ 74, 70, 67, 61, 60, 58, 54, 48, 47, 46, 43, 41 ];

SentireSoundEnvironment(\softSin,
	source: {
		var signal;
		var amplitudes;
		var rootFrequency;
		var fundamentals, frequencyRatios;
		var frequencyModulation;
		var filterCutoffFrequency, filterCutoffFrequencyModulation;

		rootFrequency = \rootFrequency.ar(notes[0].midicps);

		frequencyRatios = notes/notes.last;//Scale.major.ratios;

		fundamentals = (frequencyRatios * rootFrequency).midicps;

		amplitudes = Array.fill(fundamentals.size, {arg i;
			// map proximity to the fundamental series based on the index of the fundamental
			var iAmplitude = \frequencySeriesPosition.ar(0).linlin(0, 1, 0, fundamentals.size) - i;

			// clip the amplitudes to a range of 0-1, this also guarantees that only certain fundamentals are heard, based on the mapping
			iAmplitude = iAmplitude.clip(0, 1);
			// dampen the amplitudes based on the number of fundamentals
			iAmplitude = iAmplitude * fundamentals.size.reciprocal;
		});


		// create a LFNoise2 oscillator as source for the frequency modulation
		frequencyModulation = LFNoise2.ar(\lfoRate.ar(1), \lfoAmount.ar(0), 1).abs;

		// create non-band-limited triangle oscillators based on the given frequency/amplitude distribution
		signal = LFTri.ar(
			fundamentals * frequencyModulation,
			0,
			amplitudes
		);

		filterCutoffFrequencyModulation = LFCub.kr(1, 0, 0.125, 1);
		filterCutoffFrequency = \filterCutoff.ar(3400) * filterCutoffFrequencyModulation;

		// filter the signal with a resonant low pass filter
		signal = RLPF.ar(signal, filterCutoffFrequency);

		// Downmix the frequency series to a signle channel
		signal = Mix.ar(signal);

		// Add stereo panning and modulation
		signal = Pan2.ar(signal, SinOsc.kr(5));

		// Map signal amplitude to proximity;
		signal = signal * \amplitude.ar(0);

		// lower amplitude
		signal = signal * 0.1;

		// remove DC offset
		signal = LeakDC.ar(signal);
	},
	touchSource: { arg trig;
		var signal, freq;

		freq = \rootFrequency.ar(notes[0].midicps);
		signal = SinOsc.ar(freq, 0, 0.25);
		signal = Splay.ar(signal);
		signal = LeakDC.ar(signal);
		signal = HPF.ar(signal, 40);
		signal * EnvGen.ar(Env.perc(0.001,1), trig);
	},
	mapping: (
		frequencySeriesPosition: SentireEnvelope([0.2, 0.85], [0, 0.4, 0.8, 1.0], [-1, 1, -1], 1, \flex),
		//frequencySeriesPosition: SentireEnvelope([0, 0.4, 0.8, 1.0], [-1, 1, -1], [0.2, 0.85]),
		//amplitude: SentireEnvelope([0, 0.4, 0.8, 1.0], [-1, 1, -1], [0.2, 0.85]),
		amplitude: SentireEnvelope([0.2, 0.85], [0, 0.4, 0.8, 1.0], [-1, 1, -1], 1, \flex),
		//filterCutoff: SentireEnvelope([3100, 2580, 1430, 400], [-1, 2, -1], [0.2, 0.85]),
		filterCutoff: SentireEnvelope([0.2, 0.85], [3100, 2580, 1430, 400], [-1, 2, -1], 1, \flex),
		lfoRate: {
			var elapsedTime = Sweep.ar(\touch.ar(0)) * \touch.ar();
			elapsedTime.linlin(0, 10, 1, 1.1, 'none');
		},

		lfoAmount: {
			var elapsedTime = Sweep.ar(\touch.ar(0)) * \touch.ar();
			elapsedTime.linlin(0, 10, 0, 0.1, 'none');
		},

		rootFrequency: {
			var freq = Demand.ar(
				\touch.ar(1), 0,
				Dseq([Dxrand(notes,3)] ++
					[Ddup(Drand([1,2,3], 1), Drand(notes, 1))], inf));
			Select.ar(freq > 1.0, [K2A.ar(notes[0]), freq]);
		},


	)
);
)
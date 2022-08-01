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
TestSentireEnvelope : UnitTest {
	var <>env;

	setUp {
		env = SentireEnvelope.new([0.1,0.9], [250, 1250, 2500, 6200], [\lin, \lin, \lin], 1);
	}

	tearDown {
	}

	test_linear_mapping {
		this.assertFloatEquals(env.map(0.0), 250.0, "0.0 should map to 250");
		this.assertFloatEquals(env.map(0.05), 750.0, "0.05 should map to 750");
		this.assertFloatEquals(env.map(0.1), 1250.0, "0.1 should map to 250");
		this.assertFloatEquals(env.map(0.5), 1875.0, "0.5 should map to 1875");
		this.assertFloatEquals(env.map(0.9), 2500.0, "0.9 should map to 2500");
		this.assertFloatEquals(env.map(0.95), 4350.0, "0.95 should map to 4350");
		this.assertFloatEquals(env.map(1), 6200.0, "1 should map to 6200");
	}

	test_linear_mapping_scaled {
		env.scale = 2;
		this.assertFloatEquals(env.map(0.0), 250.0, "0.0 should map to 250");
		this.assertFloatEquals(env.map(0.05), 1250.0, "0.05 should map to 1250");
		this.assertFloatEquals(env.map(0.1), 1319.44444444444, "0.1 should map to 1319.444");
		this.assertFloatEquals(env.map(0.5), 1875.0, "0.5 should map to 1875");
		this.assertFloatEquals(env.map(0.9), 2430.55555555555, "0.9 should map to 2430.555");
		this.assertFloatEquals(env.map(0.95), 2500.0, "0.95 should map to 2500");
		this.assertFloatEquals(env.map(1), 6200.0, "1 should map to 6200");
	}

	test_linear_mapping_scaled_smaller {
		env.scale = 0.5;
		this.assertFloatEquals(env.map(0.0), 250.0, "0.0 should map to 250");
		this.assertFloatEquals(env.map(0.1), 750.0, "0.1 should map to 1319.444");
		this.assertFloatEquals(env.map(0.2), 1250.0, "0.2 should map to 1250");
		this.assertFloatEquals(env.map(0.5), 1875.0, "0.5 should map to 1875");
		this.assertFloatEquals(env.map(0.8), 2500.0, "0.8 should map to 2500");
		this.assertFloatEquals(env.map(0.9), 4350.0, "0.9 should map to 4350");
		this.assertFloatEquals(env.map(1), 6200.0, "1 should map to 6200");
	}


	test_exponential_mapping {
		var env_exp = SentireEnvelope.new([0.1,0.9], [250, 1250, 2500, 6200], [\exp, \exp, \exp], 1);
		this.assertFloatEquals(env_exp.map(0.0), 250.0, "0.0 should map to 250.0");
		this.assertFloatEquals(env_exp.map(0.05), 0.05.linexp(0.0, 0.1, 250, 1250), "0.05 should map to 559.0168...");
		this.assertFloatEquals(env_exp.map(0.1), 1250.0, "0.1 should map to 1250.0");

		this.assertFloatEquals(env_exp.map(0.5), 0.5.linexp(0.1, 0.9, 1250, 2500), "0.5 should map to 1767.7669...");
		this.assertFloatEquals(env_exp.map(0.9), 2500.0, "0.9 should map to 2500.");
		this.assertFloatEquals(env_exp.map(0.95), 0.95.linexp(0.9, 1.0, 2500, 6200), "0.95 should map to 3937.00393...");
		this.assertFloatEquals(env.map(1), 6200.0, "1 should map to 6200");
	}


	test_linear_mapping_scaled_all_flex {
		env = SentireEnvelope.new([0.1,0.9], [250, 1250, 2500, 6200], [\lin, \lin, \lin], 1, \flex);
		env.scale = 2;
		this.assertFloatEquals(env.map(0.0), 250.0, "0.0 should map to 250");
		this.assertFloatEquals(env.map(0.05), 750.0, "0.05 should map to 750");
		this.assertFloatEquals(env.map(0.1), 1250.0, "0.1 should map to 250");
		this.assertFloatEquals(env.map(0.5), 1875.0, "0.5 should map to 1875");
		this.assertFloatEquals(env.map(0.9), 2500.0, "0.9 should map to 2500");
		this.assertFloatEquals(env.map(0.95), 4350.0, "0.95 should map to 4350");
		this.assertFloatEquals(env.map(1), 6200.0, "1 should map to 6200");
	}

	test_linear_mapping_scaled_one_flex {
		env = SentireEnvelope.new([0.1,0.9], [250, 1250, 2500, 6200], [\lin, \lin, \lin], 1, [nil, \flex]);
		env.scale = 0.2;
		this.assertFloatEquals(env.map(0.0), 250.0, "0.0 should map to 250");
		this.assertFloatEquals(env.map(0.05), 350.0, "0.05 should map to 350.0");
		this.assertFloatEquals(env.map(0.1), 450.0, "0.1 should map to 450.0");
		this.assertFloatEquals(env.map(0.5), 1250.0, "0.5 should map to 1250.0");
		this.assertFloatEquals(env.map(0.9), 2500.0, "0.9 should map to 2500");
		this.assertFloatEquals(env.map(0.95), 4350.0, "0.95 should map to 4350");
		this.assertFloatEquals(env.map(1), 6200.0, "1 should map to 6200");
	}
}


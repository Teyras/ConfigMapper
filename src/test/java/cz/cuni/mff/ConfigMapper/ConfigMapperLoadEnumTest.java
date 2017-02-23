package cz.cuni.mff.ConfigMapper;

import cz.cuni.mff.ConfigMapper.Annotations.ConfigOption;
import cz.cuni.mff.ConfigMapper.Annotations.ConstantAlias;
import cz.cuni.mff.ConfigMapper.ConfigMapper;
import cz.cuni.mff.ConfigMapper.LoadingMode;
import cz.cuni.mff.ConfigMapper.MappingException;
import cz.cuni.mff.ConfigMapper.Nodes.ConfigRoot;
import cz.cuni.mff.ConfigMapper.Nodes.ScalarOption;
import cz.cuni.mff.ConfigMapper.Nodes.Section;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class ConfigMapperLoadEnumTest {
	static class WithEnum {
		enum OptionEnum {
			ON,
			OFF
		}

		@ConfigOption(section = "section")
		@ConstantAlias(constant = "OFF", alias = "OOFF")
		OptionEnum option1;

		@ConfigOption(section = "section")
		OptionEnum option2;
	}

	static class WithChainedEnum {
		enum OptionEnum {
			ON,
			OFF
		}

		@ConfigOption(section = "section")
		@ConstantAlias(constant = "OFF", alias = "ON")
		@ConstantAlias(constant = "ON", alias = "OFF")
		OptionEnum option1;

		@ConfigOption(section = "section")
		@ConstantAlias(constant = "OFF", alias = "ON")
		@ConstantAlias(constant = "ON", alias = "OFF")
		OptionEnum option2;
	}
	@Test
	public void loadEnum() throws Exception {
		ConfigRoot config = new ConfigRoot("", Collections.singletonList(
			new Section("section", Arrays.asList(
				new ScalarOption("option1", "OOFF"),
				new ScalarOption("option2", "OFF")
			))
		));

		ConfigMapper mapper = new ConfigMapper();
		WithEnum object = mapper.load(config, WithEnum.class, LoadingMode.STRICT);

		Assert.assertEquals(WithEnum.OptionEnum.OFF, object.option1);
		Assert.assertEquals(WithEnum.OptionEnum.OFF, object.option2);
	}

	@Test(expected = MappingException.class)
	public void loadEnumUnknownConstantThrows() throws Exception {
		ConfigRoot config = new ConfigRoot("", Collections.singletonList(
			new Section("section", Arrays.asList(
				new ScalarOption("option1", "FOO"),
				new ScalarOption("option2", "BAR")
			))
		));

		ConfigMapper mapper = new ConfigMapper();
		WithEnum object = mapper.load(config, WithEnum.class, LoadingMode.STRICT);
	}

	@Test
	public void loadChainedEnum() throws Exception {
		ConfigRoot config = new ConfigRoot("", Arrays.asList(
			new Section("section", Arrays.asList(
				new ScalarOption("option1", "OFF"),
				new ScalarOption("option2", "ON")
			))
		));

		ConfigMapper mapper = new ConfigMapper();
		WithChainedEnum object = mapper.load(config, WithChainedEnum.class, LoadingMode.STRICT);

		Assert.assertEquals(WithChainedEnum.OptionEnum.ON, object.option1);
		Assert.assertEquals(WithChainedEnum.OptionEnum.OFF, object.option2);
	}
}
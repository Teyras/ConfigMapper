import cz.cuni.mff.ConfigMapper.Annotations.ConfigOption;
import cz.cuni.mff.ConfigMapper.ConfigMapper;
import cz.cuni.mff.ConfigMapper.Nodes.ListOption;
import cz.cuni.mff.ConfigMapper.Nodes.ConfigRoot;
import cz.cuni.mff.ConfigMapper.Nodes.Section;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConfigMapperSaveListTest {
	static class WithList {
		@ConfigOption(section = "section")
		List<String> list;
	}

	@Test
	public void saveList() throws Exception {
		List<String> list = new ArrayList<>(Arrays.asList("foo", "bar", "baz"));

		WithList object = new WithList();
		object.list = list;

		ConfigMapper mapper = new ConfigMapper();
		ConfigRoot config = mapper.save(object, null, false);

		ConfigRoot expected = new ConfigRoot("", Arrays.asList(
			new Section("section", Collections.singletonList(
				new ListOption("list", Arrays.asList("foo", "bar", "baz"), ",")
			))
		));

		Assert.assertEquals(expected, config);
	}
}
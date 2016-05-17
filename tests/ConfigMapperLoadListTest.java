import cz.cuni.mff.ConfigMapper.Annotations.ConfigOption;
import cz.cuni.mff.ConfigMapper.ConfigMapper;
import cz.cuni.mff.ConfigMapper.LoadingMode;
import cz.cuni.mff.ConfigMapper.Nodes.ListOption;
import cz.cuni.mff.ConfigMapper.Nodes.Root;
import cz.cuni.mff.ConfigMapper.Nodes.Section;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConfigMapperLoadListTest {
	static class MappedObject {
		@ConfigOption(section = "section")
		List<String> list;
	}

	@Test
	public void loadList() throws Exception {
		List<String> listValue = Arrays.asList(
			"foo",
			"bar",
			"baz"
		);

		Root config = new Root("", Collections.singletonList(
			new Section("section", Collections.singletonList(
				new ListOption("list", listValue, ',')
			))
		));

		ConfigMapper mapper = new ConfigMapper();
		MappedObject object = mapper.load(config, MappedObject.class, LoadingMode.STRICT);

		Assert.assertEquals(listValue, object.list);

		// Make sure that the list can be modified
		object.list.add("boo");
		object.list.remove(0);

		Assert.assertEquals(Arrays.asList("bar", "baz", "boo"), object.list);
	}
}
import cz.cuni.mff.ConfigMapper.Annotations.UndeclaredOptions;
import cz.cuni.mff.ConfigMapper.ConfigMapper;
import cz.cuni.mff.ConfigMapper.Nodes.Root;
import cz.cuni.mff.ConfigMapper.Nodes.ScalarOption;
import cz.cuni.mff.ConfigMapper.Nodes.Section;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigMapperSaveRelaxedTest {
	static class WithUndeclaredOptions {
		@UndeclaredOptions
		Map<String, String> options = new LinkedHashMap<>();
	}

	@Test
	public void saveUndeclaredOpts() throws Exception {
		WithUndeclaredOptions object = new WithUndeclaredOptions();
		object.options.put("section1#foo", "bar");
		object.options.put("section2#baz", "gah");

		ConfigMapper mapper = new ConfigMapper();
		Root config = mapper.save(object, false);

		Root expected = new Root("", Arrays.asList(
			new Section("section1", Collections.singletonList(
				new ScalarOption("foo", "bar")
			)),
			new Section("section2", Collections.singletonList(
				new ScalarOption("baz", "gah")
			))
		));

		Assert.assertEquals(expected, config);
	}
}
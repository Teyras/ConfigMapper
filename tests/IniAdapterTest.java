import cz.cuni.mff.ConfigMapper.Adapters.IniAdapter;
import cz.cuni.mff.ConfigMapper.ConfigurationException;
import cz.cuni.mff.ConfigMapper.Nodes.*;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by teyras on 29.3.16.
 */
public class IniAdapterTest {
    private static final Root simpleConfig = new Root("", Arrays.asList(
            new Section("sectionA", Arrays.asList(
                    new ScalarOption("option1", "foo"),
                    new ScalarOption("option2", "bar")
            )),
            new Section("sectionB", Arrays.asList(
                    new ListOption("option3", Arrays.asList("baz", "baz", "baz"))
            ))
    ));

    private static byte[] simpleFileContent = (String.join("\n",
            "[sectionA]",
            "option1=foo",
            "option2=bar",
            "[sectionB]",
            "option3=baz:baz:baz"
    )).getBytes();

    @Ignore
	@Test
	public void readBasic() throws Exception {
		byte[] input = simpleFileContent;

		Root expectedConfig = simpleConfig;

		IniAdapter adapter = new IniAdapter();
		Root config = adapter.read(new ByteArrayInputStream(input));

		assertEquals(expectedConfig, config);
	}

    @Test
	public void writeBasic() throws Exception {
        IniAdapter adapter = new IniAdapter();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        adapter.write(simpleConfig,outputStream);
        assertArrayEquals(simpleFileContent,outputStream.toByteArray());
	}

    @Test(expected = ConfigurationException.class)
    public void writeFailWrongStructure() throws Exception {
        IniAdapter adapter = new IniAdapter();
        Root wrongStructuredConfig = new Root("", Arrays.asList(
                new ScalarOption("option1", "foo"),
                new ScalarOption("option2", "bar")
        ));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        adapter.write(wrongStructuredConfig,outputStream);
    }
}
import cz.cuni.mff.ConfigMapper.Adapters.IniAdapter;
import cz.cuni.mff.ConfigMapper.ConfigurationException;
import cz.cuni.mff.ConfigMapper.Nodes.*;
import cz.cuni.mff.ConfigMapper.parsedBoolean;
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
                    new ListOption("option3", Arrays.asList("baz", "baz", "baz"),":")
            ))
    ));

    private static byte[] simpleFileContent = (String.join("\n",
            "[sectionA]",
            "option1=foo",
            "option2=bar",
            "[sectionB]",
            "option3=baz:baz:baz"
    )).getBytes();

    private static byte[] booleanTestFileContent = (String.join("\n",
            "[sectionA]",
            "optionTrue1=1",
            "optionTrue2=t",
            "optionTrue3=enabled",
            "optionFalse1=0",
            "optionFalse2=f",
            "optionFalse3=disabled"
    )).getBytes();

    private static byte[] trickyFileContent = (String.join("\n",
            "[sectionA]",
            "option1=f\\;oo ; comment",
            ";only comment on this line ; wow",
            " \\ opti\\ on2\\ \\  =bar",
            "[sectionB]",
            "option3=baz:baz:baz",
            "option4=baz, baz : baz"
    )).getBytes();

	@Test
	public void readBasic() throws Exception {
		byte[] input = simpleFileContent;

		Root expectedConfig = simpleConfig;

		IniAdapter adapter = new IniAdapter();
		Root config = adapter.read(new ByteArrayInputStream(input));

		assertEquals(expectedConfig, config);
	}

    @Test
    public void readBooleans() throws Exception {
        byte[] booleanTestFileContent = (String.join("\n",
                "[sectionA]",
                "optionTrue1=1",
                "optionTrue2=t",
                "optionTrue3=enabled",
                "optionFalse1=0",
                "optionFalse2=f",
                "optionFalse3=disabled",
                "optionNotBoolean1=true",
                "optionNotBoolean2=notBoolean"
        )).getBytes();

        Root expectedConfig = new Root("", Arrays.asList(
                new Section("sectionA", Arrays.asList(
                        new ScalarOption("optionTrue1", "1", parsedBoolean.TRUE),
                        new ScalarOption("optionTrue2", "t", parsedBoolean.TRUE),
                        new ScalarOption("optionTrue3", "enabled", parsedBoolean.TRUE),
                        new ScalarOption("optionFalse1", "0", parsedBoolean.FALSE),
                        new ScalarOption("optionFalse2", "f", parsedBoolean.FALSE),
                        new ScalarOption("optionFalse3", "disabled", parsedBoolean.FALSE),
                        new ScalarOption("optionNotBoolean1", "true", parsedBoolean.NOT_BOOLEAN),
                        new ScalarOption("optionNotBoolean2", "notBoolean", parsedBoolean.NOT_BOOLEAN)
                ))));

        IniAdapter adapter = new IniAdapter();
        Root config = adapter.read(new ByteArrayInputStream(booleanTestFileContent));

        assertEquals(expectedConfig, config);
    }

    @Test
    public void extractSectionNameTest() throws Exception {
        IniAdapter adapter = new IniAdapter();

        assertEquals("Sekce 1",adapter.extractSectionNameTest("[Sekce 1]"));
        assertEquals("Sekce 1  ",adapter.extractSectionNameTest("[ Sekce 1  ]"));
        assertEquals("$Sekce::podsekce",adapter.extractSectionNameTest("[$Sekce::podsekce]"));
        assertEquals("$Sekce::podsekce~",adapter.extractSectionNameTest("[  ~$Sekce::podsekce~]"));
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
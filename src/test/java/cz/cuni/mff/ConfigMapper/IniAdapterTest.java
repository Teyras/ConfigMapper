package cz.cuni.mff.ConfigMapper;

import cz.cuni.mff.ConfigMapper.Adapters.IniAdapter;
import cz.cuni.mff.ConfigMapper.ConfigurationException;
import cz.cuni.mff.ConfigMapper.Nodes.*;
import cz.cuni.mff.ConfigMapper.ParsedBoolean;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Testing IniAdapter methods
 */
public class IniAdapterTest {
    private static ConfigRoot simpleConfig;

    private static ConfigRoot trickyConfig;

    @BeforeClass
    public static void prepareConfig() {
        ScalarOption fooWithComment = new ScalarOption("option1", "foo");
        fooWithComment.setDescription(" this is foo");
        simpleConfig = new ConfigRoot("", Arrays.asList(
                new Section("sectionA", Arrays.asList(
                        fooWithComment,
                        new ScalarOption("option2", "bar")
                )),
                new Section("sectionB", Arrays.asList(
                        new ListOption("option3", Arrays.asList("baz", "baz", "baz"),":")
                ))
        ));

        ScalarOption wierdFooWithComment = new ScalarOption("option1", "f\\;oo");
        wierdFooWithComment.setDescription(" comment");
        trickyConfig = new ConfigRoot("", Arrays.asList(
                new Section("sectionA", Arrays.asList(
                        wierdFooWithComment,
                        new ScalarOption(" opti\\ on2  ", "bar")
                )),
                new Section("sectionB", Arrays.asList(
                        new ListOption("option3", Arrays.asList("baz", "baz", "baz"),":"),
                        new ListOption("option4", Arrays.asList("baz", "baz : baz"),",")
                ))
        ));
    }

    private static byte[] simpleFileContent = (String.join("\n",
            "[sectionA]",
            "option1=foo\t; this is foo",
            "option2=bar",
            "[sectionB]",
            "option3=baz:baz:baz",
            ""
    )).getBytes();

    private static byte[] trickyFileContent = (String.join("\n",
            "[sectionA]",
            "option1=f\\;oo \t; comment",
            ";only comment on this line \t; wow",
            " \\ opti\\ on2\\ \\  =bar",
            "[sectionB]",
            "option3=baz:baz:baz",
            "option4=baz, baz : baz",
            ""
    )).getBytes();

    private static byte[] trickyFileExpectedOutput = (String.join("\n",
            "[sectionA]",
            "option1=f\\;oo\t; comment",
            "\\ opti\\ on2\\ \\ =bar",
            "[sectionB]",
            "option3=baz:baz:baz",
            "option4=baz,baz : baz",
            ""
    )).getBytes();

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

        ConfigRoot expectedConfig = new ConfigRoot("", Arrays.asList(
                new Section("sectionA", Arrays.asList(
                        new ScalarOption("optionTrue1", "1", ParsedBoolean.TRUE),
                        new ScalarOption("optionTrue2", "t", ParsedBoolean.TRUE),
                        new ScalarOption("optionTrue3", "enabled", ParsedBoolean.TRUE),
                        new ScalarOption("optionFalse1", "0", ParsedBoolean.FALSE),
                        new ScalarOption("optionFalse2", "f", ParsedBoolean.FALSE),
                        new ScalarOption("optionFalse3", "disabled", ParsedBoolean.FALSE),
                        new ScalarOption("optionNotBoolean1", "true", ParsedBoolean.NOT_BOOLEAN),
                        new ScalarOption("optionNotBoolean2", "notBoolean", ParsedBoolean.NOT_BOOLEAN)
                ))));

        IniAdapter adapter = new IniAdapter();
        ConfigRoot config = adapter.read(new ByteArrayInputStream(booleanTestFileContent));

        assertEquals(expectedConfig, config);
    }

    @Test
    public void readSectionNames() throws Exception {
        byte[] sectionNameTestFileContent = (String.join("\n",
                "[Sekce 1]",
                "[ Sekce 1  ]",
                "[$Sekce::podsekce]",
                "[  ~$Sekce::podsekce~]",
                ""
        )).getBytes();

        ConfigRoot expectedConfig = new ConfigRoot("", Arrays.asList(
                new Section("Sekce 1", new ArrayList<>()),
                new Section("Sekce 1  ", new ArrayList<>()),
                new Section("$Sekce::podsekce", new ArrayList<>()),
                new Section("$Sekce::podsekce~", new ArrayList<>())
        ));

        IniAdapter adapter = new IniAdapter();
        ConfigRoot config = adapter.read(new ByteArrayInputStream(sectionNameTestFileContent));

        assertEquals(expectedConfig, config);
    }

    @Test
    public void readSectionComment() throws Exception {
        byte[] sectionTestFileContent = (String.join("\n",
                "[ Sekce 1  ]",
                ";' Sekce 1  ' comment",
                "[$Sekce::podsekce]",
                "option2=bar",
                "; this is not a section comment",
                "option3=bar",
                "[ Sekce 2  ]",
                "",
                ";' Sekce 2  ' comment",
                ""
        )).getBytes();

        Section sekce1 = new Section("Sekce 1  ", new ArrayList<>());
        sekce1.setDescription("' Sekce 1  ' comment");
        Section sekce2 = new Section("Sekce 2  ", new ArrayList<>());
        sekce2.setDescription("' Sekce 2  ' comment");
        ConfigRoot expectedConfig = new ConfigRoot("", Arrays.asList(
                sekce1,
                new Section("$Sekce::podsekce", Arrays.asList(
                        new ScalarOption("option2", "bar"),
                        new ScalarOption("option3", "bar")
                )),
                sekce2
        ));

        IniAdapter adapter = new IniAdapter();
        ConfigRoot config = adapter.read(new ByteArrayInputStream(sectionTestFileContent));

        assertEquals(expectedConfig, config);
    }

    @Test
    public void readBasic() throws Exception {
        byte[] input = simpleFileContent;

        ConfigRoot expectedConfig = simpleConfig;

        IniAdapter adapter = new IniAdapter();
        ConfigRoot config = adapter.read(new ByteArrayInputStream(input));

        assertEquals(expectedConfig, config);
    }

    @Test
    public void readTricky() throws Exception {
        byte[] input = trickyFileContent;

        ConfigRoot expectedConfig = trickyConfig;

        IniAdapter adapter = new IniAdapter();
        ConfigRoot config = adapter.read(new ByteArrayInputStream(input));

        assertEquals(expectedConfig, config);
    }

    @Test
	public void writeBasic() throws Exception {
        IniAdapter adapter = new IniAdapter();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        adapter.write(simpleConfig,outputStream);
        assertArrayEquals(simpleFileContent,outputStream.toByteArray());
	}

    @Test
    public void writeTricky() throws Exception {
        IniAdapter adapter = new IniAdapter();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        adapter.write(trickyConfig,outputStream);
        assertArrayEquals(trickyFileExpectedOutput,outputStream.toByteArray());
    }

    @Test
    public void writeLists() throws Exception {
        ConfigRoot configWithLists = new ConfigRoot("", Arrays.asList(
                new Section("Lists", Arrays.asList(
                        new ListOption("easy", Arrays.asList("a", "b", "c"),":"),
                        new ListOption("withWrongSep1", Arrays.asList("a", "b, c"),":"),
                        new ListOption("withWrongSep2", Arrays.asList("a", "b: c"),","),
                        new ListOption("escaped", Arrays.asList("a", "b:c"),":")
                        )
                )
        ));

        byte[] listsTestFileContent = (String.join("\n",
                "[Lists]",
                "easy=a:b:c",
                "withWrongSep1=a:b, c",
                "withWrongSep2=a,b: c",
                "escaped=a:b\\:c",
                ""
        )).getBytes();

        IniAdapter adapter = new IniAdapter();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        adapter.write(configWithLists,outputStream);
        assertArrayEquals(listsTestFileContent,outputStream.toByteArray());
    }

    @Test(expected = ConfigurationException.class)
    public void writeFailWrongStructure() throws Exception {
        IniAdapter adapter = new IniAdapter();
        ConfigRoot wrongStructuredConfig = new ConfigRoot("", Arrays.asList(
                new ScalarOption("option1", "foo"),
                new ScalarOption("option2", "bar")
        ));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        adapter.write(wrongStructuredConfig,outputStream);
    }

    @Test(expected = ConfigurationException.class)
    public void testDuplicateSectionException() throws Exception {
        byte[] sectionNameTestFileContent = (String.join("\n",
                "[Sekce 1]",
                "easy1=a:b:c",
                "easy2=a:b:c",
                "[Sekce 1]",
                "easy3=a:b:c",
                "easy4=a:b:c",
                ""
        )).getBytes();

        IniAdapter adapter = new IniAdapter();
        ConfigRoot config = adapter.read(new ByteArrayInputStream(sectionNameTestFileContent));
        // there should be an exception before this
        assertNull(config);
    }

    @Test(expected = ConfigurationException.class)
    public void testDuplicateOptionException() throws Exception {
        byte[] sectionNameTestFileContent = (String.join("\n",
                "[Sekce 1]",
                "easy1=a:b:c",
                "easy2=a:b:c",
                "[Sekce 2]",
                "easy3=a:b:c",
                "easy3=a:b:c",
                ""
        )).getBytes();

        IniAdapter adapter = new IniAdapter();
        ConfigRoot config = adapter.read(new ByteArrayInputStream(sectionNameTestFileContent));
        // there should be an exception before this
        assertNull(config);
    }
}
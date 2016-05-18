import cz.cuni.mff.ConfigMapper.Adapters.IniAdapter;
import cz.cuni.mff.ConfigMapper.Nodes.ListOption;
import cz.cuni.mff.ConfigMapper.Nodes.ConfigRoot;
import cz.cuni.mff.ConfigMapper.Nodes.ScalarOption;
import cz.cuni.mff.ConfigMapper.Nodes.Section;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Testing IniAdapter methods
 */

public class IniAdapterAssignmentTest {
    private static byte[] fileContent = (String.join("\n",
            "[Sekce 1]",
            "; komentar",
            "Option 1 = value 1\t; volba 'Option 1' ma hodnotu 'value 1'",
            "oPtion 1    =  \\ value 2\\ \\ \\ \t; volba 'oPtion 1' ma hodnotu ' value 2   ', 'oPtion 1' a 'Option 1' jsou ruzne volby",
            "[$Sekce::podsekce]",
            "Option 2=value 1:value 2:value 3\t; volba 'Option 2' je seznam hodnot 'value 1', 'value 2' a 'value 3'",
            "Option 3 =value 1, ${Sekce 1#Option 1}\t; volba 'Option 3' je seznam hodnot 'value 1' a 'value 1'",
            "Option 4= v1,${$Sekce::podsekce#Option 3},v2\t; volba 'Option 4' je seznam hodnot 'v1', 'value 1', 'value 1', 'v2'",
            "Option 5= v1, v2:v3\t; volba 'Option 5' je seznam hodnot 'v1' a 'v2:v3', nebo 'v1, v2' a 'v3' podle zvoleneho oddelovace",
            "[Cisla]",
            "cele = -1285",
            "cele_bin = 0b01101001",
            "cele_hex = 0x12ae,0xAc2B",
            "cele_oct = 01754",
            "float1 = -124.45667356",
            "float2 = +4.1234565E+45",
            "float3 = 412.34565e45",
            "float4 = -1.1245864E-6",
            "[Other]",
            "bool1 = 1",
            "bool2 = on",
            "bool3=f",
            "" )).getBytes();

    private static byte[] outputFileContent = (String.join("\n",
            "[Sekce 1]",
            "; komentar",
            "Option 1=value 1\t; volba 'Option 1' ma hodnotu 'value 1'",
            "oPtion 1=\\ value 2\\ \\ \\ \t; volba 'oPtion 1' ma hodnotu ' value 2   ', 'oPtion 1' a 'Option 1' jsou ruzne volby",
            "[$Sekce::podsekce]",
            "Option 2=value 1:value 2:value 3\t; volba 'Option 2' je seznam hodnot 'value 1', 'value 2' a 'value 3'",
            "Option 3=value 1,value 1\t; volba 'Option 3' je seznam hodnot 'value 1' a 'value 1'",
            "Option 4=v1,value 1,value 1,v2\t; volba 'Option 4' je seznam hodnot 'v1', 'value 1', 'value 1', 'v2'",
            "Option 5=v1,v2:v3\t; volba 'Option 5' je seznam hodnot 'v1' a 'v2:v3', nebo 'v1, v2' a 'v3' podle zvoleneho oddelovace",
            "[Cisla]",
            "cele=-1285",
            "cele_bin=0b01101001",
            "cele_hex=0x12ae,0xAc2B",
            "cele_oct=01754",
            "float1=-124.45667356",
            "float2=+4.1234565E+45",
            "float3=412.34565e45",
            "float4=-1.1245864E-6",
            "[Other]",
            "bool1=1",
            "bool2=on",
            "bool3=f",
            "" )).getBytes();

    private static ConfigRoot expectedConfig;
    @BeforeClass
    public static void prepareConfig() {
        // setting up first section
        ScalarOption option1a = new ScalarOption("Option 1", "value 1");
        option1a.setDescription(" volba 'Option 1' ma hodnotu 'value 1'");

        ScalarOption option1b = new ScalarOption("oPtion 1", " value 2   ");
        option1b.setDescription(" volba 'oPtion 1' ma hodnotu ' value 2   ', 'oPtion 1' a 'Option 1' jsou ruzne volby");

        Section sekce1 = new Section("Sekce 1", Arrays.asList(
                    option1a,
                    option1b
            ));
        sekce1.setDescription("komentar");

        // setting up second section
        ListOption option2 = new ListOption("Option 2", Arrays.asList("value 1", "value 2", "value 3"), ":");
        option2.setDescription(" volba 'Option 2' je seznam hodnot 'value 1', 'value 2' a 'value 3'");

        ListOption option3 = new ListOption("Option 3", Arrays.asList("value 1", "value 1"), ",");
        option3.setDescription(" volba 'Option 3' je seznam hodnot 'value 1' a 'value 1'");

        ListOption option4 = new ListOption("Option 4", Arrays.asList("v1", "value 1", "value 1", "v2"), ",");
        option4.setDescription(" volba 'Option 4' je seznam hodnot 'v1', 'value 1', 'value 1', 'v2'");

        ListOption option5 = new ListOption("Option 5", Arrays.asList("v1", "v2:v3"), ",");
        option5.setDescription(" volba 'Option 5' je seznam hodnot 'v1' a 'v2:v3', nebo 'v1, v2' a 'v3' podle zvoleneho oddelovace");
        expectedConfig = new ConfigRoot("", Arrays.asList(
                sekce1,
                new Section("$Sekce::podsekce", Arrays.asList(
                        option2,
                        option3,
                        option4,
                        option5
                )),
                new Section("Cisla", Arrays.asList(
                        new ScalarOption("cele", "-1285"),
                        new ScalarOption("cele_bin", "0b01101001"),
                        new ListOption("cele_hex", Arrays.asList("0x12ae", "0xAc2B"), ","),
                        new ScalarOption("cele_oct", "01754"),
                        new ScalarOption("float1", "-124.45667356"),
                        new ScalarOption("float2", "+4.1234565E+45"),
                        new ScalarOption("float3", "412.34565e45"),
                        new ScalarOption("float4", "-1.1245864E-6")
                )),
                new Section("Other", Arrays.asList(
                        new ScalarOption("bool1", "1"),
                        new ScalarOption("bool2", "on"),
                        new ScalarOption("bool3", "f")
                ))
        ));
    }

	@Test
	public void readAssignmentExample() throws Exception {
		byte[] input = fileContent;

		IniAdapter adapter = new IniAdapter();
		ConfigRoot config = adapter.read(new ByteArrayInputStream(input));

		assertEquals(expectedConfig, config);
	}

    @Test
	public void writeAssignmentExample() throws Exception {
        IniAdapter adapter = new IniAdapter();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        adapter.write(expectedConfig,outputStream);

        assertArrayEquals(outputFileContent,outputStream.toByteArray());
	}
}
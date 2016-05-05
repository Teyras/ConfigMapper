package cz.cuni.mff.ConfigMapper.Adapters;

import cz.cuni.mff.ConfigMapper.ConfigurationException;
import cz.cuni.mff.ConfigMapper.Nodes.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * A configuration adapter for INI files
 */
public class IniAdapter implements ConfigAdapter {

    private static final String DEFAULT_CHARSET = "UTF-8";

    /**
	 * Parse config from an INI file
	 *
	 * @param input The input stream
	 * @throws ConfigurationException When the input file is invalid
	 * @return The configuration structure
	 */
	@Override
	public Root read(InputStream input) throws ConfigurationException {
		return null;
	}

	/**
	 * Write config into an INI file
	 * Function goes through configuration on the input, checking its' correct format,
     * and writes it into the output stream.
	 * @param configRoot The configuration structure
	 * @param output The output stream
     * @throws ConfigurationException when the input configuration has a structure incompatible with the ini format
     * @throws IOException when writing to the output stream fails
	 */
	@Override
	public void write(Root configRoot, OutputStream output) throws ConfigurationException, IOException {
        StringBuilder outputString = new StringBuilder();

		for (ConfigNode child : configRoot.getChildren()) {
            if (isNotSection(child)) {
                throw new ConfigurationException("Given configuration cannot be translated into an ini structure");
            }

            Section section = (Section) child;
            outputString.append("[").append(section.getName()).append("]\n");

            for (ConfigNode option : section.getChildren()) {

                // First write the name and "=", then the value depending on the option type
                outputString.append(option.getName()).append("=");

                if (option instanceof ListOption) {

                    ListOption listOption = (ListOption) option;
                    for (String value : listOption.getValue()) {
                        outputString.append(value).append(":");
                    }
                    // deleting the last list delimiter
                    outputString.deleteCharAt(outputString.length()-1);

                } else if (option instanceof ScalarOption){
                    ScalarOption simpleOption = (ScalarOption) option;
                    outputString.append(simpleOption.getValue())
                            .append("\n");
                } else  {
                    throw new ConfigurationException(
                            "Given configuration cannot be translated into ini structure: " +
                                    "one of the sections has children that are not options");
                }
                // TODO: print description as well?
            }
		}

        output.write(outputString.toString().getBytes(Charset.forName(DEFAULT_CHARSET)));
        output.flush();

	}

    private boolean isNotSection(ConfigNode node) {
        return !(node instanceof Section);
    }

}

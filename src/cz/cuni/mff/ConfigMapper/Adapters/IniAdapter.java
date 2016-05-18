package cz.cuni.mff.ConfigMapper.Adapters;

import cz.cuni.mff.ConfigMapper.ConfigurationException;
import cz.cuni.mff.ConfigMapper.Nodes.*;
import cz.cuni.mff.ConfigMapper.ParsedBoolean;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * A configuration adapter for INI files
 */
public final class IniAdapter implements ConfigAdapter {

	private static final String DEFAULT_CHARSET = "UTF-8";

	private static final ArrayList<String> TRUE_REPRESENTATION =
			new ArrayList<>(Arrays.asList( "1", "t", "y", "on", "yes", "enabled" ));

	private static final ArrayList<String> FALSE_REPRESENTATION =
			new ArrayList<>(Arrays.asList( "0", "f", "n", "off", "no", "disabled" ));

	/**
	 * Parse config from an INI file
	 *
	 * @param input The input stream
	 * @throws ConfigurationException When the input file is invalid
	 * @return The configuration structure
	 */
	@Override
	public Root read(InputStream input) throws ConfigurationException {
		Root outputRoot = new Root("", new ArrayList<>());

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {

			String line = reader.readLine();
			Section currentSection = null;
			boolean lastLineWasSection = false;
			while (line != null) {

				if (isDescribingSection(line)) {
					if (currentSection != null) {
						outputRoot.addChild(currentSection);
					}
					String sectionName = extractSectionName(line);
					//System.err.println("Creating section: '" + sectionName + "'");
					currentSection = new Section(sectionName,new ArrayList<>());
					lastLineWasSection = true;
					line = reader.readLine();
					continue;
				}

				// handle comment
				String comment = "";
				int commentStartIndex = indexOfUnescaped(line,';');
				// if the line contains an unescaped ';', strip the comment part
				if (commentStartIndex != -1) {
					comment = line.substring(commentStartIndex+1);
					line = line.substring(0,commentStartIndex);
					// System.err.println("Splitting line into '" + line +  "' and comment '" + comment + "'");

					if (isLineSectionDescription(line, comment, lastLineWasSection)) {
						assert currentSection != null;
						currentSection.setDescription(comment.trim());
					}
				}

				// if there is only comment on the line, do nothing
				if (line.isEmpty()) {
					line = reader.readLine();
					continue;
				}

				// now we know, that last line did not contain a section declaration
				lastLineWasSection = false;

				// split the line in the options' key and value
				String name = line.substring(0, line.indexOf('='));
				name = removeSurroundingWhitespace(name);

				String value = line.substring(line.indexOf('=')+1);
				value = removeSurroundingWhitespace(value);

				// determine, whether the value is a list or a simple one
				if (isList(value)) {

					List<String> listValue = parseIntoList(value);
					String separator = getListSeparator(value);
					SortedMap<Integer,ListOption> listsToBeInserted = new TreeMap<>(Collections.reverseOrder());
					for (int i=0; i < listValue.size(); ++i) {
						if (isLink(listValue.get(i))) {
							Option targetOption = getLinkValue(listValue.get(i), outputRoot, currentSection);

							if (targetOption instanceof ListOption) {
								ListOption opt = (ListOption) targetOption;
								/*
								 * Lists have the same separators, so the target lists items have to be
								 * all added. This would mean modifying the list that is being iterated
								 * through, so the target will be noted and added after the iteration is done
								 */
								if (opt.getSeparator().equals(separator)) {
									listsToBeInserted.put(i,opt);
								/*
								 * Lists have different separators, so the target list value is inserted
								 * as a plain String
								 */
								} else {
									StringBuilder valueStringBuilder = new StringBuilder();
									for (String val : opt.getValue()) {
										valueStringBuilder.append(val).append(opt.getSeparator());
									}
									// get rid of the last separator
									valueStringBuilder.deleteCharAt(valueStringBuilder.length()-1);
									listValue.set(i,valueStringBuilder.toString());
								}
							} else {
								ScalarOption opt = (ScalarOption) targetOption;
								listValue.set(i,opt.getValue());
							}
						}
					}

					// Adding all the list items from list that were linked
					for (Map.Entry<Integer,ListOption> toBeInserted : listsToBeInserted.entrySet()) {
						int insertionIndex = toBeInserted.getKey();
						List<String> insertingList = toBeInserted.getValue().getValue();
						listValue.set(insertionIndex,insertingList.get(0));
						for (int i = insertionIndex + 1; i <= insertingList.size(); ++i) {
							listValue.add(i,insertingList.get(i-insertionIndex));
						}
					}

					assert currentSection != null;
					ListOption newOption = new ListOption(name,listValue,separator);
					newOption.setDescription(comment);
					currentSection.addChild(newOption);
				} else { // option has a simple value
					assert currentSection != null;

					if (isLink(value)) {
						Option targetOption = getLinkValue(value, outputRoot, currentSection);
						if (targetOption instanceof ListOption) {
							ListOption targetList = (ListOption)targetOption;
							ListOption newOption = new ListOption(name,targetList.getValue(),targetList.getSeparator());
							newOption.setDescription(comment);
							currentSection.addChild(newOption);
						} else { // option is of a simple value
							ScalarOption targetValue = new ScalarOption(name,((ScalarOption)targetOption).getValue());
							targetValue.setBooleanValue(((ScalarOption)targetOption).getBooleanValue());
							targetValue.setDescription(comment);
							currentSection.addChild(targetValue);
						}

					} else { // value is not a link
						ScalarOption newOption = new ScalarOption(name, value);
						newOption.setDescription(comment);

						// if the option can be interpreted as a boolean, set the boolean value
						if (TRUE_REPRESENTATION.contains(value)) {
							newOption.setBooleanValue(ParsedBoolean.TRUE);
						} else if (FALSE_REPRESENTATION.contains(value)) {
							newOption.setBooleanValue(ParsedBoolean.FALSE);
						} else {
							newOption.setBooleanValue(ParsedBoolean.NOT_BOOLEAN);
						}

						currentSection.addChild(newOption);
					}
				}

				line = reader.readLine();
			}
			outputRoot.addChild(currentSection);

		} catch (IOException exception) {

		}

		return outputRoot;
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
			if (!section.getDescription().isEmpty()) {
				outputString.append("; ").append(section.getDescription()).append("\n");
			}

			for (ConfigNode option : section.getChildren()) {

				// First write the name and "=", then the value depending on the option type
				outputString.append(escapeSpaces(option.getName())).append("=");

				if (option instanceof ListOption) {

					ListOption listOption = (ListOption) option;
					for (String value : listOption.getValue()) {
						String outValue = escapeSpaces(value);
						outValue = escapeSeparator(outValue,listOption.getSeparator());
						outputString.append(outValue).append(listOption.getSeparator());
					}
					// deleting the last list separator
					outputString.deleteCharAt(outputString.length()-1);

				} else if (option instanceof ScalarOption){

					ScalarOption simpleOption = (ScalarOption) option;
					outputString.append(escapeSpaces(simpleOption.getValue()));

				} else  {
					throw new ConfigurationException(
							"Given configuration cannot be translated into ini structure: " +
									"one of the sections has children that are not options");
				}
				 if (! option.getDescription().isEmpty()) {
					outputString.append("\t;").append(option.getDescription());
				}
				outputString.append("\n");
			}
		}

		output.write(outputString.toString().getBytes(Charset.forName(DEFAULT_CHARSET)));
		output.flush();
	}

//////////////////////////////////// PRIVATE METHODS /////////////////////////////////////////////////////////////////

	/**
	 * Get the real value of a link
	 * The link has to the part of the configuration that is already parsed. The
	 * so far parsed configuration is passed by the currentRoot and currentSection
	 * parameters
	 * @param linkString string representation of the link
	 * @param currentRoot part of the configuration file, already parsed
	 * @param currentSection section currently where parsing is in progress
	 * @return actual value of the link
	 * @throws ConfigurationException if the link address is invalid
	 */
	private Option getLinkValue(String linkString, Root currentRoot, Section currentSection) throws ConfigurationException {
		String linkTarget = linkString.substring(linkString.indexOf('{')+1,linkString.indexOf('}'));
		String targetSection = linkTarget.substring(0,linkTarget.indexOf('#'));
		String targetOption = linkTarget.substring(linkTarget.indexOf('#')+1);

		if (currentSection.getName().equals(targetSection)) {
			return getOption(currentSection, targetOption);
		} else {
			for (ConfigNode section : currentRoot.getChildren()) {
				if (section.getName().equals(targetSection)) {
					return getOption((Section) section, targetOption);
				}
			}
		}
		throw new ConfigurationException("Link pointing to an invalid address, " +
				"section not found: " + linkTarget);
	}

	/**
	 * Extract the specified option from the section
	 * @param currentSection the section containing the option
	 * @param targetOption the option to be extracted
	 * @return the option
	 * @throws ConfigurationException if the option is not found in the section
	 */
	private Option getOption(Section currentSection, String targetOption) throws ConfigurationException {
		for (ConfigNode o : currentSection.getChildren()) {
			if (o.getName().equals(targetOption)) {
				return (Option) o;
			}
		}
		throw new ConfigurationException("Link pointing to an invalid address, " +
				"option" +  targetOption + " not found in section " + currentSection.getName());
	}

	/**
	 * Determine if the String representation of the link is a valid link
	 * A valid link has the form of .*${.*}
	 * @param potentialLink the String to be examined
	 * @return true if the String in the parameter satisfies the link definition, false otherwise
	 */
	private boolean isLink(String potentialLink) {
		int dollarCharPosition = indexOfUnescaped(potentialLink,'$');
		boolean valueHasDollarChar = dollarCharPosition != -1;
		boolean valueHasOpeningParenthesis = potentialLink.charAt(dollarCharPosition+1) == '{';
		boolean valueHasClosingParenthesis = potentialLink.substring(dollarCharPosition+2).contains("}");
		return valueHasDollarChar && valueHasOpeningParenthesis && valueHasClosingParenthesis;
	}

	/**
	 * Parse a value into list
	 * @param value string representation of a list value
	 * @return value parsed into a list
	 * @throws ConfigurationException when one of the values is invalid
	 */
	private List<String> parseIntoList(String value) throws ConfigurationException {
		String separator = getListSeparator(value);
		ArrayList<String> valueList =  new ArrayList<>();
		for (String singleValue : value.split(separator)) {
			valueList.add(removeSurroundingWhitespace(singleValue));
		}
		return valueList;
		//return new ArrayList<>(Arrays.asList(value.split(Character.toString(separator))));
	}

	/**
	 * Determine, which of the two possible separators (',' and ':') is the separator for this value
	 * @param value the value in question
	 * @return either ',' or ':', depending on what is present in the value
	 */
	private String getListSeparator(String value) {
		int indexOfComma = indexOfUnescaped(value,',');
		return indexOfComma == -1 ? ":" : ",";
	}

	/**
	 * Searches for an unescaped character
	 * Goes through all the chars on line, for each occurrence of the wanted character checks,
	 * if there is the escaping '\' in front of it.
	 * @param line line to be investigated
	 * @param wantedChar char to be searched for
	 * @return the index of the first unescaped occurrence of the wanted character, -1 otherwise
	 */
	private int indexOfUnescaped(String line, char wantedChar) {

		int searchStartIndex = -1;
		int indexOfWanted = -1;
		while (searchStartIndex < line.length()) {

			indexOfWanted = line.indexOf(wantedChar,searchStartIndex);

			// if wanted character was not found, mimic the indexOf behavior
			if (indexOfWanted == -1) {
				return -1;
			}

			if (wasEscaped(indexOfWanted,line)) {
				searchStartIndex = indexOfWanted +1;
			} else {
				break;
			}
		}
		return indexOfWanted;
	}

	/**
	 * True, if character is escaped in line
	 * When index is invalid throws IndexOutOfBoundsException
	 * @param indexOfChar index of character in question in line
	 * @param line line
	 * @return true if character was escaped
	 */
	private boolean wasEscaped(int indexOfChar, String line) {
		// char in the beginning of the string cannot be escaped
		if (indexOfChar == 0) {
			return false;
		}
		if (indexOfChar < 0 || indexOfChar > line.length()) {
			throw new IndexOutOfBoundsException("Index value " + indexOfChar);
		}
		return line.charAt(indexOfChar - 1) == '\\';
	}

	/**
	 * For a value determines whether it is a list
	 * A list value is a value that contains a separator (':' or ',') that is not escaped
	 * @param value the value to be investigated
	 * @return true if value is of list type, false otherwise
	 */
	private boolean isList(String value) {
		return (indexOfUnescaped(value, ',') != -1 || indexOfUnescaped(value, ':') != -1);
	}

	/**
	 * Remove preceding and trailing whitespaces, that are not escaped
	 * Iterate through string and determine, where the relevant part begins and ends.
	 * While doing so, count the number of escaped preceding and trailing whitespaces.
	 * In the end, add the correct number of spaces to the string.
	 * @param elementString the string from where the whitespaces shall be removed
	 * @return input string without preceding and trailing whitespaces
	 * @throws ConfigurationException when the input string does not validly
	 *  describe an element (name or value)
	 */
	private String removeSurroundingWhitespace(final String elementString) throws ConfigurationException {
		boolean elementStarted = false;
		int prefixSpaces = 0;
		int suffixSpaces = 0;
		int startIndex = -1;
		int endIndex = -1;
		for (int currentPosition = 0; currentPosition < elementString.length(); ++currentPosition) {
			char curChar = elementString.charAt(currentPosition);
			if (curChar == '\\') {
				if (elementStarted) {
					++suffixSpaces;
				} else {
					if (Character.isWhitespace(elementString.charAt(currentPosition + 1))) {
						++prefixSpaces;
					}
				}
			} else if (!Character.isWhitespace(curChar)) {
				if (startIndex < 0) {
					startIndex = currentPosition;
				}
				endIndex = currentPosition;
				elementStarted = true;
				suffixSpaces = 0;
			}
		}
		if (startIndex < 0 || endIndex < startIndex) {
			throw new ConfigurationException("Invalid element: '" + elementString + "'");
		}

		// add the correct number of spaces before and after the relevant part of the string
		StringBuilder outputElementBuilder = new StringBuilder(elementString.substring(startIndex,endIndex+1).trim());
		for (int counter = 0; counter < prefixSpaces; ++counter) {
			outputElementBuilder.insert(0,' ');
		}
		for (int counter = 0; counter < suffixSpaces; ++counter) {
			outputElementBuilder.append(' ');
		}
		return outputElementBuilder.toString();
	}

	/**
	 * Extract the section name
	 * The string from which the section name should be extracted has to start with
	 * the '[' character (a valid line containing a section starting label as described
	 * by the INI documentation). The function returns only the valid section name.
	 * @param line line with a section name beginning
	 * @return valid section name
	 * @throws ConfigurationException if the section name is not valid (e.g. all whitespace
	 * or forbidden characters
	 */
	private String extractSectionName(String line) throws ConfigurationException {
		String rawName =  line.substring(1,line.indexOf(']'));
		int startIndex = 0;
		while (isNotBeginningChar(rawName.charAt(startIndex))) {
			++startIndex;
			if (startIndex > rawName.length()) {
				throw new ConfigurationException("Illegal name of section on line: " + line);
			}
		}
		int endIndex = startIndex ;
		do {
			++endIndex;
			if (endIndex >= rawName.length()) {
				return rawName.substring(startIndex);
			}
		} while (isNameChar(rawName.charAt(endIndex)));
		return rawName.substring(startIndex, endIndex);

	}

	/**
	 * For a line determine wether it holds a section description
	 * That means that it has to contain only a comment (the line must start with ';'), and the
	 * comment cannot be empty. Also, the line before this one has to describe the section
	 * (contain section name)
	 * @param line part of the line WITHOUT the comment
	 * @param comment the comment part of the line
	 * @param lastLineWasSection flag indicating, if the last line described a section
	 * @return true if comment can be interpreted as a section description, false otherwise
	 */
	private boolean isLineSectionDescription(String line, String comment, boolean lastLineWasSection) {
		if (comment.isEmpty()) {
			return false;
		}
		return lastLineWasSection && line.isEmpty();
	}

	/**
	 * Check if a character can be on the beginning of a section name
	 * @param c the character in question
	 * @return true if the character could be on the beginning of  a section name, false otherwise
	 */
	private boolean isNameChar(char c) {
		return Character.toString(c).matches("[[a-z][A-Z][0-9].$: _~-]");
	}

	/**
	 * Check if a character could be in a section name
	 * @param c the character in question
	 * @return true if the character could be in a section name, false otherwise
	 */
	private boolean isNotBeginningChar(char c) {
		return ! Character.toString(c).matches("[[a-z][A-Z].$:]");
	}

	/**
	 * Check if a supplied line is valid section label line according to the INI documentation
	 * @param line the line in question
	 * @return true if a valid section label line, false otherwise
	 */
	private boolean isDescribingSection(String line) {
		return !line.isEmpty() && line.charAt(0) == '[';
	}

	/**
	 *
	 * @param outValue
	 * @param separator
	 * @return
	 */
	private String escapeSeparator(String outValue, String separator) {
		if (outValue.contains(separator)) {
			// cycle through the string and find all separators to be escaped
			ArrayList<Integer> occurrences = new ArrayList<>();
			int index = outValue.indexOf(separator);
			while (index >= 0) {
				occurrences.add(index);
				index = outValue.indexOf(separator, index + 1);
			}

			// escape the separators
			StringBuilder escapedOutValue = new StringBuilder(outValue);
			for (int i = occurrences.size() - 1 ; i >= 0; --i) {
				escapedOutValue.insert(occurrences.get(i),"\\");
			}
			return escapedOutValue.toString();
		} else {
			return outValue;
		}
	}

	/**
	 * Cycle through value String and count leading and trailing spaces.
	 * Once we have the counts, we trim the value and add "\ " in front for every
	 * leading space, and the same for the trailing spaces. This will also
	 * convert any surrounding whitespace to space.
	 * @param value
	 * @return value with escaped trailing and leading spaces
	 */
	private String escapeSpaces(String value) {
		// count leading and trailing spaces
		boolean valueStarted = false;
		int prefixSpaces = 0;
		int suffixSpaces = 0;
		for (int i = 0; i < value.length(); ++i) {
			char currentChar = value.charAt(i);
			if (Character.isWhitespace(currentChar)) {
				if (valueStarted) {
					++suffixSpaces;
				} else {
					++prefixSpaces;
				}
			} else {
				valueStarted = true;
				suffixSpaces = 0;
			}
		}

		// return the whitespaces as escaped spaces
		StringBuilder outputValue = new StringBuilder(value.trim());
		for (int i = 0; i<prefixSpaces; ++i) {
			outputValue.insert(0,"\\ ");
		}
		for (int i = 0; i<suffixSpaces; ++i) {
			outputValue.append("\\ ");
		}
		return outputValue.toString();
	}

	/**
	 * Determine if a ConfigNode is not a section
	 * Just another pretty wrapper
	 * @param node the node in question
	 * @return true if not a section, false otherwise
	 */
	private boolean isNotSection(ConfigNode node) {
		return !(node instanceof Section);
	}

}

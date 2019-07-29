package tascombank;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

	private static final String TASK_REGEX = "^I-\\d{4,6}";
	private static final String SERVICE_REGEX = "^\\w+-service";

	private static StringBuilder data = new StringBuilder();

	private static LinkedHashMap<String, Set<String>> tasks = new LinkedHashMap<>();

	public static void main(String[] args) throws Exception {
		if (args != null && args.length > 0) {
			File file = new File(args[0]);
			if (file.exists()) {
				read(file);
				updateFile(file.getAbsolutePath());
			}
		}
	}

	/**
	 * Takes input filePath and adds "_updated" text
	 *
	 * @param filePath old file name
	 * @return new name
	 */
	private static String createNewFileName(String filePath) {
		int extensionStart = filePath.lastIndexOf(".");
		return filePath.substring(0, extensionStart) + "_updated" + filePath.substring(extensionStart);
	}


	/**
	 * Reads file, counts the task featuring in the services
	 *
	 * @param file file to read
	 */
	private static void read(File file) {
		try (FileInputStream inputStream = new FileInputStream(file);
			 DataInputStream in = new DataInputStream(inputStream);
			 BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			String line;
			String currentService = null;
			while ((line = br.readLine()) != null) {
				String tmp;

				if (!(tmp = findService(line)).isEmpty()) {
					currentService = tmp;
				}
				if (!(tmp = findTask(line)).isEmpty()) {
					if (!tasks.containsKey(tmp)) {
						tasks.put(tmp, new LinkedHashSet<>());
					}

					tasks.get(tmp).add(currentService);
				}

				data.append(line);
				data.append(System.getProperty("line.separator"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks the line for some pattern availability
	 *
	 * @param line          line to check
	 * @param patternString pattern to find
	 * @return found matching line
	 */
	private static String checkPattern(String line, String patternString) {
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(line);

		return matcher.find() ? matcher.group().trim() : "";
	}

	/**
	 * Checks the line for task availability
	 *
	 * @param line line to check
	 * @return found task
	 */
	private static String findTask(String line) {
		return checkPattern(line, TASK_REGEX);
	}

	/**
	 * Checks the line for service availability
	 *
	 * @param line line to check
	 * @return found service
	 */
	private static String findService(String line) {
		return checkPattern(line, SERVICE_REGEX);
	}

	/**
	 * Creates new file with updated information
	 *
	 * @param oldFilePath old file path with name
	 */
	private static void updateFile(String oldFilePath) {
		try (BufferedWriter outputStream = new BufferedWriter(new FileWriter(createNewFileName(oldFilePath)))) {
			String[] oldFileData = data.toString().split(System.getProperty("line.separator"));

			String currentService = null;
			for (String line : oldFileData) {
				StringBuilder newLine = new StringBuilder(line.trim());
				String tmp;

				if (!(tmp = findService(line)).isEmpty()) {
					currentService = tmp;
				}
				if (!(tmp = findTask(line)).isEmpty()) {
					for (String service : tasks.get(tmp)) {
						if (!currentService.equals(service)) {
							newLine.append("\t\t").append(service);
						}
					}
				}

				outputStream.write(newLine.toString());
				outputStream.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

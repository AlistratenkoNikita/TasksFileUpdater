package tascombank;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

	private static final String SERVICE_BLOCK_SEPERATOR = "#######################################################" +
			"########################################################";
	private static final String TASK_REGEX = "\\nI-\\d{4,6}";
	private static final String SERVICE_REGEX = "\\n(\\w+-service)";
	private static StringBuilder data = new StringBuilder();
	private static List<String> services = new LinkedList<>();

	private static LinkedHashMap<String, Set<String>> tasks = new LinkedHashMap<>();

	public static void main(String[] args) throws Exception {
		if (args != null && args.length > 0) {
			File file = new File(args[0]);
			if (file.exists()) {
				prettify(file);
				readFile(file);

				readServices();

				countTasksEntries();

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
	 * Reads the names of the services in the file
	 */
	private static void readServices() {
		Pattern pattern = Pattern.compile(SERVICE_REGEX);
		Matcher matcher = pattern.matcher(data);

		while (matcher.find()) {
			services.add(matcher.group().trim());
		}
	}

	/**
	 * Counts where every task is featured
	 */
	private static void countTasksEntries() {
		String[] blocks = data.toString().split(SERVICE_BLOCK_SEPERATOR);

		for (int i = 0; i < blocks.length; i++) {
			for (String task : findTasks(blocks[i])) {
				if (!tasks.containsKey(task.trim())) {
					tasks.put(task.trim(), new LinkedHashSet<>());
				}

				tasks.get(task.trim()).add(services.get(i));
			}
		}
	}

	/**
	 * Finds all tasks from the file
	 *
	 * @param block String where to find the task in
	 * @return List of all represented tasks
	 */
	private static List<String> findTasks(String block) {
		List<String> result = new LinkedList<>();
		Pattern pattern = Pattern.compile(TASK_REGEX);
		Matcher matcher = pattern.matcher(block);

		while (matcher.find()) {
			result.add(matcher.group().trim());
		}

		return result;
	}

	/**
	 * Reads file to String
	 *
	 * @param file File to read
	 * @throws Exception any problem with file reading
	 */
	private static void readFile(File file) throws Exception {
		FileInputStream inputStream = new FileInputStream(file);
		DataInputStream in = new DataInputStream(inputStream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		String line;
		while ((line = br.readLine()) != null) {
			data.append(line);
			data.append(System.getProperty("line.separator"));
		}
		inputStream.close();
	}

	/**
	 * Adds first empty line
	 *
	 * @param file file to add new line to
	 * @throws Exception any problem with file reading
	 */
	private static void prettify(File file) throws Exception {
		FileInputStream inputStream = new FileInputStream(file);
		DataInputStream in = new DataInputStream(inputStream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		StringBuilder newData = new StringBuilder();

		String line;
		boolean firstLine = true;
		while ((line = br.readLine()) != null) {
			if (firstLine) {
				if (!line.isEmpty()) {
					newData.append(System.getProperty("line.separator"));
				}
				firstLine = false;
			}

			newData.append(line);
			newData.append(System.getProperty("line.separator"));
		}
		inputStream.close();

		FileOutputStream outputStream = new FileOutputStream(new File(file.getAbsolutePath()));
		outputStream.write(newData.toString().getBytes());
		outputStream.close();

	}

	/**
	 * Creates new file with updated information for tasks featuring in other services
	 *
	 * @param oldFilePath file with
	 * @throws Exception any problem with file writing
	 */
	private static void updateFile(String oldFilePath) throws Exception {
		BufferedWriter outputStream = new BufferedWriter(new FileWriter(createNewFileName(oldFilePath)));
		String[] oldFileData = data.toString().split(System.getProperty("line.separator"));

		int serviceCounter = 0;

		for (String oldFileDatum : oldFileData) {
			if (oldFileDatum.contains(SERVICE_BLOCK_SEPERATOR)) {
				serviceCounter++;
			}

			StringBuilder newLine = new StringBuilder(oldFileDatum.trim());
			if (tasks.containsKey(oldFileDatum.trim())) {
				for (String service : tasks.get(oldFileDatum.trim())) {
					if (!service.equals(services.get(serviceCounter)))
						newLine.append("\t\t").append(service);
				}
			}

			outputStream.write(newLine.toString());
			outputStream.newLine();
		}

		outputStream.close();
	}
}

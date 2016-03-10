package utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.uima.util.FileUtils;

public class FileUtil {

	private final static Logger logger = Logger.getLogger(FileUtil.class
			.getName());

	public static String removeFileNameExtension(String fileName) {
		return fileName.substring(0, fileName.lastIndexOf("."));
	}

	/**
	 * A facade to save string to a file. Uses UIMA's FileUtils at the moment.
	 * 
	 * @param fileContent
	 * @param file
	 */
	public static void saveFile(String fileContent, File file) {

		try {

			FileUtils.saveString2File(fileContent, file);

		} catch (IOException e) {

			logger.severe(e.getMessage());
			throw new RuntimeException(e);
		}
	}

	public static String readFile(File modelFile) {
		try {

			return FileUtils.file2String(modelFile, "UTF-8");

		} catch (IOException e) {

			logger.severe(e.getMessage());
			throw new RuntimeException(e);
		}
	}
}

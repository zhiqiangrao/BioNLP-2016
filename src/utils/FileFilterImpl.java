package utils;

import java.io.File;
import java.io.FileFilter;

public class FileFilterImpl implements FileFilter {

	private String extension;

	public FileFilterImpl(String extension) {

		this.extension = extension;
	}

	@Override
	public boolean accept(File file) {

		if (file.getName().endsWith(this.extension)) {
			return true;
		}
		return false;
	}

}

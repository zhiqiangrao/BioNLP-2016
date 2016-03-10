package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.logging.Logger;

import edu.stanford.nlp.hcoref.Preprocessor;

/**
 * Genia Sentence Splitter.
 *
 */
public class GeniaSS {
	
	private final static Logger logger = Logger.getLogger(GeniaSS.class.getName());

	public static void geniaSS() {
		
		String root = System.getProperty("user.dir");
		File txtdir = new File(root + "/data/see-dev/txt-normalization/dev");
		File outdir = new File(root + "/data/see-dev/geniass/dev");
		
		if (!outdir.exists())
			outdir.mkdirs();
		
		if (txtdir.isDirectory()) {
			File[] txtfiles = txtdir.listFiles(new FileFilterImpl(".txt"));
			Arrays.sort(txtfiles);
			for (File txtfile : txtfiles) {
				File input = txtfile;
				File output = new File(outdir.getPath() + "/" + FileUtil.removeFileNameExtension(txtfile.getName()) + ".ss");
	
				//File inputFolder = new File(root + "/tools/geniass/");
				//ProcessBuilder pb = new ProcessBuilder(root + "/tools/geniass/geniass", input.getPath(), output.getPath());
				File inputFolder = new File("/home/raines/geniass");
				ProcessBuilder pb = new ProcessBuilder("/home/raines/geniass/geniass", input.getPath(), output.getPath());
				
				pb.directory(inputFolder);
				Process process;
				try {
					process = pb.start();
					BufferedReader r = new BufferedReader(new InputStreamReader(
						process.getInputStream()));
					String line;
					while ((line = r.readLine()) != null) {
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}
	
	public static void main(String[] args) {
		
		geniaSS();
	
	}
	
}

package utils;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Logger;

public class TxtNormalization {
	
	private final static Logger logger = Logger.getLogger(TxtNormalization.class.getName());

	public static void txtNormalize() {
		
		String root = System.getProperty("user.dir");
		File txtdir = new File(root + "/data/BioNLP-ST-2016_SeeDev-binary_test");
		File outdir = new File(root + "/data/see-dev/txt-normalization/test");
		//File outdir = new File(root + "/SeeDev-full_train");
		
		if (!outdir.exists())
			outdir.mkdirs();
		
		if (txtdir.isDirectory()) {
			File[] txtfiles = txtdir.listFiles(new FileFilterImpl(".txt"));
			Arrays.sort(txtfiles);
			for (File txtfile : txtfiles) {
				File input = txtfile;
				File output = new File(outdir.getPath() + "/" + txtfile.getName());
				String str = FileUtil.readFile(input);
				str = str.replaceAll("\n", " ");
				FileUtil.saveFile(str, output);
			}
		}

	}
	
	public static void main(String[] args) {
		
		txtNormalize();
	
	}
	
}

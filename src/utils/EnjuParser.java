package utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * convert enju parser XML files to PTB files.
 *
 */
public class EnjuParser {
	
	private final static Logger logger = Logger.getLogger(EnjuParser.class.getName());
	
	public void xml2Ptb() {
		
		String root = System.getProperty("user.dir");
		File outdir = new File(root + "/data/see-dev/enju_ptb/dev");
		File xmldir = new File(root + "/data/see-dev/enju_parser/dev");
		
		
		
		if (!outdir.exists())
			outdir.mkdirs();	
		
		if (xmldir.isDirectory()) {
			File[] xmlfiles = xmldir.listFiles(new FileFilterImpl(".xml"));
			//Arrays.sort(ssfiles);
			int filenum = 0;
			for (File xmlfile : xmlfiles) {
				File output = new File(outdir.getPath() + "/" + FileUtil.removeFileNameExtension(xmlfile.getName()) + ".ptb");
				logger.info("Extracting from file: " + xmlfile.getName() + " " + (++filenum));
				try {   
				    // xml2ptb
					File xmlin = new File("/home/raines/enju/share/enju2ptb/xmlin.xml");
					File ptbout = new File("/home/raines/enju/share/enju2ptb/ptbout.ptb");
				    FileUtil.saveFile(FileUtil.readFile(xmlfile), xmlin);
				    
				    try {
						Thread.currentThread().sleep(1000);//延时，ms
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				    
				    ProcessBuilder pb = new ProcessBuilder("/home/raines/enju/share/enju2ptb/myconvert");
					Process process = pb.start();
					
					try {
						Thread.currentThread().sleep(1000);//延时，ms
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					String ptbstr = FileUtil.readFile(ptbout);
					ptbstr = ptbstr.replace("(TOP", "(ROOT");
					FileUtil.saveFile(ptbstr, output);
					xmlin.delete();
					ptbout.delete();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public static void main(String[] args) {
		
		//EnjuParser ep = new EnjuParser();
		//ep.xml2Ptb();
		
	}
	
}

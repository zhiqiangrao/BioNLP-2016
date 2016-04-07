package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class E2R {

	public static void main(String[] args) {
		
		File dir = new File("./4.6-2");
		if (dir.isDirectory()) {
			File[] files = dir.listFiles(new FileFilterImpl(".a2"));
			for (File file : files) {
			    BufferedReader br;
			    StringBuilder sb = new StringBuilder();
			    String s;
				try {
					br = new BufferedReader(new FileReader(file));
				    while ((s = br.readLine()) != null) {
				    	//if (s.contains("Binds_To")
				    	//		|| s.contains("Interacts_With")
				    	//		|| s.contains("Is_Linked_To"))  {
				    		sb.append("R" + s.substring(1));
				    		sb.append("\n");
				    	//}
				    }
				    br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			    FileUtil.saveFile(sb.toString(), file);
			}
		}

	}

}

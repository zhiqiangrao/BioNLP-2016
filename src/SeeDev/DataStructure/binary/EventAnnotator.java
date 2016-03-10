package SeeDev.DataStructure.binary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import utils.FileUtil;


/**
 * Read binary a2 file.
 * 
 */
public class EventAnnotator {

	private final static Logger logger = Logger.getLogger(EventAnnotator.class
			.getName());

	public void process(String filename, ArrayList<Event> events) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(FileUtil.removeFileNameExtension(filename).concat(".a2"))));
			String line;
			while ((line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line);
				String id = st.nextToken();
				EventTypes eventType = EventTypes.valueOf(st.nextToken());
				String arg1 = st.nextToken();
				String arg2 = st.nextToken();
				
				Event event = new Event();
				events.add(event);
				event.id = Integer.parseInt(id.substring(1));
				event.eventType = eventType;
				event.arg1 = Integer.parseInt(arg1.substring(arg1.lastIndexOf("T") + 1));
				event.arg2 = Integer.parseInt(arg2.substring(arg2.lastIndexOf("T") + 1));
			}

			br.close();

		} catch (IOException e) {
			logger.severe(e.getMessage());
			throw new RuntimeException(e);
		}

	}
	
	
	public static void main(String[] args) {
		
		EventAnnotator ea = new EventAnnotator();
		ArrayList<Event> events = new ArrayList<Event>();
		ea.process("./data/BioNLP-ST-2016_SeeDev-binary_train/SeeDev-binary-9657152-1.txt", events);
		
	}

}

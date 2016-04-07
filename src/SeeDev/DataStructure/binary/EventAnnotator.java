package SeeDev.DataStructure.binary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import utils.FileUtil;
import SeeDev.DataStructure.Entity;


/**
 * Read binary a2 file.
 * 
 */
public class EventAnnotator {

	private final static Logger logger = Logger.getLogger(EventAnnotator.class
			.getName());

	public void process(File a2file, List<Event> events, List<Entity> entities) {
		String filename = a2file.getPath();
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
				event.ent1Id = Integer.parseInt(arg1.substring(arg1.lastIndexOf("T") + 1));
				event.ent2Id = Integer.parseInt(arg2.substring(arg2.lastIndexOf("T") + 1));
				
				for (Entity entity : entities) {
					if (entity.id == event.ent1Id)
						event.entity1 = entity;
					if (entity.id == event.ent2Id)
						event.entity2 = entity;
					if (event.entity1 != null && event.entity2 != null)
						break;
				}
			}

			br.close();

		} catch (IOException e) {
			logger.severe(e.getMessage());
			throw new RuntimeException(e);
		}

	}
	
	
	public static void main(String[] args) {
		
//		EventAnnotator ea = new EventAnnotator();
//		ArrayList<Event> events = new ArrayList<Event>();
//		ea.process("./data/BioNLP-ST-2016_SeeDev-binary_train/SeeDev-binary-9657152-1.txt", events);
		
	}

}

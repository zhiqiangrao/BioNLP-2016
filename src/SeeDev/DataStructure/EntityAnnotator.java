package SeeDev.DataStructure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import utils.FileUtil;


/**
 * Read a1 file.
 * 
 */
public class EntityAnnotator {

	private final static Logger logger = Logger.getLogger(EntityAnnotator.class
			.getName());

	public void process(File file, Annotation document, ArrayList<Entity> entities) {
		String filename = file.getPath();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(FileUtil.removeFileNameExtension(filename).concat(".a1"))));
			String line;
			while ((line = br.readLine()) != null) {
				String[] strs = line.split("\t");
				String id = strs[0];
				
				String[] strs2 = strs[1].split(" ");
				EntityTypes entityType = EntityTypes.valueOf(strs2[0]);
				int start = Integer.parseInt(strs2[1]);
				int end;
				int start2 = -1;
				int end2 = -1;
				if (strs2.length == 3) {
					end = Integer.parseInt(strs2[2]);
				} else {
					String[] strs3 = strs2[2].split(";");
					end = Integer.parseInt(strs3[0]);
					start2 = Integer.parseInt(strs3[1]);
					end2 = Integer.parseInt(strs2[3]);
				}
				
				String name = strs[2];
				
				Entity entity = new Entity();
				entities.add(entity);
				entity.id = Integer.parseInt(id.substring(1));
				entity.entityType = entityType;
				entity.start = start;
				entity.end = end;
				entity.start2 = start2;
				entity.end2 = end2;
				entity.name = name;
				
				for ( CoreMap sen : document.get(CoreAnnotations.SentencesAnnotation.class)) {
					if (start >= sen.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class)
							&& end <= sen.get(CoreAnnotations.CharacterOffsetEndAnnotation.class)) {
						
						entity.senid = sen.get(CoreAnnotations.SentenceIndexAnnotation.class) + 1;
						
						int tokstart = 0;
						int tokend = 0;
						for (CoreLabel token : sen.get(CoreAnnotations.TokensAnnotation.class)) {
							if (token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class) <= start)
								tokstart = token.get(CoreAnnotations.IndexAnnotation.class);
							if (token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class) >= start) {
								tokend = token.get(CoreAnnotations.IndexAnnotation.class);
								break;
							}
						}
						
						Tree root = sen.get(TreeCoreAnnotations.TreeAnnotation.class);
						Iterator<Tree> rootiter = root.iterator();
						while (rootiter.hasNext()) {
							Tree tree = rootiter.next();
							//List<Tree> mLeaves = mtree.getLeaves();
					        //int beginIdx = ((CoreLabel)mLeaves.get(0).label()).get(CoreAnnotations.IndexAnnotation.class)-1;
					        //int endIdx = ((CoreLabel)mLeaves.get(mLeaves.size()-1).label()).get(CoreAnnotations.IndexAnnotation.class);
							List<Tree> leaves = tree.getLeaves();
							int a = ((CoreLabel)leaves.get(0).label()).get(CoreAnnotations.IndexAnnotation.class);
							int b = ((CoreLabel)leaves.get(leaves.size()-1).label()).get(CoreAnnotations.IndexAnnotation.class);
							
							if (a == start && b == end)
								entity.tree = tree;
							
							if (start2 != -1 && end2 != -1 && a == start2 && b == end2)
								entity.tree2 = tree;
							
						}
						
						break;
					}
				}
				
				
				
			}

			br.close();

		} catch (IOException e) {
			logger.severe(e.getMessage());
			throw new RuntimeException(e);
		}

	}
	
	
	public static void main(String[] args) {
		
		EntityAnnotator ea = new EntityAnnotator();
		ArrayList<Entity> entities = new ArrayList<Entity>();
		//ea.process(new File("./data/BioNLP-ST-2016_SeeDev-binary_train/SeeDev-binary-9657152-1.txt"), entities);
		System.out.println();
		
	}

}

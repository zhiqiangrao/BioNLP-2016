package SeeDev;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import utils.FileFilterImpl;
import utils.FileUtil;
import utils.Word2Vec;
import SeeDev.DataStructure.Entity;
import SeeDev.DataStructure.EntityAnnotator;
import SeeDev.DataStructure.EntityTypes;
import SeeDev.DataStructure.binary.Event;
import SeeDev.DataStructure.binary.EventAnnotator;
import SeeDev.DataStructure.binary.EventTypes;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

public class Train2 {

	private final static Logger logger = Logger.getLogger(Train2.class.getName());

	public static void main(String[] args) {

		train();
	}

	private final static File dir = new File("./data/binary_train+devel");
	private final static File txtdir = new File("./data/see-dev/txt-normalization/train+devel");
	private final static File stantreedir = new File("./data/see-dev/stanfordtree/train+devel");
	public static Map<String,double[]> word2vec = Word2Vec.getWord2Vec(new File("./tools/seedev-word2vec-50-2.txt"));
	
	private static void train() {

		Properties props = new Properties();
		//props.put("annotators", "tokenize, ssplit, pos, lemma, parse");
		props.put("annotators", "tokenize, ssplit, pos, lemma");
		props.put("tokenize.language", "English");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		List<Entity> allentities = new ArrayList<Entity>();
		List<Event> allevents = new ArrayList<Event>();
		List<Instance> allinstances = new ArrayList<Instance>();

		extractFromFiles(pipeline, allentities, allevents, allinstances);
		
		 Instance.instances2NumericFeature(allinstances);
		 //Instance.saveDictionary(new File("./model/train.dict"));
		 
//		 LibLinearFacade lib = new LibLinearFacade();
//		 lib.train(allinstances);
//		 lib.saveModel(new File("./model/train.model"));
		 
		 //LibSVMFacade lib = new LibSVMFacade();
		 //lib.train(allinstances);
		 //lib.saveModel("./model/train.libsvm.model");
		 
		 Test.test();
	}

	private static void extractFromFiles(StanfordCoreNLP pipeline, List<Entity> allentities, List<Event> allevents,
			List<Instance> allinstances) {
		if (dir.isDirectory()) {
			File[] files = dir.listFiles(new FileFilterImpl(".txt"));
			Arrays.sort(files);
			int filenum = 0;
			for (File file : files) {

				logger.info("Extracting from file: " + file.getName() + " " + (++filenum));

				List<Entity> entities = new ArrayList<Entity>();
				List<Event> events = new ArrayList<Event>();
				List<Instance> instances = new ArrayList<Instance>();

				extractFromSingleFile(pipeline, file, entities, events, instances);

				allentities.addAll(entities);
				allevents.addAll(events);
				allinstances.addAll(instances);
			}
		}
	}

	private static void extractFromSingleFile(StanfordCoreNLP pipeline, File file, List<Entity> entities, List<Event> events,
			List<Instance> instances) {

		File txtfile = new File(txtdir.getPath() + "/" + FileUtil.removeFileNameExtension(file.getName()) + ".txt");
		File stantreefile = new File(stantreedir.getPath() + "/" + FileUtil.removeFileNameExtension(file.getName()) + ".ptb");

		String text = FileUtil.readFile(txtfile);
		Annotation document = new Annotation(text);
		pipeline.annotate(document);
		
//		document.get(CoreAnnotations.SentencesAnnotation.class).get(0).get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class)
//		.getShortestUndirectedPathEdges(
//				new IndexedWord(document.get(CoreAnnotations.SentencesAnnotation.class).get(0).get(CoreAnnotations.TokensAnnotation.class).get(10))
//				, new IndexedWord(document.get(CoreAnnotations.SentencesAnnotation.class).get(0).get(CoreAnnotations.TokensAnnotation.class).get(0))
//				)
		
		// save Stanford parsing tree
		// StringBuilder sb = new StringBuilder();
		// for (CoreMap s :
		// document.get(CoreAnnotations.SentencesAnnotation.class)) {
		// Tree t = s.get(TreeCoreAnnotations.TreeAnnotation.class);
		// sb.append(t.toString()).append("\n");
		// }
		// FileUtil.saveFile(sb.toString(), stantreefile);

		setTreeAnnotation(document, stantreefile);

		File a1file = new File(FileUtil.removeFileNameExtension(file.getPath()) + ".a1");
		EntityAnnotator entityannotator = new EntityAnnotator();
		entityannotator.process(a1file, document, entities);

		File a2file = new File(FileUtil.removeFileNameExtension(file.getPath()) + ".a2");
		EventAnnotator eventannotator = new EventAnnotator();
		eventannotator.process(a2file, events, entities);

		// positive instances
		for (Event event : events) {
			Instance instance = new Instance();
			instances.add(instance);
			instance.event2Instance(document, event);
		}
		
		// negative instances
		for (Entity ent1 : entities) {
			for (Entity ent2 : entities) {
				if (ent2 == ent1)
					continue;
				if (ent2.senId != ent1.senId)
					continue;
				//if ((ent2.start >= ent1.start && ent2.start <= ent1.end) || (ent2.end >= ent1.start && ent2.end <= ent1.end)
				//		|| (ent1.start >= ent2.start && ent1.start <= ent2.end) || (ent1.end >= ent2.start && ent1.end <= ent2.end))
				//	continue;
				
				if (!isGoldEvent(ent1, ent2, events)) {
					Event eve = new Event();
					eve.eventType = EventTypes.Null;
					eve.entity1 = ent1;
					eve.entity2 = ent2;
					
					Instance instance = new Instance();
					instances.add(instance);
					instance.event2Instance(document, eve);
				}
			}
		}


	}

	private static boolean isGoldEvent(Entity ent1, Entity ent2, List<Event> events) {
		for (Event eve : events) {
			if (eve.entity1 == ent1 && eve.entity2 ==  ent2)
				return true;
		}
		return false;
	}

	public static void setTreeAnnotation(Annotation document, File file) {
		BufferedReader br;
		String s;
		int index = 0;
		try {
			br = new BufferedReader(new FileReader(file));
			while ((s = br.readLine()) != null) {
				document.get(CoreAnnotations.SentencesAnnotation.class).get(index++).set(TreeCoreAnnotations.TreeAnnotation.class, Tree.valueOf(s));
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}

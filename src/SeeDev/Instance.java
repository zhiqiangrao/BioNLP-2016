package SeeDev;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import utils.FileUtil;
import SeeDev.DataStructure.Entity;
import SeeDev.DataStructure.EntityTypes;
import SeeDev.DataStructure.binary.Event;
import SeeDev.DataStructure.binary.EventTypes;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;

public class Instance {

	private final static Logger logger = Logger.getLogger(Instance.class.getName());

	public List<Integer> vectors;
	public int label;
	
	// instance feature
	public EventTypes eventType;
	
	//private EntityTypes ent1Type;
//	private int ent1TreeDepth;
//	private List<String> ent1Word;
//	private List<String> ent1Lemma;
//	private List<String> ent1POS;
//	private String ent1PreWord;
//	private String ent1PreLemma;
//	private String ent1PrePOS;
//	private String ent1NextWord;
//	private String ent1NextLemma;
//	private String ent1NextPOS;
	public Entity ent1;
	
	//private EntityTypes ent2Type;
//	private int ent2TreeDepth;
//	private List<String> ent2Word;
//	private List<String> ent2Lemma;
//	private List<String> ent2POS;
//	private String ent2PreWord;
//	private String ent2PreLemma;
//	private String ent2PrePOS;
//	private String ent2NextWord;
//	private String ent2NextLemma;
//	private String ent2NextPOS;
	public Entity ent2;
	
	public String treePath;
	public int treePathLength;
	public int tokenDist;
	public int mentionDist;
	
	public List<String> midLemmas;

//	private int ent1TypeFeature;
//	private int ent2TypeFeature;
//	private int treePathFeature;
//	private int treePathLenFeature;
//	private int tokDistFeature;
//	private int menDistFeature;
	
	// Dictionary
	static Map<EventTypes, Integer> eventTypeDict;
	
	static Map<EntityTypes, Integer> ent1TypeDict;
	static Map<String, Integer> ent1WordDict;
	static Map<String, Integer> ent1LemmaDict;
	static Map<String, Integer> ent1POSDict;
	static Map<String, Integer> ent1PreWordDict;
	static Map<String, Integer> ent1PreLemmaDict;
	static Map<String, Integer> ent1PrePOSDict;
	static Map<String, Integer> ent1NextWordDict;
	static Map<String, Integer> ent1NextLemmaDict;
	static Map<String, Integer> ent1NextPOSDict;
	static Map<Integer, Integer> ent1TreeDepthDict;
	
	static Map<EntityTypes, Integer> ent2TypeDict;
	static Map<String, Integer> ent2WordDict;
	static Map<String, Integer> ent2LemmaDict;
	static Map<String, Integer> ent2POSDict;
	static Map<String, Integer> ent2PreWordDict;
	static Map<String, Integer> ent2PreLemmaDict;
	static Map<String, Integer> ent2PrePOSDict;
	static Map<String, Integer> ent2NextWordDict;
	static Map<String, Integer> ent2NextLemmaDict;
	static Map<String, Integer> ent2NextPOSDict;
	static Map<Integer, Integer> ent2TreeDepthDict;
	
	static Map<String, Integer> treePathDict;
	static Map<Integer, Integer> treePathLenDict;
	static Map<Integer, Integer> tokDistDict;
	static Map<Integer, Integer> menDistDict;

	// bag of words lemma dict
	static Map<String, Integer> bowLemmaDict;

	public void event2Instance(Annotation document, Event event) {

		eventType = event.eventType;
		ent1 = event.entity1;
		ent2 = event.entity2;
		
		//ent1Type = ent1.entityType;
//		ent1TreeDepth = ent1.treeDepth;
//		ent1Word = ent1.words;
//		ent1Lemma = ent1.lemmas;
//		ent1POS = ent1.POSs;
//		ent1PreWord = ent1.preWord;
//		ent1PreLemma = ent1.preLemma;
//		ent1PrePOS = ent1.prePOS;
//		ent1NextWord = ent1.nextWord;
//		ent1NextLemma = ent1.nextLemma;
//		ent1NextPOS = ent1.nextPOS;

		//ent2Type = ent2.entityType;
//		ent2TreeDepth = ent2.treeDepth;
//		ent2Word = ent2.words;
//		ent2Lemma = ent2.lemmas;
//		ent2POS = ent2.POSs;
//		ent2PreWord = ent2.preWord;
//		ent2PreLemma = ent2.preLemma;
//		ent2PrePOS = ent2.prePOS;
//		ent2NextWord = ent2.nextWord;
//		ent2NextLemma = ent2.nextLemma;
//		ent2NextPOS = ent2.nextPOS;
		
		
		if (ent1.senId == ent2.senId) {
			Tree root = document.get(CoreAnnotations.SentencesAnnotation.class).get(ent1.senId - 1)
					.get(TreeCoreAnnotations.TreeAnnotation.class);
			Tree t1 = ent1.tree;
			Tree t2 = ent2.tree;
			List<Tree> pathtrees = root.pathNodeToNode(t1, t2);
//			for (Tree tree : pathtrees) {
//				if (tree.dominates(t1) && tree.dominates(t2))
//					comtree = tree;
//			}
			String pathstr = null;
			//for (Tree tree : pathtrees) {
			//	if (pathstr == null)
			//		pathstr = tree.label().value();
			//	else
			//		pathstr = pathstr + "-" + tree.label().value();
			//}
			if (ent1.end < ent2.start) {
				for (Tree tree : pathtrees) {
					if (pathstr == null)
						pathstr = tree.label().value();
					else
						pathstr = pathstr + ">" + tree.label().value();
				}
			} else {
				for (Tree tree : pathtrees) {
					if (pathstr == null)
						pathstr = tree.label().value();
					else
						pathstr = pathstr + "<" + tree.label().value();
				}
			}
			
			treePath = pathstr;
			treePathLength = pathtrees.size();
			mentionDist = ent2.id - ent1.id;
			//tokenDist = ent1.senTokStart - ent2.senTokEnd;
			tokenDist = (ent2.senTokStart - ent1.senTokEnd > 0 ? ent2.senTokStart - ent1.senTokEnd 
					: ent1.senTokStart - ent2.senTokEnd);
			
			
			// middle lemmas
			List<CoreLabel> tokens = document.get(CoreAnnotations.SentencesAnnotation.class).get(ent1.senId-1)
																		.get(CoreAnnotations.TokensAnnotation.class);
			int start, end;
			if (ent2.senTokStart - ent1.senTokEnd > 0) {
				start = ent1.senTokEnd + 1;
				end = ent2.senTokStart - 1;
			} else {
				start = ent2.senTokEnd + 1;
				end = ent1.senTokStart -1;
			}
			midLemmas = new ArrayList<String>();
			for (int i = start; i <= end; i++) {
				midLemmas.add(tokens.get(i - 1).lemma());
			}

		}

	}

	public static void instances2NumericFeature(List<Instance> instances) {
		
		eventTypeDict = new HashMap<EventTypes, Integer>();
		int label = 0;
		for (EventTypes eve : EventTypes.values()) {
			eventTypeDict.put(eve, label++);
		}
	
		int index = 1;
		ent1TypeDict = new HashMap<EntityTypes, Integer>();
		for (EntityTypes ent : EntityTypes.values()) {
			ent1TypeDict.put(ent, index++);
		}
		ent2TypeDict = new HashMap<EntityTypes, Integer>();
		for (EntityTypes ent : EntityTypes.values()) {
			ent2TypeDict.put(ent, index++);
		}
		
		ent1WordDict = new HashMap<String, Integer>();
		ent1LemmaDict = new HashMap<String, Integer>();
		ent1POSDict = new HashMap<String, Integer>();
		ent1PreWordDict = new HashMap<String, Integer>();
		ent1PreLemmaDict = new HashMap<String, Integer>();
		ent1PrePOSDict = new HashMap<String, Integer>();
		ent1NextWordDict = new HashMap<String, Integer>();
		ent1NextLemmaDict = new HashMap<String, Integer>();
		ent1NextPOSDict = new HashMap<String, Integer>();
		ent1TreeDepthDict = new HashMap<Integer, Integer>();
		
		
		ent2WordDict = new HashMap<String, Integer>();
		ent2LemmaDict = new HashMap<String, Integer>();
		ent2POSDict = new HashMap<String, Integer>();
		ent2PreWordDict = new HashMap<String, Integer>();
		ent2PreLemmaDict = new HashMap<String, Integer>();
		ent2PrePOSDict = new HashMap<String, Integer>();
		ent2NextWordDict = new HashMap<String, Integer>();
		ent2NextLemmaDict = new HashMap<String, Integer>();
		ent2NextPOSDict = new HashMap<String, Integer>();
		ent2TreeDepthDict = new HashMap<Integer, Integer>();
		
		treePathDict = new HashMap<String, Integer>();
		treePathLenDict = new HashMap<Integer, Integer>();
		tokDistDict = new HashMap<Integer, Integer>();
		menDistDict = new HashMap<Integer, Integer>();
		
		
		// make dictionary
		// ent1
		for (Instance instance : instances) {
			for (String s: instance.ent1.words) {
				if (!ent1WordDict.containsKey(s)) {
				ent1WordDict.put(s, index++);
				}
			}	
		}
		
		for (Instance instance : instances) {
			for (String s: instance.ent1.lemmas) {
				if (!ent1LemmaDict.containsKey(s)) {
					ent1LemmaDict.put(s, index++);
				}
			}	
		}
		
		for (Instance instance : instances) {
			for (String s: instance.ent1.POSs) {
				if (!ent1POSDict.containsKey(s)) {
					ent1POSDict.put(s, index++);
				}
			}	
		}
		
		for (Instance instance : instances) {
			if (!ent1PreWordDict.containsKey(instance.ent1.preWord)) {
				ent1PreWordDict.put(instance.ent1.preWord, index++);
			}
		}
		
		for (Instance instance : instances) {
			if (!ent1PreLemmaDict.containsKey(instance.ent1.preLemma)) {
				ent1PreLemmaDict.put(instance.ent1.preLemma, index++);
			}
		}
		
		for (Instance instance : instances) {
			if (!ent1PrePOSDict.containsKey(instance.ent1.prePOS)) {
				ent1PrePOSDict.put(instance.ent1.prePOS, index++);
			}
		}
		
		for (Instance instance : instances) {
			if (!ent1NextWordDict.containsKey(instance.ent1.nextWord)) {
				ent1NextWordDict.put(instance.ent1.nextWord, index++);
			}
		}
		
		for (Instance instance : instances) {
			if (!ent1NextLemmaDict.containsKey(instance.ent1.nextLemma)) {
				ent1NextLemmaDict.put(instance.ent1.nextLemma, index++);
			}
		}
		
		for (Instance instance : instances) {
			if (!ent1NextPOSDict.containsKey(instance.ent1.nextPOS)) {
				ent1NextPOSDict.put(instance.ent1.nextPOS, index++);
			}
		}
		
		for (Instance instance : instances) {
			if (!ent1TreeDepthDict.containsKey(instance.ent1.treeDepth)) {
				ent1TreeDepthDict.put(instance.ent1.treeDepth, index++);
			}
		}
		
		// ent2
		for (Instance instance : instances) {
			for (String s: instance.ent2.words) {
				if (!ent2WordDict.containsKey(s)) {
				ent2WordDict.put(s, index++);
				}
			}	
		}
		
		for (Instance instance : instances) {
			for (String s: instance.ent2.lemmas) {
				if (!ent2LemmaDict.containsKey(s)) {
					ent2LemmaDict.put(s, index++);
				}
			}	
		}
		
		for (Instance instance : instances) {
			for (String s: instance.ent2.POSs) {
				if (!ent2POSDict.containsKey(s)) {
					ent2POSDict.put(s, index++);
				}
			}	
		}
		
		for (Instance instance : instances) {
			if (!ent2PreWordDict.containsKey(instance.ent2.preWord)) {
				ent2PreWordDict.put(instance.ent2.preWord, index++);
			}
		}
		
		for (Instance instance : instances) {
			if (!ent2PreLemmaDict.containsKey(instance.ent2.preLemma)) {
				ent2PreLemmaDict.put(instance.ent2.preLemma, index++);
			}
		}
		
		for (Instance instance : instances) {
			if (!ent2PrePOSDict.containsKey(instance.ent2.prePOS)) {
				ent2PrePOSDict.put(instance.ent2.prePOS, index++);
			}
		}
		
		for (Instance instance : instances) {
			if (!ent2NextWordDict.containsKey(instance.ent2.nextWord)) {
				ent2NextWordDict.put(instance.ent2.nextWord, index++);
			}
		}
		
		for (Instance instance : instances) {
			if (!ent2NextLemmaDict.containsKey(instance.ent2.nextLemma)) {
				ent2NextLemmaDict.put(instance.ent2.nextLemma, index++);
			}
		}
		
		for (Instance instance : instances) {
			if (!ent2NextPOSDict.containsKey(instance.ent2.nextPOS)) {
				ent2NextPOSDict.put(instance.ent2.nextPOS, index++);
			}
		}
		for (Instance instance : instances) {
			if (!ent2TreeDepthDict.containsKey(instance.ent2.treeDepth)) {
				ent2TreeDepthDict.put(instance.ent2.treeDepth, index++);
			}
		}
		

		//
		for (Instance instance : instances) {	
			if (!treePathDict.containsKey(instance.treePath)) {
				treePathDict.put(instance.treePath, index++);
			}
		}
		
		for (Instance instance : instances) {	
			if (!treePathLenDict.containsKey(instance.treePathLength)) {
				treePathLenDict.put(instance.treePathLength, index++);
			}
		}
		
		for (Instance instance : instances) {	
			if (!tokDistDict.containsKey(instance.tokenDist)) {
				tokDistDict.put(instance.tokenDist, index++);
			}
		}
		
		for (Instance instance : instances) {	
			if (!menDistDict.containsKey(instance.mentionDist)) {
				menDistDict.put(instance.mentionDist, index++);
			}
		}
		
		// middle lemma dict
		bowLemmaDict = new HashMap<String, Integer>();
		for (String s : Train.word2vec.keySet()) {
			bowLemmaDict.put(s, index++);
		}

		//saveDictionary(new File("./model/train.dict"));

		for (Instance instance : instances) {
			instance.label = eventTypeDict.get(instance.eventType);
	
			instance.vectors = new ArrayList<Integer>();
			
			instance.vectors.add(ent1TypeDict.get(instance.ent1.entityType));
			instance.vectors.add(ent2TypeDict.get(instance.ent2.entityType));
			
			// ent1
			for (String s : instance.ent1.words) {
				instance.vectors.add(ent1WordDict.get(s));
			}
			for (String s : instance.ent1.lemmas) {
				instance.vectors.add(ent1LemmaDict.get(s));
			}
			for (String s : instance.ent1.POSs) {
				instance.vectors.add(ent1POSDict.get(s));
			}
			instance.vectors.add(ent1PreWordDict.get(instance.ent1.preWord));
			instance.vectors.add(ent1PreLemmaDict.get(instance.ent1.preLemma));
			instance.vectors.add(ent1PrePOSDict.get(instance.ent1.prePOS));
			instance.vectors.add(ent1NextWordDict.get(instance.ent1.nextWord));
			instance.vectors.add(ent1NextLemmaDict.get(instance.ent1.nextLemma));
			instance.vectors.add(ent1NextPOSDict.get(instance.ent1.nextPOS));
			instance.vectors.add(ent1TreeDepthDict.get(instance.ent1.treeDepth));
			
			// ent2
			
			for (String s : instance.ent2.words) {
				instance.vectors.add(ent2WordDict.get(s));
			}
			for (String s : instance.ent2.lemmas) {
				instance.vectors.add(ent2LemmaDict.get(s));
			}
			for (String s : instance.ent2.POSs) {
				instance.vectors.add(ent2POSDict.get(s));
			}
			instance.vectors.add(ent2PreWordDict.get(instance.ent2.preWord));
			instance.vectors.add(ent2PreLemmaDict.get(instance.ent2.preLemma));
			instance.vectors.add(ent2PrePOSDict.get(instance.ent2.prePOS));
			instance.vectors.add(ent2NextWordDict.get(instance.ent2.nextWord));
			instance.vectors.add(ent2NextLemmaDict.get(instance.ent2.nextLemma));
			instance.vectors.add(ent2NextPOSDict.get(instance.ent2.nextPOS));
			instance.vectors.add(ent2TreeDepthDict.get(instance.ent2.treeDepth));
			
			//
			instance.vectors.add(treePathDict.get(instance.treePath));
			instance.vectors.add(treePathLenDict.get(instance.treePathLength));
			instance.vectors.add(tokDistDict.get(instance.tokenDist));
			instance.vectors.add(menDistDict.get(instance.mentionDist));

			if(instance.midLemmas != null)
				for (String s : instance.midLemmas) {
					if (bowLemmaDict.containsKey(s))
						instance.vectors.add(bowLemmaDict.get(s));
				}
			
		}
		
	}

//	public static void saveDictionary(File file) {
//
//		StringBuilder sb = new StringBuilder();
//
//		for (EventTypes et : eventTypeDict.keySet()) {
//			sb.append(String.valueOf(et)).append(":").append(eventTypeDict.get(et)).append("\t");
//		}
//		sb.append("\n");
//
//		for (EntityTypes et : ent1TypeDict.keySet()) {
//			sb.append(String.valueOf(et)).append(":").append(ent1TypeDict.get(et)).append("\t");
//		}
//		sb.append("\n");
//
//		for (EntityTypes et : ent2TypeDict.keySet()) {
//			sb.append(String.valueOf(et)).append(":").append(ent2TypeDict.get(et)).append("\t");
//		}
//		sb.append("\n");
//		
//		// ent
//		for (int i : ent1TreeDepthDict.keySet()) {
//			sb.append(i).append(":").append(ent1TreeDepthDict.get(i)).append("\t");
//		}
//		sb.append("\n");
//		for (String s : ent1WordDict.keySet()) {
//			sb.append(s).append(":").append(ent1WordDict.get(s)).append("\t");
//		}
//		sb.append("\n");
//		for (String s : ent1LemmaDict.keySet()) {
//			sb.append(s).append(":").append(ent1LemmaDict.get(s)).append("\t");
//		}
//		sb.append("\n");
//		for (String s : ent1POSDict.keySet()) {
//			sb.append(s).append(":").append(ent1POSDict.get(s)).append("\t");
//		}
//		sb.append("\n");
//		for (String s : ent1LemmaDict.keySet()) {
//			sb.append(s).append(":").append(ent1LemmaDict.get(s)).append("\t");
//		}
//		sb.append("\n");
//		for (String s : ent1PreWordDict.keySet()) {
//			sb.append(s).append(":").append(ent1PreWordDict.get(s)).append("\t");
//		}
//		sb.append("\n");
//		for (String s : ent1PreLemmaDict.keySet()) {
//			sb.append(s).append(":").append(ent1PreLemmaDict.get(s)).append("\t");
//		}
//		sb.append("\n");
//		for (String s : ent1PrePOSDict.keySet()) {
//			sb.append(s).append(":").append(ent1PrePOSDict.get(s)).append("\t");
//		}
//		sb.append("\n");
//		for (String s : ent1NextWordDict.keySet()) {
//			sb.append(s).append(":").append(ent1NextWordDict.get(s)).append("\t");
//		}
//		sb.append("\n");
//		for (String s : ent1NextLemmaDict.keySet()) {
//			sb.append(s).append(":").append(ent1NextLemmaDict.get(s)).append("\t");
//		}
//		sb.append("\n");
//		for (String s : ent1NextPOSDict.keySet()) {
//			sb.append(s).append(":").append(ent1NextPOSDict.get(s)).append("\t");
//		}
//		sb.append("\n");
//		
//		// ent2
//		for (int i : ent2TreeDepthDict.keySet()) {
//			sb.append(i).append(":").append(ent2TreeDepthDict.get(i)).append("\t");
//		}
//		sb.append("\n");
//		for (String s : ent2WordDict.keySet()) {
//			sb.append(s).append(":").append(ent2WordDict.get(s)).append("\t");
//		}
//		sb.append("\n");
//		for (String s : ent2LemmaDict.keySet()) {
//			sb.append(s).append(":").append(ent2LemmaDict.get(s)).append("\t");
//		}
//		sb.append("\n");
//		for (String s : ent2POSDict.keySet()) {
//			sb.append(s).append(":").append(ent2POSDict.get(s)).append("\t");
//		}
//		sb.append("\n");
//		for (String s : ent2LemmaDict.keySet()) {
//			sb.append(s).append(":").append(ent2LemmaDict.get(s)).append("\t");
//		}
//		sb.append("\n");
//		for (String s : ent2PreWordDict.keySet()) {
//			sb.append(s).append(":").append(ent2PreWordDict.get(s)).append("\t");
//		}
//		sb.append("\n");
//		for (String s : ent2PreLemmaDict.keySet()) {
//			sb.append(s).append(":").append(ent2PreLemmaDict.get(s)).append("\t");
//		}
//		sb.append("\n");
//		for (String s : ent2PrePOSDict.keySet()) {
//			sb.append(s).append(":").append(ent2PrePOSDict.get(s)).append("\t");
//		}
//		sb.append("\n");
//		for (String s : ent2NextWordDict.keySet()) {
//			sb.append(s).append(":").append(ent2NextWordDict.get(s)).append("\t");
//		}
//		sb.append("\n");
//		for (String s : ent2NextLemmaDict.keySet()) {
//			sb.append(s).append(":").append(ent2NextLemmaDict.get(s)).append("\t");
//		}
//		sb.append("\n");
//		for (String s : ent2NextPOSDict.keySet()) {
//			sb.append(s).append(":").append(ent2NextPOSDict.get(s)).append("\t");
//		}
//		sb.append("\n");
//		
//		//
//		for (String s : treePathDict.keySet()) {
//			sb.append(s).append(":").append(treePathDict.get(s)).append("\t");
//		}
//		sb.append("\n");
//		for (Integer i : treePathLenDict.keySet()) {
//			sb.append(i).append(":").append(treePathLenDict.get(i)).append("\t");
//		}
//		sb.append("\n");
//		for (Integer i : tokDistDict.keySet()) {
//			sb.append(i).append(":").append(tokDistDict.get(i)).append("\t");
//		}
//		sb.append("\n");
//		for (Integer i : menDistDict.keySet()) {
//			sb.append(i).append(":").append(menDistDict.get(i)).append("\t");
//		}
//		sb.append("\n");
//
//		FileUtil.saveFile(sb.toString(), file);
//		logger.info("Dictionary saved in " + file.getPath());
//	}

	/**
	 * Load dictionary from a file. The loaded dictionary will overwrite the old
	 * one.
	 * 
	 * @param file
	 */
//	public static void loadDictionary(File file) {
//
//		try {
//
//			BufferedReader br = new BufferedReader(new FileReader(file));
//
//			String line;
//
//			line = br.readLine();
//			eventTypeDict = new HashMap<EventTypes, Integer>();
//			StringTokenizer st = new StringTokenizer(line, "\t");
//			while (st.hasMoreTokens()) {
//				String[] strs = st.nextToken().split(":");
//				EventTypes et = EventTypes.valueOf(strs[0]);
//				int index = Integer.parseInt(strs[1]);
//				eventTypeDict.put(et, index);
//			}
//
//			line = br.readLine();
//			ent1TypeDict = new HashMap<EntityTypes, Integer>();
//			st = new StringTokenizer(line, "\t");
//			while (st.hasMoreTokens()) {
//				String[] strs = st.nextToken().split(":");
//				EntityTypes et = EntityTypes.valueOf(strs[0]);
//				int index = Integer.parseInt(strs[1]);
//				ent1TypeDict.put(et, index);
//			}
//
//			line = br.readLine();
//			ent2TypeDict = new HashMap<EntityTypes, Integer>();
//			st = new StringTokenizer(line, "\t");
//			while (st.hasMoreTokens()) {
//				String[] strs = st.nextToken().split(":");
//				EntityTypes et = EntityTypes.valueOf(strs[0]);
//				int index = Integer.parseInt(strs[1]);
//				ent2TypeDict.put(et, index);
//			}
//
//			line = br.readLine();
//			treePathDict = new HashMap<String, Integer>();
//			st = new StringTokenizer(line, "\t");
//			while (st.hasMoreTokens()) {
//				String[] strs = st.nextToken().split(":");
//				int index = Integer.parseInt(strs[1]);
//				treePathDict.put(strs[0], index);
//			}
//
//			br.close();
//
//		} catch (Exception e) {
//
//			throw new RuntimeException(e);
//		}
//
//		logger.info("model loaded from " + file.getPath());
//	}

}
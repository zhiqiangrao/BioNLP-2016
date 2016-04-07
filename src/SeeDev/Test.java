package SeeDev;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import utils.FileFilterImpl;
import utils.FileUtil;
import SeeDev.DataStructure.Entity;
import SeeDev.DataStructure.EntityAnnotator;
//import SeeDev.DataStructure.EntityGroups.DNA;
//import SeeDev.DataStructure.EntityGroups.DNA_Product;
//import SeeDev.DataStructure.EntityGroups.Dynamic_Process;
//import SeeDev.DataStructure.EntityGroups.Functional_Molecule;
//import SeeDev.DataStructure.EntityGroups.Internal_Factor;
//import SeeDev.DataStructure.EntityGroups.Molecule;
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

public class Test {

	private final static Logger logger = Logger.getLogger(Train.class.getName());

	public static void main(String[] args) {

		test();

	}

//	private final static File dir = new File("./data/BioNLP-ST-2016_SeeDev-binary_dev");
//	private final static File txtdir = new File("./data/see-dev/txt-normalization/dev");
//	private final static File stantreedir = new File("./data/see-dev/stanfordtree/dev");
//	static File resultdir = new File("./result");
	
	private final static File dir = new File("./data/BioNLP-ST-2016_SeeDev-binary_test");
	private final static File txtdir = new File("./data/see-dev/txt-normalization/test");
	private final static File stantreedir = new File("./data/see-dev/stanfordtree/test");
	static File resultdir = new File("./result2");
	
	// private final static File ssdir = new
	// File("./data/see-dev/tokenization/dev");
	// private final static File tokdir = new
	// File("./data/see-dev/tokenization/dev");
	// private final static File condir = new
	// File("./data/see-dev/stanford-parser/dev");
	// private final static File depdir = new
	// File("./data/see-dev/stanford-parser/dev");
	// private final static File stpdir = new
	// File("./data/see-dev/stanford-parser/dev");

	
	//static LibLinearFacade lib;
	static LibSVMFacade lib;

	public static void test() {

		//lib = new LibLinearFacade();
		//lib.loadModel(new File("./model/train.model"));
		lib = new LibSVMFacade();
		//lib.loadModel(new File("./model/train.libsvm.model"));
		lib.loadModel(new File("./model2/train+devel.libsvm.model"));
		//Instance.loadDictionary(new File("./model/train.dict"));

		Properties props = new Properties();
		// props.put("annotators", "tokenize, ssplit, pos, lemma, parse");
		props.put("annotators", "tokenize, ssplit, pos, lemma");
		props.put("tokenize.language", "English");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		extractFromFiles(pipeline);
		
	}

	private static void extractFromFiles(StanfordCoreNLP pipeline) {
		
		Map<String, String> seqdict = new HashMap<String, String>();
		Map<String, String> fundict = new HashMap<String, String>();
		getSameEntityDict(pipeline, seqdict, fundict);
		
		if (dir.isDirectory()) {
			File[] files = dir.listFiles(new FileFilterImpl(".txt"));
			Arrays.sort(files);
			int filenum = 0;
			for (File file : files) {
				logger.info("Extracting from file: " + file.getName() + " " + (++filenum));
				extractFromSingleFile(pipeline, file, seqdict, fundict);
			}
		}
	}

	private static void extractFromSingleFile(StanfordCoreNLP pipeline, File file, Map<String, String> seqdict, Map<String, String> fundict) {

		File txtfile = new File(txtdir.getPath() + "/" + FileUtil.removeFileNameExtension(file.getName()) + ".txt");

		//String filename = file.getName();
		//filename = filename.replace("binary", "full");

		// File ssfile = new File(ssdir.getPath() + "/" +
		// FileUtil.removeFileNameExtension(filename) + ".txt");
		// File tokfile = new File(tokdir.getPath() + "/" +
		// FileUtil.removeFileNameExtension(filename) + ".txt");
		// File confile = new File(condir.getPath() + "/" +
		// FileUtil.removeFileNameExtension(filename) + ".txt.connlx");
		// File depfile = new File(depdir.getPath() + "/" +
		// FileUtil.removeFileNameExtension(filename) + ".txt.depcc");
		// File stpfile = new File(stpdir.getPath() + "/" +
		// FileUtil.removeFileNameExtension(filename) + ".txt.stp");
		File stantreefile = new File(stantreedir.getPath() + "/" + FileUtil.removeFileNameExtension(file.getName()) + ".ptb");

		String text = FileUtil.readFile(txtfile);
		Annotation document = new Annotation(text);
		pipeline.annotate(document);
		setTreeAnnotation(document, stantreefile);

		File a1file = new File(FileUtil.removeFileNameExtension(file.getPath()) + ".a1");
		EntityAnnotator entityannotator = new EntityAnnotator();
		ArrayList<Entity> entities = new ArrayList<Entity>();
		entityannotator.process(a1file, document, entities);

		ArrayList<Event> events = new ArrayList<Event>();
		for (Entity ent1 : entities) {
			for (Entity ent2 : entities) {
				if (ent2 == ent1)
					continue;
				if (ent2.senId != ent1.senId)
					continue;
				if ((ent2.start >= ent1.start && ent2.start <= ent1.end) || (ent2.end >= ent1.start && ent2.end <= ent1.end)
						|| (ent1.start >= ent2.start && ent1.start <= ent2.end) || (ent1.end >= ent2.start && ent1.end <= ent2.end))
					continue;
				
				Instance instance = new Instance();
				instance.ent1 = ent1;
				instance.ent2 = ent2;
				setInstanceVector(document, instance);
				int label = lib.predict(instance);
				
				Event event = new Event();
				event.ent1Id = ent1.id;
				event.ent2Id = ent2.id;

				 
				
//				if (ent1.entityType == ent2.entityType
//						&& ent2.senTokStart - ent1.senTokEnd == 2
//						&& document.get(CoreAnnotations.SentencesAnnotation.class).get(ent1.senId - 1)
//								.get(CoreAnnotations.TokensAnnotation.class).get(ent1.senTokEnd).lemma().equals("-lrb-")) {
//					if (ent2.name.length() < ent1.name.length()
//							|| (ent1.entityType == EntityTypes.Gene_Family && ent2.entityType == EntityTypes.Gene_Family)
//							|| (ent1.entityType == EntityTypes.Protein_Family && ent2.entityType == EntityTypes.Protein_Family)) {
//						event.eventType = EventTypes.Has_Sequence_Identical_To;
//						event.ent1Role = "Element1";
//						event.ent2Role = "Element2";
//					}
//					else {
//						event.eventType = EventTypes.Is_Functionally_Equivalent_To;
//						event.ent1Role = "Element1";
//						event.ent2Role = "Element2";
//					}
//				}
				
				// supervised classifier
				if (event.eventType == null) {
					
					boolean cover = false;
					// ent1, ent2没被覆盖
					for (Entity e : entities) {
						if (e == ent1)
							continue;
						if (e.start <= ent1.start && e.end >= ent1.end)
							cover = true;
						if (e == ent2)
							continue;
						if (e.start <= ent2.start && e.end >= ent2.end)
							cover = true;
					}
					
					// 没有从句
					String pathstr = getPathOfEntities(document, ent1, ent2);
					
					if (!cover && pathstr != null && !pathstr.contains("SBAR"))
						supervisedClassifier(label, ent1, ent2, event);	
				}
				
				if (event.eventType == null) test_Regulates_Process(document, ent1, ent2, event, entities);
				if (event.eventType == null) test_Regulates_Expression(document, ent1, ent2, event, entities);
				if (event.eventType == null) test_Exists_In_Genotype(document, ent1, ent2, event, entities);
				if (event.eventType == null) test_Is_Localized_In(document, ent1, ent2, event, entities);
				if (event.eventType == null) test_Regulates_Development_Phase(document, ent1, ent2, event, entities);
				if (event.eventType == null) test_Is_Member_Of_Family(document, ent1, ent2, event, entities);
				if (event.eventType == null) test_Has_Sequence_Identical_To(document, ent1, ent2, event, entities, seqdict);
				if (event.eventType == null) test_Is_Functionally_Equivalent_To(document, ent1, ent2, event, entities, fundict);
				if (event.eventType == null) test_Is_Protein_Domain_Of(document, ent1, ent2, event, entities);
				if (event.eventType == null) test_Composes_Primary_Structure(document, ent1, ent2, event, entities);
				if (event.eventType == null) test_Composes_Protein_Complex(document, ent1, ent2, event, entities);
				//test_Occurs_In_Genotype(document, ent1, ent2, testevents, entities);	//效果太差，不用这个
				//test_Exists_At_Stage(document, ent1, ent2, testevents, entities);	//效果差，不用
				//test_Occurs_During(document, ent1, ent2, testevents, entities);	//不同
				//test_Is_Involved_In_Process(document, ent1, ent2, testevents, entities);	//不用
				//test_Transcribes_Or_Translates_To(document, ent1, ent2, testevents, entities);	//不用
				//test_Regulates_Accumulation(document, ent1, ent2, testevents, entities);	//不用
				//test_Regulates_Molecule_Activity(document, ent1, ent2, testevents, entities); //不用，devel中没有
				//test_Regulates_Tissue_Development(document, ent1, ent2, testevents, entities);	//不用
				if (event.eventType == null) test_Binds_To(document, ent1, ent2, event, entities);
				if (event.eventType == null) test_Interacts_With(document, ent1, ent2, event, entities);
				//test_Is_Linked_To(document, ent1, ent2, testevents, entities);	//

				
				// supervised classifier
				/*if (event.eventType == null) {
					if (label == Instance.eventTypeDict.get(EventTypes.Null).intValue())
						continue;
					else {
						
						boolean cover = false;
						// ent1, ent2没被覆盖
						for (Entity e : entities) {
							if (e == ent1)
								continue;
							if (e.start <= ent1.start && e.end >= ent1.end)
								cover = true;
							if (e == ent2)
								continue;
							if (e.start <= ent2.start && e.end >= ent2.end)
								cover = true;
						}
						if (cover) continue;
						
						// 没有从句
						String pathstr = getPathOfEntities(document, ent1, ent2);
						if (pathstr == null || pathstr.contains("SBAR"))
							continue;
						
						
						supervisedClassifier(label, ent1, ent2, event);	
					}
				}*/

				if (event.eventType == null) continue;
				
				events.add(event);

			}
		}

		File a2file = new File(resultdir.getPath() + "/" + FileUtil.removeFileNameExtension(file.getName()) + ".a2");
		writeResult(a2file, events);

	}

	private static void test_Exists_In_Genotype(Annotation document, Entity ent1, Entity ent2, Event event, ArrayList<Entity> entities) {
		if (ent1.entityType == EntityTypes.Gene 
				|| ent1.entityType == EntityTypes.Gene_Family 
				|| ent1.entityType == EntityTypes.RNA
				|| ent1.entityType == EntityTypes.Protein 
				|| ent1.entityType == EntityTypes.Protein_Family
				|| ent1.entityType == EntityTypes.Protein_Domain) {
			if (ent2.entityType == EntityTypes.Genotype) {
				
				if (ent1.nextWord != null && ent1.nextWord.equals("-LRB-")
						&& ent1.senTokStart - ent2.senTokEnd != 1)
					return;
				
				// Genotype后接（）时，不考虑括号中的Genotype
				if (ent2.preWord != null && ent2.preWord.equals("-LRB-"))
					return;

				// 路径上没有从句
				String pathstr = getPathOfEntities(document, ent1, ent2);
				if (pathstr != null && pathstr.contains("SBAR"))
					return;
				
				// ent1没有被更大的实体覆盖
				for (Entity e : entities) {
					if (e == ent1)
						continue;
					if (e.start <= ent1.start && e.end >= ent1.end)
						return;
				}

				// Genotyp没有包含更小的实体,没有交错
				for (Entity e : entities) {
					if (e == ent2)
						continue;
					if (e.start >= ent2.start && e.start <= ent2.end)
						return;
					if (e.end >= ent2.start && e.end <= ent2.end)
						return;
				}

				event.eventType = EventTypes.Exists_In_Genotype;
				event.ent1Role = "Molecule";
				event.ent2Role = "Genotype";
			}
		}
	}

	private static void test_Is_Localized_In(Annotation document, Entity ent1, Entity ent2, Event event, ArrayList<Entity> entities) {
		if (ent1.entityType == EntityTypes.RNA 
				|| ent1.entityType == EntityTypes.Protein 
				|| ent1.entityType == EntityTypes.Protein_Family
				|| ent1.entityType == EntityTypes.Protein_Complex 
				|| ent1.entityType == EntityTypes.Protein_Domain
				|| ent1.entityType == EntityTypes.Hormone) {
			if (ent2.entityType == EntityTypes.Tissue) {
				
				// ent1没有被更大的实体覆盖, train没用, devel有用
				for (Entity e : entities) {
					if (e == ent1)
						continue;
					if (e.start <= ent1.start && e.end >= ent1.end)
						return;
				}
				
				// Tissue没有被更大的实体覆盖
				for (Entity e : entities) {
					if (e == ent2)
						continue;
					if (e.start <= ent2.start && e.end >= ent2.end)
						return;
				}
				
				// 路径上没有从句
				String pathstr = getPathOfEntities(document, ent1, ent2);
				if (pathstr != null && pathstr.contains("SBAR"))
					return;
				
				event.eventType = EventTypes.Is_Localized_In;
				event.ent1Role = "Functional_Molecule";
				event.ent2Role = "Target_Tissue";
			}
		}
	}

	private static void test_Is_Functionally_Equivalent_To(Annotation document, Entity ent1, Entity ent2, Event event, ArrayList<Entity> entities, Map<String, String> fundict) {
			// PRN-NP-NP
			
			if (ent1.entityType == ent2.entityType
	//				|| (ent1.entityType == EntityTypes.Gene && ent2.entityType == EntityTypes.Gene_Family)
	//				|| (ent1.entityType == EntityTypes.Gene_Family && ent2.entityType == EntityTypes.Gene)
	//				|| (ent1.entityType == EntityTypes.Protein && ent2.entityType == EntityTypes.Protein_Family)
	//				|| (ent1.entityType == EntityTypes.Protein_Family && ent2.entityType == EntityTypes.Protein)
					) {
				
				// 从训练集中匹配
				if (ent1.end < ent2.start && ent2.senTokStart - ent1.senTokEnd < 3) {
					if ((fundict.get(ent1.name) != null && fundict.get(ent1.name).equals(ent2.name))
							|| (fundict.get(ent2.name) != null && fundict.get(ent2.name).equals(ent1.name))) {
						event.eventType = EventTypes.Is_Functionally_Equivalent_To;
						event.ent1Role = "Element1";
						event.ent2Role = "Element2";
						return;
					}
				}
				
				if (ent2.senTokStart - ent1.senTokEnd == 2
						&& ent1.entityType != EntityTypes.Gene_Family 
						&& ent1.entityType != EntityTypes.Protein_Family) {
					
					// ent1 (ent2)结构
					if (ent1.nextLemma != null && ent1.nextLemma.equals("-lrb-")
							/*&& ent2.name.length() > ent1.name.length()*/) {
						event.eventType = EventTypes.Is_Functionally_Equivalent_To;
						event.ent1Role = "Element1";
						event.ent2Role = "Element2";
						return;
					}
					
					// ent1/ent2结构
					// ent1 [ent2]结构
					// ent1: ent2结构
	//				if (ent1.nextLemma != null) {
	//					if (ent1.nextLemma.equals(":")
	//							|| ent1.nextLemma.equals("-lsb-")
	//							|| document.get(CoreAnnotations.TextAnnotation.class).substring(ent1.end, ent2.start).equals("/")) {
	//						testevents.add(event);
	//						return;
	//					}
	//				}
					
				}	
			}
		}

	private static void test_Has_Sequence_Identical_To(Annotation document, Entity ent1, Entity ent2, Event event, ArrayList<Entity> entities, Map<String, String> seqdict) {
			// PRN-NP-NP
			
			if (ent1.entityType == ent2.entityType) {
			
				// 从训练集中匹配
				if (ent1.end < ent2.start && ent2.senTokStart - ent1.senTokEnd < 3) {
					if ((seqdict.get(ent1.name) != null && seqdict.get(ent1.name).equals(ent2.name))
							|| (seqdict.get(ent2.name) != null && seqdict.get(ent2.name).equals(ent1.name))) {
						event.eventType = EventTypes.Has_Sequence_Identical_To;
						event.ent1Role = "Element1";
						event.ent2Role = "Element2";
						return;
					}
				}
				
				if (ent2.senTokStart - ent1.senTokEnd == 2) {
					// ent1 (ent2)结构
					if (ent1.nextLemma != null && ent1.nextLemma.equals("-lrb-")
							&& ent2.name.length() < ent1.name.length()) {
						event.eventType = EventTypes.Has_Sequence_Identical_To;
						event.ent1Role = "Element1";
						event.ent2Role = "Element2";
						return;
					}
				}
				
			}
		}

	private static void test_Regulates_Development_Phase(Annotation document, Entity ent1, Entity ent2, Event event, ArrayList<Entity> entities) {
			// ent2 = Development_Phase在Exists_At_Stage，Occurs_During中也有，但很少，可以主要考虑Regulates_Development_Phase
			
			if (ent1.entityType == EntityTypes.Gene
					|| ent1.entityType == EntityTypes.Protein
					|| ent1.entityType == EntityTypes.Genotype
					|| ent1.entityType == EntityTypes.Gene_Family) {
				if (ent2.entityType == EntityTypes.Development_Phase) {
					
					// ent1没被覆盖
					for (Entity e : entities) {
						if (e == ent1)
							continue;
						if (e.start <= ent1.start && e.end >= ent1.end)
							return;
					}
					
					// ent1(ent)结构，不连接ent1
					if (ent1.nextWord != null && ent1.nextWord.equals("-LRB-"))
						return;
					
					// ent2没被覆盖
					for (Entity e : entities) {
						if (e == ent2)
							continue;
						if (e.start <= ent2.start && e.end >= ent2.end)
							return;
					}
					
			    	// 左边指向右边，train有用，devel没用
			    	if (ent2.end < ent1.start)
			    		return;
					
					event.eventType = EventTypes.Regulates_Development_Phase;
					event.ent1Role = "Agent";
					event.ent2Role = "Development";
				}
			}
			
		}

	private static void test_Regulates_Expression(Annotation document, Entity ent1, Entity ent2, Event event, ArrayList<Entity> entities) {
			
			if (ent1.entityType == EntityTypes.Tissue 
					|| ent1.entityType == EntityTypes.Genotype
					|| ent1.entityType == EntityTypes.Protein
					|| ent1.entityType == EntityTypes.Development_Phase) {
				if (ent2.entityType == EntityTypes.Gene 
						/*|| ent2.entityType == EntityTypes.Gene_Family 
						|| ent2.entityType == EntityTypes.Box
						|| ent2.entityType == EntityTypes.Promoter*/) {
					
					// ent1没被覆盖
					for (Entity e : entities) {
						if (e == ent1)
							continue;
						if (e.start <= ent1.start && e.end >= ent1.end)
							return;
					}
					
					// ent1(ent)结构，不连接ent1,没用
	//				if (ent1.nextWord != null && ent1.nextWord.equals("-LRB-"))
	//					return;
					
					// ent2没被覆盖
					for (Entity e : entities) {
						if (e == ent2)
							continue;
						if (e.start <= ent2.start && e.end >= ent2.end)
							return;
					}
					
					// 没有从句
					String pathstr = getPathOfEntities(document, ent1, ent2);
					if (pathstr == null || pathstr.contains("SBAR"))
						return;
					
					// ent1和ent2间含关键词
					String text = null;
					if (ent1.start < ent2.end) {
						text = document.get(CoreAnnotations.TextAnnotation.class).substring(ent1.start, ent2.end);
						if (!text.contains("function")
								&& !text.contains("target")
								&& !text.contains("repress")
								&& !text.contains("bind")
								&& !text.contains("regulat")
								&& !text.contains("exclude")
								&& !text.contains("activate")
								&& !text.contains("require")
								&& !text.contains("expression")
								&& !text.contains("induce")
								&& !text.contains("detect")
								&& !text.contains("express")
								&& !text.contains("define"))
							return;
					}
					else {
						text = document.get(CoreAnnotations.TextAnnotation.class).substring(ent2.start, ent1.end);
						if (!text.contains("regulate")
								&& !text.contains("act")
								&& !text.contains("require")
								&& !text.contains("during")
								&& !text.contains("plicate")
								&& !text.contains("observe")
								&& !text.contains("repress")
								&& !text.contains("induce")
								&& !text.contains("affect")
								&& !text.contains("defect")
								&& !text.contains("transcription")
								&& !text.contains("express")
								&& !text.contains("cease")
								&& !text.contains("associate")
								&& !text.contains("restrict")
								&& !text.contains("modulate")
								&& !text.contains("function"))
							return;
					}
					
					event.eventType = EventTypes.Regulates_Expression;
					event.ent1Role = "Agent";
					event.ent2Role = "DNA";
				}
			}
		}

	private static void test_Regulates_Process(Annotation document, Entity ent1, Entity ent2, Event event, ArrayList<Entity> entities) {
			// ent2=Regulatory_Network或Pathway在这个事件中出现的最多，在其他事件中出现的很少
			
			if (ent1.entityType == EntityTypes.Genotype 
					|| ent1.entityType == EntityTypes.Tissue
					|| ent1.entityType == EntityTypes.Gene
					|| ent1.entityType == EntityTypes.Protein
					|| ent1.entityType == EntityTypes.Development_Phase
					/*|| ent1.entityType == EntityTypes.Regulatory_Network
					|| ent1.entityType == EntityTypes.Protein_Family
					|| ent1.entityType == EntityTypes.Gene_Family*/) {
				if (ent2.entityType == EntityTypes.Regulatory_Network 
						|| ent2.entityType == EntityTypes.Pathway) {
					
					// 暂时不用，召回率更重要
	//				if (ent1.end > ent2.start)
	//					return;
					
					// ent1没被覆盖
					for (Entity e : entities) {
						if (e == ent1)
							continue;
						if (e.start <= ent1.start && e.end >= ent1.end)
							return;
					}
					
					
					// ent2没被覆盖,没用
	//				for (Entity e : entities) {
	//					if (e == ent2)
	//						continue;
	//					if (e.start <= ent2.start && e.end >= ent2.end)
	//						return;
	//				}
					
					// 没有从句
					String pathstr = getPathOfEntities(document, ent1, ent2);
					if (pathstr == null || pathstr.contains("SBAR"))
						return;
					
					//if (!pathstr.contains("V"))
					//	return;
					
					// 被动语态
					Tree root = document.get(CoreAnnotations.SentencesAnnotation.class).get(ent1.senId - 1)
							.get(TreeCoreAnnotations.TreeAnnotation.class);
					Tree t1 = ent1.tree;
					Tree t2 = ent2.tree;
					List<Tree> pathtrees = root.pathNodeToNode(t1, t2);
					if (ent1.id > ent2.id) {
						boolean vbn = false;
						for (Tree tree : pathtrees) {
							if (tree.label().value().equals("VP")) {
								Tree[] sons = tree.children();
								for (Tree son : sons) {
									if (son.label().value().equals("VBN"))
										vbn = true;
								}
							}
						}
						if (!vbn) return;
					}
					
					event.eventType = EventTypes.Regulates_Process;
					event.ent1Role = "Agent";
					event.ent2Role = "Process";
				}
			}
			
		}

	private static void test_Composes_Primary_Structure(Annotation document, Entity ent1, Entity ent2, Event event, ArrayList<Entity> entities) {
			
			if (ent1.entityType == EntityTypes.Box 
						//|| ent1.entityType == EntityTypes.Promoter
						) {
				if (ent2.entityType == EntityTypes.Gene 
							//|| ent2.entityType == EntityTypes.Gene_Family 
							|| ent2.entityType == EntityTypes.Box 
							|| ent2.entityType == EntityTypes.Promoter) {
					
					
					String pathstr = getPathOfEntities(document, ent1, ent2);
					if (pathstr == null || pathstr.contains("SBAR"))
						return;
						
					event.eventType = EventTypes.Composes_Primary_Structure;
					event.ent1Role = "DNA_Part";
					event.ent2Role = "DNA";
				}
			}
		}

	private static void test_Composes_Protein_Complex(Annotation document, Entity ent1, Entity ent2, Event event, ArrayList<Entity> entities) {
					
			if (ent1.entityType == EntityTypes.Protein 
					//|| ent1.entityType == EntityTypes.Protein_Family
					//|| ent1.entityType == EntityTypes.Protein_Complex 
					//|| ent1.entityType == EntityTypes.Protein_Domain
					) {
				if (ent2.entityType == EntityTypes.Protein_Complex) {
					
					if (ent1.end > ent2.start)
						return;
					
					// ent1没被覆盖
					for (Entity e : entities) {
						if (e == ent1)
							continue;
						if (e.start <= ent1.start && e.end >= ent1.end)
							return;
					}
					
					// ent2没被覆盖
					for (Entity e : entities) {
						if (e == ent2)
							continue;
						if (e.start <= ent2.start && e.end >= ent2.end)
							return;
					}
							
					String pathstr = getPathOfEntities(document, ent1, ent2);
					if (pathstr == null || pathstr.contains("SBAR"))
						return;
						
					event.eventType = EventTypes.Composes_Protein_Complex;
					event.ent1Role = "Amino_Acid_Sequence";
					event.ent2Role = "Protein_Complex";
				}
			}
		}

	private static void test_Is_Member_Of_Family(Annotation document, Entity ent1, Entity ent2, Event event, ArrayList<Entity> entities) {
			
			if ((ent1.entityType == EntityTypes.Protein && ent2.entityType == EntityTypes.Protein_Family)
					//|| (ent1.entityType == EntityTypes.Protein_Family && ent2.entityType == EntityTypes.Protein_Family)
					|| (ent1.entityType == EntityTypes.Gene && ent2.entityType == EntityTypes.Gene_Family)
					//||  (ent1.entityType == EntityTypes.Gene_Family && ent2.entityType == EntityTypes.Gene_Family)
					//|| (ent1.entityType == EntityTypes.RNA && ent2.entityType == EntityTypes.RNA)
					) {
				
				int w = 10;
				if (ent1.senTokStart - ent2.senTokEnd > w || ent2.senTokStart - ent1.senTokEnd > w)
					return;
				
				// ent1(ent)结构，不连接ent1
				if (ent1.nextWord != null && ent1.nextWord.equals("-LRB-"))
					return;
					
				// ent1没被覆盖
				for (Entity e : entities) {
					if (e == ent1)
						continue;
					if (e.start <= ent1.start && e.end >= ent1.end)
						return;
				}
				
				// ent2没被覆盖
				for (Entity e : entities) {
					if (e == ent2)
						continue;
					if (e.start <= ent2.start && e.end >= ent2.end)
						return;
				}
				
				String pathstr = getPathOfEntities(document, ent1, ent2);
				if (pathstr == null || pathstr.contains("SBAR"))
					return;
				
				event.eventType = EventTypes.Is_Member_Of_Family;
				event.ent1Role = "Element";
				event.ent2Role = "Family";
			}
			
		}

	private static void test_Is_Protein_Domain_Of(Annotation document, Entity ent1, Entity ent2, Event event, ArrayList<Entity> entities) {
					
			if (ent1.entityType == EntityTypes.Protein_Domain) {
				if (ent2.entityType == EntityTypes.Protein 
						|| ent2.entityType == EntityTypes.Protein_Family
						//|| ent2.entityType == EntityTypes.Protein_Complex 
						//|| ent2.entityType == EntityTypes.Protein_Domain
						) {
					
								
								// ent1(ent)结构，不连接ent1
								if (ent1.nextWord != null && ent1.nextWord.equals("-LRB-"))
									return;
								
								// ent2(ent)结构，不连接ent2，没用
	//							if (ent2.nextWord != null && ent2.nextWord.equals("-LRB-"))
	//								return;
									
								// ent1没被覆盖
								for (Entity e : entities) {
									if (e == ent1)
										continue;
									if (e.start <= ent1.start && e.end >= ent1.end)
										return;
								}
								
								// ent2没被覆盖
								for (Entity e : entities) {
									if (e == ent2)
										continue;
									if (e.start <= ent2.start && e.end >= ent2.end)
										return;
								}
					
								//没用
			//					Tree root = document.get(CoreAnnotations.SentencesAnnotation.class).get(ent1.senId - 1)
			//							.get(TreeCoreAnnotations.TreeAnnotation.class);
			//					Tree t1 = ent1.tree;
			//					Tree t2 = ent2.tree;
			//					List<Tree> pathtrees = root.pathNodeToNode(t1, t2);
			//					String pathstr = null;
			//					for (Tree tree : pathtrees) {
			//						if (pathstr == null)
			//							pathstr = tree.label().value();
			//						else
			//							pathstr = pathstr + "-" + tree.label().value();
			//					}
			//					if (pathstr == null || pathstr.contains("-SBAR-"))
			//						return;
					
					
					event.eventType = EventTypes.Is_Protein_Domain_Of;
					event.ent1Role = "Domain";
					event.ent2Role = "Product";
				}
			}
		}

	private static void setInstanceVector(Annotation document, Instance instance) {
		
		Entity ent1 = instance.ent1;
		Entity ent2 = instance.ent2;
		
		instance.vectors = new ArrayList<Integer>();
		instance.vectors.add(Instance.ent1TypeDict.get(ent1.entityType));
		instance.vectors.add(Instance.ent2TypeDict.get(ent2.entityType));
		
		// ent1
		for (String s : ent1.words) {
			if (Instance.ent1WordDict.containsKey(s))
				instance.vectors.add(Instance.ent1WordDict.get(s));
		}
		for (String s : ent1.lemmas) {
			if (Instance.ent1LemmaDict.containsKey(s))
				instance.vectors.add(Instance.ent1LemmaDict.get(s));
		}
		for (String s : ent1.POSs) {
			if (Instance.ent1POSDict.containsKey(s))
				instance.vectors.add(Instance.ent1POSDict.get(s));
		}
		if (Instance.ent1PreWordDict.containsKey(ent1.preWord))
			instance.vectors.add(Instance.ent1PreWordDict.get(ent1.preWord));
		else
			instance.vectors.add(Instance.ent1PreWordDict.get(null));
			
		if (Instance.ent1PreLemmaDict.containsKey(ent1.preLemma))
			instance.vectors.add(Instance.ent1PreLemmaDict.get(ent1.preLemma));
		else
			instance.vectors.add(Instance.ent1PreLemmaDict.get(null));
		
		if (Instance.ent1PrePOSDict.containsKey(ent1.prePOS))
			instance.vectors.add(Instance.ent1PrePOSDict.get(ent1.prePOS));
		else
			instance.vectors.add(Instance.ent1PrePOSDict.get(null));
		
		if (Instance.ent1NextWordDict.containsKey(ent1.nextWord))
			instance.vectors.add(Instance.ent1NextWordDict.get(ent1.nextWord));
//				else
//					instance.vectors.add(Instance.ent1NextWordDict.get(null));
		
		if (Instance.ent1NextLemmaDict.containsKey(ent1.nextLemma))
			instance.vectors.add(Instance.ent1NextLemmaDict.get(ent1.nextLemma));
//				else
//					instance.vectors.add(Instance.ent1NextLemmaDict.get(null));
		
		if (Instance.ent1NextPOSDict.containsKey(ent1.nextPOS))
			instance.vectors.add(Instance.ent1NextPOSDict.get(ent1.nextPOS));
//				else
//					instance.vectors.add(Instance.ent1NextPOSDict.get(null));
		
		if (Instance.ent1TreeDepthDict.containsKey(ent1.treeDepth))
			instance.vectors.add(Instance.ent1TreeDepthDict.get(ent1.treeDepth));
		
		// ent2
		for (String s : ent2.words) {
			if (Instance.ent2WordDict.containsKey(s))
				instance.vectors.add(Instance.ent2WordDict.get(s));
		}
		for (String s : ent2.lemmas) {
			if (Instance.ent2LemmaDict.containsKey(s))
				instance.vectors.add(Instance.ent2LemmaDict.get(s));
		}
		for (String s : ent2.POSs) {
			if (Instance.ent2POSDict.containsKey(s))
				instance.vectors.add(Instance.ent2POSDict.get(s));
		}
		if (Instance.ent2PreWordDict.containsKey(ent2.preWord))
			instance.vectors.add(Instance.ent2PreWordDict.get(ent2.preWord));
		else
			instance.vectors.add(Instance.ent2PreWordDict.get(null));
			
		if (Instance.ent2PreLemmaDict.containsKey(ent2.preLemma))
			instance.vectors.add(Instance.ent2PreLemmaDict.get(ent2.preLemma));
		else
			instance.vectors.add(Instance.ent2PreLemmaDict.get(null));
		
		if (Instance.ent2PrePOSDict.containsKey(ent2.prePOS))
			instance.vectors.add(Instance.ent2PrePOSDict.get(ent2.prePOS));
		else
			instance.vectors.add(Instance.ent2PrePOSDict.get(null));
		
		if (Instance.ent2NextWordDict.containsKey(ent2.nextWord))
			instance.vectors.add(Instance.ent2NextWordDict.get(ent2.nextWord));
//				else
//					instance.vectors.add(Instance.ent2NextWordDict.get(null));
		
		if (Instance.ent2NextLemmaDict.containsKey(ent2.nextLemma))
			instance.vectors.add(Instance.ent2NextLemmaDict.get(ent2.nextLemma));
//				else
//					instance.vectors.add(Instance.ent2NextLemmaDict.get(null));
		
		if (Instance.ent2NextPOSDict.containsKey(ent2.nextPOS))
			instance.vectors.add(Instance.ent2NextPOSDict.get(ent2.nextPOS));
//				else
//					instance.vectors.add(Instance.ent2NextPOSDict.get(null));
		
		if (Instance.ent2TreeDepthDict.containsKey(ent2.treeDepth))
			instance.vectors.add(Instance.ent2TreeDepthDict.get(ent2.treeDepth));
		
		
		// tree path
		Tree root = document.get(CoreAnnotations.SentencesAnnotation.class).get(ent1.senId - 1).get(TreeCoreAnnotations.TreeAnnotation.class);
		Tree t1 = ent1.tree;
		Tree t2 = ent2.tree;
		List<Tree> pathtrees = root.pathNodeToNode(t1, t2);
		String pathstr = null;
		for (Tree tree : pathtrees) {
			if (pathstr == null)
				pathstr = tree.label().value();
			else
				pathstr = pathstr + "-" + tree.label().value();
		}
//		if (ent1.end < ent2.start) {
//			for (Tree tree : pathtrees) {
//				if (pathstr == null)
//					pathstr = tree.label().value();
//				else
//					pathstr = pathstr + ">" + tree.label().value();
//			}
//		} else {
//			for (Tree tree : pathtrees) {
//				if (pathstr == null)
//					pathstr = tree.label().value();
//				else
//					pathstr = pathstr + "<" + tree.label().value();
//			}
//		}
		
		
		int treePathLength = pathtrees.size();
		int tokenDist = ent1.senTokStart - ent2.senTokEnd;
		int mentionDist = ent1.id - ent2.id;
		
		if (Instance.treePathDict.containsKey(pathstr))
			instance.vectors.add(Instance.treePathDict.get(pathstr));
		else
			instance.vectors.add(Instance.treePathDict.get(null));
		
		if (Instance.treePathLenDict.containsKey(treePathLength))
			instance.vectors.add(Instance.treePathLenDict.get(treePathLength));
		
		if (Instance.tokDistDict.containsKey(tokenDist))
			instance.vectors.add(Instance.tokDistDict.get(tokenDist));
		
		if (Instance.menDistDict.containsKey(mentionDist))
			instance.vectors.add(Instance.menDistDict.get(mentionDist));
		
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
		List<String> midlemmas = new ArrayList<String>();
		for (int i = start; i <= end; i++) {
			midlemmas.add(tokens.get(i - 1).lemma());
		}
		
		for (String s : midlemmas) {
			if (Instance.bowLemmaDict.containsKey(s))
				instance.vectors.add(Instance.bowLemmaDict.get(s));
		}
		
	}

	private static String getPathOfEntities(Annotation document, Entity ent1, Entity ent2) {
		
		Tree root = document.get(CoreAnnotations.SentencesAnnotation.class).get(ent1.senId - 1)
				.get(TreeCoreAnnotations.TreeAnnotation.class);
		Tree t1 = ent1.tree;
		Tree t2 = ent2.tree;
		List<Tree> pathtrees = root.pathNodeToNode(t1, t2);
		String pathstr = null;
		for (Tree tree : pathtrees) {
			if (pathstr == null)
				pathstr = tree.label().value();
			else
				pathstr = pathstr + "-" + tree.label().value();
		}
//		if (ent1.end < ent2.start) {
//			for (Tree tree : pathtrees) {
//				if (pathstr == null)
//					pathstr = tree.label().value();
//				else
//					pathstr = pathstr + ">" + tree.label().value();
//			}
//		} else {
//			for (Tree tree : pathtrees) {
//				if (pathstr == null)
//					pathstr = tree.label().value();
//				else
//					pathstr = pathstr + "<" + tree.label().value();
//			}
//		}
		return pathstr;
	}
	
	private static void writeResult(File a2file, List<Event> events) {

		StringBuilder sb = new StringBuilder();

		int id = 1;
		for (Event event : events) {
			sb.append("E" + id + "\t" + event.eventType + " " + event.ent1Role + ":T" + event.ent1Id + " " + event.ent2Role + ":T" + event.ent2Id);
			sb.append("\n");
			id++;
		}

		FileUtil.saveFile(sb.toString(), a2file);
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
	
	private static void supervisedClassifier(int label, Entity ent1, Entity ent2, Event event) {

		 /*if (label == Instance.eventTypeDict.get(EventTypes.Binds_To).intValue()) {
			if (//ent1.entityType == EntityTypes.RNA
					//|| 
					ent1.entityType == EntityTypes.Protein 
					|| ent1.entityType == EntityTypes.Protein_Family
					//|| ent1.entityType == EntityTypes.Protein_Complex 
					|| ent1.entityType == EntityTypes.Protein_Domain
					//|| ent1.entityType == EntityTypes.Hormone
					)
				if (//ent2.entityType == EntityTypes.Gene
						//|| ent2.entityType == EntityTypes.Gene_Family
						//|| 
						ent2.entityType == EntityTypes.Box
						|| ent2.entityType == EntityTypes.Promoter
						//|| ent2.entityType == EntityTypes.RNA
						|| ent2.entityType == EntityTypes.Protein
						|| ent2.entityType == EntityTypes.Protein_Family
						|| ent2.entityType == EntityTypes.Protein_Complex 
						//|| ent2.entityType == EntityTypes.Protein_Domain
						//|| ent2.entityType == EntityTypes.Hormone
						) {

					event.eventType = EventTypes.Binds_To;
					event.ent1Role = "Functional_Molecule";
					event.ent2Role = "Molecule";
					
				}
		}*/
//		else if (label == Instance.eventTypeDict.get(EventTypes.Composes_Primary_Structure).intValue()) {
//			if (ent1.entityType == EntityTypes.Box 
//					|| ent1.entityType == EntityTypes.Promoter
//					)
//				if (ent2.entityType == EntityTypes.Gene 
//						|| ent2.entityType == EntityTypes.Gene_Family 
//						|| ent2.entityType == EntityTypes.Box 
//						|| ent2.entityType == EntityTypes.Promoter) {
//					event.eventType = EventTypes.Composes_Primary_Structure;
//					event.ent1Role = "DNA_Part";
//					event.ent2Role = "DNA";
//				}
//		}
//		else if (label == Instance.eventTypeDict.get(EventTypes.Composes_Protein_Complex).intValue()) {
//			if (ent1.entityType == EntityTypes.Protein 
//					|| ent1.entityType == EntityTypes.Protein_Family
//					|| ent1.entityType == EntityTypes.Protein_Complex 
//					|| ent1.entityType == EntityTypes.Protein_Domain
//					) {
//				if (ent2.entityType == EntityTypes.Protein_Complex) {
//					event.eventType = EventTypes.Composes_Protein_Complex;
//					event.ent1Role = "Amino_Acid_Sequence";
//					event.ent2Role = "Protein_Complex";
//				}
//			}
//		}
		//else 
			if (label == Instance.eventTypeDict.get(EventTypes.Exists_At_Stage).intValue()) {
			if (ent1.entityType == EntityTypes.RNA 
					|| ent1.entityType == EntityTypes.Protein 
					//|| ent1.entityType == EntityTypes.Protein_Family
					//|| ent1.entityType == EntityTypes.Protein_Complex 
					//|| ent1.entityType == EntityTypes.Protein_Domain
					//|| ent1.entityType == EntityTypes.Hormone
					)
				if (ent2.entityType == EntityTypes.Development_Phase) {

					event.eventType = EventTypes.Exists_At_Stage;
					event.ent1Role = "Functional_Molecule";
					event.ent2Role = "Development";
				}
		}
//		else if (label == Instance.eventTypeDict.get(EventTypes.Exists_In_Genotype).intValue()) {
//			if (ent1.entityType == EntityTypes.Gene 
//					|| ent1.entityType == EntityTypes.Gene_Family 
//					|| ent1.entityType == EntityTypes.Box
//					|| ent1.entityType == EntityTypes.Promoter 
//					|| ent1.entityType == EntityTypes.RNA
//					|| ent1.entityType == EntityTypes.Protein 
//					|| ent1.entityType == EntityTypes.Protein_Family
//					|| ent1.entityType == EntityTypes.Protein_Complex 
//					|| ent1.entityType == EntityTypes.Protein_Domain
//					|| ent1.entityType == EntityTypes.Hormone) {
//				if (ent2.entityType == EntityTypes.Genotype) {
//
//					event.eventType = EventTypes.Exists_In_Genotype;
//					event.ent1Role = "Molecule";
//					event.ent2Role = "Genotype";
//				}
//			}
//			else if (ent1.entityType == EntityTypes.Genotype 
//					|| ent1.entityType == EntityTypes.Tissue
//					|| ent1.entityType == EntityTypes.Development_Phase) {
//				if (ent2.entityType == EntityTypes.Genotype) {
//
//					event.eventType = EventTypes.Exists_In_Genotype;
//					event.ent1Role = "Element";
//					event.ent2Role = "Genotype";
//				}
//			}
//		}
		//else if (label == Instance.eventTypeDict.get(EventTypes.Has_Sequence_Identical_To).intValue()) {
		//	if ((ent1.entityType == ent2.entityType) 
		//			|| (ent1.entityType == EntityTypes.Gene && ent2.entityType == EntityTypes.Gene_Family)
		//			|| (ent1.entityType == EntityTypes.Gene_Family && ent2.entityType == EntityTypes.Gene)
		//			|| (ent1.entityType == EntityTypes.Protein && ent2.entityType == EntityTypes.Protein_Family)
		//			|| (ent1.entityType == EntityTypes.Protein_Family && ent2.entityType == EntityTypes.Protein)) {
		// 
		//		event.eventType = EventTypes.Has_Sequence_Identical_To;
		//		event.ent1Role = "Element1";
		//		event.ent2Role = "Element2";
		//	}
		//}
		/*else if (label == Instance.eventTypeDict.get(EventTypes.Interacts_With).intValue()) {
//			if (ent1.entityType == EntityTypes.Gene 
//					|| ent1.entityType == EntityTypes.Gene_Family) {
//				if (ent2.entityType == EntityTypes.Gene 
//						|| ent2.entityType == EntityTypes.Gene_Family 
//						|| ent2.entityType == EntityTypes.Box
//						|| ent2.entityType == EntityTypes.Promoter 
//						|| ent2.entityType == EntityTypes.Protein
//						|| ent2.entityType == EntityTypes.Protein_Family 
//						|| ent2.entityType == EntityTypes.Protein_Complex
//						|| ent2.entityType == EntityTypes.Protein_Domain) {
//
//					event.eventType = EventTypes.Interacts_With;
//					event.ent1Role = "Agent";
//					event.ent2Role = "Target";
//				}
//			} 
//			else if (ent1.entityType == EntityTypes.Box 
//					|| ent1.entityType == EntityTypes.Promoter) {
//				if (ent2.entityType == EntityTypes.Box 
//						|| ent2.entityType == EntityTypes.Promoter 
//						|| ent2.entityType == EntityTypes.Protein
//						|| ent2.entityType == EntityTypes.Protein_Family 
//						|| ent2.entityType == EntityTypes.Protein_Complex
//						|| ent2.entityType == EntityTypes.Protein_Domain) {
//
//					event.eventType = EventTypes.Interacts_With;
//					event.ent1Role = "Agent";
//					event.ent2Role = "Target";
//				}
//			} 
//			else
				if (ent1.entityType == EntityTypes.Protein 
					//|| ent1.entityType == EntityTypes.Protein_Family
					//|| ent1.entityType == EntityTypes.Protein_Complex 
					//|| ent1.entityType == EntityTypes.Protein_Domain
					|| ent1.entityType == EntityTypes.Environmental_Factor) {
				if (//ent2.entityType == EntityTypes.Gene 
						//|| ent2.entityType == EntityTypes.Gene_Family || 
						ent2.entityType == EntityTypes.Box
						|| ent2.entityType == EntityTypes.Promoter 
						//|| ent2.entityType == EntityTypes.RNA
						|| ent2.entityType == EntityTypes.Protein 
						|| ent2.entityType == EntityTypes.Protein_Family
						|| ent2.entityType == EntityTypes.Protein_Complex 
						|| ent2.entityType == EntityTypes.Protein_Domain) {

					event.eventType = EventTypes.Interacts_With;
					event.ent1Role = "Agent";
					event.ent2Role = "Target";
				}
			}
		}*/
//		else if (label == Instance.eventTypeDict.get(EventTypes.Is_Functionally_Equivalent_To).intValue()) {
//			if ((ent1.entityType == ent2.entityType) 
//					|| (ent1.entityType == EntityTypes.Gene && ent2.entityType == EntityTypes.Gene_Family)
//					|| (ent1.entityType == EntityTypes.Gene_Family && ent2.entityType == EntityTypes.Gene)
//					|| (ent1.entityType == EntityTypes.Protein && ent2.entityType == EntityTypes.Protein_Family)
//					|| (ent1.entityType == EntityTypes.Protein_Family && ent2.entityType == EntityTypes.Protein)) {
//				event.eventType = EventTypes.Is_Functionally_Equivalent_To;
//				event.ent1Role = "Element1";
//				event.ent2Role = "Element2";
//			}
//		}
		else if (label == Instance.eventTypeDict.get(EventTypes.Is_Involved_In_Process).intValue()) {
			if (ent1.entityType == EntityTypes.Gene 
					//|| ent1.entityType == EntityTypes.Gene_Family 
					//|| ent1.entityType == EntityTypes.Box
					//|| ent1.entityType == EntityTypes.Promoter 
					//|| ent1.entityType == EntityTypes.RNA
					|| ent1.entityType == EntityTypes.Protein 
					//|| ent1.entityType == EntityTypes.Protein_Family
					//|| ent1.entityType == EntityTypes.Protein_Complex 
					//|| ent1.entityType == EntityTypes.Protein_Domain
					//|| ent1.entityType == EntityTypes.Hormone
					)
				if (ent2.entityType == EntityTypes.Regulatory_Network 
						|| ent2.entityType == EntityTypes.Pathway) {

					event.eventType = EventTypes.Is_Involved_In_Process;
					event.ent1Role = "Participant";
					event.ent2Role = "Process";
				}

		}
//		else if (label == Instance.eventTypeDict.get(EventTypes.Is_Localized_In).intValue()) {
//			if (ent1.entityType == EntityTypes.RNA 
//					|| ent1.entityType == EntityTypes.Protein 
//					|| ent1.entityType == EntityTypes.Protein_Family
//					|| ent1.entityType == EntityTypes.Protein_Complex 
//					|| ent1.entityType == EntityTypes.Protein_Domain
//					|| ent1.entityType == EntityTypes.Hormone
//					) {
//				if (ent2.entityType == EntityTypes.Tissue) {
//
//					event.eventType = EventTypes.Is_Localized_In;
//					event.ent1Role = "Functional_Molecule";
//					event.ent2Role = "Target_Tissue";
//				}
//			}
//			else if (ent1.entityType == EntityTypes.Regulatory_Network 
//					|| ent1.entityType == EntityTypes.Pathway) {
//				if (ent2.entityType == EntityTypes.Tissue) {
//
//					event.eventType = EventTypes.Is_Localized_In;
//					event.ent1Role = "Process";
//					event.ent2Role = "Target_Tissue";
//				}
//			}
//
//		}
//		else if (label == Instance.eventTypeDict.get(EventTypes.Is_Member_Of_Family).intValue()) {
//			if (ent1.entityType == EntityTypes.Gene 
//					|| ent1.entityType == EntityTypes.Gene_Family
//					) {
//				if (ent2.entityType == EntityTypes.Gene_Family) {
//					event.eventType = EventTypes.Is_Member_Of_Family;
//					event.ent1Role = "Element";
//					event.ent2Role = "Family";
//				}
//			}
//			else if (ent1.entityType == EntityTypes.RNA 
//					&& ent2.entityType == EntityTypes.RNA) {
//				event.eventType = EventTypes.Is_Member_Of_Family;
//				event.ent1Role = "Element";
//				event.ent2Role = "Family";
//			}
//			else if (ent1.entityType == EntityTypes.Protein 
//					|| ent1.entityType == EntityTypes.Protein_Family
//					) {
//				if (ent2.entityType == EntityTypes.Protein_Family) {
//					event.eventType = EventTypes.Is_Member_Of_Family;
//					event.ent1Role = "Element";
//					event.ent2Role = "Family";
//				}
//			}
//		}
//		else if (label == Instance.eventTypeDict.get(EventTypes.Is_Protein_Domain_Of).intValue()) {
//			if (ent1.entityType == EntityTypes.Protein_Domain) {
//				if (ent2.entityType == EntityTypes.Protein 
//						|| ent2.entityType == EntityTypes.Protein_Family
//						|| ent2.entityType == EntityTypes.Protein_Complex 
//						|| ent2.entityType == EntityTypes.Protein_Domain) {
//					event.eventType = EventTypes.Is_Protein_Domain_Of;
//					event.ent1Role = "Domain";
//					event.ent2Role = "Product";
//				}
//			}
//		}
		else if (label == Instance.eventTypeDict.get(EventTypes.Occurs_During).intValue()) {
			if (ent1.entityType == EntityTypes.Regulatory_Network 
					|| ent1.entityType == EntityTypes.Pathway) {
				if (ent2.entityType == EntityTypes.Development_Phase) {
					event.eventType = EventTypes.Occurs_During;
					event.ent1Role = "Process";
					event.ent2Role = "Development";
				}
			}
		}
		else if (label == Instance.eventTypeDict.get(EventTypes.Occurs_In_Genotype).intValue()) {
			if (ent1.entityType == EntityTypes.Regulatory_Network 
					|| ent1.entityType == EntityTypes.Pathway) {
				if (ent2.entityType == EntityTypes.Genotype) {
					event.eventType = EventTypes.Occurs_In_Genotype;
					event.ent1Role = "Process";
					event.ent2Role = "Genotype";
				}
			}
		}
		else if (label == Instance.eventTypeDict.get(EventTypes.Regulates_Accumulation).intValue()) {
			if (ent2.entityType == EntityTypes.RNA 
					|| ent2.entityType == EntityTypes.Protein 
					//|| ent2.entityType == EntityTypes.Protein_Family
					//|| ent2.entityType == EntityTypes.Protein_Complex 
					//|| ent2.entityType == EntityTypes.Hormone
					) {
				event.eventType = EventTypes.Regulates_Accumulation;
				event.ent1Role = "Agent";
				event.ent2Role = "Functional_Molecule";
			}
		}
//		else if (label == Instance.eventTypeDict.get(EventTypes.Regulates_Development_Phase).intValue()) {
//			if (ent2.entityType == EntityTypes.Development_Phase) {
//				event.eventType = EventTypes.Regulates_Development_Phase;
//				event.ent1Role = "Agent";
//				event.ent2Role = "Development";
//			}
//		}
//		else if (label == Instance.eventTypeDict.get(EventTypes.Regulates_Expression).intValue()) {
//			if (ent2.entityType == EntityTypes.Gene 
//					|| ent2.entityType == EntityTypes.Gene_Family 
//					|| ent2.entityType == EntityTypes.Box
//					|| ent2.entityType == EntityTypes.Promoter) {
//				event.eventType = EventTypes.Regulates_Expression;
//				event.ent1Role = "Agent";
//				event.ent2Role = "DNA";
//			}
//		}
		else if (label == Instance.eventTypeDict.get(EventTypes.Regulates_Molecule_Activity).intValue()) {
			if (ent2.entityType == EntityTypes.Protein 
					|| ent2.entityType == EntityTypes.Protein_Family
					//|| ent2.entityType == EntityTypes.Protein_Complex 
					//|| ent2.entityType == EntityTypes.Hormone
					) {
				event.eventType = EventTypes.Regulates_Molecule_Activity;
				event.ent1Role = "Agent";
				event.ent2Role = "Molecule";
			}
		}
//		else if (label == Instance.eventTypeDict.get(EventTypes.Regulates_Process).intValue()) {
//			if (ent2.entityType == EntityTypes.Regulatory_Network 
//					|| ent2.entityType == EntityTypes.Pathway) {
//				event.eventType = EventTypes.Regulates_Process;
//				event.ent1Role = "Agent";
//				event.ent2Role = "Process";
//			}
//		}
		else if (label == Instance.eventTypeDict.get(EventTypes.Regulates_Tissue_Development).intValue()) {
			if (ent2.entityType == EntityTypes.Tissue) {
				event.eventType = EventTypes.Regulates_Tissue_Development;
				event.ent1Role = "Agent";
				event.ent2Role = "Target_Tissue";
			}
		}
		else if (label == Instance.eventTypeDict.get(EventTypes.Transcribes_Or_Translates_To).intValue()) {
			if (ent1.entityType == EntityTypes.Gene 
					|| ent1.entityType == EntityTypes.Gene_Family) {
				if (//ent2.entityType == EntityTypes.RNA
						//|| 
						ent2.entityType == EntityTypes.Protein
						|| ent2.entityType == EntityTypes.Protein_Family 
						//|| ent2.entityType == EntityTypes.Protein_Complex
						|| ent2.entityType == EntityTypes.Protein_Domain) {
					event.eventType = EventTypes.Transcribes_Or_Translates_To;
					event.ent1Role = "Source";
					event.ent2Role = "Product";
				}
			}
			//else if (ent1.entityType == EntityTypes.RNA) {
			//	if (ent2.entityType == EntityTypes.Protein 
			//			|| ent2.entityType == EntityTypes.Protein_Family
			//			|| ent2.entityType == EntityTypes.Protein_Complex 
			//			|| ent2.entityType == EntityTypes.Protein_Domain) {
			//		event.eventType = EventTypes.Transcribes_Or_Translates_To;
			//		event.ent1Role = "Source";
			//		event.ent2Role = "Product";
			//	}
			//}
		}
		else if (label == Instance.eventTypeDict.get(EventTypes.Is_Linked_To).intValue()) {
			if (ent1.entityType == EntityTypes.Gene
					//|| ent1.entityType == EntityTypes.Gene_Family 
					//|| ent1.entityType == EntityTypes.Box
					//|| ent1.entityType == EntityTypes.Promoter 
					//|| ent1.entityType == EntityTypes.RNA
					|| ent1.entityType == EntityTypes.Protein
					|| ent1.entityType == EntityTypes.Protein_Family
					|| ent1.entityType == EntityTypes.Protein_Complex 
					|| ent1.entityType == EntityTypes.Protein_Domain
					|| ent1.entityType == EntityTypes.Hormone 
					//|| ent1.entityType == EntityTypes.Regulatory_Network
					//|| ent1.entityType == EntityTypes.Pathway 
					|| ent1.entityType == EntityTypes.Environmental_Factor) {
				if (ent2.entityType == EntityTypes.Gene
						|| ent2.entityType == EntityTypes.Gene_Family
						|| ent2.entityType == EntityTypes.Protein
						|| ent2.entityType == EntityTypes.Protein_Complex 
						|| ent2.entityType == EntityTypes.Genotype
						|| ent2.entityType == EntityTypes.Tissue 
						|| ent2.entityType == EntityTypes.Development_Phase) {
					event.eventType = EventTypes.Is_Linked_To;
					event.ent1Role = "Agent1";
					event.ent2Role = "Agent2";
				}
			}
		}
	
	}

	private static void getSameEntityDict(StanfordCoreNLP pipeline, Map<String, String> seqdict, Map<String, String> fundict) {
	
//		File dir = new File("./data/BioNLP-ST-2016_SeeDev-binary_train");
//		File txtdir = new File("./data/see-dev/txt-normalization/train");
//		File stantreedir = new File("./data/see-dev/stanfordtree/train");
		
		File dir = new File("./data/binary_train+devel");
		File txtdir = new File("./data/see-dev/txt-normalization/train+devel");
		File stantreedir = new File("./data/see-dev/stanfordtree/train+devel");
		
		
		if (dir.isDirectory()) {
			File[] files = dir.listFiles(new FileFilterImpl(".txt"));
			Arrays.sort(files);
			int filenum = 0;
			for (File file : files) {
				logger.info("Extracting from file: " + file.getName() + " " + (++filenum));
				
				File txtfile = new File(txtdir.getPath() + "/" + FileUtil.removeFileNameExtension(file.getName()) + ".txt");
				File stantreefile = new File(stantreedir.getPath() + "/" + FileUtil.removeFileNameExtension(file.getName()) + ".ptb");
				String text = FileUtil.readFile(txtfile);
				Annotation document = new Annotation(text);
				pipeline.annotate(document);
				Test.setTreeAnnotation(document, stantreefile);
	
				File a1file = new File(FileUtil.removeFileNameExtension(file.getPath()) + ".a1");
				EntityAnnotator entityannotator = new EntityAnnotator();
				ArrayList<Entity> entities = new ArrayList<Entity>();
				entityannotator.process(a1file, document, entities);
				
				List<Event>	events = new ArrayList<Event>();
				File a2file = new File(FileUtil.removeFileNameExtension(file.getPath()) + ".a2");
				EventAnnotator eventannotator = new EventAnnotator();
				eventannotator.process(a2file, events, entities);
	
				for (Event event : events) {
					if (event.eventType == EventTypes.Has_Sequence_Identical_To) {
						if (!seqdict.containsKey(event.entity1.name))
							seqdict.put(event.entity1.name, event.entity2.name);
						else if (!seqdict.containsKey(event.entity2.name))
							seqdict.put(event.entity2.name, event.entity1.name);
					} else if (event.eventType == EventTypes.Is_Functionally_Equivalent_To) {
						if (!fundict.containsKey(event.entity1.name))
							fundict.put(event.entity1.name, event.entity2.name);
						else if (!fundict.containsKey(event.entity2.name))
							fundict.put(event.entity2.name, event.entity1.name);
					}
				}
				
			}
		}
		
	}

	private static void test_Binds_To(Annotation document, Entity ent1, Entity ent2, Event event, ArrayList<Entity> entities) {
			
		if (//ent1.entityType == EntityTypes.RNA || 
			ent1.entityType == EntityTypes.Protein 
			|| ent1.entityType == EntityTypes.Protein_Family
			//|| ent1.entityType == EntityTypes.Protein_Complex 
			|| ent1.entityType == EntityTypes.Protein_Domain
			//|| ent1.entityType == EntityTypes.Hormone
			)
		if (//ent2.entityType == EntityTypes.Gene
				//|| ent2.entityType == EntityTypes.Gene_Family || 
				ent2.entityType == EntityTypes.Box
				|| ent2.entityType == EntityTypes.Promoter
				//|| ent2.entityType == EntityTypes.RNA
				|| ent2.entityType == EntityTypes.Protein
				|| ent2.entityType == EntityTypes.Protein_Family
				|| ent2.entityType == EntityTypes.Protein_Complex 
				//|| ent2.entityType == EntityTypes.Protein_Domain
				//|| ent2.entityType == EntityTypes.Hormone
				) {
			
			// ent1没被覆盖
			for (Entity e : entities) {
				if (e == ent1)
					continue;
				if (e.start <= ent1.start && e.end >= ent1.end)
					return;
			}
			
			// ent2没被覆盖
			for (Entity e : entities) {
				if (e == ent2)
					continue;
				if (e.start <= ent2.start && e.end >= ent2.end)
					return;
			}
			
			// pathstr有修饰
			Tree root = document.get(CoreAnnotations.SentencesAnnotation.class).get(ent1.senId - 1)
					.get(TreeCoreAnnotations.TreeAnnotation.class);
			Tree t1 = ent1.tree;
			Tree t2 = ent2.tree;
			List<Tree> pathtrees = root.pathNodeToNode(t1, t2);
			String pathstr = null;
			for (Tree tree : pathtrees) {
				if (pathstr == null)
					pathstr = tree.label().value();
				else
					pathstr = pathstr + "-" + tree.label().value();
			}
			if (pathstr == null || pathstr.contains("SBAR"))
				return;
			
			// 关键字
			String text = null;
			if (ent1.start < ent2.end)
				text = document.get(CoreAnnotations.TextAnnotation.class).substring(ent1.start, ent2.end);
			else
				text = document.get(CoreAnnotations.TextAnnotation.class).substring(ent2.start, ent1.end);	
			if (text.contains(" binds ")
					|| text.contains(" bind ")) {
				event.eventType = EventTypes.Binds_To;
				event.ent1Role = "Functional_Molecule";
				event.ent2Role = "Molecule";
				return;
			}
			
			if (text.contains("interact")) {
				if (text.contains("physical") || text.contains("direct")) {
					event.eventType = EventTypes.Binds_To;
					event.ent1Role = "Functional_Molecule";
					event.ent2Role = "Molecule";
					return;
				}
			}
		}
	}

	private static void test_Interacts_With(Annotation document, Entity ent1, Entity ent2, Event event, ArrayList<Entity> entities) {
			
			
			
			/*if (ent1.entityType == EntityTypes.Gene 
					|| ent1.entityType == EntityTypes.Gene_Family) {
				if (ent2.entityType == EntityTypes.Gene 
						|| ent2.entityType == EntityTypes.Gene_Family 
						|| ent2.entityType == EntityTypes.Box
						|| ent2.entityType == EntityTypes.Promoter 
						|| ent2.entityType == EntityTypes.Protein
						|| ent2.entityType == EntityTypes.Protein_Family 
						|| ent2.entityType == EntityTypes.Protein_Complex
						|| ent2.entityType == EntityTypes.Protein_Domain) {
	
					testevents.add(event);
				}
			} 
			else if (ent1.entityType == EntityTypes.Box 
					|| ent1.entityType == EntityTypes.Promoter) {
				if (ent2.entityType == EntityTypes.Box 
						|| ent2.entityType == EntityTypes.Promoter 
						|| ent2.entityType == EntityTypes.Protein
						|| ent2.entityType == EntityTypes.Protein_Family 
						|| ent2.entityType == EntityTypes.Protein_Complex
						|| ent2.entityType == EntityTypes.Protein_Domain) {
	
					testevents.add(event);
				}
			} 
			else*/
				if (ent1.entityType == EntityTypes.Protein 
					//|| ent1.entityType == EntityTypes.Protein_Family
					//|| ent1.entityType == EntityTypes.Protein_Complex 
					//|| ent1.entityType == EntityTypes.Protein_Domain
					|| ent1.entityType == EntityTypes.Environmental_Factor) {
				if (/*ent2.entityType == EntityTypes.Gene 
						|| ent2.entityType == EntityTypes.Gene_Family
						|| */ent2.entityType == EntityTypes.Box
						|| ent2.entityType == EntityTypes.Promoter 
						//|| ent2.entityType == EntityTypes.RNA
						|| ent2.entityType == EntityTypes.Protein 
						|| ent2.entityType == EntityTypes.Protein_Family
						|| ent2.entityType == EntityTypes.Protein_Complex 
						|| ent2.entityType == EntityTypes.Protein_Domain) {
					
					for (Entity e : entities) {
						if (e == ent1)
							continue;
						if (e.start <= ent1.start && e.end >= ent1.end)
							return;
					}
					
					// ent2没被覆盖
					for (Entity e : entities) {
						if (e == ent2)
							continue;
						if (e.start <= ent2.start && e.end >= ent2.end)
							return;
					}
					
	
					
					// 从句
					Tree root = document.get(CoreAnnotations.SentencesAnnotation.class).get(ent1.senId - 1)
							.get(TreeCoreAnnotations.TreeAnnotation.class);
					Tree t1 = ent1.tree;
					Tree t2 = ent2.tree;
					List<Tree> pathtrees = root.pathNodeToNode(t1, t2);
					String pathstr = null;
					for (Tree tree : pathtrees) {
						if (pathstr == null)
							pathstr = tree.label().value();
						else
							pathstr = pathstr + "-" + tree.label().value();
					}
					if (pathstr == null || pathstr.contains("SBAR"))
						return;
					
					// 关键字
					String text = null;
					if (ent1.start < ent2.end)
						text = document.get(CoreAnnotations.TextAnnotation.class).substring(ent1.start, ent2.end);
					else
						text = document.get(CoreAnnotations.TextAnnotation.class).substring(ent2.start, ent1.end);	
					if (!text.contains(" interact ")
							&& !text.contains(" interacts ")
							&& !text.contains(" interacted")
							&& !text.contains(" interacting ")
							&& !text.contains(" associate ")
							&& !text.contains(" associated ")
							&& !text.contains(" associates ")
							&& !text.contains(" associating "))
						return;
	
					event.eventType = EventTypes.Interacts_With;
					event.ent1Role = "Agent";
					event.ent2Role = "Target";
					
				}
			}
			
		}
	

}
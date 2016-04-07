package utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import SeeDev.Instance;
import SeeDev.Test;
import SeeDev.DataStructure.Entity;
import SeeDev.DataStructure.EntityAnnotator;
import SeeDev.DataStructure.EntityTypes;
import SeeDev.DataStructure.binary.Event;
import SeeDev.DataStructure.binary.EventAnnotator;
import SeeDev.DataStructure.binary.EventTypes;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;

public class DevelTest {

	private final static Logger logger = Logger.getLogger(DevelTest.class.getName());
	
	private final static File dir = new File("./data/BioNLP-ST-2016_SeeDev-binary_dev");
	private final static File txtdir = new File("./data/see-dev/txt-normalization/dev");
	private final static File stantreedir = new File("./data/see-dev/stanfordtree/dev");
	
//	private final static File dir = new File("./data/BioNLP-ST-2016_SeeDev-binary_train");
//	private final static File txtdir = new File("./data/see-dev/txt-normalization/train");
//	private final static File stantreedir = new File("./data/see-dev/stanfordtree/train");
	
	public static void main(String[] args) {
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma");
		props.put("tokenize.language", "English");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		
		//Map<String, String> seqdict = new HashMap<String, String>();
		//Map<String, String> fundict = new HashMap<String, String>();
		//getSameEntityDict(pipeline, seqdict, fundict);
		
		int num = 0;
		int prednum = 0;
		int tp = 0;

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

				List<Event>	testevents = new ArrayList<Event>();
				for (Entity ent1 : entities) {
					for (Entity ent2 : entities) {
						if (ent2 == ent1)
							continue;
						if (ent2.senId != ent1.senId)
							continue;
						if ((ent2.start >= ent1.start && ent2.start <= ent1.end) || (ent2.end >= ent1.start && ent2.end <= ent1.end)
								|| (ent1.start >= ent2.start && ent1.start <= ent2.end) || (ent1.end >= ent2.start && ent1.end <= ent2.end))
							continue;
						
						//test_Exists_In_Genotype(document, ent1, ent2, testevents, entities);
						//test_Occurs_In_Genotype(document, ent1, ent2, testevents, entities);	//效果太差，不用这个
						//test_Exists_At_Stage(document, ent1, ent2, testevents, entities);	//效果差，不用
						//test_Occurs_During(document, ent1, ent2, testevents, entities);	//不同
						//test_Is_Localized_In(document, ent1, ent2, testevents, entities);
						//test_Is_Functionally_Equivalent_To(document, ent1, ent2, testevents, entities, fundict);
						//test_Has_Sequence_Identical_To(document, ent1, ent2, testevents, entities, seqdict);
						//test_Is_Involved_In_Process(document, ent1, ent2, testevents, entities);	//不用
						//test_Transcribes_Or_Translates_To(document, ent1, ent2, testevents, entities);	//不用
						//test_Regulates_Accumulation(document, ent1, ent2, testevents, entities);	//不用
						//test_Regulates_Development_Phase(document, ent1, ent2, testevents, entities);
						//test_Regulates_Expression(document, ent1, ent2, testevents, entities);
						//test_Regulates_Molecule_Activity(document, ent1, ent2, testevents, entities); //不用，devel中没有
						//test_Regulates_Process(document, ent1, ent2, testevents, entities);
						//test_Regulates_Tissue_Development(document, ent1, ent2, testevents, entities);	//不用
						//test_Composes_Primary_Structure(document, ent1, ent2, testevents, entities);
						//test_Composes_Protein_Complex(document, ent1, ent2, testevents, entities);
						//test_Is_Member_Of_Family(document, ent1, ent2, testevents, entities);
						//test_Is_Protein_Domain_Of(document, ent1, ent2, testevents, entities);
						//test_Binds_To(document, ent1, ent2, testevents, entities);
						test_Interacts_With(document, ent1, ent2, testevents, entities);	//
						//test_Is_Linked_To(document, ent1, ent2, testevents, entities);	//
						
					}
				}
				
				num += countEventNum(events, EventTypes.Interacts_With);
				prednum += testevents.size();
				for (Event eve : testevents) {
					if (isGoldEvent(eve, events))
						tp++;
				}
				
//				File newa2file = new File("./seedev-train-result/" + a2file.getName());
//				writeResult(newa2file, testevents);
				
			}
		}
		
		
		if (num != 0 && prednum != 0) {
			double r = (double) tp / num;
			double p = (double) tp / prednum;
			double f = 2 * r *p / (r + p);
			
			System.out.println("num = " + num);
			System.out.println("prednum = " + prednum);
			System.out.println("tp = " + tp);
			
			System.out.println("F = " + f);
			System.out.println("R = " + r);
			System.out.println("P = " + p);
		} else {
			System.out.println("num = " + num);
			System.out.println("prednum = " + prednum);
		}
		
		
	}
	
	private static void getSameEntityDict(StanfordCoreNLP pipeline, Map<String, String> seqdict, Map<String, String> fundict) {

		File dir = new File("./data/BioNLP-ST-2016_SeeDev-binary_train");
		File txtdir = new File("./data/see-dev/txt-normalization/train");
		File stantreedir = new File("./data/see-dev/stanfordtree/train");
		
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
	

	static void test_Exists_In_Genotype(Annotation document, Entity ent1, Entity ent2, List<Event> testevents, ArrayList<Entity> entities) {
// 只有Exists_In_Genotype, Occurs_In_Genotype含有Genotype，可以直接判断
// devel:		
//				num = 81
//				prednum = 108
//				tp = 51
//				F = 0.5396825396825397
//				R = 0.6296296296296297
//				P = 0.4722222222222222
// train:
//				num = 169
//				prednum = 272
//				tp = 117
//				F = 0.5306122448979592
//				R = 0.6923076923076923
//				P = 0.43014705882352944
		
		if (ent1.entityType == EntityTypes.Gene 
				|| ent1.entityType == EntityTypes.Gene_Family 
//				|| ent1.entityType == EntityTypes.Box//
//				|| ent1.entityType == EntityTypes.Promoter//
				|| ent1.entityType == EntityTypes.RNA
				|| ent1.entityType == EntityTypes.Protein 
				|| ent1.entityType == EntityTypes.Protein_Family
//				|| ent1.entityType == EntityTypes.Protein_Complex//
				|| ent1.entityType == EntityTypes.Protein_Domain
//				|| ent1.entityType == EntityTypes.Hormone//
				) {		
			if (ent2.entityType == EntityTypes.Genotype) {
				
				// 存在ent1（ent2）时，只连接括号中的ent2，不连接括号前的ent1
				// Genotype后面直接跟着ent时，连接上，注意Genotype ent1（ent2），连接ent2
				if (ent1.nextWord != null && ent1.nextWord.equals("-LRB-")
						&& ent1.senTokStart - ent2.senTokEnd != 1)
					return;
				
				// Genotype后接（）时，不考虑括号中的Genotype
				if (ent2.preWord != null && ent2.preWord.equals("-LRB-"))
					return;
				
				// 句子中出现in Genotype，则把所有的Molecule指向Genotype	//没用
				//if (ent2.preWord != null && !ent2.preWord.equals("in"))
				//	return;
				
				// 路径上没有从句
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
				
				if (pathstr == null)
					return;
				
				if (pathstr.contains("-SBAR-"))
					return;
				
				// ent1没有被更大的实体覆盖
				for (Entity e : entities) {
					if (e == ent1)
						continue;
					if (e.start <= ent1.start && e.end >= ent1.end)
						return;
				}
				
				// Genotype没有被更大的实体覆盖	//没用
//				for (Entity e : entities) {
//					if (e == ent2)
//						continue;
//					if (e.start <= ent2.start && e.end >= ent2.end)
//						return;
//				}
				
				// Genotyp没有包含更小的实体,没有交错
				for (Entity e : entities) {
					if (e == ent2)
						continue;
					//if (e.start >= ent2.start && e.end <= ent2.end)
					//	return;
					if (e.start >= ent2.start && e.start <= ent2.end)
						return;
					if (e.end >= ent2.start && e.end <= ent2.end)
						return;
				}
				
				// ent1到root没有PP(in等)	//没用
//				List<Tree> t1pathtrees = root.pathNodeToNode(t1, root);
//				String t1pathstr = null;
//				for (Tree tree : t1pathtrees) {
//					if (t1pathstr == null)
//						t1pathstr = tree.label().value();
//					else
//						t1pathstr = t1pathstr + "-" + tree.label().value();
//				}
//				if (t1pathstr == null || t1pathstr.contains("-PP-"))
//					return;
				
				
				Event event = new Event();
				event.eventType = EventTypes.Exists_In_Genotype;
				event.ent1Id = ent1.id;
				event.ent2Id = ent2.id;
				event.ent1Role = "Molecule";
				event.ent2Role = "Genotype";
				testevents.add(event);
			}
		}
//			else if (ent1.entityType == EntityTypes.Genotype 
//					|| ent1.entityType == EntityTypes.Tissue
//					|| ent1.entityType == EntityTypes.Development_Phase) {
//				if (ent2.entityType == EntityTypes.Genotype) {
//					Event event = new Event();
//					event.eventType = EventTypes.Exists_In_Genotype;
//					event.ent1Id = ent1.id;
//					event.ent2Id = ent2.id;
//					event.ent1Role = "Element";
//					event.ent2Role = "Genotype";
//					testevents.add(event);
//				}
//			}
			
	}
	
	static void test_Occurs_In_Genotype(Annotation document, Entity ent1, Entity ent2, List<Event> testevents, ArrayList<Entity> entities) {
		// 只有Exists_In_Genotype, Occurs_In_Genotype含有Genotype，可以直接判断
// train:	
//		num = 18
//		prednum = 90
//				tp = 11
//				F = 0.2037037037037037
//				R = 0.6111111111111112
//				P = 0.12222222222222222
// devel:
//
		
		if (ent1.entityType == EntityTypes.Regulatory_Network 
				|| ent1.entityType == EntityTypes.Pathway) {		
			if (ent2.entityType == EntityTypes.Genotype) {
				
				// ent1没有被更大的实体覆盖
				for (Entity e : entities) {
					if (e == ent1)
						continue;
					if (e.start <= ent1.start && e.end >= ent1.end)
						return;
				}
				
				// Genotype没有被更大的实体覆盖，没用
//				for (Entity e : entities) {
//					if (e == ent2)
//						continue;
//					if (e.start <= ent2.start && e.end >= ent2.end)
//						return;
//				}
				
				// 路径上没有从句
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
				if (pathstr == null)
					return;
				if (pathstr.contains("-SBAR-"))
					return;
				
				// 路径上有PP
				if (!pathstr.contains("-PP-"))
					return;
				
				// 路径上有NP-PP-NP
				if (!pathstr.contains("NP-PP-NP"))
					return;
				
				// 路径上有S,没用
//				if ( !pathstr.contains("S"))
//					return;
				
				//Genotype前有in，没用
//				if (ent2.preWord == null || !ent2.preWord.equals("in"))
//					return;
				
				Event event = new Event();
				event.eventType = EventTypes.Occurs_In_Genotype;
				event.ent1Id = ent1.id;
				event.ent2Id = ent2.id;
				event.ent1Role = "Process";
				event.ent2Role = "Genotype";
				testevents.add(event);
			}
		}
	
	}

	static void test_Exists_At_Stage(Annotation document, Entity ent1, Entity ent2, List<Event> testevents, ArrayList<Entity> entities) {
		// ent1 = Molecule和ent2 = Developmental phase
		// 在Exists_At_Stage，Regulates_Development_Phase中存在
// train:	
// devel:
		
		if (ent1.entityType == EntityTypes.RNA 
				|| ent1.entityType == EntityTypes.Protein 
//				|| ent1.entityType == EntityTypes.Protein_Family
//				|| ent1.entityType == EntityTypes.Protein_Complex 
//				|| ent1.entityType == EntityTypes.Protein_Domain
//				|| ent1.entityType == EntityTypes.Hormone
				) {
			if (ent2.entityType == EntityTypes.Development_Phase) {
				
				// ent1没有被更大的实体覆盖，没用
//				for (Entity e : entities) {
//					if (e == ent1)
//						continue;
//					if (e.start <= ent1.start && e.end >= ent1.end)
//						return;
//				}
				
				//一般是左边指向右边
				if (ent1.id > ent2.id)
					return;
				
				// Genotype没有被更大的实体覆盖，没用
				for (Entity e : entities) {
					if (e == ent2)
						continue;
					if (e.start <= ent2.start && e.end >= ent2.end)
						return;
				}
				
				// 路径上没有从句
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
				
				// 路径上有PP
				if (pathstr == null || !pathstr.contains("-PP-"))
					return;
				
				// 路径上有VP-PP-NP
				if (!pathstr.contains("VP-PP-NP"))
					return;
				
				
//				String text = document.get(CoreAnnotations.SentencesAnnotation.class).get(ent1.senId - 1)
//													.get(CoreAnnotations.TextAnnotation.class);
//				if (!(text.contains("express") && text.contains("during"))
//							&& !(text.contains("repress") && text.contains("in"))
//							&& !(text.contains("detect") && text.contains("at"))
//							&& !(text.contains("accumulate") && text.contains("during"))
//							&& !(text.contains("accumulate") && text.contains("in"))
//							&& !(text.contains("is") && text.contains("active in"))
//							&& !(text.contains("is") && text.contains("regulator of")))
//							return;
				
				Event event = new Event();
				event.eventType = EventTypes.Exists_At_Stage;
				event.ent1Role = "Functional_Molecule";
				event.ent2Role = "Development";
				event.ent1Id = ent1.id;
				event.ent2Id = ent2.id;
				testevents.add(event);
			}
		}
	}

	static void test_Occurs_During(Annotation document, Entity ent1, Entity ent2, List<Event> testevents, ArrayList<Entity> entities) {
		// ent1 = Process和ent2 = Developmental phase
		// 在Occurs_During，Regulates_Development_Phase中存在
// train:	
// devel:
		
		if (ent1.entityType == EntityTypes.Regulatory_Network 
				|| ent1.entityType == EntityTypes.Pathway) {
			if (ent2.entityType == EntityTypes.Development_Phase) {
				
				// 路径上没有从句
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
				if (pathstr == null || pathstr.contains("-SBAR-"))
					return;
				if (!pathstr.contains("VP-PP-NP"))
					return;
				
				String text = document.get(CoreAnnotations.SentencesAnnotation.class).get(ent1.senId - 1)
						.get(CoreAnnotations.TextAnnotation.class);
				if (!text.contains("during"))
					return;
				
				Event event = new Event();
				event.eventType = EventTypes.Occurs_During;
				event.ent1Role = "Process";
				event.ent2Role = "Development";
				event.ent1Id = ent1.id;
				event.ent2Id = ent2.id;
				testevents.add(event);
			}
		}
	}

	static void test_Is_Localized_In(Annotation document, Entity ent1, Entity ent2, List<Event> testevents, ArrayList<Entity> entities) {
		// ent2 = Tissue， 在Is_Localized_In，Regulates_Tissue_Development中存在，Regulates_Tissue_Development的数量很少。
//train+ent1没有被覆盖
//		num = 107
//				prednum = 119
//				tp = 66
//				F = 0.5840707964601769
//				R = 0.616822429906542
//				P = 0.5546218487394958
// devel+ent1没有被覆盖
//		num = 47
//				prednum = 65
//				tp = 29
//				F = 0.5178571428571428
//				R = 0.6170212765957447
//				P = 0.4461538461538462
		
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
					
				// ent1（ent2），只处理ent2，没用
//				if (ent1.nextWord != null && ent1.nextWord.equals("-LRB-"))
//					return;
				
				// Tissue没有被更大的实体覆盖
				for (Entity e : entities) {
					if (e == ent2)
						continue;
					if (e.start <= ent2.start && e.end >= ent2.end)
						return;
				}
				
				// 路径上没有从句
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
				if (pathstr == null || pathstr.contains("-SBAR-"))
					return;
				
				//路径只含有一个PP，没用
//				if (pathstr.matches("NP-PP.+NP-PP.+"))
//					return;
			
				// 没有分号；
				String text = null;
				if (ent1.start < ent2.end)
					text = document.get(CoreAnnotations.TextAnnotation.class).substring(ent1.start, ent2.end);
				else
					text = document.get(CoreAnnotations.TextAnnotation.class).substring(ent2.start, ent1.end);
				if (text != null && text.contains(";"))
					return;
				
				// Tissue没有被括号包围
//				int lrb = text.lastIndexOf("(");
//				if (lrb != -1) {
//					String text2 = text.substring(lrb);
//					if (!text2.contains(")"))
//						return;
//				}
				
				
				// Tissue前三个单词内没有of，accumulate, 没用
//				if (ent1.senTokEnd < ent2.senTokStart) {
//					List<CoreLabel> tokens = document.get(CoreAnnotations.SentencesAnnotation.class).get(ent1.senId-1)
//							.get(CoreAnnotations.TokensAnnotation.class).subList(ent2.senTokStart-4, ent2.senTokStart-1);
//					for (CoreLabel token : tokens) {
//						if (token.word().equals("of"))
//							return;
//					}
//				}
				
				
				Event event = new Event();
				event.eventType = EventTypes.Is_Localized_In;
				event.ent1Role = "Functional_Molecule";
				event.ent2Role = "Target_Tissue";
				event.ent1Id = ent1.id;
				event.ent2Id = ent2.id;
				testevents.add(event);
			}
		}
//		else if (ent1.entityType == EntityTypes.Regulatory_Network 
//				|| ent1.entityType == EntityTypes.Pathway) {
//			if (ent2.entityType == EntityTypes.Tissue) {
//
//				Event event = new Event();
//				event.eventType = EventTypes.Is_Localized_In;
//				event.ent1Role = "Process";
//				event.ent2Role = "Target_Tissue";
//				event.ent1Id = ent1.id;
//				event.ent2Id = ent2.id;
//				testevents.add(event);
//			}
//		}
		
	}

	static void test_Is_Functionally_Equivalent_To(Annotation document, Entity ent1, Entity ent2, List<Event> testevents, ArrayList<Entity> entities, Map<String, String> fundict) {
		// PRN-NP-NP

//devel:
//		num = 39
//				prednum = 20
//				tp = 12
//				F = 0.4067796610169492
//				R = 0.3076923076923077
//				P = 0.6
		
//		num = 39
//				prednum = 36
//				tp = 20
//				F = 0.5333333333333333
//				R = 0.5128205128205128
//				P = 0.5555555555555556

		
		if (ent1.entityType == ent2.entityType
//				|| (ent1.entityType == EntityTypes.Gene && ent2.entityType == EntityTypes.Gene_Family)
//				|| (ent1.entityType == EntityTypes.Gene_Family && ent2.entityType == EntityTypes.Gene)
//				|| (ent1.entityType == EntityTypes.Protein && ent2.entityType == EntityTypes.Protein_Family)
//				|| (ent1.entityType == EntityTypes.Protein_Family && ent2.entityType == EntityTypes.Protein)
				) {
			
			Event event = new Event();
			event.eventType = EventTypes.Is_Functionally_Equivalent_To;
			event.ent1Role = "Element1";
			event.ent2Role = "Element2";
			event.ent1Id = ent1.id;
			event.ent2Id = ent2.id;
		
			// 从训练集中匹配
			if (ent1.end < ent2.start && ent2.senTokStart - ent1.senTokEnd < 3) {
				if ((fundict.get(ent1.name) != null && fundict.get(ent1.name).equals(ent2.name))
						|| (fundict.get(ent2.name) != null && fundict.get(ent2.name).equals(ent1.name))) {
					testevents.add(event);
					return;
				}
			}
			
			
			if (ent2.senTokStart - ent1.senTokEnd == 2
					&& ent1.entityType != EntityTypes.Gene_Family 
					&& ent1.entityType != EntityTypes.Protein_Family) {
				
				// ent1 (ent2)结构
				if (ent1.nextLemma != null && ent1.nextLemma.equals("-lrb-")
						/*&& ent2.name.length() > ent1.name.length()*/) {
					testevents.add(event);
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
	
	static void test_Has_Sequence_Identical_To(Annotation document, Entity ent1, Entity ent2, List<Event> testevents, ArrayList<Entity> entities, Map<String, String> seqdict) {
		// PRN-NP-NP
		
//devel:
//		num = 20
//				prednum = 28
//				tp = 11
//				F = 0.45833333333333337
//				R = 0.55
//				P = 0.39285714285714285
		
		if (ent1.entityType == ent2.entityType) {
			
			Event event = new Event();
			event.eventType = EventTypes.Has_Sequence_Identical_To;
			event.ent1Role = "Element1";
			event.ent2Role = "Element2";
			event.ent1Id = ent1.id;
			event.ent2Id = ent2.id;
		
			// 从训练集中匹配
			if (ent1.end < ent2.start && ent2.senTokStart - ent1.senTokEnd < 3) {
				if ((seqdict.get(ent1.name) != null && seqdict.get(ent1.name).equals(ent2.name))
						|| (seqdict.get(ent2.name) != null && seqdict.get(ent2.name).equals(ent1.name))) {
					testevents.add(event);
					return;
				}
			}
			
			if (ent2.senTokStart - ent1.senTokEnd == 2) {
				// ent1 (ent2)结构
				if (ent1.nextLemma != null && ent1.nextLemma.equals("-lrb-")
						&& ent2.name.length() < ent1.name.length()) {
					testevents.add(event);
					return;
				}
			}
			
		}
	}
	
	static void test_Is_Involved_In_Process(Annotation document, Entity ent1, Entity ent2, List<Event> testevents, ArrayList<Entity> entities) {
		// Is_Involved_In_Process, Regulates_Process（最多）, Is_Linked_To有相同参数
		
			if (ent1.entityType == EntityTypes.Gene 
					|| ent1.entityType == EntityTypes.Gene_Family 
					|| ent1.entityType == EntityTypes.Box
					|| ent1.entityType == EntityTypes.Promoter 
					|| ent1.entityType == EntityTypes.RNA
					|| ent1.entityType == EntityTypes.Protein 
					|| ent1.entityType == EntityTypes.Protein_Family
					|| ent1.entityType == EntityTypes.Protein_Complex 
					|| ent1.entityType == EntityTypes.Protein_Domain
					|| ent1.entityType == EntityTypes.Hormone)
				if (ent2.entityType == EntityTypes.Regulatory_Network 
						|| ent2.entityType == EntityTypes.Pathway) {
					
					Event event = new Event();
					event.eventType = EventTypes.Is_Involved_In_Process;
					event.ent1Id = ent1.id;
					event.ent2Id = ent2.id;
					event.ent1Role = "Participant";
					event.ent2Role = "Process";
					testevents.add(event);
				}

	}
	
	static void test_Transcribes_Or_Translates_To(Annotation document, Entity ent1, Entity ent2, List<Event> testevents, ArrayList<Entity> entities) {
		
//train:
//		num = 25
//				prednum = 48
//				tp = 15
//				F = 0.4109589041095891
//				R = 0.6
//				P = 0.3125
//devel:
//		num = 13
//				prednum = 43
//				tp = 5
//				F = 0.17857142857142855
//				R = 0.38461538461538464
//				P = 0.11627906976744186
		
		if (ent1.entityType == EntityTypes.Gene 
				//|| ent1.entityType == EntityTypes.Gene_Family
				//|| ent1.entityType == EntityTypes.RNA
				) {
			if (//ent2.entityType == EntityTypes.RNA
					//|| 
					ent2.entityType == EntityTypes.Protein
					|| ent2.entityType == EntityTypes.Protein_Family 
					//|| ent2.entityType == EntityTypes.Protein_Complex
					//|| ent2.entityType == EntityTypes.Protein_Domain
					) {
				
				if (ent1.entityType == EntityTypes.RNA && ent2.entityType == EntityTypes.RNA)
					return;
				
				// gene和protein没有被更大的实体覆盖
				for (Entity e : entities) {
					if (e == ent1)
						continue;
					if (e.start <= ent1.start && e.end >= ent1.end)
						return;
//					if (e == ent2)
//						continue;
//					if (e.start <= ent2.start && e.end >= ent2.end)
//						return;
				}
				
				// 存在ent1（ent2）时，只连接括号中的ent2，不连接括号前的ent1
				if (ent1.nextWord != null && ent1.nextWord.equals("-LRB-"))
					return;
				
				// 路径上没有从句
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
				if (pathstr == null || pathstr.contains("-SBAR-"))
					return;
				
				// 中间含有关键词
				String text = null;
				if (ent1.start < ent2.end)
					text = document.get(CoreAnnotations.TextAnnotation.class).substring(ent1.start, ent2.end);
				else
					text = document.get(CoreAnnotations.TextAnnotation.class).substring(ent2.start, ent1.end);
				if (!text.contains("regulate")
						&& !text.contains("express")
						&& !text.contains("encod")
						&& !text.contains("contain"))
					return;
				
				Event event = new Event();
				event.eventType = EventTypes.Transcribes_Or_Translates_To;
				event.ent1Id = ent1.id;
				event.ent2Id = ent2.id;
				event.ent1Role = "Source";
				event.ent2Role = "Product";
				testevents.add(event);
			}
		}
	}
	
	static void test_Regulates_Accumulation(Annotation document, Entity ent1, Entity ent2, List<Event> testevents, ArrayList<Entity> entities) {
		
//train:
//		num = 36
//				prednum = 92
//				tp = 15
//				F = 0.23437500000000003
//				R = 0.4166666666666667
//				P = 0.16304347826086957
		
		if (ent2.entityType == EntityTypes.RNA 
				|| ent2.entityType == EntityTypes.Protein 
//				|| ent2.entityType == EntityTypes.Protein_Family
//				|| ent2.entityType == EntityTypes.Protein_Complex 
//				|| ent2.entityType == EntityTypes.Hormone
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
			
			// ent2(ent3)结构，不连接ent2
//			if (ent2.nextWord != null && ent2.nextWord.equals("-LRB-"))
//				return;
			
			// 句子含关键词
			String text = document.get(CoreAnnotations.SentencesAnnotation.class).get(ent1.senId-1)
					.get(CoreAnnotations.TextAnnotation.class);
			if (!text.contains("accumulat")
					&& !text.contains("increase"))
				return;
			
			// ent1和ent2间含关键词
//			text = null;
//			if (ent1.start < ent2.end)
//				text = document.get(CoreAnnotations.TextAnnotation.class).substring(ent1.start, ent2.end);
//			else
//				text = document.get(CoreAnnotations.TextAnnotation.class).substring(ent2.start, ent1.end);
//			if (!text.contains("accumulat")
//					&& !text.contains("increase"))
//				return;
			
			// 没有从句
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
			if (pathstr == null || pathstr.contains("-SBAR-"))
				return;
			
			Event event = new Event();
			event.eventType = EventTypes.Regulates_Accumulation;
			event.ent1Id = ent1.id;
			event.ent2Id = ent2.id;
			event.ent1Role = "Agent";
			event.ent2Role = "Functional_Molecule";
			testevents.add(event);
		}
		
	}

	static void test_Regulates_Development_Phase(Annotation document, Entity ent1, Entity ent2, List<Event> testevents, ArrayList<Entity> entities) {
		// ent2 = Development_Phase在Exists_At_Stage，Occurs_During中也有，但很少，可以主要考虑Regulates_Development_Phase
		
//train:
//		num = 106
//				prednum = 371
//				tp = 80
//				F = 0.33542976939203356
//				R = 0.7547169811320755
//				P = 0.215633423180593
// train左边指向右边
//		num = 106
//				prednum = 213
//				tp = 52
//				F = 0.32601880877742945
//				R = 0.49056603773584906
//				P = 0.24413145539906103
//devel:左边指向右边
//		num = 59
//				prednum = 92
//				tp = 25
//				F = 0.3311258278145695
//				R = 0.423728813559322
//				P = 0.2717391304347826
		
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
				
				// 没有从句, 暂时不用，召回率高一点好
				Tree root = document.get(CoreAnnotations.SentencesAnnotation.class).get(ent1.senId - 1)
						.get(TreeCoreAnnotations.TreeAnnotation.class);
				Tree t1 = ent1.tree;
				Tree t2 = ent2.tree;
				List<Tree> pathtrees = root.pathNodeToNode(t1, t2);
//				String pathstr = null;
//				for (Tree tree : pathtrees) {
//					if (pathstr == null)
//						pathstr = tree.label().value();
//					else
//						pathstr = pathstr + "-" + tree.label().value();
//				}
//				if (pathstr == null || pathstr.contains("-SBAR-"))
//					return;
				
				// 左边指向右边，公共头结点是S？
//		    	Tree comtree = null;
//		    	if (pathtrees != null) {
//			    	for (Tree tree : pathtrees) {
//			    		if (tree.dominates(t1) && tree.dominates(t2))
//			    			comtree = tree;
//			    	}
//		    	}
//		    	if (comtree == null)
//		    		return;
//				if (ent1.end < ent2.start) {
//					if (!comtree.label().value().equals("S"))
//						return;
//				}
		    	
		    	// 左边指向右边，train有用，devel没用
		    	if (ent2.end < ent1.start)
		    		return;
		    	
				
				Event event = new Event();
				event.eventType = EventTypes.Regulates_Development_Phase;
				event.ent1Id = ent1.id;
				event.ent2Id = ent2.id;
				event.ent1Role = "Agent";
				event.ent2Role = "Development";
				testevents.add(event);
			}
		}
		
	}
	
	static void test_Regulates_Expression(Annotation document, Entity ent1, Entity ent2, List<Event> testevents, ArrayList<Entity> entities) {
		
//train:
//		num = 201
//				prednum = 321
//				tp = 127
//				F = 0.4865900383141762
//				R = 0.6318407960199005
//				P = 0.3956386292834891
// train+关键词
//		num = 201
//				prednum = 185
//				tp = 109
//				F = 0.5647668393782382
//				R = 0.5422885572139303
//				P = 0.5891891891891892
// devel:
//		num = 111
//				prednum = 152
//				tp = 52
//				F = 0.3954372623574144
//				R = 0.46846846846846846
//				P = 0.34210526315789475
// devel+关键词
//		num = 111
//				prednum = 74
//				tp = 40
//				F = 0.43243243243243246
//				R = 0.36036036036036034
//				P = 0.5405405405405406
	
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
				if (pathstr == null || pathstr.contains("-SBAR-"))
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
				
				Event event = new Event();
				event.eventType = EventTypes.Regulates_Expression;
				event.ent1Id = ent1.id;
				event.ent2Id = ent2.id;
				event.ent1Role = "Agent";
				event.ent2Role = "DNA";
				testevents.add(event);
			}
		}
	}
	
	static void test_Regulates_Molecule_Activity(Annotation document, Entity ent1, Entity ent2, List<Event> testevents, ArrayList<Entity> entities) {
		
		if (ent2.entityType == EntityTypes.Protein 
				|| ent2.entityType == EntityTypes.Protein_Family
				|| ent2.entityType == EntityTypes.Protein_Complex 
				|| ent2.entityType == EntityTypes.Hormone
				) {
			
			
			Event event = new Event();
			event.eventType = EventTypes.Regulates_Molecule_Activity;
			event.ent1Id = ent1.id;
			event.ent2Id = ent2.id;
			event.ent1Role = "Agent";
			event.ent2Role = "Molecule";
			testevents.add(event);
		}
		
	}
	
	static void test_Regulates_Process(Annotation document, Entity ent1, Entity ent2, List<Event> testevents, ArrayList<Entity> entities) {
		// ent2=Regulatory_Network或Pathway在这个事件中出现的最多，在其他事件中出现的很少
	
//train：
//		

//devel:		
//		num = 179
//				prednum = 185
//				tp = 93
//				F = 0.510989010989011
//				R = 0.5195530726256983
//				P = 0.5027027027027027
		
		
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
				if (pathstr == null || pathstr.contains("-SBAR-"))
					return;
				
//				if (!pathstr.contains("V"))
//					return;
				
				// 主动语态和被动语态
//				if (ent1.id < ent2.id) {
//					for (Tree tree : pathtrees) {
//						if (tree.label().value().equals("VP")) {
//							Tree[] sons = tree.children();
//							for (Tree son : sons) {
//								if (son.label().value().equals("VBN"))
//									return;
//							}
//						}
//					}
//				} else {
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
				
				Event event = new Event();
				event.eventType = EventTypes.Regulates_Process;
				event.ent1Id = ent1.id;
				event.ent2Id = ent2.id;
				event.ent1Role = "Agent";
				event.ent2Role = "Process";
				testevents.add(event);
			}
		}
		
	}
	

	static void test_Regulates_Tissue_Development(Annotation document, Entity ent1, Entity ent2, List<Event> testevents, ArrayList<Entity> entities) {
		// ent2=Tissue在这个事件中出现的很少，在test_Is_Localized_In中出现的很多

			if (ent2.entityType == EntityTypes.Tissue) {

				Event event = new Event();
				event.eventType = EventTypes.Regulates_Tissue_Development;
				event.ent1Id = ent1.id;
				event.ent2Id = ent2.id;
				event.ent1Role = "Agent";
				event.ent2Role = "Target_Tissue";
				testevents.add(event);
			}
		
	}
	
	

	static void test_Composes_Primary_Structure(Annotation document, Entity ent1, Entity ent2, List<Event> testevents, ArrayList<Entity> entities) {

// train:
//		num = 20
//				prednum = 69
//				tp = 13
//				F = 0.29213483146067415
//				R = 0.65
//				P = 0.18840579710144928
// devel:
//		num = 15
//				prednum = 75
//				tp = 10
//				F = 0.2222222222222222
//				R = 0.6666666666666666
//				P = 0.13333333333333333

		
		if (ent1.entityType == EntityTypes.Box 
					//|| ent1.entityType == EntityTypes.Promoter
					) {
			if (ent2.entityType == EntityTypes.Gene 
						//|| ent2.entityType == EntityTypes.Gene_Family 
						|| ent2.entityType == EntityTypes.Box 
						|| ent2.entityType == EntityTypes.Promoter) {
				
				
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
				if (pathstr == null || pathstr.contains("-SBAR-"))
					return;
					
				Event event = new Event();
				event.eventType = EventTypes.Composes_Primary_Structure;
				event.ent1Id = ent1.id;
				event.ent2Id = ent2.id;
				event.ent1Role = "DNA_Part";
				event.ent2Role = "DNA";
				testevents.add(event);
			}
		}
	}
	
	
	
	
	static void test_Composes_Protein_Complex(Annotation document, Entity ent1, Entity ent2, List<Event> testevents, ArrayList<Entity> entities) {
				
// train:
//		num = 16
//				prednum = 20
//				tp = 12
//				F = 0.6666666666666665
//				R = 0.75
//				P = 0.6
		
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
				if (pathstr == null || pathstr.contains("-SBAR-"))
					return;
					
				Event event = new Event();
				event.eventType = EventTypes.Composes_Protein_Complex;
				event.ent1Role = "Amino_Acid_Sequence";
				event.ent2Role = "Protein_Complex";
				event.ent1Id = ent1.id;
				event.ent2Id = ent2.id;
				testevents.add(event);
			}
		}
	}
			
	static void test_Is_Member_Of_Family(Annotation document, Entity ent1, Entity ent2, List<Event> testevents, ArrayList<Entity> entities) {
		
// train:
//		num = 89
//				prednum = 148
//				tp = 60
//				F = 0.5063291139240507
//				R = 0.6741573033707865
//				P = 0.40540540540540543
// train+窗10
//		num = 89
//				prednum = 72
//				tp = 37
//				F = 0.45962732919254656
//				R = 0.4157303370786517
//				P = 0.5138888888888888
// devel:
//		num = 55
//				prednum = 59
//				tp = 23
//				F = 0.4035087719298246
//				R = 0.41818181818181815
//				P = 0.3898305084745763	
// devel+窗10
//		num = 55
//				prednum = 41
//				tp = 23
//				F = 0.47916666666666663
//				R = 0.41818181818181815
//				P = 0.5609756097560976
		
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
			if (pathstr == null || pathstr.contains("-SBAR-"))
				return;
			
			
			
			Event event = new Event();
			event.eventType = EventTypes.Is_Member_Of_Family;
			event.ent1Role = "Element";
			event.ent2Role = "Family";
			event.ent1Id = ent1.id;
			event.ent2Id = ent2.id;
			testevents.add(event);
		}
		
	}
	
	static void test_Is_Protein_Domain_Of(Annotation document, Entity ent1, Entity ent2, List<Event> testevents, ArrayList<Entity> entities) {
				
// train:
//		num = 46
//				prednum = 73
//				tp = 20
//				F = 0.3361344537815126
//				R = 0.43478260869565216
//				P = 0.273972602739726
// devel:
//		num = 29
//				prednum = 37
//				tp = 15
//				F = 0.4545454545454546
//				R = 0.5172413793103449
//				P = 0.40540540540540543

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
				
				
				Event event = new Event();
				event.eventType = EventTypes.Is_Protein_Domain_Of;
				event.ent1Role = "Domain";
				event.ent2Role = "Product";
				event.ent1Id = ent1.id;
				event.ent2Id = ent2.id;
				testevents.add(event);
			}
		}
	}
	
	static void test_Binds_To(Annotation document, Entity ent1, Entity ent2, List<Event> testevents, ArrayList<Entity> entities) {

// devel:
//		num = 24
//				prednum = 17
//				tp = 6
//				F = 0.2926829268292683
//				R = 0.25
//				P = 0.35294117647058826

		
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
					Event event = new Event();
					event.eventType = EventTypes.Binds_To;
					event.ent1Role = "Functional_Molecule";
					event.ent2Role = "Molecule";
					event.ent1Id = ent1.id;
					event.ent2Id = ent2.id;
					testevents.add(event);
					return;
				}
				
				if (text.contains("interact")) {
					if (text.contains("physical") || text.contains("direct")) {
						Event event = new Event();
						event.eventType = EventTypes.Binds_To;
						event.ent1Role = "Functional_Molecule";
						event.ent2Role = "Molecule";
						event.ent1Id = ent1.id;
						event.ent2Id = ent2.id;
						testevents.add(event);
						return;
					}
				}
				
//				if (!text.contains("interact")
//						&& !text.contains("heterodimer"))
//					return;
//				if (!pathstr.contains("PP"))
//					return;
				
				
//				Event event = new Event();
//				event.eventType = EventTypes.Binds_To;
//				event.ent1Role = "Functional_Molecule";
//				event.ent2Role = "Molecule";
//				event.ent1Id = ent1.id;
//				event.ent2Id = ent2.id;
//				testevents.add(event);
				
			}
		
	}
	
	static void test_Interacts_With(Annotation document, Entity ent1, Entity ent2, List<Event> testevents, ArrayList<Entity> entities) {

// devel
//		num = 32
//				prednum = 50
//				tp = 12
//				F = 0.2926829268292683
//				R = 0.375
//				P = 0.24

		
		Event event = new Event();
		event.eventType = EventTypes.Interacts_With;
		event.ent1Role = "Agent";
		event.ent2Role = "Target";
		event.ent1Id = ent1.id;
		event.ent2Id = ent2.id;
		
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

				testevents.add(event);
				
			}
		}
		
	}
	
	static void test_Is_Linked_To(Annotation document, Entity ent1, Entity ent2, List<Event> testevents, ArrayList<Entity> entities) {
		
		
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
				|| ent1.entityType == EntityTypes.Environmental_Factor
				) {
			if (ent2.entityType == EntityTypes.Gene
					|| ent2.entityType == EntityTypes.Gene_Family
					|| ent2.entityType == EntityTypes.Protein
					|| ent2.entityType == EntityTypes.Protein_Complex 
					|| ent2.entityType == EntityTypes.Genotype
					|| ent2.entityType == EntityTypes.Tissue 
					|| ent2.entityType == EntityTypes.Development_Phase) {
			
				Event event = new Event();
				event.eventType = EventTypes.Is_Linked_To;
				event.ent1Role = "Agent1";
				event.ent2Role = "Agent2";
				event.ent1Id = ent1.id;
				event.ent2Id = ent2.id;
				testevents.add(event);
			}
		}
		
	}
	
			

	static int countEventNum(List<Event> events, EventTypes type) {
		int num = 0;
		for (Event eve : events) {
			if (eve.eventType == type)
				num++;
		}
		
		return num;
	}

	
	static boolean isGoldEvent(Event event, List<Event> events) {
		for (Event eve : events) {
			if (eve.eventType == event.eventType) {
				if (eve.ent1Id == event.ent1Id && eve.ent2Id ==  event.ent2Id)
					return true;
				if (eve.eventType == EventTypes.Is_Functionally_Equivalent_To
						|| eve.eventType == EventTypes.Has_Sequence_Identical_To
						|| eve.eventType == EventTypes.Is_Linked_To) {
					if ((eve.ent1Id == event.ent1Id && eve.ent2Id ==  event.ent2Id)
							|| (eve.ent1Id == event.ent2Id && eve.ent2Id ==  event.ent1Id))
						return true;
				}
			}
		}
		return false;
	}

	static void writeResult(File a2file, List<Event> events) {
		StringBuilder sb = new StringBuilder();
		int id = 1;
		for (Event event : events) {
			sb.append("R" + id + "\t" + event.eventType + " " + event.ent1Role + ":T" + event.ent1Id + " " + event.ent2Role + ":T" + event.ent2Id);
			sb.append("\n");
			id++;
		}
		FileUtil.saveFile(sb.toString(), a2file);
	}
	

}

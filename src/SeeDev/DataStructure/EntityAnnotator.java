package SeeDev.DataStructure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import SeeDev.Train;
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

	private final static Logger logger = Logger.getLogger(EntityAnnotator.class.getName());

	public void process(File a1file, Annotation document, List<Entity> entities) {
		String filename = a1file.getPath();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(FileUtil.removeFileNameExtension(filename).concat(".a1"))));
			String line;
			while ((line = br.readLine()) != null) {
				String[] strs = line.split("\t");
				String id = strs[0];

				String[] strs2 = strs[1].split(" |;");
				EntityTypes entityType = EntityTypes.valueOf(strs2[0]);
				int start = Integer.parseInt(strs2[1]);
				int end = Integer.parseInt(strs2[strs2.length - 1]);
				String name = strs[2];

				Entity entity = new Entity();
				entities.add(entity);
				entity.id = Integer.parseInt(id.substring(1));
				entity.entityType = entityType;
				entity.start = start;
				entity.end = end;
				entity.name = name;

				for (CoreMap sen : document.get(CoreAnnotations.SentencesAnnotation.class)) {
					if (start >= sen.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class)
							&& end <= sen.get(CoreAnnotations.CharacterOffsetEndAnnotation.class)) {

						entity.senId = sen.get(CoreAnnotations.SentenceIndexAnnotation.class) + 1;

						int tokstart = 0;
						int tokend = 0;
						for (CoreLabel token : sen.get(CoreAnnotations.TokensAnnotation.class)) {
							if (token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class) <= start) {
								tokstart = token.get(CoreAnnotations.IndexAnnotation.class);
							}
							if (token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class) >= end) {
								tokend = token.get(CoreAnnotations.IndexAnnotation.class);
								break;
							}
						}

						entity.senTokStart = tokstart;
						entity.senTokEnd = tokend;
						double[] awordvec = new double[50];
						for (int i = entity.senTokStart; i <= entity.senTokEnd; i++) {
							CoreLabel token = sen.get(CoreAnnotations.TokensAnnotation.class).get(i-1);
							entity.words.add(token.word());
							entity.lemmas.add(token.lemma());
							entity.POSs.add(token.get(CoreAnnotations.PartOfSpeechAnnotation.class));
							
							if (Train.word2vec.containsKey(token.lemma())) {
								double[] d = Train.word2vec.get(token.lemma());
								for (int j = 0; j < d.length; j ++) {
									awordvec[j] += d[j];
								}
							}	
						}
						for (int j = 0; j < awordvec.length; j ++) {
							awordvec[j] = awordvec[j] / (tokend - tokstart + 1);
						}
						entity.wordVector = awordvec;
						
						
						if (entity.senTokStart > 1) {
							entity.preWord = sen.get(CoreAnnotations.TokensAnnotation.class).get(entity.senTokStart - 2).word();
							entity.preLemma = sen.get(CoreAnnotations.TokensAnnotation.class).get(entity.senTokStart - 2).lemma();
							entity.prePOS = sen.get(CoreAnnotations.TokensAnnotation.class).get(entity.senTokStart - 2).get(CoreAnnotations.PartOfSpeechAnnotation.class);
						}
						
						if (entity.senTokEnd < sen.get(CoreAnnotations.TokensAnnotation.class).size()) {
							entity.nextWord = sen.get(CoreAnnotations.TokensAnnotation.class).get(entity.senTokEnd).word();
							entity.nextLemma = sen.get(CoreAnnotations.TokensAnnotation.class).get(entity.senTokEnd).lemma();
							entity.nextPOS = sen.get(CoreAnnotations.TokensAnnotation.class).get(entity.senTokEnd).get(CoreAnnotations.PartOfSpeechAnnotation.class);
						}


						Tree root = sen.get(TreeCoreAnnotations.TreeAnnotation.class);
						Iterator<Tree> rootiter = root.iterator();
						while (rootiter.hasNext()) {
							Tree tree = rootiter.next();
							List<Tree> leaves = tree.getLeaves();
							int a = ((CoreLabel) leaves.get(0).label()).get(CoreAnnotations.IndexAnnotation.class);
							int b = ((CoreLabel) leaves.get(leaves.size() - 1).label()).get(CoreAnnotations.IndexAnnotation.class);

							if (a <= tokstart && b >= tokend) {
								entity.tree = tree;
								entity.treeDepth = root.depth(tree);
								if (a == tokstart && b == tokend)
									break;
							}
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
		// ea.process(new
		// File("./data/BioNLP-ST-2016_SeeDev-binary_train/SeeDev-binary-9657152-1.txt"),
		// entities);
		System.out.println();

	}

}

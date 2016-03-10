package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Read dependency pairs
 * 
 * @author Chen Li
 * 
 */
public class StanfordDependencyReader {

	private final static Logger logger = Logger
			.getLogger(StanfordDependencyReader.class.getName());

	public static final class Pair implements Comparable<Pair> {

		private int head, modifier; // token id
		private String relation;

		public int getHead() {
			return head;
		}

		public void setHead(int head) {
			this.head = head;
		}

		public int getModifier() {
			return modifier;
		}

		public void setModifier(int modifier) {
			this.modifier = modifier;
		}

		public String getRelation() {
			return relation;
		}

		public void setRelation(String relation) {
			this.relation = relation;
		}

		@Override
		public int compareTo(Pair aPair) {
			if (this.getHead() == aPair.getHead()
					&& this.getModifier() == aPair.getModifier()
					&& this.getRelation().equals(aPair.getRelation())) {
				return 0;
			} else {
				return -1;
			}
		}

	}

	/**
	 * 
	 * @param file
	 * @return Pair
	 */
	public static Map<Integer, Set<Pair>> getPairs(File file) {

		try {

			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;

			int sentenceId = 0;

			Map<Integer, Set<Pair>> pairsOfArticle = new TreeMap<Integer, Set<Pair>>();
			Set<Pair> pairsOfSentence = new HashSet<Pair>();
			pairsOfArticle.put(sentenceId, pairsOfSentence);

			while ((line = br.readLine()) != null) {

				// empty line is the end of a sentence
				if (line.trim().equals("")) {
					pairsOfSentence = new HashSet<Pair>();
					pairsOfArticle.put(++sentenceId, pairsOfSentence);
					continue;
				}

				Pair pair = new Pair();

				pair.setRelation(line.substring(0, line.indexOf("(")));
				String pairString = line.substring(line.indexOf("(") + 1,
						line.lastIndexOf(")"));
				String head = pairString.substring(0, pairString.indexOf(", "));
				if (!head.contains("-")) {
					head = pairString.substring(0, pairString.lastIndexOf(", "));
				}
				head = head.substring(head.lastIndexOf("-") + 1);
				if (head.endsWith("'")) {
					head = head.substring(0, head.indexOf("'"));
				}
				String modifier = pairString.substring(pairString
						.lastIndexOf("-") + 1);
				if (modifier.endsWith("'")) {
					modifier = modifier.substring(0, modifier.indexOf("'"));
				}

				int headId = Integer.parseInt(head);
				int modifierId = Integer.parseInt(modifier);

				//
				// TODO ignore the copy nodes for the moment.
				//
				if (headId == modifierId) {
					continue;
				}
				pair.setHead(headId);
				pair.setModifier(modifierId);

				if (!pairsOfSentence.contains(pair)) {
					pairsOfSentence.add(pair);
				}
			}

			br.close();

			return pairsOfArticle;

		} catch (Exception e) {

			logger.log(Level.SEVERE, file.getName().concat(e.getMessage()), e);
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {

		Map<Integer, Set<Pair>> map = StanfordDependencyReader
				.getPairs(new File(args[0]));
		for (int sentenceId : map.keySet()) {
			System.out.println(sentenceId);
			for (Pair pair : map.get(sentenceId)) {
				System.out.println(pair.getHead() + "\t" + pair.getModifier());
			}
		}
	}
}

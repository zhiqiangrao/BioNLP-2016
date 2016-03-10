package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnlxReader {

	private final static Logger logger = Logger.getLogger(ConnlxReader.class
			.getName());
	private static Pattern pattern = Pattern.compile("\\d");

	public static final class Token {
		private String text, pos, relation;
		private int id, dependentId;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public String getPos() {
			return pos;
		}

		public void setPos(String pos) {
			this.pos = pos;
		}

		public int getDependentId() {
			return dependentId;
		}

		public void setDependentId(int dependentId) {
			this.dependentId = dependentId;
		}

		public String getRelation() {
			return relation;
		}

		public void setRelation(String relation) {
			this.relation = relation;
		}

	}

	public static List<Token> getTokens(File file) {

		try {

			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;

			LinkedList<Token> tokens = new LinkedList<Token>();

			while ((line = br.readLine()) != null) {

				// skip empty line
				Matcher matcher = pattern.matcher(line);
				if (!matcher.find()) {
					continue;
				}

				StringTokenizer st = new StringTokenizer(line, "\t");

				Token token = new Token();
				token.setId(Integer.valueOf(st.nextToken()).intValue());
				token.setText(st.nextToken());
				//int i = token.getId();
				//String s = token.getText();
				st.nextToken();
				token.setPos(st.nextToken());
				st.nextToken();
				st.nextToken();
				token.setDependentId(Integer.parseInt(st.nextToken()));
				token.setRelation(st.nextToken());

				tokens.add(token);
			}

			br.close();

			return tokens;

		} catch (Exception e) {

			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
}

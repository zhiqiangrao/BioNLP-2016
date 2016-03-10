package SeeDev;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import SeeDev.DataStructure.Entity;
import SeeDev.DataStructure.EntityAnnotator;
import SeeDev.DataStructure.binary.Event;
import SeeDev.DataStructure.binary.EventAnnotator;
import utils.FileFilterImpl;
import utils.FileUtil;


public class EventExtraction {
	
	private final static Logger logger = Logger.getLogger(EventExtraction.class.getName());
	
	public static void main(String[] args) {
		
		extract();
		
	}
	
	static File dir = new File("./data/BioNLP-ST-2016_SeeDev-binary_train");
    static File txtdir = new File("./data/see-dev/txt-normalization/train");
    static File ssdir = new File("./data/see-dev/tokenization/train");
    static File tokdir = new File("./data/see-dev/tokenization/train");
    static File condir = new File("./data/see-dev/stanford-parser/train");
    static File depdir = new File("./data/see-dev/stanford-parser/train");
    static File stpdir = new File("./data/see-dev/stanford-parser/train");
    
	private static void extract() {
		
		Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit, pos, lemma, parse");
	    props.put("tokenize.language", "English");
	    //props.put("ssplit.newlineIsSentenceBreak", "always");
	    //props.put("ssplit.eolonly", "true");	//Only split sentences on newlines
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
			    
//	    File txtdir = new File("./data/see-dev/txt-normalization/train");
//	    File ssdir = new File("./data/see-dev/tokenization/train");
//	    File tokdir = new File("./data/see-dev/tokenization/train");
//	    File condir = new File("./data/see-dev/stanford-parser/train");
//	    File depdir = new File("./data/see-dev/stanford-parser/train");
//	    File stpdir = new File("./data/see-dev/enju_ptb/train");
	    
		ArrayList<ArrayList<Entity>> allentities = new ArrayList<ArrayList<Entity>>();
	    ArrayList<ArrayList<Event>> allevents = new ArrayList<ArrayList<Event>>();
	    extractFromFiles(pipeline, allentities, allevents);
	
	}

	private static void extractFromFiles(StanfordCoreNLP pipeline, ArrayList<ArrayList<Entity>> allentities, ArrayList<ArrayList<Event>> allevents) {
		if (dir.isDirectory()) {
			File[] files = dir.listFiles(new FileFilterImpl(".txt"));
			Arrays.sort(files);
			int filenum = 0;
			for (File file : files) {
				
				logger.info("Extracting from file: " + file.getName() + " " + (++filenum));
				
				//File ptbfile = new File(ptbdir.getPath() + "/" + FileUtil.removeFileNameExtension(txtfile.getName()) + ".ptb");
				//File tokfile = new File(tokdir.getPath() + "/" + FileUtil.removeFileNameExtension(txtfile.getName()) + ".txt");
				
				ArrayList<Entity> entities = new ArrayList<Entity>();
				allentities.add(entities);
				ArrayList<Event> events = new ArrayList<Event>();
				allevents.add(events);
				extractFromSingleFile(pipeline, file, entities, events);
			}
		}
	}

	private static void extractFromSingleFile(StanfordCoreNLP pipeline, File file, ArrayList<Entity> entities, ArrayList<Event> events) {
		
		File txtfile = new File(txtdir.getPath() + "/" + FileUtil.removeFileNameExtension(file.getName()) + ".txt");
		
		String filename = file.getName();
		filename = filename.replace("binary", "full");
		
		File ssfile = new File(ssdir.getPath() + "/" + FileUtil.removeFileNameExtension(filename) + ".txt");
		File tokfile = new File(tokdir.getPath() + "/" + FileUtil.removeFileNameExtension(filename) + ".txt");
		File confile = new File(condir.getPath() + "/" + FileUtil.removeFileNameExtension(filename) + ".txt.connlx");
		File depfile = new File(depdir.getPath() + "/" + FileUtil.removeFileNameExtension(filename) + ".txt.depcc");
		File stpfile = new File(stpdir.getPath() + "/" + FileUtil.removeFileNameExtension(filename) + ".txt.stp");
		
		String text = FileUtil.readFile(txtfile);
	    Annotation document = new Annotation(text);
	    pipeline.annotate(document);
	    //setTreeAnnotation(document, stpfile);

	    File a1file = new File(FileUtil.removeFileNameExtension(file.getPath()) + ".a1");
	    EntityAnnotator entianno = new EntityAnnotator();
	    entianno.process(a1file, document, entities);
	    
	    File a2file = new File(FileUtil.removeFileNameExtension(file.getPath()) + ".a2");
	    EventAnnotator eventanno = new EventAnnotator();
	    eventanno.process(a2file.getPath(), events);
	    
	    ArrayList<Instance> instances = new ArrayList<Instance>();
	    for (Event event : events) {
	    	Instance instance = new Instance();
	    	instances.add(instance);
	    	instance.event2Instance(document, event, entities);
	    }

	    System.out.println();
	    
	    
	    //tokens = ConnlxReader.getTokens(new File(FileUtil.removeFileNameExtension(filename).concat(".connlx")));
	


	}
	
	public static void setTreeAnnotation(Annotation document, File file) {
		BufferedReader br;
		String s;
		int index = 0;
		try {
			br = new BufferedReader(new FileReader(file));
		    while ((s = br.readLine()) != null) {
		    	document.get(CoreAnnotations.SentencesAnnotation.class)
		    		.get(index++)
		    		.set(TreeCoreAnnotations.TreeAnnotation.class, Tree.valueOf(s));
		    }
		    br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	public void process(File file) {
		
		String filename = file.getName();

		File sentencisedFile = new File(FileUtil.removeFileNameExtension(filename).concat(".ss"));
		if (!sentencisedFile.exists()) {
			sentencisedFile = new File(FileUtil.removeFileNameExtension(filename).concat(".tok"));
		}
		
		File tokenisedFile = new File(FileUtil.removeFileNameExtension(filename).concat(".tok"));
		
		List<ConnlxReader.Token> tokens = new LinkedList<ConnlxReader.Token>();
		if (new File(FileUtil.removeFileNameExtension(filename).concat(".connlx")).exists()) {
			tokens = ConnlxReader.getTokens(new File(FileUtil.removeFileNameExtension(filename).concat(".connlx")));
		} else {
			tokens = ConnlxReader.getTokens(new File(FileUtil.removeFileNameExtension(filename).concat(".conll")));
		}
		
		Map<Integer, Set<Pair>> pairsOfArticle = new HashMap<Integer, Set<Pair>>();
		if (new File(FileUtil.removeFileNameExtension(filename).concat(".sdepcc")).exists()) {
			pairsOfArticle = StanfordDependencyReader.getPairs(new File(FileUtil.removeFileNameExtension(filename).concat(".sdepcc")));
		} else {
			pairsOfArticle = StanfordDependencyReader.getPairs(new File(FileUtil.removeFileNameExtension(filename).concat(".sd")));
		}
		
		Token leftToken = null;
		try {

			// get the stream of the original text
			InputStreamReader originalTextStream = new InputStreamReader(
					new ByteArrayInputStream(jcas.getDocumentText().getBytes()));
			InputStreamReader originalTextStream2 = new InputStreamReader(
					new ByteArrayInputStream(jcas.getDocumentText().getBytes()));
			
			// get the stream of the sentencised text
			InputStreamReader sentencisedFileStream = new InputStreamReader(
					new FileInputStream(sentencisedFile), "UTF8");

			// get the stream of the tokenised text
			InputStreamReader tokenisedFileStream = new InputStreamReader(
					new FileInputStream(tokenisedFile), "UTF8");

			int originalTextCh, originalTextCh2 = 0, sentencisedTextCh, tokenisedTextCh, offset = 0, sentenceBegin = 0, tokenBegin = 0;

			Iterator<ConnlxReader.Token> tokenItor = tokens.iterator();
			

			// token map of each sentence
			TreeMap<Integer, Token> tokensOfSentence = new TreeMap<Integer, Token>();
			int sentenceId = 0;
			originalTextStream2.read();

			while ((originalTextCh = originalTextStream.read()) != -1) {
				originalTextCh2 = originalTextStream2.read();
				Character originalTextChar = (char) originalTextCh;
				if (originalTextChar == ' ' && originalTextCh2 != -1) {
					Character originalTextChar2 = (char) originalTextCh2;
					if (originalTextChar2 == System.getProperty(
								"line.separator").charAt(0)) {
						offset++;
						continue;
					}
				}
				
				//
				// Tokens
				//

				if ((tokenisedTextCh = tokenisedFileStream.read()) != -1) {
					Character tokenisedFileChar = (char) tokenisedTextCh;
					if (tokenisedFileChar == ' '
							|| tokenisedFileChar == System.getProperty(
									"line.separator").charAt(0)) {

						List<Token> tokenss = fetchToken(jcas, tokenBegin, offset,
								tokenItor, leftToken, UimaUtil.getJCasFilePath(jcas));
						Token token = tokenss.get(0);
						leftToken = tokenss.get(1);
						// put tokens in same sentence into a map.
						tokensOfSentence.put(token.getId(), token);
						 
						tokenBegin = offset;
						if (originalTextChar == ' '
								|| originalTextChar == System.getProperty(
										"line.separator").charAt(0)) {
							tokenBegin++;
						}else {
							tokenisedFileStream.read();
						}
					}
				}

				//
				// Sentences
				//
				if ((sentencisedTextCh = sentencisedFileStream.read()) != -1) {
					Character sentencisedFileChar = (char) sentencisedTextCh;
					if (sentencisedFileChar == ' ') {
						if (originalTextChar != ' ') {
							sentencisedFileStream.read();
						}
					}
					if (sentencisedFileChar == System.getProperty(
							"line.separator").charAt(0)) {

						fetchSentence(jcas, sentenceBegin, offset, sentenceId,
								pairsOfArticle.get(sentenceId));
						sentenceId++;

						tokensOfSentence = new TreeMap<Integer, Token>();

						sentenceBegin = offset;
						if (originalTextChar == ' '
								|| originalTextChar == System.getProperty(
										"line.separator").charAt(0)) {
							sentenceBegin++;
						}else {
							sentencisedFileStream.read();
						}
					}
				}

				offset++;
			}

			if (tokenItor.hasNext()) {
				fetchToken(jcas, tokenBegin, offset, tokenItor, leftToken, UimaUtil.getJCasFilePath(jcas));
			}
			fetchSentence(jcas, sentenceBegin, offset, sentenceId,
					pairsOfArticle.get(sentenceId));

			sentencisedFileStream.close();
			tokenisedFileStream.close();

		} catch (Exception e) {

			logger.log(Level.SEVERE, e.getMessage(), e);

			throw new RuntimeException(e);
		}
	}
	*/
	
}
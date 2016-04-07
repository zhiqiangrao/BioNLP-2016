package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class Word2Vec {
	
//	time ./word2vec -train seedev.txt  -output seedev-word2vec-50.txt -size 50 -hs 1 -min-cout 1 -cbow 0

	
	public static Map<String,double[]>  getWord2Vec(File word2vecFile) {
		Map<String,double[]> word2vec = new HashMap<String,double[]>();
		try {
			InputStreamReader word2vecFileStream = new InputStreamReader(
					new FileInputStream(word2vecFile), "UTF8");
			BufferedReader word2vecFileBuffer = new BufferedReader(word2vecFileStream);
			String word2vecTextCh;
			word2vecFileBuffer.readLine();
			while ((word2vecTextCh = word2vecFileBuffer.readLine()) != null) {
				String[] wordSb = word2vecTextCh.split(" ");
				String[] wordSb2 = new String[wordSb.length-1];
				double[] wordSb3 = new double[wordSb.length-1];
				System.arraycopy(wordSb, 1, wordSb2, 0, wordSb.length-1);
				
				for (int i=0; i<wordSb2.length; i++) {
					wordSb3[i] = Double.parseDouble(wordSb2[i]);
				}
				word2vec.put(wordSb[0], wordSb3);
			}
			word2vecFileBuffer.close();
			
		} catch (UnsupportedEncodingException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		return word2vec;
	}
	
	private static void convert() {
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma");
		props.put("tokenize.language", "English");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		File dir = new File("/home/raines/下载/word2vec-master/SeeDev-txt/train+devel+test");
		StringBuilder sb = new StringBuilder();
		if (dir.isDirectory()) {
			File[] files = dir.listFiles(new FileFilterImpl(".txt"));
			for (File file : files) {
				String text = FileUtil.readFile(file);
				Annotation document = new Annotation(text);
				pipeline.annotate(document);
				List<CoreMap> sens = document.get(CoreAnnotations.SentencesAnnotation.class);
				for (CoreMap s : sens) {
					List<CoreLabel> toks = s.get(CoreAnnotations.TokensAnnotation.class);
					for (CoreLabel t : toks) {
						sb.append(t.lemma());
						sb.append(" ");
					}
					sb.append("\n");
				}
			    
			}
		}
		File  out = new File("/home/raines/下载/word2vec-master/SeeDev-txt/train+devel+test/seedev2.txt");
		FileUtil.saveFile(sb.toString(), out);
		
	}

	public static void main(String[] args) {
		
		convert();
		
	}

}

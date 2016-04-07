package SeeDev.DataStructure;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.trees.Tree;

public class Entity {
	
	public int id;
	public EntityTypes entityType;
	public int start;
	public int end;
	public String name;
	
	public int senId;
	public Tree tree;
	public int treeDepth;
	
	public int senTokStart;
	public int senTokEnd;
	
	public List<String> words = new ArrayList<String>();
	public List<String> lemmas = new ArrayList<String>();
	public List<String> POSs = new ArrayList<String>();
	
	public String preWord;
	public String preLemma;
	public String prePOS;
	
	public String nextWord;
	public String nextLemma;
	public String nextPOS;
	
	public double[] wordVector;
}
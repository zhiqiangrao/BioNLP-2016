package SeeDev;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import SeeDev.DataStructure.Entity;
import SeeDev.DataStructure.EntityTypes;
import SeeDev.DataStructure.binary.Event;
import SeeDev.DataStructure.binary.EventTypes;

public class Instance {
	
	EventTypes eventType;
	EntityTypes arg1Type;
	EntityTypes arg2Type;
	String treePath;
	
	int[] eventTypeFeature;
	int[] arg1TypeFeature;
	int[] arg2TypeFeature;
	int[] treePathFeature;

	public void event2Instance(Annotation document, Event event, ArrayList<Entity> entities) {
		
		eventType = event.eventType;
		
		Entity ent1 = null;
		Entity ent2 = null;
		
		for (Entity entity : entities) {
			if (entity.id == event.arg1) {
				arg1Type = entity.entityType;
				ent1 = entity;
			}
			if (entity.id == event.arg2) {
				arg2Type = entity.entityType;
				ent2 = entity;
			}
		}
		
		if (ent1.senid == ent2.senid) {
			Tree root = document.get(CoreAnnotations.SentencesAnnotation.class).get(0)
					.get(TreeCoreAnnotations.TreeAnnotation.class);
			Tree t1 = ent1.tree;
			Tree t2 = ent2.tree;
			List<Tree> pathtrees = root.pathNodeToNode(t1, t2);
			Tree comtree = null;
	    	for (Tree tree : pathtrees) {
	    		if (tree.dominates(t1) && tree.dominates(t2))
	    			comtree = tree;
	    	}
	    	
	    	String pathstr = null;
	    	for (Tree tree : pathtrees) {
	    		if (pathstr == null)
	    			pathstr = tree.label().value();
	    		else if (tree == comtree)
	    			pathstr = pathstr + "-[" + tree.label().value() + "]";
	    		else
	    			pathstr = pathstr + "-" + tree.label().value();
	    	}
	    	treePath = pathstr;
	    	
		}
		
	}
	
	
	
}
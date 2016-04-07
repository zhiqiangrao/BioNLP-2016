package SeeDev;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;

/**
 * 
 * @author Chen Li
 * 
 */
public class LibLinearFacade {

	private final static Logger logger = Logger.getLogger(LibLinearFacade.class
			.getName());

	public Model model;

	public void train(List<Instance> instances) {

		Problem problem = new Problem();
		problem.l = instances.size(); // number of training examples

		int featureNum = 0;
		for (Instance instance : instances) {
			for (int index : instance.vectors) {
				if (index > featureNum) {
					featureNum = index;
				}
			}
		}
		featureNum = featureNum
				+ instances.get(0).ent1.wordVector.length
				+ instances.get(0).ent2.wordVector.length;
		
		System.out.println("number of features:" + featureNum);		
		problem.n = featureNum; // number of features
		problem.x = new Feature[instances.size()][]; // feature nodes
		problem.y = new double[instances.size()]; // target values
		int i = 0;
		for (Instance instance : instances) {
			problem.y[i] = instance.label;

			int previousIndex = 0;
			List<Feature> featureNodes = new ArrayList<Feature>();
			
			double[] fs1 = instance.ent1.wordVector;
			if (null != fs1) {
				for (int m=0; m<fs1.length; m++) {
					featureNodes.add(new FeatureNode(m + 1, fs1[m]));
				}
			}
			double[] fs2 = instance.ent2.wordVector;
			if (null != fs2) {
				for (int m=0; m<fs2.length; m++) {
					featureNodes.add(new FeatureNode(fs1.length + m + 1, fs2[m]));
				}
			}
			
			List<Integer> vecs = instance.vectors;
			Collections.sort(vecs);
			
			for (int index : vecs) {
				if (index > previousIndex) {
					if (fs1 != null && fs2 != null) {
						featureNodes.add(new FeatureNode(fs1.length + fs2.length + index, 1));
					}else {
						featureNodes.add(new FeatureNode(index, 1));
					}
					 //System.out.print("\t" + (index ));
				}
				previousIndex = index;
			}
			problem.x[i] = new FeatureNode[featureNodes.size()];
			problem.x[i] = featureNodes.toArray(problem.x[i]);
			i++;
		}

		SolverType solver = SolverType.MCSVM_CS; // -s 4
		double C = 1.0; // cost of constraints violation
		double eps = 0.1; // stopping criteria

		Parameter parameter = new Parameter(solver, C, eps);

		model = Linear.train(problem, parameter);

	}

	
	public int predict(Instance instance) {

//		if (featureSparseVector == null) {
//			throw new IllegalArgumentException(
//					"Empty sparse vector. This probably due to that the dictionary hasn't converted instances to numeric features yet.");
//		}

		int n;
		int nr_feature = this.model.getNrFeature();
		if (this.model.getBias() >= 0) {
			n = nr_feature + 1;
		} else {
			n = nr_feature;
		}

		List<Feature> featureNodes = new ArrayList<Feature>();
		
		double[] fs1 = instance.ent1.wordVector;
		if (null != fs1) {
			for (int m=0; m<fs1.length; m++) {
				featureNodes.add(new FeatureNode(m + 1, fs1[m]));
			}
		}
		double[] fs2 = instance.ent2.wordVector;
		if (null != fs2) {
			for (int m=0; m<fs2.length; m++) {
				featureNodes.add(new FeatureNode(fs1.length + m + 1, fs2[m]));
			}
		}

		int previousIndex = 0;
		List<Integer> vecs = instance.vectors;
		Collections.sort(vecs);
		for (int index : vecs) {
			if (index > previousIndex) {
				if (fs1 != null && fs2 != null) {
					featureNodes.add(new FeatureNode(fs1.length + fs2.length + index, 1));
				}else {
					featureNodes.add(new FeatureNode(index, 1));
				}
			}
			previousIndex = index;
		}
		if (model.getBias() >= 0) {
			Feature node = new FeatureNode(n, model.getBias());
			featureNodes.add(node);
		}
		Feature[] instance0 = new FeatureNode[featureNodes.size()];
		instance0 = featureNodes.toArray(instance0);
		return (int) Math.round(Linear.predict(this.model, instance0));

	}

	
	public String modelToString() {
		throw new UnsupportedOperationException("Unimplemented method.");
	}

	
	public void saveModel(File modelFile) {

		try {
			model.save(modelFile);
		} catch (IOException e) {
			logger.severe("Error when saving mode: ".concat(modelFile
					.getAbsolutePath()));
			throw new RuntimeException(e);
		}
	}

	
	public void loadModel(File modelFile) {

		try {
			this.model = Model.load(modelFile);
		} catch (IOException e) {
			logger.severe("Error when loading mode: ".concat(modelFile
					.getAbsolutePath()));
			throw new RuntimeException(e);
		}

	}

}

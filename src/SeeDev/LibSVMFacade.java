package SeeDev;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

public class LibSVMFacade {

	private final static Logger logger = Logger.getLogger(LibSVMFacade.class.getName());

	private svm_model model;
	private int predict_probability = 0;

	
	protected void train(List<Instance> instances) {
		
		svm_parameter param = new svm_parameter();
		param.svm_type = svm_parameter.C_SVC;
		param.kernel_type = svm_parameter.LINEAR;
		param.cache_size = 100;
		param.eps = 0.00001;
		param.C = 1;
		
		svm_problem problem = new svm_problem();
		problem.l = instances.size();
		problem.x = new svm_node[instances.size()][];;
		problem.y = new double[instances.size()];
		
		int i = 0;
		for (Instance instance : instances) {
			
			problem.y[i] = instance.label;

			int previousIndex = 0;
			List<svm_node> featureNodes = new ArrayList<svm_node>();
			
			double[] fs1 = instance.ent1.wordVector;
			if (null != fs1) {
				for (int m=0; m<fs1.length; m++) {
					svm_node node = new svm_node();
					node.index = m + 1;
					node.value = fs1[m];
					featureNodes.add(node);
				}
			}
			double[] fs2 = instance.ent2.wordVector;
			if (null != fs2) {
				for (int m=0; m<fs2.length; m++) {
					svm_node node = new svm_node();
					node.index = fs1.length + m + 1;
					node.value = fs2[m];
					featureNodes.add(node);
				}
			}
			
			List<Integer> vecs = instance.vectors;
			Collections.sort(vecs);	
			for (int index : vecs) {
				if (index > previousIndex) {
					if (fs1 != null && fs2 != null) {
						svm_node node = new svm_node();
						node.index = fs1.length + fs2.length + index;
						node.value = 1;
						featureNodes.add(node);
					}else {
						svm_node node = new svm_node();
						node.index = index;
						node.value = 1;
						featureNodes.add(node);
					}
				}
				previousIndex = index;
			}
			problem.x[i] = new svm_node[featureNodes.size()];
			problem.x[i] = featureNodes.toArray(problem.x[i]);
			i++;
		}
				
//		double[] labels = new double[instances.size()];
//		svm_node[][] vectors = new svm_node[instances.size()][];
//		for (int i = 0; i < instances.size(); i++) {
//			labels[i] = instances.get(i).label;
//			vectors[i] = new svm_node[instances.get(i).vector.size()];
//			for (int j = 0; j < instances.get(i).vector.size(); j++) {
//				svm_node node = new svm_node();
//				node.index = j;
//				node.value = instances.get(i).vector.get(j);
//				vectors[i][j] = node;
//			}
//		}

//		svm_problem problem = new svm_problem();
//		problem.l = instances.size();
//		problem.x = vectors;
//		problem.y = labels;

		System.out.println("svm_check_parameter: " + svm.svm_check_parameter(problem, param));
		model = svm.svm_train(problem, param);
	}

	protected int predict(Instance instance) {

		int svm_type = svm.svm_get_svm_type(model);
		int nr_class = svm.svm_get_nr_class(model);
		double[] prob_estimates = null;

		if (predict_probability == 1) {
			if (svm.svm_check_probability_model(model) == 0) {
				logger.severe("Model does not support probabiliy estimates.");
				throw new RuntimeException(
						"Model does not support probabiliy estimates.");
			}
			if (svm_type == svm_parameter.EPSILON_SVR
					|| svm_type == svm_parameter.NU_SVR) {
				logger.info("Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma="
						+ svm.svm_get_svr_probability(model) + "\n");
			} else {
				int[] labels = new int[nr_class];
				svm.svm_get_labels(model, labels);
				prob_estimates = new double[nr_class];
				logger.info("labels");
				for (int j = 0; j < nr_class; j++) {
					logger.info(" " + labels[j]);
				}
			}
		} else {
			if (svm.svm_check_probability_model(model) != 0) {
				logger.info("Model supports probability estimates, but disabled in prediction.\n");
			}
		}

//		svm_node[] vector = new svm_node[instance.vector.size()];
//		for (int i = 0; i < instance.vector.size(); i++) {
//			vector[i] = new svm_node();
//			vector[i].index = i;
//			vector[i].value = instance.vector.get(i);
//		}
		
		List<svm_node> featureNodes = new ArrayList<svm_node>();
		
		double[] fs1 = instance.ent1.wordVector;
		if (null != fs1) {
			for (int m=0; m<fs1.length; m++) {
				svm_node node = new svm_node();
				node.index = m + 1;
				node.value = fs1[m];
				featureNodes.add(node);
			}
		}
		double[] fs2 = instance.ent2.wordVector;
		if (null != fs2) {
			for (int m=0; m<fs2.length; m++) {
				svm_node node = new svm_node();
				node.index = fs1.length + m + 1;
				node.value = fs2[m];
				featureNodes.add(node);
			}
		}

		int previousIndex = 0;
		List<Integer> vecs = instance.vectors;
		Collections.sort(vecs);
		for (int index : vecs) {
			if (index > previousIndex) {
				if (fs1 != null && fs2 != null) {
					svm_node node = new svm_node();
					node.index = fs1.length + fs2.length + index;
					node.value = 1;
					featureNodes.add(node);
				}else {
					svm_node node = new svm_node();
					node.index = index;
					node.value = 1;
					featureNodes.add(node);
				}
			}
			previousIndex = index;
		}
		
		svm_node[] vector = new svm_node[featureNodes.size()];
		vector = featureNodes.toArray(vector);

		if (predict_probability == 1
				&& (svm_type == svm_parameter.C_SVC || svm_type == svm_parameter.NU_SVC)) {
			// It is adhoc to set probability integer during testing.
			return (int) svm.svm_predict_probability(model, vector, prob_estimates);
		} else {
			return (int) svm.svm_predict(model, vector);
		}
	}
	
	protected void saveModel(String name) {
		
		try {
			svm.svm_save_model(name, model);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	protected void loadModel(File modelFile) {
		
		try {
			model = svm.svm_load_model(new BufferedReader(new FileReader(modelFile)));
			
		} catch (FileNotFoundException e) {
			logger.severe(e.getMessage());
			throw new RuntimeException(e);
		} catch (IOException e) {
			logger.severe(e.getMessage());
			throw new RuntimeException(e);
		}
		
	}


}
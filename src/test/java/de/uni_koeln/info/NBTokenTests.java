package de.uni_koeln.info;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import de.daslaboratorium.machinelearning.classifier.Classification;
import de.daslaboratorium.machinelearning.classifier.Classifier;
import de.daslaboratorium.machinelearning.classifier.bayes.BayesClassifier;
import de.uni_koeln.info.extraction.Reader;

/**
 * Cross-validation test for binary classification with Naive Bayes.
 * Language model is based on tokens.
 * 
 * @author matana (Mihail Atanassov)
 *
 */
public class NBTokenTests {

	// Reverse \\p{L}+
	private static final String rgx = "[^\\p{L}]+";
	private static int K = 100;
	
	@Test
	public void classify() throws IOException {

		String rgFile = getClass().getClassLoader().getResource("rg-sent_4500.txt").getFile();
		String deFile = getClass().getClassLoader().getResource("de-sent_4500.txt").getFile();
		
		// GOLDSTANDARD
		List<String> rg_Sentences = Reader.getLines(new File(rgFile));
		List<String> de_Sentences = Reader.getLines(new File(deFile));
		

		// List<List<String>> partition = Lists.partition(rgSentences, 100);
		List<List<String>> rgChunks = new ArrayList<>();
		for (int i = 0; i < rg_Sentences.size(); i += K) {
			rgChunks.add(new ArrayList<String>(rg_Sentences.subList(i, Math.min(rg_Sentences.size(), i + K))));
		}
		
		List<List<String>> deChunks = new ArrayList<>();
		for (int i = 0; i < rg_Sentences.size(); i += K) {
			deChunks.add(new ArrayList<String>(de_Sentences.subList(i, Math.min(de_Sentences.size(), i + K))));
		}

		eval(de_Sentences, new ArrayList<>(), rgChunks, "RM", "DE");
		eval(rg_Sentences, new ArrayList<>(), deChunks, "DE", "RM");
	}

	private void eval(List<String> de_Sentences, List<Double> accuracies, List<List<String>> rgChunks, String labelKGroups, String labelNGrams) {
		for (int i = 0; i < rgChunks.size(); i++) {

			// THE TESTING GROUP
			List<String> tests = rgChunks.get(i);

			// THE OTHER GROUPS
			List<List<String>> tmp = new ArrayList<>(rgChunks);
			tmp.remove(i);

			List<String> model = new ArrayList<>();
			tmp.forEach(m -> model.addAll(m));

			// THE TRAINING
			Classifier<String, String> bayes = train(labelKGroups, model, labelNGrams, de_Sentences);
			
			// THE CLASSIFICATION
			List<Classification<String, String>> results = new ArrayList<>();
			tests.forEach(sent -> {
				List<String> features = new ArrayList<>();
				Arrays.asList(sent.split(" ")).forEach(t -> features.add(t.replaceAll(rgx, "").trim()));
				Classification<String, String> classification = bayes.classify(features);
				results.add(classification);
			});
			
			// THE EVALUATION
			List<Classification<String, String>> truePositives = results.stream().filter(r -> r.getCategory().equals(labelKGroups)).collect(Collectors.toList());
			List<Classification<String, String>> falsePositives = results.stream().filter(r -> r.getCategory().equals(labelNGrams)).collect(Collectors.toList());
			
			Assert.assertEquals(results.size(), truePositives.size() + falsePositives.size());
			
			int tp = truePositives.size();
			int fp = falsePositives.size();
			int fn = K - tp;
			int tn = K - fp;
			
			System.out.println(evaluate(i, tp, fp, fn));
			double accuracy = accuracy(tp, fp, fn, tn);
			//double accuracy = balancedAccuracy(tp, fp, fn, tn);
			accuracies.add(accuracy);
		}
		double arithmeticMean = (double) accuracies.stream().mapToDouble(Double::doubleValue).sum() / rgChunks.size();
		System.out.println("#################################################");
		System.out.println("k(" + K + ")-fold validation based on *unigram* model");
		System.out.println("[accuracy]=" + new DecimalFormat("#.###").format(arithmeticMean));
		System.out.println("#################################################");
		System.out.println();
	}

	private double balancedAccuracy(int tp, int fp, int fn, int tn) {
		return (double) (tp/(tp+fn) + tn/(fp+tn)) / 2;
	}
	
	private double accuracy(int tp, int fp, int fn, int tn) {
		return (double) (tp + tn) / (tp + fp + fn + tn);
	}
	
	private String evaluate(int i, int tp, int fp, int fn) {
		double p = (double) tp / (tp + fp);
		double r = (double) tp / (tp + fn);
		double f = 2 * p * r / (p + r);
		return String.format("[" + i + "]\t[precision]=%.2f [recall]=%.2f [f1]=%.2f", p, r, f);
	}

	public Classifier<String, String> train(String catA, List<String> featuresA, String catB, List<String> featuresB) {

		Classifier<String, String> bayes = new BayesClassifier<String, String>();
		bayes.setMemoryCapacity(10000);
		
		featuresA.forEach(s -> {
			List<String> features = new ArrayList<>();
			Arrays.asList(s.split(" ")).forEach(t -> features.add(t.replaceAll(rgx, "").trim()));
			bayes.learn(catA, features);
		});
		
		featuresB.forEach(s -> {
			List<String> features = new ArrayList<>();
			Arrays.asList(s.split(" ")).forEach(t -> features.add(t.replaceAll(rgx, "").trim()));
			bayes.learn(catB, features);
		});

		return bayes;
	}

}

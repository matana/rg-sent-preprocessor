package de.uni_koeln.info;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import de.daslaboratorium.machinelearning.classifier.Classification;
import de.daslaboratorium.machinelearning.classifier.Classifier;
import de.daslaboratorium.machinelearning.classifier.bayes.BayesClassifier;
import de.uni_koeln.info.extraction.Reader;

/**
 * Cross-validation test for binary classification with Naive Bayes. Language
 * model is based n-grams (n=3).
 * 
 * @author matana (Mihail Atanassov)
 *
 */
public class NBNGramTests {

	// Reverse \\p{L}+
	// private static final String rgx = "[^\\p{L}\\s]+";
	private static final String rgx = "[^\\p{L}\\s]+";
	private static int K = 100;
	private static int N = 2;

	@Test
	public void classify() throws IOException {

		String rgFile = getClass().getClassLoader().getResource("rg-sent_4500.txt").getFile();
		String deFile = getClass().getClassLoader().getResource("de-sent_4500.txt").getFile();
		// GOLDSTANDARD
		List<String> rg_Sentences = Reader.getLines(new File(rgFile));
		List<String> de_Sentences = Reader.getLines(new File(deFile));

		// TRIGRAM MODEL
		List<List<String>> de_ngrams = getNGramSequences(de_Sentences);
		List<List<String>> rg_ngrams = getNGramSequences(rg_Sentences);
		

		List<List<List<String>>> rgChunks = new ArrayList<>();
		for (int i = 0; i < rg_Sentences.size(); i += K) {
			rgChunks.add(rg_ngrams.subList(i, Math.min(rg_ngrams.size(), i + K)));
		}
		
		List<List<List<String>>> deChunks = new ArrayList<>();
		for (int i = 0; i < de_Sentences.size(); i += K) {
			deChunks.add(de_ngrams.subList(i, Math.min(de_ngrams.size(), i + K)));
		}
		

		eval(de_ngrams, new ArrayList<>(), rgChunks, "RM", "DE");
		eval(rg_ngrams, new ArrayList<>(), deChunks, "DE", "RM");
	}

	private void eval(List<List<String>> _ngrams, List<Double> accuracies, List<List<List<String>>> kGroups, String labelKGroups, String labelNGrams) {
		for (int i = 0; i < kGroups.size(); i++) {

			// THE TESTING GROUP
			List<List<String>> tests = kGroups.get(i);

			// THE OTHER GROUPS
			List<List<List<String>>> tmp = new ArrayList<>(kGroups);
			tmp.remove(i);

			List<List<String>> model = new ArrayList<>();
			tmp.forEach(m -> model.addAll(m));

			// THE TRAINING
			Classifier<String, String> bayes = trainTrigrams(labelKGroups, model, labelNGrams, _ngrams);

			// THE CLASSIFICATION
			List<Classification<String, String>> results = new ArrayList<>();
			tests.forEach(features -> {
				Classification<String, String> classification = bayes.classify(features);
				results.add(classification);
			});

			// THE EVALUATION
			List<Classification<String, String>> truePositives = results.stream()
					.filter(r -> r.getCategory().equals(labelKGroups)).collect(Collectors.toList());
			List<Classification<String, String>> falsePositives = results.stream()
					.filter(r -> r.getCategory().equals(labelNGrams)).collect(Collectors.toList());

			Assert.assertEquals(results.size(), truePositives.size() + falsePositives.size());

			int tp = truePositives.size();
			int fp = falsePositives.size();
			int fn = K - tp;
			int tn = K - fp;

			System.out.println(evaluate(i, tp, fp, fn));
			double accuracy = accuracy(tp, fp, fn, tn);
			accuracies.add(accuracy);
		}

		double arithmeticMean = (double) accuracies.stream().mapToDouble(Double::doubleValue).sum() / kGroups.size();
		System.out.println("#################################################");
		System.out.println("k(" + K + ")-fold validation based on *n-gram* model");
		System.out.println("[accuracy]=" + new DecimalFormat("#.###").format(arithmeticMean));
		System.out.println("#################################################");
		System.out.println();
	}

	private double accuracy(int tp, int fp, int fn, int tn) {
		return (double) (tp + tn) / (tp + fp + fn + tn);
	}
	
	private double balancedAccuracy(int tp, int fp, int fn, int tn) {
		return (double) (tp/(tp+fn) + tn/(fp+tn)) / 2;
	}
	
	private String binEval(int i, int tp, int fp, int fn) {
		double tpr = (double) tp / (tp + fn);
		double spc = (double) 4500 / (fp + 4500);
		double ppv = (double) tp / (tp + fp);
		return String.format("[" + i + "]\t[tpr]=%.2f [spc]=%.2f [ppv]=%.2f", tpr, spc, ppv);
	}
	
	private String evaluate(int i, int tp, int fp, int fn) {
		double p = (double) tp / (tp + fp);
		double r = (double) tp / (tp + fn);
		double f = 2 * p * r / (p + r);
		return String.format("[" + i + "]\t[precision]=%.2f [recall]=%.2f [f1]=%.2f", p, r, f);
	}

	private Classifier<String, String> trainTrigrams(String catA, List<List<String>> featuresA, String catB,
			List<List<String>> featuresB) {
		Classifier<String, String> bayes = new BayesClassifier<String, String>();
		bayes.setMemoryCapacity(10000);
		featuresA.forEach(f -> bayes.learn(catA, f));
		featuresB.forEach(f -> bayes.learn(catB, f));
		return bayes;
	}
	
	private List<List<String>> getNGramSequences(List<String> sentences) {
		List<List<String>> sequence = new ArrayList<>();
		for (String sent : sentences) {
			List<String> nGrams = nGramCharSequence(sent);
			sequence.add(nGrams);
			//System.out.println(nGrams);
		}
		return sequence;
	}

	@Test
	public void toBeOrNotToBe() {
		String[] toBeOrNotToBe = { "to", "o_", "_b", "be", "e_", "_o", "or", "r_", "_n", "no", "ot", "t_", "_t", "to", "o_", "_b", "be" };
		List<String> nGrams = nGramCharSequence(new String("to be or not to be"));
		Assert.assertArrayEquals(toBeOrNotToBe, nGrams.toArray());
	}
	
	@Test
	public void abc() {
		String abc = "a b c";
		List<String> nGrams = nGramCharSequence(new String(abc));
		Assert.assertEquals(abc.length() - 1, nGrams.size());
	}
	
	public List<String> nGramCharSequence(String input) {
		List<String> nGrams = new ArrayList<String>();
		input = input.replaceAll(rgx, "").replaceAll(" ", "_").toLowerCase();
		char[] chars = input.toCharArray();
		for (int i = 0; i <= chars.length - N; i++)
			nGrams.add(concat(chars, i, i + N));
		return nGrams;
	}
	
	public String concat(char[] chars, int from, int to) {
		StringBuilder sb = new StringBuilder();
		for (int i = from; i < to; i++)
			sb.append(chars[i]);
		return sb.toString();
	}
	
}

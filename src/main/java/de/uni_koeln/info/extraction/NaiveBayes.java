package de.uni_koeln.info.extraction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.daslaboratorium.machinelearning.classifier.Classification;
import de.daslaboratorium.machinelearning.classifier.Classifier;
import de.daslaboratorium.machinelearning.classifier.bayes.BayesClassifier;

public class NaiveBayes {
	
	
	// Reverse \\p{L}+
	private static String rgx = "[^\\p{L}]+";
	
	
/* ########################################################

				 unigram classification

######################################################## */
	
	
	public static List<String> classifyUnigram(List<String> data) throws IOException {
		
		List<String> classified = new ArrayList<>();
		
		// Get training data
		List<String> rg = Reader.getLines(new File("training-data/rg-sent_4500.txt")); 
		List<String> de = Reader.getLines(new File("training-data/de-sent_4500.txt")); 
		
		Classifier<String, String> bayes = train("RM", rg, "DE", de);
		
		for (String sentence : data) {
			
			String[] split = sentence.split("\\s");
			
			List<String> features = new ArrayList<>();
			
			Arrays.stream(split).forEach(t -> features.add(t.replaceAll(rgx, "")));
			
			Classification<String, String> classification = bayes.classify(features);
			if(classification.getCategory().equalsIgnoreCase("RM"))
				classified.add(sentence);
//			else System.out.println("[DE] " + sentence);
		}
		
		System.out.println(NaiveBayes.class.getName() + "		|	" + classified.size() + " unigram classification");
		
		return classified;
	}

	private static Classifier<String, String> train(String catA, List<String> featuresA, String catB, List<String> featuresB) {
		
		Classifier<String, String> bayes = new BayesClassifier<String, String>();
		bayes.setMemoryCapacity(20000);
		
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

/* ########################################################

 				 n-gram classification

######################################################## */
	
	private static String rgxWhiteSpace = "[^\\p{L}\\s]+";
	private static final int N = 2;
	
	public static List<String> classifyNGram(List<String> data) throws IOException {

		List<String> classified = new ArrayList<>();
		
		List<String> rg = Reader.getLines(new File("training-data/rg-sent_4500.txt")); 
		List<String> de = Reader.getLines(new File("training-data/de-sent_4500.txt")); 
		
		List<List<String>> de_trigrams = getNGramSequences(de);
		List<List<String>> rg_trigrams = getNGramSequences(rg);
		
		Classifier<String, String> bayes = trainTrigrams("RM", rg_trigrams, "DE", de_trigrams);
		
		data.forEach(s -> {
			List<String> features = nGrams(s);
			Classification<String, String> classification = bayes.classify(features);
			if(classification.getCategory().equals("RM"))
				classified.add(s);
//			else System.out.println("[DE] " + s);
		});
		
		System.out.println(NaiveBayes.class.getName() + "		|	" + classified.size() + " n-gram classification");
		
		return classified;
	}
 
	private static List<List<String>> getNGramSequences(List<String> sentences) {
		List<List<String>> sequence = new ArrayList<>();
		for (String sent : sentences) {
			List<String> nGrams = nGrams(sent);
			sequence.add(nGrams);
		}
		return sequence;
	}
	
	private static List<String> nGrams(String input) {
		List<String> nGrams = new ArrayList<String>();
		input = input.replaceAll(rgxWhiteSpace, "").replace(" ", "_").toLowerCase();
		char[] chars = input.toCharArray();
		for (int i = 0; i <= chars.length - N; i++)
			nGrams.add(concat(chars, i, i + N));
		return nGrams;
	}

	private static String concat(char[] tokens, int from, int to) {
		StringBuilder sb = new StringBuilder();
		for (int i = from; i < to; i++)
			sb.append(tokens[i]);
		return sb.toString();
	}
	
	private static Classifier<String, String> trainTrigrams(String catA, List<List<String>> featuresA, String catB,
			List<List<String>> featuresB) {
		Classifier<String, String> bayes = new BayesClassifier<String, String>();
		bayes.setMemoryCapacity(10000);
		featuresA.forEach(f -> bayes.learn(catA, f));
		featuresB.forEach(f -> bayes.learn(catB, f));
		return bayes;
	}

}

package de.uni_koeln.info.misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.daslaboratorium.machinelearning.classifier.Classification;
import de.daslaboratorium.machinelearning.classifier.Classifier;
import de.daslaboratorium.machinelearning.classifier.bayes.BayesClassifier;
import de.uni_koeln.info.extraction.Reader;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

@Deprecated
public class SentenceDetectorComparator {

//	public static void main(String[] args) {
//		try {
//			tokenClassification();
//			trigramClassification();
//			System.out.println("DONE!");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	private static void tokenClassification() throws IOException {
		// Get trinaing data and inititialize classifier
		List<String> rg = Reader.getLines(new File("training-data/rg-sent2.txt")); 
		List<String> de = Reader.getLines(new File("training-data/de-sent2.txt")); 
		Classifier<String, String> bayes = trainWithToken("rg", rg, "de", de);
		
		// Get text that will be segmented into "sentence-chunks"
		StringBuffer segmentable = getSegmentableText();

		// Recognize and get sentences
		List<String> openNLPSents = Arrays.asList(segmentSentencesOpenNLP(segmentable));
		List<String> regexSents = segmentSentencesRegex(segmentable);
		
		// Filter sentence detection with naive bayse classifier
		List<String> germanSenteces = new ArrayList<>();
		List<String> rumantschSenteces = new ArrayList<>();
		classifyTokenSentences(bayes, openNLPSents, germanSenteces, rumantschSenteces, false);
		
		System.out.println("Token language model");
		System.out.println("----------------------");
		System.out.println("	openNLP sentence detector recognized: " + openNLPSents.size());
		System.out.println("	Rgx sentence detector recognized: " + regexSents.size());
		System.out.println("	----------------------");
		System.out.println("	openNLP classification results:");
		System.out.println("		total: " + openNLPSents.size());
		System.out.println("		de_cat: " + germanSenteces.size());
		System.out.println("		rg_cat: " + rumantschSenteces.size());
		
		writeSentenceFile(new File("de_onlp_token_classified.txt"), germanSenteces);
		writeSentenceFile(new File("rg_onlp_token_classified.txt"), rumantschSenteces);
		
		germanSenteces = new ArrayList<>();
		rumantschSenteces = new ArrayList<>();
		
		classifyTokenSentences(bayes, regexSents, germanSenteces, rumantschSenteces, false);
		
		System.out.println("	----------------------");
		System.out.println("	Rgx classification results:");
		System.out.println("		total: " + regexSents.size());
		System.out.println("		de_cat: " + germanSenteces.size());
		System.out.println("		rg_cat: " + rumantschSenteces.size());
		System.out.println();
		
		writeSentenceFile(new File("de_rgx_token_classified.txt"), germanSenteces);
		writeSentenceFile(new File("rg_rgx_token_classified.txt"), rumantschSenteces);
	}

	private static void classifyTokenSentences(Classifier<String, String> bayes, List<String> allSentences,
			List<String> germanSenteces, List<String> rumantschSenteces, boolean print) {
		
		String sentCleanerRgx = "«|»|,|\\.|–|\\:|\\?|\\!|\\(|\\)|\\[|\\]|[0-9]{1,}|;|’|-|'|“|\\&|\"";
		
		Pattern emailRgx = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
		Pattern urlRgx = Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", Pattern.CASE_INSENSITIVE);
		Pattern wwwRgx = Pattern.compile("www\\.", Pattern.CASE_INSENSITIVE);
		Pattern phoneRgx = Pattern.compile("\\+[0-9]{1}[0-9]{1}", Pattern.CASE_INSENSITIVE);
		Pattern phoneLabelRgx = Pattern.compile("\\s(tel)[.]", Pattern.CASE_INSENSITIVE);
		Pattern emailLbaleRgx = Pattern.compile("\\s(e-mail)\\s", Pattern.CASE_INSENSITIVE);
		
		for (String sentence : allSentences) {
			sentence = sentence.trim();
			String[] split = sentence.split("\\s");
			
			if(split.length <= 3) 
				continue;
			
			List<String> features = new ArrayList<>();
			for (String token : split) {
				token = token.replaceAll(sentCleanerRgx, "");
				features.add(token);
			}
			Classification<String, String> classification = bayes.classify(features);
			
			Matcher email = emailRgx.matcher(sentence);
			Matcher url = urlRgx.matcher(sentence);
			Matcher phone = phoneRgx.matcher(sentence);
			Matcher www = wwwRgx.matcher(sentence);
			Matcher phoneLabel = phoneLabelRgx.matcher(sentence);
			Matcher emailLabel = emailLbaleRgx.matcher(sentence);
			
			if(email.find() || url.find() || phone.find() || www.find() || phoneLabel.find() || emailLabel.find()) {
				continue;
			}
			
//			if(phoneLabel.find() || emailLabel.find()) {
//				System.out.println(sentence);
//				continue;
//			}

			if(classification.getCategory().equalsIgnoreCase("rg"))
				rumantschSenteces.add(sentence);
			if(classification.getCategory().equalsIgnoreCase("de"))
				germanSenteces.add(sentence);
			
			if(print) {
				System.out.println("category: " + classification.getCategory());
				System.out.println("probability: " + classification.getProbability());
				System.out.println("tokens: " + features);
				System.out.println("sentencs: " + sentence);
				System.out.println();
			}
		}
	}

	private static Classifier<String, String> trainWithToken(String catA, List<String> featuresA, String catB, List<String> featuresB) {
		// Init classifier and set capacity to 2000 iterations
		Classifier<String, String> bayes = new BayesClassifier<String, String>();
		bayes.setMemoryCapacity(2000);
		String rgx = "«|»|,|\\.|–|\\:|\\?|\\!|\\(|\\)|\\[|\\]|[0-9]{1,}|;|’|-|'|“|\\&|\"";
		for (String tokens : featuresA) {
			String[] split = tokens.split("\\s");
			List<String> features = new ArrayList<>();
			for (String token : split) {
				token = token.replaceAll(rgx, "");
//				System.out.print(token + " ");
				features.add(token);
			}
//			System.out.println();
			bayes.learn(catA, features);
		}
		
		for (String tokens : featuresB) {
			String[] split = tokens.split("\\s");
			List<String> features = new ArrayList<>();
			for (String token : split) {
				token = token.replaceAll(rgx, "");
//				System.out.print(token + " ");
				features.add(token);
			}
//			System.out.println();
			bayes.learn(catB, features);
		}
		
		return bayes;
	}

	private static void trigramClassification() throws IOException {
		// Get trinaing data and inititialize classifier
		List<String> rg = Reader.getLines(new File("training-data/rg-sent2.txt")); 
		List<String> de = Reader.getLines(new File("training-data/de-sent2.txt")); 
		List<List<String>> rmTrigrams = getTrigrams(rg);
		List<List<String>> deTrigrams = getTrigrams(de);
		Classifier<String, String> bayes = trainWithTrigrams("rg", rmTrigrams, "de", deTrigrams);
		
		// Get text that will be segmented into "sentence-chunks"
		StringBuffer segmentable = getSegmentableText();

		// Recognize and get sentences
		List<String> openNLPSents = Arrays.asList(segmentSentencesOpenNLP(segmentable));
		List<String> regexSents = segmentSentencesRegex(segmentable);
		
		// Filter sentence detection with naive bayse classifier
		List<String> germanSenteces = new ArrayList<>();
		List<String> rumantschSenteces = new ArrayList<>();
		classifyTrigramSentences(bayes, openNLPSents, germanSenteces, rumantschSenteces, false);
		
		System.out.println("Trigram language model");
		System.out.println("----------------------");
		System.out.println("	openNLP sentence detector recognized: " + openNLPSents.size());
		System.out.println("	Rgx sentence detector recognized: " + regexSents.size());
		System.out.println("	----------------------");
		System.out.println("	openNLP classification results:");
		System.out.println("		total: " + openNLPSents.size());
		System.out.println("		de_cat: " + germanSenteces.size());
		System.out.println("		rg_cat: " + rumantschSenteces.size());
		
		writeSentenceFile(new File("de_onlp_trigram_classified.txt"), germanSenteces);
		writeSentenceFile(new File("rg_onlp_trigram_classified.txt"), rumantschSenteces);
		
		germanSenteces = new ArrayList<>();
		rumantschSenteces = new ArrayList<>();
		
		classifyTrigramSentences(bayes, regexSents, germanSenteces, rumantschSenteces, false);
		
		System.out.println("	----------------------");
		System.out.println("	Rgx classification results:");
		System.out.println("		total: " + regexSents.size());
		System.out.println("		de_cat: " + germanSenteces.size());
		System.out.println("		rg_cat: " + rumantschSenteces.size());
		System.out.println();
		
		writeSentenceFile(new File("de_rgx_trigram_classified.txt"), germanSenteces);
		writeSentenceFile(new File("rg_rgx_trigram_classified.txt"), rumantschSenteces);
	}

	private static void classifyTrigramSentences(Classifier<String, String> bayes, List<String> allSentences,
			List<String> germanSenteces, List<String> rumantschSenteces, boolean print) {
		List<List<String>> trigrams = getTrigrams(allSentences);
		for (int i = 0; i < trigrams.size(); i++) {
			List<String> list = trigrams.get(i);
			Classification<String, String> classification = bayes.classify(list);
			
			if(classification.getCategory().equalsIgnoreCase("rg"))
				rumantschSenteces.add(allSentences.get(i));
			if(classification.getCategory().equalsIgnoreCase("de"))
				germanSenteces.add(allSentences.get(i));
			
			if(print) {
				System.out.println("category: " + classification.getCategory());
				System.out.println("probability: " + classification.getProbability());
				System.out.println("trigrams: " + list);
				System.out.println("sentencs: " + allSentences.get(i));
				System.out.println();
			}
		}
	}

	private static List<List<String>> getTrigrams(List<String> sentences) {
		List<List<String>> trigrams = new ArrayList<>();
		for (String sent : sentences) {
			List<String> sentence = new ArrayList<>();
			// whitespace included
			String replaceAll = sent.replaceAll("«|»|,|\\.|–|\\:|\\?|\\!|\\(|\\)|\\[|\\]|[0-9]{1,}|;|’|-|'|“|\\&|\"", "").toLowerCase();
			// whitespace omitted
//			String replaceAll = sent.replaceAll("\\s|«|»|,|\\.|–|\\:|\\?|\\!|\\(|\\)|\\[|\\]|[0-9]{1,}|;|’|-|'|“|\\&|\"","").toLowerCase();
			char[] charArray = replaceAll.toCharArray();
			String trigramm = "";
			int j = 0;
			for (int i = 0; i < charArray.length; i++) {
				trigramm += charArray[i];
				if (j == 2) {
//					System.out.print(trigramm + " ");
					sentence.add(trigramm);
					trigramm = "";
					j = 0;
				} else
					j++;
			}
//			System.out.print(trigramm + " ");
//			System.out.println();
			sentence.add(trigramm);
			trigrams.add(sentence);
		}
		return trigrams;
	}

	private static String[] segmentSentencesOpenNLP(StringBuffer segmentable) throws FileNotFoundException {
		// Get opneNLP model for sentence detection
		InputStream modelIn = new FileInputStream("rg-sent.bin");
		String[] sentences = null;
		try {
			SentenceModel model = new SentenceModel(modelIn);
			SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
			sentences = sentenceDetector.sentDetect(segmentable.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				} catch (IOException e) {
					System.err.println(e);
				}
			}
		}
		return sentences;
	}

	private static StringBuffer getSegmentableText() throws FileNotFoundException, IOException {
		StringBuffer segmentable = new StringBuffer();
		File f = new File("output");
		File[] listFiles = f.listFiles();
		for (File file : listFiles) {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String sent;
			while ((sent = br.readLine()) != null) {
				segmentable.append(sent).append(" ");
			}
			br.close();
		}
		return segmentable;
	}

	private static Classifier<String, String> trainWithTrigrams(String catA, List<List<String>> featuresA, String catB, List<List<String>> featuresB) {
		Classifier<String, String> bayes = new BayesClassifier<String, String>();
		bayes.setMemoryCapacity(2000);
		for (List<String> featureList : featuresA)
			bayes.learn(catA, featureList);
		for (List<String> featureList : featuresB)
			bayes.learn(catB, featureList);
		return bayes;
	}
	
	private static List<String> segmentSentencesRegex(StringBuffer segmentable) {
		List<String> sentences = new ArrayList<>();
		String source = segmentable.toString();
		String regex = "[^.!?\\s][^.!?]*(?:[.!?](?!['\"]?\\s|$)[^.!?]*)*[.!?]?['\"]?(?=\\s|$)";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(source);
		while (matcher.find()) {
			String sentence = matcher.group();
			sentences.add(sentence);
		}
		return sentences;
	}
	
	public static void writeSentenceFile(File file, List<String> data) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		for (int i = 0; i < data.size(); i++) {
			bw.write(data.get(i));
			bw.newLine();
		}
		bw.close();
	}

}
package de.uni_koeln.info.extraction;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

public class SentenceDetector {
	
	
	public static List<String> segmentSentencesBreakIterator(List<String> texts) throws FileNotFoundException {
		List<String> sentences = new ArrayList<>();
		for (String input : texts) {
			if(!input.isEmpty()) {
				BreakIterator sentenceIterator = BreakIterator.getSentenceInstance();
				sentenceIterator.setText(input);
				int start = sentenceIterator.first();
				for (int end = sentenceIterator.next(); end != BreakIterator.DONE; start = end, end = sentenceIterator.next()) {
					sentences.add(input.substring(start, end).trim());
				}
			}
		}
		System.out.println(SentenceDetector.class.getName() + "	|	" + sentences.size() + " sentences with BreakIterator");
		return sentences;
	}


	public static String[] segmentSentencesOpenNLP(List<String> texts) throws FileNotFoundException {
		
		StringBuffer segmentable = new StringBuffer();
		texts.forEach(s -> {
				if(!s.isEmpty()) {
					segmentable.append(s);
				}
		});
		
		InputStream modelIn = new FileInputStream("ALLEGRA_corpus/rg-sent.bin");
		String[] sentences = null;
		try {
			SentenceModel model = new SentenceModel(modelIn);
			opennlp.tools.sentdetect.SentenceDetector sentenceDetector = new SentenceDetectorME(model);
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
		
		System.out.println(SentenceDetector.class.getName() + "	|	" + sentences.length + " sentences with OpenNLP");
		
		return sentences;
	}

}
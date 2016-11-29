package de.uni_koeln.info.extraction;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

/**
 * This class represents a preprocessor, whichs runs multiple routines
 * successively nessessary for preparing the input data for the index.
 * 
 * @author matana (Mihail Atanassov)
 *
 */
public class Preprocessor {

	public static void main(String[] args) throws IOException, TikaException, SAXException {
		
		// Each String elements contains the textual content of a MS Word document
		List<String> rawTexts = Reader.read(new File("input"));
		
		// OpenNLP SentenceDetector detects sentence bounderies and returns recognized sentences 
		//String[] sentences = SentenceDetector.segmentSentencesOpenNLP(rawTexts);
		
		List<String> sentences = SentenceDetector.segmentSentencesBreakIterator(rawTexts);
		
		// Remove 'noise', e.g. urls, emails, phone numbers etc.
		List<String> filtered = RegEx.filter(sentences);
		
		// Filter german sentences
		List<String> rumantschSentences = NaiveBayes.classifyNGram(filtered);
		
		// Write result to output file
		Writer.writeSentenceFile(new File("output/preprocessor_output.txt"), rumantschSentences);
		
	}
}

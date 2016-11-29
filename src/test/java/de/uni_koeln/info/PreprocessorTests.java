package de.uni_koeln.info;

import java.io.File;
import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import de.uni_koeln.info.extraction.Reader;
import de.uni_koeln.info.extraction.SentenceDetector;
import de.uni_koeln.info.extraction.Writer;

public class PreprocessorTests {
	
	//@Test
	public void brekIterator() throws IOException, TikaException, SAXException {
		List<String> rawTexts = Reader.read(new File("input"));
		long time = System.currentTimeMillis();
		List<String> sentences = new ArrayList<>();
		for (String input : rawTexts) {
			if(!input.isEmpty()) {
				BreakIterator sentenceIterator = BreakIterator.getSentenceInstance();
				sentenceIterator.setText(input);
				int start = sentenceIterator.first();
				for (int end = sentenceIterator.next(); end != BreakIterator.DONE; start = end, end = sentenceIterator.next()) {
					sentences.add(input.substring(start, end).trim());
				}
			}
		}
		time = System.currentTimeMillis() - time;
		System.out.println("BreakIterator detected " + sentences.size() + " ... took " + time);
		Writer.writeSentenceFile(new File("junit_tests_break_iterator.txt"), sentences);
	}
	
	//@Test
	public void sentenceDetector() throws IOException, TikaException, SAXException {
		List<String> rawTexts = Reader.read(new File("input"));
		long time = System.currentTimeMillis();
		String[] sentences = SentenceDetector.segmentSentencesOpenNLP(rawTexts);
		time = System.currentTimeMillis() - time;
		System.out.println("SentenceDetector detected " + sentences.length + " ... took " + time);
		Writer.writeSentenceFile(new File("junit_tests_sent_detect.txt"), Arrays.asList(sentences));
	}
}

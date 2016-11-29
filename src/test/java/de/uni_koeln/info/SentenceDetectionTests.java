package de.uni_koeln.info;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.BreakIterator;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

public class SentenceDetectionTests {


	private String input = "Joachim Ringelnatz wurde am am 07.08.1883 in Wurzen (Sachsen) geboren und ist am 17.11.1934 in Berlin gestorben. Seinen Lebensunterhalt verdiente er sich als Bibliothekar bei der gräflichen Familie Yorck von Wartenburg in Schlesien und im Elternhaus des Balladendichters Börries von Münchhausen in Hannover sowie als Fremdenführer auf einer Burg. Am 1.8.1914 schrieb er schwungvoll in sein Tagebuch »Ich ziehe in den Krieg!« Nach dem Kriege versuchte er sich in unterschiedlichen Branchen, u.a. in einer Gartenbauschule und als Archivar in einem Berliner Verlag. 1920 erhielt er ein Engagement an der Berliner Kleinkunstbühne »Schall und Rauch«.";
	private static final int COUNT = 5;
	
	@Test
	public void breakIterator() {
		System.out.println("############################################");
		System.out.println("Sentence Detection with Java's BreakIterator");
		System.out.println("############################################");
		BreakIterator sentenceIterator = BreakIterator.getSentenceInstance(Locale.GERMAN);
		sentenceIterator.setText(input);
		int start = sentenceIterator.first();
		int count = 0;
		for (int end = sentenceIterator.next(); end != BreakIterator.DONE; start = end, end = sentenceIterator.next()) {
			System.out.printf("[%s]: %s \n",  count++, input.substring(start, end));
		}
		Assert.assertNotEquals(COUNT, count);
	}
	
	@Test
	public void simpleRegex() {
		System.out.println("##########################################");
		System.out.println("Sentence Detection with simple RegEx [.?!]");
		System.out.println("##########################################");
		String delimiter = "[.!?]";
		String[] sentences = input.split(delimiter);
		int count = 0;
		for (String sent : sentences) {
			System.out.printf("[%s]: %s \n", count++, sent.trim());
		}
		Assert.assertNotEquals(COUNT, count);
	}

	@Test
	public void openNLPSentenceDetection() throws FileNotFoundException {
		System.out.println("########################################################");
		System.out.println("Sentence Detection with openNLP (Trained on tiger data.)");
		System.out.println("########################################################");
		// http://opennlp.sourceforge.net/models-1.5/
		String binFile = getClass().getClassLoader().getResource("de-sent.bin").getFile();
		InputStream modelIn = new FileInputStream(binFile);
		try {
			SentenceModel model = new SentenceModel(modelIn);
			opennlp.tools.sentdetect.SentenceDetector sentenceDetector = new SentenceDetectorME(model);
			String[] sentences = sentenceDetector.sentDetect(input);
			int count = 0;
			for (String sent : sentences) {
				System.out.printf("[%s]: %s \n", count++, sent.trim());
			}
			Assert.assertEquals(COUNT, sentences.length);
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
	}

}

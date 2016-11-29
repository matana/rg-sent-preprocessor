package de.uni_koeln.info.misc;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import opennlp.tools.cmdline.sentdetect.SentenceDetectorEvaluatorTool;
import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.sentdetect.SentenceDetectorFactory;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.sentdetect.SentenceSample;
import opennlp.tools.sentdetect.SentenceSampleStream;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;

public class SentDetectTrainer {

	public static void train() throws IOException {

		// String input = "training-data/sent/rg-sent.train";
		// String output = "training-data/model/rg-sent.bin";

		String input = "ALLEGRA_corpus/rg-sent.train";
		String output = "ALLEGRA_corpus/rg-sent.bin";
		String evalData = "ALLEGRA_corpus/rg-sent.eval";

		ObjectStream<String> lineStream = new PlainTextByLineStream(new MarkableFileInputStreamFactory(new File(input)),
				"UTF-8");
		ObjectStream<SentenceSample> sampleStream = new SentenceSampleStream(lineStream);
		SentenceModel model;

		try (FileInputStream dict = new FileInputStream("training-data/abb.xml")) {
			SentenceDetectorFactory factory = new SentenceDetectorFactory("rg", false, new Dictionary(dict),
					new char[] { '.', '?', '!' });
			model = SentenceDetectorME.train("rg", sampleStream, factory, TrainingParameters.defaultParams());
		} finally {
			sampleStream.close();
		}

		OutputStream modelOut = null;
		try {
			modelOut = new BufferedOutputStream(new FileOutputStream(new File(output)));
			model.serialize(modelOut);
		} finally {
			if (modelOut != null)
				modelOut.close();
		}

		SentenceDetectorEvaluatorTool sentenceDetectorEvaluatorTool = new SentenceDetectorEvaluatorTool();
		sentenceDetectorEvaluatorTool.run("opennlp", new String[] { "-model", output, "-data", evalData });
	}

}

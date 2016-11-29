package de.uni_koeln.info.extraction;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

public class Reader {
	
	/**
	 * This method reads and parses Microsoft Word documents. The extracted
	 * plain text of each document represents an element in the returned list.
	 */
	public static List<String> read(File input) throws IOException, SAXException, TikaException {
		List<String> toReturn = new ArrayList<>();
		File[] files = input.listFiles();
		int count = 0;
		for (File f : files) {
			if (!f.isDirectory()) {
				BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
				BodyContentHandler handler = new BodyContentHandler(-1);
				Parser parser = new AutoDetectParser();
				parser.parse(bis, handler, new Metadata(), new ParseContext());
				toReturn.add(handler.toString());
				count++;
			}
		}
		System.out.println(Reader.class.getName() + "		|	input: " + count + " files");
		return toReturn;
	}

	/**
	 * This routine reads and parses Microsoft Word documents. The extracted
	 * plain text of each document represents an element in the returned list.
	 * 
	 * @throws IOException
	 */
	public static List<String> readFile(File input) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(input));
		String line;
		List<String> toreturn = new ArrayList<>();

		try {
			while ((line = br.readLine()) != null) {
				toreturn.add(line);
			}
		} finally {
			br.close();
		}

		return toreturn;
	}

	public static List<String> getLines(File readable) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(readable));
		List<String> lines = new ArrayList<>();
		String line;
		while ((line = br.readLine()) != null) {
			lines.add(line);
		}
		br.close();
		return lines;
	}

	public static String getContent(File readable) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(readable));
		StringBuffer sb = new StringBuffer();
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		br.close();
		return sb.toString();
	}
}

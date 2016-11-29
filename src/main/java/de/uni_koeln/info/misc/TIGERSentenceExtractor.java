package de.uni_koeln.info.misc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TIGERSentenceExtractor {
	
	
	public static void extract() throws SAXException, IOException, ParserConfigurationException {
		
		// Structure: http://www.ims.uni-stuttgart.de/forschung/ressourcen/werkzeuge/TIGERSearch/doc/html/TigerXML.html
		File corpus = new File("TiGer_corpus/tiger_release_aug07.corrected.16012013.xml");
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(corpus);
		doc.getDocumentElement().normalize();
		NodeList terminals = doc.getElementsByTagName("terminals"); 
		
		int sentenceCount = terminals.getLength();
		
		List<String> germanSentences = new ArrayList<>();
		for (int i = 0; i < terminals.getLength(); i++) {
			Node t = terminals.item(i);
			NodeList words = t.getChildNodes();
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < words.getLength(); j++) {
				Node word = words.item(j);
				if (word.getNodeType() == Node.ELEMENT_NODE) {
					Element wordElement = (Element) word;
					String wordValue = wordElement.getAttribute("word");
					sb.append(wordValue).append(" ");
				}
			}
			double random = Math.random();
			// Extract only 10 % randomly
			if(random > 0.9d) {
				germanSentences.add(sb.toString());
			}
			
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("TiGer_corpus/tiger_corpus_de_sent.txt")));
		int count = 0;
		for (String deSent : germanSentences) {
			deSent = deSent.replaceAll("[^\\p{L}]", " ").trim();	
			String[] data = deSent.split("\\s");
			StringBuilder sb = new StringBuilder();
			for (String token : data) {
				token = token.trim();
				if(token.length() > 1) {
					sb.append(token).append(" ");
				}
 			}
			bw.write(sb.toString());
			bw.newLine();
			count++;
		}
		System.out.println(TIGERSentenceExtractor.class.getName() + "	|	extcracted " + count + " sentences of " + sentenceCount);
		bw.close();
	}

}

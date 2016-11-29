package de.uni_koeln.info.extraction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Writer {

	/**
	 * This routine writes the extracted sentences into a plain text file. Each
	 * line represents a sentence.
	 */
	public static void writeSentenceFile(File file, List<String> data) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		for (int i = 0; i < data.size(); i++) {
			bw.write(data.get(i));
			bw.newLine();
		}
		bw.close();
		
		System.out.println(Writer.class.getName() + "		|	ouput: '" + file + "'");
	
	}
	
}

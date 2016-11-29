package de.uni_koeln.info.extraction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author matana (Mihail Atanassov)
 *
 */
public class RegEx {

	private static Pattern phoneRgx = Pattern.compile("((tel.{4}|fax|mobil|service|numer|gratuit)(\\.|:|\\.:)?)?\\s?\\+?[0-9()\\s]{11,18}", Pattern.CASE_INSENSITIVE);
	private static Pattern emailRgx = Pattern.compile("[a-z0-9._-]+@[a-z0-9.-]+\\.[a-z]{2,6}", Pattern.CASE_INSENSITIVE);
	private static Pattern urlRgx = Pattern.compile("(https?)://[a-z.]*[a-z0-9+&#/%=\\.\\-~_]", Pattern.CASE_INSENSITIVE);
	private static Pattern mediaTokenRgx = Pattern.compile("(SSR|SRG|RTR|ARD|SRF|RTL|UFEL|AFS)");
	private static Pattern formatRgx = Pattern.compile("(.\\.jpg|.\\.pdf|.\\.png|.\\.docx?|.\\.txt)", Pattern.CASE_INSENSITIVE);
	private static Pattern postRgx = Pattern.compile("CH-[0-9]{4}", Pattern.CASE_INSENSITIVE);
	private static Pattern wwwRgx = Pattern.compile("www\\.", Pattern.CASE_INSENSITIVE);
	private static Pattern whiteSpaceRgx = Pattern.compile("(\\s{2,}|\t)");
	private static Pattern currencyRgx = Pattern.compile("(CHF | EURO | EUR)", Pattern.CASE_INSENSITIVE);
	private static Pattern lineStartRgx = Pattern.compile("^\\([A-Za-z]{2,}\\)");

	/**
	 * This routine removes 'noise' from the given text. By 'noise' it means all
	 * tokens matching the patterns defined in {@link RegEx}, e.g. urls, emails,
	 * phone numbers etc.
	 */
	public static List<String> filter(String[] sentences) throws IOException {
		List<String> filtered = new ArrayList<>();
		for (String sent : sentences) {
			sent = removeNoise(filtered, sent); 
		}
		System.out.println(RegEx.class.getName() + "		|	" + filtered.size() + " sentences after noise reduction");
		return filtered;
	}
	
	public static List<String> filter(List<String> sentences) throws IOException {
		List<String> filtered = new ArrayList<>();
		for (String sent : sentences) {
			sent = removeNoise(filtered, sent); 
		}
		System.out.println(RegEx.class.getName() + "		|	" + filtered.size() + " sentences after noise reduction");
		return filtered;
	}

	private static String removeNoise(List<String> filtered, String sent) {
		Matcher email = emailRgx.matcher(sent);
		Matcher url = urlRgx.matcher(sent);
		Matcher phone = phoneRgx.matcher(sent);
		Matcher www = wwwRgx.matcher(sent);
		Matcher mediaToken = mediaTokenRgx.matcher(sent);
		Matcher post = postRgx.matcher(sent);
		Matcher currency = currencyRgx.matcher(sent);
		Matcher format = formatRgx.matcher(sent);
		
		// ignore the noise
		if (email.find() || url.find() || phone.find() || www.find() || mediaToken.find() || post.find()
				|| currency.find() || format.find()) {
			return sent;
		}
		
		if (sent.split(" ").length > 3) {
			// ignore sentences containing '*'
			if (sent.contains("*"))
				return sent;
			//sent = sent.replaceFirst("· ", "");
			Matcher whiteSpace = whiteSpaceRgx.matcher(sent);
			if (!whiteSpace.find()) {
				if (Pattern.matches("[^.!?\\s][^.!?]*(?:[.!?](?!['\"]?\\s|$)[^.!?]*)*[.!?]?['\"]?(?=\\s|$)", sent)) {
					Matcher lineStart = lineStartRgx.matcher(sent);
					if(lineStart.find()) {
						sent = sent.substring(lineStart.end(), sent.length()).trim();
					}
					String replaceAll = sent.replaceAll("\\[footnoteRef:[0-9]{1,}\\] ", "");
					
					String[] split = replaceAll.split("[\n\r]");
					for (String string : split) {
						if(string.split(" ").length > 5) {
							filtered.add(string);
						}
					}
				} 
			}
		}
		return sent;
	}
}

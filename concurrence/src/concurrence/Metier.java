package concurrence;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Metier implements Runnable {
	private int id;
	private final Object obj;
	private final List<String> FILE_EXTENSION = Arrays.asList("php", "html", "htm");
	private Methodes methodes;

	Metier(HashSet<String> resteFaire) {
		id = new Random().nextInt(1000 + 1);
		obj = new Object();
		methodes =new Methodes(resteFaire);
	}

	@Override
	public void run() {
		while (Methodes.encoreFaire() || Main.workers.get() > 0) {
			if (Methodes.encoreFaire()) {
				String strURL = Methodes.getResteFaire();
				if (strURL != null) {
					Main.workers.incrementAndGet();
					System.out.println("id = "+id + " : " + strURL);
					try {
						URL url = new URL(strURL);
						try {
							BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
							String inputLine;
							Pattern patternSearch = Pattern.compile(Main.search);
							Pattern patternLink = Pattern.compile("href=\"([^\"]*)\"", Pattern.DOTALL);
							Matcher matcherSearch;
							Matcher matcherLink;
							int count = 0;
							HashSet<String> resteFaire = new HashSet<>();
							while ((inputLine = in.readLine()) != null) {
								matcherSearch = patternSearch.matcher(inputLine);
								matcherLink = patternLink.matcher(inputLine);
								while (matcherSearch.find())
									count++;
								if (matcherLink.find()) {
									String groupStr = matcherLink.group(1);
									try {
										URL currentURL;
										if (groupStr.substring(0, 1).equals("/")) {
											currentURL = new URL(url.getProtocol() + "://" + url.getHost() + groupStr);
										} else
											currentURL = new URL(groupStr);
										String fileName = currentURL.getFile();
										int i = fileName.lastIndexOf(".");
										if (i > 0) {
											String fileExtension = fileName.substring(i + 1);
											if (FILE_EXTENSION.contains(fileExtension)) {
												resteFaire.add(currentURL.toString());
											}
										} else {
											resteFaire.add(currentURL.toString());
										}
									} catch (Exception ignored) {
									}
								}
							}
							if (count != 0) {
								for (String tmpURL : resteFaire) {
									Methodes.ajouterResteFaire(tmpURL);
									Methodes.notifyListAttente();
								}
								Main.count.addAndGet(count);
							}
							in.close();
							
						} catch (Exception e) {
							System.out.println("Erreur lors de la lecture : \n" + e.getMessage());
						}finally {
							Main.workers.decrementAndGet();
						}
					} catch (MalformedURLException e) {
						Main.workers.decrementAndGet();
					}
				}
			} else {
				try {
					System.out.println("id = "+id + " est en attente");
					synchronized (obj) {
						Methodes.ajouterListAttente(obj);
						obj.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		Methodes.notifyListAttente();
	}
}

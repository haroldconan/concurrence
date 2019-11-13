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

public class ThreadsConcurrence implements Runnable {
	private int id;
	private final Object obj;
	private final List<String> FILE_EXTENSION = Arrays.asList("php", "html", "htm");

	ThreadsConcurrence() {
		this.id = new Random().nextInt(1000 + 1);
		this.obj = new Object();
	}

	@Override
	public void run() {
		while (Metier.stillTodo() || Metier.workers.get() > 0) {
			if (Metier.stillTodo()) {
				String strURL = Metier.getTodo();
				if (strURL != null) {
					Metier.workers.incrementAndGet();
					System.out.println(this.id + " : " + strURL);
					try {
						URL url = new URL(strURL);
						try {
							BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
							String inputLine;
							Pattern patternSearch = Pattern.compile(Metier.search);
							Pattern patternLink = Pattern.compile("href=\"([^\"]*)\"", Pattern.DOTALL);
							Matcher matcherSearch;
							Matcher matcherLink;
							int count = 0;
							HashSet<String> todo = new HashSet<>();
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
												todo.add(currentURL.toString());
											}
										} else {
											todo.add(currentURL.toString());
										}
									} catch (Exception ignored) {
									}
								}
							}
							if (count != 0) {
								for (String tmpURL : todo) {
									Metier.addTodo(tmpURL);
									Metier.notifyWaitingList();
								}
								Metier.count.addAndGet(count);
							}
							in.close();
							Metier.workers.decrementAndGet();
						} catch (Exception e) {
							Metier.workers.decrementAndGet();
						}
					} catch (MalformedURLException e) {
						Metier.workers.decrementAndGet();
					}
				}
			} else {
				try {
					System.out.println(this.id + " : waiting");
					synchronized (this.obj) {
						Metier.addWaitingList(this.obj);
						obj.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		Metier.notifyWaitingList();
	}
}

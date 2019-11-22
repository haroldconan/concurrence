package com.tp.concurrence;

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
	@SuppressWarnings("unused")
	private Methodes methodes;

	/**
	 * Constructeur
	 * 
	 * @param resteFaire --> list d'url
	 */
	Metier(HashSet<String> resteFaire) {
		id = new Random().nextInt(1000 + 1);// plage de choix de thread
		obj = new Object();// Objet de synchronisation
		methodes = new Methodes(resteFaire);
	}

	/**
	 * Méthode qui appelé pour lancer un thread
	 */
	@Override
	public void run() {
		while (Methodes.encoreFaire() || Main.workers.get() > 0) { // tant qu'il rest des url à parcourir ou que le
																	// nombre de thread est > à 0
			if (Methodes.encoreFaire()) {// si il rest des url à parcourir
				String strURL = Methodes.getResteFaire(); // on sort l'url
				if (strURL != null) {
					Main.workers.incrementAndGet();// on incrémente le compteur de mots trouvé
					System.out.println("id = " + id + " : " + strURL);

					try {
						URL url = new URL(strURL);
						try {
							BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));// on lit le
																											// lien
							String inputLine;// ligne
							Pattern patternSearch = Pattern.compile(Main.motChercher);// mot à chercher
							Pattern patternLink = Pattern.compile("href=\"([^\"]*)\"", Pattern.DOTALL);// patterne d'url
							Matcher matcherSearch;// matcher pour le mot
							Matcher matcherLink;// matcher pour l'url
							int count = 0; // conteur de mot
							HashSet<String> resteFaire = new HashSet<>();// liste de lien qui vont être trouvés
							while ((inputLine = in.readLine()) != null) {// tant qu'il reste des lignes à lire
								matcherSearch = patternSearch.matcher(inputLine);
								matcherLink = patternLink.matcher(inputLine);
								while (matcherSearch.find())// tant que le mot est trouvé
									count++;// on incrémante le compteur
								if (matcherLink.find()) {// si un lien est trouvé
									String groupStr = matcherLink.group(1);// on récupère le lien
									URL currentURL = null;
									try {
										if (groupStr.substring(0, 1).equals("/")) {// si c'est un lien local au site
																					// présent
											currentURL = new URL(url.getProtocol() + "://" + url.getHost() + groupStr);
											// on complète le lien pour qu'il fonctionne tout seul
										} else {
											currentURL = new URL(groupStr);// si non il créé l'url comme trouvée
										}
									} catch (Exception e) {
										// on ignore l'erreur car on vérifi simplement si il s'agit bien d'un lien
									}
									if (currentURL != null) {// si on a un lien
										resteFaire.add(currentURL.toString());// on l'ajout permis ceux à parcourir
									}
								}
							}
							if (count != 0) {//si on à trouvé des lien
								for (String tmpURL : resteFaire) {//pour chaque url à parcourir
									Methodes.ajouterResteFaire(tmpURL);//on ajoute à la queue de lien à parcourir
									Methodes.notifyListAttente();//on notifi qu'il y a une nouvelle tâche à faire
								}
								Main.count.addAndGet(count);//on ajout le nombre de lien trouvé
							}
							in.close();//on ferme le stream
							System.out.println(
									"Le mot " + Main.motChercher + " est trouvé " + count + " fois sur ce site");
						} catch (Exception e) {
							System.out.println("Erreur lors de la lecture : \n" + e.getMessage());
						} finally {
							Main.workers.decrementAndGet();//le thread à fini de travailler alors on décrémente le nombre de thread en cour
						}
					} catch (MalformedURLException e) {
						Main.workers.decrementAndGet();//le thread à fini de travailler alors on décrémente le nombre de thread en cour
					}
				}
			} else {
				try {
					System.out.println("id = " + id + " est en attente");
					synchronized (obj) {// on se synchronise sur l'objet
						Methodes.ajouterListAttente(obj);//on l'ajout à la liste d'attente
						obj.wait();//on lui dit d'attendre
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		Methodes.notifyListAttente();//on notifi qu'une place est libre
	}
}

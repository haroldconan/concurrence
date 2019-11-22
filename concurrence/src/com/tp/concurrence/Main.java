package com.tp.concurrence;

import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Main {

	private static volatile HashSet<String> txtVue = new HashSet<>();
	private static volatile HashSet<String> resteFaire = new HashSet<>();
	private static ReentrantReadWriteLock txtVueLock = new ReentrantReadWriteLock();
	
	static volatile AtomicInteger count = new AtomicInteger(0);
	static AtomicInteger workers = new AtomicInteger(0);
	static String motChercher;
	
	
	/**Point d'entrée du programme
	 * @param args
	 */
	public static void main(String[] args) {
			motChercher="nantes";
			resteFaire.add("https://fr.wikipedia.org/wiki/Nantes");
		ExecutorService executorService = Executors.newCachedThreadPool();//un récupère les tâches en cours
		for (int i = 0; i < 2; i++)
			executorService.execute(new Metier(resteFaire));//on execute la tâche à faire
		executorService.shutdown();//on la tue
		try {
			executorService.awaitTermination(1, TimeUnit.MINUTES);//on donne une 1 minutes de vie max à la tâche
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("\n======================================================================================\n");
		System.out.println("\t\tle texte "+motChercher+" a été trouvé "+count+" fois");
		System.out.println("\n======================================================================================\n");

	}
	/**
	 * Méthode qui permet de dire si le lien courant est dans l'url :
	 * @param url
	 * @return
	 */
	static boolean trouvee(String url) {
		txtVueLock.readLock().lock();//on bloque le thread
		boolean result = txtVue.contains(url);//si le lien à déjà était visité
		txtVueLock.readLock().unlock();//on deboque le thread
		return result;
	}

	/**Méthode qui ajoute l'url trouvée à la liste de celles qui sont déjà lue
	 * @param url
	 */
	static void ajouterTxtTrouvee(String url) {
		txtVueLock.writeLock().lock();//on bloque le thread
		txtVue.add(url);//on ajout l'url trouvée
		txtVueLock.writeLock().unlock();//on deboque le thread
	}
	

}

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
	
	
	/**Point d'entr�e du programme
	 * @param args
	 */
	public static void main(String[] args) {
			motChercher="nantes";
			resteFaire.add("https://fr.wikipedia.org/wiki/Nantes");
		ExecutorService executorService = Executors.newCachedThreadPool();//un r�cup�re les t�ches en cours
		for (int i = 0; i < 2; i++)
			executorService.execute(new Metier(resteFaire));//on execute la t�che � faire
		executorService.shutdown();//on la tue
		try {
			executorService.awaitTermination(1, TimeUnit.MINUTES);//on donne une 1 minutes de vie max � la t�che
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("\n======================================================================================\n");
		System.out.println("\t\tle texte "+motChercher+" a �t� trouv� "+count+" fois");
		System.out.println("\n======================================================================================\n");

	}
	/**
	 * M�thode qui permet de dire si le lien courant est dans l'url :
	 * @param url
	 * @return
	 */
	static boolean trouvee(String url) {
		txtVueLock.readLock().lock();//on bloque le thread
		boolean result = txtVue.contains(url);//si le lien � d�j� �tait visit�
		txtVueLock.readLock().unlock();//on deboque le thread
		return result;
	}

	/**M�thode qui ajoute l'url trouv�e � la liste de celles qui sont d�j� lue
	 * @param url
	 */
	static void ajouterTxtTrouvee(String url) {
		txtVueLock.writeLock().lock();//on bloque le thread
		txtVue.add(url);//on ajout l'url trouv�e
		txtVueLock.writeLock().unlock();//on deboque le thread
	}
	

}

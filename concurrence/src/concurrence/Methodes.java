package concurrence;

import java.util.HashSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Methodes {

	private static volatile HashSet<Object> AttenteList = new HashSet<>();
	private static volatile HashSet<String> resteFaire = new HashSet<>();

	private static ReentrantReadWriteLock resteFaireLock = new ReentrantReadWriteLock();
	private static ReentrantReadWriteLock AttenteListLock = new ReentrantReadWriteLock();
	
	
	
	/**
	 * constructeur
	 * @param r liste de lien à parcourir
	 */
	public Methodes(HashSet<String> r) {
		super();
		// TODO Auto-generated constructor stub
		resteFaire = r;//
	}

	/**
	 * Méthode qui permet de donner un lien qui reste à parcourir
	 * 
	 * @return
	 */
	static String getResteFaire() {
		resteFaireLock.writeLock().lock();//on bloque le thread
		String result = null;
		if (encoreFaire()) {//si il reste des lien à parcourir
			result = resteFaire.iterator().next();//on récupère le dernier lien
			resteFaire.remove(result);// on l'enlève de la liste
			Main.ajouterTxtTrouvee(result);//on le donne au main pour l'affichage
		}
		resteFaireLock.writeLock().unlock();// on ddébloque le thread
		return result;//on retourne le lien qui reste ou null si il n'y en a plus
	}

	/**
	 * Méthode qui permet d'ajouter un lien dans la liste de lien à parcourir
	 * @param url
	 */
	static void ajouterResteFaire(String url) {
		resteFaireLock.writeLock().lock();//on bloque le thread
		if (!Main.trouvee(url))//Si l'url n'a pas était visitée
			resteFaire.add(url);//on m'ajout  à la queue
		resteFaireLock.writeLock().unlock();//on deboque le thread
	}

	/**
	 * Méthode qui permet de reparcourir
	 * @return
	 */
	static boolean encoreFaire() {
		resteFaireLock.readLock().lock();//on bloque le thread
		boolean result = resteFaire.size() > 0;// si il faut refaire
		resteFaireLock.readLock().unlock();//on deboque le thread
		return result;
	}

	static void ajouterListAttente(Object obj) {
		AttenteListLock.writeLock().lock();//on bloque le thread
		AttenteList.add(obj);//on ajout l'objet à la liste d'attente
		AttenteListLock.writeLock().unlock();//on deboque le thread
	}

	static void notifyListAttente() {
		AttenteListLock.writeLock().lock();//on bloque le thread
		for (Object obj : AttenteList) {//pour chaque objet en attente
			synchronized (obj) {//on se synchronise
				obj.notify();//en nitifiant qu'on attend
			}
			AttenteList.remove(obj);//il est synchonisé donc on l'enlève de la liste d'attente
		}
		AttenteListLock.writeLock().unlock();//on deboque le thread
	}
}

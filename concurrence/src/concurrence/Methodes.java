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
	 * @param r liste de lien � parcourir
	 */
	public Methodes(HashSet<String> r) {
		super();
		// TODO Auto-generated constructor stub
		resteFaire = r;//
	}

	/**
	 * M�thode qui permet de donner un lien qui reste � parcourir
	 * 
	 * @return
	 */
	static String getResteFaire() {
		resteFaireLock.writeLock().lock();//on bloque le thread
		String result = null;
		if (encoreFaire()) {//si il reste des lien � parcourir
			result = resteFaire.iterator().next();//on r�cup�re le dernier lien
			resteFaire.remove(result);// on l'enl�ve de la liste
			Main.ajouterTxtTrouvee(result);//on le donne au main pour l'affichage
		}
		resteFaireLock.writeLock().unlock();// on dd�bloque le thread
		return result;//on retourne le lien qui reste ou null si il n'y en a plus
	}

	/**
	 * M�thode qui permet d'ajouter un lien dans la liste de lien � parcourir
	 * @param url
	 */
	static void ajouterResteFaire(String url) {
		resteFaireLock.writeLock().lock();//on bloque le thread
		if (!Main.trouvee(url))//Si l'url n'a pas �tait visit�e
			resteFaire.add(url);//on m'ajout  � la queue
		resteFaireLock.writeLock().unlock();//on deboque le thread
	}

	/**
	 * M�thode qui permet de reparcourir
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
		AttenteList.add(obj);//on ajout l'objet � la liste d'attente
		AttenteListLock.writeLock().unlock();//on deboque le thread
	}

	static void notifyListAttente() {
		AttenteListLock.writeLock().lock();//on bloque le thread
		for (Object obj : AttenteList) {//pour chaque objet en attente
			synchronized (obj) {//on se synchronise
				obj.notify();//en nitifiant qu'on attend
			}
			AttenteList.remove(obj);//il est synchonis� donc on l'enl�ve de la liste d'attente
		}
		AttenteListLock.writeLock().unlock();//on deboque le thread
	}
}

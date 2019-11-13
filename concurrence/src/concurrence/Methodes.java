package concurrence;

import java.util.HashSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Methodes {

	private static volatile HashSet<Object> AttenteList = new HashSet<>();
	private static volatile HashSet<String> resteFaire = new HashSet<>();

	private static ReentrantReadWriteLock resteFaireLock = new ReentrantReadWriteLock();
	private static ReentrantReadWriteLock AttenteListLock = new ReentrantReadWriteLock();
	
	
	
	public Methodes(HashSet<String> r) {
		super();
		// TODO Auto-generated constructor stub
		resteFaire = r;
	}

	static String getResteFaire() {
		resteFaireLock.writeLock().lock();
		String result = null;
		if (encoreFaire()) {
			result = resteFaire.iterator().next();
			resteFaire.remove(result);
			Main.ajouterTxtTrouvee(result);
		}
		resteFaireLock.writeLock().unlock();
		return result;
	}

	static void ajouterResteFaire(String url) {
		resteFaireLock.writeLock().lock();
		if (!Main.trouvee(url))
			resteFaire.add(url);
		resteFaireLock.writeLock().unlock();
	}

	static boolean encoreFaire() {
		resteFaireLock.readLock().lock();
		boolean result = resteFaire.size() > 0;
		resteFaireLock.readLock().unlock();
		return result;
	}

	static void ajouterListAttente(Object obj) {
		AttenteListLock.writeLock().lock();
		AttenteList.add(obj);
		AttenteListLock.writeLock().unlock();
	}

	static void notifyListAttente() {
		AttenteListLock.writeLock().lock();
		for (Object obj : AttenteList) {
			synchronized (obj) {
				obj.notify();
			}
			AttenteList.remove(obj);
		}
		AttenteListLock.writeLock().unlock();
	}
}

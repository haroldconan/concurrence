package concurrence;

import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Main {

	private static volatile HashSet<String> txtVue = new HashSet<>();
	private static volatile HashSet<Object> AttenteList = new HashSet<>();
	private static volatile HashSet<String> resteFaire = new HashSet<>();
	private static ReentrantReadWriteLock txtVueLock = new ReentrantReadWriteLock();
	private static ReentrantReadWriteLock resteFaireLock = new ReentrantReadWriteLock();
	private static ReentrantReadWriteLock AttenteListLock = new ReentrantReadWriteLock();
	
	
	static volatile AtomicInteger count = new AtomicInteger(0);
	static AtomicInteger workers = new AtomicInteger(0);
	static String search;
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub http://localhost:8080/td.web/page1.jsp
		if(args.length>=1){
			search=args[0];
			resteFaire.add(args[1]);
		      
		   }
		ExecutorService executorService = Executors.newCachedThreadPool();
		for (int i = 0; i < 2; i++)
			executorService.execute(new Metier());
		executorService.shutdown();
		try {
			executorService.awaitTermination(1, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("\n======================================================================================\n");
		System.out.println("\t\ttexte trouvé : " + txtVue);
		System.out.println("\t\tnombre de fois : " + count);
		System.out.println("\n======================================================================================\n");

	}
	private static boolean trouvee(String url) {
		txtVueLock.readLock().lock();
		boolean result = txtVue.contains(url);
		txtVueLock.readLock().unlock();
		return result;
	}

	private static void ajouterTxtTrouvee(String url) {
		txtVueLock.writeLock().lock();
		txtVue.add(url);
		txtVueLock.writeLock().unlock();
	}

	static String getResteFaire() {
		resteFaireLock.writeLock().lock();
		String result = null;
		if (encoreFaire()) {
			result = resteFaire.iterator().next();
			resteFaire.remove(result);
			ajouterTxtTrouvee(result);
		}
		resteFaireLock.writeLock().unlock();
		return result;
	}

	static void ajouterResteFaire(String url) {
		resteFaireLock.writeLock().lock();
		if (!trouvee(url))
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

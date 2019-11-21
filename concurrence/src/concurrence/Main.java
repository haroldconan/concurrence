package concurrence;

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
	static String search;
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub http://localhost:8080/td.web/page1.jsp
	
			search="nantes";
			resteFaire.add("https://fr.wikipedia.org/wiki/Nantes");
		ExecutorService executorService = Executors.newCachedThreadPool();
		for (int i = 0; i < 2; i++)
			executorService.execute(new Metier(resteFaire));
		executorService.shutdown();
		try {
			executorService.awaitTermination(1, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("\n======================================================================================\n");
		System.out.println("\t\tle texte "+search+" a été trouvé "+count+" fois à l'adresse:\n\t\t" + txtVue);
		System.out.println("\n======================================================================================\n");

	}
	static boolean trouvee(String url) {
		txtVueLock.readLock().lock();
		boolean result = txtVue.contains(url);
		txtVueLock.readLock().unlock();
		return result;
	}

	static void ajouterTxtTrouvee(String url) {
		txtVueLock.writeLock().lock();
		txtVue.add(url);
		txtVueLock.writeLock().unlock();
	}
	

}

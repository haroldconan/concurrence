package concurrence;

import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Metier {

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
		ExecutorService es = Executors.newCachedThreadPool();
		for (int i = 0; i < 2; i++)
			es.execute(new ThreadsConcurrence());
		es.shutdown();
		try {
			es.awaitTermination(1, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("VISITED : " + txtVue);
		System.out.println("COUNT : " + count);

	}
	private static boolean isVisited(String url) {
		txtVueLock.readLock().lock();
		boolean result = txtVue.contains(url);
		txtVueLock.readLock().unlock();
		return result;
	}

	private static void addVisited(String url) {
		txtVueLock.writeLock().lock();
		txtVue.add(url);
		txtVueLock.writeLock().unlock();
	}

	static String getTodo() {
		resteFaireLock.writeLock().lock();
		String result = null;
		if (stillTodo()) {
			result = resteFaire.iterator().next();
			resteFaire.remove(result);
			addVisited(result);
		}
		resteFaireLock.writeLock().unlock();
		return result;
	}

	static void addTodo(String url) {
		resteFaireLock.writeLock().lock();
		if (!isVisited(url))
			resteFaire.add(url);
		resteFaireLock.writeLock().unlock();
	}

	static boolean stillTodo() {
		resteFaireLock.readLock().lock();
		boolean result = resteFaire.size() > 0;
		resteFaireLock.readLock().unlock();
		return result;
	}

	static void addWaitingList(Object object) {
		AttenteListLock.writeLock().lock();
		AttenteList.add(object);
		AttenteListLock.writeLock().unlock();
	}

	static void notifyWaitingList() {
		AttenteListLock.writeLock().lock();
		for (Object object : AttenteList) {
			synchronized (object) {
				object.notify();
			}
			AttenteList.remove(object);
		}
		AttenteListLock.writeLock().unlock();
	}

}

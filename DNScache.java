import java.util.ArrayList;
import org.xbill.DNS.*;

public class DNScache {
	
	public volatile ArrayList<Record> cache;
	private ArrayList<Long> sysTime;

	public DNScache() {
		ArrayList<Record> cache = new ArrayList<Record>();
		this.cache = cache;
		sysTime = new ArrayList<Long>();
	}

	public void addRecord(Record rec) {
		sysTime.add(System.currentTimeMillis());
		cache.add(rec);
//		System.out.println(rec);
	}
	
	public void deleteRecords(String name, int type) {
		cache.removeAll(findRecords(name, type));
	}
	public void deleteRecords(String name) {
		cache.removeAll(findRecords(name));
	}
	
	public void printRecords() {
		for (int i = 0; i < cache.size(); i++) {
			System.out.println((i + 1) + " " + cache.get(i));
		}
	}
	
	public ArrayList<Record> findRecords(String name, int type) {
		ArrayList<Record> result = new ArrayList<>();
		for (int i = 0; i < cache.size(); i++) {
//			System.out.println("Name: " + cache.get(i).getName() + ", type: " + cache.get(i).getType() + "; wanted: " + name + ", " + type + " - " + (cache.get(i).getName().toString().equals(name)));
			if (cache.get(i).getName().toString().equals(name) && cache.get(i).getType() == type)
				result.add(cache.get(i));
		}
		return result;
	}
	
	public ArrayList<Record> findRecords(String name) {
		ArrayList<Record> result = new ArrayList<>();
		for (int i = 0; i < cache.size(); i++) {
			if (cache.get(i).getName().toString().equals(name))
				result.add(cache.get(i));
		}
		return result;
	}
	
	public void clear() {
		cache.clear();
	}
	
	public void update() {

		for (int i = 0; i < cache.size(); i++) {
			if (System.currentTimeMillis() >= (sysTime.get(i) + cache.get(i).getTTL()*1000)) {
				System.out.println("Removing " + cache.get(i).getName().toString() + " with ttl = " + cache.get(i).getTTL());
				deleteRecords(cache.get(i).getName().toString(), cache.get(i).getType());
				break;
			}
		}
	}

}

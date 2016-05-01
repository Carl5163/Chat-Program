import java.io.*;
import java.util.concurrent.*;

@SuppressWarnings("serial")
public class UserMap extends ConcurrentHashMap<String, User> {
	
	private String fileName;
	
	public UserMap(String fileName) {		
		this.fileName = fileName;
	}
	
	@Override
	public User put(String key, User value) {
		return super.put(key.toUpperCase(), value);
	}
	
	@Override
	public User get(Object key) {
		return super.get(((String)key).toUpperCase());
	}
	
	
	public void write() {
		try {
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(fileName));
			dos.writeInt(values().size());
			for(User user : values()) {
				user.write(dos);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

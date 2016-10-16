package proxy;

public class Session {
	private final String ip;

	public Session(String ip) {
		this.ip = ip;
	}
	
	public String getIP(){
		return ip;
	}
	
	public boolean equals(Object other){
		if(other == null)
			return false;
		
		if(other instanceof Session){
			Session o = (Session) other;
			return o.getIP().equals(getIP());
		}
		else if(other instanceof String){
			String o = (String) other;
			return o.equals(getIP());
		}
		
		return false;
	}
}
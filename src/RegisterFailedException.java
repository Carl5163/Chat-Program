@SuppressWarnings("serial")
public class RegisterFailedException extends Exception {
	
	public static int INTERNAL_SPACES_USER = 0;
	public static int SHORT_USER = 1;
	public static int SHORT_PASS = 2;
	
	private boolean userBad;
	private boolean passBad;
	private String[] report;
	
	public RegisterFailedException(String message, boolean userBad, boolean passBad, String[] report) {
		super(message);
		this.userBad = userBad;
		this.passBad = passBad;
		this.report = report;
	}

	public boolean isUserBad() {
		return userBad;
	}
	public boolean isPassBad() {
		return passBad;
	}
	
	public String getReportOn(int reportErrorType) {
		return report[reportErrorType];
	}
}

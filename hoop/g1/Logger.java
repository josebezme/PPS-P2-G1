package hoop.g1;

public interface Logger {
	void log(String message);
	
	public static final Logger DEFAULT_LOGGER = new Logger() {
		@Override
		public void log(String message) {
			System.out.println(message);
		}
	};
}

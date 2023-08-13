import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Amith GC https://amithgc.com
 */
@Aspect
@Component
public class LoggingAspect {

	// Minimum time to track (in Seconds).
	// If the execution time is above this, API Call will be logged.
	private static final int MIN_EXECUTION_TIME_TO_TRACK = 5;

	// Your Application Name
	private static final String APPLICATION_NAME = "SAMPLE";

	// Your Database in Influx Database
	private String INFLUX_DATABASE = "monitoring";

	// Replace the IP & Port with your Influx DB IP & Port
	private String INFLUX_URL = "http://localhost:8080/write?db=" + INFLUX_DATABASE;

	/**
	 * Method to Send the Data to Influx DB
	 * @param className - ClassName being Tracked
	 * @param methodName - MethodName being Tracked
	 * @param timeInSec - Method Execution Time (Response Time)
	 */
	private void pushDataToInfluxDB(String className, String methodName, double timeInSec) {
		new Thread(() -> {
			String classMethod = className + "." + methodName;
			String data = "od-execution-time,host=" + APPLICATION_NAME + ",classMethod=" + classMethod + " value="
					+ timeInSec;

			try {
				URL obj = new URL(INFLUX_URL);
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();

				con.setRequestMethod("POST");
				con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
				con.setConnectTimeout(2000);

				// Send post request
				con.setDoOutput(true);
				DataOutputStream wr = new DataOutputStream(con.getOutputStream());
				wr.writeBytes(data);
				wr.flush();
				wr.close();

				// Making HTTP Call
				con.getResponseCode();
			}
			catch (Exception ignored) {
			}

		}).start();
	}

	// Please use the package which has all of your controllers
	@Around("execution(* com.amithgc.tcpflash.controller..*(..))")
	public Object profileAllMethods(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();

		// Get intercepted method details
		String className = methodSignature.getDeclaringType().getSimpleName();
		String methodName = methodSignature.getName();

		final StopWatch stopWatch = new StopWatch();

		// Measure method execution time
		stopWatch.start();
		Object result = proceedingJoinPoint.proceed();
		stopWatch.stop();

		if (stopWatch.getTotalTimeSeconds() > MIN_EXECUTION_TIME_TO_TRACK) {
			pushDataToInfluxDB(className, methodName, stopWatch.getTotalTimeSeconds());
		}
		return result;
	}

}
package mrpnsim.application.reachabilityAsp;
import java.io.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class RunPython {
	public static String run(String arg1, String arg2,String arg3,String arg4,String arg5,String arg6,String arg7) {
		String result = "";

		try {
			// create a ProcessBuilder for the Python program
			
			ProcessBuilder pb = new ProcessBuilder(arg1, arg2 , arg3, arg4,
					arg5,arg6, arg7);
		
			// set the PYTHONUNBUFFERED environment variable to prevent buffering
			Map<String, String> env = pb.environment();
			env.put("PYTHONUNBUFFERED", "1");

			// start the process
			Process process = pb.start();

			// get the input stream of the process
			InputStream inputStream = process.getInputStream();

			// create a BufferedReader to read the output of the process
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

			// read the output of the process
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
				result += line;
			}
			// wait for the process to complete
			process.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

		return result;
	}
	public static void main(String args[]) throws IOException {
//		String goalAsString = "goal :- holds(p1,a,10).\r\n"
//				+ "\r\n"
//				+ ":- goal.";
//		String timeToRun = "10";
//		 try {
//	            FileWriter writer = new FileWriter("MRPN_Scenario.lp", true);
//	            writer.write("time(0.." + timeToRun + ").\n");
//	            writer.close();
//	        } catch (IOException e) {
//	            e.printStackTrace();
//	        }
//	        
//	        System.out.println("DEBUG");
//	        
//	        try {
//	            FileWriter writer = new FileWriter("goal.lp");
//	            String goal = goalAsString.replace(").\n", ",\n");
//	            goal = "goal:-" + goal;
//	            //goal = goal.substring(0, goal.length()-3) + "." + goal.substring(goal.length()+1);
//	            goal = goal + "\n:- not goal.";
//	            writer.write(goal);
//	            writer.close();
//	        } catch (IOException e) {
//	            e.printStackTrace();
//	        }
//		
		

//        FileWriter writer = new FileWriter("goal.lp");
//        writer.write(goalAsString+"\n");
//        writer.close();
//		//System.out.println(goalAsString);
//		System.out.println(run("python", "resources/test_work.py" , "clingo.exe", "MRPN_Scenario.lp",
//		        "aspCode.lp","goal.lp", "15"));
	}

}

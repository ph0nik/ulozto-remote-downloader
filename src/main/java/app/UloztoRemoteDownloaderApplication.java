package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UloztoRemoteDownloaderApplication {
	/*
	* 1. read url address
	* 2. get the id of the file
	* 3. send get request for given address
	* 4. parse received html
	* 5. get all the post parameters and picture url
	* 6. show user picture and prompt for captcha
	* 7. send post request with given parameters
	* 8. download file, showing progress
	*
	* */

	// TODO add server and websocket ports as program parameters
	/*
	* Check if argument is provided
	* */
	public static void main(String[] args) {
		if (args.length > 0) {
			SpringApplication.run(UloztoRemoteDownloaderApplication.class, args);
		} else {
			System.out.println("No folder path provided, run with argument '--dpathr=<path>'");
		}
	}


}

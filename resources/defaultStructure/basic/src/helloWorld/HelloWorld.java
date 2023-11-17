package helloWorld;

import org.testng.Assert;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * this class demonstrate the simple example of a project code.
 * this code is used too by some builder tests.
*/
public class HelloWorld{
	/**
	 * a runnable main method.
	*/
	public static void main(String ... args) throws URISyntaxException, IOException {
		System.out.println("hello world");
		// for tests of build tool
		Assert.assertTrue(true);
		// for tests of build tool
		helloWorld.HelloWorld.printFromResource();
	}
	
	/**
	 * an example of a method.
	*/
	public static int calculate(int x, int y){
		return x + y;
	}
	
	/**
	* this function will be used during testing to test the 'resources' directory.
	 * this should to read the 'resources/testResourcesFile.txt' and print their content.
	*/
	public static void printFromResource() throws URISyntaxException, IOException {
		InputStream input = ClassLoader.getSystemClassLoader().getResourceAsStream("testResourcesFile.txt");

		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(input.readAllBytes());

		String readed = new String(byteArrayInputStream.readAllBytes());

		System.out.println(readed);
	}
}

package helloWorld;

import org.testng.Assert;

/**
 * this class demonstrate the simple example of a project code.
 * this code is used too by some builder tests.
*/
public class HelloWorld{
	/**
	 * a runnable main method.
	*/
	public static void main(String ... args){
		System.out.println("hello world");
		// for tests of build tool
		Assert.assertTrue(true);
	}
	
	/**
	 * an example of a method.
	*/
	public static int calculate(int x, int y){
		return x + y;
	}
}
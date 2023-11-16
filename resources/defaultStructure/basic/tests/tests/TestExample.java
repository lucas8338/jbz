package tests;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestExample{
	@Test
	public void testExample(){
		Assert.assertTrue(2 > 1);
		Assert.assertEquals(10 + 10, 20);
		Assert.assertEquals(helloWorld.HelloWorld.calculate(10, 20), 30);
	}
}

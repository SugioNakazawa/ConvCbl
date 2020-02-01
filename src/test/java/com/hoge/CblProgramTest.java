package com.hoge;

import org.junit.Assert;
import org.junit.Test;

public class CblProgramTest {

	@Test
	public void testGetFileName() {

		String fileName = "src/test/resources/com/hoge/convcbl/sample01.cbl";
		CblProgram pgm = new CblProgram(fileName);

		Assert.assertEquals(fileName, pgm.getFileName());
	}

}

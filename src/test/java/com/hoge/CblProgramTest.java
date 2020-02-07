package com.hoge;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

public class CblProgramTest {

	@Test
	public void testGetFileName() throws IOException {

		Path fileName = Paths.get("src/test/resources/com/hoge/convcbl/sample01");
		CblProgram pgm = new CblProgram(fileName);

		Assert.assertEquals(0, pgm.dataDiv.recList.size());
	}

}

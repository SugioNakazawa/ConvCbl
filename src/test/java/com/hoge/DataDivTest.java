package com.hoge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataDivTest {
	static Logger logger = LoggerFactory.getLogger(DataDivTest.class.getName());
	static private final String PATH = "src/test/resources/com/hoge";

	private CblProgram program;

	@Test
	public void testCopy() throws IOException {
		program = new CblProgram(PATH + "/convcbl/sample01.cbl");
		program.read();
		program.analyze();
		List<String[]> retList = program.dataDiv.getRecList();
//		program.dataDiv.logoutContent();

		Assert.assertEquals(56, retList.size());

		Assert.assertEquals("I1-AA0001", retList.get(4)[1]);
		Assert.assertEquals("I1-AA0005", retList.get(8)[1]);
		Assert.assertEquals("O1-BB0001", retList.get(11)[1]);
		Assert.assertEquals("O2-BB0001", retList.get(18)[1]);
		Assert.assertEquals("O3-BB0001", retList.get(25)[1]);
		Assert.assertEquals("CODE", retList.get(42)[1]);
		Assert.assertEquals("INI-O1-BB0001", retList.get(45)[1]);
		Assert.assertEquals("W1-BB0001", retList.get(51)[1]);

		Assert.assertEquals("I1-REC", program.dataDiv.getFdList().get(0).recName);
		Assert.assertEquals("I1-AA0001", program.dataDiv.getFdList().get(0).fdColList.get(0).colName);
		Assert.assertEquals("X(1)", program.dataDiv.getFdList().get(0).fdColList.get(0).colType);

		Assert.assertEquals("WRK-AREA", program.dataDiv.getWsList().get(0).recName);
		Assert.assertEquals("CAN-PGMID", program.dataDiv.getWsList().get(0).fdColList.get(0).colName);
		Assert.assertEquals("X(08)", program.dataDiv.getWsList().get(0).fdColList.get(0).colType);

		Assert.assertEquals("W1-REC", program.dataDiv.getWsList().get(2).recName);
		Assert.assertEquals("W1-BB0005", program.dataDiv.getWsList().get(2).fdColList.get(4).colName);
		Assert.assertEquals("999", program.dataDiv.getWsList().get(2).fdColList.get(4).colType);
	}

	@Test
	public void testCreateDmdl() throws IOException {
		program = new CblProgram(PATH + "/convcbl/sample01.cbl");
		program.read();
		program.analyze();
		program.dataDiv.createDmdl("out");

		String fileA = PATH + "/datadiv/exp_sample01.dmdl";
		String fileB = "out/" + program.dataDiv.getFileName() + ".dmdl";
		Assert.assertTrue(Arrays.equals(Files.readAllBytes(Paths.get(fileA)), Files.readAllBytes(Paths.get(fileB))));
	}

	@Test
	public void testCopyError() throws IOException {
		try {
			program = new CblProgram(PATH + "/convcbl/error01.cbl");
			program.read();
			program.analyze();
		} catch (Exception e) {
			Assert.assertEquals(DataDiv.MSG_NO_SUPPORT, e.getMessage());
		}
	}
}

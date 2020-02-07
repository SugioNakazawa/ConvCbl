package com.hoge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Arrays;

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
		program = new CblProgram(Paths.get(PATH + "/convcbl/sample01.cbl"));
		program.read();
		program.analyze();
		DataDiv dataDiv = program.dataDiv;
		dataDiv.logoutContent();

		Assert.assertEquals(63, dataDiv.recList.size());

		Assert.assertEquals("I1-AA0001", dataDiv.getRec(4)[1]);
		Assert.assertEquals("I1-AA0005", dataDiv.getRec(8)[1]);
		Assert.assertEquals("O1-BB0001", dataDiv.getRec(11)[1]);
		Assert.assertEquals("O2-BB0001", dataDiv.getRec(18)[1]);
		Assert.assertEquals("O3-BB0001", dataDiv.getRec(25)[1]);
		Assert.assertEquals("CODE", dataDiv.getRec(42)[1]);
		Assert.assertEquals("INI-O1-BB0001", dataDiv.getRec(45)[1]);
		Assert.assertEquals("W1-BB0001", dataDiv.getRec(51)[1]);

		Assert.assertEquals("I1-REC", program.dataDiv.getFdList().get(0).recName);
		Assert.assertEquals("I1-AA0001", program.dataDiv.getFdList().get(0).fdColList.get(0).colName);
		Assert.assertEquals("X(1)", program.dataDiv.getFdList().get(0).fdColList.get(0).colType);

		Assert.assertEquals("WRK-AREA", program.dataDiv.getWsList().get(0).recName);
		Assert.assertEquals("CAN-PGMID", program.dataDiv.getWsList().get(0).fdColList.get(0).colName);
		Assert.assertEquals("X(08)", program.dataDiv.getWsList().get(0).fdColList.get(0).colType);

		Assert.assertEquals("W1-REC", program.dataDiv.getWsList().get(2).recName);
		Assert.assertEquals("W1-BB0005", program.dataDiv.getWsList().get(2).fdColList.get(4).colName);
		Assert.assertEquals("999", program.dataDiv.getWsList().get(2).fdColList.get(4).colType);

		Assert.assertEquals("P1-REC", program.dataDiv.getLkList().get(0).recName);
		Assert.assertEquals("P1-BB0005", program.dataDiv.getLkList().get(0).fdColList.get(4).colName);
		Assert.assertEquals("999", program.dataDiv.getLkList().get(0).fdColList.get(4).colType);
	}

	@Test
	public void testCreateDmdl() throws IOException {
		program = new CblProgram(Paths.get(PATH + "/datadiv/sample01.cbl"));
		program.read();
		program.analyze();
		program.dataDiv.createDmdl(Paths.get("out/sample01.dot"));

		String fileA = PATH + "/datadiv/exp_sample01.dmdl";
		String fileB = "out/sample01.dmdl";
		Assert.assertTrue(Arrays.equals(Files.readAllBytes(Paths.get(fileA)), Files.readAllBytes(Paths.get(fileB))));
	}

	@Test
	public void testCopyError1() throws IOException {
		try {
			program = new CblProgram(Paths.get(PATH + "/datadiv/error01.cbl"));
			program.read();
			program.analyze();
			Assert.fail();
		} catch (Exception e) {
			Assert.assertEquals(Const.MSG_NO_SUPPORT, e.getMessage());
		}
	}

	@Test
	public void testCopyError2() throws IOException {
		Path path = Paths.get(PATH + "/datadiv/error02.cbl");
		try {
			program = new CblProgram(path);
			program.read();
			program.analyze();
			Assert.fail();
		} catch (Exception e) {
			Assert.assertEquals(
					MessageFormat.format(Const.MSG_NOT_FOUND_COPY, path.getParent().toAbsolutePath() + "/ZZZ001"),
					e.getMessage());
		}
	}
}

/**
 * 
 */
package com.hoge;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nakazawasugio
 *
 */
public class ConvCblTest {
	static Logger logger = LoggerFactory.getLogger(ConvCblTest.class.getName());
	static String PATH = "src/test/resources/com/hoge/convcbl";

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link com.hoge.ConvCbl#main(java.lang.String[])}.
	 */
	@Test
	public void testMain01() {
		String[] args = { "-i", "src/test/resources/com/hoge/convcbl/sample01.cbl", "-o", "out" };
		try {
			ConvCbl.main(args);
			Assert.assertEquals(84, Files.readAllLines(Paths.get("out/sample01.dmdl")).size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testMainOutDmdlDot() throws IOException {
		String output = this.tempFolder.getRoot().getAbsolutePath();
		{
			String[] args = { "-i", "src/test/resources/com/hoge/convcbl/sample01.cbl", //
					"-o", output, "-t", "dmdl", "dot" };
			ConvCbl.main(args);
			// ls
			Files.list(Paths.get(output)).forEach(System.out::println);
			// check exist
			Assert.assertEquals(84, Files.readAllLines(Paths.get(output + "/sample01.dmdl")).size());
			Assert.assertEquals(447, Files.readAllLines(Paths.get(output + "/sample01.dot")).size());
			Assert.assertEquals(19745, Files.size(Paths.get(output + "/sample01.dot")));
		}
	}

	@Test
	public void testMainNomerge() throws IOException {
		String output = this.tempFolder.getRoot().getAbsolutePath();
		{
			String[] args = { "-i", "src/test/resources/com/hoge/convcbl/sample01.cbl", //
					"-o", output, "-t", "dmdl", "dot", "--nomerge" };
			ConvCbl.main(args);
			// ls
			Files.list(Paths.get(output)).forEach(System.out::println);
			// check exist
			Assert.assertEquals(84, Files.readAllLines(Paths.get(output + "/sample01.dmdl")).size());
			Assert.assertEquals(447, Files.readAllLines(Paths.get(output + "/sample01.dot")).size());
			Assert.assertEquals(20238, Files.size(Paths.get(output + "/sample01.dot")));
		}
	}

	@Test
	public void testMainNoreturn() throws IOException {
		String output = this.tempFolder.getRoot().getAbsolutePath();
		{
			String[] args = { "-i", "src/test/resources/com/hoge/convcbl/sample01.cbl", //
					"-o", output, "-t", "dmdl", "dot", "--noreturn" };
			ConvCbl.main(args);
			// ls
			Files.list(Paths.get(output)).forEach(System.out::println);
			// check exist
			Assert.assertEquals(84, Files.readAllLines(Paths.get(output + "/sample01.dmdl")).size());
			Assert.assertEquals(435, Files.readAllLines(Paths.get(output + "/sample01.dot")).size());
			Assert.assertEquals(19613, Files.size(Paths.get(output + "/sample01.dot")));
		}
	}

	@Test
	public void testMainNomergeNoreturn() throws IOException {
		String output = this.tempFolder.getRoot().getAbsolutePath();
		{
			String[] args = { "-i", "src/test/resources/com/hoge/convcbl/sample01.cbl", //
					"-o", output, "-t", "dmdl", "dot", "--nomerge", "--noreturn" };
			ConvCbl.main(args);
			// ls
			Files.list(Paths.get(output)).forEach(System.out::println);
			// check exist
			Assert.assertEquals(84, Files.readAllLines(Paths.get(output + "/sample01.dmdl")).size());
			Assert.assertEquals(435, Files.readAllLines(Paths.get(output + "/sample01.dot")).size());
			Assert.assertEquals(20092, Files.size(Paths.get(output + "/sample01.dot")));
		}
	}

	@Test
	public void testMainOutDot() throws IOException {
		String output = this.tempFolder.getRoot().getAbsolutePath();
		{
			String[] args = { "-i", "src/test/resources/com/hoge/convcbl/sample01.cbl", //
					"-o", output, "-t", "dot" };
			ConvCbl.main(args);
			// check exist
			Assert.assertFalse(Files.exists(Paths.get(output + "/sample01.dmdl")));
			Assert.assertTrue(Files.exists(Paths.get(output + "/sample01.dot")));
		}
	}

	@Test
	public void testMainNoFileParam() {
		String[] args = {};
		try {
			ConvCbl.main(args);
			fail();
		} catch (IllegalArgumentException e) {
			Assert.assertEquals(Const.MSG_NO_FILE_PARAM, e.getMessage());
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testMainNoFile() {
		String[] args = { "-i", "nofile.cbl" };
		try {
			ConvCbl.main(args);
			fail();
		} catch (IllegalArgumentException iae) {
			Assert.assertEquals(MessageFormat.format(Const.MSG_NO_FILE, "nofile.cbl"), iae.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testMainNoDir() throws IOException {
		{
			String[] args = { "-i", "src/test/resources/com/hoge/convcbl/sample01.cbl" };
			ConvCbl.main(args);
			// check exist
			Assert.assertTrue(Files.exists(Paths.get("out/sample01.dmdl")));
			Assert.assertTrue(Files.exists(Paths.get("out/sample01.dot")));
		}
	}

	@Test
	public void testMainIllegalDir() {
		{
			String[] args = { "-i", "src/test/resources/com/hoge/convcbl/sample01.cbl", "-o", "aaa" };
			try {
				ConvCbl.main(args);
			} catch (IllegalArgumentException e) {
				Assert.assertEquals(MessageFormat.format(Const.MSG_NO_DIR, "aaa"), e.getMessage());
			} catch (IOException e) {
				Assert.fail();
				e.printStackTrace();
			}
		}
	}

	@Test
	public void testMainIllegalParam() {
		String[] args = { "-p", "unknown" };
		try {
			ConvCbl.main(args);
			fail();
		} catch (IllegalArgumentException iae) {
			Assert.assertEquals("Unrecognized option: -p", iae.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testExec00() throws IOException {
		ProcedureDiv.LONG_LABEL = true;
		String programId = "sample01";
		Path fileName = Paths.get(PATH + "/" + programId + ".cbl");
		ConvCbl target = new ConvCbl(fileName);
		try {
			target.setOutDir(Paths.get("out"));
			target.exec();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		target.getProgram().procDiv.setForkedMerge(true);
		target.getProgram().procDiv.outputDataDot(null);
	}

	@Test
	public void testExec01() throws IOException {
		ProcedureDiv.LONG_LABEL = true;
		String programId = "sample01";
		Path fileName = Paths.get(PATH + "/" + programId + ".cbl");
		ConvCbl target = new ConvCbl(fileName);
		try {
			target.setOutDir(Paths.get("out"));
			target.exec();

			Assert.assertEquals("SAMPLE01", target.getProgram().idDiv.getProgramId());
			Assert.assertEquals(2, target.getProgram().idDiv.recList.size());
			Assert.assertEquals(7, target.getProgram().envDiv.recList.size());
			Assert.assertEquals(63, target.getProgram().dataDiv.recList.size());
			Assert.assertEquals(61, target.getProgram().procDiv.recList.size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		outputLog(programId, target);
		target.getProgram().dataDiv.logoutContent();
		target.getProgram().procDiv.outputDataDot(Paths.get("out/" + programId + ".dot"));
		{
			// DATA CHEK
			String expFile = PATH + "/exp_" + programId + ".dmdl";
			String actFile = target.getOutDir() + "/" + programId + ".dmdl";
			Assert.assertTrue(
					Arrays.equals(Files.readAllBytes(Paths.get(expFile)), Files.readAllBytes(Paths.get(actFile))));
		}
		{
			// PROCEDURE CHECK
			target.getProgram().procDiv.logoutCmdTree("out/" + programId + "_tree.txt");
			String expFile = PATH + "/exp_" + programId + "_tree.txt";
			String actFile = "out/" + programId + "_tree.txt";
			Assert.assertTrue(
					Arrays.equals(Files.readAllBytes(Paths.get(expFile)), Files.readAllBytes(Paths.get(actFile))));
		}
	}

	@Test
	public void testExec02() throws IOException {
		String programId = "sample02";
		String PATH = "src/test/resources/com/hoge/convcbl";
		Path fileName = Paths.get(PATH + "/" + programId + ".cbl");
		ConvCbl target = new ConvCbl(fileName);
		try {
			target.setOutDir(Paths.get("out"));
			target.exec();

			Assert.assertEquals(2, target.getProgram().idDiv.recList.size());
			Assert.assertEquals(6, target.getProgram().envDiv.recList.size());
			Assert.assertEquals(44, target.getProgram().dataDiv.recList.size());
			Assert.assertEquals(63, target.getProgram().procDiv.recList.size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		outputLog(programId, target);
		target.getProgram().procDiv.outputDataDot(Paths.get("out/" + programId + ".dot"));
		// DATA CHEK
		{
			String expFile = PATH + "/exp_" + programId + ".dmdl";
			String actFile = "out/" + programId + ".dmdl";
			Assert.assertTrue(
					Arrays.equals(Files.readAllBytes(Paths.get(expFile)), Files.readAllBytes(Paths.get(actFile))));
		}
		// PROCEDURE CHECK
		target.getProgram().procDiv.logoutCmdTree("out/" + programId + "_tree.txt");
		{
			String expFile = PATH + "/exp_" + programId + "_tree.txt";
			String actFile = "out/" + programId + "_tree.txt";
			Assert.assertTrue(
					Arrays.equals(Files.readAllBytes(Paths.get(expFile)), Files.readAllBytes(Paths.get(actFile))));
		}
	}

	@Test
	public void testExec03() throws IOException {
		String programId = "sample03";
		String PATH = "src/test/resources/com/hoge/convcbl";
		Path fileName = Paths.get(PATH + "/" + programId + ".cbl");
		ConvCbl target = new ConvCbl(fileName);
		try {
			target.setOutDir(Paths.get("out"));
			target.exec();

			Assert.assertEquals(2, target.getProgram().idDiv.recList.size());
			Assert.assertEquals(5, target.getProgram().envDiv.recList.size());
			Assert.assertEquals(65, target.getProgram().dataDiv.recList.size());
			Assert.assertEquals(76, target.getProgram().procDiv.recList.size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		outputLog(programId, target);
		target.getProgram().procDiv.outputDataDot(Paths.get("out/" + programId + ".dot"));
		// DATA CHEK
		{
			String expFile = PATH + "/exp_" + programId + ".dmdl";
			String actFile = "out/" + programId + ".dmdl";
			Assert.assertTrue(
					Arrays.equals(Files.readAllBytes(Paths.get(expFile)), Files.readAllBytes(Paths.get(actFile))));
		}
		// PROCEDURE CHECK
		target.getProgram().procDiv.logoutCmdTree("out/" + programId + "_tree.txt");
		{
			String expFile = PATH + "/exp_" + programId + "_tree.txt";
			String actFile = "out/" + programId + "_tree.txt";
			Assert.assertTrue(
					Arrays.equals(Files.readAllBytes(Paths.get(expFile)), Files.readAllBytes(Paths.get(actFile))));
		}
	}

	private void outputLog(String programId, ConvCbl target) throws IOException {
		// log
		logger.info("===========================================================");
		target.getProgram().procDiv.logoutRecList();
		logger.info("===========================================================");
		target.getProgram().procDiv.logoutSecTree();
		logger.info("===========================================================");
		target.getProgram().procDiv.logoutCmdTree(null);
		logger.info("===========================================================");
		target.getProgram().procDiv.outputDataDot(null);
	}

}
/**
 * 
 */
package com.hoge;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nakazawasugio
 *
 */
public class ConvCblTest {
	static Logger logger = LoggerFactory.getLogger(ConvCblTest.class.getName());

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
		String[] args = { "-i", "src/test/resources/com/hoge/convcbl/sample01.cbl" };
		try {
			ConvCbl.main(args);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testMain02() {
		String[] args = { "-i", "src/test/resources/com/hoge/convcbl/sample02.cbl" };
		try {
			ConvCbl.main(args);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testMain03() {
		String[] args = { "-i", "src/test/resources/com/hoge/convcbl/sample03.cbl" };
		try {
			ConvCbl.main(args);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
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
		} catch (NoSuchFileException nsfe) {
			Assert.assertEquals("nofile.cbl", nsfe.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testExec() throws IOException {
		String PATH = "src/test/resources/com/hoge/convcbl";
		String fileName = PATH + "/sample01.cbl";
		try {
			ConvCbl target = new ConvCbl();
			target.setOutDir("out");
			target.exec(fileName);

			Assert.assertEquals(2, target.getProgram().idDiv.recList.size());
			Assert.assertEquals(7, target.getProgram().envDiv.recList.size());
			Assert.assertEquals(55, target.getProgram().dataDiv.recList.size());
			Assert.assertEquals(59, target.getProgram().procDiv.recList.size());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		String fileA = PATH + "/exp_sample01.dmdl";
		String fileB = "out/sample01.dmdl";
		Assert.assertTrue(Arrays.equals(Files.readAllBytes(Paths.get(fileA)), Files.readAllBytes(Paths.get(fileB))));
	}

}
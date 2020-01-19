/**
 * 
 */
package com.hoge;

import static org.junit.Assert.fail;

import java.nio.file.NoSuchFileException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author nakazawasugio
 *
 */
public class ConvCblTest {

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
	public void testMain() {
		String[] args = { "-i", "src/test/resources/com/hoge/convcbl/sample01.cbl" };
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
			Assert.assertEquals(ConvCbl.MSG_NO_FILE_PARAM, e.getMessage());
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
		}catch(NoSuchFileException nsfe) {
			Assert.assertEquals("nofile.cbl",nsfe.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}

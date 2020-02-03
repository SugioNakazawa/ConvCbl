/**
 * 
 */
package com.hoge;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author nakazawasugio
 *
 */
public class CblSourceTest {
	static String PATH = "src/test/resources/com/hoge/convcbl";

	@Test
	public void test() throws IOException {
		CblSourceReader reader = new CblSourceReader(PATH + "/sample01.cbl");
		List<String[]> ret = reader.read();

		Assert.assertEquals(104, ret.size());
	}
}

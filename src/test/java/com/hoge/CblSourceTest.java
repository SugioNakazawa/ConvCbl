/**
 * 
 */
package com.hoge;

import java.io.IOException;

import org.junit.Test;

/**
 * @author nakazawasugio
 *
 */
public class CblSourceTest {
	static String PATH = "src/test/resources/com/hoge/convcbl";

	@Test
	public void test() throws IOException {
		CblSource source = new CblSource();
		CblSource.read(PATH + "/sample01.cbl");
	}
}

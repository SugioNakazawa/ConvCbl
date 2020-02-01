package com.hoge;

import org.junit.Assert;
import org.junit.Test;

public class ComUtilTest {

	@Test
	public void testNormal() {
		ComUtil com = new ComUtil();
		// for test
	}

	@Test
	public void testSearch() {
		String[] sentence = { "A", "B" };
		Assert.assertEquals(1, ComUtil.search(sentence, "B"));
		Assert.assertEquals(-1, ComUtil.search(sentence, "C"));
	}

}

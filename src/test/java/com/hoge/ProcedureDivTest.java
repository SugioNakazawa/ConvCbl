package com.hoge;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hoge.ProcedureDiv.IfCmd;

public class ProcedureDivTest {
	static Logger logger = LoggerFactory.getLogger(ProcedureDiv.class.getName());
	ProcedureDiv proc;

	@Before
	public void setUp() throws Exception {
		proc = new ProcedureDiv();
	}

	@Test
	public void testDevideIfSentence1() {
		String expCond = "A = B";
		String expThen = "MOVE A TO B";
		String expElse = "MOVE C TO D";

		{
			IfCmd ret = proc.devideIfSentence("IF A = B MOVE A TO B".split(" "));
			Assert.assertEquals(expCond + " TRUE", ret.thenCond);
			Assert.assertEquals(expThen, String.join(" ", ret.thenSentence.get(0)));
			Assert.assertEquals(null, ret.elseCond);
		}
//		{
//			List<String[]> ret = proc.devideIfSentence("IF A = B MOVE A TO B END-IF".split(" "));
//			Assert.assertEquals(expCond, String.join(" ", ret.get(0)));
//			Assert.assertEquals(expThen, String.join(" ", ret.get(1)));
//			Assert.assertEquals(2, ret.size());
//		}
//		{
//			List<String[]> ret = proc.devideIfSentence("IF A = B THEN MOVE A TO B".split(" "));
//			Assert.assertEquals(expCond, String.join(" ", ret.get(0)));
//			Assert.assertEquals(expThen, String.join(" ", ret.get(1)));
//			Assert.assertEquals(2, ret.size());
//		}
//		{
//			List<String[]> ret = proc.devideIfSentence("IF A = B THEN MOVE A TO B END-IF".split(" "));
//			Assert.assertEquals(expCond, String.join(" ", ret.get(0)));
//			Assert.assertEquals(expThen, String.join(" ", ret.get(1)));
//			Assert.assertEquals(2, ret.size());
//		}
//		{
//			List<String[]> ret = proc.devideIfSentence("IF A = B THEN MOVE A TO B ELSE MOVE C TO D".split(" "));
//			Assert.assertEquals(expCond, String.join(" ", ret.get(0)));
//			Assert.assertEquals(expThen, String.join(" ", ret.get(1)));
//			Assert.assertEquals(expElse, String.join(" ", ret.get(2)));
//		}
//		{
//			List<String[]> ret = proc.devideIfSentence("IF A = B THEN MOVE A TO B ELSE MOVE C TO D END-IF".split(" "));
//			Assert.assertEquals(expCond, String.join(" ", ret.get(0)));
//			Assert.assertEquals(expThen, String.join(" ", ret.get(1)));
//			Assert.assertEquals(expElse, String.join(" ", ret.get(2)));
//		}
	}

}

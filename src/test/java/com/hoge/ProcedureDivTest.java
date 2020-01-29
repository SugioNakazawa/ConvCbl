package com.hoge;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hoge.ProcedureDiv.BranchCmd;

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
			BranchCmd ret = proc.expandIf("IF A = B MOVE A TO B".split(" "));
			Assert.assertEquals(expCond + " TRUE", ret.branchList.get(0).cond);
			Assert.assertEquals(expThen, String.join(" ", ret.branchList.get(0).sentence));
			Assert.assertEquals(expCond + " FALSE", ret.branchList.get(1).cond);
			Assert.assertEquals("continue", String.join(" ", ret.branchList.get(1).sentence));
		}
		{
			BranchCmd ret = proc.expandIf("IF A = B MOVE A TO B END-IF".split(" "));
			Assert.assertEquals(expCond + " TRUE", ret.branchList.get(0).cond);
			Assert.assertEquals(expThen, String.join(" ", ret.branchList.get(0).sentence));
			Assert.assertEquals(expCond + " FALSE", ret.branchList.get(1).cond);
			Assert.assertEquals("continue", String.join(" ", ret.branchList.get(1).sentence));
		}
		{
			BranchCmd ret = proc.expandIf("IF A = B THEN MOVE A TO B".split(" "));
			Assert.assertEquals(expCond + " TRUE", ret.branchList.get(0).cond);
			Assert.assertEquals(expThen, String.join(" ", ret.branchList.get(0).sentence));
			Assert.assertEquals(expCond + " FALSE", ret.branchList.get(1).cond);
			Assert.assertEquals("continue", String.join(" ", ret.branchList.get(1).sentence));
		}
		{
			BranchCmd ret = proc.expandIf("IF A = B THEN MOVE A TO B END-IF".split(" "));
			Assert.assertEquals(expCond + " TRUE", ret.branchList.get(0).cond);
			Assert.assertEquals(expThen, String.join(" ", ret.branchList.get(0).sentence));
			Assert.assertEquals(expCond + " FALSE", ret.branchList.get(1).cond);
			Assert.assertEquals("continue", String.join(" ", ret.branchList.get(1).sentence));
		}
		{
			BranchCmd ret = proc.expandIf("IF A = B THEN MOVE A TO B ELSE MOVE C TO D".split(" "));
			Assert.assertEquals(expCond + " TRUE", ret.branchList.get(0).cond);
			Assert.assertEquals(expThen, String.join(" ", ret.branchList.get(0).sentence));
			Assert.assertEquals(expCond + " FALSE", ret.branchList.get(1).cond);
			Assert.assertEquals(expElse, String.join(" ", ret.branchList.get(1).sentence));
		}
		{
			BranchCmd ret = proc.expandIf("IF A = B THEN MOVE A TO B ELSE MOVE C TO D END-IF".split(" "));
			Assert.assertEquals(expCond + " TRUE", ret.branchList.get(0).cond);
			Assert.assertEquals(expThen, String.join(" ", ret.branchList.get(0).sentence));
			Assert.assertEquals(expCond + " FALSE", ret.branchList.get(1).cond);
			Assert.assertEquals(expElse, String.join(" ", ret.branchList.get(1).sentence));
		}
	}

}

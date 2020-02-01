package com.hoge;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hoge.ProcedureDiv.BranchCmd;
import com.hoge.ProcedureDiv.ExecCmd;

public class ProcedureDivTest {
	static Logger logger = LoggerFactory.getLogger(ProcedureDiv.class.getName());
	ProcedureDiv proc;

	@Before
	public void setUp() throws Exception {
		proc = new ProcedureDiv();
	}

	@Test
	public void testExpandIf() {
		String expCond = "A = B";
		String expThen = "MOVE A TO B";
		String expElse = "MOVE C TO D";

		{
			// 不正な呼び出し
			try {
				proc.expandIf("A = B".split(" "));
				Assert.fail();
			} catch (Exception e) {
				Assert.assertEquals("internal error", e.getMessage());
			}
		}
		{
			// 不正な呼び出し
			try {
				proc.expandIf("IF A = B".split(" "));
				Assert.fail();
			} catch (Exception e) {
				Assert.assertEquals("IF文内に実行命令がありません。", e.getMessage());
			}
		}
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

	@Test
	public void testDevideReadSentence() {
		String expCond1 = "A-REC AT END";
		String expCond2 = "A-REC NOT AT END";
		String expExec1 = "MOVE HIGH-VALUE TO KEY-1";
		String expExec2 = "MOVE A TO B";
		{
			BranchCmd ret = proc.expandRead("READ A-REC AT END MOVE HIGH-VALUE TO KEY-1".split(" "));
			Assert.assertEquals(expCond1, ret.branchList.get(0).cond);
			Assert.assertEquals(expExec1, String.join(" ", ret.branchList.get(0).sentence));
			Assert.assertEquals(expCond2, ret.branchList.get(1).cond);
			Assert.assertEquals("continue", String.join(" ", ret.branchList.get(1).sentence));
		}
		{
			BranchCmd ret = proc
					.expandRead("READ A-REC AT END MOVE HIGH-VALUE TO KEY-1 MOVE A TO B END-READ".split(" "));
			Assert.assertEquals(expCond1, ret.branchList.get(0).cond);
			Assert.assertEquals("MOVE HIGH-VALUE TO KEY-1 MOVE A TO B",
					String.join(" ", ret.branchList.get(0).sentence));
			Assert.assertEquals(expCond2, ret.branchList.get(1).cond);
			Assert.assertEquals("continue", String.join(" ", ret.branchList.get(1).sentence));
		}
		{
			BranchCmd ret = proc
					.expandRead("READ A-REC AT END MOVE HIGH-VALUE TO KEY-1 NOT AT END MOVE A TO B".split(" "));
			Assert.assertEquals(expCond1, ret.branchList.get(0).cond);
			Assert.assertEquals(expExec1, String.join(" ", ret.branchList.get(0).sentence));
			Assert.assertEquals(expCond2, ret.branchList.get(1).cond);
			Assert.assertEquals(expExec2, String.join(" ", ret.branchList.get(1).sentence));
		}
		{
			BranchCmd ret = proc.expandRead(
					"READ A-REC AT END MOVE HIGH-VALUE TO KEY-1 NOT AT END MOVE A TO B PERFORM A-SECTION END-READ"
							.split(" "));
			Assert.assertEquals(expCond1, ret.branchList.get(0).cond);
			Assert.assertEquals(expExec1, String.join(" ", ret.branchList.get(0).sentence));
			Assert.assertEquals(expCond2, ret.branchList.get(1).cond);
			Assert.assertEquals(expExec2 + " PERFORM A-SECTION", String.join(" ", ret.branchList.get(1).sentence));
		}
		{
			// END条件なしREAD
			BranchCmd ret = proc.expandRead("READ A-REC".split(" "));
			Assert.assertEquals(0, ret.branchList.size());
		}
		{
			// 命令なし（ありえない）
			BranchCmd branch = proc.expandRead("A-REC".split(" "));
			Assert.assertEquals(0, branch.branchList.size());
		}
		{
			// THEN なし
			BranchCmd ret = proc.expandRead("READ A-REC MOVE HIGH-VALUE TO KEY-1".split(" "));
			Assert.assertEquals(0, ret.branchList.size());
		}
	}

	@Test
	public void testDevideEvaluateSentence() {
		{
			BranchCmd ret = proc.expandEvaluate(
					"EVALUATE A = B WHEN TRUE MOVE A TO B WHEN FALSE MOVE C TO D END-EVALUATE".split(" "));
			Assert.assertEquals("A = B [TRUE]", ret.branchList.get(0).cond);
			Assert.assertEquals("MOVE A TO B", String.join(" ", ret.branchList.get(0).sentence));
			Assert.assertEquals("A = B [FALSE]", ret.branchList.get(1).cond);
			Assert.assertEquals("MOVE C TO D", String.join(" ", ret.branchList.get(1).sentence));
		}
		{
			BranchCmd ret = proc.expandEvaluate(
					"EVALUATE WORK-A WHEN 1 WHEN 2 MOVE A TO B WHEN OTHER MOVE C TO D PERFORM A-SEC END-EVALUATE"
							.split(" "));
			Assert.assertEquals("WORK-A [1, 2]", ret.branchList.get(0).cond);
			Assert.assertEquals("MOVE A TO B", String.join(" ", ret.branchList.get(0).sentence));
			Assert.assertEquals("WORK-A [OTHER]", ret.branchList.get(1).cond);
			Assert.assertEquals("MOVE C TO D PERFORM A-SEC", String.join(" ", ret.branchList.get(1).sentence));
		}
		{
			String org = "EVALUATE WORK-A WHEN 1 WHEN 2 MOVE A TO B WHEN OTHER MOVE C TO D PERFORM A-SEC END-EVALUATE";
			BranchCmd ret = proc.expandEvaluate(org.split(" "));
			Assert.assertEquals("WORK-A [1, 2]", ret.branchList.get(0).cond);
			Assert.assertEquals("MOVE A TO B", String.join(" ", ret.branchList.get(0).sentence));
			Assert.assertEquals("WORK-A [OTHER]", ret.branchList.get(1).cond);
			Assert.assertEquals("MOVE C TO D PERFORM A-SEC", String.join(" ", ret.branchList.get(1).sentence));
		}
		{
			String exec1 = "PERFORM 21-I1-ONLY PERFORM 11-READ";
			String exec2 = "PERFORM 22-I2-ONLY PERFORM 12-READ";
			String exec3 = "PERFORM UNTIL KEY-I1 NOT = KEY-I2 PERFORM 23-MATCH PERFORM 12-READ END-PERFORM PERFORM 11-READ";

			String org = "EVALUATE TRUE WHEN KEY-I1 < KEY-I2 " + exec1 + " WHEN KEY-I1 > KEY-I2 " + exec2
					+ " WHEN KEY-I1 = KEY-I2 " + exec3 + " END-EVALUATE";
			BranchCmd ret = proc.expandEvaluate(org.split(" "));
			Assert.assertEquals("TRUE [KEY-I1 < KEY-I2]", ret.branchList.get(0).cond);
			Assert.assertEquals(exec1, String.join(" ", ret.branchList.get(0).sentence));
			Assert.assertEquals("TRUE [KEY-I1 > KEY-I2]", ret.branchList.get(1).cond);
			Assert.assertEquals(exec2, String.join(" ", ret.branchList.get(1).sentence));
			Assert.assertEquals("TRUE [KEY-I1 = KEY-I2]", ret.branchList.get(2).cond);
			Assert.assertEquals(exec3, String.join(" ", ret.branchList.get(2).sentence));
		}
	}

	@Test
	public void testShrinkBranchSentence() {
		String org = "IF A < B THEN MOVE A = B";
		String[] sentence = org.split(" ");
		ExecCmd prev = null;
		ExecCmd cmd = proc.new ExecCmd(prev, sentence);
		proc.shrinkBranchSentence(cmd);
	}
}

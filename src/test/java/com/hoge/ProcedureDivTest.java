package com.hoge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hoge.ProcedureDiv.BranchCmd;
import com.hoge.ProcedureDiv.CmdType;
import com.hoge.ProcedureDiv.ExecCmd;

public class ProcedureDivTest {
	static Logger logger = LoggerFactory.getLogger(ProcedureDivTest.class.getName());
	static String PATH = "src/test/resources/com/hoge/procedurediv";
	ProcedureDiv proc;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

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

	/**
	 * isTreeStruct = true / false
	 * 
	 * @throws IOException
	 */
	@Test
	public void testAddDotNode() throws IOException {
		String[] sentence = { "MOVE", "A", "TO", "B" };
		ExecCmd cmd = proc.new ExecCmd(null, sentence);
		String actualHash = cmd.getSeq();

		ExecCmd nextCmd1 = proc.new ExecCmd(cmd, sentence);
		String actualNextHash1 = nextCmd1.getSeq();
		String cond1 = "condition1";

		ExecCmd nextCmd2 = proc.new ExecCmd(cmd, sentence);
//		String actualNextHash2 = nextCmd2.getSeq();
		String cond2 = "condition2";
		{
			// Next 0
			String actual = proc.addDotNode(cmd, 1, null);
			Assert.assertEquals(actualHash + "1", actual);
		}
		{
			// Next 1
			cmd.addNextCmd(nextCmd1, cond1);
			String actual = proc.addDotNode(cmd, 1, null);
			Assert.assertEquals(actualHash + "1 -> " + actualNextHash1 + "1", actual);
		}
		{
			// Next 2 isTreeStruct=false
			proc.setForkedMerge(true);
			cmd.addNextCmd(nextCmd1, cond1);
			cmd.addNextCmd(nextCmd2, cond2);
			String actual = proc.addDotNode(cmd, 1, new ArrayDeque<String>());
			Assert.assertEquals(actualHash + "1", actual);
			Assert.assertEquals(1, proc.getNestCounter());
		}
		{
			// Next 2 isTreeStruct=true
			proc.setForkedMerge(false);
			cmd.addNextCmd(nextCmd1, cond1);
			cmd.addNextCmd(nextCmd2, cond2);
			String actual = proc.addDotNode(cmd, 1, new ArrayDeque<String>());
			Assert.assertEquals(actualHash + "1", actual);
			Assert.assertEquals(6, proc.getNestCounter());
		}
	}

	private void prepareDotData() {
		// テストデータ作成
		proc.nodeSeq = 0;
		proc.rootCmd = proc.new ExecCmd(null, "PROCEDURE DIVISION".split(" "));
		ExecCmd cmd1 = proc.new ExecCmd(proc.rootCmd, "PERFORM UNTIL KEY < HIGH-VALUE".split(" "));
		ExecCmd cmd2 = proc.new ExecCmd(cmd1, "READ IN-FILE01".split(" "));
		ExecCmd cmd21 = proc.new ExecCmd(cmd2, "MOVE IN-A TO KEY".split(" "));
		ExecCmd cmd22 = proc.new ExecCmd(cmd2, "MOVE HIGH-VALUE TO KEY".split(" "));
		ExecCmd cmd3 = proc.new ExecCmd(cmd21, "WRITE OUT-FILE01".split(" "));
		ExecCmd cmd4 = proc.new ExecCmd(cmd3, "END-PERFORM".split(" "));
		ExecCmd cmd5 = proc.new ExecCmd(cmd4, "EXIT PROGRAM".split(" "));
		cmd1.setPair(cmd4);
		cmd4.setPair(cmd1);
		cmd4.addNextCmd(cmd5, "no");
		cmd3.addNextCmd(cmd4, "no");
		cmd22.addNextCmd(cmd3, "no");
		cmd21.addNextCmd(cmd3, "no");
		cmd2.addNextCmd(cmd22, "NOT AT END");
		cmd2.addNextCmd(cmd21, "AT END");
		cmd1.addNextCmd(cmd2, "no");
		proc.rootCmd.addNextCmd(cmd1, "no");
	}

	/**
	 * return connector true / false
	 * 
	 * @throws IOException
	 */
	@Test
	public void testReturnArrlow1() throws IOException {
		prepareDotData();
		{
			proc.setForkedMerge(false);
			proc.setReturnArrow(true);
			String expFile = PATH + "/exp_test1.dot";
			String actFile = tempFolder.getRoot().getAbsolutePath() + "/test.dot";
//			actFile = "out" + "/test.dot";
			// 実行
			proc.outputDataDot(Paths.get(actFile));
			// FILE CHECK
			Assert.assertTrue(
					Arrays.equals(Files.readAllBytes(Paths.get(expFile)), Files.readAllBytes(Paths.get(actFile))));
		}
	}

	@Test
	public void testReturnArrlow2() throws IOException {
		prepareDotData();
		{
			proc.setForkedMerge(false);
			proc.setReturnArrow(false);
			String expFile = PATH + "/exp_test2.dot";
			String actFile = tempFolder.getRoot().getAbsolutePath() + "/test.dot";
			// 実行
			proc.outputDataDot(Paths.get(actFile));
			// FILE CHECK
			Assert.assertTrue(
					Arrays.equals(Files.readAllBytes(Paths.get(expFile)), Files.readAllBytes(Paths.get(actFile))));
		}
	}

	@Test
	public void testReturnArrlow3() throws IOException {
		prepareDotData();
		{
			proc.setForkedMerge(true);
			proc.setReturnArrow(true);
			String expFile = PATH + "/exp_test3.dot";
			String actFile = tempFolder.getRoot().getAbsolutePath() + "/test.dot";
			// 実行
			proc.outputDataDot(Paths.get(actFile));
			// FILE CHECK
			Assert.assertTrue(
					Arrays.equals(Files.readAllBytes(Paths.get(expFile)), Files.readAllBytes(Paths.get(actFile))));
		}
	}

	@Test
	public void testReturnArrlow4() throws IOException {
		prepareDotData();
		{
			proc.setForkedMerge(true);
			proc.setReturnArrow(false);
			String expFile = PATH + "/exp_test4.dot";
			String actFile = tempFolder.getRoot().getAbsolutePath() + "/test.dot";
			// 実行
			proc.outputDataDot(Paths.get(actFile));
			// FILE CHECK
			Assert.assertTrue(
					Arrays.equals(Files.readAllBytes(Paths.get(expFile)), Files.readAllBytes(Paths.get(actFile))));
		}
	}

	@Test
	public void testDoEndPerform() {
		proc.nodeSeq = 0;
		ExecCmd cmd0 = proc.new ExecCmd(null, "PROCEDURE DIVISION".split(" "));
		ExecCmd cmd1 = proc.new ExecCmd(cmd0, "PERFORM UNTIL A < B".split(" "));
		ExecCmd cmd2 = proc.new ExecCmd(cmd1, "MOVE A TO B".split(" "));
		ExecCmd cmd3 = proc.new ExecCmd(cmd2, "END-PERFORM".split(" "));
		cmd0.addNextCmd(cmd1, "no");
		cmd1.addNextCmd(cmd2, "no");
		cmd2.addNextCmd(cmd3, "no");

		proc.doEndPerform(cmd3);

		Assert.assertEquals(cmd3, cmd1.getPair());
		Assert.assertEquals(cmd1, cmd3.getPair());
	}

	/**
	 * 対応するPERFORMがないEND-PERFORMのケース。
	 */
	@Test
	public void testDoEndPerformError() {
		proc.nodeSeq = 0;
		ExecCmd cmd0 = proc.new ExecCmd(null, "PROCEDURE DIVISION".split(" "));
		ExecCmd cmd1 = proc.new ExecCmd(proc.rootCmd, "END-PERFORM".split(" "));
		cmd0.addNextCmd(cmd1, "no");
		try {
			proc.doEndPerform(cmd1);
		} catch (Exception e) {
			Assert.assertEquals(Const.MSG_NOT_FOUND_PAIR_PERFORM_UNTIL, e.getMessage());
		}
	}

	@Test
	public void testNextValid() {
		proc.nodeSeq = 0;
		ExecCmd cmd0 = proc.new ExecCmd(null, "PROCEDURE DIVISION".split(" "));
		ExecCmd cmd1 = proc.new ExecCmd(cmd0, "PERFORM UNTIL A < B".split(" "));
		ExecCmd cmd2 = proc.new ExecCmd(cmd1, "SKIP SENTENCE".split(" "), CmdType.OTHER);
		ExecCmd cmd3 = proc.new ExecCmd(cmd2, "END-PERFORM".split(" "));
		ExecCmd cmd4 = proc.new ExecCmd(cmd3, "SKIP SENTENCE".split(" "), CmdType.OTHER);
		cmd0.addNextCmd(cmd1, "no");
		cmd1.addNextCmd(cmd2, "no");
		cmd2.addNextCmd(cmd3, "no");
		cmd3.addNextCmd(cmd4, "no");

		Assert.assertEquals(cmd3, proc.nextValid(cmd2));
		Assert.assertEquals(cmd4, proc.nextValid(cmd4));
	}
}

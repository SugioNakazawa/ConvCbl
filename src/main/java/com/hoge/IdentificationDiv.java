package com.hoge;

public class IdentificationDiv extends BaseDiv {
	private String programId;

	public IdentificationDiv() {
		super();
	}

	public void analyze() {
		for (String[] rec : recList) {
			if ("PROGRAM-ID.".equals(rec[0])) {
				this.programId = rec[1];
			}
		}
	}

	public String getStat(String title) {
		StringBuilder sb = new StringBuilder(super.getStat(title));
		sb.append("program_id " + programId +"\n");
		return sb.toString();
	}
}

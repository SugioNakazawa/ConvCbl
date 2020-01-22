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

	public String getProgramId() {
		return programId;
	}

}

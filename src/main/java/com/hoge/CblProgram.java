package com.hoge;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CblProgram {
	static Logger logger = LoggerFactory.getLogger(CblProgram.class.getName());
	static private String START_IDENTIFICATION = "startIdentification";
	static private String START_ENVIRONMENT = "startEnvironment";
	static private String START_DATA = "startData";
	static private String START_PROCEDURE = "startProcedure";
	static private String END_IDENTIFICATION = "endIdentification";
	static private String END_ENVIRONMENT = "endEnvironment";
	static private String END_DATA = "endData";
	static private String END_PROCEDURE = "endProcedure";
	static private String ALL_LINES = "allLines";
	static private String VALID_LINES = "validLines";

//	Map<String, Object> statMap;
	private List<String> recListAll;
	private List<String[]> recList;

	private String fileName;
	private IdentificationDiv idDiv;
	private EnvironmentDiv envDiv;
	DataDiv dataDiv;
	private ProcedureDiv procDiv;

	public CblProgram(String fileName) {
//		statMap = new LinkedHashMap<String, Object>();
		this.fileName = fileName;
		recList = new ArrayList<String[]>();
		idDiv = new IdentificationDiv();
		envDiv = new EnvironmentDiv();
		dataDiv = new DataDiv(fileName);
		procDiv = new ProcedureDiv();
	}

	private CblProgram() {
	}

	public String getFileName() {
		return this.fileName;
	}

	public void read() throws IOException {
		recList = CblSource.read(fileName);
		logger.debug("read " + recList.size() + " lines");
	}

	void analyze() {
		separate();
		this.idDiv.analyze();
		this.dataDiv.analyze();
	}

	/**
	 * 実行行のみを抽出しDIVISIONの開始行、終了行を求める。
	 */
	private void separate() {
		BaseDiv saveDiv = null;
		for (String[] cols : recList) {
			if (cols.length > 0) {
				if (cols.length > 1) {
					// check DIVISION
					if ("DIVISION".equals(cols[1])) {
						if ("IDENTIFICATION".equals(cols[0])) {
							saveDiv = idDiv;
						} else if ("ENVIRONMENT".equals(cols[0])) {
							saveDiv = envDiv;
						} else if ("DATA".equals(cols[0])) {
							saveDiv = dataDiv;
						} else if ("PROCEDURE".equals(cols[0])) {
							saveDiv = procDiv;
						}
					}
				}
				saveDiv.addRec(cols);
			}
		}
	}

	public String getStat() {
		StringBuilder sb = new StringBuilder();
		sb.append("fileName : " + fileName + "\n");

		sb.append(idDiv.getStat("ID"));
		sb.append(envDiv.getStat("ENV"));
		sb.append(dataDiv.getStat("DATA"));
		sb.append(procDiv.getStat("PROC"));

//		sb.append("total lines " + recListAll.size() + "\n");
		sb.append("valid lines " + recList.size() + "\n");

		return sb.toString();
	}
}

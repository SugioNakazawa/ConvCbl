package com.hoge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CblSource {
	static Logger logger = LoggerFactory.getLogger(CblSource.class.getName());
	static String FILE_NAME = "fileNamae";
	static String START_IDENTIFICATION = "startIdentification";
	static String START_ENVIRONMENT = "startEnvironment";
	static String START_DATA = "startData";
	static String START_PROCEDURE = "startProcedure";
	static String ALL_LINES = "allLines";
	static String VALID_LINES = "validLines";

	Map<String, Object> statMap;
	private List<String> recListAll;
	private List<String[]> recList;

//	private String fileName;
//	private int startIdentification;
//	private int startEnvironment;
//	private int startData;
//	private int startProcedure;

	public CblSource(String fileName) {
		statMap = new HashMap<String, Object>();
		statMap.put(FILE_NAME, fileName);
		recList = new ArrayList<String[]>();
	}

	private CblSource() {
	}

	public String getFileName() {
		return (String) statMap.get(FILE_NAME);
	}

	public void read() throws IOException {
		Path path = Paths.get(getFileName());
		recListAll = Files.readAllLines(path);
		logger.debug("read " + recListAll.size() + " lines");
	}

	void analyze() {
		int currentDivision = 0;
		for (int i = 0; i < recListAll.size(); i++) {
			String rec = recListAll.get(i);
			// コメント行以外の8カラム移行を空白で分割
			if (rec.length() > 7 && !"*".equals(rec.subSequence(7, 8))) {
				String[] cols = rec.substring(7).split(" ");
				recList.add(cols);
				// check DIVISION
				if (cols.length > 1) {
					if ("DIVISION.".equals(cols[1])) {
						if ("IDENTIFICATION".equals(cols[0])) {
							statMap.put(START_IDENTIFICATION, i);
							currentDivision = 1;
						} else if ("ENVIRONMENT".equals(cols[0])) {
							statMap.put(START_ENVIRONMENT, i);
							currentDivision = 2;
						} else if ("DATA".equals(cols[0])) {
							statMap.put(START_DATA, i);
							currentDivision = 3;
						} else if ("PROCEDURE".equals(cols[0])) {
							statMap.put(START_PROCEDURE, i);
							currentDivision = 4;
						}
					}
				}
				switch (currentDivision) {
				case 1:// IDENTIFICATION
					break;
				case 2:// ENVIRONMENT
					break;
				case 3:// DATA
					break;
				case 4:// PROCEDURE
					break;
				}
			}
		}
	}

	public String getStat() {
		StringBuilder sb = new StringBuilder();
		statMap.forEach((key, value) -> sb.append(key + " : " + value.toString() + "\n"));

//		sb.append("file : " + this.fileName + "\n");
		sb.append("total lines : " + recListAll.size() + "\n");
		sb.append("valid lines : " + recList.size() + "\n");
//		sb.append("start IDENTIFICATION " + startIdentification + "\n");
//		sb.append("startEnvironment = " + startEnvironment + "\n");
//		sb.append("startData = " + startData + "\n");
//		sb.append("startProcedure = " + startProcedure + "\n");
		return sb.toString();
	}
}

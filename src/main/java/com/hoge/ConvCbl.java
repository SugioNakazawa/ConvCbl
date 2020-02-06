/**
 * 
 */
package com.hoge;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nakazawasugio
 *
 */
public class ConvCbl {
	static Logger logger = LoggerFactory.getLogger(ConvCbl.class.getName());

	private String inFile;
	private String outDir;
	List<String> outTypeList;
	private CblProgram program;

	public ConvCbl() {
		outTypeList = Arrays.asList("dmdl");
	}

	/**
	 * @param args <br>
	 *             -i 入力COBOLソースファイル<br>
	 *             -o 出力ディレクトリ(default=out)<br>
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Options options = new Options();
		options.addOption("i", "infile", true, "入力ファイル");
		options.addOption("o", "outdir", true, "出力ディレクトリ");
		options.addOption("t", "type", true, "出力ファイルのタイプ");
		HelpFormatter hf = new HelpFormatter();

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		ConvCbl obj = new ConvCbl();
		try {
			cmd = parser.parse(options, args);
			obj.setParams(cmd);
		} catch (ParseException e) {
			hf.printHelp("[opts]", options);
			String msg = e.getMessage();
			logger.error(msg);
			throw new IllegalArgumentException(msg);
		} catch (IllegalArgumentException e) {
			hf.printHelp("[opts]", options);
			throw (e);
		}
		try {
			obj.exec(cmd.getOptionValues("i")[0]);
		} catch (IOException e) {
			logger.error(Const.MSG_NO_FILE);
			throw (e);
		}
	}

	void exec(String cblSourceFileName) throws IOException {
		program = new CblProgram(cblSourceFileName);
		program.read();
		program.analyze();
		if (outTypeList.contains("dmdl")) {
			program.createDmdl(outDir);
		}
		if (outTypeList.contains("dot")) {
			//	TODO	ファイル名とディレクトリ名の整理が必要
			String[] filePathSplit = cblSourceFileName.split("/");
			String tmp = filePathSplit[filePathSplit.length - 1];
			if (tmp.indexOf(".") > 0) {
				tmp = tmp.substring(0, tmp.lastIndexOf("."));
			}
			program.procDiv.outputDataDot(outDir + "/" + tmp + ".dot");
		}
		program.logout();
	}

	public String getOutDir() {
		return outDir;
	}

	public void setOutDir(String outDir) {
		this.outDir = outDir;
	}

	public CblProgram getProgram() {
		return program;
	}

	private void setParams(CommandLine cmd) {
		// 入力ファイル
		if (!cmd.hasOption("i")) {
			logger.error(Const.MSG_NO_FILE_PARAM);
			throw new IllegalArgumentException(Const.MSG_NO_FILE_PARAM);
		} else {
			inFile = cmd.getOptionValues("i")[0];
		}
		// 出力ディレクトリ
		if (!cmd.hasOption("o")) {
			outDir = "out";
		} else {
			outDir = cmd.getOptionValues("o")[0];
		}
		// 出力タイプ デフォルトはdmdl
		if (cmd.hasOption("t")) {
			outTypeList = Arrays.asList(cmd.getOptionValues("t"));
		}
		logger.info("入力ファイル　" + inFile);
		logger.info("出力ディレクトリ " + outDir);
		logger.info("出力ファイルタイプ " + outTypeList);
	}
}

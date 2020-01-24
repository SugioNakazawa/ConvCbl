/**
 * 
 */
package com.hoge;

import java.io.IOException;

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
	private CblProgram program;

	/**
	 * @param arg
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Options options = new Options();
		options.addOption("i", "infile", true, "入力ファイル");
		options.addOption("o", "outdir", true, "出力ディレクトリ");
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

	void exec(String fileName) throws IOException {
		program = new CblProgram(fileName);
		program.read();
		program.analyze();
		program.dataDiv.createDmdl(outDir);
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
		if (!cmd.hasOption("i")) {
			logger.error(Const.MSG_NO_FILE_PARAM);
			throw new IllegalArgumentException(Const.MSG_NO_FILE_PARAM);
		} else {
			inFile = cmd.getOptionValues("i")[0];
		}
		if (!cmd.hasOption("o")) {
			outDir = "out";
		} else {
			outDir = cmd.getOptionValues("o")[0];
		}
		logger.info("入力ファイル　" + inFile);
		logger.info("出力ディレクトリ " + outDir);
	}
}

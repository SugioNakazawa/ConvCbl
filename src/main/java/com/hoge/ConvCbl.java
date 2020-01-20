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
	static String MSG_NO_FILE_PARAM = "入力ファイルが指定されていません。";
	static String MSG_NO_FILE = "指定ファイルが存在しません。";

	/**
	 * @param arg
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Options options = new Options();
		options.addOption("i", "inputfile", true, "入力ファイル");
		HelpFormatter hf = new HelpFormatter();

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
			setParams(cmd);
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
			ConvCbl obj = new ConvCbl();
			obj.exec(cmd.getOptionValues("i")[0]);
		} catch (IOException e) {
			logger.error(MSG_NO_FILE);
			throw (e);
		}
	}

	private void exec(String fileName) throws IOException {
		CblProgram program = new CblProgram(fileName);
		program.read();
		program.analyze();

		System.out.println(program.getStat());
	}

	private static void setParams(CommandLine cmd) {
		if (!cmd.hasOption("i")) {
			logger.error(MSG_NO_FILE_PARAM);
			throw new IllegalArgumentException(MSG_NO_FILE_PARAM);
		}
		logger.info("入力ファイル[" + String.join(",", cmd.getOptionValues("i")) + "]");
	}
}

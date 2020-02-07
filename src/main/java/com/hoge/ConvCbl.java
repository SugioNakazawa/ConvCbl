/**
 * 
 */
package com.hoge;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
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
	/** 入力ソースファイル **/
	private Path sourceFile;
	/** 出力ディレクトリ **/
	private Path outDir;
	/** 出力タイプ **/
	List<String> outTypeList;
	/** 分岐合流フラグ **/
	private boolean forkedMerge = true;
	/** 戻り線 **/
	private boolean returnArrow = true;

	private CblProgram program;

	public ConvCbl(Path sourceFile) {
		this.sourceFile = sourceFile;
		this.outTypeList = Arrays.asList("dmdl");
	}

	CblProgram getProgram() {
		return program;
	}

	private ConvCbl() {

	}

	/**
	 * @param args <br>
	 *             -i 入力COBOLソースファイル<br>
	 *             -o 出力ディレクトリ(default=out)<br>
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Options options = new Options();
		options.addOption("i", "infile", true, "入力ファイル。");
		options.addOption("o", "outdir", true, "出力ディレクトリ。default:out");
//		options.addOption("t", "type", true, "出力ファイルのタイプ。複数可。 [dmdl, dot]");
		options.addOption(Option.builder("t").longOpt("type").required(false).hasArgs()
				.desc("出力ファイルのタイプ。複数可。 [dmdl, dot]").build());
		options.addOption(Option.builder().longOpt("nomerge").required(false).hasArg(false)
				.desc("DOTファイルの分岐後合流をしない。 default:する").build());
		options.addOption(Option.builder().longOpt("noreturn").required(false).hasArg(false)
				.desc("DOTファイルの戻り線。 default:する").build());
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
		// 実行
		obj.exec();
	}

	void exec() throws IOException {
		program = new CblProgram(sourceFile);
		program.read();
		program.analyze();
		if (outTypeList.contains("dmdl")) {
			program.createDmdl(outDir);
		}
		if (outTypeList.contains("dot")) {
			program.outputDataDot(outDir, forkedMerge, returnArrow);
		}
		program.logout();
	}

	public Path getOutDir() {
		return outDir;
	}

	public void setOutDir(Path outDir) {
		this.outDir = outDir;
	}

	private void setParams(CommandLine cmd) {
		// 入力ファイル
		if (!cmd.hasOption("i")) {
			logger.error(Const.MSG_NO_FILE_PARAM);
			throw new IllegalArgumentException(Const.MSG_NO_FILE_PARAM);
		} else {
			sourceFile = Paths.get(cmd.getOptionValues("i")[0]);
			if (!sourceFile.toFile().isFile() || !sourceFile.toFile().exists()) {
				String msg = MessageFormat.format(Const.MSG_NO_FILE, sourceFile.toString());
				logger.error(msg);
				throw new IllegalArgumentException(msg);
			}
		}
		// 出力ディレクトリ
		if (!cmd.hasOption("o")) {
			outDir = Paths.get("out");
		} else {
			outDir = Paths.get(cmd.getOptionValues("o")[0]);
		}
		if (!outDir.toFile().isDirectory()) {
			String msg = MessageFormat.format(Const.MSG_NO_DIR, outDir.toString());
			logger.error(msg);
			throw new IllegalArgumentException(msg);
		}
		// 出力タイプ デフォルトはdmdl
		if (cmd.hasOption("t")) {
			outTypeList = Arrays.asList(cmd.getOptionValues("t"));
		} else {
			outTypeList = Arrays.asList("dmdl");
		}
		// 分岐後合流
		if (cmd.hasOption("nomerge")) {
			this.forkedMerge = false;
		}
		// 戻り線
		if (cmd.hasOption("noreturn")) {
			this.returnArrow = false;
		}
		logger.info("入力ファイル　" + sourceFile);
		logger.info("出力ディレクトリ " + outDir);
		logger.info("出力ファイルタイプ " + outTypeList);
		logger.info("DOT出力：分岐後の合流 " + forkedMerge);
		logger.info("DOT出力：戻り線出力 " + returnArrow);
	}
}

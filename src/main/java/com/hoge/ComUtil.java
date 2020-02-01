/**
 * 
 */
package com.hoge;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nakazawasugio
 *
 */
public class ComUtil {
	private static Logger logger = LoggerFactory.getLogger(ComUtil.class.getName());

	/**
	 * String配列から指定文字のインデックスを取得。ないときは-1
	 * 
	 * @param cols
	 * @param key
	 * @return
	 */
	public static int search(String[] cols, String key) {
		return Arrays.asList(cols).indexOf(key);
	}
}

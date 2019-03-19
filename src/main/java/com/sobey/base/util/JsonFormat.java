package com.sobey.base.util;

public class JsonFormat {
	private static String SPACE = "    ";

	/**
	 * 返回格式化JSON字符串。
	 * 
	 * @param json
	 *            未格式化的JSON字符串。
	 * @return 格式化的JSON字符串。
	 */
	public static String formatJson(String json) {
		StringBuffer result = new StringBuffer();
		int length = json.length();
		int number = 0;
		char key = 0;
		boolean haveEnd = true;
		boolean haveQuotEnd = true;
		// 遍历输入字符串。
		for (int i = 0; i < length; i++) {
			// 1、获取当前字符。
			key = json.charAt(i);
			// 2、如果当前字符是前方括号、前花括号做如下处理：
			if (haveQuotEnd && (key == '[' || key == '{')) {
				// （1）如果前面还有字符，并且字符为“：”，打印：换行和缩进字符字符串。
				if ((i - 1 > 0) && (json.charAt(i - 1) == ':')) {
					if (i >= length - 1 || json.charAt(i + 1) != '\n') {
						result.append('\n');
					}
					result.append(indent(number));
				} else if ((i - 1 > 0) && key == '{' && (json.charAt(i - 1) == '$')) {
					haveEnd = false;
				}
				// （2）打印：当前字符。
				result.append(key);
				// （3）前方括号、前花括号，的后面必须换行。打印：换行。
				if (haveEnd) {
					if (i >= length - 1 || json.charAt(i + 1) != '\n') {
						result.append('\n');
					}
					// （4）每出现一次前方括号、前花括号；缩进次数增加一次。打印：新行缩进。
					number++;
					result.append(indent(number));
				}
				// （5）进行下一次循环。
				continue;
			}

			// 3、如果当前字符是后方括号、后花括号做如下处理：
			if (haveQuotEnd && (key == ']' || key == '}')) {
				// （1）后方括号、后花括号，的前面必须换行。打印：换行。
				if (key == '}' && !haveEnd) {
					result.append(key);
					haveEnd = true;
					continue;
				}
				if (i >= length - 1 || json.charAt(i + 1) != '\n') {
					result.append('\n');
				}
				// （2）每出现一次后方括号、后花括号；缩进次数减少一次。打印：缩进。
				number--;
				result.append(indent(number));
				// （3）打印：当前字符。
				result.append(key);
				// （4）如果当前字符后面还有字符，并且字符不为“，”，打印：换行。
				if (((i + 1) < length) && (json.charAt(i + 1) != ',') && (json.charAt(i + 1) != '}')
						&& (json.charAt(i + 1) != ']')) {
					if (i >= length - 1 || json.charAt(i + 1) != '\n') {
						result.append('\n');
					}
				}
				// （5）继续下一次循环。
				continue;
			}
			if (key == '\"' && (i == 0 || json.charAt(i - 1) != '\\')) {
				result.append(key);
				if (haveQuotEnd) {
					haveQuotEnd = false;
				} else {
					haveQuotEnd = true;
				}
				continue;
			}
			// 4、如果当前字符是逗号。逗号后面换行，并缩进，不改变缩进次数。
			if ((key == ',')) {
				result.append(key);
				if (haveQuotEnd) {
					if (json.charAt(i + 1) != '\n') {
						result.append('\n');
					}
					result.append(indent(number));
				}
				continue;
			}
			// 5、打印：当前字符。
			result.append(key);
		}

		return result.toString();
	}

	/**
	 * @param number
	 *            缩进次数。
	 * @return 指定缩进次数的字符串。
	 */
	private static String indent(int number) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < number; i++) {
			result.append(SPACE);
		}
		return result.toString();
	}
}

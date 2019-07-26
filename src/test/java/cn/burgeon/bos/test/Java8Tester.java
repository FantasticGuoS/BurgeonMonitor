package cn.burgeon.bos.test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class Java8Tester {

	public static void main(String args[]) {
		final int num = 1;
		Converter<Integer, String> con = (param) -> {
			System.out.println("this is lambda");
			return String.valueOf(param + num);
		};
		String result = con.convert(5); // 输出结果为 6
		System.out.println("结果为：" + result);

		List<String> names = new ArrayList<String>();
		names.add("Google");
		names.add("Runoob");
		names.add("");
		names.add("Taobao");
		names.add("");
		names.add("Baidu");
		names.add("Sina");
		// names.forEach(str -> { System.out.println(str); });
		names.forEach(System.out::println);// 遍历直接输出

		// 获取空字符串的数量
		int count = (int) names.stream().filter(string -> string.isEmpty()).count();
		System.out.println(count);

		// 使用 sorted 方法对输出的 10 个随机数进行排序
		Random random = new Random();
		random.ints().limit(10).sorted().forEach(System.out::println);

		List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
		// Predicate<Integer> predicate = n -> true
		// n 是一个参数传递到 Predicate 接口的 test 方法
		// n 如果存在则 test 方法返回 true
		System.out.println("输出所有数据:");
		// 传递参数 n
		eval(list, n -> true);
		// Predicate<Integer> predicate1 = n -> n%2 == 0
		// n 是一个参数传递到 Predicate 接口的 test 方法
		// 如果 n%2 为 0 test 方法返回 true
		System.out.println("输出所有偶数:");
		eval(list, n -> n % 2 == 0);

		// Predicate<Integer> predicate2 = n -> n > 3
		// n 是一个参数传递到 Predicate 接口的 test 方法
		// 如果 n 大于 3 test 方法返回 true
		System.out.println("输出大于 5 的所有数字:");
		eval(list, n -> n > 5);

		LocalDateTime currentTime = LocalDateTime.now();
		System.out.println("当前时间: " + currentTime);
		Month month = currentTime.getMonth();
		int day = currentTime.getDayOfMonth();
		int seconds = currentTime.getSecond();
		System.out.println("月: " + month + ", 日: " + day + ", 秒: " + seconds);
		LocalDate date1 = currentTime.toLocalDate();
		System.out.println("date1: " + date1);
		LocalDateTime date2 = currentTime.withMonth(5).withDayOfMonth(10).withYear(2012);
		System.out.println("date2: " + date2);
		// 12 december 2014
		LocalDate date3 = LocalDate.of(2014, Month.DECEMBER, 12);
		System.out.println("date3: " + date3);
		// 22 小时 15 分钟
		LocalTime date4 = LocalTime.of(22, 15);
		System.out.println("date4: " + date4);
		// 解析字符串
		LocalTime date5 = LocalTime.parse("20:15:30");
		System.out.println("date5: " + date5);
	}

	public interface Converter<T1, T2> {
		String convert(int i);
	}

	public static void eval(List<Integer> list, Predicate<Integer> predicate) {
		for (Integer n : list) {
			if (predicate.test(n)) {
				System.out.print(n + " ");
			}
		}
		System.out.println();
	}
}

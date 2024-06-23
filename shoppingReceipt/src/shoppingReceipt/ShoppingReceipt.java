/**
 * @Description : 包含銷售稅的購物收據
 * @ClassName : ShoppingReceipt.java
 * @author    : jerrychen
 * @ModifyHistory : 
 *  v1.0.0, 2024/06/23, jerrychen
 *   1) First Release.
 */

package shoppingReceipt;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/*
 * 需求:
 * 編寫一個程式來列印購物車的收據，包括銷售稅。
 * 顧客從美國不同的州購物，銷售稅根據地點和產品類別徵收。
 * 銷售稅 = 無條件進位（價格 * 數量 * 銷售稅率）
 * 特定產品類別免徵銷售稅（即稅額為 0），且特定商品銷售稅金額應四捨五入到最接近的 0.05（例如 1.13->1.15、1.16->1.20、1.151->1.20）
 */
public class ShoppingReceipt {
	
	// 程式入口點
	public static void main(String[] args) {
		input();
	}
	
	
	// 稅率
	private static final Map<String, BigDecimal> TAX_RATES = new HashMap<>();
	// 免稅項目
	private static final Map<String, List<String>> TAX_EXEMPTIONS = new HashMap<>();

	// 定義各州稅率與免稅商品類型
	static {
		TAX_RATES.put("CA", new BigDecimal("0.0975"));
		TAX_RATES.put("NY", new BigDecimal("0.08875"));

		List<String> caExemptions = new ArrayList<>();
		// CA加州食物免稅
		caExemptions.add("food");
		TAX_EXEMPTIONS.put("CA", caExemptions);

		List<String> nyExemptions = new ArrayList<>();
		// 紐約食物跟衣服免稅
		nyExemptions.add("food");
		nyExemptions.add("clothing");
		TAX_EXEMPTIONS.put("NY", nyExemptions);
	}

	// 定義產品項目
	public static class Item {
		String name;
		BigDecimal price;
		int quantity;
		String category;

		public Item(String name, BigDecimal price, int quantity, String category) {
			this.name = name;
			this.price = price;
			this.quantity = quantity;
			this.category = category;
		}
	}


	/**
	 * @Description : 供使用者輸入州別與產品名稱、金額、數量與產品類型，計算商品含稅金額。
	 * @author : jerrychen
	 * @since v1.0.0
	 */

	private static void input() {
		try (Scanner scanner = new Scanner(System.in)) {
			System.out.println("請輸入州別 (CA/NY/other):");

			String location = scanner.nextLine().trim().toUpperCase();
			List<Item> items = new ArrayList<>();

			while (true) {
				System.out.println("請輸入產品名稱，或是輸入done結束");
				String name = scanner.nextLine().trim();
				if (name.equalsIgnoreCase("done")) {
					break;
				}
				System.out.println("請輸入該商品的金額");
				BigDecimal price = scanner.nextBigDecimal();

				System.out.println("請輸入該商品的數量");
				int quantity = scanner.nextInt();
				scanner.nextLine(); // consume the newline

				System.out.println("請輸入該產品的類別(例如:food, clothing, general)");
				String category = scanner.nextLine().trim();

				items.add(new Item(name, price, quantity, category));
			}
			printReceipt(items, location);
		}catch (Exception e) {
			System.out.printf("發生錯誤:%s",e);
		}
	}

	// 應稅項目的稅金，無條件進位到最接近的0.05
	public static BigDecimal roundUpToNearest05(BigDecimal amount) {
		return amount.divide(new BigDecimal("0.05"), 0, RoundingMode.UP).multiply(new BigDecimal("0.05"));
	}

	// 印出收據
	public static void printReceipt(List<Item> items, String location) {
		BigDecimal subtotal = BigDecimal.ZERO;
		BigDecimal salesTax = BigDecimal.ZERO;
		BigDecimal total = BigDecimal.ZERO;
		System.out.printf("%-20s%-10s%-5s\n", "item", "price", "qty");
		
		// 如果location不在TAX_EXEMPTIONS中，返回空列表。也就是如果該州不在定義範圍內，則免稅。
	    List<String> exemptions = TAX_EXEMPTIONS.getOrDefault(location, new ArrayList<>()); 
	    
	    // 如果location不在TAX_RATES中，返回0
	    BigDecimal taxRate = TAX_RATES.getOrDefault(location, BigDecimal.ZERO); 

		for (Item item : items) {
			// multiply方法 為BigDecimal類提供精確的浮點數運算
			BigDecimal itemTotal = item.price.multiply(new BigDecimal(item.quantity));
			subtotal = subtotal.add(itemTotal); // 稅前總金額

			BigDecimal itemTax = BigDecimal.ZERO;
			//如果商品品項不是該州的免稅項目，或是txaRate=0或null(表示該州未定義)，則都不用計算稅率。
			if (!exemptions.contains(item.category) && taxRate != null && taxRate.compareTo(BigDecimal.ZERO) != 0) {
				itemTax = roundUpToNearest05(
						item.price.multiply(new BigDecimal(item.quantity)).multiply(TAX_RATES.get(location)));
//				System.out.println("此商品須計算稅金:");
//				System.out.printf("商品名稱:%s%n",item.name);
//				System.out.printf("商品類型:%s%n",item.category);
//				System.out.printf("商品稅額:%s%n",itemTax);
			}

			salesTax = salesTax.add(itemTax); // 稅額
			total = subtotal.add(salesTax); // 稅後金額
			System.out.printf("%-20s$%-10.2f%-5d\n", item.name, item.price, item.quantity);
		}
		System.out.printf("subtotal:              $%.2f\n", subtotal);
		System.out.printf("tax:                   $%.2f\n", salesTax);
		System.out.printf("total:                 $%.2f\n", total);
	}
}

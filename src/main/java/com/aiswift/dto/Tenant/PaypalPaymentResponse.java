package com.aiswift.dto.Tenant;

import java.util.List;
import lombok.Data;

@Data
public class PaypalPaymentResponse {
	private String id;
	private String status;
	private List<PurchaseUnit> purchaseUnits; //PayPal's API typically returns these as a list
	private List<Link> links;

	@Data
	public static class PurchaseUnit {
		private Amount amount;
		private Payee payee;
		private String reference_id;
	}

	@Data
	public static class Payee {
		private String email_address;
	}

	@Data
	public static class Amount {
		private String currencyCode;
		private String value;
	}

	@Data
	public static class Link {
		private String href;
		private String rel;
		private String method;
	}

	public String getApprovalUrl() {

		return links.stream().filter(link -> "approve".equals(link.getRel())).map(Link::getHref).findFirst()
				.orElse(null);

	}

}

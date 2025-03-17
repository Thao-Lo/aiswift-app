package com.aiswift.Enum;

public enum RefundStatus {
	NOT_REQUESTED, // No refund requested
	AUTO_APPROVED, // Refund automatically approved (before preparation)
	PENDING_APPROVAL, // Refund needs manual approval (after shipped)
	APPROVED, // Refund manually approved
	REJECTED // Refund rejected
}

#!/usr/bin/env python3
"""Rule-based fraud risk scorer used by the Spring Boot order API."""

from __future__ import annotations

import argparse
import json
from dataclasses import dataclass
from decimal import Decimal
from typing import List


@dataclass(frozen=True)
class RiskResult:
    score: int
    level: str
    reasons: List[str]

    def to_dict(self) -> dict:
        return {"score": self.score, "level": self.level, "reasons": self.reasons}


def calculate_risk(
    account_age_days: int,
    order_value: Decimal,
    order_frequency: int,
    address_mismatch: bool,
) -> RiskResult:
    score = 0
    reasons: List[str] = []

    if order_value >= Decimal("600"):
        score += 35
        reasons.append("High order value (>= $600)")
    elif order_value >= Decimal("300"):
        score += 20
        reasons.append("Elevated order value (>= $300)")

    if account_age_days < 7:
        score += 20
        reasons.append("Account created less than 7 days ago")
    elif account_age_days < 30:
        score += 10
        reasons.append("Account created less than 30 days ago")

    if order_frequency >= 5:
        score += 35
        reasons.append("Five or more orders in the last 24 hours")
    elif order_frequency >= 3:
        score += 25
        reasons.append("Three or more orders in the last 24 hours")

    if address_mismatch:
        score += 25
        reasons.append("Billing and shipping addresses do not match")

    score = min(score, 100)

    if score >= 60:
        level = "HIGH"
    elif score >= 30:
        level = "MEDIUM"
    else:
        level = "LOW"

    if not reasons:
        reasons.append("No configured risk indicators were triggered")

    return RiskResult(score=score, level=level, reasons=reasons)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Calculate e-commerce order fraud risk")
    parser.add_argument("--account-age-days", type=int, required=True)
    parser.add_argument("--order-value", type=Decimal, required=True)
    parser.add_argument("--order-frequency", type=int, required=True)
    parser.add_argument("--address-mismatch", choices=["true", "false"], required=True)
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    result = calculate_risk(
        account_age_days=max(0, args.account_age_days),
        order_value=max(Decimal("0"), args.order_value),
        order_frequency=max(0, args.order_frequency),
        address_mismatch=args.address_mismatch == "true",
    )
    print(json.dumps(result.to_dict()))


if __name__ == "__main__":
    main()

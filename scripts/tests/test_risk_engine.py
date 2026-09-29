import unittest
from decimal import Decimal
from pathlib import Path
import sys

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from risk_engine import calculate_risk


class RiskEngineTests(unittest.TestCase):
    def test_low_risk_order(self):
        result = calculate_risk(90, Decimal("75"), 0, False)
        self.assertEqual("LOW", result.level)
        self.assertEqual(0, result.score)

    def test_medium_risk_new_customer(self):
        result = calculate_risk(2, Decimal("350"), 0, False)
        self.assertEqual("MEDIUM", result.level)
        self.assertEqual(40, result.score)

    def test_high_risk_order(self):
        result = calculate_risk(1, Decimal("700"), 5, True)
        self.assertEqual("HIGH", result.level)
        self.assertEqual(100, result.score)
        self.assertGreaterEqual(len(result.reasons), 4)


if __name__ == "__main__":
    unittest.main()

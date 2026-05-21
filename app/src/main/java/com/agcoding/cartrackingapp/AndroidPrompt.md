# 📊 Investment Assistant – Full Product Specification v3

---

## Table of Contents

1. [Overview & Philosophy](#1-overview--philosophy)
2. [Core Concepts](#2-core-concepts)
3. [Data Layer](#3-data-layer)
4. [Indicator Engine](#4-indicator-engine)
5. [Signal Engine](#5-signal-engine)
6. [Scoring & Confidence Engine](#6-scoring--confidence-engine)
7. [Risk Management Engine](#7-risk-management-engine)
8. [Decision Engine (Advice Output)](#8-decision-engine-advice-output)
9. [What-If Simulator](#9-what-if-simulator)
10. [Portfolio Management](#10-portfolio-management)
11. [Trade Execution Simulation](#11-trade-execution-simulation)
12. [Watchlist Engine](#12-watchlist-engine)
13. [Backtesting Module (Future)](#13-backtesting-module-future)
14. [Rebalancing Engine (Future)](#14-rebalancing-engine-future)
15. [Technical Architecture](#15-technical-architecture)
16. [MVP Scope & Exclusions](#16-mvp-scope--exclusions)
17. [Edge Cases & Failure Handling](#17-edge-cases--failure-handling)
18. [Glossary](#18-glossary)

---

## 1. Overview & Philosophy

This application is a **local-first investment assistant and portfolio simulator**.

It does NOT predict the market. It:
- Evaluates rule-based signals derived from technical indicators
- Manages and simulates portfolio allocation decisions
- Outputs structured, explainable advice with confidence scores
- Provides "what-if" scenario analysis for portfolio changes

### Design Principles

| Principle | Implementation |
|---|---|
| Deterministic | Same input → same output, always |
| Explainable | Every decision includes full reasoning breakdown |
| Portfolio-first | Focus on capital allocation, not individual stock picking |
| Conservative by default | Prefers HOLD over forced action |
| Offline-first | All computation runs locally, no cloud dependency |

---

## 2. Core Concepts

### 2.1 System States

The application maintains the following persistent state:

```
AppState {
  portfolios: Portfolio[]        // All user portfolios
  watchlist: WatchlistItem[]     // Monitored assets (no position)
  marketData: AssetData[]        // Cached price history per symbol
  lastFetchTimestamp: DateTime   // Last successful data fetch
  globalSettings: Settings       // API keys, fetch frequency, etc.
}
```

### 2.2 Asset Types Supported (MVP)

| Type | Examples | Source |
|---|---|---|
| US Stocks | AAPL, MSFT, NVDA | Finnhub / Alpha Vantage |
| ETFs | SPY, QQQ, VTI | Finnhub / Alpha Vantage |
| Commodity ETFs | GLD (Gold), SLV (Silver) | Finnhub / Alpha Vantage |

> **Note:** Crypto, forex, and options are explicitly out of scope for MVP.

### 2.3 Risk Profiles

Risk profiles are not just labels — they define hard constraints on the engine.

| Parameter | Low Risk | Medium Risk | High Risk |
|---|---|---|---|
| Asset universe | ETFs, Bonds (BND), Gold | ETFs + Large-cap stocks | Growth stocks, sector ETFs |
| Max position size | 5% of portfolio | 10% of portfolio | 20% of portfolio |
| Max open positions | 5 | 8 | 12 |
| Stop loss | 3–5% | 5–10% | 10–15% |
| Take profit | 8–12% | 12–20% | 20–35% |
| Min cash reserve | 40% | 25% | 15% |
| Min signal score to BUY | 3 (Strong) | 2 (Moderate) | 1 (Weak) |
| Max single-sector exposure | 30% | 40% | 60% |

> **Business Rule:** The engine NEVER issues a BUY signal that would breach any constraint from the active risk profile, even if the signal score is strong.

---

## 3. Data Layer

### 3.1 Required Fields per Asset

```
AssetData {
  symbol: String              // e.g., "AAPL"
  name: String
  assetType: Enum(STOCK, ETF, COMMODITY_ETF)
  priceHistory: PriceBar[]    // Ordered ascending by date
  lastUpdated: DateTime
  dataQuality: Enum(FULL, PARTIAL, STALE, MISSING)
}

PriceBar {
  date: Date
  open: Float
  high: Float
  low: Float
  close: Float
  volume: Long
  adjustedClose: Float        // Adjusted for splits/dividends
}
```

> **Critical:** All calculations MUST use `adjustedClose`, not `close`. Using unadjusted close prices will produce incorrect indicator values around stock splits and dividend events.

### 3.2 Minimum Data Requirements

| Indicator | Minimum Bars Required | Recommended |
|---|---|---|
| RSI(14) | 15 bars | 30 bars |
| SMA50 | 50 bars | 60 bars |
| SMA200 | 200 bars | 220 bars |
| ATR(14) | 15 bars | 30 bars |
| Volume MA(20) | 20 bars | 30 bars |

> **Business Rule:** If an asset has fewer than 200 bars of history, the engine MUST flag `dataQuality: PARTIAL` and disable any indicator that cannot be computed. The decision engine must acknowledge this limitation in its reasoning output.

### 3.3 Data Fetch Strategy

**Default mode:** End-of-day fetch (once daily, after 16:30 ET)

**Fetch priority order:**
1. Assets in active portfolios with open positions
2. Assets in active portfolio watchlists
3. Global watchlist assets
4. New assets being evaluated

**Optimization rules:**
- Maximum 50 unique symbols across all portfolios and watchlists
- Batch API calls where the provider supports it
- On partial failure: mark failed assets as `STALE`, do not block the full update

**Stale data policy:**
- Data older than 1 day → `STALE` (warn user)
- Data older than 5 days → `MISSING` (block signals for this asset)
- Engine must never produce signals on MISSING data

### 3.4 API Fallback Strategy

Primary: **Finnhub**
Fallback: **Alpha Vantage**

If both fail for a symbol:
1. Retain last cached data
2. Mark asset as `STALE` with timestamp
3. Exclude asset from signal generation
4. Surface error to user with last-known price and staleness age

---

## 4. Indicator Engine

All indicators are computed from `adjustedClose` prices unless otherwise specified. Indicators are recomputed on every data refresh.

### 4.1 RSI (Relative Strength Index)

**Period:** 14 days (configurable per portfolio: 7–21)

**Algorithm (Wilder's Smoothing Method):**

```
Step 1: Calculate daily price changes
  change[i] = close[i] - close[i-1]

Step 2: Separate gains and losses
  gain[i] = max(change[i], 0)
  loss[i] = abs(min(change[i], 0))

Step 3: Initial averages (first 14 bars)
  avgGain = sum(gain[1..14]) / 14
  avgLoss = sum(loss[1..14]) / 14

Step 4: Subsequent bars (Wilder's Smoothing)
  avgGain = (prevAvgGain * 13 + gain[i]) / 14
  avgLoss = (prevAvgLoss * 13 + loss[i]) / 14

Step 5: RS and RSI
  RS = avgGain / avgLoss
  RSI = 100 - (100 / (1 + RS))

Edge case: if avgLoss == 0 → RSI = 100
```

**Interpretation thresholds:**

| RSI Value | Signal | Strength |
|---|---|---|
| < 20 | Strongly Oversold | Very Strong BUY signal |
| 20–30 | Oversold | BUY signal |
| 30–45 | Neutral-Low | Weak or no signal |
| 45–55 | Neutral | No signal |
| 55–70 | Neutral-High | Weak or no signal |
| 70–80 | Overbought | SELL signal |
| > 80 | Strongly Overbought | Very Strong SELL signal |

> **Note on RSI alone:** RSI is a momentum oscillator and is NOT sufficient as a standalone signal. It must always be combined with trend indicators (SMA) to avoid buying in a sustained downtrend. An asset can be RSI < 30 and still fall another 40%.

### 4.2 SMA – Simple Moving Averages

**Periods:** SMA50, SMA200

**Algorithm:**
```
SMA(n)[i] = sum(adjustedClose[i-n+1 .. i]) / n
```

**Key derived signals:**

| Condition | Name | Meaning |
|---|---|---|
| SMA50 crosses above SMA200 | Golden Cross | Long-term bullish shift |
| SMA50 crosses below SMA200 | Death Cross | Long-term bearish shift |
| Price > SMA200 | Above Long-Term Trend | Asset in uptrend |
| Price < SMA200 | Below Long-Term Trend | Asset in downtrend |
| Price > SMA50 | Short-term momentum | Positive |
| Price > SMA50 > SMA200 | Full alignment | Strongest trend confirmation |

**Cross detection logic:**
```
goldenCross = SMA50[today] > SMA200[today] AND SMA50[yesterday] <= SMA200[yesterday]
deathCross  = SMA50[today] < SMA200[today] AND SMA50[yesterday] >= SMA200[yesterday]
```

> **Important:** A Golden Cross that occurred 60+ days ago is NOT a current signal. The engine must check cross recency. A cross is "recent" if it occurred within the last 20 trading days.

### 4.3 ATR – Average True Range

**Period:** 14 days

**Algorithm:**
```
TrueRange[i] = max(
  high[i] - low[i],
  abs(high[i] - close[i-1]),
  abs(low[i] - close[i-1])
)

ATR[i] = (ATR[i-1] * 13 + TrueRange[i]) / 14   // Wilder's Smoothing
```

**Usage:**
- Dynamic stop loss calculation: `stopLoss = entryPrice - (ATR * multiplier)`
- Position sizing: higher ATR → smaller position to maintain constant risk exposure
- Signal quality filter: very low ATR (< 0.5% of price) → flat market, signals less reliable

**ATR-based position sizing formula:**
```
riskPerTrade = portfolioValue * riskPercent  // e.g., 1% of portfolio
positionSize = riskPerTrade / (ATR * atrMultiplier)  // atrMultiplier = 1.5–2.0
```

### 4.4 Volume Moving Average

**Period:** 20 days (simple average of volume)

**Usage:**
- Volume confirmation: a BUY signal on volume > 1.5x VolumeMA is stronger
- Volume < 0.5x VolumeMA on a breakout → signal is suspicious, reduce confidence

### 4.5 Indicator Availability Matrix

| Asset Data Available | Indicators Computable |
|---|---|
| < 15 bars | None — asset excluded from engine |
| 15–49 bars | RSI only |
| 50–199 bars | RSI + SMA50 + ATR + VolumeMA |
| 200+ bars | All indicators (RSI + SMA50 + SMA200 + ATR + VolumeMA) |

---

## 5. Signal Engine

Signals are boolean conditions derived from indicators. Each signal has a direction (BUY/SELL) and a weight.

### 5.1 BUY Signals

| ID | Condition | Weight | Requires |
|---|---|---|---|
| B1 | RSI < 30 | 1.0 | RSI |
| B2 | RSI < 20 (strongly oversold) | 2.0 | RSI |
| B3 | Price > SMA200 (uptrend) | 1.0 | SMA200 |
| B4 | SMA50 > SMA200 (bullish structure) | 1.0 | SMA50 + SMA200 |
| B5 | Golden Cross (recent, ≤20 days) | 1.5 | SMA50 + SMA200 |
| B6 | Volume > 1.5x VolumeMA (confirmation) | 0.5 | VolumeMA |
| B7 | RSI rising for 3 consecutive days | 0.5 | RSI |

### 5.2 SELL Signals

| ID | Condition | Weight | Requires |
|---|---|---|---|
| S1 | RSI > 70 | 1.0 | RSI |
| S2 | RSI > 80 (strongly overbought) | 2.0 | RSI |
| S3 | Price < SMA200 (downtrend) | 1.0 | SMA200 |
| S4 | SMA50 < SMA200 (bearish structure) | 1.0 | SMA50 + SMA200 |
| S5 | Death Cross (recent, ≤20 days) | 1.5 | SMA50 + SMA200 |
| S6 | Stop loss triggered | FORCED | position data |
| S7 | Take profit triggered | FORCED | position data |

### 5.3 Conflicting Signal Resolution

When BUY and SELL signals coexist, the engine does NOT simply sum them. It follows this resolution hierarchy:

```
Priority 1 (FORCED): Stop loss / Take profit → always SELL, no override possible
Priority 2 (BLOCK):  Price < SMA200 → blocks all BUY signals regardless of RSI
Priority 3 (REDUCE): Death Cross active → reduce all BUY signal weights by 50%
Priority 4 (NET):    Net score = sum(BUY weights) - sum(SELL weights)
  if net > 0 → direction = BUY, score = net
  if net < 0 → direction = SELL, score = abs(net)
  if net == 0 → HOLD
```

**Example – Conflicting scenario:**
```
RSI = 28 (B1: +1.0, but Price < SMA200 → B1 BLOCKED)
Price = $95, SMA200 = $100 (S3: -1.0)
SMA50 = $92 < SMA200 = $100 (S4: -1.0)

Result: Net = -2.0 → SELL signal score 2
Reasoning: "Despite oversold RSI, asset is in a confirmed downtrend. 
            Price below SMA200 and bearish SMA structure override oversold conditions."
```

> **Business Rule:** The engine MUST NEVER issue a BUY signal when `price < SMA200` AND `SMA50 < SMA200` simultaneously, regardless of RSI. This combination indicates a confirmed downtrend and buying is prohibited.

---

## 6. Scoring & Confidence Engine

### 6.1 Raw Score Calculation

```
buyScore  = sum of weights of all active BUY signals (after conflict resolution)
sellScore = sum of weights of all active SELL signals (after conflict resolution)
netScore  = buyScore - sellScore
direction = BUY if netScore > 0, SELL if netScore < 0, HOLD if netScore == 0
absScore  = abs(netScore)
```

### 6.2 Score Bucketing

| absScore | Label | Action |
|---|---|---|
| 0 | Neutral | HOLD |
| 0.1–0.9 | Very Weak | HOLD (unless High Risk profile) |
| 1.0–1.9 | Weak | HOLD (Low), conditional (Medium), act (High) |
| 2.0–2.9 | Moderate | HOLD (Low), act (Medium + High) |
| 3.0–3.9 | Strong | Act (all profiles meeting threshold) |
| 4.0+ | Very Strong | Act (all profiles) |

### 6.3 Confidence Score Calculation

Confidence is NOT a fixed mapping from score. It is calculated as:

```
baseConfidence = scoreBucketBase[bucket]
  // Neutral: 0%, VeryWeak: 35%, Weak: 50%, Moderate: 62%, Strong: 75%, VeryStrong: 85%

volumeBonus = (volume > 1.5x VolumeMA) ? +5% : 0%
trendBonus  = (price > SMA200 AND SMA50 > SMA200) ? +5% : 0%
dataQualityPenalty = (dataQuality == PARTIAL) ? -15% : 0%
rsiExtremeBonus = (RSI < 20 OR RSI > 80) ? +5% : 0%

finalConfidence = clamp(baseConfidence + volumeBonus + trendBonus + rsiExtremeBonus + dataQualityPenalty, 0, 90%)
```

> **Hard cap:** Confidence is capped at 90%. The system must NEVER display 100% confidence. No rule-based system can guarantee an outcome.

> **Display Rule:** Confidence must always be shown with a label: "This score reflects historical signal reliability, not a guarantee of returns."

### 6.4 Expected Move Calculation

Expected move is a historically-derived range, NOT a price prediction.

```
For BUY signal:
  Look back at last 50 instances where the same signal combination fired on this asset (or similar assets in same sector)
  Calculate median forward return over next 10 and 20 trading days
  Calculate 25th and 75th percentile as the range

  Display: "Historically, this signal combination has been followed by a 
            median return of +X.X% over 20 days (range: +Y.Y% to +Z.Z%)"
```

If there are fewer than 10 historical instances for this asset:
- Fall back to sector-level historical data
- Increase the displayed range width by 50%
- Flag: "Limited historical data for this asset — range is wider than usual"

**This field must NEVER be displayed as a price target.** It is a statistical range from historical patterns only.

---

## 7. Risk Management Engine

This engine runs AFTER signal scoring and acts as a hard filter. No action passes to execution simulation without clearing all risk checks.

### 7.1 Pre-Trade Checks (BUY)

Run in this exact order. First failure → reject BUY, output reason.

```
CHECK 1: Cash availability
  availableCash = portfolio.cash - (portfolio.value * minCashReserve)
  allocationAmount = portfolio.value * positionSizePct
  if allocationAmount > availableCash → REJECT ("Insufficient cash reserve")

CHECK 2: Position count
  if openPositions.count >= maxPositions → REJECT ("Max positions reached")

CHECK 3: Duplicate position
  if symbol already in openPositions → REJECT ("Position already open — use ADD or HOLD")

CHECK 4: Single-asset exposure
  newExposure = (allocationAmount / portfolio.value)
  if newExposure > maxSingleAssetExposure → REJECT ("Single asset limit exceeded")

CHECK 5: Sector concentration
  sectorExposure = sum of portfolio value in same sector + allocationAmount
  if sectorExposure / portfolio.value > maxSectorExposure → REJECT ("Sector concentration limit")

CHECK 6: Signal threshold
  if signalScore < minScoreForProfile → REJECT ("Signal too weak for risk profile")

CHECK 7: Data quality
  if asset.dataQuality == MISSING → REJECT ("Insufficient data for this asset")

All checks pass → APPROVE BUY
```

### 7.2 Stop Loss & Take Profit Logic

Two modes supported per portfolio (user-selectable):

**Mode A: Percentage-based (simple)**
```
stopLossPrice  = entryPrice * (1 - stopLossPct)
takeProfitPrice = entryPrice * (1 + takeProfitPct)
```

**Mode B: ATR-based (dynamic)**
```
stopLossPrice  = entryPrice - (ATR_at_entry * atrMultiplier)   // default multiplier: 1.5
takeProfitPrice = entryPrice + (ATR_at_entry * atrMultiplier * rewardRatio)  // default rewardRatio: 2.0
```

> **Recommendation:** Default new portfolios to ATR-based. ATR adapts to each asset's volatility, producing more sensible stop distances than fixed percentages.

**Trailing Stop (optional, future MVP+):**
```
trailingStop = currentPrice * (1 - trailingStopPct)
// Update daily: only move UP, never down
```

### 7.3 Position Sizing Summary

```
positionSize = min(
  portfolio.value * maxPositionSizePct,         // profile cap
  availableCash,                                  // cash cap
  riskPerTrade / (entryPrice - stopLossPrice)   // ATR-derived (if ATR mode)
)
```

---

## 8. Decision Engine (Advice Output)

This is the final output layer. It assembles all previous engine outputs into a structured, human-readable advice object.

### 8.1 Advice Object Schema

```
Advice {
  symbol: String
  timestamp: DateTime
  action: Enum(BUY, SELL, HOLD)
  signalScore: Float
  confidence: Float (0–90%)
  positionSuggestion: {
    allocationAmount: Float
    allocationPct: Float
    stopLossPrice: Float
    takeProfitPrice: Float
    stopLossMode: Enum(PERCENTAGE, ATR)
  }
  expectedMove: {
    median10d: Float (%)
    median20d: Float (%)
    rangeLow: Float (%)
    rangeHigh: Float (%)
    dataSource: Enum(ASSET_HISTORY, SECTOR_HISTORY)
    historicalInstances: Int
  }
  reasoning: ReasoningBreakdown
  blockers: String[]   // Risk checks that blocked action, if any
  dataQuality: Enum(FULL, PARTIAL, STALE, MISSING)
}

ReasoningBreakdown {
  activeSignals: Signal[]        // All signals that fired, with weights
  conflictResolution: String     // Human-readable explanation of how conflicts resolved
  riskChecks: CheckResult[]      // Each check, pass/fail, reason
  indicatorsUsed: String[]       // Which indicators were available and used
  indicatorValues: {             // Actual computed values shown to user
    rsi: Float
    sma50: Float
    sma200: Float
    atr: Float
    volumeRatio: Float           // current volume / VolumeMA
  }
}
```

### 8.2 Human-Readable Reasoning Template

The engine generates a narrative explanation for every advice object.

**Example BUY output:**
```
Action: BUY AAPL
Confidence: 72%
Signal Score: 2.5 (Strong)

Why:
- RSI at 27.4 → asset is oversold (signal weight: 1.0)
- Price ($171.20) is above SMA200 ($165.40) → uptrend confirmed (+1.0)
- SMA50 ($168.90) is above SMA200 → bullish structure (+1.0)
- Volume today is 1.8x the 20-day average → strong participation (+0.5)

Expected move (next 20 days): median +3.2% | range: +1.1% to +6.4%
Based on 34 historical instances of this signal combination.

Suggested position: $1,840 (9.2% of portfolio)
Stop loss: $162.90 (ATR-based, -4.9%)
Take profit: $187.60 (+9.6%)

⚠️ Confidence reflects historical signal reliability. Past performance does not guarantee future results.
```

**Example HOLD output (blocked BUY):**
```
Action: HOLD TSLA
Signal Score: 1.0 (Weak BUY) — blocked by risk rules

Why score exists:
- RSI at 29.1 → oversold (+1.0)

Why action is HOLD:
- BLOCKED: Price ($185.40) is below SMA200 ($220.10) — downtrend confirmed
- BLOCKED: SMA50 ($192.00) is below SMA200 — bearish structure
- Rule: BUY signals are suppressed when price is below SMA200 in a bearish SMA structure

Recommendation: Monitor. Wait for price to reclaim SMA200 before considering entry.
```

---

## 9. What-If Simulator

The What-If Simulator allows users to model hypothetical portfolio changes without executing them. This is a core feature and must be included in MVP.

### 9.1 Supported Scenarios

**Scenario A: "What if I sell X?"**
```
Input: symbol to sell, quantity or full position
Output:
  - New cash position
  - New portfolio allocation breakdown
  - Realized PnL (simulated)
  - Signals that would now be actionable with freed cash
  - New portfolio risk metrics (sector exposure, concentration)
```

**Scenario B: "What if I buy Y?"**
```
Input: symbol, allocation amount or percentage
Output:
  - New cash position
  - New portfolio allocation breakdown
  - Risk check results (would this pass all checks?)
  - Contribution to portfolio volatility (ATR-based estimate)
  - New sector exposure
  - Signal for Y at current market data
```

**Scenario C: "What if I swap X for Y?"**
```
Input: sell symbol X, buy symbol Y
Output:
  - All outputs from Scenario A (sell X) +
  - All outputs from Scenario B (buy Y) +
  - Portfolio-level comparison: before vs. after
    - Total expected volatility change
    - Sector exposure change
    - Signal quality comparison (was X signaling? Is Y signaling?)
```

### 9.2 Simulator Rules

- Simulator uses live cached market data (not real-time)
- All simulator outputs are clearly labeled "SIMULATION — NOT EXECUTED"
- Simulator results do NOT modify actual portfolio state
- Risk checks run in full during simulation and results are shown

---

## 10. Portfolio Management

### 10.1 Portfolio Schema

```
Portfolio {
  id: UUID
  name: String
  createdAt: DateTime
  currency: String (default: "USD")
  initialCapital: Float
  cash: Float
  strategy: StrategyConfig
  positions: Position[]
  tradeHistory: Trade[]
  performanceHistory: DailySnapshot[]
}

StrategyConfig {
  riskProfile: Enum(LOW, MEDIUM, HIGH)
  assetUniverse: AssetType[]
  maxPositions: Int
  positionSizePct: Float
  stopLossMode: Enum(PERCENTAGE, ATR)
  stopLossPct: Float (if PERCENTAGE mode)
  takeProfitPct: Float (if PERCENTAGE mode)
  atrMultiplier: Float (if ATR mode)
  fetchFrequency: Enum(DAILY, EVERY_6H)
  minSignalScore: Float
}

Position {
  symbol: String
  openDate: Date
  entryPrice: Float
  quantity: Float
  allocationAmount: Float
  stopLossPrice: Float
  takeProfitPrice: Float
  currentPrice: Float
  unrealizedPnL: Float
  unrealizedPnLPct: Float
  daysHeld: Int
}
```

### 10.2 Portfolio Performance Metrics

Computed daily and stored as snapshots:

```
DailySnapshot {
  date: Date
  totalValue: Float               // cash + positions market value
  dailyReturn: Float (%)
  totalReturn: Float (%)          // since inception
  positionsValue: Float
  cashValue: Float
  openPositionsCount: Int
  drawdown: Float (%)             // from peak
  peakValue: Float                // all-time high of portfolio value
}
```

**Key metrics displayed:**

| Metric | Formula |
|---|---|
| Total Return | (currentValue - initialCapital) / initialCapital |
| Drawdown | (peakValue - currentValue) / peakValue |
| Win Rate | closedPositions with profit / total closed positions |
| Avg Win | mean of positive closed trades (%) |
| Avg Loss | mean of negative closed trades (%) |
| Profit Factor | totalGains / abs(totalLosses) |

---

## 11. Trade Execution Simulation

### 11.1 BUY Execution

```
1. Run all Pre-Trade Checks (Section 7.1)
2. If all pass:
   a. Deduct allocationAmount from portfolio.cash
   b. Create Position object with:
      - entryPrice = last close price (NOT current price — simulated EOD)
      - quantity = allocationAmount / entryPrice
      - stopLossPrice and takeProfitPrice computed per active mode
   c. Append to portfolio.positions
   d. Record Trade in tradeHistory (type: BUY)
3. Recalculate portfolio metrics
```

### 11.2 SELL Execution

**Trigger sources:** User-initiated | Stop loss hit | Take profit hit | Signal-driven

```
1. Locate position by symbol
2. Calculate realized PnL:
   realizedPnL = (exitPrice - entryPrice) * quantity
   realizedPnLPct = (exitPrice - entryPrice) / entryPrice
3. Add proceeds to portfolio.cash:
   portfolio.cash += exitPrice * quantity
4. Remove from portfolio.positions
5. Record Trade in tradeHistory:
   Trade {
     type: SELL
     exitReason: Enum(SIGNAL, STOP_LOSS, TAKE_PROFIT, USER_MANUAL)
     realizedPnL: Float
     realizedPnLPct: Float
     daysHeld: Int
   }
6. Recalculate portfolio metrics
```

### 11.3 Price Used for Simulation

The engine ALWAYS uses the **last available closing price** for trade simulation. It NEVER uses intraday prices, bid/ask spreads, or live quotes. This must be clearly labeled in the UI as "simulated at last close."

---

## 12. Watchlist Engine

### 12.1 Purpose

The watchlist allows users to monitor assets without holding a position. Assets on the watchlist:
- Receive full signal analysis
- Appear in "Opportunities" section with their current signal
- Can be quickly added to a portfolio via the What-If Simulator first

### 12.2 Watchlist Item Schema

```
WatchlistItem {
  symbol: String
  addedDate: Date
  notes: String (user-defined)
  alertConfig: AlertConfig
  lastSignal: Advice
}

AlertConfig {
  notifyOnScore: Float   // alert when signal score >= this value
  notifyOnRSI: Float     // alert when RSI drops below this value
  notifyOnPriceBelow: Float
  notifyOnPriceAbove: Float
}
```

---

## 13. Backtesting Module (Future Phase)

> Planned for post-MVP. Implementation notes included for forward compatibility.

### 13.1 What Backtesting Does

Runs the full signal engine (Sections 4–7) on historical data as if operating in the past, to measure strategy effectiveness.

### 13.2 Backtesting Pipeline

```
For each day in backtest window:
  1. Provide only data available up to that day (no lookahead)
  2. Run Indicator Engine
  3. Run Signal Engine
  4. Run Risk Management Engine
  5. Simulate trade if action approved
  6. Update simulated portfolio state
  7. Record daily snapshot

Output metrics:
  - Total return
  - Max drawdown
  - Win rate
  - Profit factor
  - Sharpe ratio (if risk-free rate provided)
  - Trade count
  - Average holding period
```

> **Critical:** The backtesting engine MUST enforce strict data isolation — it cannot use any price data from after the simulated "current" day. This is the look-ahead bias problem and corrupts all results if violated.

---

## 14. Rebalancing Engine (Future Phase)

> Planned for post-MVP.

**Purpose:** Periodically adjust portfolio to maintain target allocation percentages.

**Trigger conditions:**
- Asset weight drifts more than ±5% from target
- User-initiated manual rebalance
- Periodic (monthly/quarterly, user-defined)

**Output:** List of suggested BUY/SELL actions to restore target weights, run through full risk engine before surfacing.

---

## 15. Technical Architecture

### 15.1 Local-First Stack (Android)

| Layer | Technology |
|---|---|
| Database | Room (SQLite) |
| Background jobs | WorkManager |
| Networking | Retrofit + OkHttp |
| Data serialization | Gson / Moshi |
| Computation | Pure Kotlin — no ML libraries |

### 15.2 Database Schema Overview

**Tables:**
- `portfolios` — portfolio metadata and strategy config
- `positions` — open positions per portfolio
- `trade_history` — all closed trades
- `portfolio_snapshots` — daily performance snapshots
- `asset_data` — cached OHLCV price history per symbol
- `watchlist` — watchlist items
- `indicator_cache` — cached computed indicator values with timestamp

> **Optimization:** Computed indicator values are cached with a `computedAt` timestamp. On data refresh, only recompute indicators for assets whose `lastUpdated` is newer than `indicatorCache.computedAt`. This avoids redundant computation on every app open.

### 15.3 Background Job Schedule

| Job | Trigger | Conditions |
|---|---|---|
| DataFetchJob | Daily at 17:00 ET | Network available |
| IndicatorComputeJob | After DataFetchJob | New data available |
| SignalEngineJob | After IndicatorComputeJob | Indicators fresh |
| StopLossScanJob | Daily at 17:30 ET | Open positions exist |
| PortfolioSnapshotJob | Daily at 18:00 ET | After all above |

### 15.4 Computation Performance Targets

| Operation | Target Time |
|---|---|
| Full indicator compute (50 assets, 200 bars) | < 500ms |
| Signal engine for all portfolios | < 200ms |
| Portfolio metrics recalculation | < 100ms |
| What-If Simulator scenario | < 300ms |
| Full daily pipeline (fetch excluded) | < 2s |

---

## 16. MVP Scope & Exclusions

### 16.1 MVP Include

- Up to 3 portfolios
- Up to 10 assets per portfolio (30 total including watchlist)
- Indicators: RSI, SMA50, SMA200, ATR, Volume MA
- Full signal engine with conflict resolution
- Full risk management engine with all 7 checks
- What-If Simulator (Scenarios A, B, C)
- Watchlist with signal monitoring
- Daily data fetch (EOD)
- Portfolio performance metrics
- Full reasoning output per advice

### 16.2 MVP Exclude

| Feature | Reason |
|---|---|
| Backtesting | Requires significant historical data, post-MVP |
| Rebalancing engine | Post-MVP |
| Real-time / intraday data | API cost, battery, complexity |
| Machine learning models | Out of scope, rule-based only |
| Real trade execution | Explicitly not a trading platform |
| Crypto / Forex | Data quality and volatility make indicators unreliable |
| Trailing stop | Post-MVP |
| Multi-currency portfolios | All portfolios USD only in MVP |

---

## 17. Edge Cases & Failure Handling

| Scenario | Handling |
|---|---|
| API rate limit hit | Exponential backoff, retry after 60s, max 3 retries |
| Asset delisted | Mark as INACTIVE, close position at last known price, notify user |
| Stock split not reflected | Flag data anomaly if close drops >40% in one day — pause signals |
| Portfolio value = $0 | Lock portfolio, prevent further trades, show warning |
| All signals HOLD for 30+ days | Surface "no opportunity" message, suggest watchlist review |
| Historical data gap (missing days) | Fill using prior bar close. Flag if gap > 5 consecutive days |
| ATR = 0 (no price movement) | Use percentage-based stop as fallback |
| Volume = 0 reported by API | Mark volume as missing, skip volume-based signal adjustments |
| Conflicting signal: all BUY blocked by price < SMA200 | Output HOLD with full explanation — never force a BUY |

---

## 18. Glossary

| Term | Definition |
|---|---|
| ATR | Average True Range — measures volatility over N days |
| RSI | Relative Strength Index — momentum oscillator, 0–100 scale |
| SMA | Simple Moving Average — average closing price over N days |
| Golden Cross | SMA50 crosses above SMA200 — bullish long-term signal |
| Death Cross | SMA50 crosses below SMA200 — bearish long-term signal |
| Adjusted Close | Close price adjusted for splits and dividends |
| Drawdown | Peak-to-trough decline in portfolio value |
| Position Size | Amount of capital allocated to a single asset |
| Stop Loss | Price at which a position is automatically exited to limit loss |
| Take Profit | Price at which a position is automatically exited to lock in gain |
| Signal Score | Weighted sum of active signals after conflict resolution |
| Confidence | Probability-like measure of signal reliability (0–90%) |
| Look-ahead bias | Using future data in historical simulation — invalidates results |
| PnL | Profit and Loss |
| EOD | End of Day |

---

*Specification Version: 3.0 | Status: Ready for Development Review*
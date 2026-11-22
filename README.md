# Java & OOP Adventure — Week 03: Contracts, Invariants & Rewards

**Theme:** fantasy battle. RECOVERY = healing, POWER = stronger basic attack.

## Environment & Run Instructions
JDK 25.

```bash
mkdir -p build
javac -d build src/main/java/edu/starter/*.java
java -cp build edu.starter.Main
# automated scenarios used for the traces below:
java -cp build edu.starter.SimulationRunner
```

## Design Principles
- **Contracts in suppliers:** `Combatant`, `Player` and `Game` document `@pre`/`@post` in Javadoc
  and never re-check their own preconditions (Golden Rule).
- **Checks in clients:** `Main` calls `canSpecial()` before `special()` and `canClaimReward()`
  before `reward()`, and verifies the reward postcondition and the invariants
  (`0 <= HP <= 100`, `0 <= resource <= 10`) with `Checks.that`.
- **Invariants:** `takeDamage` and `heal` keep HP in `0..MAX_HP`, `recover` keeps resource at most 10,
  `spendResource` relies on its `@pre` (checked by `canSpecial()`). Attack never decreases.
- **State changes** go through focused methods (`takeDamage`, `heal`, `spendResource`,
  `recover`, `applyReward`); there is no setter that writes an arbitrary HP or resource.
  Fields are `protected` (approved by the instructor).
- **Enum:** rewards are `Reward { RECOVERY, POWER }`, applied by `Player.applyReward`.
- **Input robustness:** `ConsoleChoice` returns -1 for invalid input; if input ends (EOF),
  the program exits cleanly without starting another encounter.
- `Combatant` is the base class kept from earlier weeks.

## Design Note
State ownership: Combatant and Player own HP, resource, attack damage and the rewardClaimed flag.
Game owns encounter sequencing and the reward gate. Contracts live in the suppliers as @pre/@post
Javadoc, and the client is responsible for meeting them: Main calls canSpecial() before special()
and canClaimReward() before reward(). Suppliers never re-check their own preconditions.

Invariants: 0 <= HP <= 100 and 0 <= resource <= 10 after every action; attack never decreases.
Main verifies them with Checks.that after each action.

Reward: the precondition is canClaimReward(): Enemy 1 defeated, player alive, no reward claimed yet.
Recovery sets HP = min(100, old HP + 25), leaving resource and attack unchanged. Power adds 2 to
attack, leaving HP and resource unchanged. Special stays 15 because it is a constant in Game,
independent of attack damage. Main checks the reward postcondition after the call.

Invalid reward input prints an error and re-prompts without changing state. rewardClaimed guarantees
uniqueness. The same Player object continues into Encounter 2 against a fresh 45-HP enemy.
On a loss, Main returns immediately and no reward is offered.

## Acceptance Tests
Evidence comes from `SimulationRunner`. T1, T2 and T9 use `new Game(HP, ...)` to reach boundary
states directly, without separate test files.

| ID | Expected | Actual (from the traces below) |
|----|----------|--------------------------------|
| T1 | Recovery from 90 → 100, resource/Attack unchanged | Scenario "T1": HP 90 → 100, resource 5, Attack 10 |
| T2 | Recovery at 100 → 100, reward consumed | Scenario "T2": HP stays 100, `canClaimReward after reward: false` |
| T3 | Power → Attack 12, Special still 15 | Scenario 1: Attack 10 → 12, HP and resource unchanged. Special is the constant 15 in `Game.special()` and is not derived from Attack (in Encounter 1 it dealt 15 before the reward) |
| T4 | Invalid input, state unchanged, then Power once | Scenario 3: `Invalid reward choice!`, then `POWER applied!`, HP 80 / resource 2 / Attack 12 |
| T5 | No reward before victory | Every scenario: `canClaimReward before fight: false` |
| T6 | No second reward | Every scenario: `canClaimReward after reward: false` |
| T7 | Loss → no reward, no second battle | Scenario 4: `game over, you lose :(`, `No reward offered` |
| T8 | HP 80 / resource 2 persist into Encounter 2 | Scenarios 1 and 2: HP 80 / res 2 (Power) and HP 100 / res 2 (Recovery) |
| T9 | Finishing attack on Enemy 2, no counterattack | Scenario "T9": Enemy 2 reaches 0 HP, player HP stays 100, `game over, you won!!` |

## Traces
Output of `java -cp build edu.starter.SimulationRunner`.
In scenarios 1–3 and in T1/T2 the scripted input ends right after Encounter 2 starts, so the
trace stops with `Input ended.`; those scenarios only demonstrate the transition and the reward.
Scenario T9 plays Encounter 2 to the end.

### Scenario 1: Victory with POWER reward (T3, T5, T6, T8)
```text
=========================================
  SCENARIO 1 (POWER)
=========================================
canClaimReward before fight: false
--- Current Status ---
Name: player | HP left: 100
Player resource left: 5 | Attack: 10
Name: enemy1 | HP left: 30
----------------------

Enter your choice (1-4): 2
player attack enemy
enemy attack player
--- Current Status ---
Name: player | HP left: 90
Player resource left: 2 | Attack: 10
Name: enemy1 | HP left: 15
----------------------

Enter your choice (1-4): 1
player attack enemy
enemy attack player
--- Current Status ---
Name: player | HP left: 80
Player resource left: 2 | Attack: 10
Name: enemy1 | HP left: 5
----------------------

Enter your choice (1-4): 1
player attack enemy
Enemy 1 defeated!
--- Current Status ---
Name: player | HP left: 80
Player resource left: 2 | Attack: 10
Name: enemy1 | HP left: 0
----------------------

=== VICTORY IN ENCOUNTER 1! CHOOSE YOUR REWARD ===
1. RECOVERY (Restore 25 HP, capped at 100)
2. POWER (Permanently increase basic Attack by 2)
Select reward (1 or 2): POWER applied!
Enemy 2 appears with 45 HP!
canClaimReward after reward: false

--- Encounter 2 vs Enemy 2 ---
--- Current Status ---
Name: player | HP left: 80
Player resource left: 2 | Attack: 12
Name: enemy2 | HP left: 45
----------------------
Input ended.
```

### Scenario 2: Victory with RECOVERY reward (T8)
```text
=========================================
  SCENARIO 2 (RECOVERY)
=========================================
canClaimReward before fight: false
--- Current Status ---
Name: player | HP left: 100
Player resource left: 5 | Attack: 10
Name: enemy1 | HP left: 30
----------------------

Enter your choice (1-4): 2
player attack enemy
enemy attack player
--- Current Status ---
Name: player | HP left: 90
Player resource left: 2 | Attack: 10
Name: enemy1 | HP left: 15
----------------------

Enter your choice (1-4): 1
player attack enemy
enemy attack player
--- Current Status ---
Name: player | HP left: 80
Player resource left: 2 | Attack: 10
Name: enemy1 | HP left: 5
----------------------

Enter your choice (1-4): 1
player attack enemy
Enemy 1 defeated!
--- Current Status ---
Name: player | HP left: 80
Player resource left: 2 | Attack: 10
Name: enemy1 | HP left: 0
----------------------

=== VICTORY IN ENCOUNTER 1! CHOOSE YOUR REWARD ===
1. RECOVERY (Restore 25 HP, capped at 100)
2. POWER (Permanently increase basic Attack by 2)
Select reward (1 or 2): RECOVERY applied!
Enemy 2 appears with 45 HP!
canClaimReward after reward: false

--- Encounter 2 vs Enemy 2 ---
--- Current Status ---
Name: player | HP left: 100
Player resource left: 2 | Attack: 10
Name: enemy2 | HP left: 45
----------------------
Input ended.
```

### Scenario 3: Invalid reward input, then POWER (T4)
```text
=========================================
  SCENARIO 3 (INVALID REWARD THEN POWER, T4)
=========================================
canClaimReward before fight: false
--- Current Status ---
Name: player | HP left: 100
Player resource left: 5 | Attack: 10
Name: enemy1 | HP left: 30
----------------------

Enter your choice (1-4): 2
player attack enemy
enemy attack player
--- Current Status ---
Name: player | HP left: 90
Player resource left: 2 | Attack: 10
Name: enemy1 | HP left: 15
----------------------

Enter your choice (1-4): 1
player attack enemy
enemy attack player
--- Current Status ---
Name: player | HP left: 80
Player resource left: 2 | Attack: 10
Name: enemy1 | HP left: 5
----------------------

Enter your choice (1-4): 1
player attack enemy
Enemy 1 defeated!
--- Current Status ---
Name: player | HP left: 80
Player resource left: 2 | Attack: 10
Name: enemy1 | HP left: 0
----------------------

=== VICTORY IN ENCOUNTER 1! CHOOSE YOUR REWARD ===
1. RECOVERY (Restore 25 HP, capped at 100)
2. POWER (Permanently increase basic Attack by 2)
Select reward (1 or 2): Invalid reward choice! Please try again without changing state.
1. RECOVERY (Restore 25 HP, capped at 100)
2. POWER (Permanently increase basic Attack by 2)
Select reward (1 or 2): POWER applied!
Enemy 2 appears with 45 HP!
canClaimReward after reward: false

--- Encounter 2 vs Enemy 2 ---
--- Current Status ---
Name: player | HP left: 80
Player resource left: 2 | Attack: 12
Name: enemy2 | HP left: 45
----------------------
Input ended.
```

### Scenario 4: Loss in Encounter 1, no reward (T7)
```text
=========================================
  SCENARIO 4 (LOSS, T7)
=========================================
canClaimReward before fight: false
--- Current Status ---
Name: player | HP left: 100
Player resource left: 5 | Attack: 10
Name: enemy1 | HP left: 30
----------------------

Enter your choice (1-4): 4
--- Current Status ---
Name: player | HP left: 95
Player resource left: 5 | Attack: 10
Name: enemy1 | HP left: 30
----------------------

Enter your choice (1-4): 4
--- Current Status ---
Name: player | HP left: 90
Player resource left: 5 | Attack: 10
Name: enemy1 | HP left: 30
----------------------

Enter your choice (1-4): 4
--- Current Status ---
Name: player | HP left: 85
Player resource left: 5 | Attack: 10
Name: enemy1 | HP left: 30
----------------------

Enter your choice (1-4): 4
--- Current Status ---
Name: player | HP left: 80
Player resource left: 5 | Attack: 10
Name: enemy1 | HP left: 30
----------------------

Enter your choice (1-4): 4
--- Current Status ---
Name: player | HP left: 75
Player resource left: 5 | Attack: 10
Name: enemy1 | HP left: 30
----------------------

Enter your choice (1-4): 4
--- Current Status ---
Name: player | HP left: 70
Player resource left: 5 | Attack: 10
Name: enemy1 | HP left: 30
----------------------

Enter your choice (1-4): 4
--- Current Status ---
Name: player | HP left: 65
Player resource left: 5 | Attack: 10
Name: enemy1 | HP left: 30
----------------------

Enter your choice (1-4): 4
--- Current Status ---
Name: player | HP left: 60
Player resource left: 5 | Attack: 10
Name: enemy1 | HP left: 30
----------------------

Enter your choice (1-4): 4
--- Current Status ---
Name: player | HP left: 55
Player resource left: 5 | Attack: 10
Name: enemy1 | HP left: 30
----------------------

Enter your choice (1-4): 4
--- Current Status ---
Name: player | HP left: 50
Player resource left: 5 | Attack: 10
Name: enemy1 | HP left: 30
----------------------

Enter your choice (1-4): 4
--- Current Status ---
Name: player | HP left: 45
Player resource left: 5 | Attack: 10
Name: enemy1 | HP left: 30
----------------------

Enter your choice (1-4): 4
--- Current Status ---
Name: player | HP left: 40
Player resource left: 5 | Attack: 10
Name: enemy1 | HP left: 30
----------------------

Enter your choice (1-4): 4
--- Current Status ---
Name: player | HP left: 35
Player resource left: 5 | Attack: 10
Name: enemy1 | HP left: 30
----------------------

Enter your choice (1-4): 4
--- Current Status ---
Name: player | HP left: 30
Player resource left: 5 | Attack: 10
Name: enemy1 | HP left: 30
----------------------

Enter your choice (1-4): 4
--- Current Status ---
Name: player | HP left: 25
Player resource left: 5 | Attack: 10
Name: enemy1 | HP left: 30
----------------------

Enter your choice (1-4): 4
--- Current Status ---
Name: player | HP left: 20
Player resource left: 5 | Attack: 10
Name: enemy1 | HP left: 30
----------------------

Enter your choice (1-4): 4
--- Current Status ---
Name: player | HP left: 15
Player resource left: 5 | Attack: 10
Name: enemy1 | HP left: 30
----------------------

Enter your choice (1-4): 4
--- Current Status ---
Name: player | HP left: 10
Player resource left: 5 | Attack: 10
Name: enemy1 | HP left: 30
----------------------

Enter your choice (1-4): 4
--- Current Status ---
Name: player | HP left: 5
Player resource left: 5 | Attack: 10
Name: enemy1 | HP left: 30
----------------------

Enter your choice (1-4): 4
--- Current Status ---
Name: player | HP left: 0
Player resource left: 5 | Attack: 10
Name: enemy1 | HP left: 30
----------------------
game over, you lose :(
No reward offered. canClaimReward: false
```

### Boundary scenarios: T1, T2, T9
These use `new Game(HP, resource, enemy1HP, resource2, enemy2HP)` to start from an exact state.

```text
=========================================
  T1: RECOVERY FROM HP 90
=========================================
canClaimReward before fight: false
--- Current Status ---
Name: player | HP left: 90
Player resource left: 5 | Attack: 10
Name: enemy1 | HP left: 10
----------------------

Enter your choice (1-4): 1
player attack enemy
Enemy 1 defeated!
--- Current Status ---
Name: player | HP left: 90
Player resource left: 5 | Attack: 10
Name: enemy1 | HP left: 0
----------------------

=== VICTORY IN ENCOUNTER 1! CHOOSE YOUR REWARD ===
1. RECOVERY (Restore 25 HP, capped at 100)
2. POWER (Permanently increase basic Attack by 2)
Select reward (1 or 2): RECOVERY applied!
Enemy 2 appears with 45 HP!
canClaimReward after reward: false

--- Encounter 2 vs Enemy 2 ---
--- Current Status ---
Name: player | HP left: 100
Player resource left: 5 | Attack: 10
Name: enemy2 | HP left: 45
----------------------
Input ended.

=========================================
  T2: RECOVERY AT HP 100
=========================================
canClaimReward before fight: false
--- Current Status ---
Name: player | HP left: 100
Player resource left: 5 | Attack: 10
Name: enemy1 | HP left: 10
----------------------

Enter your choice (1-4): 1
player attack enemy
Enemy 1 defeated!
--- Current Status ---
Name: player | HP left: 100
Player resource left: 5 | Attack: 10
Name: enemy1 | HP left: 0
----------------------

=== VICTORY IN ENCOUNTER 1! CHOOSE YOUR REWARD ===
1. RECOVERY (Restore 25 HP, capped at 100)
2. POWER (Permanently increase basic Attack by 2)
Select reward (1 or 2): RECOVERY applied!
Enemy 2 appears with 45 HP!
canClaimReward after reward: false

--- Encounter 2 vs Enemy 2 ---
--- Current Status ---
Name: player | HP left: 100
Player resource left: 5 | Attack: 10
Name: enemy2 | HP left: 45
----------------------
Input ended.

=========================================
  T9: FINISHING ATTACK ON ENEMY 2
=========================================
canClaimReward before fight: false
--- Current Status ---
Name: player | HP left: 100
Player resource left: 5 | Attack: 10
Name: enemy1 | HP left: 10
----------------------

Enter your choice (1-4): 1
player attack enemy
Enemy 1 defeated!
--- Current Status ---
Name: player | HP left: 100
Player resource left: 5 | Attack: 10
Name: enemy1 | HP left: 0
----------------------

=== VICTORY IN ENCOUNTER 1! CHOOSE YOUR REWARD ===
1. RECOVERY (Restore 25 HP, capped at 100)
2. POWER (Permanently increase basic Attack by 2)
Select reward (1 or 2): RECOVERY applied!
Enemy 2 appears with 10 HP!
canClaimReward after reward: false

--- Encounter 2 vs Enemy 2 ---
--- Current Status ---
Name: player | HP left: 100
Player resource left: 5 | Attack: 10
Name: enemy2 | HP left: 10
----------------------

Enter your choice (1-4): 1
player attack enemy
--- Current Status ---
Name: player | HP left: 100
Player resource left: 5 | Attack: 10
Name: enemy2 | HP left: 0
----------------------
game over, you won!!
```

## Responsible AI Use
**Prompt used for review** (replace with the prompt you actually used):
"Review the contracts for my two-encounter Java adventure. Find cases where a reward could be
offered incorrectly or violate a state invariant. Suggest tests; do not produce a replacement
architecture."

- **Useful suggestion:** The AI pointed out that my first loss scenario was wrong: `Defend` costs only
  5 HP, so 10 inputs did not kill the player. I changed the scenario to 20 inputs, and the loss
  trace now ends with "game over, you lose".
- **Rejected suggestion:** Adding precondition validation inside the supplier methods
  (`receiveRecovery` / `receivePower`). I rejected it because the lecture's Golden Rule says a
  method must not check its own preconditions; the client checks `canClaimReward()` instead.
- **Test I verified independently:** I ran Scenario 3 and confirmed that the invalid reward input
  printed an error and that HP (80), resource (2) and Attack (10) stayed the same until POWER was
  chosen, after which Attack became 12.
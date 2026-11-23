# Java & OOP Adventure — Profiles, Contracts, Invariants & Rewards

**Theme:** Fantasy battle adventure with multiple hero profiles (`Standard`, `Guardian`, `Striker`). RECOVERY = healing, POWER = stronger basic attack.

## Environment & Run Instructions
JDK 25.

```bash
mkdir -p build
javac -d build src/main/java/edu/starter/*.java
java -cp build edu.starter.Main
# automated scenarios including profiles:
java -cp build edu.starter.SimulationRunner
```

## Design Principles & Profiles
- **Contracts in suppliers:** `Combatant`, `Player` and `Game` document `@pre`/`@post` in Javadoc
  and never re-check their own preconditions (Golden Rule).
- **Checks in clients:** `Main` and the client code call `canSpecial()` before `special()` and `canClaimReward()`
  before `reward()`, verifying postconditions and invariants with `Checks.that`.
- **Character Profiles:** 
  - **Standard:** 100 max HP, Attack: 10, Defend block: 5.
  - **Guardian:** 130 max HP, Attack: 8, Defend block: 7 (heals capped at 130).
  - **Striker:** 80 max HP, Attack: 12, Defend block: 5 (heals capped at 80).
- **Invariants:** `takeDamage` and `heal` keep HP bounded by profile maximums (`0..MAX_HP`), `recover` keeps resource at most 10,
  `spendResource` relies on its `@pre` (checked by `canSpecial()`). Attack never decreases.
- **State changes** go through focused methods (`takeDamage`, `heal`, `spendResource`,
  `recover`, `applyReward`); there is no arbitrary setter for HP or resource.
  Fields are `protected`.
- **Enum:** rewards are `Reward { RECOVERY, POWER }`, applied by `Player.applyReward`.
- **Input robustness:** `ConsoleChoice` returns -1 for invalid input; if input ends (EOF),
  the program exits cleanly without starting another encounter.

## Design Note
State ownership: Combatant and Player own HP, resource, attack damage, and the rewardClaimed flag.
Game owns encounter sequencing, dynamic defense values per profile, and the reward gate. Contracts live in suppliers as @pre/@post
Javadoc, and the client meets them.

Invariants: $0 \le \text{HP} \le \text{maxHP}$ and $0 \le \text{resource} \le 10$ after every action; attack never decreases.
Main verifies them with `Checks.that` after each action.

Reward: the precondition is `canClaimReward()`: Enemy 1 defeated, player alive, no reward claimed yet.
Recovery sets HP = $\min(\text{maxHP}, \text{old HP} + 25)$, leaving resource and attack unchanged. Power adds 2 to
attack, leaving HP and resource unchanged. Special stays 15 as a constant in Game.
Invalid reward input prints an error and re-prompts without changing state. `rewardClaimed` guarantees
uniqueness. The same Player object continues into Encounter 2 against a fresh 45-HP enemy.
On a loss, Main returns immediately and no reward is offered.

## Acceptance Tests
Evidence comes from `SimulationRunner`, including tests for custom profiles (`Guardian` and `Striker`).

| ID | Expected | Actual (from the traces below) |
|----|----------|--------------------------------|
| T1 | Recovery from 90 → 100, resource/Attack unchanged | Scenario "T1": HP 90 → 100, resource 5, Attack 10 |
| T2 | Recovery at 100 → 100, reward consumed | Scenario "T2": HP stays 100, `canClaimReward after reward: false` |
| T3 | Power → Attack 12, Special still 15 | Scenario 1: Attack 10 → 12, HP and resource unchanged. |
| T4 | Invalid input, state unchanged, then Power once | Scenario 3: `Invalid reward choice!`, then `POWER applied!`, HP 80 / resource 2 / Attack 12 |
| T5 | No reward before victory | Every scenario: `canClaimReward before fight: false` |
| T6 | No second reward | Every scenario: `canClaimReward after reward: false` |
| T7 | Loss → no reward, no second battle | Scenario 4: `game over, you lose :(`, `No reward offered` |
| T8 | HP 80 / resource 2 persist into Encounter 2 | Scenarios 1 and 2: HP 80 / res 2 (Power) and HP 100 / res 2 (Recovery) |
| T9 | Finishing attack on Enemy 2, no counterattack | Scenario "T9": Enemy 2 reaches 0 HP, player HP stays 100, `game over, you won!!` |
| P1 | Guardian profile initialization & custom block | Guardian Scenario: Initial HP 130, Attack 8, Block 7 |
| P2 | Striker profile initialization & high attack | Striker Scenario: Initial HP 80, Attack 12, Block 5 |

## Traces
Output of `java -cp build edu.starter.SimulationRunner`.

### Scenario 1: Victory with POWER reward
```text
=========================================
  SCENARIO 1 (POWER)
=========================================
canClaimReward before fight: false
--- Current Status ---
Name: player | HP left: 100
Player resource left: 5 | Power of Attack: 10
Name: enemy1 | HP left: 30
----------------------

Enter your choice (1-4): 2
player attack enemy
enemy attack player
--- Current Status ---
Name: player | HP left: 90
Player resource left: 2 | Power of Attack: 10
Name: enemy1 | HP left: 15
----------------------

Enter your choice (1-4): 1
player attack enemy
enemy attack player
--- Current Status ---
Name: player | HP left: 80
Player resource left: 2 | Power of Attack: 10
Name: enemy1 | HP left: 5
----------------------

Enter your choice (1-4): 1
player attack enemy
Enemy 1 defeated!
--- Current Status ---
Name: player | HP left: 80
Player resource left: 2 | Power of Attack: 10
Name: enemy1 | HP left: 0
----------------------

=== VICTORY IN ENCOUNTER 1! CHOOSE YOUR REWARD ===
1. RECOVERY (Restore 25 HP, capped)
2. POWER (Permanently increase basic Attack by 2)
Select reward (1 or 2): POWER applied!
Enemy 2 appears with 45 HP!
canClaimReward after reward: false

--- Encounter 2 vs Enemy 2 ---
--- Current Status ---
Name: player | HP left: 80
Player resource left: 2 | Power of Attack: 12
Name: enemy2 | HP left: 45
----------------------
Input ended.
```

### Guardian Scenario: Profile Test
```text
=========================================
  GUARDIAN SCENARIO
=========================================
canClaimReward before fight: false
--- Current Status ---
Name: Guardian | HP left: 130
Player resource left: 5 | Power of Attack: 8
Name: enemy1 | HP left: 30
----------------------

Enter your choice (1-4): 2
player attack enemy
enemy attack player
--- Current Status ---
Name: Guardian | HP left: 120
Player resource left: 2 | Power of Attack: 8
Name: enemy1 | HP left: 15
----------------------

Enter your choice (1-4): 1
player attack enemy
enemy attack player
--- Current Status ---
Name: Guardian | HP left: 110
Player resource left: 2 | Power of Attack: 8
Name: enemy1 | HP left: 7
----------------------

Enter your choice (1-4): 1
player attack enemy
Enemy 1 defeated!
--- Current Status ---
Name: Guardian | HP left: 110
Player resource left: 2 | Power of Attack: 8
Name: enemy1 | HP left: 0
----------------------

=== VICTORY IN ENCOUNTER 1! CHOOSE YOUR REWARD ===
1. RECOVERY (Restore 25 HP, capped)
2. POWER (Permanently increase basic Attack by 2)
Select reward (1 or 2): POWER applied!
Enemy 2 appears with 45 HP!
canClaimReward after reward: false

--- Encounter 2 vs Enemy 2 ---
--- Current Status ---
Name: Guardian | HP left: 110
Player resource left: 2 | Power of Attack: 10
Name: enemy2 | HP left: 45
----------------------
Input ended.
```

### Striker Scenario: Profile Test
```text
=========================================
  STRIKER SCENARIO
=========================================
canClaimReward before fight: false
--- Current Status ---
Name: Striker | HP left: 80
Player resource left: 5 | Power of Attack: 12
Name: enemy1 | HP left: 30
----------------------

Enter your choice (1-4): 2
player attack enemy
enemy attack player
--- Current Status ---
Name: Striker | HP left: 70
Player resource left: 2 | Power of Attack: 12
Name: enemy1 | HP left: 15
----------------------

Enter your choice (1-4): 1
player attack enemy
enemy attack player
--- Current Status ---
Name: Striker | HP left: 60
Player resource left: 2 | Power of Attack: 12
Name: enemy1 | HP left: 3
----------------------

Enter your choice (1-4): 1
player attack enemy
Enemy 1 defeated!
--- Current Status ---
Name: Striker | HP left: 60
Player resource left: 2 | Power of Attack: 12
Name: enemy1 | HP left: 0
----------------------

=== VICTORY IN ENCOUNTER 1! CHOOSE YOUR REWARD ===
1. RECOVERY (Restore 25 HP, capped)
2. POWER (Permanently increase basic Attack by 2)
Select reward (1 or 2): POWER applied!
Enemy 2 appears with 45 HP!
canClaimReward after reward: false

--- Encounter 2 vs Enemy 2 ---
--- Current Status ---
Name: Striker | HP left: 60
Player resource left: 2 | Power of Attack: 14
Name: enemy2 | HP left: 45
----------------------
Input ended.
```

## Responsible AI Use
**Prompt used for review:**
"Review the contract updates for adding Guardian and Striker hero profiles to my battle adventure. Ensure state invariants and dynamic defense blocks are properly checked and maintained."

- **Useful suggestion:** The AI recommended implementing dynamic maximum HP caps for recovery per profile (e.g., 130 for Guardian, 80 for Striker) rather than hardcoding 100 everywhere.
- **Rejected suggestion:** Hardcoding profile logic inside the base Player class. I kept it extensible through inheritance (`Guardian` and `Striker` overriding methods).
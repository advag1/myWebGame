let selectedProfile = "standard";
const gameId = Math.random().toString(36).substring(2, 10);
let prevPlayerHP = 100;
let prevEnemyHP = 30;
let isAnimating = false;

function selectProfile(profile) {
    selectedProfile = profile;
    document.getElementById("profile-select-panel").classList.add("hidden");
    document.getElementById("game-container").classList.remove("hidden");
    document.getElementById("battle-log").textContent = "";

    // החלפת דמות השחקן בזירה לפי הבחירה
    document.getElementById("svg-player-standard").classList.toggle("hidden", profile !== "standard");
    document.getElementById("svg-player-guardian").classList.toggle("hidden", profile !== "guardian");
    document.getElementById("svg-player-striker").classList.toggle("hidden", profile !== "striker");

    const titleMap = {
        standard: "Standard (you)",
        guardian: "Guardian (you)",
        striker: "Striker (you)"
    };
    document.getElementById("player-name-text").textContent = titleMap[profile] || "Player (you)";

    prevPlayerHP = profile === "guardian" ? 130 : (profile === "striker" ? 80 : 100);
    prevEnemyHP = 30;
    sendAction("new");
}

function spawnFloatingText(targetBoxId, text, colorClass) {
    const box = document.getElementById(targetBoxId);
    const el = document.createElement("div");
    el.className = "floating-text " + colorClass;
    el.textContent = text;
    box.appendChild(el);
    setTimeout(() => el.remove(), 950);
}

function triggerClassAnim(elementId, className, duration = 450) {
    const el = document.getElementById(elementId);
    el.classList.remove(className);
    void el.offsetWidth;
    el.classList.add(className);
    setTimeout(() => el.classList.remove(className), duration);
}

function playBattleAnimations(action, data) {
    const enemyDmg = Math.max(0, prevEnemyHP - data.enemyHP);
    const playerDmg = Math.max(0, prevPlayerHP - data.playerHP);

    if (action === "attack") {
        triggerClassAnim("player-sprite", "lunge-right", 400);
        triggerClassAnim("slash-fx", "show-fx", 350);
        setTimeout(() => {
            triggerClassAnim("enemy-sprite", "hit-shake", 400);
            if (enemyDmg > 0) spawnFloatingText("enemy-box", "-" + enemyDmg, "dmg-red");
        }, 150);

        if (data.enemyHP > 0 && playerDmg > 0) {
            setTimeout(() => {
                triggerClassAnim("enemy-sprite", "lunge-left", 400);
                setTimeout(() => {
                    triggerClassAnim("player-sprite", "hit-shake", 400);
                    spawnFloatingText("player-box", "-" + playerDmg, "dmg-red");
                }, 150);
            }, 500);
        }
    } else if (action === "special") {
        triggerClassAnim("player-sprite", "lunge-right", 400);
        triggerClassAnim("fireball-fx", "shoot-fireball", 450);
        setTimeout(() => {
            triggerClassAnim("enemy-sprite", "hit-shake", 450);
            if (enemyDmg > 0) spawnFloatingText("enemy-box", "-" + enemyDmg + "!", "dmg-orange");
        }, 280);

        if (data.enemyHP > 0 && playerDmg > 0) {
            setTimeout(() => {
                triggerClassAnim("enemy-sprite", "lunge-left", 400);
                setTimeout(() => {
                    triggerClassAnim("player-sprite", "hit-shake", 400);
                    spawnFloatingText("player-box", "-" + playerDmg, "dmg-red");
                }, 150);
            }, 650);
        }
    } else if (action === "recover") {
        spawnFloatingText("player-box", "+3 ⚡", "heal-blue");
        if (playerDmg > 0) {
            setTimeout(() => {
                triggerClassAnim("enemy-sprite", "lunge-left", 400);
                setTimeout(() => {
                    triggerClassAnim("player-sprite", "hit-shake", 400);
                    spawnFloatingText("player-box", "-" + playerDmg, "dmg-red");
                }, 150);
            }, 400);
        }
    } else if (action === "defend") {
        const shield = document.getElementById("player-shield");
        shield.classList.remove("hidden");
        triggerClassAnim("enemy-sprite", "lunge-left", 400);
        setTimeout(() => {
            triggerClassAnim("player-sprite", "hit-shake", 300);
            spawnFloatingText("player-box", "🛡️ -" + playerDmg, "dmg-blue");
        }, 150);
        setTimeout(() => shield.classList.add("hidden"), 650);
    } else if (action === "reward1") {
        spawnFloatingText("player-box", "+25 HP ❤️", "heal-green");
    } else if (action === "reward2") {
        spawnFloatingText("player-box", "+2 ATK ⚔️", "heal-green");
    }
}

async function sendAction(action) {
    if (isAnimating) return;
    isAnimating = true;

    try {
        const response = await fetch(`/api?gameId=${gameId}&profile=${selectedProfile}&action=` + action);
        const data = await response.json();

        playBattleAnimations(action, data);

        document.getElementById("player-hp-text").textContent = `${data.playerHP}/${data.playerMaxHP}`;
        document.getElementById("player-hp-bar").style.width = `${(data.playerHP / data.playerMaxHP) * 100}%`;

        document.getElementById("player-res-text").textContent = `${data.playerResource}/${data.playerMaxResource}`;
        document.getElementById("player-res-bar").style.width = `${(data.playerResource / data.playerMaxResource) * 100}%`;

        document.getElementById("player-atk-text").textContent = data.playerAttack;

        const isEnemy1 = data.enemyName === "enemy1";
        document.getElementById("svg-enemy1").classList.toggle("hidden", !isEnemy1);
        document.getElementById("svg-enemy2").classList.toggle("hidden", isEnemy1);

        document.getElementById("enemy-name").textContent = data.enemyName;
        document.getElementById("enemy-rank").textContent = isEnemy1 ? "שלב 1" : "בוס";
        document.getElementById("enemy-hp-text").textContent = `${data.enemyHP}/${data.enemyMaxHP}`;
        document.getElementById("enemy-hp-bar").style.width = `${(data.enemyHP / data.enemyMaxHP) * 100}%`;

        document.getElementById("encounter-badge").textContent =
            isEnemy1 ? "שלב 1 מתוך 2" : "שלב 2 מתוך 2";

        document.getElementById("player-sprite").classList.toggle("defeated", data.playerHP <= 0);
        document.getElementById("enemy-sprite").classList.toggle("defeated", data.enemyHP <= 0);

        if (data.logs) {
            const logBox = document.getElementById("battle-log");
            logBox.textContent += data.logs;
            logBox.scrollTop = logBox.scrollHeight;
        }

        const actionsPanel = document.getElementById("actions-panel");
        const rewardPanel = document.getElementById("reward-panel");
        const gameoverPanel = document.getElementById("gameover-panel");

        if (data.isGameOver) {
            actionsPanel.classList.add("hidden");
            rewardPanel.classList.add("hidden");
            gameoverPanel.classList.remove("hidden");
            document.getElementById("gameover-message").textContent =
                data.playerLost ? "הפסדת במשחק! (Game Over)" : "ניצחת את כל האויבים! (Victory!)";
        } else if (data.canClaimReward) {
            actionsPanel.classList.add("hidden");
            rewardPanel.classList.remove("hidden");
            gameoverPanel.classList.add("hidden");
        } else {
            actionsPanel.classList.remove("hidden");
            rewardPanel.classList.add("hidden");
            gameoverPanel.classList.add("hidden");
            document.getElementById("btn-special").disabled = !data.canSpecial;
        }

        prevPlayerHP = data.playerHP;
        prevEnemyHP = data.enemyHP;
    } finally {
        setTimeout(() => { isAnimating = false; }, 450);
    }
}

function resetGame() {
    isAnimating = false;
    document.getElementById("battle-log").textContent = "";
    prevPlayerHP = 100;
    prevEnemyHP = 30;
    document.getElementById("game-container").classList.add("hidden");
    document.getElementById("profile-select-panel").classList.remove("hidden");
}
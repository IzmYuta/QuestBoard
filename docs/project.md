### 1️⃣ クエストボードを開くショートカットボタン

| 項目           | 推奨実装                                                               | 補足                                                                            |
| ------------ | ------------------------------------------------------------------ | ----------------------------------------------------------------------------- |
| **キー割当**     | `R` キー（リロード系と被りにくい）                                                | *Fabric API* の `KeyBindingHelper` で登録し、`ClientTickEvents.END_CLIENT_TICK` で検知 |
| **GUI 呼び出し** | `MinecraftClient.getInstance().setScreen(new QuestBoardScreen());` | 最初は *ChestScreen* 風の 9×3 グリッドに “進行中”／“未受注” を配置すると手早い                          |
| **音・演出**     | 開くとき `SoundEvents.UI_TOAST_IN` を再生、アイテム受領時は花火のパーティクル               | 視覚・聴覚で “ご褒美感” を追加                                                             |

---

### 2️⃣ クエストの管理形式

#### ● ファイル単位

```
/config/questboard/quests/
  ├─ daily/
  │    └─ 2025-05-17.json      ← 日替わり
  ├─ story/
  │    └─ chapter1.json        ← 連続クエスト
  └─ repeatable/
       └─ kill_zombie.json     ← 周回クエ
```

#### ● JSON フォーマット例

```jsonc
{
  "id": "kill_zombie",
  "title": "ゾンビ討伐",
  "description": "ゾンビを§b10体§r倒す",
  "type": "REPEATABLE",
  "goal": { "action": "KILL_ENTITY", "target": "minecraft:zombie", "count": 10 },
  "rewards": [
    { "type": "POINT", "amount": 50 },
    { "type": "ITEM",  "id": "minecraft:iron_ingot", "count": 2 }
  ],
  "cooldown": 1800   // 秒。0 なら無制限周回
}
```

*ポイント*

* **国際化を考慮**して `"title"` と `"description"` を lang キーにしても OK
* 起動時に全ファイルをパースし **`QuestRegistry`** へ格納
* 将来は **SQLLite → MySQL** へ載せ替えやすいよう、内部では `Quest` オブジェクトへマッピングして扱う

---

### 3️⃣ ポイント・アイテムの利用方法

| 利用シーン        | 実装メモ                                                                                     |
| ------------ | ---------------------------------------------------------------------------------------- |
| **プレイヤー間送金** | `/pay <player> <amount>` コマンド。権限は `permission: quest.pay`                                |
| **NPC／村人交換** | `VillagerTradesEvents` で交換リスト拡張。<br>取引 UI 内アイテムを “エメラルド” の代わりに “ポイント券” の仮想アイテムへ置換        |
| **サーバーショップ** | `/questshop` GUI で常設商品。価格は `shops.yml` で編集可能                                             |
| **消費ギミック**   | - `/bounty <mob> <bet>` で懸賞金設定→他プレイヤーが討伐すると獲得<br>- 視覚演出：ポイント増減時に `ActionBar` へ `+50Ｐ` 表示 |

---

### 4️⃣ Web API（進捗確認）

| 項目             | 推奨実装                                                                                                  | 参考                                            |
| -------------- | ----------------------------------------------------------------------------------------------------- | --------------------------------------------- |
| **HTTP ライブラリ** | *Blue Lightning WebServer* または *NanoHTTPD*（Jar 1 本で済む）                                                | シングルスレッドでも十分軽い                                |
| **エンドポイント**    | `GET /api/player/{uuid}` → <br>`{ "points":123, "quests":[{"id":"kill_zombie","state":"COMPLETE"}] }` | 認証は最初は無しでローカルのみ。将来 JWT・APIKey                 |
| **デプロイ**       | サーバー jar 内で `new HttpServer(8080)` 起動<br>→ reverse proxy で HTTPS                                      | `/api/leaderboard` でランキング JSON を返すと外部サイト表示が簡単 |

---

### 5️⃣ プレイヤーを楽しませる＋α 施策

| 施策             | 内容                                             | 実装負荷  |
| -------------- | ---------------------------------------------- | ----- |
| **連続ログインボーナス** | 24h 内初ログイン時に `Streak` 加算→追加ポイント                | ★★☆☆☆ |
| **シーズンパス**     | 30日ごとに総ポイントをリセットし、トップ3 へ特製称号（Advancement）      | ★★★☆☆ |
| **協力クエスト**     | サーバー全体で「エンダーマン1,000体討伐」など。貢献度で分配               | ★★★★☆ |
| **視覚化**        | `/leader` コマンドで頭スキン付きランキングを scoreboard に表示     | ★★☆☆☆ |
| **ガチャ**        | `gacha.yml` に排出テーブル→ `/gacha` で 100P 消費しランダム報酬 | ★★☆☆☆ |

---

## “まず2時間で動かす” 最小セットアップ手順（ざっくり）

1. **プロジェクト雛形作成**

   ```bash
   loom setup fabric-1.20.5+0.16.0
   ```

2. **PointManager**

   * `Map<UUID,Integer> balances` を `ServerPlayerEntity#getPersistentData` と同期
   * `/pay` コマンド & `QuestRewardHandler` だけ書く

3. **QuestRegistry & JSON Loader**

   * `/config/questboard/` を走査し `Gson` で読み込み
   * `HashMap<String, Quest>` に保持

4. **Kill イベントだけ登録**

   * `AttackEntityCallback` で `if (target instanceof ZombieEntity)` →進捗++
   * `checkCompletion()` 後に PointManager.add(player, rewardPoint)

5. **GUI 省略でチャット通知**

   * 完了時 `player.sendMessage(Text.of("[Quest] ゾンビ討伐クリア! +50P"))`

これで **「ポイントが貯まる / 送金できる / キル系クエストが回る」** の骨格が完成。
その後 **キー開幕 GUI** → **村人交換** → **WebAPI** と段階投入すれば無理なく拡張できます。

---

🎉 **試しにゾンビ10体討伐して、/pay で友達に 25P 送れるところまで** が 2 時間ゴールのイメージです。
楽しんで開発してください！

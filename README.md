# PointPlugin

Paper サーバー向けのポイント管理プラグインです。  
プレイヤーは

- 実績の達成
- 1日1回のログイン
- ポイントショップの利用

を通してポイントを集めたり使ったりできます。

この README は、実際のコードを読んだ内容に合わせて、初心者向けに整理した説明です。

## できること

このプラグインには、主に次の機能があります。

### 1. 実績を達成するとポイントがもらえる

`config.yml` の `advancement-rewards` に書かれた実績を達成すると、ポイントが加算されます。  
同じ実績で何度も受け取ることはできません。

例:

```yml
advancement-rewards:
  "minecraft:story/mine_diamond": 1
```

この設定なら、ダイヤモンドを初めて入手する実績で `100` ポイントもらえます。

### 2. デイリーログインボーナス

プレイヤーがその日に初めてログインしたとき、ポイントを付与します。  
日付の判定は **日本時間 (Asia/Tokyo)** です。

例:

```yml
daily-login:
  enabled: true
  points: 50
```

この設定なら、1日1回 `50` ポイントもらえます。

### 3. ポイントショップ

看板を使って、ポイントでアイテムや効果を購入できるショップを作れます。

購入できるものは `config.yml` の `shop` に定義します。  
現在のコードでは、次の3種類に対応しています。

- `item`: アイテムを渡す
- `potion`: ポーション効果を付与する
- `login_bonus_boost`: 今後のログインボーナスを増やす

設定例:

```yml
shop:
  DIAMOND:
    type: item
    material: DIAMOND
    amount: 1

  SPEED_BUFF:
    type: potion
    effect: SPEED
    duration: 1200
    amplifier: 1

  LOGIN_UP:
    type: login_bonus_boost
    increase: 5
```

### 4. ランキング表示

`/ranking` コマンドで、ポイント上位 10 人を表示できます。  
自分が 11 位以下なら、自分の順位もあわせて表示されます。

## 必要環境

- Java 21
- Paper 1.21 系
- Maven 3.x（ビルドする場合）

`pom.xml` では `paper-api 1.21.4-R0.1-SNAPSHOT` が指定されています。

## 導入方法

### サーバーに入れるだけで使う場合

1. `jar` をビルドします
2. できた `jar` を Paper サーバーの `plugins` フォルダに入れます
3. サーバーを起動します
4. `plugins/PointPlugin/` に設定ファイルが生成されます

### ビルド方法

プロジェクトのルートで次を実行します。

```bash
mvn package
```

ビルド後の `jar` は通常 `target/` に出力されます。

## コマンド

### `/ranking`

ポイントランキングを表示します。

## 権限

### `pointpligin.admin`

ポイントショップ用の看板を作成できます。  
`plugin.yml` では `op` のみに許可されています。

注意:
権限名はコード上も `plugin.yml` 上も **`pointpligin.admin`** です。  
`pointplugin.admin` ではないので、権限プラグインで設定するときはスペルに注意してください。

## ポイントショップの作り方

権限 `pointpligin.admin` を持つプレイヤーが、看板の 1 行目に次を書いて設置します。

```text
[PointShop]
```

設置後、プラグインがその看板をショップ看板として登録します。  
購入時には、看板の 2〜4 行目を次のように読み取ります。

### 看板の書式

```text
1行目: [PointShop]
2行目: 商品キー [個数]
3行目: 価格
4行目: 説明文（任意）
```

例:

```text
[PointShop]
DIAMOND 3
150
ダイヤ3個
```

この例では、`config.yml` の `shop.DIAMOND` を 3 個分、`150` ポイントで購入します。

### 購入時の動き

- 1回目の右クリックで確認状態に入る
- 5 秒以内にもう一度右クリックすると購入する
- ポイントが足りない場合は購入できない
- アイテムがインベントリに入りきらない場合は購入失敗になり、消費したポイントは戻る

また、登録されたショップ看板の位置は `shops.yml` に保存されます。  
その看板を壊すと、登録情報も削除されます。

## 設定ファイル

### `config.yml`

主な設定は次の 3 つです。

- `advancement-rewards`: 実績ごとの報酬ポイント
- `daily-login`: デイリーログインボーナス
- `shop`: ショップ商品の定義

### `players.yml`

プレイヤーごとの補助データを保存します。

- 最後にログインボーナスを受け取った日
- 報酬を受け取った実績
- ログインボーナスの追加値

### `shops.yml`

ポイントショップとして登録された看板の座標を保存します。

## ポイントの保存方法

このプラグインのポイントは、Paper の **メインスコアボード** にある `points` という Objective に保存されます。  
そのため、ポイントそのものは `players.yml` ではなく、スコアボード側で管理されています。

内部的には次のような処理です。

- ポイント名: `points`
- 存在しなければ自動作成
- プレイヤー名をキーとしてスコアを保存

## 初心者向けの設定例

```yml
advancement-rewards:
  "minecraft:story/mine_diamond": 1
  "minecraft:nether/root": 2

daily-login:
  enabled: true
  points: 50

shop:
  APPLE:
    type: item
    material: APPLE
    amount: 5

  SPEED:
    type: potion
    effect: SPEED
    duration: 1200
    amplifier: 1

  LOGIN_PLUS:
    type: login_bonus_boost
    increase: 10
```

この設定なら、

- ダイヤ実績で 100 ポイント
- ネザー到達で 200 ポイント
- 毎日ログインで 50 ポイント
- リンゴ、移動速度アップ、ログインボーナス強化をショップで販売

という形になります。

## コード構成

初心者向けに、クラスごとの役割もまとめておきます。

- `Main.java`: プラグイン起動時の初期化
- `PointManager.java`: ポイントの加算・減算・取得
- `RankingCommand.java`: `/ranking` の処理
- `AdvancementListener.java`: 実績達成時の報酬処理
- `LoginBonusListener.java`: ログインボーナス処理
- `ShopListener.java`: ショップ看板の作成と購入処理
- `DataStorage.java`: `players.yml` の読み書き

## 補足

- ログインボーナスの増加値 (`login_bonus_boost`) は、購入するたびに加算されます
- ショップの `potion` は、既に同じ効果がある場合にレベルや持続時間を調整して付与します
- 設定ミスがある商品は購入に失敗し、ポイントは返却されます

## ライセンス

必要ならここに追記してください。

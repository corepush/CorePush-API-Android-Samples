# Core Push API Android Samples

###  概要
<p>CORE PUSH APIを使用したサンプルプロジェクトを提供します。</p>
<p>本プロジェクトでは、以下のサンプルを提供しています。</p>
* トークン登録
  * <a href="http://developer.core-asp.com/api_token.php">事前登録API</a>を使用して、Androidのデバイストークンを登録するサンプルコード
* トークン削除
  * <a href="http://developer.core-asp.com/api_token.php">事前登録API</a>を使用して、Androidのデバイストークンを削除するサンプルコード
* 通知履歴取得
  * <a href="http://developer.core-asp.com/api_history.php">通知履歴取得API</a>を使用して、通知履歴を取得するサンプルコード

<p>各APIの詳細については、<a href="http://developer.core-asp.com/index.php">Core Push Developer Support</a>の<a href="http://developer.core-asp.com/api_token.php">CORE PUSH API</a>をご参照ください。</p>

### 前提
  * Androidのバージョン4.0以上で動作します。
  * Android Studio2.0を使用しています。
  * FCM SDKを使用しています。
  * CORE PUSH SDKは使用していません。
  * CORE PUSHの設定キーが必要になります。CORE PUSHの管理画面でアプリの設定キーをご確認できます。
  設定キーの設定については、<a href="#config_key">CORE PUSHの設定キーの設定</a>をご参照ください。
  
  * アプリのプロジェクト固有のgoogle-service.jsonが必要になります。Firebaseの管理画面でgoogle-service.jsonを取得してください。 google-service.jsonの取得については、<a href="#firebase_setting">Firebaseの設定</a>をご参照ください。
  * サンプルアプリに対して、通知を送信するにはプロジェクトのAuthKeyが必要になります。CORE PUSH管理画面にて、AuthKeyを登録してください。
  
  
### <div id="firebase_setting">Firebaseの設定</div>
  * <a href="https://console.firebase.google.com">Firebaseの管理画面</a>
  で「新規プロジェクトの作成」 あるいは「Googleプロジェクトのインポート」を行い、Firebase用の
  プロジェクトを作成します。
  * 「AndroidアプリにFirebaseを追加」のボタンをクリックし、追加の設定を行います。
    * 追加の設定画面では、アプリのパッケージ名を指定します。サンプルプロジェクトの例では、
    com.coreasp.api.android.samples がパッケージ名になります。
    * パッケージ名の指定後、google-package.jsonをローカルにダウンロードします。
  * サンプルプロジェクト内のappフォルダ配下のgoogle-package.jsonファイルをダウンロードしたjsonファイルで置換します。
    * サンプルプロジェクトのパッケージ名と異なるパッケージ名でFirebaseの追加設定を行った場合は、指定したパッケージ名に合わせて
    サンプルプロジェクトのパッケージ構成を変更してください。
    

### <div id="config_key">CORE PUSHの設定キーの設定</div>
* CORE PUSHの管理画面で確認したアプリの設定キーを res/values/strings.xml内の 
core_push_config_keyキーの値に指定してください。

```
<resources>
    <string name="app_name">CorePushSamples</string>
    <string name="core_push_config_key">xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx</string>
</resources>

```

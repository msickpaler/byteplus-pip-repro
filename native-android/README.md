# Androidの最小PiP検証

1つのActivity、1つのTTVideoEngine、1本の動画で、再生中と停止中のPiP復帰を検証します。React Native、本体の画面遷移、課金、連続再生は含みません。

## 起動

必要なものはJava 17、Android SDK 36、Android 8.0以降のarm64端末（またはarm64エミュレータ）です。BytePlusのPiP資料のサポート条件は実機のため、エミュレータの成功だけでは実機確認を完了扱いにしません。

1. リポジトリ直下の`local.config.example.json`を`local.config.json`へコピーします。既存のiOS用設定があれば、そのファイルに次の2項目を追加します。
   - `androidPackageName`: ライセンスで許可されたAndroidパッケージ名
   - `appName`: BytePlusのアプリ管理に登録したアプリ名
2. `appId`と`video`に有効な作品の情報を設定します。`video.auth`は暗号化動画の復号用情報です。暗号化されていない動画は空文字列にします。
3. リポジトリ直下に`license.lic`を置きます。
4. このディレクトリの`local.properties`に`sdk.dir=/absolute/path/to/Android/sdk`を設定するか、`ANDROID_HOME`を設定します。
5. `./gradlew :app:assembleDebug`でビルドし、対象端末を明示してインストールします。

```sh
adb -s <serial> install -r app/build/outputs/apk/debug/app-debug.apk
adb -s <serial> shell am start -n <androidPackageName>/com.example.byteplusrepro.PlayerActivity
```

ライセンスの都合で本体と同じパッケージ名を使う場合は、専用エミュレータを使ってください。既存アプリへの上書きやアンインストールで検証環境を壊さないようにします。

ライセンス、ローカル設定、生成物はGit管理対象外です。APKにはライセンスと署名付き再生URLが入るため、共有するのはソースコードだけにしてください。URLが失効した場合は設定を更新して再ビルドします。APIの認証情報をこのプロジェクトへ追加する必要はありません。

## 確認手順

1. `Play / Pause`で再生を開始し、ホームへ戻るとPiPで映像が進むことを確認します。
2. PiPをタップしてOSの全画面復帰を使い、同じ位置から再生が続くことを確認します。
3. 再びPiPへ移り、OSの一時停止を押してから全画面へ復帰します。停止場面・停止状態が維持されるか、暗転・拡縮があるかを確認します。
4. 停止したままホームへ戻っても自動PiPにならないこと、PiPを閉じると再生が止まることを確認します。

```sh
./gradlew :app:lintDebug :app:assembleDebugAndroidTest
ANDROID_SERIAL=<serial> ./gradlew :app:connectedDebugAndroidTest
```

計測テストは実際の作品を再生し、PiP中の再生時間の進行、同じエンジンでの再生中／停止中の復帰、停止中の自動PiP抑止を検証します。復帰にはActivityの再表示を使うため、OSの復帰ボタンや映像の暗転は別途画面録画で確認します。UiAutomatorを使う端末操作ツールと計測テストは同時に起動しないでください。

## 公式資料に基づく構成

- [BytePlus SDKの導入](https://docs.byteplus.com/en/docs/byteplus-vod/docs-integration-android): 本体と同じStandard SDK `1.48.200.1`とOkHttpを使用。内部キャッシュだけを使うため外部ストレージ権限は要求しません。検証用Debugビルドは難読化しません。
- [BytePlus Quickstart](https://docs.byteplus.com/en/docs/byteplus-vod/docs-getting-started-with-the-android-player-sdk): Applicationで初期化し、FrameLayout内のSurfaceViewを再生開始前にエンジンへ設定します。DirectUrlSourceとURLのMD5キャッシュキーを使います。
- [BytePlusのPiP実装](https://docs.byteplus.com/en/docs/byteplus-vos/docs-vod-picture-in-picture): ActivityのPiP宣言と構成変更の処理を使用します。
- [AndroidのPiP推奨設定](https://developer.android.com/develop/ui/views/picture-in-picture): Android 12以降は再生中だけ自動PiPを有効にし、動画の表示領域と縦横比を指定します。それ以前は`onUserLeaveHint`で移行します。停止は`onStop`で行い、PiP移行時の`onPause`では止めません。MediaSessionでOSの再生・停止を扱い、音声フォーカスを失うと停止します。Android 15以降は移行開始時にボタンを非表示にします。

## 2026-11-12の検証結果

- Android 16 / API 36、Pixel 8 Pro相当のarm64エミュレータで確認しました。既存の検証端末とは別の一時AVDを使用しました。
- ステージングの「7回の恋と1つの愛」第1話（360×640、非暗号化）を再生しました。保存済みのiOS用URLはHTTP 403だったため、既存APIから有効なURLを再取得しました。
- `assembleDebug`、`assembleDebugAndroidTest`、`lintDebug`が成功し、計測テストは1件成功しました。lintのエラーは0件です。SDK依存内のx86_64用ライブラリの16 KB警告などは残りますが、このAPKの対象ABIはarm64-v8aとarmeabi-v7aです。
- OSのPiP操作メニューで一時停止後、全画面へ復帰できました。MediaSessionの状態は復帰前後ともPAUSED、再生位置は24,951 msでした。
- 復帰区間を録画から8 fpsで抽出して確認した範囲では、停止フレームが表示されたまま拡大し、全画面への復帰後も同じフレームでした。取得したサンプル間の短い暗転までは否定できません。
- 実機、Android 8〜11の手動移行、暗号化作品、音声の実聴、本体のReact Native画面への組み込みは未検証です。

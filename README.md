# [DICOM Tag Classifier ImageJ](https://github.com/ramnoob/dicom-tag-classifier-ImageJ)

## 概要
DICOM Tag Classifier は、DICOM（Digital Imaging and Communications in Medicine）ファイルを ImageJ 環境内で直接分類する便利な方法を提供する Java コードで作成されたプラグインです。このプラグインを使用すると、ユーザーは DICOM タグの値に基づいてディレクトリ構造を作成し、ファイルを指定のディレクトリに整理することができます。

## 特徴
- **インタラクティブユーザーインターフェース**: 分類設定の設定や分類プロセスのモニタリングのために、ImageJ環境に統合された使いやすいGUI。
- **タグベースの分類**: 再構成関数、管電流、SeriesDescriptionなどのDICOMタグ値に基づいてディレクトリを作成し、DICOMファイルを分類する。
- **ファイル名変更**: DICOMタグの値に基づいてファイル名（XXX.DCM）を変更する。
- **範囲フィルタリング**: ImageNumberまたはSliceLocationの値に基づいて、指定された範囲内のファイルをフィルタリングする。
- **高度なフィルタリング**: ユーザーが指定した複数のDICOMタグに対して、ANDもしくはOR条件を使用してDICOMファイルをフィルタリングする。
- **ファイル管理**: 保存先ディレクトリに同名のファイルが既に存在する場合、ファイル操作を上書き、リネーム、キャンセルするオプション。

## 使い方
1. **インストール**: `DICOM_Tag_Classifier-X.X.X.jar`ファイルをダウンロードし、ImageJの `plugins` ディレクトリ内に配置する。以前のバージョンがある場合は削除してください。  
2. **ImageJの起動**: プラグインを読み込むために、ImageJを開くか、すでに起動している場合は再起動する。
3. **プラグインの有効化**: ImageJ の "Plugins "メニューに移動し、"DICOM Tag Classifier "を選択してプラグインを起動する。
4. **設定**: プラグインを使用して、ディレクトリ構造やフィルタリング条件などの分類設定を行う。
5. **分類**: Startボタンから分類を開始し、DICOMファイルを指定されたディレクトリに整理する。
6. **モニタリング**: 分類プロセスの進行状況を監視し、ImageJ 内でエラーまたは警告メッセージを表示する。
![DICOM Tag Classifier Configuration](https://github.com/ramnoob/dicom-tag-classifier-ImageJ/assets/70456441/6f23c20a-35c6-4617-b799-33b00c4323a2)

## 依存関係
- **ImageJ**: Javaベースの画像処理ソフトウェア。
- **Fiji**: ImageJに複数の便利なプラグインを追加したもの。このプラグインはこちら (https://fiji.sc/) で使用を推奨している。

## 貢献
Contributions are welcome! Please fork the repository, make your changes, and submit a pull request.  
貢献を歓迎します！リポジトリをフォークして変更を加え、プルリクエストを提出してください。

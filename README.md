# [DICOM Tag Classifier ImageJ](https://github.com/ramnoob/dicom-tag-classifier-ImageJ)

## Overview
DICOM Tag Classifier is a plugin created in Java code that provides a convenient way to classify DICOM (Digital Imaging and Communications in Medicine) files directly within the ImageJ environment. The plugin allows users to create a directory structure based on DICOM tag values and organize DICOM files into specified directories.  

DICOM Tag Classifier は、DICOM（Digital Imaging and Communications in Medicine）ファイルを ImageJ 環境内で直接分類する便利な方法を提供する Java コードで作成されたプラグインです。このプラグインを使用すると、ユーザーは DICOM タグの値に基づいてディレクトリ構造を作成し、ファイルを指定のディレクトリに整理することができます。

## Features
- **Interactive User Interface**: Easy-to-use graphical interface integrated into the ImageJ environment for configuring classification settings and monitoring the classification process.
- **Tag-Based Classification**: Create directories and classify DICOM files based on DICOM tag values such as reconstruction function, tube current, and SeriesDescription, etc.
- **Rename File**: Rename the file（XXX.DCM） based on the DICOMTag value.
- **Range Filtering**: Filters files within a specified numerical range based on ImageNumber or SliceLocation DICOM tags.
- **Advanced Filtering**: Filter DICOM files using advanced criteria with AND and OR conditions for multiple user-specified DICOM tags.
- **File Management**: Option to overwrite, rename, or cancel file operations when a file with the same name already exists in the destination directory.
- **インタラクティブユーザーインターフェース**: 分類設定の設定や分類プロセスのモニタリングのために、ImageJ環境に統合された使いやすいGUI。
- **タグベースの分類**: 再構成関数、管電流、SeriesDescriptionなどのDICOMタグ値に基づいてディレクトリを作成し、DICOMファイルを分類する。
- **ファイル名変更**: DICOMタグの値に基づいてファイル名（XXX.DCM）を変更する。
- **範囲フィルタリング**: ImageNumberまたはSliceLocationの値に基づいて、指定された範囲内のファイルをフィルタリングする。
- **高度なフィルタリング**: ユーザーが指定した複数のDICOMタグに対して、ANDもしくはOR条件を使用してDICOMファイルをフィルタリングする。
- **ファイル管理**: 保存先ディレクトリに同名のファイルが既に存在する場合、ファイル操作を上書き、リネーム、キャンセルするオプション。

## Usage
1. **Installation**: Download the `DICOM_Tag_Classifier-X.X.X.jar` file and place it in the ImageJ `plugins` directory. If you have a previous version, delete it.
2. **Launch ImageJ**: Open ImageJ or restart if it's already running to load the plugin.
3. **Activate Plugin**: Navigate to the "Plugins" menu in ImageJ and select "DICOM Tag Classifier" to activate the plugin.
4. **Configure**: Use the plug-in interface to configure classification settings such as directory structure, filtering criteria, etc.
5. **Classify**: Start the classification process from the plugin interface to organize DICOM files into the specified directories.
6. **Monitor**: Monitor the progress of the classification process and view any error or warning messages within ImageJ.

![DICOM Tag Classifier UI](https://github.com/ramnoob/dicom-tag-classifier-ImageJ/assets/70456441/d9486ff1-e26c-4c71-bafb-03d3268fc513)

1. **インストール**: `DICOM_Tag_Classifier-X.X.X.jar`ファイルをダウンロードし、ImageJの `plugins` ディレクトリに置く。以前のバージョンがある場合は削除してください。  
2. **ImageJの起動**: プラグインを読み込むために、ImageJを開くか、すでに起動している場合は再起動する。
3. **プラグインの有効化**: ImageJ の "Plugins "メニューに移動し、"DICOM Tag Classifier "を選択してプラグインを起動する。
4. **設定**: プラグインのインターフェイスを使用して、ディレクトリ構造やフィルタリング条件などの分類設定を行う。
5. **分類**: プラグインインターフェースから分類プロセスを開始し、DICOMファイルを指定されたディレクトリに整理する。
6. **モニタリング**: 分類プロセスの進行状況を監視し、ImageJ 内でエラーまたは警告メッセージを表示する。

![DICOM Tag Classifier Configuration](https://github.com/ramnoob/dicom-tag-classifier-ImageJ/assets/70456441/6f23c20a-35c6-4617-b799-33b00c4323a2)

## Dependencies
- **ImageJ**: Java-based image processing software.
- **Fiji**: ImageJ with useful plug-ins added. This plugin is recommended for use with Fiji (https://fiji.sc/) .
- **ImageJ**: Javaベースの画像処理ソフトウェア。
- **Fiji**: ImageJに複数の便利なプラグインを追加したもの。このプラグインはこちら (https://fiji.sc/) で使用を推奨している。

## Contributing
Contributions are welcome! Please fork the repository, make your changes, and submit a pull request.  
貢献を歓迎します！リポジトリをフォークして変更を加え、プルリクエストを提出してください。

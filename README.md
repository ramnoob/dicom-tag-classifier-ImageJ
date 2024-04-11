# DICOM Classifier ImageJ

## Overview
DICOM Classifier Plugin is an extension for ImageJ, providing a convenient way to classify DICOM (Digital Imaging and Communications in Medicine) files directly within the ImageJ environment. With this plugin, users can organize DICOM files into specified directories based on DICOM tag values and other criteria.

## Features
- **Tag-Based Classification**: Classify DICOM files into directories based on DICOM tag values such as SOP Instance UID, Image Number, Slice Location, etc.
- **Advanced Filtering**: Filter DICOM files using advanced criteria, including logical AND and OR operations on multiple DICOM tags.
- **Range Filter**: Filter DICOM files within specified numerical ranges for certain DICOM tags.
- **Custom Directory Structure**: Define a custom directory structure for organizing DICOM files based on tag values.
- **Interactive User Interface**: Easy-to-use graphical interface integrated into the ImageJ environment for configuring classification settings and monitoring the classification process.
- **File Management**: Option to overwrite, rename, or cancel file operations when a file with the same name already exists in the destination directory.

## Usage
1. **Installation**: Copy the DICOM Classifier Plugin JAR file into the "plugins" directory of your ImageJ installation.
2. **Launch ImageJ**: Open ImageJ or restart if it's already running to load the plugin.
3. **Activate Plugin**: Navigate to the "Plugins" menu in ImageJ and select "DICOM Classifier" to activate the plugin.
4. **Configure**: Use the plugin interface to configure the classification settings, including directory structure, filtering criteria, and file management options.
5. **Classify**: Start the classification process from the plugin interface to organize DICOM files into the specified directories.
6. **Monitor**: Monitor the progress of the classification process and view any error or warning messages within ImageJ.

## Dependencies
- ImageJ (Java-based image processing software)
- Java SE Development Kit (JDK)
- Apache Commons IO
- Apache Commons Lang
- Apache Commons Logging

## License
This plugin is licensed under the [MIT License](LICENSE).

## Contributing
Contributions are welcome! Please fork the repository, make your changes, and submit a pull request.

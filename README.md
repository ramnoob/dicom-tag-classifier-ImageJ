# DICOM Classifier ImageJ

## Overview
DICOM Classifier is a Java application designed to classify DICOM (Digital Imaging and Communications in Medicine) files based on user-defined criteria. It provides a user-friendly interface to organize DICOM files into specified directories according to DICOM tag values and other parameters.

## Features
- **Tag-Based Classification**: Classify DICOM files into directories based on DICOM tag values such as SOP Instance UID, Image Number, Slice Location, etc.
- **Advanced Filtering**: Filter DICOM files using advanced criteria, including logical AND and OR operations on multiple DICOM tags.
- **Range Filter**: Filter DICOM files within specified numerical ranges for certain DICOM tags.
- **Custom Directory Structure**: Define a custom directory structure for organizing DICOM files based on tag values.
- **Interactive User Interface**: Easy-to-use graphical interface for configuring classification settings and monitoring the classification process.
- **File Management**: Option to overwrite, rename, or cancel file operations when a file with the same name already exists in the destination directory.

## Usage
1. **Download**: Clone or download the DICOM Classifier project from this repository.
2. **Build**: Build the project using your preferred Java IDE or build tool.
3. **Run**: Execute the generated JAR file to launch the DICOM Classifier application.
4. **Configure**: Configure the classification settings, including directory structure, filtering criteria, and file management options.
5. **Classify**: Start the classification process to organize DICOM files into the specified directories.
6. **Monitor**: Monitor the progress of the classification process and view any error or warning messages.

## Dependencies
- Java SE Development Kit (JDK)
- Apache Commons IO
- Apache Commons Lang
- Apache Commons Logging

## License
This project is licensed under the [MIT License](LICENSE).

## Contributing
Contributions are welcome! Please fork the repository, make your changes, and submit a pull request.

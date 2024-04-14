# DICOM Tag Classifier ImageJ

## Overview
DICOM Tag Classifier is a plugin created in Java code that provides a convenient way to classify DICOM (Digital Imaging and Communications in Medicine) files directly within the ImageJ environment. The plugin allows users to create a directory structure based on DICOM tag values and organize DICOM files into specified directories.

## Features
- **Interactive User Interface**: Easy-to-use graphical interface integrated into the ImageJ environment for configuring classification settings and monitoring the classification process.
- **Tag-Based Classification**: Create directories and classify DICOM files based on DICOM tag values such as reconstruction function, tube current, and series description, etc.
- **Rename File**: Rename the file based on the DICOMTag value.
- **Range Filtering**: Filters files within a specified numerical range based on ImageNunber or SliceLocation DICOM tags.
- **Advanced Filtering**: Filter DICOM files using advanced criteria with AND and OR conditions for multiple user-specified DICOM tags.
- **File Management**: Option to overwrite, rename, or cancel file operations when a file with the same name already exists in the destination directory.

## Usage
1. **Installation**: Download the `DICOM_Classifier-X.X.X.jar` file and place it in the ImageJ `plugins` directory. If you have a previous version, delete it.
2. **Launch ImageJ**: Open ImageJ or restart if it's already running to load the plugin.
3. **Activate Plugin**: Navigate to the "Plugins" menu in ImageJ and select "DICOM Classifier" to activate the plugin.
4. **Configure**: Use the plug-in interface to configure classification settings such as directory structure, filtering criteria, etc.

  <img width="1003" alt="HowtoUse" src="https://github.com/ramnoob/dicom-tag-classifier-ImageJ/assets/70456441/4704fa45-4ce6-4529-a516-df83157175c7">
  
5. **Classify**: Start the classification process from the plugin interface to organize DICOM files into the specified directories.
6. **Monitor**: Monitor the progress of the classification process and view any error or warning messages within ImageJ.

## Dependencies
- **ImageJ**: Java-based image processing software.
- **Fiji**: A distribution that adds plug-ins and features to ImageJ, often used for scientific image analysis.

## License
This plugin is licensed under the [MIT License](LICENSE).

## Contributing
Contributions are welcome! Please fork the repository, make your changes, and submit a pull request.

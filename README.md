![Version 8.0.0 alpha 5](https://img.shields.io/badge/v8.0.0-alpha_5-royalblue)
![Java 17+](https://img.shields.io/badge/OpenJDK-17+-5382A1?labelColor=E76F00)
[![License: LGPL v3](https://img.shields.io/badge/LGPLv3-008033?logo=GPLv3&logoSize=auto)](https://www.gnu.org/licenses/lgpl-3.0)
[![Twitter](https://img.shields.io/twitter/follow/Icy_BioImaging)](https://x.com/Icy_BioImaging)
[![Image.sc forum](https://img.shields.io/badge/dynamic/json?url=https%3A%2F%2Fforum.image.sc%2Ftag%2Ficy.json&query=%24.topic_list.tags.0.topic_count&suffix=%20topics&label=forum&color=0AA0B4)](https://forum.image.sc/tag/icy)

# ImgLib2 for Icy

This is the repository for the source code of *ImgLib2 for Icy*, an extension for the [bioimage analysis software Icy](https://icy.bioimageanalysis.org/), which was developed by members or former members of the [Biological Image Analysis unit at Institut Pasteur](https://research.pasteur.fr/en/team/bioimage-analysis/). This plugin is licensed under the LGPL3 license.     
Icy is developed and maintained by [Biological Image Analysis unit at Institut Pasteur](https://research.pasteur.fr/en/team/bioimage-analysis/). The [source code of Icy](https://gitlab.pasteur.fr/bia/icy/icy) is also licensed under the LGPL3 license.

## Extension description

ImgLib2 wrappers for [Icy](https://icy.bioimageanalysis.org/).

## Installation instructions

### Install from Icy website

*Work In Progress*

### Install from source code

#### Requirements
- JDK 17+
- Maven 3.6.3+

#### Steps
1. Clone from git repository using `git clone` command
2. Open your terminal and navigate to the folder containing the cloned repository
3. Run `mvn` command
4. The extension was automatically installed in your local maven repository `~/.m2/repository/`, and registered in your Icy workspace

## Import in your extension

Add this dependency block in your `pom.xml`
```xml
<dependency>
    <groupId>org.bioimageanalysis.icy</groupId>
    <artifactId>imglib2-icy</artifactId>
</dependency>
```

## Citation

Please cite this extension as follows:

Please also cite the Icy software and mention the version of Icy you used (bottom right corner of the GUI or first lines of the Output tab):     
de Chaumont, F. et al. (2012) Icy: an open bioimage informatics platform for extended reproducible research, [Nature Methods](https://www.nature.com/articles/nmeth.2075), 9, pp. 690-696       
https://icy.bioimageanalysis.org

## Author(s)

- Jean-Yves Tivenez
- Thomas Musset


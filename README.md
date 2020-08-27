# MP3 Folder Creator
This Java tool groups MP3 files of the same artist and album together. It moves the MP3 files in a folder to new subfolders with names that correspond to an album by an artist. It finds the artist name and album name in the MP3 files' ID3 tag, which contains music metadata. Then, it creates a folder with its name in the format of Artst Name - Album Name. Lastly, it moves the MP3 file to that folder (provided that there is not a file in that folder with the exact same name).<br>

Currently, this tool only supports MP3 files with ID3V2.3 or ID3V2.4 tags. Also, ID3 tags with complete tag or frame unsynchronizaton, extended headers, and footers should be supported, but only complete tag unsynchronizaton has been tested.<br>

Only MP3 files in the selected folder are able to move, meaning that MP3 files in subfolders of the selected folder would not move.<br>

This program was written in tandem with the Folder Size Searcher, which also features Java code dealing with file systems.<br>

# Installation
Firstly, clone this repository, 
[using these instructions.](https://docs.github.com/en/enterprise/2.13/user/articles/cloning-a-repository)
<br><br>
Secondly, you should [install JDK 14 if you don't have it already, and set the PATH variable](https://docs.oracle.com/en/java/javase/14/install/overview-jdk-installation.html#GUID-8677A77F-231A-40F7-98B9-1FD0B48C346A). 

# Running the program
If you have an IDE that can run Java programs, you can just open the `ProjectFiles/src/MP3Folder.java` in the IDE and run it.<br><br>

Otherwise, open a terminal/command-line window, and change the directory to the location of the cloned repository.
Then, enter:
```
cd ProjectFiles/src
```
If you haven't compiled this code since you cloned it or last git pulled it, you should compile the Java files before running them by entering:
```
javac MP3Mover.java MP3FolderListener.java MP3Folder.java
```
To run the program, enter:
```
java MP3Folder
```
Note: Closing the terminal/command-line window will also close the program.

# Usage
Click the "Choose folder" button to pick a folder for the program to analyze. Click on the folder you want to choose, then click `Open` or press the Enter key.<br>
Note: if you want to go inside a folder, double-click that folder. Single-clicking it and either clicking `Open` or pressing the Enter key will result in the program checking and moving MP3 files within that selected folder.<br>
Then, wait for the program to finish. You can see the status of the program by looking to the right of the `Choose folder` button, and seeing if it is still `Waiting` for the program's analysis to finish, or if the program is `Done` analyzing. Once it is done, you will see if there were any files that have not successfully been put in folders.

# Credits:
This program was written by:<br>
[Devin Gopaul](https://github.com/DevinGopaul)<br>
Sources that were referred to while marking this program include:<br>
[Java Documentation](https://docs.oracle.com/en/java/javase/14/docs/api/)<br>
[ID3V2 Documentation](https://id3.org)<br>
Other specific sources are cited in `MP3Mover.java`

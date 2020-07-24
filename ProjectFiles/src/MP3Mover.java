import java.nio.file.*;
import java.lang.Math;
import java.io.*;
import java.util.ArrayList;

//explanation of ID3V2 unsynchronization: https://stackoverflow.com/questions/19671469/id3-unsynchronization-how-it-works
//remove null byte from string: https://stackoverflow.com/questions/36491851/how-to-remove-nul-characters-0-from-string-in-java 
//byte order mark in the beginning of ID3V2 frame data strings: https://stackoverflow.com/questions/9857727/text-encoding-in-id3v2-3-tags

public class MP3Mover{

    public MP3Mover(){
	}
	
    public int moveFile(String pathStr){
		int returnCode = 0;
		Path baseDir = Paths.get(pathStr);
		//explore all MP3 files in the base directory
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(baseDir,"*.mp3")) {
			int errorOccurred ;
			for (Path filePath : stream) {
				errorOccurred = 0;
				/*store the paths of the file's original location before being moved and
				the file's new locationb after being moved in an ArrayList of paths*/
				ArrayList<Path> oldAndNewFileLocations = new ArrayList<Path>();
				System.out.println(filePath.getFileName());
				String fileFullName = baseDir + "\\" + filePath.getFileName().toString();
				File file = new File(fileFullName);
				RandomAccessFile inputStream = null;
				try {
					//read the data of this file
					//currently trying to read ID3 header
					inputStream = new RandomAccessFile(file, "r");
					inputStream.seek(0);
					//Figure out whether or not the file uses ID3V2 by checking if the first 3 bytes are "ID3"
					char[] id3Chars = new char[3];
					for (int i = 0; i < 3; i++) {
						id3Chars[i] = (char) inputStream.read();
					}
					String id3String = String.valueOf(id3Chars);
					if (id3String.equalsIgnoreCase("ID3")) {
						//this file does use ID3V2
						//store the ID3V2 version number
						int versionNumber = 0;
						versionNumber = inputStream.read();
						//store the revision number
						int revisionNumber = inputStream.read();
						System.out.println("ID3V2."+versionNumber+"."+revisionNumber);
						//if file uses ID3V2 version that is not 2.3 or 2.4, it is not supported as of yet
						if (versionNumber != 3 && versionNumber != 4){
							System.out.println("This version of ID3V2 is not currently supported");
							returnCode++;
							errorOccurred = 1;
							continue;
						}
						//the header's flags byte for ID3V2.4 must be divisible by 16
						int headerFlags = 0;
						headerFlags = inputStream.read();
						if (headerFlags % 16 != 0){
							System.out.println("Flags byte not divisible by 16");
							returnCode++;
							errorOccurred = 1;
							continue;
						}
						//the header's flags byte for ID3V2.3 must be divisible by 32
						else if (versionNumber == 3 && headerFlags % 32 != 0){
							System.out.println("Flags byte not divisible by 16");
							returnCode++;
							errorOccurred = 1;
							continue;
						}
						
						else {
							System.out.println("ID3 flags: "+headerFlags);
							/*check the header's flags bytes to see whether or not this file has unsynchronization,
							an extended header, an experimental bit, or a footer (if it uses ID3V2.4)*/
							int headerExtraFlags = (int) (headerFlags / 16);
							System.out.println("Extra Flags: "+headerExtraFlags);

							int footer = 0;
							if (versionNumber == 4){
								footer = headerExtraFlags & 1;
								if (footer == 1){
									System.out.println("Footer exists");
								}
							}
							//check if experimental indicator is on
							int experimental = (headerExtraFlags >> 1) & 1;
							//check if extended header is used
							int extendedHeader = (headerExtraFlags >> 2) & 1;
							//check if unsynchronization is used
							int unsynchronization = (headerExtraFlags >> 3) & 1;

							if (experimental == 1){
								//an experimental bit which is on is currently unsupported, exit
								System.out.println("experimental bit is on");
								returnCode++;
								errorOccurred = 1;
								continue;
							}

							//setup synchronization by creating variables to store previous bit read and current bit read
							int previousBit;
							int currentBit;
							previousBit = 0;
							currentBit = 0;

							//find header size
							System.out.println("Header size");
							int headerFirstSizeByte = 0;
							headerFirstSizeByte = inputStream.read();
							System.out.println(headerFirstSizeByte);

							int headerSecondSizeByte = 0;
							headerSecondSizeByte = inputStream.read();
							System.out.println(headerSecondSizeByte);
							
							int headerThirdSizeByte = 0;
							headerThirdSizeByte = inputStream.read();
							System.out.println(headerThirdSizeByte);

							int headerFourthSizeByte = 0;
							headerFourthSizeByte = inputStream.read();
							System.out.println(headerFourthSizeByte);
							
							int totalSize = 0;
							totalSize += headerFirstSizeByte * Math.pow(2,21) + headerSecondSizeByte * Math.pow(2,14) + headerThirdSizeByte * Math.pow(2,7) + headerFourthSizeByte;
							System.out.println("ID3 tag size: "+totalSize);

							if (extendedHeader!=0){
								System.out.println("Extended Header: "+extendedHeader);
								System.out.println("\n\n\n");
							}
							if (unsynchronization!=0){
								System.out.println("Unsynchronization: "+unsynchronization);
								System.out.println("\n\n\n");
							}

							//if an extended header exists, read past it
							if (extendedHeader != 0){
								//calculate the extended header's size
								int extendedHeaderSizeArray[] = new int[4];
								int extendedHeaderTotalSize = 0;
								extendedHeaderSizeArray[0] = (int) inputStream.read();
								if (unsynchronization != 0){
									previousBit = currentBit;
									currentBit = extendedHeaderSizeArray[0];
									if (previousBit == 0xFF && currentBit == 0x00){
										System.out.println("Unsynchronization fixed");
										extendedHeaderSizeArray[0] = inputStream.read();
										currentBit = extendedHeaderSizeArray[0];
									}
								}
								extendedHeaderSizeArray[1] = (int) inputStream.read();
								if (unsynchronization != 0){
									previousBit = currentBit;
									currentBit = extendedHeaderSizeArray[1];
									if (previousBit == 0xFF && currentBit == 0x00){
										System.out.println("Unsynchronization fixed");
										extendedHeaderSizeArray[1] = inputStream.read();
										currentBit = extendedHeaderSizeArray[1];
									}
								}
								extendedHeaderSizeArray[2] = (int) inputStream.read();
								if (unsynchronization != 0){
									previousBit = currentBit;
									currentBit = extendedHeaderSizeArray[2];
									if (previousBit == 0xFF && currentBit == 0x00){
										System.out.println("Unsynchronization fixed");
										extendedHeaderSizeArray[2] = inputStream.read();
										currentBit = extendedHeaderSizeArray[2];
									}
								}
								extendedHeaderSizeArray[3] = (int) inputStream.read();
								if (unsynchronization != 0){
									previousBit = currentBit;
									currentBit = extendedHeaderSizeArray[3];
									if (previousBit == 0xFF && currentBit == 0x00){
										System.out.println("Unsynchronization fixed");
										extendedHeaderSizeArray[3] = inputStream.read();
										currentBit = extendedHeaderSizeArray[3];
									}
								}
								/*check if version is ID3V2.3 or ID3v2.4, and calculate the extended header size depending on the version, as
								ID3V2.4's extended header size bytes are syncsafe, while ID3V2.3's are not*/
								int factor = 8;
								if (versionNumber == 4){
									factor = 7;
								}
								extendedHeaderTotalSize += extendedHeaderSizeArray[3] + extendedHeaderSizeArray[2]*Math.pow(2,factor) + extendedHeaderSizeArray[1]*Math.pow(2,factor*2) + extendedHeaderSizeArray[0]*Math.pow(2,factor*3);
								System.out.println(extendedHeaderSizeArray[0]+" "+extendedHeaderSizeArray[1]+" "+extendedHeaderSizeArray[2]+" "+extendedHeaderSizeArray[3]);
								System.out.println(extendedHeaderTotalSize);
								//subtract the four extended header's size bytes from the extended header's size itself
								if (versionNumber == 3){
									extendedHeaderTotalSize -= 4;
								}
								
								//Read through the rest of the extended header, in which there is extendedHeaderTotalSize bytes.
								for (int i = 0; i < extendedHeaderTotalSize; i++){
									int temp = inputStream.read();
									if (unsynchronization != 0){
										previousBit = currentBit;
										currentBit = temp;
										if (previousBit == 0xFF && currentBit == 0x00){
											System.out.println("Unsynchronization fixed");
											temp = inputStream.read();
											currentBit = temp;
										}
									}
								}
							}

							//Frames
							//keep count of how many bytes there are left to read
							int counter = totalSize;
							String album = "";
							String artist = "";
							int setUnsynchronizationBackToZero = 0;
							//continue looking through the file's frames if all the bytes in the file haven't been read through yet
							while (counter > 0){
								//each frame's header contains 10 bytes
								counter-=10;
								//Find the ID of the frame
								char frameIDBytes[] = new char[4];
								frameIDBytes[0] = (char) inputStream.read();
								if (unsynchronization != 0){
									previousBit = currentBit;
									currentBit = (int) frameIDBytes[0];
									if (previousBit == 0xFF && currentBit == 0x00){
										System.out.println("Unsynchronization fixed");
										frameIDBytes[0] = (char) inputStream.read();
										currentBit = (int) frameIDBytes[0];
									}
								}
								frameIDBytes[1] = (char) inputStream.read();
								if (unsynchronization != 0){
									previousBit = currentBit;
									currentBit = (int) frameIDBytes[1];
									if (previousBit == 0xFF && currentBit == 0x00){
										System.out.println("Unsynchronization fixed");
										frameIDBytes[1] = (char) inputStream.read();
										currentBit = (int) frameIDBytes[1];
									}
								}
								frameIDBytes[2] = (char) inputStream.read();
								if (unsynchronization != 0){
									previousBit = currentBit;
									currentBit = (int) frameIDBytes[2];
									if (previousBit == 0xFF && currentBit == 0x00){
										System.out.println("Unsynchronization fixed");
										frameIDBytes[2] = (char) inputStream.read();
										currentBit = (int) frameIDBytes[2];
									}
								}
								frameIDBytes[3] = (char) inputStream.read();
								if (unsynchronization != 0){
									previousBit = currentBit;
									currentBit = (int) frameIDBytes[3];
									if (previousBit == 0xFF && currentBit == 0x00){
										System.out.println("Unsynchronization fixed");
										frameIDBytes[3] = (char) inputStream.read();
										currentBit = (int) frameIDBytes[3];
									}
								}

								System.out.println("ID: "+String.valueOf(frameIDBytes));
								
								/*if the ID string is empty, that would indicate that there are no more ID's,
								also indicating that there are no more frames. Thus, immediately stop searching
								the file by setting counter to -1*/
								if (String.valueOf(frameIDBytes).trim().length() == 0){
									counter = -1;
								}
								
								//calculate the size of the frame
								int frameSizeBytes[] = new int[4];
								frameSizeBytes[0] = (int) inputStream.read();
								if (unsynchronization != 0){
									previousBit = currentBit;
									currentBit = frameSizeBytes[0];
									if (previousBit == 0xFF && currentBit == 0x00){
										System.out.println("Unsynchronization fixed");
										frameSizeBytes[0] = inputStream.read();
										currentBit = frameSizeBytes[0];
									}
								}

								frameSizeBytes[1] = (int) inputStream.read();
								if (unsynchronization != 0){
									previousBit = currentBit;
									currentBit = frameSizeBytes[1];
									if (previousBit == 0xFF && currentBit == 0x00){
										System.out.println("Unsynchronization fixed");
										frameSizeBytes[1] = inputStream.read();
										currentBit = frameSizeBytes[1];
									}
								}

								frameSizeBytes[2] = (int) inputStream.read();
								if (unsynchronization != 0){
									previousBit = currentBit;
									currentBit = frameSizeBytes[2];
									if (previousBit == 0xFF && currentBit == 0x00){
										System.out.println("Unsynchronization fixed");
										frameSizeBytes[2] = inputStream.read();
										currentBit = frameSizeBytes[2];
									}
								}

								frameSizeBytes[3] = (int) inputStream.read();
								if (unsynchronization != 0){
									previousBit = currentBit;
									currentBit = frameSizeBytes[3];
									if (previousBit == 0xFF && currentBit == 0x00){
										System.out.println("Unsynchronization fixed");
										frameSizeBytes[3] = inputStream.read();
										currentBit = frameSizeBytes[3];
									}
								}

								//check if version is ID3v2.3 or ID3v2.4, and calculate the frame size depending on the version
								int factor = 8;
								if (versionNumber == 4){
									factor = 7;
								}
								int size = 0;
								size += frameSizeBytes[3] + frameSizeBytes[2]*Math.pow(2,factor) + frameSizeBytes[1]*Math.pow(2,factor*2) + frameSizeBytes[0]*Math.pow(2,factor*3);
								counter-=size;
								//Look at the frame's flags
								int flag1 = (int) inputStream.read();
								if (unsynchronization != 0){
									previousBit = currentBit;
									currentBit = flag1;
									if (previousBit == 0xFF && currentBit == 0x00){
										System.out.println("Unsynchronization fixed");
										flag1 = inputStream.read();
										currentBit = flag1;
									}
								}
								int flag2 = (int) inputStream.read();
								if (unsynchronization != 0){
									previousBit = currentBit;
									currentBit = flag2;
									if (previousBit == 0xFF && currentBit == 0x00){
										System.out.println("Unsynchronization fixed");
										flag2 = inputStream.read();
										currentBit = flag2;
									}
								}

								System.out.println("Frame flags: "+flag1+" and "+flag2);
								if (versionNumber == 4){
									//check if frame is unsynchronized
									int tempUnsync = (flag2 >> 1) & 1;
									if (tempUnsync == 1 && unsynchronization == 0){
										System.out.println("There is unsynchronization in this frame");
										setUnsynchronizationBackToZero = 1;
										unsynchronization = 1;
									}									
								}

								/*Finished reading frame's header, so now look through its main text data, and if the artist
								and album data is found, store it*/
								char mainDataChars[] = new char[size];
								
								//tag is TALB, meaning that the frame contains the album name, 
								if (String.valueOf(frameIDBytes).equals("TALB")){
									for (int i = 0; i < size; i++) {
										mainDataChars[i]=(char) inputStream.read();
										if (unsynchronization != 0){
											previousBit = currentBit;
											currentBit = (int) mainDataChars[i];
											if (previousBit == 0xFF && currentBit == 0x00){
												System.out.println("Unsynchronization fixed");
												mainDataChars[i] = (char) inputStream.read();
												currentBit = (int) mainDataChars[i];
											}
										}
									}
									//remove null byte from album string
									album = String.valueOf(mainDataChars).trim().replace("\0", "");
								}
								//tag is TPE2 or TPE1 (meaning that the frame contains the artist name)
								else if (String.valueOf(frameIDBytes).equals("TPE2") || String.valueOf(frameIDBytes).equals("TPE1")){
									for (int i = 0; i < size; i++) {
										mainDataChars[i]=(char) inputStream.read();
										if (unsynchronization != 0){
											previousBit = currentBit;
											currentBit = (int) mainDataChars[i];
											if (previousBit == 0xFF && currentBit == 0x00){
												System.out.println("Unsynchronization fixed");
												mainDataChars[i] = (char) inputStream.read();
												currentBit = (int) mainDataChars[i];
											}
										}
									}
									//remove null byte from artist string
									artist= String.valueOf(mainDataChars).trim().replace("\0", "");
								}
								//tag is neither TALB nor TPE1/TPE2 (frame contains neither album nor artist info), so just read past the frame
								else if (String.valueOf(frameIDBytes).trim().length() == 4){

									for (int i = 0; i < size; i++) {
										mainDataChars[i]=(char) inputStream.read();
										if (unsynchronization != 0){
											previousBit = currentBit;
											currentBit = (int) mainDataChars[i];
											if (previousBit == 0xFF && currentBit == 0x00){
												System.out.println("Unsynchronization fixed");
												mainDataChars[i] = (char) inputStream.read();
												currentBit = (int) mainDataChars[i];
											}
										}
									}
								}

								//if frame is unsynchronized, but the entire tag is not, turn off unsynchronization for now
								if (setUnsynchronizationBackToZero == 1){
									unsynchronization = 0;
								}
							}
							
							//if the artist and/or the album are not found
							if (artist.length() == 0 || album.length() == 0){
								System.out.println("Strange error: album and/or artist not found\n\n");
								returnCode++;
								errorOccurred = 1;
								continue;
							}

							//remove byte order mark, which could be at the beginning of the strings stored in the artist and/or album strings
							if ((int) artist.charAt(0) == 255 && (int) artist.charAt(1) == 254){
								artist = artist.substring(2);
							}
							if ((int) album.charAt(0) == 255 && (int) album.charAt(1) == 254){
								album = album.substring(2);
							}

							//take out illegal characters for filenames from artist and album strings
							String legalAlbum = album.replaceAll(":|>|<|\\\\|/|\\*|\\?|\\|", "");
							String legalArtist = artist.replaceAll(":|>|<|\\\\|/|\\*|\\?|\\|", "");
							
							//use the artist and album strings as the name for a new directory
							String newDirString = legalArtist + " - " + legalAlbum;

							Path newDirPath = baseDir.resolve(newDirString);
							try {
								Files.createDirectory(newDirPath);
							} catch (Exception e) {
							}
							Path newFileLocation = newDirPath.resolve(filePath.getFileName());
							File newFile = new File(newFileLocation.toString());
							if (newFile.exists()){
								System.out.println("File/directory with same name already exists in folder");
								returnCode++;
								errorOccurred = 1;
								continue;
							}
							System.out.println("\n\n\nOut! "+newDirString);
							oldAndNewFileLocations.add(filePath);
							oldAndNewFileLocations.add(newDirPath.resolve(filePath.getFileName()));
						}

					} else {
						//Tag is not in the ID3V2 format
						System.out.println("Does not conform to ID3");
						returnCode++;
						errorOccurred = 1;
						continue;
					}
				} finally {
					//move the file to the new directory after closing the inputStream
					if (errorOccurred == 0 && inputStream != null) {
						inputStream.close();
						System.out.println(oldAndNewFileLocations);
						try {
							Files.move(oldAndNewFileLocations.get(0),oldAndNewFileLocations.get(1));
						}
						catch (IOException | IndexOutOfBoundsException e){
							System.err.println(e);
						}
					}

				}

			}
		} catch (IOException | DirectoryIteratorException x) {
			System.err.println("A caught exception: "+x);
		}
        return returnCode;
    }
}
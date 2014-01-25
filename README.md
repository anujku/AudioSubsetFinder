AudioSubsetFinder
=================

This is a audio subset finder project. You can find the exact time when the match between two audio files starts. 
Please refer the specification to get better idea of the problem to be solved and the solution.

Instructions to run the project:

Build process :
1) UnZip the folder MSDAssFinal and cd into the folder MSDAssFinal.

2) Execute :   
	./buildscript.sh

3) The build script will internally compile all java classes and create 
a jar from them and in the next step it will create an executebale
from that jar file that can be used anywhere. The intermediate files
like .class, .jar files created during the process are deleted by the 
buildscript.sh.

4) Execute arc5500 executable :- 
format-./arc5500 <pathspec> <pathspec>
e.g :  ./arc5500 -f /1234abc/a.wav --dir ~/abc/

5) If you do not have execute access or encounter any error, please 
execute following commands to gain execute access on buildcript.sh 
and the required artifacts.

Important notes after step 2) in case of failure :
check if manifest.txt exists in the folder. If not then execute the
following command on the terminal to create manifest.txt

echo Main-Class: AudioFileMatcherMain > manifest.txt

Commands to gain execute access on the required artefacts

chmod +x buildscript.sh
chmod +x stub.sh

Execute buildscript.sh  :- Type ./buildscript.sh
Execute arc5500 executable :- Type ./arc5500 -f /1234abc/a.wav --dir ---


Files submitted in Assignment Twelve : 
- README
- buildscript.sh
- manifest.txt
- stub.sh
- Java source files :
	- AbstractFileMatcherFactory.java
	- AudioFileMatcherMain.java
	- AudioFileTypeValidator.java
	- Complex.java
	- Constants.java
	- FFT.java
	- FileMatcher.java
	- FileUtils.java
	- FingerPrintWav.java
	- StreamReaderThread.java

No third party software used.

README

# COMP4321_2019Spring
---------------------------------------------------------
Group member

Xu, Feiting(fxuaf)  20329359
Lam Hon Wa(hwlamad) 20348745
Li, Junze(jlicx)    20413186



## File Structure
---------------------------------------------------------
-project
   -db
   -lib
	 -rocksdbjni-6.0.0-linux64.jar
	 -htmlparser.jar
	 -jsoup-1.8.1.jar
	 -StopStem.jar
   -project_database.java
   -stopwords.txt



## Instruction
---------------------------------------------------------
Put project_database.java
	stopwords.txt			in ROOT/project

Put htmlparser.jar
	rocksdbjni-6.0.0-linux64.jar
	jsoup-1.8.1.jar
	StopStem.jar			in ROOT/project/lib

create directory ROOT/project/db



## Crawler
---------------------------------------------------------
compile

javac -cp .:lib/* project_database.java


run

java -cp lib/*:. project_database



## Output
---------------------------------------------------------
spider_result.txt		in ROOT/project



## Remarks
---------------------------------------------------------
You may recompile StopStem.java in order to reproduce the StopStem.jar
Make sure stopwords.txt is in the same directory with StopStem.java

javac StopStem.java
jar -cvf StopStem.jar StopStem.class NewString.class Porter.class
README
You can access the web directly from the following link:
http://143.89.130.247:8080/form.html

# COMP4321_2019Spring
---------------------------------------------------------
Group member

Xu, Feiting(fxuaf)  20329359
Lam Hon Wa(hwlamad) 20348745
Li, Junze(jlicx)    20413186



## File Structure
---------------------------------------------------------
 -form.html
 -afterSubmit.jsp
 -keywordList.jsp
 -queryHistory.jsp
 -similarPages.jsp
 -project_database.java
 -WEB-INF
|     -lib
|    |    -htmlpraser.jar
|    |    -rocksdbini-6.0.0-linux64.jar
|    |    -jsoup-1.8.1.jar
|
|     -classes
|    |
|    |      -Database
|    |     |     -Search.java
|    |     |     -stopwords.txt
|    |     |     -StopStem.class          
|    |     |     -lib
|    |     |    |     -rocksdbjni-6.0.0-linux64.jar
|    |     |    |     -htmlparser.jar
|    |     |    |     -jsoup-1.8.1.jar
|    |     |    |     -StopStem.jar
 -lib
|     -rocksdbjni-6.0.0-linux64.jar
|     -htmlparser.jar
|     -jsoup-1.8.1.jar
|     -StopStem.jar
 -db



## Installation procedure
---------------------------------------------------------

1.	Put below files in ROOT/WEB-INF/classes/Database:
	-project_database.java
	-stopwords.txt
	-StopStem.class

2.	Put below files in ROOT/WEB-INF/classes/Database/lib:
	-htmlpraser.jar
	-rocksdbini-6.0.0-linux64.jar
	-jsoup-1.8.1.jar

3.	Create directory db in the ROOT folder

4.	Compile the file with following command in the Database file:
	javac -cp .:lib/* project_database.java

5.	Run the file with following command:
	java -cp lib/*:. project_database

6.	Put below files in ROOT/WEB-INF/classes/Database:
	-Search.java

7.	Compile the file with following command:
	javac -cp .:lib/* Search.java

8.	Put below files in ROOT/WEB-INF/lib:
	-htmlpraser.jar
	-rocksdbini-6.0.0-linux64.jar
	-jsoup-1.8.1.jar

9.	Put below files in ROOT:
	form.html
	afterSubmit.jsp
	keywordList.jsp
	queryHistory.jsp
	similarPages.jsp

10.	visit VM_location:8080/form.html to use the search engine


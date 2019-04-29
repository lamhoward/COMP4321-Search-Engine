README

The web interface has built successfully, please enter the following website to access the search engine:
http://143.89.130.247:8080/form.html



# COMP4321_2019Spring
---------------------------------------------------------
Group member

Xu, Feiting(fxuaf)  20329359
Lam Hon Wa(hwlamad) 20348745
Li, Junze(jlicx)    20413186

## Aim
---------------------------------------------------------
Develop a web-based search engine with following functions:
	1. A spider (or called a crawler) function to fetch pages recursively from a given web site
	2. An indexer which extracts keywords from a page and inserts them into an inverted file
	3. A retrieval function that compares a list of query terms against the inverted file and returns the top documents, up to a maximum of 50, to the user in a ranked order according to the vector space model.  As noted about, phrase must be supported, e.g., “hong kong” universities
	4. A web interface that accepts a user query in a text box, submits the query to the search engine, and displays the returned results to the user


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

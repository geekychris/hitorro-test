Explanation of how to run basic tests

There are three different index datasets, each generated randomly.  They all have the same shape.  Their
names indicate the number of rows of data, so "Table100AdapterSource.xml" loads a 100-row set of data.

There are four different challenge documents:
1) shortnegtest.txt - short text with no hits
2) longnegtest.txt - long text with no hits
3) shortpostest.txt - short text with a single hit for each of the 100, 1000 and 10000 row datasets
4) longpostest.txt - short text with a single hit for each of the 100, 1000 and 10000 row datasets


To run a test:
1) Start the server
    working directory of cvsclient/build
    java ht.rover.application.Server config/searchindexparams.xml
2) Load an index
    index testdata/basic/Table100AdapterSource.xml
3) Test against a challenge document
    test shortpostest.txt


Alternatively, use the quick testing script to run a stress test:
java ht.rover.application.Server config/searchindexparams.xml < testdata/basic/test1script.txt




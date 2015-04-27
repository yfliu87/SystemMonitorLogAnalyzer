# SystemMonitorLogAnalyzer
System Monitor is a specific log used to record system information from end user experience.
It records detailed information which can be useful for development team debug and reproduce problems.
The original log format is totally CSV file which is inconvenient to read and search, this project aims to 
find a way to make full use of these log through extracting information, indexing keywords and web search.

There are several parts of it:

1. logParser
  
  1.1 A converter for converting CSV file to excel file
 
  1.2 File parser which reads excel log line by line and record important information in separate sheets
  

2. Indexer
 
  1.1 The key part is Lucene which is used ot index key words for future query
 
  1.2 The index creation process will open each log file and read key information from sheets, index these information
  

3. QueryParser
 
  1.1 It handles different querys and searches by keywords through aboundent of interfaces
  

4. Spring MVC
 
  1.1 The MVC pattern enables the architeture simple and clear
 
  1.2 Controller will handle query condition from Viewer, then execute query according to different key words
 
  1.3 Controller will also try to arrange the output format according to diffent output format
  
Future task
  1. Replace raw Lucene with ElasticSearch which supports JSON format
  2. Simplify query and index logic
  3. Better designed UI
  4. Support parallel parsing logic for raw log, currently the sequential parsing logic is very time consuming
  5. Support MapReduce for collecting special statistic
  
  

# Map-Reduce-Programs
Map reduce programs

This repository contains the different Map Reduce programs . Below topics are covered in these examples

1)Partitioner in Map Reduce

2)Combiner in Map Reduce

3)Custom Input for Map Reduce

4)Secondary Sort in Map Reduce

ORC and Parquet are very Similar File Formats. They have more in similarity as compare to differences. 
1. Both are Columnar File systems 2. Both have block level compression.

However we have following pointers to chose them 1. Parquet is developed and supported by Cloudera. 
It is inspired from columnar file format and Google Dremel. So Cloudera supported products and distributions prefer parquet. 
if you are planning to use impala with your data, then prefer parquet

ORC format has evolved from RCFile format. It is very good when you have complex datatypes as part of your data.

ORC can provide you better compression.

ORC is more mature than Parquet when it comes to providing predicate pushdown features. Recently this has been provided in parquet also.

You can watch this video on youtube. It covers this topic well. Link : https://www.youtube.com/watch?v=NZLrJmjoXw8




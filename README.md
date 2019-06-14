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



------------------------------
3. Parameters of Hive Map Side Join
Moreover, let’s discuss  the Hive map side join options below

a. hive.auto.convert.join
However, this option set true, by default. Moreover, when a table with a size less than 25 MB (hive.mapjoin.smalltable.filesize) is 
found, When it is enabled, during joins, the joins are 
converted to map-based joins.


-----------------------------------------------


6

Partitioning and Bucketing are two different types of splitting your data at the physical layer.

As you saw, when you partition a table by a column, a directory will be created for each value of the column. As such, you'll typically want to partition on a column that has low cardinality. One of the most common partition columns you'll see is date.

With bucketing, the column value is hashed into a fixed number of buckets. This also physically splits your data. In your case, if you inspect the files in the city directories, you'll see 16 files, 1 for each bucket. Bucketing is typically used for high cardinality columns.

So, what is the advantage of partitioning and bucketing? Since the data is physically "partitioned", the query layer can apply two types of optimizations called partition pruning and bucket pruning. These optimizations will kick in when a WHERE clause is applied that can allow the optimizer to apply the pruning strategies. For example, in your case you have 6 directories (cities) times 16 files (id buckets), so you have a total of 96 files in your table. If you included a where clause for city = "city1", then only 16 files will be scanned since partition pruning will kick in. If you used a where clause for id = 10101, then only 6 files will be scanned since bucket pruning can be applied. If you apply both a city filter and an id filter, the only 1 file will need to be scanned.

EDIT: As pointed out in the comments, bucket pruning has only been implemented in the Tez engine. So, while in theory, buckets can be pruned, the optimization has not been implemented yet in Hive MR.


--------------------------

Bucket Join

In bucketing, the data at storage level is distributed in buckets. Each bucket is expected to hold certain rows based on the bucketing key/column.

As an example, lets say there are two tables user and user_visits and both table data is bucketed using user_id in 4 buckets . It means bucket 1 of user will contain rows with same user ids as that of bucket 1 of user_visits. And if a join is performed on these two tables on user_id columns, if it is possible to send bucket 1 of both tables to same mapper then good amount of optimization can be achived. This is exactly done in bucketed map join.

Prerequisites for bucket map join:

Tables being joined are bucketized on the join columns,
The number of buckets in one table is a multiple of the number of buckets in the other table, the buckets can be joined with each other
If the tables being joined are bucketized on the join columns, and the number of buckets in one table is a multiple of the number of buckets in the other table, the buckets can be joined with each other. If table A has 4 buckets and table B has 4 buckets, the following join

SELECT /*+ MAPJOIN(b) */ a.key, a.valueFROM a JOIN b ON a.key = b.key 

can be done on the mapper only. Instead of fetching B completely for each mapper of A, only the required buckets are fetched. For the query above, the mapper processing bucket 1 for A will only fetch bucket 1 of B. It is not the default behavior, and is governed by the following parameter

set hive.optimize.bucketmapjoin = true

If the tables being joined are sorted and bucketized on the join columns, and they have the same number of buckets, a sort-merge join can be performed. The corresponding buckets are joined with each other at the mapper. If both A and B have 4 buckets,

SELECT /*+ MAPJOIN(b) */ a.key, a.valueFROM A a JOIN B b ON a.key = b.key 

can be done on the mapper only. The mapper for the bucket for A will traverse the corresponding bucket for B. This is not the default behavior, and the following parameters need to be set:

set hive.input.format=org.apache.hadoop.hive.ql.io.BucketizedHiveInputFormat;

set hive.optimize.bucketmapjoin = true;

set hive.optimize.bucketmapjoin.sortedmerge = true;
-----------------------------
 create table salesdata_source
(salesperson_id int,
product_id int,
date_of_sale string)


Static partitioning
We specify that value of the partition while inserting the data.

insert into table salesdata partition (date_of_sale=’10-27-2017’)
select * from salesdata_source where date_of_sale=’10-27-2017’;
insert into table salesdata partition (date_of_sale=’10-28-2017’)
select * from salesdata_source where date_of_sale=’10-28-2017’;


Dynamic partitioning
There is another way of partitioning where we let the Hive engine dynamically determine the partitions based on the values of the partition column.

Before using this, we have to set a property that allows dynamic partition:
 

set hive.exec.dynamic.partition.mode=nonstrict;

hive> insert into table salesdata partition (date_of_sale)
select salesperson_id,product_id,date_of_sale from salesdata_source ;



Alter partiotion
----------------
1. alter table salesdata partition (date_of_sale=10-27-2017) rename to partition (date_of_sale=10-27-2018);
Partition name will be changed(can be verified by show partitions), and the subdirectory name in the warehouse will be changed, but the data underneath will remain same.
 

2. alter table salesdata drop partition (date_of_sale=10-27-2017) ; (internal table)
Partition will be dropped and the subdirectory will be deleted.
 

3. alter table salesdata_ext drop partition (date_of_sale=10-27-2017) ; (external table)
-------------------------------------

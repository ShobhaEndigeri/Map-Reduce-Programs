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
The answer by @user1668782 is a great explanation for the question and I'll try to give a graphical depiction of it.

Assume we have a file of 400MB with consists of 4 records(e.g : csv file of 400MB and it has 4 rows, 100MB each)

enter image description here

If the HDFS Block Size is configured as 128MB, then the 4 records will not be distributed among the blocks evenly. It will look like this.
enter image description here

Block 1 contains the entire first record and a 28MB chunk of the second record.
If a mapper is to be run on Block 1, the mapper cannot process since it won't have the entire second record.
This is the exact problem that input splits solve. Input splits respects logical record boundaries.

Lets Assume the input split size is 200MB

enter image description here

Therefore the input split 1 should have both the record 1 and record 2. And input split 2 will not start with the record 2 since record 2 has been assigned to input split 1. Input split 2 will start with record 3.

This is why an input split is only a logical chunk of data. It points to start and end locations with in blocks.

Hope this helps.



https://stackoverflow.com/questions/17727468/hadoop-input-split-size-vs-block-size

---------------------------------------------
As it is said in the API documentation:

commitSync
This is a synchronous commits and will block until either the commit succeeds or an unrecoverable error is encountered (in which case it is thrown to the caller).

That means, the commitSync is a blocking method. Calling it will block your thread until it either succeeds or fails.

For example,

while (true) {
    ConsumerRecords<String, String> records = consumer.poll(100);
    for (ConsumerRecord<String, String> record : records) {
        System.out.printf("offset = %d, key = %s, value = %s", record.offset(), record.key(), record.value());
        consumer.commitSync();
    }
}
For each iteration in the for-loop, only after consumer.commitSync() successfully returns or interrupted with exception thrown, your code will move to the next iteration.

commitAsync
This is an asynchronous call and will not block. Any errors encountered are either passed to the callback (if provided) or discarded.

That means, the commitAsync is a non-blocking method. Calling it will not block your thread. Instead, it will continue processing the following instructions, no matter whether it will succeed or fail eventually.

For example, similar to previous example, but here we use commitAsync:

while (true) {
    ConsumerRecords<String, String> records = consumer.poll(100);
    for (ConsumerRecord<String, String> record : records) {
        System.out.printf("offset = %d, key = %s, value = %s", record.offset(), record.key(), record.value());
        consumer.commitAsync(callback);
    }
}
For each iteration in the for-loop, no matter what will happen to consumer.commitAsync() eventually, your code will move to the next iteration. And, the result of the commit is going to be handled by the callback function you defined.

Trade-offs: latency vs. data consistency

If you have to ensure the data consistency, choose commitSync() because it will make sure that, before doing any further actions, you will know whether the offset commit is successful or failed. But because it is sync and blocking, you will spend more time on waiting for the commit to be finished, which leads to high latency.
If you are ok of certain data inconsistency and want to have low latency, choose commitAsync() because it will not wait to be finished. Instead, it will just send out the commit request and handle the response from Kafka (success or failure) later, and meanwhile, your code will continue executing.
This is all generally speaking, the actually behaviour will depend on your actual code and where you are calling the method.


----------------------------------------------------------


BOOTSTRAP_SERVERS_CONFIG: The Kafka broker's address. If Kafka is running in a cluster then you can provide comma (,) seperated addresses. For example:localhost:9091,localhost:9092 

CLIENT_ID_CONFIG: Id of the producer so that the broker can determine the source of the request.

KEY_SERIALIZER_CLASS_CONFIG: The class that will be used to serialize the key object. In our example, our key is Long, so we can use the LongSerializer class to serialize the key. If in your use case you are using some other object as the key then you can create your custom serializer class by implementing the Serializer interface of Kafka and overriding the serialize method.

VALUE_SERIALIZER_CLASS_CONFIG: The class that will be used to serialize the value object. In our example, our value is String, so we can use the StringSerializer class to serialize the key. If your value is some other object then you create your custom serializer class. For example:


------------------------------------------------------------------
bootstrap.servers Property. bootstrap.servers is a comma-separated list of host and port pairs that are the addresses of the Kafka brokers in a "bootstrap" Kafka cluster that a Kafka client connects to initially to bootstrap itself. A host and port pair uses : as the separator.
---------------------------------------------
mapside join
https://www.edureka.co/blog/map-side-join-vs-join/

HBase Write
-----------
WAL
memstore
blockcache ---- read cache


https://mapr.com/blog/in-depth-look-hbase-architecture/


Hive
-----

reate table product ( productid: int, productname: string, price: float, category: string) rows format delimited fields terminated by ‘,’ ;
describe product;
load data local inpath ‘/home/cloudera/input.txt’ into table product;
local means local file system

partionedby(city String)
clusteredby(street) into n buckets

----------------------------------------------------------------------------------------------
storm

nimbus  master
supervisor worker nodes





----------------
yarn client mode
yarn cluster mode
yarn standalone mode
----------------

PARTITIONING will be used when there are few unique values in the Column - which you want to load with your required WHERE clause

BUCKETING will be used if there are multiple unique values in your Where clause



----------------------------------------------
CREATE TABLE employees (
name STRING,
salary FLOAT,
subordinates ARRAY<STRING>,
deductions MAP<STRING, FLOAT>,
address STRUCT<street:STRING, city:STRING, state:STRING, zip:INT>
)
ROW FORMAT DELIMITED
FIELDS TERMINATED BY '\001'
COLLECTION ITEMS TERMINATED BY '\002'
MAP KEYS TERMINATED BY '\003'
LINES TERMINATED BY '\n'
STORED AS TEXTFILE;



val input = sc.wholeTextFiles("file://home/holden/salesFiles")
val result = input.mapValues{y =>
val nums = y.split(" ").map(x => x.toDouble)
nums.sum / nums.size.toDouble
}
---------------------------------------------------------------------------------

persist vs cache?
checksum
map side join
inputSplit
broadcast vs accumulator
checksum

-------------------------

hbase(main):015:0> create 'test_table', 'f1', SPLITS=> ['a', 'b', 'c']
---------------------------------------------------------
https://data-flair.training/blogs/mapreduce-performance-tuning/
--------------------------------


1. yarn-client vs. yarn-cluster mode
There are two deploy modes that can be used to launch Spark applications on YARN: Per Spark documentation:

In yarn-client mode, the driver runs in the client process, and the application master is only used for requesting resources from YARN.
In yarn-cluster mode, the Spark driver runs inside an application master process which is managed by YARN on the cluster, and the client can go away after initiating the application.

---------------------------------------

Narrow transformation – In Narrow transformation, all the elements that are required to compute the records in single partition live in the single partition of parent RDD. A limited subset of partition is used to calculate the result. Narrow transformations are the result of map(), filter()



Wide transformation – In wide transformation, all the elements that are required to compute the records in the single partition may live in many partitions of parent RDD. The partition may live in many partitions of parent RDD. Wide transformations are the result of groupbyKey() and reducebyKey().

------------------------------------------------

ReduceByKey reduceByKey(func, [numTasks])-

Data is combined so that at each partition there should be at least one value for each key. And then shuffle happens and it is sent over the network to some particular executor for some action such as reduce.

GroupByKey - groupByKey([numTasks])

It doesn't merge the values for the key but directly the shuffle process happens and here lot of data gets sent to each partition, almost same as the initial data.

And the merging of values for each key is done after the shuffle. Here lot of data stored on final worker node so resulting in out of memory issue.

AggregateByKey - aggregateByKey(zeroValue)(seqOp, combOp, [numTasks]) It is similar to reduceByKey but you can provide initial values when performing aggregation.

Use of reduceByKey

reduceByKey can be used when we run on large data set.

reduceByKey when the input and output value types are of same type over aggregateByKey

Moreover it recommended not to use groupByKey and prefer reduceByKey. For details you can refer here.

You can also refer this question to understand in more detail how reduceByKey and aggregateByKey

-------------------------------------------------

ReduceByKey reduceByKey(func, [numTasks])-

Data is combined so that at each partition there should be at least one value for each key. And then shuffle happens and it is sent over the network to some particular executor for some action such as reduce.

GroupByKey - groupByKey([numTasks])

It doesn't merge the values for the key but directly the shuffle process happens and here lot of data gets sent to each partition, almost same as the initial data.

And the merging of values for each key is done after the shuffle. Here lot of data stored on final worker node so resulting in out of memory issue.

AggregateByKey - aggregateByKey(zeroValue)(seqOp, combOp, [numTasks]) It is similar to reduceByKey but you can provide initial values when performing aggregation.

Use of reduceByKey

reduceByKey can be used when we run on large data set.

reduceByKey when the input and output value types are of same type over aggregateByKey

Moreover it recommended not to use groupByKey and prefer reduceByKey. For details you can refer here.

You can also refer this question to understand in more detail how reduceByKey and aggregateByKey


------------------------------------------------------


local mode
Think of local mode as executing a program on your laptop using single JVM. It can be java, scala or python program where you have defined & used spark context object, imported spark libraries and processed data residing in your system.


YARN
In reality Spark programs are meant to process data stored across machines. Executors process data stored on these machines. We need a utility to monitor executors and manage resources on these machines( clusters). Hadoop has its own resources manager for this purpose. So when you run spark program on HDFS you can leverage hadoop's resource manger utility i.e. yarn. Hadoop properties is obtained from ‘HADOOP_CONF_DIR’ set inside spark-env.sh or bash_profile


Spark Standalone
Spark distribution comes with its own resource manager also. When your program uses spark's resource manager, execution mode is called Standalone. Moreover, Spark allows us to create distributed master-slave architecture, by configuring properties file under $SPARK_HOME/conf directory. By Default it is set as single node cluster just like hadoop's psudo-distribution-mode.


-----------------------------------------
InputSplits are created by logical division of data, which serves as the input to a single Mapper job. Blocks, on the other hand, are created by the physical division of data. One input split can spread across multiple physical blocks of data.

The basic need of Input splits is to feed accurate logical locations of data correctly to the Mapper so that each Mapper can process complete set of data spread over more than one blocks. When Hadoop submits a job, it splits the input data logically (Input splits) and these are processed by each Mapper. The number of Mappers is equal to the number of input splits created.

InputFormat.getSplits() is responsible for generating the input splits which uses each split as input for each mapper job.
-----------------------------------------------------------------


RecordReader. The MapReduce RecordReader in Hadoop takes the byte-oriented view of input, provided by the InputSplit and presents as a record-oriented view for Mapper. It uses the data within the boundaries that were created by the InputSplit and creates Key-value pair.

----------------------------
Cache and Persist both are optimization techniques for Spark computations.

Cache is a synonym of Persist with MEMORY_ONLY storage level(i.e) using Cache technique we can save intermediate results in memory only when needed.

Persist marks an RDD for persistence using storage level which can be MEMORY, MEMORY_AND_DISK, MEMORY_ONLY_SER, MEMORY_AND_DISK_SER, DISK_ONLY, MEMORY_ONLY_2, MEMORY_AND_DISK_2

Just because you can cache an RDD in memory doesn’t mean you should blindly do so. Depending on how many times the dataset gets accessed and the amount of work involved in doing so, recomputation can be faster by the increased memory pressure.


--------------------------
local mode
Think of local mode as executing a program on your laptop using single JVM. It can be java, scala or python program where you have defined & used spark context object, imported spark libraries and processed data residing in your system.


YARN
In reality Spark programs are meant to process data stored across machines. Executors process data stored on these machines. We need a utility to monitor executors and manage resources on these machines( clusters). Hadoop has its own resources manager for this purpose. So when you run spark program on HDFS you can leverage hadoop's resource manger utility i.e. yarn. Hadoop properties is obtained from ‘HADOOP_CONF_DIR’ set inside spark-env.sh or bash_profile


Spark Standalone
Spark distribution comes with its own resource manager also. When your program uses spark's resource manager, execution mode is called Standalone. Moreover, Spark allows us to create distributed master-slave architecture, by configuring properties file under $SPARK_HOME/conf directory. By Default it is set as single node cluster just like hadoop's psudo-distribution-mode.

------------------
Apache Storm is a distributed realtime computation system. Similar to how Hadoop provides a set of general primitives for doing batch processing, Storm provides a set of general primitives for doing the realtime computation. Storm is simple, can be used with any programming language, is used by many companies, and is a lot of fun to use!

Components of a Storm cluster
Apache Storm cluster is superficially similar to a Hadoop cluster. Whereas on Hadoop you run “MapReduce jobs”, on Storm you run “topologies”. “Jobs” and “topologies” themselves are very different — one key difference is that a MapReduce job eventually finishes, whereas a topology processes messages forever (or until you kill it).

There are two kinds of nodes in a Storm cluster:

Master node (Nimbus)
The master node runs a daemon called “Nimbus” that is similar to Hadoop’s “JobTracker”. Nimbus is responsible for distributing code around the cluster, assigning tasks to machines, and monitoring for failures.


Nimbus service is an Apache Thrift service enabling you to submit the code in any programming language. This way, you can always utilize the language that you are proficient in, without the need of learning a new language to utilize Apache Storm.

Nimbus service relies on Apache ZooKeeper service to monitor the message processing tasks as all the worker nodes update their tasks status in Apache ZooKeeper service.

Worker nodes (Supervisor)
Each worker node runs a daemon called the “Supervisor”. The supervisor listens for work assigned to its machine and starts and stops worker processes as necessary based on what Nimbus has assigned to it. Each worker process executes a subset of a topology; a running topology consists of many worker processes spread across many machines.



All coordination between Nimbus and the Supervisors is done through a Zookeeper cluster. Additionally, the Nimbus daemon and Supervisor daemons are fail-fast and stateless. Even though stateless nature has its own disadvantages, it actually helps Storm to process real-time data in the best possible and quickest way.

Storm is not entirely stateless though. It stores its state in Apache ZooKeeper. Since the state is available in Apache ZooKeeper, a failed nimbus can be restarted and made to work from where it left. Usually, service monitoring tools like monit will monitor Nimbus and restart it if there is any failure.

Apache Storm also has an advanced topology called Trident Topology with state maintenance and it also provides a high-level API like Pig.

Components of a Storm cluster
Apache Storm

Topologies
To do realtime computation on Storm, you create what are called “topologies”. A topology is a graph of computation and is implemented as DAG (Directed Acyclic Graph) data structure.

Each node in a topology contains processing logic (bolts), and links between nodes indicate how data should be passed around between nodes (streams).

Apache Storm

When a topology is submitted to a Storm cluster, Nimbus service on master node consults the supervisor services on different worker nodes and submits the topology. Each supervisor, creates one or more worker processes, each having its own separate JVM. Each process runs within itself threads which we call Executors.

The thread/executor processes the actual computational tasks: Spout or Bolt.

Running a topology is straightforward. First, you package all your code and dependencies into a single jar. Then, you run a command like the following:

storm jar all-my-code.jar org.apache.storm.MyTopology arg1 arg2
Streams
Streams represent the unbounded sequences of tuples(collection of key-value pairs) where a tuple is a unit of data.



A stream of tuples flows from spout to bolt(s) or from bolt(s) to another bolt(s). There is various stream grouping techniques to let you define how the data should flow in topology like Global grouping etc.

Spouts
Spout is the entry point in a storm topology. It represents the source of data in Storm. Generally, spouts will read tuples from an external source and emit them into the topology. You can write spouts to read data from data sources such as database, distributed file systems, messaging frameworks or message queue as Kafka, from where it gets continuous data, converts the actual data into a stream of tuples & emits them to bolts for actual processing. Spouts run as tasks in worker processes by Executor threads.

Spouts can broadly be classified as following –

Reliable – These spouts have the capability to replay the tuples (a unit of data in the data stream). This helps applications achieve ‘at least once message processing’ semantic as in case of failures, tuples can be replayed and processed again. Spouts for fetching the data from messaging frameworks are generally reliable as these frameworks provide the mechanism to replay the messages.
Unreliable – These spouts don’t have the capability to replay the tuples. Once a tuple is emitted, it cannot be replayed irrespective of whether it was processed successfully or not. This type of spouts follows ‘at most once message processing’ semantic.
Bolts
All processing in topologies is done in bolts. Bolts can do anything from filtering, functions, aggregations, joins, talking to databases, and more.

Bolts can do simple stream transformations. Doing complex stream transformations often requires multiple steps and thus multiple bolts. For example, transforming a stream of tweets into a stream of trending images requires at least two steps: a bolt to do a rolling count of retweets for each image, and one or more bolts to stream out the top X images (you can do this particular stream transformation in a more scalable way with three bolts than with two).

Image result for bolt in storm

Bolts can also emit more than one stream.

What makes a running topology: worker processes, executors and tasks
Storm distinguishes between the following three main entities that are used to actually run a topology in a Storm cluster:

Worker processes
Executors (threads)
Tasks
Here is a simple illustration of their relationships:

The relationships of worker processes, executors (threads) and tasks in Storm

A worker process executes a subset of a topology. A worker process belongs to a specific topology and may run one or more executors for one or more components (spouts or bolts) of this topology. A running topology consists of many such processes running on many machines within a Storm cluster.

An executor is a thread that is spawned by a worker process. It may run one or more tasks for the same component (spout or bolt).

A task performs the actual data processing — each spout or bolt that you implement in your code executes as many tasks across the cluster. The number of tasks for a component is always the same throughout the lifetime of a topology, but the number of executors (threads) for a component can change over time. This means that the following condition holds true: #threads = #tasks. By default, the number of tasks is set to be the same as the number of executors, i.e. Storm will run one task per thread.

This pretty much sums up the architecture of Apache Storm. Hope it was helpful.
-------------
Zookeeper API
There are 6 primary operations exposed by the API:

create /path data    –  Creates a znode named with /path and containing data
delete /path     –  Deletes the znode /path
exists /path     – Checks whether /path exists
setData /path data    –  Sets the data of znode /path to data
getData /path    –  Returns the data in /path
getChildren /path    – Returns the list of children under /path



kafka
------
offset commit


enable.auto.commit
auto.commit.interval.ms

# Map-Reduce-Programs
Map reduce programs

This repository contains the different Map Reduce programs . Below topics are covered in these examples

1)Partitioner in Map Reduce

2)Combiner in Map Reduce

3)Custom Input for Map Reduce

4)Secondary Sort in Map Reduce

Hbasse

1

To understand what is column oriented database, it is better to contrast it with row oriented database.

Row oriented databases (e.g. MS SQL Server and SQLite) are designed to efficiently return data for an entire row. It does it by storing all the columns values of a row together. Row-oriented databases are well-suited for OLTP systems (e.g., retail sales, and financial transaction systems).

Column oriented databases are designed to efficiently return data for a limited number of columns. It does it by storing all of the values of a column together. Two widely used Column oriented databases are Apache Hbase and Google BigTable (used by Google for its Search, Analytics, Maps and Gmail). They are suitable for the big data projects. A column oriented database will excel at read operations on a limited number of columns, however write operation will be expensive compared to row oriented databases.--------------

-------------------
Hive


The Metastore Database
The metastore database is an important aspect of the Hive infrastructure. It is a separate database, relying on a
traditional RDBMS such as MySQL or PostgreSQL, that holds metadata about Hive databases, tables, columns, partitions,
and Hadoop-specific information such as the underlying data files and HDFS block locations.
8 | Apache Hive Guide
Using Apache Hive with CDH
The metastore database is shared by other components. For example, the same tables can be inserted into, queried,
altered, and so on by both Hive and Impala. Although you might see references to the “Hive metastore”, be aware
that the metastore database is used broadly across the Hadoop ecosystem, even in cases where you are not using Hive
itself.
The metastore database is relatively compact, with fast-changing data. Backup, replication, and other kinds of
management operations affect this database. See Configuring the Hive Metastore on page 19 for details about
configuring the Hive metastore.
Cloudera recommends that you deploy the Hive metastore, which stores the metadata for Hive tables and partitions,
in “remote mode.” In this mode the metastore service runs in its own JVM process and other services, such as
HiveServer2, HCatalog, and Apache Impala communicate with the metastore using the Thrift network API.
See Starting the Metastore on page 34 for details about starting the Hive metastore service.


HiveServer2
HiveServer2 is a server interface that enables remote clients to submit queries to Hive and retrieve the results. It
replaces HiveServer1, which has been deprecated and will be removed in a future release of CDH. HiveServer2 supports
multi-client concurrency, capacity planning controls, Sentry authorization, Kerberos authentication, LDAP, SSL, and
provides better support for JDBC and ODBC clients.
HiveServer2 is a container for the Hive execution engine. For each client connection, it creates a new execution context
that serves Hive SQL requests from the client. It supports JDBC clients, such as the Beeline CLI, and ODBC clients. Clients
connect to HiveServer2 through the Thrift API-based Hive service.


-----------------------------------


The number of map tasks is dependent on the data volume, block size and split size. For example: If you have block size 128 MB and your file size is 1 GB then there will be 8 number of map tasks. You can control it by using split size.

And number of reducers in a Hive job is 1 by default. You have to update it via configuration


  <name>mapred.reduce.tasks</name>
  <value>-1</value>
 
---------------------------------------

In Hive terminal type:

hive> set hive.metastore.warehouse.dir;
(it will print the path)

------------------------------------------
Kafka


auto.offset.reset
This property controls the behavior of the consumer when it starts reading a partition
for which it doesn’t have a committed offset or if the committed offset it has is invalid
(usually because the consumer was down for so long that the record with that offset
was already aged out of the broker). The default is “latest,” which means that lacking a
valid offset, the consumer will start reading from the newest records (records that
were written after the consumer started running). The alternative is “earliest,” which
means that lacking a valid offset, the consumer will read all the data in the partition,
starting from the very beginning.

enable.auto.commit
We discussed the different options for committing offsets earlier in this chapter. This
parameter controls whether the consumer will commit offsets automatically, and
defaults to true. Set it to false if you prefer to control when offsets are committed,
which is necessary to minimize duplicates and avoid missing data. If you set
enable.auto.commit to true, then you might also want to control how frequently
offsets will be committed using auto.commit.interval.ms.


https://dzone.com/articles/kafka-clients-at-most-once-at-least-once-exactly-o


------------------------------------------------------

2

Your job may or may not need reducers, it depends on what are you trying to do. When there are multiple reducers, the map tasks partition their output, each creating one partition for each reduce task. There can be many keys (and their associated values) in each partition, but the records for any given key are all in a single partition. One rule of thumb is to aim for reducers that each run for five minutes or so, and which produce at least one HDFS block’s worth of output. Too many reducers and you end up with lots of small files.

-------------------------------------------------------------------

3.4. Compile-time type safety
DataFrame- There is a case if we try to access the column which is not on the table. Then, dataframe APIs does not support compile-time error.

DataSets- Datasets offers compile-time type safety.


--------------------------------------------------------------

The memory you need to assign to the driver depends on the job.

If the job is based purely on transformations and terminates on some distributed output action like rdd.saveAsTextFile, rdd.saveToCassandra, ... then the memory needs of the driver will be very low. Few 100's of MB will do. The driver is also responsible of delivering files and collecting metrics, but not be involved in data processing.

If the job requires the driver to participate in the computation, like e.g. some ML algo that needs to materialize results and broadcast them on the next iteration, then your job becomes dependent of the amount of data passing through the driver. Operations like .collect,.take and takeSample deliver data to the driver and hence, the driver needs enough memory to allocate such data.

e.g. If you have an rdd of 3GB in the cluster and call val myresultArray = rdd.collect, then you will need 3GB of memory in the driver to hold that data plus some extra room for the functions mentioned in the first paragraph.


----------------------------------------


Characteristics of Partitions in Apache Spark
Every machine in a spark cluster contains one or more partitions.
The number of partitions in spark are configurable and having too few or too many partitions is not good.
Partitions in Spark do not span multiple machines.


-----------------------------------------------------

-1

It depends on how your data is partitioned. In Spark SQL, when you read data from the source the number of partitions will depend on the size of your dataset, number of input files and on the number of cores that you have available. Spark will determine how many partitions should be created and so in the first stage of your job this will be the number of 'mappers tasks'. Then if you perform transformation that induces shuffle (like groupBy, join, dropDuplicates, ...), the number of 'reducers tasks' will be 200 by default, because Spark will create 200 partitions. You can change that by this setting:

sparkSession.conf.set("spark.sql.shuffle.partitions", n)
where n is the number of partitions that you want to use (number of tasks that you want to have after each shuffle). Here is a link to configuration options in Spark documentation which mentions this setting.

-----------------------------------------------------------

Simplistic view: Partition vs Number of Cores

When you invoke an action an RDD,

A "Job" is created for it. So, Job is a work submitted to spark.
Jobs are divided in to "STAGE" based n the shuffle boundary!!!
Each stage is further divided to tasks based on the number of partitions on the RDD. So Task is smallest unit of work for spark.
Now, how many of these tasks can be executed simultaneously depends on the "Number of Cores" available!!!


-------------------


Partition (or task) refers to a unit of work. If you have a 200G hadoop file loaded as an RDD and chunked by 128M (Spark default), then you have ~2000 partitions in this RDD. The number of cores determines how many partitions can be processed at any one time, and up to 2000 (capped at the number of partitions/tasks) can execute this RDD in parallel.


----------------------------

The memory you need to assign to the driver depends on the job.

If the job is based purely on transformations and terminates on some distributed output action like rdd.saveAsTextFile, rdd.saveToCassandra, ... then the memory needs of the driver will be very low. Few 100's of MB will do. The driver is also responsible of delivering files and collecting metrics, but not be involved in data processing.

If the job requires the driver to participate in the computation, like e.g. some ML algo that needs to materialize results and broadcast them on the next iteration, then your job becomes dependent of the amount of data passing through the driver. Operations like .collect,.take and takeSample deliver data to the driver and hence, the driver needs enough memory to allocate such data.

e.g. If you have an rdd of 3GB in the cluster and call val myresultArray = rdd.collect, then you will need 3GB of memory in the driver to hold that data plus some extra room for the functions mentioned in the first paragraph.



----------------------------------------------

The 2 jobs which are running are Spark Thrift servers which will run as yarn applications. There is no need to worry. If you stop spark thrift servers then you won't see them running.

Spark2 thrift server will be running with app name "Thrift JDBC/ODBC Server"

Spark/Spark1 thrift server will be running with app name "org.apache.spark.sql.hive.thriftserver.HiveThriftServer2".



--------------------------------------------

Name service— A naming service is a service that maps a name to some information associated with that name. A telephone directory is a name service that maps the name of a person to his/her telephone number. In the same way, a DNS service is a name service that maps a domain name to an IP address. In your distributed system, you may want to keep a track of which servers or services are up and running and look up their status by name.
Locking— To allow for serialized access to a shared resource in your distributed system, you may need to implement distributed mutexes.
Synchronization— Hand in hand with distributed mutexes is the need for synchronizing access to shared resources. Whether implementing a producer-consumer queue or a barrier.
Configuration management— The configuration of your distributed system must centrally stored and managed.This means that any new nodes joining should pick up the up-to-date centralized configuration as soon as they join the system.
Leader election— Your distributed system may have to deal with the problem of nodes going down, and you may want to implement an automatic fail-over strategy. You can do this by leader election.


World without ZooKeeper
When designing a distributed system, there is typically a need for designing and developing some coordination services:

Name service— A naming service is a service that maps a name to some information associated with that name. A telephone directory is a name service that maps the name of a person to his/her telephone number. In the same way, a DNS service is a name service that maps a domain name to an IP address. In your distributed system, you may want to keep a track of which servers or services are up and running and look up their status by name.
Locking— To allow for serialized access to a shared resource in your distributed system, you may need to implement distributed mutexes.
Synchronization— Hand in hand with distributed mutexes is the need for synchronizing access to shared resources. Whether implementing a producer-consumer queue or a barrier.
Configuration management— The configuration of your distributed system must centrally stored and managed.This means that any new nodes joining should pick up the up-to-date centralized configuration as soon as they join the system.
Leader election— Your distributed system may have to deal with the problem of nodes going down, and you may want to implement an automatic fail-over strategy. You can do this by leader election.

-------------------------------------------------------------------------------------

4. How Spark Builds a DAG?
There are following steps of the process defining how spark creates a DAG:

Very first, the user submits an apache spark application to spark.
Than driver module takes the application from spark side.
The driver performs several tasks on the application. That helps to identify whether transformations and actions are present in the application.
All the operations are arranged further in a logical flow of operations, that arrangement is DAG.
Than DAG graph converted into the physical execution plan which contains stages.
As we discussed earlier driver identifies transformations. It also sets stage boundaries according to the nature of transformation. There are two types of transformation process applied on RDD: 1. Narrow transformations 2. Wide transformations. Let’s discuss each in brief :
Narrow Transformations – Transformation process like map() and filter() comes under narrow transformation. In this process, it does not require to shuffle the data across partitions.
Wide Transformations – Transformation process like ReduceByKey comes under wide transformation. In this process, it is required shuffling the data across partitions.
As wide Transformation requires data shuffling that shows it results in stage boundaries.

After all, DAG scheduler makes a physical execution plan, which contains tasks. Later on, those tasks are joint to make bundles to send them over the cluster.
-------------------------------------------------------------------------------------

Group Membership in ZooKeeper
One way of understanding ZooKeeper is to think of it as providing a high-availability
filesystem. It doesn’t have files and directories, but a unified concept of a node, called a
znode, that acts both as a container of data (like a file) and a container of other znodes
(like a directory). Znodes form a hierarchical namespace, and a natural way to build a
membership list is to create a parent znode with the name of the group and child znodes
with the names of the group members (servers).

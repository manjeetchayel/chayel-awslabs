# CloudFormation - Presto on EMR with remote metastore

This AWS CloudFormation template spins up all the resource you need to play with Presto on EMR.

Resources launched:
  - Amazon EMR - version emr-4.6.0
  - Apache Hive and Presto
  - Amazon RDS MySQL Instance - this is remote Hive metastore for Presto

Pre-requiste:
  - AWS account
  - VPC with atleast 2 subnets

## What is Apache Presto?
[Presto] is an open-source distributed SQL query engine optimized for low-latency, ad-hoc analysis of data. It supports the ANSI SQL standard, including complex queries, aggregations, joins, and window functions. Presto can process data from multiple data sources including the Hadoop Distributed File System (HDFS) and Amazon S3.

### Instructions
 - From your browser open [AWS CloudFormation] console 
 - Download the CloudFormation template from this repository
 - Click on Create Stack and upload the template to Amazon S3  
 - In the next page on parameters fill in the details 
 
| Parmeters        | Description  |
| ------------- |:-------------:| 
| DBAllocatedStorage     | Size of your RDS MySQL metastore (default: 5Gb) | 
| DBClass      | Instance type for your metastore (default: db.m1.small)      |  
| DBname | Database name      |  
| DBAllocatedStorage |  Size of your RDS MySQL metastore (default: 5Gb)| 
| DBClass | Instance type for your metastore (default: db.m1.small)| 
| DBname |  Database name| 
| DBPassword |  Database admin password for your metastore| 
| DBUsername |  Database admin account username| 
| EMRKeyPair |  Name of existing EC2 KeyPair to enable SSH access to your EMR cluster| 
| myEMRCoreSize |  Number of core instances in your EMR cluster (default: 5)| 
| myEMRInstanceType |  Instance type for Core (default: m3.xlarge)| 
| myEMRSubnetGroup | Your VPC subnet where you want to launch EMR cluster | 
| CDIR |  CDIR of your EMR subnet  (allows connection on port 3306 from EMR cluster to RDS metastore)| 
| Subnets | Subnets in your VPC| 
| VPCId |  VPC where your EMR cluster and RDS instance will be launched| 

[Presto]: <https://aws.amazon.com/elasticmapreduce/details/presto/>
[AWS CloudFormation]: <https://console.aws.amazon.com/cloudformation/home>

Once your Stack is ready it show output the EMR master DNS

### Playing with Presto

1. Login to your EMR cluster
```
     ssh -i <<YOUR-EC2-KEY-PAIR>> hadoop@<<EMR-MASTER-DNS>>
```

2. Type “hive” in the command line to enter Hive interactive mode and run the following commands:

```sql
CREATE EXTERNAL TABLE wikistats_parq (
language STRING,
page_title STRING,
hits BIGINT,
retrived_size BIGINT
)
STORED AS PARQUET
LOCATION 's3://emr.presto.airpal/wikistats/parquet';
```

3. Now using Presto you can play around with the wikistats_parq table you have created.

Connect to Presto

```sh
 $ presto-cli --catalog hive --schema default
```

``Sample queries:``

```sql
SELECT count(*) from wikistats_parq;
```

```sql
SELECT language, count(*) as cnt
FROM wikistats_parq
group by language;
```

```sql
SELECT language,page_title, AVG(hits) AS avg_hits
FROM wikistats_parq
GROUP BY language, page_title
ORDER BY avg_hits DESC
LIMIT 10;
```

```sql
SELECT language, page_title, AVG(hits) AS avg_hits
FROM wikistats_parq
WHERE language = 'en'
AND page_title NOT IN ('Main_Page',  '404_error/')
AND page_title NOT LIKE '%Special%'
AND page_title NOT LIKE '%index%'
AND page_title NOT LIKE '%Search%'
AND NOT regexp_like(page_title, '\%20')
GROUP BY language, page_title
ORDER BY avg_hits DESC
LIMIT 10;
```

4. You can verfiy the RDS MySQL metastore has your table metadata by doing a simple query against your DB instance, using the below SQL

```sql
[hadoop@ip-172-31-43-48 ~]$ mysql -h emr-metastore-mydatabase.cbh0nfkgt17p.us-east-1.rds.amazonaws.com -u hive -e "Select * from hive.TBLS;" -p
Enter password:
````

Result will be something like below
```
+--------+-------------+-------+------------------+--------+-----------+-------+----------------+----------------+--------------------+--------------------+----------------+
| TBL_ID | CREATE_TIME | DB_ID | LAST_ACCESS_TIME | OWNER  | RETENTION | SD_ID | TBL_NAME       | TBL_TYPE       | VIEW_EXPANDED_TEXT | VIEW_ORIGINAL_TEXT | LINK_TARGET_ID |
+--------+-------------+-------+------------------+--------+-----------+-------+----------------+----------------+--------------------+--------------------+----------------+
|      1 |  1463168125 |     1 |                0 | hadoop |         0 |     1 | wikistats_parq | EXTERNAL_TABLE | NULL               | NULL               |           NULL |
+--------+-------------+-------+------------------+--------+-----------+-------+----------------+----------------+--------------------+--------------------+----------------+
```
### Cleanup
Don't forget to cleanup your resources so you don't get billed after you have played around with it.

# Spring Akka
Example project to use Akka with Spring in Java.
This can be used as base project for Java based state machine in Akka.

export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-11.0.15.1.jdk/Contents/Home/

export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.0.1.jdk/Contents/Home



export SSL_VERSION=TLSv1_2


export SSL_VALIDATE=false

    cqlsh.py stfc-ur-poc.cassandra.cosmos.azure.com 10350 -u stfc-ur-poc -p boALM0lE9TSnC8WT8NbYxLywHYx1oAulO0wx2eTEinEvVY0CowdaauGhFOmCphuKUJuQuJgB584MACDbqwbK5A== --ssl

select data_center from system.local;

# creating the tables in cassandra

CREATE TABLE IF NOT EXISTS akka_snapshot.snapshots (
  persistence_id text,
  sequence_nr bigint,
  timestamp bigint,
  ser_id int,
  ser_manifest text,
  snapshot_data blob,
  snapshot blob,
  meta_ser_id int,
  meta_ser_manifest text,
  meta blob,
  PRIMARY KEY (persistence_id, sequence_nr))
  WITH CLUSTERING ORDER BY (sequence_nr DESC) AND gc_grace_seconds =0
  AND compaction = {
    'class' : 'SizeTieredCompactionStrategy',
    'enabled' : true,
    'tombstone_compaction_interval' : 86400,
    'tombstone_threshold' : 0.2,
    'unchecked_tombstone_compaction' : false,
    'bucket_high' : 1.5,
    'bucket_low' : 0.5,
    'max_threshold' : 32,
    'min_threshold' : 4,
    'min_sstable_size' : 50
    };
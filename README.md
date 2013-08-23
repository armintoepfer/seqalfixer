# ProfileSim
Creates a profile file for SimSeq, given a BAM alignment and a set of references in FASTA format.
Only conserved regions across the references are taken into consideration.
- - -

#### PREREQUISITES TO RUN:
 - JDK 7 (http://jdk7.java.net/)

## RUN:
 `java -jar ProfileSim.jar -i alignment.bam -g references.fasta`

### Restrict to a certain area of the genome
 `java -jar ProfileSim.jar -i alignment.bam -g references.fasta -r 300-4590`

## Technical details
#####To minimize the memory consumption and the number of full garbage collector executions, use:
`java -XX:NewRatio=9 -jar ProfileSim.jar`

#####If your dataset is very large and you run out of memory, increase the heapspace with:
`java -XX:NewRatio=9 -Xms2G -Xmx10G -jar ProfileSim.jar`

####On multicore systems:
`java -XX:+UseParallelGC -XX:NewRatio=9 -Xms2G -Xmx10G -jar ProfileSim.jar`

####On multi-CPU systems:
`java -XX:+UseParallelGC -XX:+UseNUMA -XX:NewRatio=9 -Xms2G -Xmx10G -jar ProfileSim.jar`

#####Unix wrapper:
`function psim() { java -XX:+UseParallelGC -Xms2g -Xmx10g -XX:+UseNUMA -XX:NewRatio=9 -jar ~/ProfileSim.jar $*; }`

### Help:
 Help can be shown by running without additional parameters:
  `java -jar ProfileSim.jar`

## PREREQUISITES COMPILE (only for dev):
 - Maven 3 (http://maven.apache.org/)

## INSTALL (only for dev):
    cd ProfileSim
    mvn -DartifactId=samtools -DgroupId=net.sf -Dversion=1.8.9 -Dpackaging=jar -Dfile=src/main/resources/jars/sam-1.89.jar -DgeneratePom=false install:install-file
    mvn clean package
    java -jar ProfileSim/target/ProfileSim.jar

# CONTACT:
    Armin Töpfer
    armin.toepfer (at) gmail.com
    http://www.bsse.ethz.ch/cbg/people/toepfera

# LICENSE:
 GNU GPLv3 http://www.gnu.org/licenses/gpl-3.0

# <b>S</b>equence<b>A</b>lignment<b>F</b>ixer

### CONTENT:
This java command line application is a toolbox, combining a multitude of alignment statistics and fixes for Next Generation Sequencing (NGS) data.

### CITATION:
If you use <b>S</b>equence<b>A</b>lignment<b>F</b>ixer, please cite <i>Töpfer A.</i> https://github.com/armintoepfer/seqalfixer

### DOWNLOAD:
Please get the latest binary at https://sourceforge.net/projects/seqalfixer/releases

### FEATURES:
 - Select only a region for analysis
 - Coverage plot
 - SNV entropy landscape plot
 - Consensus sequence:
 1) Gapped, including major in-frame deletions
 2) In-frame insertions are including, with minmal coverage of `-insertions INT`
 3) Minimal coverage with `-coverage INT`

- - -

#### PREREQUISITES TO RUN:
 - JDK 7 (http://jdk7.java.net/)

## RUN:
 `java -jar SequenceAlignmentFixer.jar -i alignment.bam`
 Reads need to be properly aligned.

### Reconstruct specific region with respect to reference genome numbering
 `-r 790-2292`

### Consensus
 `--consensus` 

### Minimal coverage of insertion
 `-insertions INT [default: do not include insertions]` 

### Minimal coverage
 `-coverage INT [default: 0]`

### Plots
 Summary statistics can be produced with R:
`R CMD BATCH support/plots.R`

## Technical details
#####To minimize the memory consumption and the number of full garbage collector executions, use:
`java -XX:NewRatio=9 -jar SequenceAlignmentFixer.jar`

#####If your dataset is very large and you run out of memory, increase the heapspace with:
`java -XX:NewRatio=9 -Xms2G -Xmx10G -jar SequenceAlignmentFixer.jar`

####On multicore systems:
`java -XX:+UseParallelGC -XX:NewRatio=9 -Xms2G -Xmx10G -jar SequenceAlignmentFixer.jar`

####On multi-CPU systems:
`java -XX:+UseParallelGC -XX:+UseNUMA -XX:NewRatio=9 -Xms2G -Xmx10G -jar SequenceAlignmentFixer.jar`

#####Unix wrapper:
`function saf() { java -XX:+UseParallelGC -Xms2g -Xmx10g -XX:+UseNUMA -XX:NewRatio=9 -jar SequenceAlignmentFixer.jar $*; }`

### Help:
 Further help can be showed by running without additional parameters:
  `java -jar SequenceAlignmentFixer.jar`

## PREREQUISITES COMPILE (only for dev):
 - Maven 3 (http://maven.apache.org/)

## INSTALL (only for dev):
    cd SequenceAlignmentFixer
    mvn -DartifactId=samtools -DgroupId=net.sf -Dversion=1.9.6 -Dpackaging=jar -Dfile=src/main/resources/jars/sam-1.96.jar -DgeneratePom=false install:install-file
    mvn clean package
    java -jar SequenceAlignmentFixer/target/SequenceAlignmentFixer.jar

# CONTACT:
    Armin Töpfer
    armin.toepfer (at) gmail.com
    http://www.bsse.ethz.ch/cbg/people/toepfera

# LICENSE:
 GNU GPLv3 http://www.gnu.org/licenses/gpl-3.0

## How to use seqalfixer in a production mode
#### Requirements
 - [GATK](http://www.broadinstitute.org/gatk/)
 - [Picard tools](https://sourceforge.net/projects/picard/files/latest/download?source=files)
 - [Samtools](https://sourceforge.net/projects/samtools/files/latest/download?source=files)
 - [BWA](https://sourceforge.net/projects/bio-bwa/files/latest/download?source=files)
 - [sickle](https://github.com/najoshi/sickle)

Install __samtools__, __sickle__, and __BWA__ and link it in __$PATH__.  
Export enviroment variables for __GATK__ and __picard__:
```bash
export PICARD_HOME=~/tools/picard-tools/
export GATK_HOME=~/tools/gatk/
export IDF_HOME=~/tools/InDelFixer/
```

###Realign
Realign against the consensus sequence and create alignment summaries:  
`realignSingle reads.fastq genome.fasta`

```bash
function realignSingle() {
    function alignMemUGS() {
        bwa index -a bwtsw $2;
        bwa mem -w 100 -d 1 -r 5 -B 5 -A 3 -O 25 -E 1 -t 79 $2 $1 > aln.sam;
        samtools faidx $2;
        samtools view -q 20 -F 4 -bt $2.fai aln.sam > aln.bam;
        samtools sort aln.bam reads;
        rm aln*;
        samtools index reads.bam
    };
    function alignMem() {
        bwa index -a bwtsw $2;
        bwa mem -t 79 $2 $1 > aln.sam;
        samtools faidx $2;
        samtools view -q 20 -F 4 -bt $2.fai aln.sam > aln.bam;
        samtools sort aln.bam reads;
        rm aln*;
        samtools index reads.bam
    };
    mkdir tmp_sequences;
    sickle se -f $1 -o tmp_sequences/filtered1.fastq -q 20 -l 200 -n -t sanger;
    alignMemUGS tmp_sequences/filtered1.fastq $2;
    java -jar $PICARD_HOME/CreateSequenceDictionary.jar R=$2 O=${2%.*}.dict 2> /dev/null;
    java -jar $PICARD_HOME/AddOrReplaceReadGroups.jar I=reads.bam O=group.bam LB=bla PL=LS454 PU=barcode SM=XLR;
    samtools faidx $2;
    samtools index group.bam;
    rm reads.bam*;
    java -Xmx2g -jar $GATK_HOME/GenomeAnalysisTK.jar -T LeftAlignIndels -R $2 -I group.bam -rf ReassignMappingQuality -DMQ 60 -o reads.bam;
    rm -rf group.bam*;
    samtools index reads.bam;
    saf -i reads.bam --consensus -coverage 10 -insertions 10;
    rm reads.bam;
    alignMem tmp_sequences/filtered1.fastq consensus.fasta;
    rm -rf tmp_sequences;
    java -jar $PICARD_HOME/CreateSequenceDictionary.jar R=consensus_ungapped.fasta O=consensus_ungapped.dict;
    java -jar $PICARD_HOME/AddOrReplaceReadGroups.jar I=reads.bam O=group.bam LB=bla PL=LS454 PU=barcode SM=XLR;
    samtools faidx consensus_ungapped.fasta;
    samtools index group.bam;
    rm reads.bam*;
    java -Xmx2g -jar $GATK_HOME/GenomeAnalysisTK.jar -T LeftAlignIndels -R consensus_ungapped.fasta -I group.bam -rf ReassignMappingQuality -DMQ 60 -o reads.bam;
    samtools index reads.bam
    R CMD BATCH support/plots.R
    rm reads.bai group.bam* *.dict consensus.fasta.* plots.Rout
}
```

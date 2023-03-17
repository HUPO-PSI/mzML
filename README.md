## mzML - Reporting Spectra Information in MS-based experiments


## General

Mass spectrometry is a popular method to analyse bio-molecules by measuring the intact mass-to-charge ratios of their in-situ generated ionised forms or the mass-to-charge ratios of in-situ-generated fragments of these ions. The resulting mass spectra are used for a variety of purposes, among which is the identification, characterization, and absolute or relative quantification of the analysed molecules. The processing steps to achieve these goals typically involve semi-automatic computational analysis of the recorded mass spectra and sometimes also of the associated metadata (e.g., elution characteristics if the instrument is coupled to a chromatography system). The result of the processing can be assigned a score, rank or confidence measure.

Differences inherent in the use of a variety of instruments, different experimental conditions under which analyses are performed, and potential automatic data preprocessing steps by the instrument software can influence the actual measurements and therefore the results after processing. Additionally, most instruments output their acquired data in a very specific and often proprietary format. These proprietary formats are then typically transformed into so-called peak lists to be analysed by identification and characterisation software. Data reduction such as peak centroiding and deisotoping is often performed during this transformation from proprietary formats to peak lists. In addition, these peak list file formats lack information about the precursor MS signals and about the associated metadata (i.e., instrument settings and description, acquisition mode, etc) compared to the files they were derived from. The peak lists are then used as inputs for subsequent analysis. The many different and often proprietary formats make integration or comparison of mass spectrometer output data difficult or impossible, and the use of the heavily processed and data-poor peak lists is often suboptimal.

This document addresses this problem with the presentation of the mzML XML format, which is designed to hold the data output of a mass spectrometer as well as a systematic description of the conditions under which this data was acquired and transformed. The following target objectives can be defined for the format:

     1- The discovery of relevant results, so that, for example, data sets in a database or public repository that use a
        particular technique or combination of techniques can be identified and studied by experimentalists during experiment
         design or data analysis.

     2- The sharing of best practice, whereby, for example, approaches that have been successful at analysing low abundance
        analytes can be captured alongside the results produced.
     
     3- The evaluation of results, whereby, for example, the number and quality of the spectra recorded from a sample can be
        assessed in the light of the experimental conditions.

     4-	The sharing of data sets, so that, for example, public repositories can import or export data, multi-site projects
        can share results to support integrated analysis, or meta-analyses can be performed by third parties from previously
        published data.
     
     5- The most comprehensive support of the instruments output, so that data can be captured in profile mode, centroid
        mode, and other relevant forms of biomolecular mass spectrometry data representation

The primary focus of the model is to support long-term archiving and sharing, rather than day-to-day laboratory management, although the model is extensible to support context-specific details.

The description of mass spectrometry data output and its experimental context requires that models include: (i) the actual data acquired, to a sufficient precision, as well as its associated metadata; and (ii) an adequate description of the instrument characteristics, its configuration and possible preprocessing steps applied. This document details both these parts, as they are required to support the tasks T1 to T5 above.
 
This document defines a specification and is not a tutorial. As such, the presentation of technical details is deliberately direct. The role of the text is to describe the schema model and justify design decisions made. This document does not provide comprehensive examples of the schema in use. Example documents are provided separately and should be examined in conjunction with this document. It is anticipated that tutorial material will be developed in the future to aid implementation. Although the present specification document describes constraints and guidelines related to the content of an mzML document as well as the availability of tools helping to read and write mzML, it does not describe any implementation constraints or specifications such as coding language or operating system for software that will generate and/or read mzML data. 

When you use mzML format, please cite the following publication:

Martens L., Chambers M., Sturm M., Kessner D., Levander F., Shofstahl J., Tang W.H., Römpp A., Neumann S., Pizarro A.D., Montecchi-Palazzi L., Tasman N., Coleman M., Reisinger F., Souda P., Hermjakob H., Binz P.A., Deutsch E.W..mzML--a community standard for mass spectrometry data. Mol Cell Proteomics. 2011 Jan;10(1):R110.000133

## Specification documents

**Version 1.1.0 (June 2014):**

  > Specification document [docx](https://github.com/HUPO-PSI/mzML/blob/master/specification_document/mzML1.1.0_specificationDocument.doc),


## Example Files
Several example of the format can be download from the next link [Examples](https://github.com/HUPO-PSI/mzML/tree/master/examples)

## Tools, Libraries, readers and exporters

1- [jmzML](http://github.com/PRIDE-UTILITIES/jmzML/): Java API for reading and writing mzML (**IMPORT AND EXPORT**) 

2- [ms-data-core-api](http://github.com/PRIDE-UTILITIES/ms-data-core-api/): Java API for reading PSI standard file formats.



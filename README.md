## mzIdentML - Reporting Identifications in MS-based Proteomics Experiments


## General

A large number of different proteomics search engines are available that produce output in a variety of different formats. It is intended that **mzIdentML** will provide a common format for the export of identification results from any search engine. The format was originally developed under the name AnalysisXML as a format for several types of computational analyses performed over mass spectra in the proteomics context. It has been decided to split development into two formats: mzIdentML for peptide and protein identification (described here) and mzQuantML .

mzIdentML has been developed with a view to supporting the following general tasks:

 1- The discovery of relevant results, so that, for example, data sets in a database that use a particular technique or combination of techniques can be identified and studied by experimentalists during experiment design or data analysis.
 2- The sharing of best practice, so that, for example, analyses that have been particularly successful at identifying a certain group of peptides/proteins can be interpreted by consumers of the data.
 3- The evaluation of results, so that, for example, sufficient information is provided about how a particular analysis was performed to allow the results to be critically evaluated.
 4- The sharing of data sets, so that, for example, public repositories can import or export data, or multi-site projects can share results to support integrated analysis.
 5- The creation of a format for input to analysis software, for example, allowing software to be designed that provides a meta-score over the output from several search engines.
 6- An internal format for pipeline analysis software, for example, allowing analysis software to store intermediate results from different stages of an identification pipeline, prior to the final results being assembled in a single mzIdentML file.

The description of the analysis of proteomics mass spectra requires that models describe: (i) the identity and configuration of software used to perform the analysis and the protocol used to apply this software to the analysis; (ii) the identity of molecules; and (iii) the way in which these relate to other techniques to form a proteomics workflow. Most of this document is concerned with (i) and (ii) – the identification of the key features of different techniques that are required to support the tasks T1 to T5 above. Models of type (iii) are created by developments in the context of the Functional Genomics Experimental Object Model (FuGE), which defines model components of relevance to a wide range of experimental techniques. Several components from FuGE are re-used in the development of mzIdentML.

This document presents a specification, not a tutorial. As such, the presentation of technical details is deliberately direct. The role of the text is to describe the model and justify design decisions made. The document does not discuss how the models should be used in practice, consider tool support for data capture or storage, or provide comprehensive examples of the models in use. It is anticipated that tutorial material will be developed when the specification is stable. 


When you use mzIdentML format, please cite the following publication:

Jones A.R., Eisenacher M., Mayer G., Kohlbacher O., Siepen J., Hubbard S.J., Selley J.N., Searle B.C., Shofstahl J., Seymour S.L., Julian R., Binz P.A., Deutsch E.W., Hermjakob H., Reisinger F., Griss J., Vizcaíno J.A., Chambers M., Pizarro A., Creasy D. The mzIdentML data standard for mass spectrometry-based proteomics results. Mol Cell Proteomics. 2012 Jul;11(7):M111.014381 [pdf](http://www.mcponline.org/content/11/7/M111.014381.full.pdf+html)


## Specification documents

**Version 1.1.0 (June 2014):**

  > Specification document [docx](https://github.com/HUPO-PSI/mzIdentML/blob/master/specification_document-releases/specdoc1_1/mzIdentML1.1.0.doc),

  > The 20 minute guide to mzTab [docx](https://github.com/HUPO-PSI/mzIdentML/blob/master/specification_document-releases/specdoc1_1/TenMinuteGuideToImplementingMzidentml.docx)

## Example Files
Several example of the format can be download from the next link [Examples](https://github.com/HUPO-PSI/mzIdentML/tree/master/examples/1_1examples)
[PRIDE Toolsuite Examples](https://github.com/PRIDE-Toolsuite/inspector-example-files/tree/master/mzIdentML)

## Tools, Libraries, readers and exporters

1- [jmzIdentML](http://github.com/PRIDE-UTILITIES/jmzidentml/): Java API for reading and writing mzIdentML (**IMPORT AND EXPORT**) 

2- [ms-data-core-api](http://github.com/PRIDE-UTILITIES/ms-data-core-api/): Java API for reading PSI standard file formats.

3- [Mascot](http://www.matrixscience.com/help/export_help.html#MZIDENTML): mzIdentML Version 1.0 available in Mascot version 2.3, mzid stable version 1.1 available in Mascot version 2.4 (**EXPORT** )

4- OMSSA: [http://code.google.com/p/mzidentml-lib/](http://code.google.com/p/mzidentml-lib/) from the U.Liverpool group ( **EXPORT**)

5- [MSGF+](http://proteomics.ucsd.edu/Software/MSGFPlus.html): Full support for results into mzid 1.1 (**EXPORT**)

6- [Peaks](http://www.bioinfor.com/): Native export of mzIdentML version 1.1 (**EXPORT**)

7- [Phenyx](http://www.genebio.com/products/phenyx/): Exporter to mzIdentML v1.1 now available - contact GeneBio for details. ( **EXPORT**)

8- [ProCon](http://www.ruhr-uni-bochum.de/mpc/software/ProCon/index.html.en) (EXPORT):

     - conversion of SEQUEST resp. Comet *.out files/folder information into mzIdentML 1.0 / 1.1 
       (SpectrumIdentificationResults only)

     - conversion of ProteinScape 1.3 results into mzIdentML

     - conversion of ProteinScape2.1 results into mzIdentML 1.1

     - conversion of ProteomeDiscoverer 1.1 + 1.2 *.msf and *.prot.xml files (Thermo) resp. ProteomeDiscoverer 1.3     
       + 1.4 *.msf files (Thermo) into mzIdentML 1.1

9- [ProteoWizard](http://proteowizard.sourceforge.net): pepXML converter available now - impl. of C++ library for reading/writing MzIdentML / interface for importing other formats (**IMPORT AND EXPORT**)

10- X!Tandem: [http://code.google.com/p/mzidentml-lib/](http://code.google.com/p/mzidentml-lib/) from the U.Liverpool group. (**EXPORT**)

11- [OpenMS](http://open-ms.sourceforge.net/): Fully supported in release 1.9 (**IMPORT AND EXPORT**)

12- [Scaffold](http://www.proteomesoftware.com/): Available now in Scaffold version 3.0 (export only); Scaffold 4.0 (IMPORT AND EXPORT)

13- TPP: pepXML to mzIdentML converter available from [ProteoWizard](http://proteowizard.sourceforge.net) (**IMPORT AND EXPORT** )

14- mzIdentML2CSV: [http://code.google.com/p/mzidentml-lib/](http://code.google.com/p/mzidentml-lib/) converter written by U.Liverpool group (IMPORTER)

15- [PAnalyzer](https://code.google.com/p/ehu-bio/wiki/PAnalyzer): The PAnalyzer tool developed by the University of the Basque Country also imports and exports mzIdentML (v1.0.0 and v1.1.0) (**IMPORT AND EXPORT**)

16- [Myrimatch](http://fenchurch.mc.vanderbilt.edu/software.php): Identifications exported in mzIdentML (**EXPORT**)

17- [IDPicker](http://fenchurch.mc.vanderbilt.edu/software.php): Version 3.x implements mzIdentML import (**IMPORT**)

18- [mzidLibrary](http://code.google.com/p/mzidentml-lib/): Library of routines for post-processing mzIdentML (setting thresholds, FDR, protein inference, CSV export) (****IMPORT AND EXPORT****)

19- [ProteoIDViewer](http://code.google.com/p/mzidentml-viewer/): Open source viewer (**IMPORT**)

20- [PRIDE Inspector Toolsuite](http://github.com/PRIDE-Toolsuite/pride-inspector): Open-Source Visualizer of PSI standard file formarts.

21- [mzID package](http://www.bioconductor.org/packages/release/bioc/html/mzID.html): R package available through Bioconductor (**IMPORT**)

22- [PeptideShaker](https://code.google.com/p/peptide-shaker/): Java standalone tool for the analysis and post-processing of MS proteomics experiments (**IMPORT AND EXPORT**)

23- [PIA](https://github.com/mpc-bioinformatics/pia): PIA is a toolbox for MS based protein inference and identification analysis. [PMID: <span>25938255</span>](http://www.ncbi.nlm.nih.gov/pubmed?term=25938255). ( **IMPORT AND EXPORT**)

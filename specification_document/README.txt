
mzML Community Review Kit (mzML 0.99.1 2007-11-05)

This kit contains the latest information, schema, examples for mzML. It is
intended for initial submission to the PSI document process on 2007-10-01.

This kit contains:

README.txt                   - This file, describing the kit
TODO.txt                     - Notes on still-to-be-addressed items

mzML0.99.1.xsd               - mzML schema
mzML0.99.1_idx.xsd           - mzML index wrapper schema
psi-ms.obo                   - Controlled vocabulary in OBO format

tiny1.mzML0.99.1.xml         - Tiny hand-crafted two spectrum MS1 + MS2 LCQ example
tiny2_SRM.mzML0.99.1.xml     - Tiny hand-crafted example of SRM data
tiny4_LTQ-FT.mzML0.99.1.xml  - Tiny hand-crafted example of describing hybrid instrument

1min.mzML                    - Example output of ReAdW Thermo -> mzML converter

validateMzML.pl              - Small Perl program to use Xerces to perform
                               crude validation of an mzML file.
mzMLContentHandler.pm        - Reader class used by validateMzML.pl

Additional files in the larger kit:
2min.mzML                    - Example output of wolf Waters -> mzML converter
mzML_0.99_validator/         - Java sematic validator

--------------------------------------------------------------

Example of the validator (requires Perl with Xerces installed)

./validateMzML.pl tiny1.mzML0.99.1.mzML
./validateMzML.pl tiny2_SRM.mzML0.99.1.mzML
./validateMzML.pl tiny4_LTQ-FT.mzML0.99.1.mzML
./validateMzML.pl 1min.mzML
./validateMzML.pl 2min.mzML
  (only included in large kit)

Note that this is a trivial validator. This validator on checks that the
file is valid XML according to the schema and checks that all the controlled
vocabulary accessions and terms conform to the OBO file.  Proper use of the
CV is not checked.  A far more complete semantic validator
is now available.  It enforces
proper use of the controlled vocabulary and other rules not enforceable
with XML schema itself.

--------------------------------------------------------------

Full validator: (available only in the large kit)

cd mzML_0.99_validator
java -jar mzML_validator-0.99.jar
  (requires at least java 1.5)
  (select files to validate with GUI)
cd ..

--------------------------------------------------------------

Current mzML kits:

http://tools.proteomecenter.org/software/mzMLKit/mzML0.99.1.zip
http://tools.proteomecenter.org/software/mzMLKit/mzML0.99.1_large.zip

Older full kits for converters:

http://tools.proteomecenter.org/software/mzMLKit/mzMLKit-0.99.0-Thermo.zip
http://tools.proteomecenter.org/software/mzMLKit/mzMLKit-0.99.0-Waters.zip

This includes current state of converters including some sample data
to convert. These converters work only on a MS Windows computer with the
appropriate vendor libraries preinstalled.  The converters are open
source, but they will not run without the vendor libraries, which
are available only for MS Windows.

--------------------------------------------------------------

Development web page with additional materials:
http://psidev.info/index.php?q=node/257

Please send feedback to:
psidev-ms-dev@lists.sourceforge.net






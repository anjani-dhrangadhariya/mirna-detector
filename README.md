# mirna-detector

The UIMA miRNA detector was a part of UIMA-HPC project at Fraunhofer SCAI. UIMA miRNA detector is a UIMA pipelet and its function is to identify microRNA mentions from free text biomedical literature using text mining techniques. It is now integrated with SCAIView: The knowledge discovery framework. [SCAIView]((http://academia.scaiview.com/academia/)) is an advanced semantic search engine that addresses questions of interest to general biomedical and life science researchers.

miRNA detector pipelet is developed using Apache UIMA framework within a Java application. The pipelet has two chief components – 1) Collection Reader, and 2) Analysis Engine – Annotator. Collection Reader: The Collection Reader loads the input “.xmi” file into a CAS (Common Annotation Structure) object. Analysis Enginer: 

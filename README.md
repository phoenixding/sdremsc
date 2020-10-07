```       _                             
         | |                            
  ___  __| |_ __ ___ _ __ ___  ___  ___ 
 / __|/ _` | '__/ _ \ '_ ` _ \/ __|/ __|
 \__ \ (_| | | |  __/ | | | | \__ \ (__ 
 |___/\__,_|_|  \___|_| |_| |_|___/\___|
                                                                           
```
                                                                        
# INTRODUCTION 
sdremsc is the single-cell extension of SDREM software(http://sb.cs.cmu.edu/sdrem/)
It can be  used to infer the signaling network that connects  the external stimuli (e.g., SARS-CoV-2 infection)
and regulatory network underlying various biological processes (e.g., disease progression). 
All the nodes (proteins) in the inferred networks will be ranked using an in silico knock-out strategy.  
The top-ranked proteins can be regarded as potential diagnostic or therapeutic targets for interventions (e.g., repressing the viral infection 
its progression).  

# PREREQUISITES

* python (python 3.6+)  
It was installed by default for most Linux distribution and MAC.  
If not, please check [https://www.python.org/downloads/](https://www.python.org/downloads/) for installation 
instructions. 
* Java   
Please refer [here](https://www.java.com/en/download/help/download_options.html) for instructions to install Java. 
*scdiff2
Please install [scdiff2](https://github.com/phoenixding/scdiff2) following the instructions in its Github page. 

# INSTALLATION
To download sdremsc, simply download and extract the package. 
Please use the sdremsc jar package (sdremsc.jar) inside the unzipped directory together with the depending libraries and data files (TF targets and miRNA targets).
If users want to use the jar package outside the directory, please add the sdremsc directory to $PATH. 
Please refer https://www.java.com/en/download/help/path.xml for instructions to set up system environment variable.

# USAGE
There are two-steps to run sdremsc 

(1) Precomputing and storing paths
***********************************************
This is a requied step before running sdremsc.

StorePaths.jar - The executable for preprocessing the network data.  It searches for paths from the sources to each TF and writes them all to disk, optionally filtering them to keep only the highest confidence paths.  The stored files are large and can require many GBs of disk space in total.  Without this preprocessing, SDREM has to search for the paths many times as it iterates, which is wasteful and makes it very, very, very slow for large networks.

allPaths_scDREM_Priors.props - An example properties file for StorePaths.jar.  store/filter means the user wants to enumerate paths and remove low-confidence paths (recommended for large PPI networks to speed network orientation).  The next lines define the sources, targets, and networks.  Node priors are used to give weights to vertices in the network.  If the user stores and filters paths, then he/she can delete the intermediate output in stored.paths.dir once the filtering step is done (give the final directory with the filtered paths to SDREM as input).


```
java -jar StorePaths.jar allPaths_SDREMSC_Priors.props
```

(2) 
sdremsc
***********************************************
The sdremsc algorithm executable.
sdremsc.jar - The sdremsc executable.
SDREMSC_Priors.props - A sample SDREM properties file.  model.dir is where the output will be written and where some of the input files must be located.  stored.paths.dir is the location of the filtered paths from StorePaths.jar if it was used.  It's important to use the same settings (sources, targets, node priors, path length, etc.) for the StorePaths and SDREM properties.  predict.knockdown is used to enable the knockdown effect prediction described in the SDREM Bioinformatics paper and can be commented out if this feature is not desired.  The rest of the parameters can usually be left at the default values and are described in the SDREM Genome Research paper supplement.

```
java -jar sdremsc.jar SDREMSC_Priors.props
```

# EXAMPLE  
example "allPaths_SDREMSC_Priors.props" config and "SDREMSC_Priors.props" config are available under the [example/](example/) directory
along with example input files. 
Please refer to [SDREM](http://sb.cs.cmu.edu/sdrem/software.html) for the detailed description for each parameters of the ".props" config file. 


# CREDITS
 
This software was developed by Jun Ding and Ziv Bar-Joseph (Systems Biology Group at CMU). 


# LICENSE 
 
This software is under MIT license.  
see the LICENSE.txt file for details. 


# CONTACT

jund  at cs.cmu.edu




                                 
                                 
                                 
                                 
                                 

                                                     

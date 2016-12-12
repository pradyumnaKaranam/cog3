package com.ibm.research.cogassist.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Each data-set in DigDeep belongs to exactly one domain, and the 
 * processes used for data input, analysis and indexing differ from domain
 * to domain.
 * 
 * This abstract class serves as a descriptor for concrete domains to
 * implement. The DigDeep platform uses information from a singleton 
 * instance of the domain descriptor to display Web pages in the admin module,
 * and to determine which analyses to run when the task is picked up by the
 * queue-processor.
 * 
 * Client implementations must provide a no-argument constructor which will
 * be called exactly once since instances are cached.
 * 
 * A sample implementation is provided in the DataExplorationDomain, which
 * does nothing but index CSV data into Solr.
 * 
 * @see DataExplorationDomain
 *
 */
public abstract class Domain {
	
	private static final Logger log = LoggerFactory.getLogger(Domain.class);
	
	/** Returns the name of this domain as used to identify it internally. */
	public abstract String getName();
	
	/** 
	 * Returns the user-friendly name of this domain that is to be 
	 * shown to users on the Web interface. 
	 */
	public abstract String getDisplayName();
	
	/**
	 * Returns the base-name of the resource(s) containing localized
	 * strings to be displayed by the front-end, or null if no such
	 * strings are required (default implementation).
	 */
	public String getLocalizationResourceName() {
		return null;
	}
	
	/**
	 * Returns the name of the class representing the JAX-RS resource
	 * which would serve as the base node for the Web admin module for 
	 * data-sets in this domain.
	 * 
	 * Constraint: The class /must/ extend 
	 * com.ibm.research.digdeep.web.resources.AccountsAdminResource.
	 */
	public abstract String getWebResourceClassName();
	
	/**
	 * Returns the name of the class representing the Callable
	 * which would serve as the task processor for data-sets in this
	 * domain.
	 * 
	 * Constraint: The class /must/ extend java.util.Callable<Void>
	 */
	public abstract String getTaskProcessorClassName();
	
	/** Cache of domain descriptor instances. */
	private static Map<String, Domain> domainDescriptors = new HashMap<String, Domain>();
	
	/** Load domain descriptors at app start-up. */
	static {
		try {			
			// Read all .domain files
			File[] domainDescriptorFiles = CogAssist.getSharedDir().listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File file, String filename) {
					return filename.endsWith(".domain");
				}
			});
			
			// Get all the class names in the .domain files
			List<String> domainDesctiptorClassNames = new ArrayList<String>(domainDescriptorFiles.length);
			for (File domainDescriptorFile : domainDescriptorFiles) {
				try (BufferedReader br = new BufferedReader(new FileReader(domainDescriptorFile))) {
					domainDesctiptorClassNames.add(br.readLine());
				}
			}
			
			// Find and load each domain descriptor class
			for (String domainDescriptorClassName : domainDesctiptorClassNames) {
				try {
					// Try loading the class mentioned in the properties file
					@SuppressWarnings("unchecked")
					Class<Domain> domainDescriptorClass = (Class<Domain>) Class.forName(domainDescriptorClassName);
					
					// Try creating an instance of that class
					Domain domainDescriptor = domainDescriptorClass.newInstance();
					
					// If this succeeds, then store the instance (singleton) in a cache and log info
					domainDescriptors.put(domainDescriptor.getName(), domainDescriptor);
					log.info("Loaded domain {} with descriptor class {}", domainDescriptor.getName(), domainDescriptorClassName);
					
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
					log.error("Could not load domain descriptor " + domainDescriptorClassName, e);
				}
			}
		} catch (IOException e) {
			log.error("Could not read domain descriptor mappings.", e);
			throw new RuntimeException(e);
		}
	}
	
	
	public static Domain valueOf(String domainName) {
		if (domainDescriptors.containsKey(domainName)) {
			return domainDescriptors.get(domainName);
		} else {
			throw new IllegalArgumentException(domainName + " is not a valid domain name.");
		}
	}
	
	public static Collection<Domain> getDomains() {
		return Collections.unmodifiableCollection(domainDescriptors.values());
	}
	
	public static Collection<String> getDomainNames() {
		return Collections.unmodifiableCollection(domainDescriptors.keySet());
	}
	
}

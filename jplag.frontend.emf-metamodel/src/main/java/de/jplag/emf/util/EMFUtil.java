package de.jplag.emf.util;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for EMF specific functionality.
 * @author Timur Saglam
 */
public final class EMFUtil {

    private static final Logger logger = LoggerFactory.getLogger(EMFUtil.class);

    private EMFUtil() {
        // private constructor for non-instantiability.
    }

    /**
     * Registers a file extension in the EMF registry for a XMI resource factory.
     * @param extension is the file extension string including a dot. Use "*" for any extension.
     */
    public static void registerModelExtension(String extension) {
        final Resource.Factory.Registry registry = Resource.Factory.Registry.INSTANCE;
        final Map<String, Object> extensionMap = registry.getExtensionToFactoryMap();
        extensionMap.put(extension, new XMIResourceFactoryImpl());
    }

    /**
     * Register the Ecore file extension in the EMF registry.
     */
    public static void registerEcoreExtension() {
        EcorePackage.eINSTANCE.eClass();
        registerModelExtension(EcorePackage.eNAME);
    }

    /**
     * Registers a collection of EPackages in the EPackage registry via their URIs.
     * @param ePackages are the EPackages to register.
     */
    public static void registerEPackageURIs(Collection<EPackage> ePackages) {
        ePackages.forEach(it -> EPackage.Registry.INSTANCE.put(it.getNsURI(), it));
    }

    /**
     * Loads a model or metamodel from a absolute file path.
     * @param filePath is the absolute path to the (meta)model.
     * @return the content of the loaded (meta)model resource or null if it could not be loaded.
     */
    public static List<EObject> loadModel(String filePath) {
        final ResourceSet resourceSet = new ResourceSetImpl();
        try {
            final Resource resource = resourceSet.getResource(URI.createFileURI(filePath), true);
            return resource.getContents();
        } catch (WrappedException exception) {
            String name = new File(filePath).getName();
            logger.error("Could not load " + name + ": " + exception.getCause().getMessage());
        }
        return null;
    }

}

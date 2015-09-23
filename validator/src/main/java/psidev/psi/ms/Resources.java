package psidev.psi.ms;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import org.apache.commons.io.FilenameUtils;

/**
 * Provides convenience methods to extract resources from the classpath.
 * 
 * @author Nils Hoffmann
 */
public class Resources {
	
	/**
	 * Extracts a resource using the classloader of {@code Resources} to locate the resource
	 * given by {@code resourcePath}. The extracted resource will be placed in the default 
	 * temporary directory as returned by {@code System.getProperty(java.io.tmpdir)}, 
	 * with the same filename as the original resource (minus path prefixes).
	 * 
	 * @param resourcePath the path of the resource
	 * @return the extracted file
	 * @throws IOException 
	 */
	public static File extractResource(String resourcePath) throws IOException {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"));
		return extractResource(Resources.class, resourcePath, new File(tmpDir,FilenameUtils.getName(resourcePath)));
	}

	/**
	 * Extracts a resource using the classloader of {@code Resources} to locate the resource
	 * given by {@code resourcePath}. The extracted resource will be placed in directory 
	 * {@code destDir}.
	 * 
	 * @param resourcePath the path of the resource
	 * @param destDir the destination directory
	 * @return the extracted file
	 * @see #extractResource(java.lang.Class, java.lang.String, java.io.File)
	 */
	public static File extractResource(String resourcePath, File destDir) {
		return extractResource(Resources.class, resourcePath, destDir);
	}
	
	/**
	 * Extracts a resource using the classloader of {@code clazz} to locate the resource
	 * given by {@code resourcePath}. The extracted resource will be placed in directory 
	 * {@code destDir}.
	 * 
	 * @param clazz the class whose classloader should be used 
	 * @param resourcePath the path of the resource
	 * @param destDir the destination directory
	 * @return the extracted file
	 */
	public static File extractResource(Class<?> clazz, String resourcePath, File destDir) {
		System.out.println("Extracting " + resourcePath + " to directory: " + destDir);
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
		URL resourceURL = clazz.getResource(resourcePath);
		if (resourceURL == null) {
			throw new NullPointerException(
					"Could not retrieve resource for path: " + resourcePath);
		}
		File outputFile = null;
		try {
			InputStream resourceInputStream = resourceURL.openStream();
			InputStream in = null;
			OutputStream out = null;
			try {
				String outname = new File(resourceURL.getPath()).getName();
				outname = outname.replaceAll("%20", " ");
				in = new BufferedInputStream(resourceInputStream);
				outputFile = new File(destDir, outname);
				out = new BufferedOutputStream(new FileOutputStream(outputFile));
				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return outputFile;
	}
}

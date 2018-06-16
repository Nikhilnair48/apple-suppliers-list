package com.apple.test;

import java.io.File;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDMetadata;

public class PDFTests {

	public static void main(String[] args) {
		File file = new File("Apple-Supplier-List.pdf");
		
		try {
			PDDocument doc = PDDocument.load(file);
			
			PDDocumentCatalog catalog = doc.getDocumentCatalog();
            PDMetadata meta = catalog.getMetadata();
            
            if ( meta != null) {
            	
            }
			
			//cosStream.
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

}

package com.apple;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class AppleSupplierListPDFReader {
	
	public static void main(String[] args) {
		File file = new File("Apple-Supplier-List.pdf");
		try {
			PDDocument suppliersList = PDDocument.load(file);
			Splitter splitter = new Splitter();
			List<PDDocument> pages = splitter.split(suppliersList);
			
			Iterator<PDDocument> iterator = pages.listIterator();
			while(iterator.hasNext()) {
				PDDocument doc = iterator.next();
				PDFTextStripper pdfStripper = new PDFTextStripper();
				pdfStripper.setAddMoreFormatting(true);
				parsePage(pdfStripper.getText(doc));

			}
			
			suppliersList.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		
	}
	
	public static void parsePage(String page) {
		
		String[] lines = page.split("[\\r\\n]+");
		boolean foundColumnHeader = false;
		for(int i = 0; i < lines.length;i++) {
			if(lines[i].contains("Supplier Name Address")) foundColumnHeader = true;
			
			if(foundColumnHeader) {
				// REMAINDER OF AN ADDRESS, APPEND
				/*if(lines[i] != null && lines[i].length() > 0 && lines[i].substring(0,1).equals(" ")) {}*/
				System.out.println(lines[i]);
			}
		}
	}

}

package com.apple.test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import com.apple.AppleSupplier;

public class BaseParserEx {
	
	
	public static final Logger logger = LogManager.getLogger(BaseParserEx.class);
	public static String currentSupplier;
	public static String currentAddress;
	public static boolean tableStarted = false;
	public static boolean tableEnded = false;
	// NOT ENOUGH INFORMATION TO CONFIRM COMPLETION - ESPECIALLY FOR THE SUPPLIER NAME
	public static boolean supplierDetailPossiblyComplete = false;
	public static List<AppleSupplier> appleSupplier;
	public static List<String> countries;
	
	
	public static void main(String[] args) throws IOException {
		File file = new File("Apple-Supplier-List.pdf");
		FileInputStream fis = new FileInputStream(file);
		appleSupplier = new ArrayList<AppleSupplier>();
		
		// COUNTRY LIST - LAZY APPROACH
		populateCountriesList();
		
		PDFParserTextStripper.extractText(fis);
		
	}
	
	public static void populateCountriesList() throws FileNotFoundException {
		File file = new File("ListOfCountries.txt");
		BufferedReader reader = new BufferedReader(new FileReader(file));
		
		
		
	}
}

class PDFParserTextStripper extends PDFTextStripper {
	public PDFParserTextStripper(PDDocument pdd) throws IOException {
		super();
		document = pdd;
	}

	public void stripPage(int pageNr) throws IOException {
		this.setStartPage(pageNr + 1);
		this.setEndPage(pageNr + 1);
		Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
		writeText(document, dummy); // This call starts the parsing process and calls writeString repeatedly.
	}

	/*System.out.println("String[" + text.getXDirAdj() + ","
	+ text.getYDirAdj() + " fs=" + text.getFontSizeInPt()
	+ " xscale=" + text.getXScale() + " height="
	+ text.getHeightDir() + " space=" + text.getWidthOfSpace()
	+ " width=" + text.getWidthDirAdj() + " ] "
	+ text.getUnicode());*/
	@Override
	protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
		
		// SET THE APPROPIRATE FLAGS PRIOR TO PARSING THE TABLE
		if (string.toLowerCase().contains("apple supplier responsibility")) {
			BaseParserEx.tableEnded = true;
			BaseParserEx.tableStarted = false;
		}
		
		if(BaseParserEx.tableStarted && !BaseParserEx.tableEnded) {		// ENSURE THE TABLE HAS BEEN FOUND
			for (TextPosition text : textPositions) {
				// COLUMN 1
				if((int)text.getXDirAdj() >= 54 && (int)text.getXDirAdj() < 247) {
					appendToColumnOneString(text.getUnicode());
				}
				// COLUMN 2
				if((int)text.getXDirAdj() >= 247) {
					appendToColumnTwoString(text.getUnicode());
				}
			}
			
			addSupplierToListIfReady(string, textPositions);
		}
		
		// TABLE FOUND - START PARSING FROM THE NEXT LINE
		if (string.toLowerCase().equals("address")) {	//"supplier name" -- part of the header
			BaseParserEx.tableEnded = false;
			BaseParserEx.tableStarted = true;
		}
	}
	// CONDITIONS:
	// IS THE SUPPLIER NAME COMPLETE?
	// IS THE ADDRESS COMPLETE? COMPLETE IF WE CAN FIND THE COUNTRY IN THE ADDRESS
	public static void addSupplierToListIfReady(String string, List<TextPosition> textPositions) {
		boolean updatedPreviousSupplier = false;
		
		// CONDITIONS TO UPDATE PREVIOUS SUPPLIER ADDRESS
		if(BaseParserEx.appleSupplier.size() > 0) {
			AppleSupplier supplier = BaseParserEx.appleSupplier.get(BaseParserEx.appleSupplier.size()-1);
			
			// THE CASE WHERE WE'VE DETAILS FOR THE SUPPLIER NAME, BUT ADDRESS LINE HAS NO VALUE
			if(BaseParserEx.currentAddress.isEmpty() && !BaseParserEx.currentSupplier.isEmpty()) {
				supplier.setSupplierName(supplier.getSupplierName() + BaseParserEx.currentSupplier);
				BaseParserEx.currentSupplier = "";
				updatedPreviousSupplier = true;
			}
			// 
			String country = supplier.getSupplierAddress().substring(supplier.getSupplierAddress().lastIndexOf(",")+1).trim();
			if(country != null && country.length() > 1 &&
					String.valueOf(country.charAt(country.length()-1)).equals(".")) 
				country = country.substring(0, country.length()-1);
			if(!BaseParserEx.currentSupplier.isEmpty() && (!BaseParserEx.countries.contains(country) || BaseParserEx.currentAddress.trim().isEmpty())) { 
				supplier.setSupplierName(supplier.getSupplierName() + BaseParserEx.currentSupplier);
				BaseParserEx.currentSupplier = "";
				updatedPreviousSupplier = true;
			}
			
			// PREVIOUSLY ADDED ADDRESS DOESN'T CONTAIN THE COUNTRY, THUS, INCOMPLETE
			String possibleCountry = supplier.getSupplierAddress().substring(supplier.getSupplierAddress().lastIndexOf(",")+1).trim();
			if(possibleCountry != null && possibleCountry.length() > 1 &&
					String.valueOf(possibleCountry.charAt(possibleCountry.length()-1)).equals(".")) 
				possibleCountry = possibleCountry.substring(0, possibleCountry.length()-1);
			if(!BaseParserEx.supplierDetailPossiblyComplete && !BaseParserEx.countries.contains(possibleCountry)) {
				supplier.setSupplierAddress(supplier.getSupplierAddress() + BaseParserEx.currentAddress);
				BaseParserEx.currentAddress = "";
				updatedPreviousSupplier = true;
			}
			
		}
		if(!updatedPreviousSupplier) // NO UPDATES TO PREVIOUS RECORD; ADD AS NEW SUPPLIER
		{
			AppleSupplier supplier = new AppleSupplier(BaseParserEx.currentSupplier, BaseParserEx.currentAddress);
			BaseParserEx.appleSupplier.add(supplier);
			BaseParserEx.currentAddress = "";
			BaseParserEx.currentSupplier = "";

			// UNABLE TO CONFIRM IF DETAIL IS COMPLETE GIVEN THE CURRENT INFORMATION
			String possibleCountry = supplier.getSupplierAddress().substring(supplier.getSupplierAddress().lastIndexOf(",")+1).trim();
			if(BaseParserEx.countries.contains(possibleCountry)) {
				BaseParserEx.supplierDetailPossiblyComplete = true;
			} else {
				BaseParserEx.supplierDetailPossiblyComplete = false;
			}
		}
			
	}
	
	public static void appendToColumnOneString(String str) {
		BaseParserEx.currentSupplier += str;
	}
	
	public static void appendToColumnTwoString(String str) {
		BaseParserEx.currentAddress += str;
	}

	public static void extractText(InputStream inputStream) {
		PDDocument pdd = null;
		
		try {
			pdd = PDDocument.load(inputStream);
			PDFParserTextStripper stripper = new PDFParserTextStripper(pdd);
			stripper.setSortByPosition(true);
			for (int i = 0; i < pdd.getNumberOfPages(); i++) {
				BaseParserEx.currentAddress = "";
				BaseParserEx.currentSupplier = "";
				BaseParserEx.tableEnded = false;
				BaseParserEx.tableStarted = false;
				stripper.stripPage(i);
				//BaseParserEx.logger.info("\n" + BaseParserEx.appleSupplier);
			}
		} catch (IOException e) {
		} finally {
			if (pdd != null) {
				try {
					pdd.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public static void run() throws IOException {
		File f = new File("Apple-Supplier-List.pdf");
		FileInputStream fis = null;
		
		try {
			fis = new FileInputStream(f);
			extractText(fis);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fis != null)
					fis.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}

package com.apple.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import com.apple.AppleSupplier;

public class BaseParserEx {
	
	public static String currentSupplier;
	public static String currentAddress;
	public static boolean tableStarted = false;
	public static boolean tableEnded = false;
	public static List<AppleSupplier> appleSupplier;
	
	public static void main(String[] args) throws IOException {
		File file = new File("Apple-Supplier-List.pdf");
		FileInputStream fis = new FileInputStream(file);
		PDFParserTextStripper.extractText(fis);
		appleSupplier = new ArrayList<AppleSupplier>();
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
				if((int)text.getXDirAdj() >= 54 && (int)text.getXDirAdj() < 248) {
					appendToColumnOneString(text.getUnicode());
				}
				// COLUMN 2
				if((int)text.getXDirAdj() >= 248) {
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
	
	public static void addSupplierToListIfReady(String string, List<TextPosition> textPositions) {
		
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

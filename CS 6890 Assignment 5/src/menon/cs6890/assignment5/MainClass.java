package menon.cs6890.assignment5;

public class MainClass {
	
	private static final String INPUT_FILE = "input.txt";
	private static final String OUTPUT_FILE_1 = "LEDoutput.txt";
	private static final String OUTPUT_FILE_2 = "DTWoutput.txt";
	private static final String SOURCE_STRING_1 = "sewmg";
	private static final String TARGET_STRING_1 = "serving";
	private static final String SOURCE_STRING_2 = "intention";
	private static final String TARGET_STRING_2 = "EXECUTION";
	private static final String SOURCE_STRING_3 = "sewmg";
	private static final String TARGET_STRING_3 = "serving";
	private static final String SOURCE_STRING_4 = "semngs";
	private static final String TARGET_STRING_4 = "size";
	private static final String SOURCE_STRING_5 = "siivinq";
	private static final String TARGET_STRING_5 = "zinc";
	private static final String TARGET_STRING_6 = "serving";
	private static final String TARGET_STRING_7 = "from";
	
	
	public static void main(String[] args) {

		System.out.println("Going to check spellings in contents of file " + INPUT_FILE + " using LED.");
		
		LEDSpellchecker ledSpellchecker = new LEDSpellchecker();
		ledSpellchecker.spellcheckFile(INPUT_FILE, OUTPUT_FILE_1);
		
		System.out.println("Corrected spelling put into " + OUTPUT_FILE_1);
		
		System.out.println("Finding edits to convert string " + SOURCE_STRING_1 + " into " + TARGET_STRING_1);		
		String edits = ledSpellchecker.getEdits(SOURCE_STRING_1, TARGET_STRING_1, LEDSpellchecker.STANDARD_INSERTION_COST, LEDSpellchecker.STANDARD_DELETION_COST, LEDSpellchecker.STANDARD_SUBSTITUTION_COST);
		System.out.println(edits);
		
		System.out.println("Finding edits to convert string " + SOURCE_STRING_2 + " into " + TARGET_STRING_2);		
		edits = ledSpellchecker.getEdits(SOURCE_STRING_2, TARGET_STRING_2, LEDSpellchecker.STANDARD_INSERTION_COST, LEDSpellchecker.STANDARD_DELETION_COST, LEDSpellchecker.STANDARD_SUBSTITUTION_COST);
		System.out.println(edits);
		
		System.out.println("Finding edits to convert string " + SOURCE_STRING_3 + " into " + TARGET_STRING_3 + " with substitution cost of 1");		
		edits = ledSpellchecker.getEdits(SOURCE_STRING_3, TARGET_STRING_3, LEDSpellchecker.STANDARD_INSERTION_COST, LEDSpellchecker.STANDARD_DELETION_COST, 1);
		System.out.println(edits);
		
		System.out.println("Finding edits to convert string " + SOURCE_STRING_4 + " into " + TARGET_STRING_4 + " with substitution cost of 1");		
		edits = ledSpellchecker.getEdits(SOURCE_STRING_4, TARGET_STRING_4, LEDSpellchecker.STANDARD_INSERTION_COST, LEDSpellchecker.STANDARD_DELETION_COST, 1);
		System.out.println(edits);
		

		System.out.println("Going to check spellings in contents of file " + INPUT_FILE + " using DTW.");
		
		DTWSpellchecker dtwSpellchecker = new DTWSpellchecker();
		dtwSpellchecker.spellcheckFile(INPUT_FILE, OUTPUT_FILE_2);
		
		System.out.println("Corrected spelling put into " + OUTPUT_FILE_2);
		
		System.out.println("Finding edits to convert string " + SOURCE_STRING_1 + " into " + TARGET_STRING_1);		
		edits = dtwSpellchecker.getEdits(SOURCE_STRING_1, TARGET_STRING_1);
		System.out.println(edits);
		
		System.out.println("Finding edits to convert string " + SOURCE_STRING_2 + " into " + TARGET_STRING_2);		
		edits = dtwSpellchecker.getEdits(SOURCE_STRING_2, TARGET_STRING_2);
		System.out.println(edits);
		
		System.out.println("Finding edits to convert string " + SOURCE_STRING_3 + " into " + TARGET_STRING_3);		
		edits = dtwSpellchecker.getEdits(SOURCE_STRING_3, TARGET_STRING_3);
		System.out.println(edits);
		
		System.out.println("Finding edits to convert string " + SOURCE_STRING_1 + " into " + TARGET_STRING_7);		
		edits = dtwSpellchecker.getEdits(SOURCE_STRING_1, TARGET_STRING_7);
		System.out.println(edits);
		
		System.out.println("Finding edits to convert string " + SOURCE_STRING_5 + " into " + TARGET_STRING_5);		
		edits = dtwSpellchecker.getEdits(SOURCE_STRING_5, TARGET_STRING_5);
		System.out.println(edits);
		
		System.out.println("Finding edits to convert string " + SOURCE_STRING_5 + " into " + TARGET_STRING_6);		
		edits = dtwSpellchecker.getEdits(SOURCE_STRING_5, TARGET_STRING_6);
		System.out.println(edits);
	}

}

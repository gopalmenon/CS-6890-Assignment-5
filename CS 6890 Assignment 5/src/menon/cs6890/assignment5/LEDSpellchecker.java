package menon.cs6890.assignment5;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Uses Levenshtein Edit Distance (LED) to detect and correct OCR spellings
 */

public class LEDSpellchecker {
	
	private Set<String> nutritionDictionary;
	
	
	public static final int STANDARD_INSERTION_COST = 1;
	public static final int STANDARD_DELETION_COST = 1;
	public static final int STANDARD_SUBSTITUTION_COST = 2;
	private static final String NUTRITION_DICTIONARY_FILE_NAME = "NutritionDictionary.txt";
	private static final String NO_MATCH_FOUND = "NULL";
	private static final String ACTION_CODE_DELETE = "del";
	private static final String ACTION_CODE_INSERT = "ins";
	private static final String ACTION_CODE_MATCH = "mat";
	private static final String ACTION_CODE_SUBSTITUTE = "sub";
	private static final String SINGLE_QUOTE = "\'";
	private static final String COMMA_SEPARATOR = ", ";
	private static final char DUMMY_CHARACTER = ' ';
	
	/**
	 * Constructor
	 */
	public LEDSpellchecker() {
		
		this.nutritionDictionary = new HashSet<String>();
		
		if (!ableToLoadDictionaryFromFileInputFile()) {
			System.exit(0);
		}
	}
		
	/**
	 * This method reads a file given by the input file path, which has one misspelling per line. It outputs the best
	 * correction for each misspelling or NULL if no correction is found. The output is sent to the file specified by the
	 * output file path. The output will have one correction per line, so that it can be easily compared with the input.
	 * 
	 * @param input_file_path
	 * @param output_file_path
	 */
	
	public void spellcheckFile(String input_file_path, String output_file_path) {
			
		List<String> wordsToSpellcheck = getWordsFromTextFile(input_file_path);
		List<String> correctedWords = new ArrayList<String>();
		
		Calendar start = Calendar.getInstance();
		
		//Loop through all the words to be spell checked
		for (String word : wordsToSpellcheck) {
			correctedWords.add(spellcheckWord(word));
		}
		
		Calendar end = Calendar.getInstance();		
		System.out.println("LED Spellcheck took " + (end.getTimeInMillis() - start.getTimeInMillis()) + " milliseconds to complete.");
		
		//Write the corrected words to the output file
		insertWordsIntoTextFile(correctedWords, output_file_path);
	}
	

	/**
	 * Returns a list of edits that transform the source into the target
	 * 
	 * @param source
	 * @param target
	 * @param ins_cost
	 * @param del_cost
	 * @param subs_cost
	 * @return
	 */
	public String getEdits(String source, String target, int ins_cost, int del_cost, int subs_cost) {
		
		//Convert to lower case
		source = source.toLowerCase();
		target = target.toLowerCase();
		
		//Convert the source string into the target string
		LevenshteinEditDistanceTable levenshteinEditDistanceTable = new LevenshteinEditDistanceTable(target, source, ins_cost, del_cost, subs_cost);
		levenshteinEditDistanceTable.convertSourceToTarget();
		
		//Start at the end of the table and store the edits into a stack
		Deque<String> edits = new ArrayDeque<String>();
		LevenshteinEditDistanceTableElement currentElement = levenshteinEditDistanceTable.getElement(levenshteinEditDistanceTable.getTargetSize() - 1, levenshteinEditDistanceTable.getSourceSize() - 1);
		LevenshteinEditDistanceTableElement backTraceElement = null;
		
		//Loop through the best path and put it into a stack
		String currentEdit = null;
		
		while(true) {
			
			//Get the previous element corresponding to the minimum alignment distance
			backTraceElement = currentElement.getBackTraceElement();
			
			//Stop if the end has been reached
			if (backTraceElement == null) {
				break;
			}
			
			//Store the operation performed to get from the back trace to the current element
			if (currentElement.getSourceStringOffset() == backTraceElement.getSourceStringOffset()) {
				//A character was inserted
				currentEdit = constructEditString(ACTION_CODE_INSERT, 
						                          levenshteinEditDistanceTable.getTarget().charAt(currentElement.getTargetStringOffset() - 1),
                                                  DUMMY_CHARACTER, 
						                          currentElement.getTargetStringOffset(),
						                          0,
						                          currentElement.getAlignmentCost() - backTraceElement.getAlignmentCost());
			} else {
				if (currentElement.getTargetStringOffset() == backTraceElement.getTargetStringOffset()) {
					//A character was deleted
					currentEdit = constructEditString(ACTION_CODE_DELETE, 
                               						  levenshteinEditDistanceTable.getSource().charAt(currentElement.getSourceStringOffset() - 1),
                                                      DUMMY_CHARACTER, 
                                                      currentElement.getSourceStringOffset(),
                                                      0,
                                                      currentElement.getAlignmentCost() - backTraceElement.getAlignmentCost());
				} else {
					if (currentElement.getAlignmentCost() == backTraceElement.getAlignmentCost()) {
						//If cost did not change then the characters were the same
						currentEdit = constructEditString(ACTION_CODE_MATCH, 
                                						  levenshteinEditDistanceTable.getSource().charAt(currentElement.getSourceStringOffset() - 1),
                                                          levenshteinEditDistanceTable.getTarget().charAt(currentElement.getTargetStringOffset() - 1),
                                                          currentElement.getSourceStringOffset(),
                                                          currentElement.getTargetStringOffset(),
                                                          currentElement.getAlignmentCost() - backTraceElement.getAlignmentCost());
					} else {
						//A character was substituted
						currentEdit = constructEditString(ACTION_CODE_SUBSTITUTE, 
      						                              levenshteinEditDistanceTable.getSource().charAt(currentElement.getSourceStringOffset() - 1),
      						                              levenshteinEditDistanceTable.getTarget().charAt(currentElement.getTargetStringOffset() - 1),
      						                              currentElement.getSourceStringOffset(),
      						                              currentElement.getTargetStringOffset(),
      						                              currentElement.getAlignmentCost() - backTraceElement.getAlignmentCost());
					}
				}
			}
			
			edits.push(currentEdit);
			
			//Repeat the loop with the back trace element
			currentElement = backTraceElement;
		}
		
		//Construct the edits string from the edits stack		
		StringBuffer editString = new StringBuffer();
		boolean firstTime = true;
		while (!edits.isEmpty()) {
			
			currentEdit = edits.pop();
			
			if (firstTime) {
				firstTime = false;
			} else {
				editString.append(COMMA_SEPARATOR);
			}
			
			editString.append(currentEdit);
		}
				
		return editString.toString();
		
	}
	
	/**
	 * @param actionCode
	 * @param sourceCharacter
	 * @param targetCharacter
	 * @param sourcePosition
	 * @param targetPostion
	 * @param cost
	 * @return the edit string 
	 */
	private String constructEditString(String actionCode, char sourceCharacter, char targetCharacter, int sourcePosition, int targetPostion, int cost) {
		
		StringBuffer editString = new StringBuffer();
		
		editString.append(actionCode).append("(");
		
		if (ACTION_CODE_DELETE.equals(actionCode) || ACTION_CODE_INSERT.equals(actionCode)) {
			editString.append(SINGLE_QUOTE).append(sourceCharacter).append(SINGLE_QUOTE);
			editString.append(COMMA_SEPARATOR);
			editString.append(sourcePosition);
			editString.append(COMMA_SEPARATOR);
			editString.append(cost);
		} else { //this means the action was match or substitute
			editString.append(SINGLE_QUOTE).append(sourceCharacter).append(SINGLE_QUOTE);
			editString.append(COMMA_SEPARATOR);
			editString.append(SINGLE_QUOTE).append(targetCharacter).append(SINGLE_QUOTE);
			editString.append(COMMA_SEPARATOR);
			editString.append(sourcePosition);
			editString.append(COMMA_SEPARATOR);
			editString.append(targetPostion);
			editString.append(COMMA_SEPARATOR);
			editString.append(cost);
		}
		
		editString.append(")");
		
		return editString.toString();

	}
	
	/**
	 * @param word to be spell checked
	 * @return the best matching nutrition term
	 */
	private String spellcheckWord(String word) {
		
		LevenshteinEditDistanceTable levenshteinEditDistanceTable = null;
		List<LevenshteinEditDistanceTable> wordMatchingAttemptResults = null;
		
		wordMatchingAttemptResults = new ArrayList<LevenshteinEditDistanceTable>();
		
		//Find the LED between the word to spell check and each nutrition term
		for (String nutritionTerm : this.nutritionDictionary) {
			levenshteinEditDistanceTable = new LevenshteinEditDistanceTable(nutritionTerm, word, STANDARD_INSERTION_COST, STANDARD_DELETION_COST, STANDARD_SUBSTITUTION_COST);
			levenshteinEditDistanceTable.convertSourceToTarget();
			wordMatchingAttemptResults.add(levenshteinEditDistanceTable);
		}
		
		//Return the closest nutrition term to the list of corrected words
		 return getBestMatch(wordMatchingAttemptResults);
	}
	
	/**
	 * @param wordMatchingAttemptResults
	 * @return the String with the best match
	 */
	private String getBestMatch(List<LevenshteinEditDistanceTable> wordMatchingAttemptResults) {
		
		int lowestMatchingCost = Integer.MAX_VALUE, lowestMatchingCostCandidate = 0, maximumAllowedCost = 0;
		LevenshteinEditDistanceTableElement lowestMatchingCostCandidateElement = null;
		String bestMatch = null;
		boolean matchFound = false;
		
		//Loop through the candidates to find the best match
		for (LevenshteinEditDistanceTable levenshteinEditDistanceTable : wordMatchingAttemptResults) {
			
			assert levenshteinEditDistanceTable != null;
			
			//The maximum allowed cost is the cost of replacing the source string with the target by removing all source characters
			maximumAllowedCost = levenshteinEditDistanceTable.getFullStringSubstitutionCost();
			
			//The lowest matching candidate is at the end of the table
			lowestMatchingCostCandidateElement = levenshteinEditDistanceTable.getElement(levenshteinEditDistanceTable.getTargetSize() - 1, levenshteinEditDistanceTable.getSourceSize() - 1);
			lowestMatchingCostCandidate = lowestMatchingCostCandidateElement.getAlignmentCost();
			//Get the lowest match that is below the maximum allowed
			if (lowestMatchingCostCandidate < lowestMatchingCost && lowestMatchingCostCandidate < maximumAllowedCost) {
				lowestMatchingCost = lowestMatchingCostCandidate;
				bestMatch = levenshteinEditDistanceTable.getTarget();
				matchFound = true;
			}
		}
		
		return matchFound == true ? bestMatch : NO_MATCH_FOUND;
		
	}
	/**
	 * @param inputFilePath
	 * @return the list of words in the text file having one word per line
	 */
	private List<String> getWordsFromTextFile(String inputFilePath) {
		
		String words = null;
		List<String> returnValue = new ArrayList<String>();
		try {
			BufferedReader textFileReader = new BufferedReader(new FileReader(inputFilePath));
			words = textFileReader.readLine();
			while(words != null) {
				if (words.trim().length() > 0) {
					returnValue.add(words.trim().toLowerCase());
				}
				words = textFileReader.readLine();
			}
			textFileReader.close();
		} catch (FileNotFoundException e) {
			System.err.println("File " + inputFilePath + " was not found.");
			e.printStackTrace();
			return null;
		}
		catch (IOException e) {
			System.err.println("IOException thrown while reading file " + inputFilePath + ".");
			e.printStackTrace();
			return null;
		}
		
		return returnValue;
	}

	/**
	 * This method loads all nutrition terms from the dictionary file and puts them into the internal dictionary set
	 * 
	 * @return true if able to load dictionary
	 */
	private boolean ableToLoadDictionaryFromFileInputFile() {
		
		List<String> nutritionTerms = getWordsFromTextFile(NUTRITION_DICTIONARY_FILE_NAME);
		this.nutritionDictionary.addAll(nutritionTerms);
		
		if (this.nutritionDictionary.size() > 0) {
			return true;
		} else {
			return false;
		}

	}
	
	/**
	 * @param words
	 * @param outputFilePath
	 * @return true if able to insert the words into the text file
	 */
	private boolean insertWordsIntoTextFile(List<String> words, String outputFilePath) {
		
	    Path path = Paths.get(outputFilePath);
        try {
    	    BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
    	    for (String word : words) {
				writer.write(word);
		        writer.newLine();
    	    }
    	    writer.close();
		} catch (IOException e) {
			System.err.println("IOException thrown while trying to write to file " + outputFilePath);
			e.printStackTrace();
			return false;
		}

        return true;
	}

}
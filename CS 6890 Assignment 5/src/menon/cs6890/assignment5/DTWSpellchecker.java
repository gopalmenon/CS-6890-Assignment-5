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
 * Uses Dynamic Time Warping (DTW) to detect and correct OCR spellings
 */

public class DTWSpellchecker {
	
	private Set<String> nutritionDictionary;
	
	

	private static final String NUTRITION_DICTIONARY_FILE_NAME = "NutritionDictionary.txt";
	private static final String NO_MATCH_FOUND = "NULL";
	private static final String SINGLE_QUOTE = "\'";
	private static final String COMMA_SEPARATOR = ", ";
	private static final String ACTION_CODE_WARP = "wrp";
	private static final String ACTION_CODE_MATCH = "mat";
	
	/**
	 * Constructor
	 */
	public DTWSpellchecker() {
		
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
		System.out.println("DTW Spellcheck took " + (end.getTimeInMillis() - start.getTimeInMillis()) + " milliseconds to complete.");
		
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
	public String getEdits(String source, String target) {
		
		//Convert to lower case
		source = source.toLowerCase();
		target = target.toLowerCase();
		
		//Convert the source string into the target string
		DynamicTimeWarpEditDistanceTable dynamicTimeWarpEditDistanceTable = new DynamicTimeWarpEditDistanceTable(target, source);
		dynamicTimeWarpEditDistanceTable.convertSourceToTarget();
		
		//Start at the end of the table and store the edits into a stack
		Deque<String> edits = new ArrayDeque<String>();
		TableElement currentElement = dynamicTimeWarpEditDistanceTable.getElement(dynamicTimeWarpEditDistanceTable.getTargetSize() - 1, dynamicTimeWarpEditDistanceTable.getSourceSize() - 1);
		TableElement backTraceElement = null;
		
		//Loop through the best path and put it into a stack
		String currentEdit = null;
		
		while(true) {
			
			//Get the previous element corresponding to the minimum alignment distance
			backTraceElement = currentElement.getBackTraceElement();
			
			//Stop if the end has been reached
			if (backTraceElement == null) {
				if (currentElement.getAlignmentCost() == 0) {
					//A character was matched
					currentEdit = constructEditString(ACTION_CODE_MATCH, 
							  dynamicTimeWarpEditDistanceTable.getSource().charAt(currentElement.getSourceStringOffset()),
							  dynamicTimeWarpEditDistanceTable.getTarget().charAt(currentElement.getTargetStringOffset()),
							  currentElement.getSourceStringOffset() + 1, 
						      currentElement.getTargetStringOffset() + 1,
						      0);
				} else {
					//A character was warped
					currentEdit = constructEditString(ACTION_CODE_WARP, 
							      dynamicTimeWarpEditDistanceTable.getSource().charAt(currentElement.getSourceStringOffset()),
							      dynamicTimeWarpEditDistanceTable.getTarget().charAt(currentElement.getTargetStringOffset()),
							      currentElement.getSourceStringOffset() + 1, 
							      currentElement.getTargetStringOffset() + 1,
							      1);
				}
				edits.push(currentEdit);
				break;
			}
			
			//Store the operation performed to get from the back trace to the current element
			if (currentElement.getAlignmentCost() == backTraceElement.getAlignmentCost()) {
				//A character was matched
				currentEdit = constructEditString(ACTION_CODE_MATCH, 
							  dynamicTimeWarpEditDistanceTable.getSource().charAt(currentElement.getSourceStringOffset()),
							  dynamicTimeWarpEditDistanceTable.getTarget().charAt(currentElement.getTargetStringOffset()),
							  currentElement.getSourceStringOffset() + 1, 
						      currentElement.getTargetStringOffset() + 1,
						      0);
			} else {
				//A character was warped
				currentEdit = constructEditString(ACTION_CODE_WARP, 
						      dynamicTimeWarpEditDistanceTable.getSource().charAt(currentElement.getSourceStringOffset()),
						      dynamicTimeWarpEditDistanceTable.getTarget().charAt(currentElement.getTargetStringOffset()),
						      currentElement.getSourceStringOffset() + 1, 
						      currentElement.getTargetStringOffset() + 1,
						      1);
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
		editString.append(SINGLE_QUOTE).append(sourceCharacter).append(SINGLE_QUOTE);
		editString.append(COMMA_SEPARATOR);
		editString.append(SINGLE_QUOTE).append(targetCharacter).append(SINGLE_QUOTE);
		editString.append(COMMA_SEPARATOR);
		editString.append(sourcePosition);
		editString.append(COMMA_SEPARATOR);
		editString.append(targetPostion);
		editString.append(COMMA_SEPARATOR);
		editString.append(cost);
		editString.append(")");
		
		return editString.toString();

	}
	
	/**
	 * @param word to be spell checked
	 * @return the best matching nutrition term
	 */
	private String spellcheckWord(String word) {
		
		DynamicTimeWarpEditDistanceTable dynamicTimeWarpEditDistanceTable = null;
		List<DynamicTimeWarpEditDistanceTable> wordMatchingAttemptResults = new ArrayList<DynamicTimeWarpEditDistanceTable>();
		
		//Find the LED between the word to spell check and each nutrition term
		for (String nutritionTerm : this.nutritionDictionary) {
			dynamicTimeWarpEditDistanceTable = new DynamicTimeWarpEditDistanceTable(nutritionTerm, word);
			dynamicTimeWarpEditDistanceTable.convertSourceToTarget();
			wordMatchingAttemptResults.add(dynamicTimeWarpEditDistanceTable);
		}
		
		//Return the closest nutrition term to the list of corrected words
		 return getBestMatch(wordMatchingAttemptResults);
	}
	
	/**
	 * @param wordMatchingAttemptResults
	 * @return the String with the best match
	 */
	private String getBestMatch(List<DynamicTimeWarpEditDistanceTable> wordMatchingAttemptResults) {
		
		int lowestMatchingCost = Integer.MAX_VALUE, lowestStringLengthDifference = Integer.MAX_VALUE, lowestMatchingCostCandidate = 0, maximumAllowedCost = 0, lowestStringLengthDifferenceCandidate = 0;
		TableElement lowestMatchingCostCandidateElement = null;
		String bestMatch = null;
		boolean matchFound = false;
		
		//Loop through the candidates to find the best match
		for (DynamicTimeWarpEditDistanceTable dynamicTimeWarpEditDistanceTable : wordMatchingAttemptResults) {
			
			assert dynamicTimeWarpEditDistanceTable != null;
			
			//The maximum allowed cost is the cost of replacing the source string with the target by removing all source characters
			maximumAllowedCost = dynamicTimeWarpEditDistanceTable.getFullStringSubstitutionCost();
			
			//The lowest matching candidate is at the end of the table
			lowestMatchingCostCandidateElement = dynamicTimeWarpEditDistanceTable.getElement(dynamicTimeWarpEditDistanceTable.getTargetSize() - 1, dynamicTimeWarpEditDistanceTable.getSourceSize() - 1);
			lowestMatchingCostCandidate = lowestMatchingCostCandidateElement.getAlignmentCost();
			lowestStringLengthDifferenceCandidate = Math.abs(dynamicTimeWarpEditDistanceTable.getSourceSize() - dynamicTimeWarpEditDistanceTable.getTargetSize());
			//Get the lowest match that is below the maximum allowed
			if ((lowestMatchingCostCandidate < lowestMatchingCost || (lowestMatchingCostCandidate == lowestMatchingCost && lowestStringLengthDifferenceCandidate < lowestStringLengthDifference)) && lowestMatchingCostCandidate < maximumAllowedCost) {
				lowestMatchingCost = lowestMatchingCostCandidate;
				lowestStringLengthDifference = lowestStringLengthDifferenceCandidate;
				bestMatch = dynamicTimeWarpEditDistanceTable.getTarget();
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
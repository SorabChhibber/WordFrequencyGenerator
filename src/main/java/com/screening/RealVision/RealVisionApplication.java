package com.screening.RealVision;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
public class RealVisionApplication implements ApplicationRunner {

	private static final Logger logger = LoggerFactory.getLogger(RealVisionApplication.class);
	private static final String contentFileArgName = "content.file.name";
	private static final String commonWordsFileArgName = "common.words.file.name";
	private static final String maxWordsArgName = "max.number.words";
	private static final String inputFilesPath = "./src/main/java/com/screening/RealVision/";


	public static void main(String[] args) {
		SpringApplication.run(RealVisionApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {

		// Get common words that need to be excluded
		Set<String> commonWords =  getCommonWord(args);
		// Get map of words and occurrence for content file with common words excluded
		Optional<Map> optional = getContentFileMap(args, commonWords);
		// Output the top n words
		if(optional.isPresent()) {
			printOutput(optional.get(), args);
		}else {
			logger.error("Process Terminated with error");
		}
	}

	private Set<String> getCommonWord(ApplicationArguments args) throws FileNotFoundException {
		Set<String> commonWordSet = new TreeSet<>();

		if(args.containsOption(commonWordsFileArgName) ) {

			String commonWordsFileName = args.getOptionValues(commonWordsFileArgName).get(0);
			File file = new File( inputFilesPath + commonWordsFileName);
			if(file.isFile()) {
				Scanner input = new Scanner(file);
				while(input.hasNextLine()) {
					commonWordSet.add(input.nextLine().toLowerCase(Locale.ROOT).trim());
				}
			}else {
				logger.error("File {} does not exist", file.getAbsolutePath());
			}
		}else {
			logger.error("Command line argument {} missing", commonWordsFileArgName);
		}
		return  commonWordSet;
	}

	private void printOutput(Map map, ApplicationArguments args) {

		int maxNumOfWords = 9999;
		if(args.containsOption(maxWordsArgName)) {
			maxNumOfWords = Integer.parseInt(args.getOptionValues(maxWordsArgName).get(0));
		}else {
			logger.error("Command line argument {} missing. Using default : {}", maxWordsArgName, maxNumOfWords);
		}

		Stream<Map.Entry<String, Integer>> sorted = map.entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.limit(maxNumOfWords);

		System.out.println("Count     Word");
		for(Map.Entry<String, Integer> entry: sorted.collect(Collectors.toList())) {
			System.out.println(String.format("%-10d%s", entry.getValue(), entry.getKey()));
		}
	}


	private Optional<Map> getContentFileMap(ApplicationArguments args, Set<String> commonWords) throws FileNotFoundException {

		Map<String,Integer> map = new HashMap<>();

		if(args.containsOption(contentFileArgName)) {

			String contentFileName = args.getOptionValues(contentFileArgName).get(0);
			File file = new File( inputFilesPath + contentFileName);
			if(file.isFile()) {
				Scanner input = new Scanner(file);
				while(input.hasNext()) {
					String word = input.next().toLowerCase(Locale.ROOT).replaceAll("[-+.^:,`*?]","").trim();
					if(!commonWords.contains(word) && (word.length() != 0)) {
						if(map.containsKey(word)) {
							map.put(word, map.get(word) + 1);
						}else {
							map.put(word, 1);
						}
					}
				}
			}else {
				logger.error("File {} does not exist", file.getAbsolutePath());
				return Optional.empty();
			}
		}else {
			logger.error("Command line argument {} missing", contentFileArgName);
			return Optional.empty();
		}
		return  Optional.of(map);
	}
}

package com.testdatagen;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.github.javafaker.Faker;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@SpringBootApplication
@RestController

public class TestdataGenApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestdataGenApplication.class, args);
	}

	ColorLogger colorLogger = new ColorLogger();

	@GetMapping(value = "healthcheck")
	public ResponseEntity<String> healthcheck() {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CACHE_CONTROL, "no-cache");
		headers.add(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8");
		headers.add(HttpHeaders.CONTENT_ENCODING, "UTF-8");
		return ResponseEntity.ok()
				.headers(headers)
				.body("{\"status\":\"ok\",\"service\":\"API Health Check\"}");
	}

	@GetMapping(value = "generateTestdataCsv")
	public ResponseEntity<String> RandomPerson(
			@RequestParam(required = true) String count,
			@RequestParam(required = false, defaultValue = "18") String minAge,
			@RequestParam(required = false, defaultValue = "65") String maxAge
	) {

		int parsedCount;
		int parsedMinAge;
		int parsedMaxAge;

		// Validate count (should be an integer)
		try {
			parsedCount = Integer.parseInt(count);
		} catch (NumberFormatException e) {
			return ResponseEntity.badRequest().body("Invalid count value, it must be an integer.");
		}

		// Validate minAge (should be an integer and within 1 to 150)
		try {
			parsedMinAge = Integer.parseInt(minAge);
			if (parsedMinAge < 1 || parsedMinAge > 150) {
				return ResponseEntity.badRequest().body("minAge must be between 1 and 150.");
			}
		} catch (NumberFormatException e) {
			return ResponseEntity.badRequest().body("Invalid minAge value, it must be an integer.");
		}
		// Validate maxAge (should be an integer and within 1 to 160)
		try {
			parsedMaxAge = Integer.parseInt(maxAge);
			if (parsedMaxAge < 1 || parsedMaxAge > 160) {
				return ResponseEntity.badRequest().body("maxAge must be between 1 and 160.");
			}
		} catch (NumberFormatException e) {
			return ResponseEntity.badRequest().body("Invalid maxAge value, it must be an integer.");
		}

		// Ensure that maxAge is greater than or equal to minAge
		if (parsedMaxAge < parsedMinAge) {
			return ResponseEntity.badRequest().body("maxAge cannot be less than minAge.");
		}
		// Set up the response headers
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CACHE_CONTROL, "no-cache");
		headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=RandomPerson.csv");
		headers.add(HttpHeaders.CONTENT_ENCODING, "UTF-8");
		headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "*");
		// Initialize variables for generation
		String gen = "";
		String lowerCharacters = "abcdefghijklmnopqrstuvwxyz";
		String numberCharacters = "0123456789";
		gen = "\uFEFF" + "shortid1;shortid2;longid1;longid2;birthday;firstname;lastname;address;postaladress;zip;phone;mobilphone;jobposition;jobtitel;email;uuid\n";
		Faker faker = new Faker(new Locale("sv"));

		// Generate and log personal numbers with all variants
		List<List<String>> personalNumbers = generateUniqueSwedishPersonalNumbers(parsedMinAge, parsedMaxAge, parsedCount);
		// Log each personal number with all its variants on the same row
		for (List<String> personalNumberVariants : personalNumbers) {
			// Print all variants of the current personal number on one row
			String id = "";
			for (String variant : personalNumberVariants) {
				//System.out.print(variant + ";");  // Print variants in one row
				id=id + variant + ";";
			}
			// Generate a random email
			String mail = RandomStringUtils.random(6, lowerCharacters) +
					RandomStringUtils.random(6, numberCharacters) + "@" +
					RandomStringUtils.random(6, lowerCharacters) +
					RandomStringUtils.random(6, numberCharacters) + ".com";

			// Append the generated data to the result
			gen += id +
					faker.name().firstName() + ";" +
					faker.name().lastName() + ";" +
					faker.address().streetAddress() + ";" +
					faker.address().state() + ";" +
					faker.address().zipCode() + ";" +
					faker.phoneNumber().phoneNumber().replace("-", "") + ";" +
					faker.phoneNumber().cellPhone().replace("-", "") + ";" +
					faker.job().position() + ";" +
					faker.job().title() + ";" +
					mail + ";" +
					UUID.randomUUID().toString() + "\n";
		}

		return ResponseEntity.ok()
				.headers(headers)
				.body(gen);
	}

	// Generate multiple unique personal numbers, each with all four variants
	public static List<List<String>> generateUniqueSwedishPersonalNumbers(int minAge, int maxAge, int count) {
		Set<String> personalNumbersSet = new HashSet<>();
		List<List<String>> personalNumbersList = new ArrayList<>();
		Random random = new Random();

		// Generate personal numbers until we have the desired count
		while (personalNumbersList.size() < count) {
			List<String> personalNumberVariants = generateSwedishPersonalNumber(minAge, maxAge, random);
			String personalNumberWithVariants = personalNumberVariants.get(0); // Use the first variant to check for uniqueness
			if (personalNumbersSet.add(personalNumberWithVariants)) {
				personalNumbersList.add(personalNumberVariants);
			}
		}

		return personalNumbersList;
	}

	// Generate all four variants of a Swedish personal number (short and long, with and without dashes)
	public static List<String> generateSwedishPersonalNumber(int minAge, int maxAge, Random random) {
		LocalDate today = LocalDate.now();

		// Randomly select an age within the range
		int age = minAge + random.nextInt(maxAge - minAge + 1);

		// Calculate birth year
		int birthYear = today.getYear() - age;

		// Randomly select a month and day
		int month = random.nextInt(1, 13);
		int day = getRandomDayForMonth(birthYear, month, random);

		// Format birth date (short and long formats)
		LocalDate birthDate = LocalDate.of(birthYear, month, day);
		String birthDateShort = birthDate.format(DateTimeFormatter.ofPattern("yyMMdd"));
		String birthDateLong = birthDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

		// Randomly generate a 3-digit individual number
		int individualNumber = random.nextInt(0, 1000);
		String individualNumberStr = String.format("%03d", individualNumber);

		// Calculate the checksum
		String partialNumber = birthDateShort.substring(0, 6) + individualNumberStr;
		int checksum = calculateModulus10Checksum(partialNumber);

		// Generate all four variants
		List<String> variants = new ArrayList<>();

		// Short format (with and without dash)
		variants.add(birthDateShort + "-" + individualNumberStr + checksum); // Short format with dash
		variants.add(birthDateShort + individualNumberStr + checksum); // Short format without dash

		// Long format (with and without dash)
		variants.add(birthDateLong + "-" + individualNumberStr + checksum); // Long format with dash
		variants.add(birthDateLong + individualNumberStr + checksum); // Long format without dash
		variants.add(birthDate.toString()); // Long format without dash

		return variants;
	}

	// Randomly select a day for the given month and year
	public static int getRandomDayForMonth(int year, int month, Random random) {
		LocalDate date = LocalDate.of(year, month, 1);
		return random.nextInt(1, date.lengthOfMonth() + 1);
	}

	// Calculate the checksum using the modulus-10 method
	public static int calculateModulus10Checksum(String number) {
		int sum = 0;
		int[] weights = {2, 1}; // Alternate between weights 2 and 1

		for (int i = 0; i < number.length(); i++) {
			int digit = Character.getNumericValue(number.charAt(i));
			int product = digit * weights[i % 2];
			sum += product / 10 + product % 10;
		}

		int checksum = (10 - (sum % 10)) % 10; // If remainder is 0, set checksum to 0
		return checksum;
	}

	public class ColorLogger {
		private static final Logger LOGGER = LoggerFactory.getLogger("");
		public void logDebug(String logging) {
			LOGGER.debug("\u001B[92m" + logging + "\u001B[0m");
		}
		public void logInfo(String logging) {
			LOGGER.info("\u001B[93m" + logging + "\u001B[0m");
		}
		public void logError(String logging) {
			LOGGER.error("\u001B[91m" + logging + "\u001B[0m");
		}
	}

}



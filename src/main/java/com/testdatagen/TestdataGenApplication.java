package com.testdatagen;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import co.elastic.apm.attach.ElasticApmAttacher;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.github.javafaker.Faker;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.*;
import java.sql.*;
import java.time.Year;
import java.util.*;

import static java.lang.Integer.parseInt;

@OpenAPIDefinition(
		info = @Info(
				title = "Test Data Generator",
				version = "v1.0",
				description = "Test Data Generator API for creating realistic Swedish test data for performance testing.\n" +
						"Endpoints:\n\n" +
						"**Health**\n" +
						"1. **GET /healthcheck** - API health check.\n\n" +
						"**Test Data Generation**\n" +
						"2. **GET /RandomPerson** - Generate Swedish persons with personnummer (CSV format).\n" +
						"   - Parameters: `antal` (1-25000) - Number of persons to generate\n" +
						"   - Generates: Personnummer, name, address, phone, email, job info\n" +
						"   - Age range: 17-70 years old\n" +
						"   - Correctly handles 100+ year old persons with '+' delimiter\n\n" +
						"3. **GET /RandomPersonJson** - Generate single Swedish person (JSON format).\n" +
						"   - Returns one person with complete details in JSON\n" +
						"   - Includes both short and long personnummer formats\n\n" +
						"4. **GET /RandomUUID** - Generate random UUIDs.\n" +
						"   - Parameters: `antal` (1-50000) - Number of UUIDs\n" +
						"   - Returns: Line-separated UUIDs in CSV format\n\n" +
						"**Database Operations**\n" +
						"5. **POST /SQL** - Execute SQL query and return results.\n" +
						"   - Accepts: JDBC URL, SQL query, credentials\n" +
						"   - Returns: CSV formatted results\n" +
						"   - ⚠️ Use with caution - executes raw SQL queries\n\n" +
						"**Swedish Personnummer Format:**\n" +
						"- Short format: YYMMDD-XXXX (under 100 years) or YYMMDD+XXXX (100+ years)\n" +
						"- Long format: YYYYMMDDXXXX (12 digits, no delimiter)\n" +
						"- The '+' delimiter indicates person is 100 years or older\n" +
						"- The '-' delimiter indicates person is under 100 years old\n"
		),
		externalDocs = @ExternalDocumentation(
				description = "GitHub Repository",
				url = "https://github.com/ostbergjohan/testdatagen"
		)
)
@SpringBootApplication
@RestController
@Tag(name = "Test Data Generator", description = "Generate realistic Swedish test data for performance testing")
public class TestdataGenApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestdataGenApplication.class, args);
	}

	ColorLogger colorLogger = new ColorLogger();

	@Configuration
	public class WebMvc implements WebMvcConfigurer {
		@Override
		public void addCorsMappings(CorsRegistry registry) {
			registry.addMapping("/**")
					.allowedMethods("*")
					.allowedOrigins("*");
		}
	}

	@Operation(
			summary = "Health Check",
			description = "Verify that the service is running and responsive"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Service is healthy",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "{\"status\":\"ok\",\"service\":\"API Health Check\"}")))
	})
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

	@Operation(
			summary = "Generate Random UUIDs",
			description = "Generate a specified number of random UUIDs for use in test data"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "UUIDs generated successfully",
					content = @Content(mediaType = "text/csv")),
			@ApiResponse(responseCode = "400", description = "Invalid parameter")
	})
	@GetMapping(value = "RandomUUID")
	public ResponseEntity<String> RandomUUID(
			@Parameter(description = "Number of UUIDs to generate (1-50000)", required = true)
			@RequestParam String antal) {

		if (!antal.matches("[0-9]+")) {
			return ResponseEntity.status(400)
					.body("parameter must be an integer");
		}

		int count = parseInt(antal);
		if (count > 50000) {
			return ResponseEntity.status(400)
					.body("parameter limit 50000");
		}

		if (count < 1) {
			return ResponseEntity.status(400)
					.body("parameter must be at least 1");
		}

		long start = System.currentTimeMillis();
		colorLogger.logInfo("Creating RandomUUID: " + antal);

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CACHE_CONTROL, "no-cache");
		headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");
		headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");

		StringBuilder gen = new StringBuilder();
		for (int i = 0; i < count; i++) {
			gen.insert(0, UUID.randomUUID().toString() + "\n");
		}

		long finish = System.currentTimeMillis();
		long timeElapsed = finish - start;
		colorLogger.logInfo("RandomUUID exec time: " + timeElapsed + "ms");

		return ResponseEntity.ok()
				.headers(headers)
				.body(gen.toString());
	}

	@Operation(
			summary = "Generate Random Swedish Person (JSON)",
			description = "Generate a single random Swedish person with complete details in JSON format. " +
					"Includes personnummer (both short and long format), name, address, contact info, and job details."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Person generated successfully",
					content = @Content(mediaType = "application/json",
							examples = @ExampleObject(value = "{\n" +
									"  \"Personnummer\": \"501015-1234\",\n" +
									"  \"longPersonnummer\": \"195010151234\",\n" +
									"  \"namn\": \"Erik\",\n" +
									"  \"efterNamn\": \"Andersson\",\n" +
									"  \"Address\": \"Storgatan 1\",\n" +
									"  \"postAdress\": \"Stockholm\",\n" +
									"  \"zip\": \"11122\",\n" +
									"  \"telefon\": \"0812345678\",\n" +
									"  \"mobil\": \"0701234567\",\n" +
									"  \"jobPosition\": \"Developer\",\n" +
									"  \"jobTitel\": \"Software Engineer\",\n" +
									"  \"email\": \"test123456@example123456.com\",\n" +
									"  \"kommun\": \"Stockholm\"\n" +
									"}")))
	})
	@GetMapping(value = "RandomPersonJson")
	public ResponseEntity<String> RandomPersonJson() {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CACHE_CONTROL, "no-cache");
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
		headers.add(HttpHeaders.CONTENT_ENCODING, "UTF-8");
		headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");

        Random randomizer = new Random();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("static/postnummer.csv");

        if (inputStream == null) {
            colorLogger.logError("postnummer.csv not found in resources");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Resource file not found\"}");
        }

        List<String> lines = IOUtils.readLines(inputStream, "UTF-8");
        String random = lines.get(randomizer.nextInt(lines.size()));
        String[] arrOfStr = random.split(";");

        String from = String.valueOf(Year.now().getValue() - 70);
        String to = String.valueOf(Year.now().getValue() - 17);
        String gen = null;
        String lowerCharacters = "abcdefghijklmnopqrstvxyz";
        String numberCharacters = "0123456789";
        String mail;

        Faker faker = new Faker(new Locale("sv"));

        int maxAttempts = 100;
        for (int i = 0; i < maxAttempts; i++) {
            String rawPersonnummer = faker.idNumber().validSvSeSsn();
            String[] formattedNumbers = formatPersonnummer(rawPersonnummer);
            String shortPersonnummer = formattedNumbers[0];
            String longPersonnummer = formattedNumbers[1];

            // Check if within age range
            int birthYear = Integer.parseInt(longPersonnummer.substring(0, 4));

            if (birthYear > Integer.parseInt(from) && birthYear < Integer.parseInt(to)) {
                mail = RandomStringUtils.random(6, lowerCharacters) +
                        RandomStringUtils.random(6, numberCharacters) + "@" +
                        RandomStringUtils.random(6, lowerCharacters) +
                        RandomStringUtils.random(6, numberCharacters) + ".com";

                gen = "{\"Personnummer\":\"" + shortPersonnummer + "\",\"longPersonnummer\":\"" + longPersonnummer + "\",\"namn\":\""
                        + faker.name().firstName() + "\",\"efterNamn\":\""
                        + faker.name().lastName() + "\",\"Address\":\""
                        + faker.address().streetAddress() + "\",\"postAdress\":\"" + arrOfStr[1] + "\",\"zip\":\""
                        + arrOfStr[0].replace(" ", "") + "\",\"telefon\":\""
                        + faker.phoneNumber().phoneNumber().replace("-", "") + "\",\"mobil\":\""
                        + faker.phoneNumber().cellPhone().replace("-", "") + "\",\"jobPosition\":\""
                        + faker.job().position() + "\",\"jobTitel\":\""
                        + faker.job().title() + "\",\"email\":\"" + mail + "\",\"kommun\":\"" + arrOfStr[2] + "\"}";
                break;
            }
        }

        if (gen == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Could not generate person within age range\"}");
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement je = JsonParser.parseString(gen);
        String prettyJsonString = gson.toJson(je);

        return ResponseEntity.ok()
                .headers(headers)
                .body(prettyJsonString);

    }

	@Operation(
			summary = "Generate Random Swedish Persons (CSV)",
			description = "Generate multiple random Swedish persons with complete details in CSV format. " +
					"Each person includes personnummer (correctly formatted with - or + based on age), " +
					"name, address, phone numbers, email, and job information. Age range: 17-70 years."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Persons generated successfully",
					content = @Content(mediaType = "text/csv")),
			@ApiResponse(responseCode = "400", description = "Invalid parameter")
	})
	@GetMapping(value = "RandomPerson")
	public ResponseEntity<String> RandomPerson(
			@Parameter(description = "Number of persons to generate (1-25000)", required = true)
			@RequestParam(required = true) String antal) {

		if (!antal.matches("[0-9]+")) {
			return ResponseEntity.status(400)
					.body("parameter must be an integer");
		}

		int count = parseInt(antal);
		if (count > 25000) {
			return ResponseEntity.status(400)
					.body("parameter limit 25000");
		}

		if (count < 1) {
			return ResponseEntity.status(400)
					.body("parameter must be at least 1");
		}

		String from = String.valueOf(Year.now().getValue() - 70);
		String to = String.valueOf(Year.now().getValue() - 17);

		long start = System.currentTimeMillis();
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CACHE_CONTROL, "no-cache");
		headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"RandomPerson.csv\"");
		headers.add(HttpHeaders.CONTENT_ENCODING, "UTF-8");
		headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");

		String lowerCharacters = "abcdefghijklmnopqrstvxyz";
		String numberCharacters = "0123456789";

		StringBuilder gen = new StringBuilder("\uFEFF" +
				"Personnummer;longPersonnummer;namn;efterNamn;Address;postAdress;zip;telefon;mobil;jobPosition;jobTitel;email\n");

		Faker faker = new Faker(new Locale("sv"));

		colorLogger.logInfo("Creating RandomPerson: " + antal + " from year: " + from + " to year: " + to);

		int attempts = 0;
		int maxAttempts = count * 10; // Prevent infinite loop

		for (int i = 0; i < count && attempts < maxAttempts; attempts++) {
			String rawPersonnummer = faker.idNumber().validSvSeSsn();
			String[] formattedNumbers = formatPersonnummer(rawPersonnummer);
			String shortPersonnummer = formattedNumbers[0];
			String longPersonnummer = formattedNumbers[1];

			// Check if within age range
			int birthYear = Integer.parseInt(longPersonnummer.substring(0, 4));

			if (birthYear > Integer.parseInt(from) && birthYear < Integer.parseInt(to)) {
				String mail = RandomStringUtils.random(6, lowerCharacters) +
						RandomStringUtils.random(6, numberCharacters) + "@" +
						RandomStringUtils.random(6, lowerCharacters) +
						RandomStringUtils.random(6, numberCharacters) + ".com";

				gen.append(shortPersonnummer).append(";")
						.append(longPersonnummer).append(";")
						.append(faker.name().firstName()).append(";")
						.append(faker.name().lastName()).append(";")
						.append(faker.address().streetAddress()).append(";")
						.append(faker.address().state()).append(";")
						.append(faker.address().zipCode()).append(";")
						.append(faker.phoneNumber().phoneNumber().replace("-", "")).append(";")
						.append(faker.phoneNumber().cellPhone().replace("-", "")).append(";")
						.append(faker.job().position()).append(";")
						.append(faker.job().title()).append(";")
						.append(mail).append("\n");
				i++;
			}
		}

		long finish = System.currentTimeMillis();
		long timeElapsed = finish - start;
		colorLogger.logInfo("RandomPerson exec time: " + timeElapsed + "ms");

		return ResponseEntity.ok()
				.headers(headers)
				.body(gen.toString());
	}

	@Operation(
			summary = "Execute SQL Query",
			description = "Execute a SQL query against a specified database and return results in CSV format. " +
					"⚠️ WARNING: This endpoint executes raw SQL queries. Use with caution and only in test environments."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Query executed successfully",
					content = @Content(mediaType = "text/csv")),
			@ApiResponse(responseCode = "400", description = "Invalid request"),
			@ApiResponse(responseCode = "500", description = "Database error")
	})
	@PostMapping("/SQL")
	public ResponseEntity<String> SQL(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					description = "SQL query request with JDBC connection details",
					required = true,
					content = @Content(
							mediaType = "application/json",
							examples = @ExampleObject(value = "{\n" +
									"  \"jdbc\": \"jdbc:mysql://localhost:3306/testdb\",\n" +
									"  \"sql\": \"SELECT * FROM users LIMIT 10\",\n" +
									"  \"user\": \"dbuser\",\n" +
									"  \"password\": \"dbpass\"\n" +
									"}")
					)
			)
			@RequestBody String jsonString) {

		try {
			colorLogger.logInfo("\nReceived SQL request");

			jsonString = jsonString.replace("'{", "{");
			jsonString = jsonString.replace("}'", "}");

			JSONObject json = new JSONObject(jsonString);

			if (!json.has("jdbc") || !json.has("sql") || !json.has("user") || !json.has("password")) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body("Missing required fields: jdbc, sql, user, password");
			}

			String jdbc = json.getString("jdbc");
			String sql = json.getString("sql");
			String user = json.getString("user");
			String password = json.getString("password");

			String result = OraSQL(sql, jdbc, user, password);
			colorLogger.logInfo("\nQuery executed successfully");

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
					.body(result);

		} catch (JSONException e) {
			colorLogger.logError("Invalid JSON: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("Invalid JSON format: " + e.getMessage());
		} catch (SQLException e) {
			colorLogger.logError("SQL Error: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Database error: " + e.getMessage());
		} catch (Exception e) {
			colorLogger.logError("Unexpected error: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Unexpected error: " + e.getMessage());
		}
	}

	/**
	 * Execute SQL query and return results in CSV format
	 */
	private String OraSQL(String query, String jdbc, String user, String password) throws SQLException {
		try (Connection conn = DriverManager.getConnection(jdbc, user, password);
			 Statement st = conn.createStatement();
			 ResultSet rs = st.executeQuery(query)) {

			colorLogger.logInfo("\nData source created: " + jdbc);
			colorLogger.logInfo("\nRunning query: " + query);

			ResultSetMetaData rsmd = rs.getMetaData();

			// Build column headers
			StringBuilder columns = new StringBuilder();
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				if (i > 1) columns.append(";");
				columns.append(rsmd.getColumnName(i));
			}
			columns.append("\n");

			// Build data rows
			StringBuilder values = new StringBuilder();
			while (rs.next()) {
				for (int j = 1; j <= rsmd.getColumnCount(); j++) {
					if (j > 1) values.append(";");
					String value = rs.getString(j);
					values.append(value != null ? value : "");
				}
				values.append("\n");
			}

			return columns.toString() + values.toString();
		}
	}

	/**
	 * Correctly formats a Swedish personnummer based on age
	 * @param rawPersonnummer The personnummer from Faker (with - or +)
	 * @return Array with [0] = short format (10 digits with - or +), [1] = long format (12 digits)
	 */
	private String[] formatPersonnummer(String rawPersonnummer) {
		// Remove existing delimiter
		String cleanNumber = rawPersonnummer.replace("-", "").replace("+", "");

		// Determine century and full birth year
		String birthYear;

		if (cleanNumber.length() == 10) {
			// Format: YYMMDDXXXX
			String yy = cleanNumber.substring(0, 2);
			int yearPart = Integer.parseInt(yy);
			int currentYear = Year.now().getValue();
			int currentCentury = currentYear / 100;
			int currentYY = currentYear % 100;

			// Determine century based on year
			String century;
			if (yearPart <= currentYY) {
				// Born in current century
				century = String.valueOf(currentCentury);
			} else {
				// Born in previous century
				century = String.valueOf(currentCentury - 1);
			}

			birthYear = century + yy;
		} else {
			// Already has century prefix
			birthYear = cleanNumber.substring(0, 4);
		}

		// Calculate age
		int birthYearInt = Integer.parseInt(birthYear);
		int currentYear = Year.now().getValue();
		int age = currentYear - birthYearInt;

		// Determine correct delimiter based on age
		String delimiter = (age >= 100) ? "+" : "-";

		// Get the last 8 digits (MMDDXXXX)
		String lastEight = cleanNumber.substring(cleanNumber.length() - 8);

		// Create short format (YYMMDD-XXXX or YYMMDD+XXXX)
		String yy = birthYear.substring(2, 4);
		String shortFormat = yy + lastEight.substring(0, 2) + lastEight.substring(2, 4) +
				delimiter + lastEight.substring(4);

		// Create long format (YYYYMMDDXXXX - no delimiter)
		String longFormat = birthYear + lastEight;

		return new String[]{shortFormat, longFormat};
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
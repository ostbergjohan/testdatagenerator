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
import java.util.concurrent.*;

import static java.lang.Integer.parseInt;

@OpenAPIDefinition(
		info = @Info(
				title = "Test Data Generator API",
				version = "v2.0",
				description = "Test Data Generator API for creating realistic Swedish test data. " +
						"Features multithreaded generation for optimal performance.\n\n" +
						"**Endpoints:**\n\n" +
						"**Health & Monitoring**\n" +
						"1. **GET /healthcheck** - API health check and service status verification.\n\n" +
						"**Test Data Generation**\n" +
						"2. **GET /RandomPerson** - Generate Swedish persons with personnummer (CSV format).\n" +
						"   - Parameters: `antal` (1-25000), `from` (birth year), `to` (birth year)\n" +
						"   - Generates: Personnummer, name, address, phone, email, job info\n" +
						"   - Age range: Configurable via from/to parameters\n" +
						"   - Correctly handles 100+ year old persons with '+' delimiter\n" +
						"   - **Multithreaded generation for high performance**\n\n" +
						"3. **GET /RandomPersonJson** - Generate single Swedish person (JSON format).\n" +
						"   - Returns one person with complete details in JSON\n" +
						"   - Includes both short and long personnummer formats\n" +
						"   - Perfect for single record testing\n\n" +
						"4. **GET /RandomUUID** - Generate random UUIDs.\n" +
						"   - Parameters: `antal` (1-50000) - Number of UUIDs\n" +
						"   - Returns: Line-separated UUIDs in CSV format\n" +
						"   - High-performance generation\n\n" +
						"**Database Operations**\n" +
						"5. **POST /SQL** - Execute SQL query and return results.\n" +
						"   - Accepts: JDBC URL, SQL query, credentials\n" +
						"   - Returns: CSV formatted results\n" +
						"   - Supports multiple database types (MySQL, PostgreSQL, Oracle, etc.)\n" +
						"   - ⚠️ Use with caution - executes raw SQL queries\n\n" +
						"**Swedish Personnummer Format:**\n" +
						"- Short format: YYMMDD-XXXX (under 100 years) or YYMMDD+XXXX (100+ years)\n" +
						"- Long format: YYYYMMDDXXXX (12 digits, no delimiter)\n" +
						"- The '+' delimiter indicates person is 100 years or older\n" +
						"- The '-' delimiter indicates person is under 100 years old\n" +
						"- Last 4 digits: 3 random + 1 checksum digit\n\n" +
						"**Performance Features:**\n" +
						"- Multithreaded generation for bulk operations\n" +
						"- Optimized for high-volume test data creation\n" +
						"- Efficient resource utilization\n"
		),
		externalDocs = @ExternalDocumentation(
				description = "GitHub Repository",
				url = "https://github.com/ostbergjohan/testdatagen"
		)
)
@SpringBootApplication
@RestController
@Tag(name = "Test Data Generator", description = "Generate realistic Swedish test data for performance testing with multithreaded processing")
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
			summary = "Health Check Endpoint",
			description = "Verify that the Test Data Generator service is running and responsive. " +
					"Returns a JSON status message confirming service availability. " +
					"Use this endpoint for monitoring and health checks in your infrastructure."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Service is healthy and operational",
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
			description = "Generate a specified number of random UUIDs (Universally Unique Identifiers) for use in test data. " +
					"UUIDs are returned in standard format (e.g., 550e8400-e29b-41d4-a716-446655440000). " +
					"Each UUID is guaranteed to be unique and follows RFC 4122 standards. " +
					"Results are returned in CSV format with one UUID per line. " +
					"**Multithreaded generation for high performance.**"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "UUIDs generated successfully",
					content = @Content(mediaType = "text/csv",
							examples = @ExampleObject(value = "550e8400-e29b-41d4-a716-446655440000\n" +
									"6ba7b810-9dad-11d1-80b4-00c04fd430c8\n" +
									"f47ac10b-58cc-4372-a567-0e02b2c3d479"))),
			@ApiResponse(responseCode = "400", description = "Invalid parameter - must be integer between 1 and 50000",
					content = @Content(mediaType = "text/plain",
							examples = @ExampleObject(value = "parameter must be an integer")))
	})
	@GetMapping(value = "RandomUUID")
	public ResponseEntity<String> RandomUUID(
			@Parameter(description = "Number of UUIDs to generate (1-50000)", required = true, example = "100")
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
		colorLogger.logInfo("Creating RandomUUID: " + antal + " (multithreaded)");

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CACHE_CONTROL, "no-cache");
		headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");
		headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");

		// Multithreaded generation
		int numThreads = Math.min(Runtime.getRuntime().availableProcessors(), Math.max(1, count / 100));
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		List<Future<List<String>>> futures = new ArrayList<>();

		int batchSize = (int) Math.ceil((double) count / numThreads);

		for (int thread = 0; thread < numThreads; thread++) {
			final int startIdx = thread * batchSize;
			final int endIdx = Math.min(startIdx + batchSize, count);
			final int itemsToGenerate = endIdx - startIdx;

			if (itemsToGenerate <= 0) break;

			Future<List<String>> future = executor.submit(() -> generateUUIDBatch(itemsToGenerate));
			futures.add(future);
		}

		// Collect results
		StringBuilder gen = new StringBuilder();
		try {
			for (Future<List<String>> future : futures) {
				List<String> batch = future.get();
				for (String uuid : batch) {
					gen.append(uuid).append("\n");
				}
			}
		} catch (InterruptedException | ExecutionException e) {
			colorLogger.logError("Error during multithreaded UUID generation: " + e.getMessage());
			executor.shutdown();
			return ResponseEntity.status(500).body("Error generating UUIDs");
		}

		executor.shutdown();

		long finish = System.currentTimeMillis();
		long timeElapsed = finish - start;
		colorLogger.logInfo("RandomUUID exec time: " + timeElapsed + "ms (multithreaded with " + numThreads + " threads)");

		return ResponseEntity.ok()
				.headers(headers)
				.body(gen.toString());
	}

	/**
	 * Generate a batch of UUIDs (helper method for multithreading)
	 */
	private List<String> generateUUIDBatch(int count) {
		List<String> results = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			results.add(UUID.randomUUID().toString());
		}
		return results;
	}

	@Operation(
			summary = "Generate Random Swedish Person (JSON)",
			description = "Generate a single random Swedish person with complete details in JSON format. " +
					"Includes valid Swedish personnummer (both short and long format), realistic name, address, " +
					"contact information, and job details. Perfect for single record testing or API integration testing. " +
					"The personnummer correctly handles age-based delimiter rules (- for under 100 years, + for 100+ years)."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Person generated successfully with complete details",
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
			return ResponseEntity.status(500).body("{\"error\":\"Configuration error: postnummer.csv not found\"}");
		}

		try {
			Faker faker = new Faker(new Locale("sv-SE"));
			String lowerCharacters = "abcdefghijklmnopqrstuvwxyz";
			String numberCharacters = "0123456789";

			// Parse CSV file
			String csvContent = IOUtils.toString(inputStream, "UTF-8");
			String[] lines = csvContent.split("\n");
			String line = lines[randomizer.nextInt(lines.length)];
			String[] values = line.split(";");

			String postnummer = values[0].trim();
			String kommun = values[1].trim();

			// Generate personnummer using Faker
			String rawPersonnummer = faker.idNumber().validSvSeSsn();
			String[] formattedPersonnummer = formatPersonnummer(rawPersonnummer);
			String shortPersonnummer = formattedPersonnummer[0];
			String longPersonnummer = formattedPersonnummer[1];

			String mail = RandomStringUtils.random(6, lowerCharacters) +
					RandomStringUtils.random(6, numberCharacters) + "@" +
					RandomStringUtils.random(6, lowerCharacters) +
					RandomStringUtils.random(6, numberCharacters) + ".com";

			JSONObject person = new JSONObject();
			person.put("Personnummer", shortPersonnummer);
			person.put("longPersonnummer", longPersonnummer);
			person.put("namn", faker.name().firstName());
			person.put("efterNamn", faker.name().lastName());
			person.put("Address", faker.address().streetAddress());
			person.put("postAdress", faker.address().state());
			person.put("zip", postnummer);
			person.put("telefon", faker.phoneNumber().phoneNumber().replace("-", ""));
			person.put("mobil", faker.phoneNumber().cellPhone().replace("-", ""));
			person.put("jobPosition", faker.job().position());
			person.put("jobTitel", faker.job().title());
			person.put("email", mail);
			person.put("kommun", kommun);

			return ResponseEntity.ok()
					.headers(headers)
					.body(person.toString());

		} catch (IOException e) {
			colorLogger.logError("Error reading postnummer.csv: " + e.getMessage());
			return ResponseEntity.status(500).body("{\"error\":\"Failed to read postal code data\"}");
		} catch (Exception e) {
			colorLogger.logError("Error generating person: " + e.getMessage());
			return ResponseEntity.status(500).body("{\"error\":\"Failed to generate person data\"}");
		}
	}

	@Operation(
			summary = "Generate Multiple Random Swedish Persons (CSV)",
			description = "Generate multiple random Swedish persons with complete details in CSV format using multithreaded processing. " +
					"Each person includes valid Swedish personnummer (both formats), realistic name, address, contact info, and job details. " +
					"Supports filtering by birth year range for age-specific test data. " +
					"**Performance optimized with parallel processing for bulk generation.**\n\n" +
					"**CSV Format:** " +
					"Personnummer;longPersonnummer;FirstName;LastName;Address;State;Zip;Phone;Mobile;JobPosition;JobTitle;Email\n\n" +
					"**Usage Examples:**\n" +
					"- Generate 100 persons born 1950-2000: `?antal=100&from=1950&to=2000`\n" +
					"- Generate 1000 young adults: `?antal=1000&from=1995&to=2005`\n" +
					"- Generate 500 seniors: `?antal=500&from=1940&to=1960`"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Persons generated successfully in CSV format",
					content = @Content(mediaType = "text/csv",
							examples = @ExampleObject(value = "501015-1234;195010151234;Erik;Andersson;Storgatan 1;Stockholm;11122;0812345678;0701234567;Developer;Software Engineer;test123@example456.com\n" +
									"920325-5678;199203255678;Anna;Svensson;Drottninggatan 2;Göteborg;41118;0317654321;0709876543;Manager;Project Manager;user789@test123.com"))),
			@ApiResponse(responseCode = "400", description = "Invalid parameters",
					content = @Content(mediaType = "text/plain",
							examples = @ExampleObject(value = "parameter limit 25000")))
	})
	@GetMapping(value = "RandomPerson")
	public ResponseEntity<String> RandomPerson(
			@Parameter(description = "Number of persons to generate (1-25000)", required = true, example = "100")
			@RequestParam String antal,
			@Parameter(description = "Birth year range start (e.g., 1950)", required = true, example = "1950")
			@RequestParam String from,
			@Parameter(description = "Birth year range end (e.g., 2005)", required = true, example = "2005")
			@RequestParam String to) {

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

		long start = System.currentTimeMillis();
		colorLogger.logInfo("Creating RandomPerson: " + antal + " (multithreaded)");

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CACHE_CONTROL, "no-cache");
		headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");
		headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");

		// Multithreaded generation
		int numThreads = Math.min(Runtime.getRuntime().availableProcessors(), Math.max(1, count / 100));
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		List<Future<List<String>>> futures = new ArrayList<>();

		int batchSize = (int) Math.ceil((double) count / numThreads);

		for (int thread = 0; thread < numThreads; thread++) {
			final int startIdx = thread * batchSize;
			final int endIdx = Math.min(startIdx + batchSize, count);
			final int itemsToGenerate = endIdx - startIdx;

			if (itemsToGenerate <= 0) break;

			Future<List<String>> future = executor.submit(() -> generatePersonBatch(itemsToGenerate, from, to));
			futures.add(future);
		}

		// Collect results
		StringBuilder gen = new StringBuilder();
		try {
			for (Future<List<String>> future : futures) {
				List<String> batch = future.get();
				for (String person : batch) {
					gen.append(person);
				}
			}
		} catch (InterruptedException | ExecutionException e) {
			colorLogger.logError("Error during multithreaded generation: " + e.getMessage());
			executor.shutdown();
			return ResponseEntity.status(500).body("Error generating persons");
		}

		executor.shutdown();

		long finish = System.currentTimeMillis();
		long timeElapsed = finish - start;
		colorLogger.logInfo("RandomPerson exec time: " + timeElapsed + "ms (multithreaded with " + numThreads + " threads)");

		return ResponseEntity.ok()
				.headers(headers)
				.body(gen.toString());
	}

	/**
	 * Generate a batch of persons (helper method for multithreading)
	 */
	private List<String> generatePersonBatch(int count, String from, String to) {
		List<String> results = new ArrayList<>();
		Faker faker = new Faker(new Locale("sv-SE"));
		String lowerCharacters = "abcdefghijklmnopqrstuvwxyz";
		String numberCharacters = "0123456789";

		int generated = 0;
		int attempts = 0;
		int maxAttempts = count * 10;

		while (generated < count && attempts < maxAttempts) {
			attempts++;

			String rawPersonnummer = faker.idNumber().validSvSeSsn();
			String[] formattedPersonnummer = formatPersonnummer(rawPersonnummer);
			String shortPersonnummer = formattedPersonnummer[0];
			String longPersonnummer = formattedPersonnummer[1];

			int birthYear = Integer.parseInt(longPersonnummer.substring(0, 4));

			if (birthYear > Integer.parseInt(from) && birthYear < Integer.parseInt(to)) {
				String mail = RandomStringUtils.random(6, lowerCharacters) +
						RandomStringUtils.random(6, numberCharacters) + "@" +
						RandomStringUtils.random(6, lowerCharacters) +
						RandomStringUtils.random(6, numberCharacters) + ".com";

				StringBuilder person = new StringBuilder();
				person.append(shortPersonnummer).append(";")
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

				results.add(person.toString());
				generated++;
			}
		}

		return results;
	}

	@Operation(
			summary = "Execute SQL Query",
			description = "Execute a SQL query against a specified database and return results in CSV format. " +
					"Supports multiple database types including MySQL, PostgreSQL, Oracle, SQL Server, and more via JDBC. " +
					"Results are formatted as semicolon-separated CSV with column headers. " +
					"⚠️ **WARNING:** This endpoint executes raw SQL queries. Use with caution and only in test environments. " +
					"Never expose this endpoint in production without proper authentication and authorization.\n\n" +
					"**Supported Databases:**\n" +
					"- MySQL: `jdbc:mysql://host:port/database`\n" +
					"- PostgreSQL: `jdbc:postgresql://host:port/database`\n" +
					"- Oracle: `jdbc:oracle:thin:@host:port:sid`\n" +
					"- SQL Server: `jdbc:sqlserver://host:port;databaseName=db`\n\n" +
					"**Security Notes:**\n" +
					"- Only use in isolated test environments\n" +
					"- Credentials are transmitted in request body\n" +
					"- No query validation or sanitization is performed\n" +
					"- Use read-only database accounts when possible"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Query executed successfully, results returned in CSV format",
					content = @Content(mediaType = "text/csv",
							examples = @ExampleObject(value = "id;name;email\n1;John Doe;john@example.com\n2;Jane Smith;jane@example.com"))),
			@ApiResponse(responseCode = "400", description = "Invalid request - missing required fields",
					content = @Content(mediaType = "text/plain",
							examples = @ExampleObject(value = "Missing required fields: jdbc, sql, user, password"))),
			@ApiResponse(responseCode = "500", description = "Database connection or query execution error",
					content = @Content(mediaType = "text/plain",
							examples = @ExampleObject(value = "Database error: Connection refused")))
	})
	@PostMapping("/SQL")
	public ResponseEntity<String> SQL(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					description = "SQL query request with JDBC connection details and credentials",
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

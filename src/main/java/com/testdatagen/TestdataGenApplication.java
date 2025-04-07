package com.testdatagen;

import co.elastic.apm.attach.ElasticApmAttacher;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.github.javafaker.Faker;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.Year;
import java.util.*;
import java.util.logging.Logger;

import static java.lang.Integer.parseInt;

@SpringBootApplication
@RestController

public class TestdataGenApplication {

	public static void main(String[] args) {
		ElasticApmAttacher.attach();
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
	@RequestMapping(value = "getInskrivning")
	public ResponseEntity<String> getInskrivning(@RequestParam String identitetsbeteckning)
	{
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CACHE_CONTROL, "no-cache");
		headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");
		headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "*");
		String svar;
		try {
			svar = executeGetData("https://ipf-test.arbetsformedlingen.se/ais-f-inskrivning/v2/arbetssokande/"+ identitetsbeteckning);
		} catch (IOException e) {
			colorLogger.logError(e.toString());
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			colorLogger.logError(e.toString());
			throw new RuntimeException(e);
		} catch (KeyStoreException e) {
			colorLogger.logError(e.toString());
			throw new RuntimeException(e);
		}

		return ResponseEntity.ok()
				.headers(headers)
				.body(svar);
	}
	@ResponseBody
	@RequestMapping("/inskrivning")
	public ResponseEntity<String> postBody(@RequestBody String body) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CACHE_CONTROL, "no-cache");
		headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "*");
		headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");

		String svar;
		try {
			try {
				svar = executePostData(body, "https://ipf-test.arbetsformedlingen.se/ais-f-inskrivning/v2/inskrivning");
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			} catch (KeyStoreException e) {
				throw new RuntimeException(e);
			}

		} catch (IOException e) {
			colorLogger.logError(e.toString());
			throw new RuntimeException(e);
		}
		return ResponseEntity.ok()
				.headers(headers)
				.body(svar);
	}


	@RequestMapping(value = "RandomUUID")
	public ResponseEntity<String> RandomUUID(@RequestParam String antal)
	{
		if (antal.matches("[0-9]+")){

		} else {
			return ResponseEntity.status(400)
					.body("parameter must be an integer");
		}

		if (parseInt(antal) > 50000) {
			return ResponseEntity.status(400)
					.body("parameter limit 50000");
		}

		long start = System.currentTimeMillis();

		colorLogger.logInfo("Creating RandomUUID: " + antal);

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CACHE_CONTROL, "no-cache");
		headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");
		headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "*");

		String gen="";
		for (int i = 0; i < parseInt(antal); i++) {
			gen = UUID.randomUUID().toString() + "\n" + gen ;
		}
		long finish = System.currentTimeMillis();
		long timeElapsed = finish - start;
		colorLogger.logInfo("RandomUUID exec time: " + timeElapsed +"ms");

		return ResponseEntity.ok()
				.headers(headers)
				.body(gen);
	}

	@RequestMapping(value = "RandomPersonJson")
	public ResponseEntity<String> RandomPersonJson()
	{
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CACHE_CONTROL, "no-cache");
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
		headers.add(HttpHeaders.CONTENT_ENCODING, "UTF-8");
		headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "*");

		Random randomizer = new Random();
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("static/postnummer.csv");
		List<String> lines = null;
		lines = IOUtils.readLines(inputStream);
		String random = lines.get(randomizer.nextInt(lines.size()));
		String[] arrOfStr = random.split(";");

		String from = String.valueOf(Year.now().getValue()-70);
		String to = String.valueOf(Year.now().getValue()-17);
		String gen = null;
		String lowerCharacters = "abcdefghijklmnopqrstvxyz";
		String numberCharacters = "0123456789";
		String mail = null;
		String Personnummer = null;
		String KortPersonnummer = null;
		int antal = 1;

		Faker faker = new Faker((new Locale("sv")));

		for (int i = 0; i < antal; i++) {
			Personnummer = faker.idNumber().validSvSeSsn();
			KortPersonnummer = Personnummer;

			if (Personnummer.contains("+")) {
				Personnummer = Personnummer.replace("+", "");
				Personnummer = "20" + Personnummer;
			}
			if (Personnummer.contains("-")) {
				Personnummer = Personnummer.replace("-","");
				Personnummer="19" + Personnummer;
			}
			mail = RandomStringUtils.random(6, lowerCharacters) + RandomStringUtils.random(6, numberCharacters) + "@" + RandomStringUtils.random(6, lowerCharacters) + RandomStringUtils.random(6, numberCharacters) + ".com";

			if (parseInt(Personnummer.substring(0,4)) > parseInt(from) && parseInt(Personnummer.substring(0,4)) < parseInt(to) ){

				gen = "{\"Personnummer\":\""+ KortPersonnummer + "\",\"longPersonnummer\":\""+ Personnummer + "\",\"namn\":\""
						+ faker.name().firstName() + "\",\"efterNamn\":\""
						+ faker.name().lastName() +  "\",\"Address\":\""
						+ faker.address().streetAddress() + "\",\"postAdress\":\""+ arrOfStr[1] + "\",\"zip\":\""
						+ arrOfStr[0].replace(" ", "") + "\",\"telefon\":\""
						+ faker.phoneNumber().phoneNumber().replace("-", "") +"\",\"mobil\":\""
						+ faker.phoneNumber().cellPhone().replace("-", "") + "\",\"jobPosition\":\""
						+ faker.job().position()  + "\",\"jobTitel\":\""
						+ faker.job().title() +"\",\"email\":\""+ mail  + "," + "\",\"kommun\":\"" +arrOfStr[2] +"\"}";
			}
			else {
				i--;
				continue;
			}
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(gen);
		String prettyJsonString = gson.toJson(je);

		return ResponseEntity.ok()
				.headers(headers)
				.body(prettyJsonString);
	}

	@RequestMapping(value = "RandomPerson")
	public ResponseEntity<String> RandomPerson(@RequestParam(required=true) String antal)
	{
		if (antal.matches("[0-9]+")){

		} else {
			return ResponseEntity.status(400)
					.body("parameter must be an integer");
		}

		if (parseInt(antal) > 25000) {
			return ResponseEntity.status(400)
					.body("parameter limit 25000");
		}

		String from = String.valueOf(Year.now().getValue()-70);
		String to = String.valueOf(Year.now().getValue()-17);

		long start = System.currentTimeMillis();
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CACHE_CONTROL, "no-cache");
		headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "RandomPerson.csv");
		headers.add(HttpHeaders.CONTENT_ENCODING, "UTF-8");
		headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "*");
		String gen = null;
		String lowerCharacters = "abcdefghijklmnopqrstvxyz";
		String numberCharacters = "0123456789";
		String mail = null;

		gen = "\uFEFF" + "Personnummer;longPersonnummer;namn;efterNamn;Address;postAdress;zip;telefon;mobil;jobPosition;jobTitel;email\n";
		//Faker faker = new Faker(new Locale("sv"));
		Faker faker = new Faker((new Locale("sv")));

		String Personnummer = null;
		String KortPersonnummer = null;

		colorLogger.logInfo("Creating RandomPerson: " + antal + " from year: " + from + " to year: " + to );

		for (int i = 0; i < parseInt(antal); i++) {
			Personnummer = faker.idNumber().validSvSeSsn();
			KortPersonnummer = Personnummer;

			if (Personnummer.contains("+")) {
				Personnummer = Personnummer.replace("+", "");
				Personnummer = "20" + Personnummer;
			}
			if (Personnummer.contains("-")) {
				Personnummer = Personnummer.replace("-","");
				Personnummer="19" + Personnummer;
			}

			if (parseInt(Personnummer.substring(0,4)) > parseInt(from) && parseInt(Personnummer.substring(0,4)) < parseInt(to) ){
				mail = RandomStringUtils.random(6, lowerCharacters) + RandomStringUtils.random(6, numberCharacters) + "@" + RandomStringUtils.random(6, lowerCharacters) + RandomStringUtils.random(6, numberCharacters) + ".com";

				gen = gen + KortPersonnummer + ";" + Personnummer + ";" + faker.name().firstName() + ";" + faker.name().lastName() + ";" + faker.address().streetAddress()
						+ ";" + faker.address().state() + ";" + faker.address().zipCode()
						+ ";" + faker.phoneNumber().phoneNumber().replace("-", "") + ";" + faker.phoneNumber().cellPhone().replace("-", "") + ";" + faker.job().position() +
						";" + faker.job().title() + ";" + mail
						//faker.name().username().replace("å","a").replace("ö","o").replace("ä","a") + "@gmail.com"
						+ "\n";
			}
			else {
				i--;
				continue;
			}
		}
		long finish = System.currentTimeMillis();
		long timeElapsed = finish - start;
		colorLogger.logInfo("RandomPerson exec time: " + timeElapsed + "ms");
		return ResponseEntity.ok()
				.headers(headers)
				.body(gen);
	}
	@PostMapping("/SQL")
	public String SQL(@RequestBody String jsonString) throws JSONException, IOException, SQLException {

		colorLogger.logInfo("\n + json string:\n" + jsonString);
		jsonString= jsonString.replace("'{","{");
		jsonString= jsonString.replace("}'","}");
		JSONObject json = new JSONObject(jsonString);
		String jdbc, sql, user, password;
		jdbc = json.get("jdbc").toString();
		sql = json.get("sql").toString();
		user = json.get("user").toString();
		password = json.get("password").toString();
		String result = OraSQL(sql,jdbc,user,password);
		colorLogger.logInfo("\n result: \n"+ result);
		return result;
	}

	public String OraSQL(String query, String jdbc, String user, String password) throws SQLException {

		Connection conn=DriverManager.getConnection(
				jdbc,user,password);

		colorLogger.logInfo("\nData source created: \n" + jdbc);
		colorLogger.logInfo("\nRunning query: \n" + query);
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery(query);
		ResultSetMetaData rsmd;
		rsmd = rs.getMetaData();
		String Columns = null;
		int i = 1;
		while (i <= rsmd.getColumnCount()) {
			if (i==1) {
				Columns = rsmd.getColumnName(i) + ";";
			} else {
				Columns = Columns + rsmd.getColumnName(i) + ";";
			}
			i++;
		}
		String Values = null;
		int j = 1;
		while (rs.next()) {
			j=1;
			while (j <= rsmd.getColumnCount()) {
				if (Values == null) {
					Values = rs.getString(j)+ ";";
				}else {
					Values = Values + rs.getString(j)+ ";";
				}
				j++;
			}
			Values = Values + "\n";
		}
		conn.close();
		return Columns + "\n" + Values;
	}

	public String executePostData(String postJsonData, String UrlString) throws IOException, NoSuchAlgorithmException, KeyStoreException {
		SSLContext sslContext = null;
		try {
			sslContext = new SSLContextBuilder()
					.loadTrustMaterial(null, (TrustStrategy) (arg0, arg1) -> true).build();
		} catch (KeyManagementException e) {
			throw new RuntimeException(e);
		}

		CloseableHttpClient httpClient = HttpClients
				.custom()
				.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
				.setSSLContext(sslContext)
				.build();

		HttpUriRequest request = RequestBuilder.post()
				.setUri(UrlString)
				.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
				.setHeader("client_id", "76f2f68dbf754932b7e2ec79bb282d37")
				.setHeader("client_secret", "0e57888BdE4f434eBF850518C3FB5397")
				.setHeader("AF-TrackingId", "d93035e0-36ac-482e-9297-285d74015ae4")
				.setHeader("AF-SystemId", "testdatagen")
				.setEntity(new StringEntity(postJsonData))
				.build();

		//System.out.println(httpClient.execute(request));
		String result = "";

		CloseableHttpResponse response = httpClient.execute(request);
		result = EntityUtils.toString(response.getEntity());
		colorLogger.logInfo(result);
		return result;
	}
	public String executeGetData(String UrlString) throws IOException, NoSuchAlgorithmException, KeyStoreException {
		SSLContext sslContext = null;
		try {
			sslContext = new SSLContextBuilder()
					.loadTrustMaterial(null, (TrustStrategy) (arg0, arg1) -> true).build();
		} catch (KeyManagementException e) {
			throw new RuntimeException(e);
		}

		CloseableHttpClient httpClient = HttpClients
				.custom()
				.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
				.setSSLContext(sslContext)
				.build();

		HttpUriRequest request = RequestBuilder.get()
				.setUri(UrlString)
				.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
				.setHeader("client_id", "76f2f68dbf754932b7e2ec79bb282d37")
				.setHeader("client_secret", "0e57888BdE4f434eBF850518C3FB5397")
				.setHeader("AF-TrackingId", "d93035e0-36ac-482e-9297-285d74015ae4")
				.setHeader("AF-SystemId", "testdatagen")
				.build();

		//System.out.println(httpClient.execute(request));
		String result = "";

		CloseableHttpResponse response = httpClient.execute(request);
		result = EntityUtils.toString(response.getEntity());
		colorLogger.logInfo(result);
		return result;
	}

	public class ColorLogger {

		private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("");

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






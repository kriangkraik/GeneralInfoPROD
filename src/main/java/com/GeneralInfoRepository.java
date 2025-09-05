package com;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import lombok.extern.slf4j.Slf4j;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@Slf4j
public class GeneralInfoRepository {

	private final RestTemplate restTemplate = new RestTemplate();
	private final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

	@Value("${soapendpointurl}")
	private String soapEndpointUrl;

	@Value("${jwt.secret}")
	private String jwtSecret;

	/**
	 * Validate JWT and request claim info
	 */
	public String getGeneralInfo(String tokenCode, String claimNo) {
		JSONObject json;
		try {
			if (!StringUtils.hasText(tokenCode) || !StringUtils.hasText(claimNo)) {
				log.warn("Invalid input parameters - tokenCode or claimNo is empty");
				return createEmptyResponse("Invalid input parameters").toString();
			}

			// Verify JWT
			Algorithm algorithm = Algorithm.HMAC512(jwtSecret);
			JWTVerifier verifier = JWT.require(algorithm).build();
			DecodedJWT jwt = verifier.verify(tokenCode);

			// Decode payload
			JSONObject payload = new JSONObject(
					new String(Base64.getUrlDecoder().decode(jwt.getPayload()), StandardCharsets.UTF_8));

			// Check Username
			if ("EMCS_PROD".equals(payload.optString("Username"))) {
				log.debug("JWT validation successful");
				json = sendDataToNotes(tokenCode, claimNo);
			} else {
				log.warn("Invalid username");
				json = createEmptyResponse("Invalid Username");
			}

		} catch (JWTVerificationException e) {
			log.error("JWT verification failed", e);
			json = createEmptyResponse("JWT Verification Failed: " + e.getMessage());
		}

		return json.toString();
	}

	/**
	 * Send SOAP request to Notes service
	 */
	public JSONObject sendDataToNotes(String token, String claimNo) {
		String xml = """
				<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
				                  xmlns:urn="urn:WSA-Safety:GetGeneralInfoPROD:GetGeneralInfoPROD">
				   <soapenv:Header/>
				   <soapenv:Body>
				      <urn:GetGenInf>
				         <urn:TOKENCODE>%s</urn:TOKENCODE>
				         <urn:CLAIMNO>%s</urn:CLAIMNO>
				      </urn:GetGenInf>
				   </soapenv:Body>
				</soapenv:Envelope>
				""".formatted(token, claimNo);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_XML);
		headers.add("SOAPAction", "");

		try {
			ResponseEntity<String> response = restTemplate.exchange(
					soapEndpointUrl,
					HttpMethod.POST,
					new HttpEntity<>(xml, headers),
					String.class);

			return parseXmlToJson(response.getBody());
		} catch (Exception e) {
			return createEmptyResponse("SOAP Request Failed: " + e.getMessage());
		}
	}

	/**
	 * Parse XML response to JSON
	 */
	private JSONObject parseXmlToJson(String xml) {
		try {
			DocumentBuilder builder = dbFactory.newDocumentBuilder();
			try (StringReader sr = new StringReader(xml)) {
				Document doc = builder.parse(new InputSource(sr));

				JSONObject json = new JSONObject();
				json.put("CLAIMNO", getTagValue(doc, "CLAIMNO"));
				json.put("POLICYNO", getTagValue(doc, "POLICYNO"));
				json.put("CPOLICYNO", getTagValue(doc, "CPOLICYNO"));
				json.put("INSURANCETYPE", getTagValue(doc, "INSURANCETYPE"));
				json.put("CLAIMNOTINO", getTagValue(doc, "CLAIMNOTINO"));
				json.put("INSUREDNAME", getTagValue(doc, "INSUREDNAME"));
				json.put("CREATEDDATE", getTagValue(doc, "CREATEDDATE"));
				json.put("CREATEDTIME", getTagValue(doc, "CREATEDTIME"));
				json.put("COSI", getTagValue(doc, "COSI"));
				json.put("DATEOFLOSS", getTagValue(doc, "DATEOFLOSS"));
				json.put("TIMEOFLOSS", getTagValue(doc, "TIMEOFLOSS"));
				json.put("INFORMERNAME", getTagValue(doc, "INFORMERNAME"));
				json.put("ERRORCODE", getTagValue(doc, "ERRORCODE"));
				json.put("ERRORMESSAGE", getTagValue(doc, "ERRORMESSAGE"));
				json.put("CARTHIRDPARTY", getTagValue(doc, "CARTHIRDPARTY"));
				return json;
			}
		} catch (Exception e) {
			return createEmptyResponse("XML Parse Error: " + e.getMessage());
		}
	}

	/**
	 * Get XML tag value
	 */
	private String getTagValue(Document doc, String tag) {
		return doc.getElementsByTagName(tag).getLength() > 0
				? doc.getElementsByTagName(tag).item(0).getTextContent()
				: "";
	}

	/**
	 * Create default empty response with error message
	 */
	private JSONObject createEmptyResponse(String errorMsg) {
		JSONObject json = new JSONObject();
		json.put("CLAIMNO", "");
		json.put("POLICYNO", "");
		json.put("CPOLICYNO", "");
		json.put("INSURANCETYPE", "");
		json.put("CLAIMNOTINO", "");
		json.put("INSUREDNAME", "");
		json.put("CREATEDDATE", "");
		json.put("CREATEDTIME", "");
		json.put("COSI", "");
		json.put("DATEOFLOSS", "");
		json.put("TIMEOFLOSS", "");
		json.put("INFORMERNAME", "");
		json.put("ERRORCODE", "");
		json.put("ERRORMESSAGE", errorMsg);
		json.put("CARTHIRDPARTY", "");
		return json;
	}
}

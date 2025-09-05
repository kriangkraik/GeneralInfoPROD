package com;

import org.json.JSONObject;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import generalinfo.GetGeneralInfoRequest;
import generalinfo.GetGeneralInfoResponse;
import lombok.RequiredArgsConstructor;

@Endpoint
@RequiredArgsConstructor
public class GeneralInfoEndpoint {

	private static final String NAMESPACE_URI = "GeneralInfo";
	private final GeneralInfoRepository infoRepository;

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetGeneralInfoRequest")
	@ResponsePayload
	public GetGeneralInfoResponse getToken(@RequestPayload GetGeneralInfoRequest request) {
		GetGeneralInfoResponse response = new GetGeneralInfoResponse();
		String result = infoRepository.getGeneralInfo(request.getTOKENCODE(), request.getCLAIMNO());
		JSONObject json = new JSONObject(result);
		response.setCLAIMNO(json.get("CLAIMNO").toString());
		response.setPOLICYNO(json.get("POLICYNO").toString());
		response.setCPOLICYNO(json.get("CPOLICYNO").toString());
		response.setINSURANCETYPE(json.get("INSURANCETYPE").toString());
		response.setCLAIMNOTINO(json.get("CLAIMNOTINO").toString());
		response.setINSUREDNAME(json.get("INSUREDNAME").toString());
		response.setCREATEDDATE(json.get("CREATEDDATE").toString());
		response.setCREATEDTIME(json.get("CREATEDTIME").toString());
		response.setCOSI(json.get("COSI").toString());
		response.setDATEOFLOSS(json.get("DATEOFLOSS").toString());
		response.setTIMEOFLOSS(json.get("TIMEOFLOSS").toString());
		response.setINFORMERNAME(json.get("INFORMERNAME").toString());
		response.setERRORCODE(json.get("ERRORCODE").toString());
		response.setERRORMESSAGE(json.get("ERRORMESSAGE").toString());
		response.setCARTHIRDPARTY(json.get("CARTHIRDPARTY").toString());

		return response;
	}
}

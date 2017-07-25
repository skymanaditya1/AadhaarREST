package in.gov.uidai.auth.sampleapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import in.gov.uidai.auth.aua.helper.AuthRequestCreator;
import in.gov.uidai.auth.aua.helper.AuthResponseValidator;
import in.gov.uidai.auth.aua.helper.DigitalSigner;
import in.gov.uidai.auth.aua.helper.SignatureVerifier;
import in.gov.uidai.auth.aua.httpclient.AuthClient;
import in.gov.uidai.auth.device.helper.AuthAUADataCreator;
import in.gov.uidai.auth.device.helper.Encrypter;
import in.gov.uidai.auth.device.helper.PidCreator;
import in.gov.uidai.auth.device.model.AuthDataFromDeviceToAUA;
import in.gov.uidai.auth.device.model.AuthResponseDetails;
import in.gov.uidai.auth.device.model.DeviceCollectedAuthData;
import in.gov.uidai.authentication.common.types._1.LocationType;
import in.gov.uidai.authentication.common.types._1.Meta;
import in.gov.uidai.authentication.uid_auth_request._1.Auth;
import in.gov.uidai.authentication.uid_auth_request._1.DataType;
import in.gov.uidai.authentication.uid_auth_request._1.Tkn;
import in.gov.uidai.authentication.uid_auth_request._1.Uses;
import in.gov.uidai.authentication.uid_auth_request._1.UsesFlag;
import in.gov.uidai.authentication.uid_auth_request_data._1.MatchingStrategy;
import in.gov.uidai.authentication.uid_auth_response._1.AuthRes;

class RestClient {

	private AuthClient authClient;
	private AuthResponseValidator authResponseValidator;
	private AuthAUADataCreator auaDataCreator = null;
	// private final String staging_Certificate = "/Users/aishwaryat/Downloads/Staging_Signature_PrivateKey.p12";
	private String signatureAlias = "";
	private String signaturePassword = "";
	private String certificatePath = "";
	private String certificateDSIGKey = "";
	private String signatureLicenseKey = "";
	private String authServerURL = "";
	boolean useProto = true;
	private String aadhaarNumber = "999999990019";
	private String name = "Shivshankar Choudhury";
	private String udc = "";
	private String pincode = "";
	private String terminalID = "";
	private String auaCode = "";
	private String serviceAgency = "";
	private String licenseKey = "";
	private String publicKeyFile = "";
	
@SuppressWarnings("unused")
private Uses createUsesElement() {
		
		Uses uses = new Uses();
		uses.setPi(UsesFlag.valueOf(true ? "Y" : "N"));
		uses.setPa(UsesFlag.valueOf(false ? "Y" : "N"));
		uses.setPin(UsesFlag.valueOf(false ? "Y" : "N"));
		uses.setOtp(UsesFlag.valueOf(false ? "Y" : "N"));
		uses.setBio(UsesFlag.valueOf(false ? "Y" : "N"));
		uses.setPfa(UsesFlag.valueOf(false ? "Y" : "N"));
		
		return uses;
	}
	
private DeviceCollectedAuthData constructAuthRequest() {
	DeviceCollectedAuthData request = new DeviceCollectedAuthData();

	
	String uid = aadhaarNumber;
	request.setUid(uid);
	
	request.setLanguage(null);
	request.setName(name);
	
	// Assemble gender
	request.setGender("Select gender");
	// request.setGender((String) jComboGender.getSelectedItem());

	// Assemble DOB
	String dob = null;


	request.setDob(dob);
	
	request.setNameMatchValue(100);
	request.setLocalNameMatchValue(90);

	request.setFullAddress(null);
	request.setLocalFullAddress(null);
	request.setFullAddressMatchValue(100);
	request.setLocalFullAddressMatchValue(90);
	
	// Name match strategy
	request.setNameMatchStrategy(MatchingStrategy.E);

	// Pa match strategy
	// request.setAddressMatchStrategy(jRadioButtonAddressExactMatch.isSelected() ? MatchingStrategy.E : MatchingStrategy.P);
	request.setAddressMatchStrategy(MatchingStrategy.E);
	
	// Pfa match strategy
	request.setFullAddressMatchStrategy(MatchingStrategy.E);
	
		Meta m = createMeta();
		request.setDeviceMetaData(m);
		
		return request;

	}

	private Meta createMeta() {
		Meta m = new Meta();
		// m.setFdc(this.jTextFieldFDC.getText());
		m.setFdc("NC");
		// m.setIdc(this.jTextFieldIDC.getText());
		m.setIdc("NA");
		// m.setPip(this.jTextFieldPIP.getText());
		m.setPip("127.0.0.1");
		// m.setLot(LocationType.valueOf(this.jComboBoxLocationType.getSelectedItem().toString()));
		m.setLot(LocationType.valueOf("P"));
		// m.setLov(this.jTextFieldLocationValue.getText());
		m.setLov(pincode);
		// m.setUdc(this.jTextFieldUDC.getText());
		m.setUdc(udc);
		return m;
	}
	
	// method to read the XML file and generate the response 
	public String generateResponse(AuthRes authResult) {
		return authResult.getRet().toString();
	}

	@SuppressWarnings("static-access")
	public String authenticate(String uid, String name) {
		String response = "No";
		loadPasswordHash();

		try {
			authClient = new AuthClient(new URL("http://auth.uidai.gov.in/1.6").toURI());
			DigitalSigner ds = new DigitalSigner(certificatePath, signaturePassword.toCharArray(),
					signatureAlias);
			authClient.setDigitalSignator(ds);
			authClient.setAsaLicenseKey(signatureLicenseKey);
			authResponseValidator = new AuthResponseValidator(new SignatureVerifier(certificateDSIGKey));
			auaDataCreator = new AuthAUADataCreator(new Encrypter(publicKeyFile), "YES".equalsIgnoreCase("Yes"));
			new URL(authServerURL).openConnection().connect();
			Uses usesElement = createUsesElement();
			AuthDataFromDeviceToAUA auaData = null;
			DeviceCollectedAuthData authData = constructAuthRequest();
			if(useProto) {
				auaData = auaDataCreator.prepareAUAData(authData.getUid(), terminalID, authData.getDeviceMetaData(), (Object) PidCreator.createProtoPid(authData), DataType.P);
			}
			else {
				auaData = auaDataCreator.prepareAUAData(authData.getUid(), terminalID, authData.getDeviceMetaData(), (Object) PidCreator.createXmlPid(authData), DataType.X);
			}
			Tkn token = null;
			AuthRequestCreator authRequestCreator = new AuthRequestCreator();
			Auth auth = authRequestCreator.createAuthRequest(auaCode, serviceAgency, licenseKey, usesElement, token, auaData, authData.getDeviceMetaData());
			AuthResponseDetails data = authClient.authenticate(auth);
			AuthRes authResult = data.getAuthRes();
			System.out.println("The auth result is : " + authResult);
			String xmlResponse = generateResponse(authResult);
			if(xmlResponse.toLowerCase().charAt(0) == 'y')
				System.out.println("Aadhaar successfully authenticated");
			else
				System.out.println("Invalid aadhaar number / demographics information");

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Server not reachable");
			e.printStackTrace();
		}
		return response;
	}

	// Generates the staging certificate password hash
	private void loadPasswordHash() {
		FileInputStream is = null;
		try {
			File preferencesFile = new File("authclient.properties");
			if (preferencesFile.exists()) {
				is = new FileInputStream(preferencesFile);
				Properties p = new Properties();
				p.load(is);

				if (p.get("signatureAlias") != null) {
					System.out.println(p.getProperty("signatureAlias").toString());
					signatureAlias = p.getProperty("signatureAlias").toString();
				}

				if (p.get("signaturePassword") != null) {
					System.out.println(p.getProperty("signaturePassword").toString());
					signaturePassword = p.getProperty("signaturePassword").toString();
				}
				if (p.get("signKeyStore") != null) {
					System.out.println(p.getProperty("signKeyStore").toString());
					certificatePath = p.getProperty("signKeyStore").toString();
				}
				if(p.get("publicKeyFileDSIG") != null) {
					System.out.println(p.getProperty("publicKeyFileDSIG").toString());
					certificateDSIGKey = p.getProperty("publicKeyFileDSIG").toString();
				}
				if(p.get("asaLicenseKey") != null) {
					System.out.println(p.getProperty("asaLicenseKey").toString());
					signatureLicenseKey = p.getProperty("asaLicenseKey").toString();
				}
				if(p.get("authServerUrl") != null) {
					System.out.println(p.getProperty("authServerUrl").toString());
					authServerURL = p.getProperty("authServerUrl").toString();
				}
				if(p.get("udc") != null) {
					System.out.println(p.getProperty("udc").toString());
					udc = p.getProperty("udc").toString();
				}
				if(p.get("pincode") != null) {
					System.out.println(p.getProperty("pincode").toString());
					pincode = p.getProperty("pincode").toString();
				}
				if(p.get("terminalId") != null) {
					System.out.println(p.getProperty("terminalId").toString());
					terminalID = p.getProperty("terminalId").toString();
				}
				if(p.get("auaCode") != null) {
					System.out.println(p.getProperty("auaCode").toString());
					auaCode = p.getProperty("auaCode").toString();
				}
				if(p.get("sa") != null) {
					System.out.println(p.getProperty("sa").toString());
					serviceAgency = p.getProperty("sa").toString();
				}
				if(p.get("licenseKey") != null) {
					System.out.println(p.getProperty("licenseKey").toString());
					licenseKey = p.getProperty("licenseKey").toString();
				}
				if(p.get("publicKeyFile") != null) {
					System.out.println(p.getProperty("publicKeyFile").toString());
					publicKeyFile = p.getProperty("publicKeyFile").toString();
				}
			}

		} catch (IOException ex) {
			Logger.getLogger(SampleClientMainFrame.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException ex) {
				Logger.getLogger(SampleClientMainFrame.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
}

public class AadhaarRestClient {

	public static void main(String[] args) {
		RestClient restClient = new RestClient();
		restClient.authenticate("Hello", "World");
	}
}
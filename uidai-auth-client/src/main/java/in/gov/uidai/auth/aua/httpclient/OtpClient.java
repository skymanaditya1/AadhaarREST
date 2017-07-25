/*******************************************************************************
 * DISCLAIMER: The sample code or utility or tool described herein
 *    is provided on an "as is" basis, without warranty of any kind.
 *    UIDAI does not warrant or guarantee the individual success
 *    developers may have in implementing the sample code on their
 *    environment. 
 *    
 *    UIDAI does not warrant, guarantee or make any representations
 *    of any kind with respect to the sample code and does not make
 *    any representations or warranties regarding the use, results
 *    of use, accuracy, timeliness or completeness of any data or
 *    information relating to the sample code. UIDAI disclaims all
 *    warranties, express or implied, and in particular, disclaims
 *    all warranties of merchantability, fitness for a particular
 *    purpose, and warranties related to the code, or any service
 *    or software related thereto. 
 *    
 *    UIDAI is not responsible for and shall not be liable directly
 *    or indirectly for any direct, indirect damages or costs of any
 *    type arising out of use or any action taken by you or others
 *    related to the sample code.
 *    
 *    THIS IS NOT A SUPPORTED SOFTWARE.
 ******************************************************************************/
package in.gov.uidai.auth.aua.httpclient;

import in.gov.uidai.auth.aua.helper.DigitalSigner;
import in.gov.uidai.auth.device.model.AuthResponseDetails;
import in.gov.uidai.auth.device.model.BfdResponseDetails;
import in.gov.uidai.auth.device.model.OtpResponseDetails;
import in.gov.uidai.authentication.otp._1.Otp;
import in.gov.uidai.authentication.otp._1.OtpRes;
import in.gov.uidai.authentication.uid_auth_request._1.Auth;
import in.gov.uidai.authentication.uid_auth_response._1.AuthRes;
import in.gov.uidai.authentication.uid_bfd_request._1.Bfd;
import in.gov.uidai.authentication.uid_bfd_response._1.BfdRes;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

/**
 * <code>OtpClient</code> class can be used for submitting an OTP Generation request to
 * UIDAI OTP Server, and to get the response back
 * 
 * @author UIDAI
 *
 */
public class OtpClient {
	private URI otpServerURI = null;
	
	private String asaLicenseKey;
	private DigitalSigner digitalSignator;

	
	public OtpClient(URI otpServerURI) {
		this.otpServerURI = otpServerURI;
	}
	
	public OtpResponseDetails generateOtp(Otp otp) {
		try {
			System.out.println("Reaching inside the core generating OTP method");
			String signedXML = generateSignedOtpXML(otp);
			System.out.println("The signed XML file is : " + signedXML);
			System.out.println(signedXML);

			String uriString = otpServerURI.toString() + (otpServerURI.toString().endsWith("/") ? "" : "/")
					+ otp.getAc() + "/" + otp.getUid().charAt(0) + "/" + otp.getUid().charAt(1);
			
			if (StringUtils.isNotBlank(asaLicenseKey)) {
				uriString  = uriString + "/" + asaLicenseKey;
			}
			
			URI otpURI = new URI(uriString);
			System.out.println("This is the OTP URI String : " + uriString);
			System.out.println("The otpURI string is : " + otpURI.toString());

			/**WebResource webResource = Client.create(HttpClientHelper.getClientConfig(otpServerURI.getScheme())).resource(otpURI);

			// This is the line where its getting stuck
			// AUA adds headers -> REMOTE_ADDR
			// String q = "REMOTE_ADDR" + InetAddress.getLocalHost().getHostAddress();
			// System.out.println("The signature generated is : " + q);
			System.out.println(InetAddress.getLocalHost().getHostAddress());
			// webResource.header("REMOTE_ADDR", "10.5.87.189");
			// String responseXML = webResource.post(String.class, signedXML);
			
			// System.out.println("Signed XML : " + signedXML);
			// return new OtpResponseDetails(null, null);
			String responseXML = webResource.header("REMOTE_ADDR", InetAddress.getLocalHost().getHostAddress()).post(String.class,
					signedXML);
			// Response XML is returned here
			
			System.out.println("Stopping here");
			System.out.println("Returning the response XML as : " + responseXML);
			return new OtpResponseDetails(responseXML, parseOtpResponseXML(responseXML));*/
			return null;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Exception during OTP generation " + e.getMessage(), e);
		}

	}

	private String generateSignedOtpXML(Otp otp) throws JAXBException, Exception {
		StringWriter otpXML = new StringWriter();

		JAXBElement element = new JAXBElement(new QName(
				"http://www.uidai.gov.in/authentication/otp/1.0", "Otp"), Otp.class, otp);

		JAXBContext.newInstance(Otp.class).createMarshaller().marshal(element, otpXML);
		boolean includeKeyInfo = true;

		if(System.getenv().get("SKIP_DIGITAL_SIGNATURE") != null) {
			return otpXML.toString();
		} else {
			return this.digitalSignator.signXML(otpXML.toString(), includeKeyInfo);
		}
	}

	private OtpRes parseOtpResponseXML(String xmlToParse) throws JAXBException {
		 
		//Create an XMLReader to use with our filter 
		try {
			//Prepare JAXB objects 
			JAXBContext jc = JAXBContext.newInstance(OtpRes.class); 
			Unmarshaller u = jc.createUnmarshaller(); 

			XMLReader reader;
			reader = XMLReaderFactory.createXMLReader();

			//Create the filter (to add namespace) and set the xmlReader as its parent. 
			NamespaceFilter inFilter = new NamespaceFilter("http://www.uidai.gov.in/authentication/otp/1.0", true); 
			inFilter.setParent(reader); 
			 
			//Prepare the input, in this case a java.io.File (output) 
			InputSource is = new InputSource(new StringReader(xmlToParse)); 
			 
			//Create a SAXSource specifying the filter 
			SAXSource source = new SAXSource(inFilter, is); 
			 
			//Do unmarshalling 
			OtpRes res = u.unmarshal(source, OtpRes.class).getValue(); 
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Erorr while parsing response XML" + e.getMessage());
		}
	}
	
	
	/**
	 * Method to inject an instance of <code>DigitalSigner</code> class.
	 * @param digitalSignator
	 */
	public void setDigitalSignator(DigitalSigner digitalSignator) {
		this.digitalSignator = digitalSignator;
	}

	public void setAsaLicenseKey(String asaLicenseKey) {
		this.asaLicenseKey = asaLicenseKey;
	}
	
}

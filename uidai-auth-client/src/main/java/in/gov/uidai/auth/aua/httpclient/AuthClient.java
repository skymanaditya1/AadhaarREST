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
 * <code>AuthClient</code> class can be used for submitting an Authentication
 * request to UIDAI Auth Server, and to get the response back. Given an
 * <code>Auth</code> object, this class (@see {@link AuthClient#authenticate})
 * will convert it to XML string, then, digitally sign it, and submit it to
 * UIDAI Auth Server using HTTP POST message. After, receiving the response,
 * this class converts the response XML into authentication response
 * 
 * @see AuthRes object
 * 
 * 
 * @author UIDAI
 *
 */
public class AuthClient {
	private URI authServerURI = null;

	private String asaLicenseKey;
	private DigitalSigner digitalSignator;

	/**
	 * Constructor
	 * 
	 * @param authServerUri
	 *            - URI of the authentication server
	 */
	public AuthClient(URI authServerUri) {
		this.authServerURI = authServerUri;
		System.out.println("The authServerURI set inside the constructor is : " + this.authServerURI.toString());
	}

	/**
	 * Method to perform authentication
	 * 
	 * @param auth
	 *            Authentication request
	 * @return Authentication response
	 */
	public AuthResponseDetails authenticate(Auth auth) {
		try {
			String signedXML = generateSignedAuthXML(auth);
			System.out.println(signedXML);

			String uriString = this.authServerURI.toString() + (authServerURI.toString().endsWith("/") ? "" : "/")
					+ auth.getAc() + "/" + auth.getUid().charAt(0) + "/" + auth.getUid().charAt(1);
			System.out.println("The REST API call made from the AuthClient class is : " + uriString);
			System.out.println("The authServerURI string is : " + authServerURI.toString());
			// System.out.println("The REST call is : " + uriString);

			if (StringUtils.isNotBlank(asaLicenseKey)) {
				uriString = uriString + "/" + asaLicenseKey;
				System.out.println("Added asaLicenseKey : " + asaLicenseKey);
			}

			System.out.println("The REST call is : " + uriString);

			URI authServiceURI = new URI(uriString);
			System.out.println("The REST Call is : " + uriString);
			// Preventing the authentication request from being sent
			WebResource webResource = Client.create(HttpClientHelper.getClientConfig(authServerURI.getScheme()))
					.resource(authServiceURI);

			String responseXML = webResource.header("REMOTE_ADDR", InetAddress.getLocalHost().getHostAddress())
					.post(String.class, signedXML);

			System.out.println(responseXML);

			return new AuthResponseDetails(responseXML, parseAuthResponseXML(responseXML));
			// return null;

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Exception during authentication " + e.getMessage(), e);
		}
	}

	private String generateSignedAuthXML(Auth auth) throws JAXBException, Exception {
		StringWriter authXML = new StringWriter();

		@SuppressWarnings({ "rawtypes", "unchecked" })
		JAXBElement authElement = new JAXBElement(
				new QName("http://www.uidai.gov.in/authentication/uid-auth-request/1.0", "Auth"), Auth.class, auth);

		JAXBContext.newInstance(Auth.class).createMarshaller().marshal(authElement, authXML);
		boolean includeKeyInfo = true;

		if (System.getenv().get("SKIP_DIGITAL_SIGNATURE") != null) {
			return authXML.toString();
		} else {
			return this.digitalSignator.signXML(authXML.toString(), includeKeyInfo);
		}
	}

	private AuthRes parseAuthResponseXML(String xmlToParse) throws JAXBException {

		// Create an XMLReader to use with our filter
		try {
			// Prepare JAXB objects
			JAXBContext jc = JAXBContext.newInstance(AuthRes.class);
			Unmarshaller u = jc.createUnmarshaller();

			XMLReader reader;
			reader = XMLReaderFactory.createXMLReader();

			// Create the filter (to add namespace) and set the xmlReader as its parent.
			NamespaceFilter inFilter = new NamespaceFilter(
					"http://www.uidai.gov.in/authentication/uid-auth-response/1.0", true);
			inFilter.setParent(reader);

			// Prepare the input, in this case a java.io.File (output)
			InputSource is = new InputSource(new StringReader(xmlToParse));

			// Create a SAXSource specifying the filter
			SAXSource source = new SAXSource(inFilter, is);

			// Do unmarshalling
			AuthRes res = u.unmarshal(source, AuthRes.class).getValue();
			return res;
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Method to inject an instance of <code>DigitalSigner</code> class.
	 * 
	 * @param digitalSignator
	 */
	public void setDigitalSignator(DigitalSigner digitalSignator) {
		this.digitalSignator = digitalSignator;
	}

	public void setAsaLicenseKey(String asaLicenseKey) {
		this.asaLicenseKey = asaLicenseKey;
	}

}

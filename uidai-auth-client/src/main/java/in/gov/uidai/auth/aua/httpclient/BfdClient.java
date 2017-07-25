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
 * <code>AuthClient</code> class can be used for submitting an Authentication request to
 * UIDAI Auth Server, and to get the response back.  Given an <code>Auth</code> object, this
 * class (@see {@link BfdClient#authenticate}) will convert it to XML string, then, 
 * digitally sign it, and submit it to UIDAI Auth Server using HTTP POST message.  After, 
 * receiving the resonse, this class converts the response XML into authentication response
 * @see AuthRes object  
 * 
 * 
 * @author UIDAI
 *
 */
public class BfdClient {

	private URI bfdServerURI = null;
	
	private String asaLicenseKey;
	private DigitalSigner digitalSignator;

	public BfdClient(URI bfdServerURI) {
		this.bfdServerURI = bfdServerURI;
	}
	
	
	
	public BfdResponseDetails performBfd(Bfd bfd) {
		try {
			String signedXML = generateSignedBfdXML(bfd);
			System.out.println(signedXML);

			
			String uriString = bfdServerURI.toString() + (bfdServerURI.toString().endsWith("/") ? "" : "/")
			+ bfd.getAc() + "/" + bfd.getUid().charAt(0) + "/" + bfd.getUid().charAt(1);
			
			if (StringUtils.isNotBlank(asaLicenseKey)) {
				uriString  = uriString + "/" + asaLicenseKey;
			}

			URI authServiceURI = new URI(uriString);
		
			WebResource webResource = Client.create(HttpClientHelper.getClientConfig(bfdServerURI.getScheme())).resource(authServiceURI);

			String responseXML = webResource.header("REMOTE_ADDR", InetAddress.getLocalHost().getHostAddress()).post(String.class,
					signedXML);
			
			System.out.println(responseXML);
			
			return new BfdResponseDetails(responseXML, parseBfdResponseXML(responseXML));
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Exception during authentication " + e.getMessage(), e);
		}
	}


	private String generateSignedBfdXML(Bfd bfd) throws JAXBException, Exception {
		StringWriter bfdXML = new StringWriter();

		JAXBElement bfdElement = new JAXBElement(new QName(
				"http://www.uidai.gov.in/authentication/uid-bfd-request/1.0", "Bfd"), Bfd.class, bfd);
		
		JAXBContext.newInstance(Bfd.class).createMarshaller().marshal(bfdElement, bfdXML);
		boolean includeKeyInfo = true;

		if(System.getenv().get("SKIP_DIGITAL_SIGNATURE") != null) {
			return bfdXML.toString();
		} else {
			return this.digitalSignator.signXML(bfdXML.toString(), includeKeyInfo);
		}
	}
	
	private BfdRes parseBfdResponseXML(String xmlToParse) throws JAXBException {
		 
		//Create an XMLReader to use with our filter 
		try {
			//Prepare JAXB objects 
			JAXBContext jc = JAXBContext.newInstance(BfdRes.class); 
			Unmarshaller u = jc.createUnmarshaller(); 

			XMLReader reader;
			reader = XMLReaderFactory.createXMLReader();

			//Create the filter (to add namespace) and set the xmlReader as its parent. 
			NamespaceFilter inFilter = new NamespaceFilter("http://www.uidai.gov.in/authentication/uid-bfd-response/1.0", true); 
			inFilter.setParent(reader); 
			 
			//Prepare the input, in this case a java.io.File (output) 
			InputSource is = new InputSource(new StringReader(xmlToParse)); 
			 
			//Create a SAXSource specifying the filter 
			SAXSource source = new SAXSource(inFilter, is); 
			 
			//Do unmarshalling 
			BfdRes res = u.unmarshal(source, BfdRes.class).getValue(); 
			return res;
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
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
